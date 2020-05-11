package common.widgets.virtuoso

import common.widgets._
import hub.{DSL, HubAutomata}
import hub.analyse.TemporalFormula
import hub.backend._
import preo.ast.CoreConnector
import preo.backend.Automata

/**
  * Created by guillecledou on 2019-10-06
  */


class VirtuosoTemporalBox(connector: Box[CoreConnector], default: String, errorBox: OutputArea,outputBox:VerifytaOutputArea)
  extends Box[String]("Temporal Logic", List(connector)) with CodeBox with Setable[String] {

  override protected var input: String = default
  override protected val boxId: String = "temporalInputArea"
  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
    List(
      Right("help") -> (()=>
        common.Utils.goto("https://hubs.readthedocs.io/en/latest/tutorial.html#temporal-logic"),
        "See documentation for this widget"),
      Right("refresh") -> (() =>
        reload(),
        "Load the logical formula (shift-enter)")
    )

  override protected val codemirror: String = "temporal"

  //protected var expandedFormulas:String = ""

  override def reload(): Unit = try {
    update()
    outputBox.clear()
    errorBox.clear()
    if (input.nonEmpty) {
      DSL.parseFormula(input) match {
        case Left(err) => errorBox.error(err)
        case Right(forms) =>
          outputBox.clear()
          process(forms)
      }
    }
  }
  catch Box.checkExceptions(errorBox, "Temporal-Logic")

  def process(formulas:List[TemporalFormula]):Unit = try {
    // get hub
    val hub = Automata[HubAutomata](connector.get).serialize.simplify

    // make verifyta calls
    val calls:Map[TemporalFormula,VerifytaCall] = Verifyta.createVerifytaCalls(formulas,hub)

    // show expanded formulas and models
    var show: List[(TemporalFormula,VerifytaCall,Option[Either[String,List[String]]])] = formulas.map(f=> (f,calls(f),None))

    outputBox.setResults(show)

  } catch Box.checkExceptions(errorBox,"Temporal-Logic")

}