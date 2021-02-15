package common.widgets.choreography

import common.Utils
import common.widgets.{Box, OutputArea}
import choreo.choreo2.DSL
import choreo.choreo2.view.SequenceChart
import choreo.choreo2.syntax._
import choreo.choreo2.syntax.Choreo
import choreo.choreo2.syntax.Choreo._
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}

/**
  * Created by guillecledou on 02/11/2020
  */


class ChoreographyBox(choreography: Box[Choreo], errorBox: OutputArea)
  extends Box[String]("Sequence diagram of the choreography", List(choreography)) {

  val sequenceChart:String = ""
  private var box:Block = _

  override def get: String = sequenceChart

  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div     Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible,buttons=List(
      Right("download")-> (() => Utils.downloadSvg("svgChoreography"), "Download SVG")
    )).append("div")
    .attr("class","mermaid")
    .attr("id", "choreographyBox")
      .style("text-align","center")
    .append("div").attr("id","svgChoreography")

    dom.document.getElementById("Sequence diagram of the choreography").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if(!isVisible) showChoreo() }
  }

  /**
      * Block of code that should read the dependencies and:
      *  - update its output value, and
      *  - produce side-effects (e.g., redraw a diagram)
      */
  override def update(): Unit = if(isVisible) showChoreo()

  def showChoreo():Unit = {
      try {
        val mermaid = SequenceChart(choreography.get)
        val initMermaid =
          s"""
            |  var display = document.getElementById('choreographyBox');
            |  var text = `
            |    ${mermaid}
            |  `
            |  var graph = mermaid.mermaidAPI.render('svgChoreography', text, function(svgCode){ display.innerHTML = svgCode});
            """.stripMargin
        scalajs.js.eval(initMermaid)
      } catch Box.checkExceptions(errorBox)
    }

}
