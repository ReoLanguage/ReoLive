package widgets.Virtuoso

import java.util.Base64

import common.widgets._
import hub.analyse.{TemporalFormula, UppaalFormula, UppaalStFormula}
import hub.{DSL, HubAutomata, Utils}
import hub.backend._
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, XMLHttpRequest, html}
import preo.ast.CoreConnector
import preo.backend.Automata
import widgets.RemoteBox


/**
  * Created by guillecledou on 2019-10-04
  */


class RemoteVerifytaBox(connector: Box[CoreConnector], connectorStr:Box[String],errorBox:OutputArea, outputBox: VerifytaOutputArea, defaultText:String = "") extends
  Box[String]("Temporal Logic", List(connector)) with CodeBox with Setable[String] {

  override protected var input: String = defaultText
  override protected val boxId: String = "temporalLogicArea"
  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
    List(
      Right("refresh")-> (()=>reload(),"Check if the property holds (shift-enter)")//,
      //Left("&dArr;")-> (()=>download(), "Download query in temporal logic for Uppaal")
    )


  override def reload(): Unit = doOperation("check")

  protected var operation:String = "check"

  private def doOperation(op:String): Unit = {
    outputBox.clear()
    errorBox.clear()
    operation = op
    update()
    callVerifyta()
  }

  private def callVerifyta(): Unit = {
    val msg = s"""{ "query": "$input","""+
      s""" "connector" : "${connectorStr.get}", """+
      s""" "operation" : "$operation" }"""
    RemoteBox.remoteCall("verifyta",msg,process)
  }

  override protected val codemirror: String = "temporal"

  // todo: perhaps this should be a reusable method, e.g. in Utils, because many boxes use this.
  private def download(content:String,file:String): Unit = {
    val enc = Base64.getEncoder.encode(content.getBytes()).map(_.toChar).mkString
    val filename = file
    val url= "data:application/octet-stream;charset=utf-16le;base64,"+enc
    //
    val x = new XMLHttpRequest()
    x.open("GET", url, true)
    x.onload = e => {
      if(x.status == 200){
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
      else if(x.status == 404){
        errorBox.error(x.responseText)
      }
    }
    x.send()
  }

  private def process(response:String):Unit = try {
    // parsed formulas
    var formulas = DSL.parseFormula(input) match {
      case Left(err) => errorBox.error(err); List()
      case Right(list) => list
    }
    // get hub
    val hub = Automata[HubAutomata](connector.get).serialize.simplify

    // make verifyta calls
    val calls:Map[TemporalFormula,VerifytaCall] = Verifyta.createVerifytaCalls(formulas,hub)
    // parse and match results to formulas
    val results:Map[String,Either[String,List[String]]] = Verifyta.matchResults(response,calls.map(_._2).toList)

    // show results in order
    var show: List[(TemporalFormula,VerifytaCall,Option[Either[String,List[String]]])] = formulas.map(f=> (f,calls(f),Some(results(Show(f)))))

    outputBox.setResults(show)

  } catch Box.checkExceptions(errorBox,"Temporal-Logic")


}
