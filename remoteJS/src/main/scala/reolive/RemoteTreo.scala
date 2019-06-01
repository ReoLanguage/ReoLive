package reolive

import common.widgets.Lince.{LinceBox, LinceExamplesBox}
import common.widgets._
import org.scalajs.dom.html
import org.singlespaced.d3js.d3
import widgets.RemoteGraphicBox
import widgets.Treo.{TreoBox, TreoExamplesBox, TreoResultBox}

import scala.scalajs.js.annotation.JSExportTopLevel

object RemoteTreo {

  var inputBox: TreoBox = _
  //  var typeInfo: PanelBox[Connector] = _
  //var information: Box[Syntax] = _
  var examples: TreoExamplesBox = _
//  var graphic: RemoteGraphicBox = _
  var treo: TreoResultBox = _
  var errors: OutputArea = _
//  var result: OutputArea = _
  var descr: OutputArea = _
//  var perturbation: InputBox = _


  @JSExportTopLevel("reolive.RemoteTreo.main")
  def main(content: html.Div): Unit = {

    val program =
      """import reo.sync;
        |import reo.syncdrain;
        |import reo.fifo1;
        |
        |main(a, b, c) { reo.syncdrain(a,b) reo.sync(b,x) reo.fifo1(x,c) reo.sync(a,c)}
      """.stripMargin

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

    // add InputArea
    //      inputBox = new InputBox(reload(),default = "v:=0;p:=0;p=v,v=10&p<=0 /\\ v<=0 ; v:=v* -0.5",id = "Lince",rows=3)
    //      inputBox = new InputBox(reload(),default = "x:=2;y:=1;\n  x=2*x,\n  y=y & x>10",id = "Lince",rows=3)
    descr = new OutputArea
    errors = new OutputArea //(id="Lince")
//    result = new OutputArea //(id="Lince")

    inputBox = new TreoBox(reload(),program,errors)
    //    inputBox = new LinceBox(reload(), "", errors)
    examples = new TreoExamplesBox(softReload(), inputBox, descr)
    //information = new LinceInfoBox(inputBox, errors)
//    inputBox = new InputBox(reload(), program, "treo", 7,
//        title = "Treo program",
//        refreshLabel = "Reload.")
    treo = new TreoResultBox(inputBox,errors)
    //    graphic = new RemoteGraphicBox(inputBox, perturbation, errors)

    inputBox.init(leftColumn, true)
    treo.init(rightColumn, visible = true)
    errors.init(leftColumn)
//    result.init(rightColumn)
    examples.init(leftColumn, true)
    descr.init(leftColumn)
//    perturbation.init(leftColumn, visible = false)
    //information.init(rightColumn,true)
//    graphic.init(rightColumn, true)

    //    typeInfo = new TypeBox(inputBox, errors)
    //    typeInfo.init(colDiv1,true)


    // load default button
    if (!examples.loadButton("Alternator")) {
      reload()
    }
//    reload()

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
    treo.update()
  }



}
