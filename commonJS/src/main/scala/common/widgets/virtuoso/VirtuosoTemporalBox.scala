package common.widgets.virtuoso

import java.util.Base64

import common.widgets._
import hub.{DSL, HubAutomata, Utils}
import hub.analyse.{TemporalFormula, UppaalFormula}
import hub.backend.{Show, Simplify, Uppaal}
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
      Right("glyphicon glyphicon-refresh") -> (() => reload, "Load the logical formula (shift-enter)"),
      Left("&dArr;")-> (()=>expandAnddownload(), "Download query in temporal logic for Uppaal")
    )

  override protected val codemirror: String = "temporal"

  protected var expandedFormulas:String = ""

  override def reload(): Unit = try {
    update()
    errorBox.clear()

    DSL.parseFormula(input) match {
      case Left(err) => errorBox.error(err)
      case Right(forms) =>
        outputBox.clear()
        process(forms)
    }
  }
  catch Box.checkExceptions(errorBox, "Temporal-Logic")

  private def expandAnddownload():Unit = try {
    DSL.parseFormula(input) match {
      case Left(err) => errorBox.error(err)
      case Right(forms) =>
        expandedFormulas = expandFormulas(forms).map(f=>Show(Simplify(f))).mkString("\n")
        download()
    }
  }catch Box.checkExceptions(errorBox,"Temporal-Logic")

  // todo: perhaps this should be a reusable method, e.g. in Utils, because many boxes use this.
  private def download(): Unit = {
    val enc = Base64.getEncoder.encode(expandedFormulas.toString.getBytes()).map(_.toChar).mkString
    val filename = "UppaalQuery.q"
    val url = "data:application/octet-stream;charset=utf-16le;base64," + enc
    //
    val x = new XMLHttpRequest()
    x.open("GET", url, true)
    x.onload = e => {
      if (x.status == 200) {
        scalajs.js.eval(
          s"""
            let a = document.createElement("a");
            a.style = "display: none";
            document.body.appendChild(a);
            a.href = "$url";
            a.download="$filename";
            a.text = "hidden link";
            //programatically click the link to trigger the download
            a.click();
            //release the reference to the file by revoking the Object URL
            window.URL.revokeObjectURL("$url");
          """
        )
      }
      else if (x.status == 404) {
        errorBox.error(x.responseText)
      }
    }
    x.send()
  }

  protected def expandFormulas(formulas:List[TemporalFormula]):List[UppaalFormula] = {
    val hub:HubAutomata = Automata[HubAutomata](connector.get).serialize.simplify
    val interfaces:Map[Int,String] = (hub.getInputs ++ hub.getOutputs).map(p=>p->hub.getPortName(p)).toMap
    val ta = Uppaal.mkTimeAutomata(hub)
    val act2locs = ta.act2locs.map(a => interfaces(a._1)-> a._2)
    formulas.map(f=>Uppaal.toUppaalFormula(f,act2locs,interfaces.map(i => i._2->i._1)))
  }

  def process(formulas:List[TemporalFormula]):Unit = {
    // get hub
    val hub = Automata[HubAutomata](connector.get).serialize.simplify
    // get a map from port number to shown name (from hub)
    val interfaces:Map[Int,String] = (hub.getInputs ++ hub.getOutputs).map(p=>p->hub.getPortName(p)).toMap

    // create an uppaal model for simple formulas
    val ta = Set(Uppaal.mkTimeAutomata(hub))

    // map each formula to a custom network of TA to verify such formula (simple formulas are maped to the same based TA
    val formulas2nta:List[(TemporalFormula,Set[Uppaal])] =
      formulas.map(f => if (f.hasUntil || f.hasBefore) (f,Uppaal.fromFormula(f,hub)) else  (f,ta))

    // map each formula to its expanded formula for uppaal
    val formula2nta2uf:List[(TemporalFormula,UppaalFormula,Set[Uppaal])] =
      formulas2nta.map(f=>(f._1,Uppaal.toUppaalFormula(
        f._1,
        f._2.flatMap(ta=>ta.act2locs.map(a=> interfaces(a._1)->a._2)).toMap, interfaces.map(i => i._2->i._1)),
        f._2))

    // simplify formulas and convert them to a string suitable for uppaal (formula,uppaalformula,model for such formula)
    val formulasStr: List[(String,String,String)] =
      formula2nta2uf.map(f => (Show(f._1),Show(Simplify(f._2)),Uppaal(f._3)))


    // show parsed results with the extra information

    val show = formulasStr.map(f=> (f,None))

    outputBox.setResults(show)
  }

}