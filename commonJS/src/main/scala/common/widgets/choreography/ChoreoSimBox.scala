package common.widgets.choreography

import choreo.choreo2.analysis.Global
import choreo.choreo2.analysis.pomsets.GlobalPom
import choreo.choreo2.syntax.{Choreo}
import choreo.choreo2.syntax.Choreo.{Action, Tau}
import choreo.choreo2.view.MermaidPomset
import common.frontend.MermaidJS
import common.widgets.{Box, OutputArea}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}

/**
 * Created by guillecledou on 16/03/2021
 */

class ChoreoSimBox(choreoInstance: Box[Choreo], errorBox: OutputArea)
  extends Box[Unit]("Choreo Simulation", List(choreoInstance)) {

  private var container:Block = _
  private var left:Block = _
  private var right:Block = _
  private var top:Block = _

  override def get: Unit = ()

  protected var trace:List[Action] = List()
  protected var lastChoreo:Choreo = _
  protected var stepsChoreos:List[Choreo] = List()

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
    dom.document.getElementById("Choreo Simulation").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if(!isVisible) initialise() }
    top = box
      .append("div")
      .style("width:100%;margin-bottom:10px;margin:5px 1px 5px 15px")

    val goBack = box.append("div").style("padding","5px 1px 5px 15px")
      .append("button")
      .textEl("undo")
      .on("click",() => undo())

    container = box.append("div")
      .style("display", "flex")
      .style("justify-content", "flex-start")
      .style("padding","5px 1px 5px 15px")

    left = container.append("div")
      .style("width:15%; border-right-style:solid;border-right-width:1px;border-right-color: #ddd;")

    right = container.append("div")
      .style("display:inline; width:100%;")
    right.append("div")
      .attr("id", "choreoSimBox")
      .style("text-align", "left")
  }
  /**
   * Block of code that should read the dependencies and:
   *  - update its output value, and
   *  - produce side-effects (e.g., redraw a diagram)
   */
  override def update(): Unit =
    if(isVisible) initialise()

  def initialise():Unit = try {
    val c = choreoInstance.get//DSL.pomset(choreography)
    initialiseWith(c,Nil,c::Nil)
  } catch Box.checkExceptions(errorBox)

  def initialiseWith(c:Choreo, t:List[Action], s:List[Choreo]):Unit = {
    lastChoreo = c
    trace = t
    stepsChoreos = s
    updateEnabledActions(lastChoreo)
    updateSimulationSteps((None::(trace.map(Some(_)))).zip(stepsChoreos))
    //updateSimulation((None,lastChoreo)::Nil)
  }

  protected def undo():Unit =
    if (trace.size<=1) initialise()
    else initialiseWith(stepsChoreos.init.last,trace.init,stepsChoreos.init)

  protected def takeStep(a:Action,goesTo:Choreo):Unit = {
    lastChoreo = goesTo
    stepsChoreos :+= goesTo
    if (a!=Tau) trace :+=a
    updateSimulationSteps((None::(trace.map(Some(_)))).zip(stepsChoreos))
    updateEnabledActions(goesTo)
  }

  def updateEnabledActions(c: Choreo):Unit = {
    showTrace()
    showEnabled(c)
  }

  def showTrace():Unit = {
    top.text("")
    top.append("span").style("font-weight:bold;").textEl("Trace:")
      .append("span").style("font-weight:normal")
      .text(s""" ${trace.mkString(", ")}""")
  }

  def showEnabled(from:Choreo):Unit = {
    left.html("")
    var enabled = Global.nextChoreo(from).toSet
    if (Global.canSkip(from)) //todo: && !from.isFinal then
      enabled += ((Tau,Choreo.End))

    val ul = left.append("ul")
      .style("list-style-type:none;padding:0;margin:0;")//.attr("class", "list-group list-group-flush")
    ul.append("li")
      .append("span").style("font-weight:bold;").textEl("Enabled transitions:")

    for ((a,p)<-enabled) {
      val li = ul.append("li")
      val b = li.append("button").attr("title",p.toString)
        .textEl(if (a == Tau) "terminate" else a.toString)
      b.on("click", () => { takeStep(a,p)})
    }
  }

  protected def updateSimulationSteps(sim: List[(Option[Action],Choreo)]):Unit = {
    right.text("")
    right.html(sim.map(s => showStep(s)).mkString(""))
  }

  protected def showStep(step:(Option[Action],Choreo)):String =
    s"""<div style="display:flex;justify-content:flex-start;padding:0px 1px 5px 15px;">
       |  ${showActionStep(step._1)}
       |  ${showChoreoStep(step._2)}
       |</div>""".stripMargin

  protected def showChoreoStep(c:Choreo):String =
    s"""<div style="display:inline;width:100%;text-align:left;">
       |${htmlChoreo(c)}
       |</div>""".stripMargin

  protected def showActionStep(a:Option[Action]):String = {
   s"""<div style="text-align:left;width:15%;font-weight:bold;">
       |  ${if (a.isDefined)  s"""${a.get} &#8594;""" else "&#8594;"}
       |</div>""".stripMargin
  }

  protected def htmlChoreo(c:Choreo):String =
    c.toString.replace("->","&#8594;")

}

