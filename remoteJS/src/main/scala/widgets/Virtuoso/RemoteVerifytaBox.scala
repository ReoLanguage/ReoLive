package widgets.Virtuoso

import java.util.Base64

import common.widgets.{Box, CodeBox, OutputArea}
import org.scalajs.dom.XMLHttpRequest
import preo.ast.CoreConnector
import widgets.RemoteBox

/**
  * Created by guillecledou on 2019-10-04
  */


class RemoteVerifytaBox(connector: Box[CoreConnector],connectorStr:Box[String], errorBox: OutputArea, defaultText:String = "") extends
  Box[String]("Temporal Logic", List(connector)) with CodeBox  {

  override protected var input: String = defaultText
  override protected val boxId: String = "temporalLogicArea"
  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
    List(
      Right("glyphicon glyphicon-refresh")-> (()=>reload(),"Check if the property holds (shift-enter)"),
      Left("&dArr;")-> (()=>download(), "Download query in temporal logic for Uppaal")
    )


  override def reload(): Unit = {}

  protected var operation:String = "check"

  private def doOperation(op:String): Unit = {
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

  def process(receivedData: String): Unit = {
//    if (receivedData != "ok") {
//      val result = Loader.loadModalOutput(receivedData)
//
//      result match {
//        case Right(message) =>
//          outputBox.error(message)
//        case Left(message) =>
//          val res = message.filterNot(_=='\n')
//          outputBox.message("- "+res+" -")
//          ParserUtils.parseFormula(input) match {
//            case Left(err) => outputBox.error(err)
//            case Right(form) =>
//              val prefixes = Formula.notToHide(form).filterNot(_==Nil)
//              if (prefixes.nonEmpty)
//                outputBox.warning(s"Exposing ${prefixes.map(_.map(_.name).mkString("[", "/", "]")).mkString(",")}")
//              ParserUtils.hideBasedOnFormula(form,connector.get) match {
//                case Left(err) => outputBox.error(err)
//                case Right((_,formExpanded)) =>
//                  outputBox.warning("Expanded formula:\n" + formExpanded)
//              }
//          }
//      }
//    }
  }

  override protected val codemirror: String = "temporal"

  // todo: perhaps this should be a reusable method, e.g. in Utils, because many boxes use this.
  private def download(): Unit = {
    val enc = Base64.getEncoder.encode(get.toString.getBytes()).map(_.toChar).mkString
    val filename = "UppaalQuery.q"
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
}
