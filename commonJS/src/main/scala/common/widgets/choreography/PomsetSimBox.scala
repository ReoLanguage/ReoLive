package common.widgets.choreography

import common.widgets.{Box, OutputArea}
import choreo.choreo2.DSL
import choreo.choreo2._
import choreo.choreo2.analysis.pomsets.Pomset
import choreo.choreo2.analysis.pomsets.GlobalPom
import choreo.choreo2.view.MermaidPomset
/**
 * Created by guillecledou on 12/02/2021
 */

class PomsetSimBox(choreocode: Box[String], errorBox: OutputArea)
  extends Box[Unit]("Pomset Simulation", List(choreocode)) {

  private var box:Block = _

  override def get: Unit = ()

  protected var pomset:Pomset = _

  /**
   * Executed once at creation time, to append the content to the inside of this box
   *
   * @param div     Placeholder that will receive the "append" with the content of the box
   * @param visible is true when this box is initially visible (i.e., expanded).
   */
  override def init(div: Block, visible: Boolean=false): Unit =
    box = panelBox(div, visible, buttons = List(
      Right("refresh") -> (() =>
        update(), "Simulate next actions of current nested pomset")
      ))//.append("div")
       // .attr("id", "choreoSimBox")

  /**
   * Block of code that should read the dependencies and:
   *  - update its output value, and
   *  - produce side-effects (e.g., redraw a diagram)
   */
  override def update(): Unit =
    try {
      val choreography = DSL.parse(choreocode.get)
      pomset = DSL.pomset(choreography)
      showNexts()
    } catch Box.checkExceptions(errorBox)

  protected def showNexts():Unit = try {
    box.text("")
    errorBox.clear()

    // todo: pomset.trans not recognized but supposedly supported
    val enabled = GlobalPom.nextPom(pomset)
    for ((a,p)<-enabled) {
      val b = box.append("button").textEl(a.toString)
      b.on("click", () => {pomset = p; this.showNexts(); this.showPom(p)}) //: b.DatumFunction[U
    }
  } catch Box.checkExceptions(errorBox)

  protected def showPom(p:Pomset):Unit = try {
    box.append("div")
      .attr("class","mermaid")
      .attr("id", "pomsetSimBox")
      .style("text-align","center")
      .append("div").attr("id","svgSimPomset")

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
