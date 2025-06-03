package widgets

import common.widgets.{Box, OutputArea}
import hprog.backend.ProbChecker
import org.scalajs.dom
import org.scalajs.dom.html

class RemoteProbBox(program: Box[String], errorBox: OutputArea,  maxT: Box[String], maxI: Box[String])
  extends Box[Unit]("Probability check", List(program)) {

  private var box : Block = _
  private var input: String = ""
  private var inputAreaDom: html.TextArea = _
  private var outEval: Block = _

  private val boxID = title+"_id"


  override def get: Unit = {}

  override def init(div: Block, visible: Boolean): Unit = {
    box = super.panelBox(div,visible,
      buttons = List(
        Right("refresh") -> (() => update(), "Check the probability of a given property (Shift-enter)")
//        Left("resample") -> (() => redraw(true), "Resample: draw again the image, using the current zooming window"),
//        Left("all jumps") -> (() => redraw(false), "Resample and include all boundary nodes")
        //        Left("&dArr;")-> (() => saveSvg(),"Download image as SVG")
      ))

    toggleVisibility(visible = ()=>{getProb()})

    val evaluator = box.append("div")
      .attr("id", "prob evaluator box")
    outEval = box.append("div")
      .attr("id","probOutput")

    evaluator.append("div")
      .attr("style","color: blue; display: inline; vertical-align: top; line-height: 20pt;")
      .text("Query: ")
    val inputArea = evaluator.append("textarea")
      .attr("id", boxID)
      .attr("name", boxID)
      .attr("class","my-textarea prettyprint lang-java")
      .attr("rows", "1")
      .attr("style", "width: 75%; background-color: rgb(238, 238, 255);")
      //      .attr("placeholder", input)
      .text(input)

    inputAreaDom = dom.document.getElementById(boxID).asInstanceOf[html.TextArea]

    inputAreaDom.onkeydown = {e: dom.KeyboardEvent =>
      if(e.keyCode == 13 && e.shiftKey){e.preventDefault(); update()}
      else ()
    }

  }

  override def update(): Unit = try {
    if (isVisible) getProb()
  }
  catch Box.checkExceptions(errorBox,"Prob. Evaluator")

  private def getProb() = {
    inputAreaDom = dom.document.getElementById(boxID).asInstanceOf[html.TextArea]
    if(inputAreaDom.value != "") {
      input = inputAreaDom.value

      try {
        val res = ProbChecker(input, program.get, maxT.get.toDouble, maxI.get.toInt)
        outEval.text(res)
      }
      catch Box.checkExceptions(errorBox, "Prob. Evaluator")
    }
  }

}
