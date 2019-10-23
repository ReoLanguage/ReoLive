package common.widgets.virtuoso

import java.util.Base64

import common.widgets.{Box, CodeBox, OutputArea}
import hub.{DSL, HubAutomata}
import hub.analyse.{TemporalFormula, UppaalFormula}
import hub.backend.{Show, Simplify, Uppaal}
import org.scalajs.dom.XMLHttpRequest
import preo.ast.CoreConnector
import preo.backend.Automata

/**
  * Created by guillecledou on 2019-10-06
  */


class VirtuosoTemporalBox(connector: Box[CoreConnector], default: String, outputBox: OutputArea)
  extends Box[String]("Temporal Logic", List(connector)) with CodeBox {

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
    outputBox.clear()

    DSL.parseFormula(input) match {
      case Left(err) => outputBox.error(err)
      case Right(forms) =>
        outputBox.message("Parsed formulas:\n" +
          forms.zip(expandFormulas(forms)).map(f=> s"Original: ${Show(f._1)} \nExpanded: ${Show(Simplify(f._2))}").mkString("\n"))
    }
  }
  catch Box.checkExceptions(outputBox, "Temporal-Logic")

  private def expandAnddownload():Unit = try {
    DSL.parseFormula(input) match {
      case Left(err) => outputBox.error(err)
      case Right(forms) =>
        expandedFormulas = expandFormulas(forms).map(f=>Show(Simplify(f))).mkString("\n")
        download()
    }
  }catch Box.checkExceptions(outputBox,"Temporal-Logic")

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
        outputBox.error(x.responseText)
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
}