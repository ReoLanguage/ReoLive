package common.widgets

import common.frontend.{AutomataToJS, AutomataToVisJS}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import preo.ast.CoreConnector
import preo.backend.Network.Mirrors
import preo.backend.{Automata, Circuit, PortAutomata}


class AutomataVisBox(dependency: Box[CoreConnector], errorBox: OutputArea)
    extends Box[Automata]("Port Vis-Automaton of the instance", List(dependency)) {
  private var canvas: Block = _
  private var automaton: Automata = _

  override def get: Automata = automaton

  override def init(div: Block, visible: Boolean): Unit = {
    canvas= panelBox(div, visible) //GraphBox.appendSvg(panelBox(div, visible),"automata")
      .attr("id","visnetwork")
//    println("[EvalVis] Canvas: "+AutomataToVisJS.generateCanvasJS("",""))
    scalajs.js.eval(AutomataToVisJS.generateCanvasJS("",""))
    dom.document.getElementById("Port Vis-Automaton of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if(!isVisible) drawAutomata() else deleteAutomaton()}
  }

  override def toggleVisibility(visible:()=>Unit = ()=>{}, invisible:()=>Unit = ()=>{}): Unit =
    super.toggleVisibility( () => {drawAutomata(); visible()}, ()=>{deleteAutomaton();invisible()})

  override def update(): Unit = if(isVisible) drawAutomata()


  private def drawAutomata(): Unit =
  try{
    val mirrors = new Mirrors()
    //println("- Starting Automata drawing - 1st the circuit")
    Circuit(dependency.get,true,mirrors) // just to update mirrors
    //println("- Mirrors after circuit creation: "+mirrors)
    val automaton = Automata[PortAutomata](dependency.get,mirrors)

//    println("[EvalVis] Upd: "+AutomataToVisJS(automaton,mirrors,"automata"))
    scalajs.js.eval(AutomataToVisJS(automaton,mirrors,"automata"))
  }
  catch Box.checkExceptions(errorBox,"Automata")

  private def deleteAutomaton(): Unit = {
//      println("[EvalVis] Delete: "+AutomataToVisJS.generateClearJS())
      scalajs.js.eval(AutomataToVisJS.generateClearJS())
  }
}
