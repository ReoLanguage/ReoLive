package common.widgets.choreography

import common.widgets.{Box, OutputArea}
import choreo.choreo2.DSL
import choreo.choreo2._
import choreo.choreo2.analysis.pomsets.Pomset
import choreo.choreo2.analysis.pomsets.GlobalPom
import choreo.choreo2.view.MermaidPomset
import choreo.choreo2.syntax.Choreo._
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}

/**
 * Created by guillecledou on 12/02/2021
 */

class PomsetSimBox(pomInstance: Box[Pomset], errorBox: OutputArea)
  extends Box[Unit]("Nested Pomset Simulation", List(pomInstance)) {

  private var container:Block = _
  private var left:Block = _
  private var right:Block = _
  private var top:Block = _

  override def get: Unit = ()

  protected var pomset:Pomset = _
  //protected var steps:List[(Set[(Action,Pomset)])] = List()
  protected var steps:List[Pomset] = List()
  protected var trace:List[Action] = List()

  /**
   * Executed once at creation time, to append the content to the inside of this box
   *
   * @param div     Placeholder that will receive the "append" with the content of the box
   * @param visible is true when this box is initially visible (i.e., expanded).
   */
  override def init(div: Block, visible: Boolean=false): Unit = {
    val box = panelBox(div, visible, buttons = List(
      Right("refresh") -> (() =>
        update(), "Simulate next actions of current nested pomset")
    ))
    dom.document.getElementById("Nested Pomset Simulation").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if(!isVisible) showTraces() }
    top = box
      .append("div")
      .style("width:100%;margin-bottom:10px;margin:5px 1px 5px 15px")

    val goBack = box.append("div").style("padding","5px 1px 5px 15px")
      .append("button")
      .textEl("undo")
      .on("click",() =>
        if (steps.size>1) {
          pomset = steps.init.last
          steps = steps.init
          trace = trace.init
          showNexts()
          showPom(pomset)
        }
        else showTraces()
        )

    container = box.append("div")
      .style("display", "flex")
      .style("justify-content", "flex-start")
      .style("padding","5px 1px 5px 15px")

    left = container.append("div")
      .style("width:15%; border-right-style:solid;border-right-width:1px;border-right-color: #ddd;")

    right = container.append("div")
      .style("display:inline; width:100%;")
    right.append("div")
      .attr("class", "mermaid")
      .attr("id", "pomsetSimBox")
      .style("text-align", "center")
      .append("div").attr("id", "svgSimPomset")
  }
  /**
   * Block of code that should read the dependencies and:
   *  - update its output value, and
   *  - produce side-effects (e.g., redraw a diagram)
   */
  override def update(): Unit = if(isVisible) showTraces()

  def showTraces():Unit = try {
    //val choreography = DSL.parse(pomInstance.get)
    pomset = pomInstance.get//DSL.pomset(choreography)
    trace = Nil
    steps = pomset::Nil
    showNexts()
    showPom(pomset)
  } catch Box.checkExceptions(errorBox)

  protected def showNexts():Unit = try {
    left.html("")
    top.text("")
    top.append("span").style("font-weight:bold;").textEl("Trace:")
      .append("span").style("font-weight:normal")
      .text(s""" ${trace.mkString(", ")}""")
    //errorBox.clear()
    var enabled = GlobalPom.nextPom(pomset)
    if (GlobalPom.isTerminating(pomset) && !GlobalPom.isFinal(pomset))
      enabled += ((Tau,GlobalPom.terminate(pomset)))
    //steps :+= enabled
    val ul = left.append("ul")
      .style("list-style-type:none;padding:0;margin:0;")//.attr("class", "list-group list-group-flush")
    ul.append("li")
      .append("span").style("font-weight:bold;").textEl("Enabled transitions:")
    for ((a,p)<-enabled) {
      val li = ul.append("li")
        //.attr("class", "list-group-item")

      val b = li.append("button").textEl(if (a == Tau) "terminate" else a.toString)
      b.on("click", () => {
        pomset = p
        steps :+= p
        if (a!=Tau) trace :+=a
        showNexts()
        showPom(p)
      })
    }
  } catch Box.checkExceptions(errorBox)

  protected def showPom(p:Pomset):Unit = try {
    val mermaid = MermaidPomset(p)
    val initMermaid =
      s"""
         |  var display = document.getElementById('pomsetSimBox');
         |  var text = `
         |    ${mermaid}
         |  `
         |  var graph = mermaid.mermaidAPI.render('svgSimPomset', text, function(svgCode){ display.innerHTML = svgCode});
         |
            """.stripMargin
    scalajs.js.eval(initMermaid)
  } catch Box.checkExceptions(errorBox)

}
