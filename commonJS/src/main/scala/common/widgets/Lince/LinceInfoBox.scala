package common.widgets.Lince

import common.widgets.{Box, OutputArea}
import hprog.DSL
import hprog.ast.Syntax
import hprog.backend.Show


//todo: this should be local to localJS
class LinceInfoBox(dependency: Box[String], errorArea: OutputArea)
    extends Box[Syntax]("Parsed program", List(dependency)){

  // state
  var prog: Syntax= _
  var block: Block = _


  override def get: Syntax = prog

  override def init(div: Block, visible: Boolean): Unit = {
    block = panelBox(div, visible).append("div")
      .attr("id", "HProgBox")
  }

  override def update(): Unit = {

    block.text("")
    try {
      DSL.parseWithError(dependency.get) match {
        case Right(result) => //hprog.lang.Parser.Success(result, _) =>
          block //.append("p")
            .html(Show(result).replace("\n", " <br>\n"))
          prog = result
        case Left(msg) => //hprog.lang.Parser.Failure(msg,_) =>
          errorArea.error("Parser failure: " + msg)
        //        instanceInfo.append("p").text("-")
      }
    } catch {
      case msg: Throwable => //hprog.lang.Parser.Error(msg,_) =>
        errorArea.error("Parser error: " + msg.toString)
      //        instanceInfo.append("p").text("-")
    }
  }
}
