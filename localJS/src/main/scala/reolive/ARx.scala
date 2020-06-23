package reolive

import common.widgets.{ButtonsBox, OutputArea}
import common.widgets.arx._
import org.scalajs.dom.html
import org.singlespaced.d3js.d3

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * Created by guillecledou on 2019-06-07
  */


object ARx {
  var inputBox: DslBox = _
  var errors: OutputArea = _
  var result: DslAnalysisBox = _
  var examples: DslExamplesBox = _
  var graph: DslGraphBox = _
  var descr: OutputArea = _
//  var dsllib:DslLibBox = _
//  var dsllibout:DslLibOutputArea = _
  var aut:DslAutomataBox = _


  @JSExportTopLevel("reolive.ARx.main")
  def main(content: html.Div): Unit = {

    val program =
      """import conn.prim
        |
        |def alt(i1,i2) = {
        |  a<-in1(i1) b<-in2(i2)
        |  drain(a, b)
        |  x<-a x<-fifo(b)
        |  out(x)
        |}
        |alt(x,y)""".stripMargin

    // Creating outside containers:
    val contentDiv = d3.select(content).append("div")
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

    descr = new OutputArea
    errors = new OutputArea //(id="Lince")
//    dsllibout = new DslLibOutputArea

    inputBox = new DslBox(reload(),program,errors)
    result = new DslAnalysisBox(inputBox,errors)
    examples = new DslExamplesBox(softReload(),List(inputBox,descr))
    graph = new DslGraphBox(result,errors)
//    dsllib = new DslLibBox(softReload(),List(dsllibout,descr))
    aut = new DslAutomataBox(inputBox,errors)

    inputBox.init(leftColumn, true)
    errors.init(leftColumn)
    graph.init(rightColumn,visible = true)
    aut.init(rightColumn,visible = false)
    result.init(rightColumn,visible = true)
    descr.init(leftColumn)
//    dsllib.init(leftColumn,visible = true)
//    dsllibout.init(leftColumn)
    examples.init(leftColumn,visible = true)


    common.Utils.moreInfo(rightColumn,"https://github.com/arcalab/arx")
    common.Utils.temporaryInfo(rightColumn,"Coordination'20 slides and video presentation: ","http://arca.di.uminho.pt/content/arx-slides-20.pdf")

    // default button
    if (examples.loadButton("alt")) {
      softReload()
    }

  }


  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def reload(): Unit = {
    descr.clear()
//    dsllibout.clear()
    softReload()
  }
  private def softReload(): Unit = {
    errors.clear()
    inputBox.update()
    result.update()
    graph.update()
    aut.update()
  }


}
