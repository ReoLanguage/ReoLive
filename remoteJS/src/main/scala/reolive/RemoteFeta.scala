package reolive

import common.DomNode
import common.widgets.OutputArea
import widgets.feta.{FetaBox, FetaGraphBox}
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

    val feta:String =
      s"""FCA user (confirm)(join,leave) = {
         |  start 0
         |  0 --> 1 by join if s
         |  1 --> 2 by confirm if s
         |  0 --> 2 by join if o
         |  2 --> 0 by leave
         |}
         |
         |FCA server (join,leave)(confirm) = {
         |  start 0
         |  0 --> 1 by join if s
         |  1 --> 0 by confirm if s
         |  0 --> 0 by join if o
         |  0 --> 0 by leave
         |}
         |
         |FS = (u1->user,u2->user,s->server)
         |
         |FM = s xor o
         |
         |FST = {
         | default = one to one
         | {o}:join,leave = many to one
         |}
         |""".stripMargin

    descriptionArea = new OutputArea
    errorArea = new OutputArea
    fetaBox = new FetaBox(reload(), fta.Examples.spec, errorArea)
    fetaGraphBox = new FetaGraphBox(fetaBox, errorArea)

    fetaBox.init(leftColumn, true)
    errorArea.init(leftColumn)
    descriptionArea.init(leftColumn)
    fetaGraphBox.init(rightColumn, true)

    common.Utils.moreInfo(rightColumn, "https://github.com/arcalab/team-a")

    //// load default button
    //if (exampleBox.loadButton("ex4")) {
    //  softReload()
    //}
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
  }

}
