package common.widgets.arx

import common.frontend.AutomataToVisJS
import common.widgets.{Box, OutputArea}
import dsl.DSL
import dsl.analysis.semantics.StreamBuilder.StreamBuilderEntry
import dsl.analysis.types.TExp
import dsl.backend.Show
//import org.singlespaced.d3js.Selection

/**
  * Created by guillerminacledou on 2019-06-07
  */


class DslAnalysisBox(circuitBox: DslGraphBox, errorBox: OutputArea)
  extends Box[Unit]("Analysis of the program", List(circuitBox)) {
  var box: Block = _

  override def get: Unit = {}

  override def init(div: Block, visible: Boolean): Unit = {
    box = super.panelBox(div, visible, buttons = List())
      .append("div")
      .attr("id", "newdlsRes")
      .style("white-space", "pre-wrap")
  }

  override def update(): Unit = if(isVisible) analyse()

  protected def analyse():Unit = try {
    this.clear()

    val (sbCtx,types,net,nID) = circuitBox.get

//    prog = DSL.parse(program.get)
//    val (tprog,tctx) = DSL.typeCheck(prog)
//    //val (sbprog,sbOuts,sbCtx) = DSL.encode(tprog,tctx)
//    val sbCtx = DSL.encode(tprog,tctx)

    val out = box.append("div").attr("class", "sb-result")
      .append("ul")
      .attr("class", "list-group list-group-flush mb-3")
      .style("margin-bottom", "0px")

    val primFuns= DSL.prelude.primitiveFunctionNames()

    // filter out primitive functions and 1-1 undefined functions
    val functionsInfo: Map[String, (TExp, StreamBuilderEntry)] =
      types.functions.filterNot(f => primFuns.contains(f._1) || !sbCtx.contains(f._1))
        .map(f => f._1 -> (f._2.tExp, sbCtx(f._1)))

//    var functionsInfo:Map[String,(TExp,StreamBuilderEntry)] =
//      tctx.functions.filterNot(f=>primFuns.contains(f._1) || !sbCtx.contains(f._1))
//        .map(f=> f._1 -> (f._2.tExp,sbCtx(f._1)))

    for( (fun,(texp,(sb, _,outSeq,_))) <- functionsInfo) {

      val li = out.append("li")
        .attr("class", "list-group-item lh-condensed")
        .style("margin", "5px")

      val div = li.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")

      val elemTitle = div.append("span")
        .style("font-weight", "600")
        .attr("class", "element-name").text(fun+" :: ")//.text("Program:")

      val elemType = div.append("span")
        .style("text-align", "left")
      elemType.append("span")
        .style("margin", "5px")
        .text(Show(texp))//.text(Show(tctx.functions("program").tExp))

      val info = li.append("div")

      val mems = info.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")
        .style("margin", "2px 10px 0px 0px")
      mems.append("span")
        .text("Memory Variables:")
      mems.append("span")
        .style("margin-left", "5px")
        .text(sb.memory.mkString(", "))

      val inputs = info.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")
        .style("margin", "2px 10px 0px 0px")
      inputs.append("span")
        .text("I/O Streams:")
      inputs.append("span")
        .style("margin-left", "5px")
        .text("[" + sb.inputs.mkString(", ") + " | " + sb.outputs.mkString(", ") + "]")

      // var outputs = info.append("div")
      //   .style("display", "flex")
      //   .style("justify-content", "fix-start")
      //   .style("margin", "2px 10px 0px 0px")
      // outputs.append("span").text("Output Streams:")
      // outputs.append("span")
      //   .style("margin-left", "5px")
      //   .text(sb.outputs.mkString(", "))

      val outseq = info.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")
        .style("margin", "2px 10px 0px 0px")
      outseq.append("span")
        .text("Output Sequence:")
      outseq.append("span")
        .style("margin-left", "5px")
        .style("text-decoration","overline")
        .text(outSeq.mkString(", "))

      val initinfo = info.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")
        .style("margin", "2px 10px 0px 0px")
      initinfo.append("span")
        .text("Initial State:")
      initinfo.append("span")
        .style("margin-left", "5px")
        .text(if (sb.init.isEmpty) "∅" else sb.init.map(Show(_)).mkString(", "))

      val sbs = info.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")
        .style("margin", "2px 10px 0px 0px")
      sbs.append("span")
        .text("Guarded Commands:")
//      val gcdiv = sbs.append("div")
//        .attr("class","container-fluid")
//        .style("margin-left", "5px")
        val gcdiv = info.append("div")
        //.attr("class", "list-group list-group-flush ")
        .style("margin","5px 5px 5px 10px")
      // for each guarded command:
      for (gc <- sb.gcs) {
        val row = gcdiv.append("div")
          .style("display", "flex")
          .style("margin", "5px")
          .style("justify-content", "fix-start")

        row.append("div")
          .style("margin", "auto 5px")
          //.attr("class", "alert alert-info")
          .attr("class","sb-box")
          .style("padding","5px")
          .style("color","#008900")
          .text(Show(gc.guard))
        row.append("div")
          .style("margin", "auto 5px")
          .style("text-align", "center")
          .style("padding","5px")
          .text("→")
        row.append("div")
          .style("margin", "auto 5px")
          //.attr("class", "alert alert-success")
          .attr("class", "sb-box")
          .style("padding","5px")
          .style("background-color","white")
          .style("color","#0F024F")
          .text(if (gc.cmd.isEmpty) "∅" else gc.cmd.map(c=>Show(c)).mkString(", "))
//        row.append("div")
//          .style("margin", "auto 5px")
//          .style("text-align", "center")
//          .style("padding","5px")
//          .text(
//            gc.highlights.mkString(",") + " -> " +
//            gc.highlights.map(m1).mkString(",") + " -> " +
//            gc.highlights.map(m1).map(x => x.flatMap(nID)).mkString(",") + " -> "+
//            gc.highlights.flatMap(nID).mkString(",") + " -> " +
//            gc.highlights.flatMap(m1).flatMap(nID).mkString(",")
////            highlight(gc.highlights.flatMap(net.mirror).flatMap(nID))
//            )
        def m1(s:String) = net.mirror(s)+s
        val hls = gc.highlights.flatMap(m1).flatMap(nID)
        row.on("mouseenter", ()=>scala.scalajs.js.eval(highlight(hls)))
        row.on("mouseleave", ()=>scala.scalajs.js.eval(deHighlight(hls)))
      }

      def highlight(ports:Iterable[Int]): String = {
        s"""[${ports.mkString(",")}].forEach(function(portId) {
           |  var p = document.getElementById("gr_"+portId);  //("gr_"+el);
           |  ${AutomataToVisJS.defaultHl("p")}
           |})""".stripMargin
      }
      def deHighlight(ports:Iterable[Int]): String = {
        s"""[${ports.mkString(",")}].forEach(function(portId) {
           |  var p = document.getElementById("gr_"+portId);  //("gr_"+el);
           |  ${AutomataToVisJS.defaultDeHl("p")}
           |})""".stripMargin
      }

       // .style("margin-left", "5px")

//      // for each guarded command:
//      for (gc <- sb.gcs) {
//        gcdiv.append("li")
//          .style("list-style-type","none")
//          .text(s"${Show(gc)}")
//      }
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