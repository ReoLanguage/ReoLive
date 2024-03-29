package reolive

import common.DomNode
import common.widgets.Lince.{LinceBox, LinceExamplesBox}
import common.widgets._
import org.scalajs.dom.html
import widgets.LocalGraphicBox
//import org.singlespaced.d3js.d3
import widgets.{RemoteEvalBox, RemoteGraphicBox}

import scala.scalajs.js.annotation.JSExportTopLevel

object RemoteLince {

  object Lince extends{

    var inputBox: LinceBox = _
    //  var typeInfo: PanelBox[Connector] = _
    //var information: Box[Syntax] = _
    var examples: LinceExamplesBox = _
    var graphic: RemoteGraphicBox = _
    var localGraphic: LocalGraphicBox = _
    var eval: RemoteEvalBox = _
    var errors: OutputArea = _
    var descr: OutputArea = _
    var perturbation: InputBox = _
    var bounds: InputBox = _


    @JSExportTopLevel("reolive_RemoteLince_main")
    def main(content: html.Div): Unit = {


      // Creating outside containers:
      val contentDiv = DomNode.select(content).append("div") //d3.select(content).append("div")
        .attr("class", "content")

      val rowDiv = contentDiv.append("div")
        //      .attr("class", "row")
        .attr("id", "mytable")

      val leftColumn = rowDiv.append("div")
        //      .attr("class", "col-sm-4")
        .attr("id", "leftbar")
        .attr("class", "leftside")

      leftColumn.append("div")
        .attr("id","dragbar")
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
      inputBox = new LinceBox(reload(), "",errors)
      //information = new LinceInfoBox(inputBox, errors)
      perturbation = new InputBox(softReload(),"0","perturbation",1,
        title = "Perturbations up-to  (experimental)",
        refreshLabel = "Add warnings when conditions would differ when deviating the variables by some perturbation > 0. Set to 0 to ignore these warnings.")
      bounds = new InputBox(softReload(),"150 // maximum time in the plot","bounds",1,
        title = "Plot length",
        refreshLabel = "\"t\" or \"t l\": Maximum time \"t\" when drawing the plot, and maximum \"l\" number of while loop unfolds (default 1000).")
      examples = new LinceExamplesBox(softReload(),inputBox,descr,bounds)
      graphic= new RemoteGraphicBox(()=>prepareGraphics(),inputBox, perturbation, bounds, errors)
      localGraphic= new LocalGraphicBox(()=>prepareGraphics(),inputBox, perturbation, bounds, errors)
      eval   = new RemoteEvalBox(inputBox, errors, bounds, "")

      inputBox.init(leftColumn, visible = true)
      errors.init(leftColumn)
      examples.init(leftColumn, visible = true)
      descr.init(leftColumn)
      perturbation.init(leftColumn,visible = false)
      bounds.init(leftColumn,visible = false)
      //information.init(rightColumn,true)
      graphic.init(rightColumn, visible = true)
      localGraphic.init(rightColumn, visible = false)
      eval.init(rightColumn,visible = false)


      // val moreInfo = rightColumn.append("div")
      //   .attr("class","panel-group")
      //   .append("p")
      // moreInfo
      //   .style("font-size","larger")
      //   .style("text-align","center")
      //   .style("padding-top","18px")
      // moreInfo
      //   .text("More information on the project: ")
      //   .append("a")
      //     .attr("href","https://github.com/arcalab/lince")
      //     .attr("target","#")
      //     .text("https://github.com/arcalab/lince")

      common.Utils.moreInfo(rightColumn,"https://github.com/arcalab/lince")


      // load default button

      if (!examples.loadButton("Cruise control")) {
//      if (!examples.loadButton("Avoiding approx. error")) {
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
      //information.update()
      perturbation.update()
      bounds.update()
      graphic.update()
      localGraphic.update()
      eval.update()
    }

    private def prepareGraphics(): Unit = {
      errors.clear()
      inputBox.update()
      perturbation.update()
      //graphic.update()
    }

  }

}
