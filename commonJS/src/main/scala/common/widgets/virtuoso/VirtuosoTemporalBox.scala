package common.widgets.virtuoso

import java.util.Base64

import common.widgets._
import hub.{DSL, HubAutomata, Utils}
import hub.analyse.{TemporalFormula, UppaalFormula}
import hub.backend._
import org.scalajs.dom.XMLHttpRequest
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
      Right("refresh") -> (() => reload, "Load the logical formula (shift-enter)")//,
//      Left("&dArr;")-> (()=>expandAnddownload(), "Download query in temporal logic for Uppaal")
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