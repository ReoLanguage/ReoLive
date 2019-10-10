package common.widgets.newdsl

import common.widgets.{Box, OutputArea}
import dsl.DSL
import dsl.analysis.syntax.Program
import dsl.backend.Show

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
    list.append("li")
      .text(dsl.backend.Net(prog).toString)
    list.append("li")
      .text("------")
    list.append("li")
      .text(Show(prog))
  } catch Box.checkExceptions(errorBox,"DSL Analysis")

}