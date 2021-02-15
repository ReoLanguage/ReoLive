package common.widgets.choreography

import choreo.choreo2.analysis.Bisimulation
import choreo.choreo2.syntax._
import choreo.choreo2.syntax.Choreo
import choreo.choreo2.syntax.Choreo._
import common.widgets.{Box, OutputArea}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}

/**
 * Created by guillecledou on 15/02/2021
 */

class BisimBox(choreography: Box[Choreo], errorBox: OutputArea)
  extends Box[Unit]("Realisability via weak bisimulation", List(choreography)) {

  protected var box:Block = _
  override def get: Unit = ()

  /**
   * Executed once at creation time, to append the content to the inside of this box
   *
   * @param div     Placeholder that will receive the "append" with the content of the box
   * @param visible is true when this box is initially visible (i.e., expanded).
   */
  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible, buttons = List(
      Right("refresh") -> (() =>
        update(), "Check realisability")
    )).append("div")
      .style("width:100%;margin-bottom:10px;margin:5px 1px 5px 15px")
    dom.document.getElementById("Realisability via weak bisimulation").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { e: MouseEvent => if (!isVisible) check() }
  }

  /**
   * Block of code that should read the dependencies and:
   *  - update its output value, and
   *  - produce side-effects (e.g., redraw a diagram)
   */
  override def update(): Unit = if (isVisible) check()

  def check():Unit = {
    box.text("")
    val bisim = Bisimulation.findWBisim2(choreography.get)
    bisim match {
      case Left(e) =>
        box.append("span")
          .style("color:red;font-weight:bold;")
          .text("Not realisable:")
        box.append("div")
          .text(e.msg.mkString("\n"))
      case Right(b) =>
        box.append("span")
          .style("color:green;font-weight:bold;")
          .text("Realisable")
    }
  }
}
