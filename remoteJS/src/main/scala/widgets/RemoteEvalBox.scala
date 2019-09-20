package widgets

import common.widgets.{Box, OutputArea}
import hprog.ast.SageExpr.SExpr
import hprog.ast.Syntax
import hprog.frontend.Deviator
import hprog.frontend.solver.StaticSageSolver
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}

class RemoteEvalBox(program: Box[String], errorBox: OutputArea, default:String = "")
  extends Box[Unit]("Symbolic Evaluation", List(program)) {

  private var box : Block = _
  private var input: String = default
  private var inputAreaDom: html.TextArea = _
  private var outEval: Block = _

  private val boxID = title+"_id"


  override def get: Unit = {}

  override def init(div: Block, visible: Boolean): Unit = {
    box = super.panelBox(div,visible,
      buttons = List(
        Right("glyphicon glyphicon-refresh") -> (() => update(), "Evaluate expression at a given time (Shift-enter_")
//        Left("resample") -> (() => redraw(true), "Resample: draw again the image, using the current zooming window"),
//        Left("all jumps") -> (() => redraw(false), "Resample and include all boundary nodes")
        //        Left("&dArr;")-> (() => saveSvg(),"Download image as SVG")
      ))

    toggleVisibility(visible = ()=>{callSage()})

    val evaluator = box.append("div")
      .attr("id", "evaluator box")
    outEval = box.append("div")
      .attr("id","evalOutput")

    evaluator.append("div")
      .attr("style","color: blue; display: inline; vertical-align: top; line-height: 20pt;")
      .text("Time: ")
    val inputArea = evaluator.append("textarea")
      .attr("id", boxID)
      .attr("name", boxID)
      .attr("class","my-textarea prettyprint lang-java")
      .attr("rows", "1")
      .attr("style", "width: 75%; ")
      //      .attr("placeholder", input)
      .text(input)

    inputAreaDom = dom.document.getElementById(boxID).asInstanceOf[html.TextArea]

    inputAreaDom.onkeydown = {e: dom.KeyboardEvent =>
      if(e.keyCode == 13 && e.shiftKey){e.preventDefault(); update()}
      else ()
    }

  }

  override def update(): Unit = try {
    if (isVisible) callSage()
  }
  catch Box.checkExceptions(errorBox,"Evaluator1")

  private def callSage() = {
    inputAreaDom = dom.document.getElementById(boxID).asInstanceOf[html.TextArea]
    if(inputAreaDom.value != "") {
      input = inputAreaDom.value

      outEval.text("<waiting>")
      errorBox.clear()
      errorBox.message("Waiting for SageMath...")
      RemoteBox.remoteCall("linceWS", s"E$input§${program.get}", eval)
    }
  }

  def eval(sageReply: String): Unit = {
    //errorBox.message(s"got reply: ${sageReply}")
    errorBox.clear()
    outEval.text("")
    if (sageReply startsWith "Error")
      errorBox.error(sageReply)
    else try {
      //println(s"got reply from sage: ${sageReply}. About to parse ${dependency.get}.")
      val sol = sageReply.split("§§").map(_.split("§"))
      var res:List[String] = Nil
      for (kv <- sol)
        kv match {
          case Array(k,v) => res ::= s"$k: $v"
          case _ => errorBox.error(s"unexpected reply ${kv.map(x=>s"'$x'")
            .mkString(", ")} : ${kv.getClass}")
        }
//      errorBox.message(res.mkString("</br>"))
      outEval.html(res.mkString("</br>"))
    }
    catch Box.checkExceptions(errorBox, "Evaluator2")  }



}
