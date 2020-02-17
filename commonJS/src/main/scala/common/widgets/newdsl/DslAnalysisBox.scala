package common.widgets.newdsl

import common.widgets.{Box, OutputArea}
import dsl.DSL
import dsl.analysis.semantics.StreamBuilder.StreamBuilderEntry
import dsl.analysis.syntax.Program
import dsl.analysis.types.{Context, TExp}
import dsl.backend.{Net, Show}

/**
  * Created by guillerminacledou on 2019-06-07
  */


class DslAnalysisBox(program: Box[String], errorBox: OutputArea)
  extends Box[Program]("DSL Analysis", List(program)) {
  var box: Block = _
  var prog: Program = _

  override def get: Program = prog

  override def init(div: Block, visible: Boolean): Unit = {
    box = super.panelBox(div, visible,
      buttons = List(
      ))
      .append("div")
      .attr("id", "newdlsRes")
      .style("white-space", "pre-wrap")
  }

  override def update(): Unit = if(isVisible) analyse()

  protected def analyse():Unit = try {
    this.clear()
    prog = DSL.parse(program.get)
    val (tprog,tctx) = DSL.typeCheck(prog)
    //val (sbprog,sbOuts,sbCtx) = DSL.encode(tprog,tctx)
    val sbCtx = DSL.encode(tprog,tctx)

    var out = box.append("div").attr("class", "sb-result")
      .append("ul")
      .attr("class", "list-group list-group-flush mb-3")
      .style("margin-bottom","0px")

    val primFuns= DSL.prelude.primitiveFunctionNames()

    val functionsInfo:Map[String,(TExp,StreamBuilderEntry)] =
      tctx.functions.filterNot(f=>primFuns.contains(f._1)).map(f=> f._1 -> (f._2.tExp,sbCtx(f._1)))

    for( (fun,(texp,(sb,inSeq,outSeq))) <- functionsInfo) {

      var li = out.append("li")
        .attr("class", "list-group-item lh-condensed")
        .style("margin","5px")

      var div = li.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")

      var elemTitle = div.append("span")
        .style("font-weight", "600")
        .attr("class", "element-name").text(fun+" :: ")//.text("Program:")

      var elemType = div.append("span")
        .style("text-align", "left")
      elemType.append("span")
        .style("margin", "5px")
        .text(Show(texp))//.text(Show(tctx.functions("program").tExp))

      var info = li.append("div")

      var mems = info.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")
        .style("margin", "2px 10px 0px 10px")
      mems.append("span").text("Memory Variables:")
      mems.append("span")
        .style("margin-left", "5px")
        .text(sb.memory.mkString(", "))

      var inputs = info.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")
        .style("margin", "2px 10px 0px 10px")
      inputs.append("span").text("Input Streams:")
      inputs.append("span")
        .style("margin-left", "5px")
        .text(sb.inputs.mkString(", "))

      var outputs = info.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")
        .style("margin", "2px 10px 0px 10px")
      outputs.append("span").text("Output Streams:")
      outputs.append("span")
        .style("margin-left", "5px")
        .text(sb.outputs.mkString(", "))

      var outseq = info.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")
        .style("margin", "2px 10px 0px 10px")
      outseq.append("span")
        .text("Output Sequence:")
      outseq.append("span")
        .style("margin-left", "5px")
        .text(outSeq.mkString(", "))

      var sbs = info.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")
        .style("margin", "2px 10px 0px 10px")
      sbs.append("span").text("Guarded Commands:")
      var gcdiv = sbs.append("div")
        .style("margin-left", "5px")

      // for each guarded command:
      for (gc <- sb.gcs) {
        gcdiv.append("li")
          .style("list-style-type","none")
          .text(s"${Show(gc)}")
      }
    }
  } catch Box.checkExceptions(errorBox,"DSL Analysis")

  def clear(): Unit = box.text("")

//  override def update(): Unit = try {
//    box.html("")
////    var prog = DSL.parse(program.get)
//    prog = DSL.parse(program.get)
////    println("Inferred tree: " + Show(prog))
//    val list = box.append("ul")
//    list.attr("style","margin-bottom: 20pt;")
////    var types = DSL.typeCheck(DSL.parse(program.get))
////    types.map(v =>
////      list.append("li")
////        .text(s"${v._1}: ${Show(v._2)}")
////    )
////    list.append("li")
////      .text("Net:\n"+Net(prog)._1.pretty.toString)
//////    list.append("li")
//////      .text("----- Parsed:")
////    list.append("li")
////      .text("---- Parsed:\n"+Show(prog))
//    list.append("li")
//      .text("---- Types:\n")
//
//    var (typedProgram,typeCtx) = DSL.typeCheck(prog)
//    var types = typeCtx.functions.map(f=>f._1->f._2.tExp) ++ typeCtx.ports.map(p=>p._1->p._2.head.tExp)
//
//    types.filterNot(t=> DSL.prelude.primitiveFunctionNames().contains(t._1)).map(v =>
//      list.append("li")
//        .text(s"${v._1}: ${Show(v._2)}")
//    )
//    list.append("li")
//      .text("---- Stream Builders:\n")
//    var (sb,sbOuts,sbCtx) = DSL.encode(typedProgram,typeCtx)
//      list.append("li")
//          .text(s"Program: ${Show(sb)} " +
//            s"\nOutput sequence: ${sbOuts.mkString(",")}")
//     sbCtx.get().filterNot(f=> DSL.prelude.primitiveFunctionNames().contains(f._1)).map(sbe=>
//       list.append("li")
//         .text(s"${sbe._1}: ${Show(sbe._2._1)} " +
//           s"\nInput sequence: ${sbe._2._2.mkString(",")}" +
//           s"\nOutput sequence:${sbe._2._3.mkString(",")} "))
//  } catch Box.checkExceptions(errorBox,"DSL Analysis")

}