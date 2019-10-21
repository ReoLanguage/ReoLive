package widgets.Virtuoso

import java.util.Base64

import common.widgets.{Box, CodeBox, OutputArea}
import hub.DSL
import hub.backend.{Show, Simplify, Uppaal}
import org.scalajs.dom.XMLHttpRequest
import preo.ast.CoreConnector
import widgets.RemoteBox


/**
  * Created by guillecledou on 2019-10-04
  */


class RemoteVerifytaBox(connector: Box[CoreConnector], connectorStr:Box[String],expandedBox:OutputArea, outputBox: OutputArea, defaultText:String = "") extends
  Box[String]("Temporal Logic", List(connector)) with CodeBox  {

  override protected var input: String = defaultText
  override protected val boxId: String = "temporalLogicArea"
  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
    List(
      Right("glyphicon glyphicon-refresh")-> (()=>reload(),"Check if the property holds (shift-enter)"),
      Left("&dArr;")-> (()=>download(), "Download query in temporal logic for Uppaal")
    )


  override def reload(): Unit = doOperation("check")

  protected var operation:String = "check"

  private def doOperation(op:String): Unit = {
    outputBox.clear()
    expandedBox.clear()
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
    if (receivedData.startsWith("error:")) outputBox.error(receivedData.drop(6)) //drop error:
    else {
//      DSL.parseFormula(input) match {
//        case Left(err) => expandedBox.error(err)
//        case Right(list) => expandedBox.message("Expanded Formulas:\n"+ list.map(f=>Show(Simplify(Uppaal.toVerifyta(f)))).mkString("\n"))
//      }
      outputBox.message(receivedData.drop(3) //drop ok:
        //.replaceFirst("\\[2K","")
        .replaceAll("\\[2K","")
        .replaceAll("Verifying formula ([0-9]+) at \\/tmp\\/uppaal_([0-9]*)\\.q:[0-9]*","\n")
        .replaceFirst("\\n","")
        .split("\\n").zipWithIndex.map(f=> s"(${f._2+1}) ${f._1}").mkString("\n"))
    }
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
        outputBox.error(x.responseText)
      }
    }
    x.send()
  }
}
