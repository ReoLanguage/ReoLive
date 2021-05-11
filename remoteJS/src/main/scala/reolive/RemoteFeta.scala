package reolive

import common.DomNode
import common.widgets.OutputArea
import widgets.feta.{FCABox, FETAInfoBox, FetaBox, FetaExamplesBox, FetaGraphBox}
import org.scalajs.dom.html

import scala.scalajs.js.annotation.JSExportTopLevel

/**
 * Created by guillecledou on 21/04/2021
 */

object RemoteFeta {

  var errorArea: OutputArea = _
  var descriptionArea: OutputArea = _
  var fetaBox: FetaBox = _
  var fetaGraphBox: FetaGraphBox = _
  var examples:FetaExamplesBox = _
  var fcaBox: FCABox = _
  var fetaInfo:FETAInfoBox = _

  @JSExportTopLevel("reolive_RemoteFeta_main")
  def main(content: html.Div): Unit = {


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
    fetaBox = new FetaBox(reload(), "", errorArea)
    fetaGraphBox = new FetaGraphBox(fetaBox, errorArea)
    examples = new FetaExamplesBox(softReload(),List(fetaBox,descriptionArea))
    fcaBox = new FCABox(fetaBox,errorArea)
    fetaInfo = new FETAInfoBox(fetaBox,errorArea)

    fetaBox.init(leftColumn, true)
    errorArea.init(leftColumn)
    descriptionArea.init(leftColumn)
    fetaGraphBox.init(rightColumn, true)
    examples.init(leftColumn,true)
    fcaBox.init(rightColumn,false)
    fetaInfo.init(leftColumn,true)

    common.Utils.moreInfo(rightColumn, "https://github.com/arcalab/team-a")

    // load default button
    if (!examples.loadButton("Auth")) {
      reload()
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
    fetaBox.update()
    fetaGraphBox.update()
    fcaBox.update()
    fetaInfo.update()
  }

}
