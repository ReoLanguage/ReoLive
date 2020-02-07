package common.widgets.newdsl

import common.widgets.{Box, OutputArea}
import dsl.DSL
import dsl.analysis.syntax.Program
import dsl.analysis.types.{Context, TExp}
import dsl.backend.{Net, Show}

/**
  * Created by guillerminacledou on 2019-06-07
  */


class DslAnalysisBox(program: Box[String], errorBox: OutputArea)
  extends Box[Program]("DSL type analysis result", List(program)) {
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


  override def update(): Unit = try {
    box.html("")
//    var prog = DSL.parse(program.get)
    prog = DSL.parse(program.get)
//    println("Inferred tree: " + Show(prog))
    val list = box.append("ul")
    list.attr("style","margin-bottom: 20pt;")
//    var types = DSL.typeCheck(DSL.parse(program.get))
//    types.map(v =>
//      list.append("li")
//        .text(s"${v._1}: ${Show(v._2)}")
//    )
//    list.append("li")
//      .text("Net:\n"+Net(prog)._1.pretty.toString)
////    list.append("li")
////      .text("----- Parsed:")
//    list.append("li")
//      .text("---- Parsed:\n"+Show(prog))
    list.append("li")
      .text("---- Types:\n")

    var typeCtx = DSL.typeCheck(prog)
    var types = typeCtx.functions.map(f=>f._1->f._2.tExp) ++ typeCtx.ports.map(p=>p._1->p._2.head.tExp)

    types.filterNot(t=> DSL.prelude.primitiveFunctionNames().contains(t._1)).map(v =>
      list.append("li")
        .text(s"${v._1}: ${Show(v._2)}")
    )
    list.append("li")
      .text("---- Stream Builders:\n")
    var (sb,sbOuts,sbCtx) = DSL.encode(prog,typeCtx)
      list.append("li")
          .text(s"Program: ${Show(sb)} " +
            s"\nOutput sequence: ${sbOuts.mkString(",")}")
     sbCtx.get().filterNot(f=> DSL.prelude.primitiveFunctionNames().contains(f._1)).map(sbe=>
       list.append("li")
         .text(s"${sbe._1}: ${Show(sbe._2._1)} " +
           s"\nInput sequence: ${sbe._2._2.mkString(",")}" +
           s"\nOutput sequence:${sbe._2._3.mkString(",")} "))
  } catch Box.checkExceptions(errorBox,"DSL Analysis")

}