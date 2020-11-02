package common.widgets.choreo

import choreo.DSL
import choreo.backend.Mermaid._
import choreo.backend.MermaidChoreography._
import common.Utils
import common.widgets.{Box, OutputArea}

/**
  * Created by guillecledou on 02/11/2020
  */


class ChoreographyBox(choreo: Box[String], errorBox: OutputArea)
  extends Box[String]("Sequence diagram of the choreography", List(choreo)) {

  val sequenceChart:String = ""
  private var box:Block = _

  override def get: String = sequenceChart

  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div     Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div: Block, visible: Boolean): Unit =
    box = panelBox(div, visible,buttons=List(
      //Right("download")-> (()=>Utils.downloadSvg("choreographyBox"), "Download model as a TA in Uppaal")
    )).append("div")
    .attr("class","mermaid")
    .attr("id", "choreographyBox")
    .append("div").attr("id","disposableDiv")


  /**
      * Block of code that should read the dependencies and:
      *  - update its output value, and
      *  - produce side-effects (e.g., redraw a diagram)
      */
  override def update(): Unit = {
      try {
        val (choreography,_) = DSL.parseAndValidate(choreo.get)
        val mermaid = choreography.toMermaid
//        box.text(mermaid)
        val initMermaid =
          s"""
            |  var display = document.getElementById('choreographyBox');
            |  var text = `
            |    ${mermaid}
            |  `
            |  var graph = mermaid.mermaidAPI.render('disposableDiv', text, function(svgCode){ display.innerHTML = svgCode});
            """.stripMargin
        scalajs.js.eval(initMermaid)
      } catch Box.checkExceptions(errorBox)
    }

}
