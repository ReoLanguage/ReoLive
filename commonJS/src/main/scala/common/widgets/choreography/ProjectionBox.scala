package common.widgets.choreography

import common.widgets.{Box, OutputArea}
import common.frontend.MermaidJS
import choreo.choreo2.view.MermaidPomset
import choreo.choreo2.DSL
import choreo.choreo2._
import choreo.choreo2.analysis.pomsets.Pomset
import choreo.choreo2.syntax.Agent
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}

/**
 * Created by guillecledou on 15/02/2021
 */

class ProjectionBox(pomInstance: Box[Pomset], errorBox: OutputArea)
  extends Box[String]("Projections", List(pomInstance)) {

  val pomset:String = ""
  private var box:Block = _

  override def get: String = pomset

  /**
   * Executed once at creation time, to append the content to the inside of this box
   *
   * @param div     Placeholder that will receive the "append" with the content of the box
   * @param visible is true when this box is initially visible (i.e., expanded).
   */
  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible,buttons=List())
      .append("div")
      //.style("display", "flex")
      //.style("justify-content", "flex-start")
      .style("padding","5px 1px 5px 15px")
    dom.document.getElementById("Projections").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if(!isVisible) showPom() }
  }


  /**
   * Block of code that should read the dependencies and:
   *  - update its output value, and
   *  - produce side-effects (e.g., redraw a diagram)
   */
  override def update(): Unit = if(isVisible) showPom()

  def showPom():Unit =
    try {
      box.text("")
      val pom = pomInstance.get
      pom.agents.map(a=>showProj(a,MermaidPomset(pom.project(a))))
    } catch Box.checkExceptions(errorBox)

  def showProj(agent:Agent,mermaid:String):Unit = {
    val mbox = box.append("div")
      .style("text-align","center")
    mbox.append("h4")
      .text(s"${agent.s}")
    mbox.append("div")
      .attr("class","mermaid")
      .attr("id", s"pomsetBox${agent.s}")
      .style("text-align","center")
      .append("div").attr("id",s"svgPomset${agent.s}")

    val mermaidJs = MermaidJS(mermaid,s"pomsetBox${agent.s}",s"svgPomset${agent.s}")
    scalajs.js.eval(mermaidJs)
  }

}