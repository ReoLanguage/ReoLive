package widgets.feta

import common.widgets.{Box, OutputArea}
import fta.{DSL, Specification, TeamLogic}
import ifta.{DSL => FDSL}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import widgets.RemoteBox

class SafetyReqBox(code: Box[String], errorBox: OutputArea)
  extends Box[Unit]("Safety Requirements Characterisation", List(code)) {


  protected var box: Block = _
  protected var spec: Specification = _

  override def get: Unit = ()

  /**
   * Executed once at creation time, to append the content to the inside of this box
   *
   * @param div     Placeholder that will receive the "append" with the content of the box
   * @param visible is true when this box is initially visible (i.e., expanded).
   */
  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible, buttons = List())
      .append("div")
      .attr("id", "safetyReqBox")
      .style("margin", "10px")
    dom.document.getElementById("Safety Requirements Characterisation").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { e: MouseEvent => if (!isVisible) show() }
  }


  /**
   * Block of code that should read the dependencies and:
   *  - update its output value, and
   *  - produce side-effects (e.g., redraw a diagram)
   */
  override def update(): Unit = if (isVisible) show()

  def show(): Unit =
    try {
      spec = DSL.parse(code.get)
      val fm = spec.fm
      val fmInfo =
        s"""{ "fm":     "${fm.simplify.toString}", """ +
          s"""  "feats":  "${spec.fcas.flatMap(f => f.features).mkString("(", ",", ")")}" }"""

      RemoteBox.remoteCall("ifta", fmInfo, showInfo)

    } catch Box.checkExceptions(errorBox)

  def showInfo(data: String): Unit = {
    val products = FDSL.parseProducts(data)
    val feta = DSL.interpretInServer(spec, products)

    box.text("")

    box.append("p")
      .append("strong")
      .text(s"Receptiveness: \n")
    box.append("pre")
      .text(TeamLogic.getReceptivenesReq(feta.s, feta.fst).toString)
  }
}