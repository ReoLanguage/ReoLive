package reolive

import common.widgets.{ButtonsBox, OutputArea}
import common.widgets.dsl.{DslAnalysisBox, DslBox, DslExamplesBox}
import org.scalajs.dom.html
import org.singlespaced.d3js.d3

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * Created by guillecledou on 2019-06-07
  */


object NewDSL {
  var inputBox: DslBox = _
  var errors: OutputArea = _
  var result: DslAnalysisBox = _
  var examples: DslExamplesBox = _
  var descr: OutputArea = _


  @JSExportTopLevel("reolive.NewDSL.main")
  def main(content: html.Div): Unit = {

    val program =
      """data List<a> = Nil | Cons(a,List<a>)
        |data Bool = True | False
        |data Nat = Zero | Succ(Nat)
        |
        |x = Cons(Zero,Nil)
        |y = Cons(Zero,x)
        |z = Cons(Succ(Succ(Zero)),y)
        |w = True""".stripMargin

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

    inputBox = new DslBox(reload(),program,errors)
    result = new DslAnalysisBox(inputBox,errors)
    examples = new DslExamplesBox(softReload(),List(inputBox,descr))

    inputBox.init(leftColumn, true)
    errors.init(leftColumn)
    result.init(rightColumn,visible = true)
    descr.init(leftColumn)
    examples.init(leftColumn,visible = true)

    // default button
    if (examples.loadButton("Various types")) {
      reload()
    }

  }


  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def reload(): Unit = {
    descr.clear()
    softReload()
  }
  private def softReload(): Unit = {
    errors.clear()
    inputBox.update()
    result.update()
  }


}
