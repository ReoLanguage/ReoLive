package common.widgets.arx

import common.frontend.AutomataToJS
import common.widgets.{Box, GraphBox, OutputArea}
import dsl.DSL
import dsl.analysis.semantics._
import dsl.backend.Show
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
/**
  * Created by guillerminacledou on 2020-02-18
  */


class DslAutomataBox(program: Box[String], errorBox: OutputArea)
  extends Box[SBAutomata]("Automaton of the program", List(program)) {
  private var svg: Block = _
  private var textAut:Block = _
  private var automaton: SBAutomata = _
  private var panel:Block = _
  private var text:Boolean = false

  protected val widthAutRatio = 7
  protected val heightAutRatio = 3
  private val densityAut = 0.2 // nodes per 100x100 px

  override def get: SBAutomata = automaton

  override def init(div: Block, visible: Boolean): Unit = {
    //svg= GraphBox.appendSvg(panelBox(div, visible),"sbautomata")
    panel = panelBox(div, visible, buttons = List(
      Left("simple")      -> (()=> if (isVisible) drawAutomata(SimpleMode) else (),"Without Push/Pull interpretation"),
      Left("push")      -> (()=> if (isVisible) drawAutomata(PushMode) else (),"Environment can push streams"),
      Left("pull")      -> (()=> if (isVisible) drawAutomata(PullMode) else (),"Environment can pull streams"),
      Left("all")       -> (()=> if (isVisible) drawAutomata(AllMode) else (),"Environment can push/pull streams"),
      Left("none")      -> (()=> if (isVisible) drawAutomata(NoneMode) else (),"Environment cannot push/pull streams"),
      Left("text")      -> (()=> if (isVisible) showText() else (),"Show automaton as text")))
    svg =  GraphBox.appendSvg(panel,"sbAutomata")
    textAut = panel.append("div")
      .attr("id","sbAutomatonTxt")
      .style("white-space", "pre-wrap")
    dom.document.getElementById("Automaton of the program").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if(!isVisible) drawAutomata() else deleteAutomaton()}
  }

  override def update(): Unit = if(isVisible) drawAutomata()

  private def showText() = {
    text = !text
    textAut.style("display",if(text) "block" else "none")
  }

  private def drawAutomata(buildMode:BuildMode=SimpleMode): Unit = try{
      deleteAutomaton()

      val prog = DSL.parse(program.get)
      val (tprog,tctx) = DSL.typeCheck(prog)
      val sbCtx = DSL.encode(tprog,tctx)
      val sbProgram = sbCtx("Program")
      //println(s"[arx-aut] SBs: ${Show(sbProgram._1)} § ${sbProgram._2} § ${sbProgram._3}")
      automaton = SBAutomata(sbProgram._1,mode=buildMode)

      val sizeAut = automaton.sts.size
      val factorAut = Math.sqrt(sizeAut * 10000 / (densityAut * widthAutRatio * heightAutRatio))
      val width = (widthAutRatio * factorAut).toInt
      val height = (heightAutRatio * factorAut).toInt

      textAut.style("max-height",height.toString)
        .style("display",if(text) "block" else "none")
        .style("overflow","scroll")
        .text(Show(automaton))

      svg.attr("viewBox", s"00 00 $width $height")
//      println(automaton.sts)
      scalajs.js.eval(AutomataToJS(automaton, "sbAutomata"))
//      svg.text(Show(SBAutomata(sbProgram._1,mode=buildMode)))
    }
    catch Box.checkExceptions(errorBox,"sbAutomata")

  private def deleteAutomaton(): Unit = {
    //svg.selectAll("g").html("")
    //// EXPERIMENTING TO DROP D3JS
    svg.deleteAll("g")
    textAut.text("")
  }
}