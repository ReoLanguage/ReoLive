package reolive

import common.DomNode
import common.widgets.OutputArea
import common.widgets.choreography._
import org.scalajs.dom.html

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * Created by guillecledou on 02/11/2020
  */


object Choreo {

  var errorArea:OutputArea = _
  var descriptionArea: OutputArea = _
  var choreo:ChoreoBox = _
  var choreoInstance:ChoreoInstantiate = _
  var pomsetInstance:PomsetInstantiate = _
  var choreographyBox:ChoreographyBox = _
  var exampleBox:ChoreoExamplesBox = _
  var pomsetBox:PomsetBox = _
  var pomsetSimBox:PomsetSimBox = _
//  var pomsetCytoBox:PomsetCytoBox = _

  @JSExportTopLevel("reolive_Choreo_main")
  def main(content: html.Div): Unit = {

    val defaultChoreo =
      """a->b:x"""
        .stripMargin

    // Creating outside containers:
    val contentDiv = DomNode.select(content).append("div")
      .attr("class", "content")

    val rowDiv = contentDiv.append("div")
      //      .attr("class", "row")
      .attr("id", "mytable")

    val leftColumn = rowDiv.append("div")
      //      .attr("class", "col-sm-4")
      .attr("id", "leftbar")
      .attr("class", "leftside")

    leftColumn.append("div")
      .attr("id", "dragbar")
      .attr("class", "middlebar")

    val rightColumn = rowDiv.append("div")
      //      .attr("class", "col-sm-8")
      .attr("id", "rightbar")
      .attr("class", "rightside")

    descriptionArea = new OutputArea
    errorArea = new OutputArea
    choreo = new ChoreoBox(reload(),defaultChoreo,errorArea)
    choreoInstance = new ChoreoInstantiate(choreo,errorArea)
    pomsetInstance = new PomsetInstantiate(choreoInstance,errorArea)
    choreographyBox = new ChoreographyBox(choreoInstance,errorArea)
    exampleBox = new ChoreoExamplesBox(softReload(),List(choreo,descriptionArea))
    pomsetBox = new PomsetBox(pomsetInstance,errorArea)
    pomsetSimBox = new PomsetSimBox(pomsetInstance,errorArea)
//    pomsetCytoBox = new PomsetCytoBox(choreo,errorArea)


    choreo.init(leftColumn, true)
    errorArea.init(leftColumn)
    descriptionArea.init(leftColumn)
    exampleBox.init(leftColumn,true)
    choreographyBox.init(rightColumn, true)
//    pomsetCytoBox.init(rightColumn,true)
    pomsetBox.init(rightColumn,true)
    pomsetSimBox.init(rightColumn)


    common.Utils.moreInfo(rightColumn,"https://github.com/arcalab/choreo")

    // load default button
    if (exampleBox.loadButton("ex4")) {
      softReload()
    }
  }

  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def reload(): Unit = {
    descriptionArea.clear()
    softReload()
  }

  private def softReload(): Unit = {
    errorArea.clear()
    choreo.update()
    choreoInstance.update()
    pomsetInstance.update()
    choreographyBox.update()
    pomsetBox.update()
//    pomsetCytoBox.update()
    pomsetSimBox.update()
  }

}
