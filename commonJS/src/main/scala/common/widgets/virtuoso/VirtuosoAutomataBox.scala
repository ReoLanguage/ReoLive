package common.widgets.virtuoso

import common.frontend.AutomataToJS
import common.widgets.{Box, GraphBox, OutputArea}
import hub.HubAutomata
import hub.backend.Uppaal
import ifta.backend.IftaAutomata
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import preo.ast.CoreConnector
import preo.backend.Network.Mirrors
import preo.backend.{Automata, Circuit, Network, PortAutomata}
import preo.frontend.Show

/**
  * Created by guille on 31/01/2019
  */


class VirtuosoAutomataBox(dependency: Box[CoreConnector], errorBox: OutputArea)
  extends Box[Automata]("Hub Automaton of the instance", List(dependency)) {
  private var svg: Block = _
  private var automaton: HubAutomata = _


  private val widthAutRatio = 7
  private val heightAutRatio = 3
  private val densityAut = 0.2 // nodes per 100x100 px

  override def get: Automata = automaton

  override def init(div: Block, visible: Boolean): Unit = {
    svg= GraphBox.appendSvg(panelBox(div, visible,buttons=List(
      Left("port names")      -> (()=> if (isVisible) drawAutomata(true) else (),"See port names"),
      Left("hub names")      -> (()=> if (isVisible) drawAutomata(false) else (),"See hub names"),
      Left("show")      -> (()=> if (isVisible) debug() else (),"See automata as text")
    )),"virtuosoAutomata")
    dom.document.getElementById("Hub Automaton of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if(!isVisible) drawAutomata() else deleteAutomaton()}

  }

  override def update(): Unit = if(isVisible) drawAutomata()


  private def drawAutomata(portNames:Boolean=true): Unit = {
    deleteAutomaton()
    try {
      //      automaton = Automata.fromOneToOneSimple[HubAutomata](dependency.get)//
      val mirrors = new Mirrors()
      //println("- Starting Automata drawing - 1st the circuit")
      Circuit(dependency.get, true, mirrors) // just to update mirrors
      //println("- Mirrors after circuit creation: "+mirrors)
      automaton = Automata[HubAutomata](dependency.get, mirrors).serialize.simplify
//      println("hub: \n" + automaton.show)
      //println(s"%%%\n${automaton.show}\n%%%")
      //println(s"${automaton.getTrans.mkString(" > ")}")
      val sizeAut = automaton.getStates.size
      //              println("########")
      //              println(aut)
      //              println("++++++++")
      val factorAut = Math.sqrt(sizeAut * 10000 / (densityAut * widthAutRatio * heightAutRatio))
      val width = (widthAutRatio * factorAut).toInt
      val height = (heightAutRatio * factorAut).toInt
      svg.attr("viewBox", s"00 00 $width $height")

      scalajs.js.eval(AutomataToJS(automaton, mirrors, "virtuosoAutomata", portNames))
    }
    catch Box.checkExceptions(errorBox)
  }

  private def deleteAutomaton(): Unit = {
    svg.selectAll("g").html("")
  }

  private def debug(): Unit = {
    errorBox.message(automaton.show)
    errorBox.message(automaton.smallShow)
  }
}