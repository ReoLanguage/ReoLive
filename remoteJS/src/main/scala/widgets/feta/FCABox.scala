package widgets.feta

import common.frontend.MermaidJS
import common.widgets.{Box, OutputArea}
import fta.DSL
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import widgets.RemoteBox

/**
 * Created by guillecledou on 03/05/2021
 */

class FCABox(code: Box[String], errorBox: OutputArea)
  extends Box[Unit]("(F)CA", List(code)) {


  private var box:Block = _

  override def get: Unit = ()

  /**
   * Executed once at creation time, to append the content to the inside of this box
   *
   * @param div     Placeholder that will receive the "append" with the content of the box
   * @param visible is true when this box is initially visible (i.e., expanded).
   */
  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible,buttons=List())
      .append("div")
      .style("display", "flex")
      .style("flex-flow","row wrap")
      .style("align-items","baseline")
      .style("align-content","flex-start")
      //.style("justify-content", "flex-start")
      .style("padding","5px 1px 5px 15px")
    dom.document.getElementById(title).firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if(!isVisible) showFCA() }
  }


  /**
   * Block of code that should read the dependencies and:
   *  - update its output value, and
   *  - produce side-effects (e.g., redraw a diagram)
   */
  override def update(): Unit = if(isVisible) showFCA()

  def showFCA():Unit =
    try {
      box.text("")
      val spec = DSL.parse(code.get)
      //spec.fcas.map(f=>println(DSL.toMermaid(f)))
      spec.fcas.map(fca=>showOne(fca.name,DSL.toMermaid(fca)))
    } catch Box.checkExceptions(errorBox)

  def showOne(name:String,mermaid:String):Unit = {
    val mbox = box.append("div")
      .style("text-align","center")
    mbox.append("h4")
      .text(s"${name}")
    mbox.append("div")
      .attr("class","mermaid")
      .attr("id", s"pomsetBox${name}")
      .style("text-align","center")
      .append("div").attr("id",s"svgPomset${name}")

    val mermaidJs = MermaidJS(mermaid,s"pomsetBox${name}",s"svgPomset${name}")
    scalajs.js.eval(mermaidJs)
  }

}
