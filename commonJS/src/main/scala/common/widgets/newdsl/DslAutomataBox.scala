package common.widgets.newdsl

import common.widgets.{Box, GraphBox, OutputArea}
import dsl.analysis.semantics._
import common.frontend.AutomataToJS
import dsl.DSL
import dsl.backend.Show
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
/**
  * Created by guillerminacledou on 2020-02-18
  */


class DslAutomataBox(program: Box[String], errorBox: OutputArea)
  extends Box[SBAutomata]("Automaton of the program", List(program)) {
  private var svg: Block = _
  private var automaton: SBAutomata = _

  protected val widthAutRatio = 7
  protected val heightAutRatio = 3
  private val densityAut = 0.2 // nodes per 100x100 px

  override def get: SBAutomata = automaton

  override def init(div: Block, visible: Boolean): Unit = {
    //svg= GraphBox.appendSvg(panelBox(div, visible),"sbautomata")
    svg =  super.panelBox(div, visible, buttons = List(
      Left("closed")      -> (()=> if (isVisible) drawAutomata(ClosedMode) else (),"Closed for push/pull from the environment"),
      Left("push")      -> (()=> if (isVisible) drawAutomata(PushMode) else (),"Environment can push streams"),
      Left("pull")      -> (()=> if (isVisible) drawAutomata(PullMode) else (),"Environment can pull streams"),
      Left("all")      -> (()=> if (isVisible) drawAutomata(AllMode) else (),"Environment can push/pull streams")))
      .append("div")
      .attr("id", "newdlsRes")
      .style("white-space", "pre-wrap")
    dom.document.getElementById("Automaton of the program").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if(!isVisible) drawAutomata() else deleteAutomaton()}
  }

  override def update(): Unit = if(isVisible) drawAutomata()


  private def drawAutomata(buildMode:BuildMode=ClosedMode): Unit =
    try{
      this.svg.text("")
      val prog = DSL.parse(program.get)
      val (tprog,tctx) = DSL.typeCheck(prog)
      val sbCtx = DSL.encode(tprog,tctx)
      val sbProgram = sbCtx("Program")
      svg.text(Show(SBAutomata(sbProgram._1,mode=buildMode)))
    }
    catch Box.checkExceptions(errorBox,"Automata")

  private def deleteAutomaton(): Unit = {
    svg.selectAll("g").html("")
  }
}