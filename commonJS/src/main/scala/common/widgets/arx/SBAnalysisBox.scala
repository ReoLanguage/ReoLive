package common.widgets.arx


import common.widgets.{Box, OutputArea}
import dsl.DSL
import dsl.analysis.semantics.{Guard, SBAutomata}
import dsl.analysis.semantics.StreamBuilder
import dsl.analysis.syntax.Program
import dsl.analysis.types.{Context, TExp}
import dsl.backend.{Net, Show}
import org.scalajs.dom.EventTarget
//import org.singlespaced.d3js.Selection
/**
  * Created by guillerminacledou on 30/07/2020
  */


class SBAnalysisBox(streamBuilder: Box[String], errorBox: OutputArea)
  extends Box[StreamBuilder]("Stream builder Analysis", List(streamBuilder)) {
  var box: Block = _
  var sb: StreamBuilder = _

  override def get: StreamBuilder = sb

  override def init(div: Block, visible: Boolean): Unit = {
    box = super.panelBox(div, visible, buttons = List())
      .append("div")
      .attr("id", "sbRes")
      .style("white-space", "pre-wrap")
  }

  override def update(): Unit = if(isVisible) analyse()

  protected def analyse():Unit = try {
    this.clear()

    sb = DSL.parseSB(streamBuilder.get)

    var out = box.append("div").attr("class", "sb-result")
      .append("ul")
      .attr("class", "list-group list-group-flush mb-3")
      .style("margin-bottom","0px")

      var li = out.append("li")
        .attr("class", "list-group-item lh-condensed")
        .style("margin","5px")

      var div = li.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")

      var elemTitle = div.append("span")
        .style("font-weight", "600")
        .attr("class", "element-name").text("Stream Builder")//.text("Program:")

      var info = li.append("div")

      var mems = info.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")
        .style("margin", "2px 10px 0px 0px")
      mems.append("span")
        .text("Memory Variables:")
      mems.append("span")
        .style("margin-left", "5px")
        .text(sb.memory.mkString(", "))

      var inputs = info.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")
        .style("margin", "2px 10px 0px 0px")
      inputs.append("span")
        .text("I/O Streams:")
      inputs.append("span")
        .style("margin-left", "5px")
        .text("[" + sb.inputs.mkString(", ") + " | " + sb.outputs.mkString(", ") + "]")

      var initinfo = info.append("div")
        .style("display", "flex")
        .style("justify-content", "fix-start")
        .style("margin", "2px 10px 0px 0px")
      initinfo.append("span")
        .text("Initial State:")
      initinfo.append("span")
        .style("margin-left", "5px")
        .text(if (sb.init.isEmpty) "∅" else sb.init.map(Show(_)).mkString(", "))

      var sbs = info.append("div")
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
      }
  } catch Box.checkExceptions(errorBox,"DSL Analysis")

  def clear(): Unit = box.text("")

}