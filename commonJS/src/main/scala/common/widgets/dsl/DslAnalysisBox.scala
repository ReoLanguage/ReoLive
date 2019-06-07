package common.widgets.dsl

import common.widgets.{Box, OutputArea}
import dsl.DSL
import dsl.backend.Show

/**
  * Created by guillerminacledou on 2019-06-07
  */


class DslAnalysisBox(program: Box[String], errorBox: OutputArea)
  extends Box[Unit]("DSL type analysis result", List(program)) {
  var box: Block = _

  override def get: Unit = {}

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
    var types = DSL.typeCheck(DSL.parse(program.get))
    val list = box.append("ul")
    list.attr("style","margin-bottom: 20pt;")
    types.map(v =>
      list.append("li")
        .text(s"${v._1}: ${Show(v._2)}")
    )
  } catch Box.checkExceptions(errorBox)

}