package reolive

import common.DomNode
import common.widgets.{ButtonsBox, OutputArea}
import common.widgets.arx._
import org.scalajs.dom.html
//import org.singlespaced.d3js.d3

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
  var sb:SBComposerBox = _
  var sbRes:SBAnalysisBox = _


  @JSExportTopLevel("reolive_ARx_main")
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

    val sbs =
      """/// a>>b
        |seq = {
        |	get(a) -> bx:=_;
        |  get(b,bx) ->
        |}
        |
        |// c<-a d<-b
        |sb1 = {
        |	get(a,b) ->c:=a, d:=b;
        |	get(a) -> c:=a;
        |  get(b) -> d:=b
        |}
        |// a>>b
        |seq2 = {
        |	get(a,c) -> bx:=_;
        |	get(b,bx) ->
        |}
        |// a<-c d<-b
        |sb2 = {
        |	get(c,b) -> a:=c, d:=b;
        |	get(c) ->  a:=c;
        |  get(b) -> d:=b
        |}
        |
        |// a<-c b<-d
        |sb3 = {
        |	get(c,d) -> a:=c, b:=d;
        |	get(c) ->  a:=c;
        |  get(d) -> b:=d
        |}
        |// a>>b
        |seq3 = {
        |	get(a,c) -> bx:=_;
        |	get(b,bx,d) ->
        |}
        |
        |// a>>b
        |seq4 = {
        |	get(a) -> bx:=_;
        |	get(b,bx,d) ->
        |}
        |
        |
        |// c<-a b<-d
        |sb4 = {
        |	get(a,d) -> c:=a, b:=d;
        |	get(a) ->  c:=a;
        |  get(d) -> b:=d
        |}
        |
        |// a>>b c<-a d<-b (a,b inputs)
        |//seq * sb1
        |
        |// a>>b a<-c d<-b (a output,b input)
        |//seq * sb2 // not ok
        |//seq2 * sb2 // ok
        |// a>>b a<-c b<-d (a,b output)
        |//seq3 * sb3 // ok
        |// a>>b c<-a b<-d (a input,b output)
        |seq4 * sb4 // ok""".stripMargin

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

    descr = new OutputArea
    errors = new OutputArea //(id="Lince")
//    dsllibout = new DslLibOutputArea

    inputBox = new DslBox(reload(),program,errors)
    graph = new DslGraphBox(inputBox,errors)
    result = new DslAnalysisBox(graph,errors)
    examples = new DslExamplesBox(softReload(),List(inputBox,descr))
//    dsllib = new DslLibBox(softReload(),List(dsllibout,descr))
    aut = new DslAutomataBox(inputBox,errors)
    sb = new SBComposerBox(reloadSB(),sbs,errors)
    sbRes = new SBAnalysisBox(sb,errors)

    inputBox.init(leftColumn, true)
    errors.init(leftColumn)
    graph.init(rightColumn,visible = true)
    aut.init(rightColumn,visible = false)
    result.init(rightColumn,visible = true)
    descr.init(leftColumn)
//    dsllib.init(leftColumn,visible = true)
//    dsllibout.init(leftColumn)
    examples.init(leftColumn,visible = true)
    sb.init(leftColumn,visible = true)
    sbRes.init(rightColumn,visible = true)

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
  private def reloadSB(): Unit = {
    sbRes.update()
    errors.clear()
  }

  private def softReload(): Unit = {
    errors.clear()
    inputBox.update()
    graph.update()
    result.update()
    aut.update()
  }


}
