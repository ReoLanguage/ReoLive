package widgets.Virtuoso

import java.util.Base64

import common.widgets._
import hub.analyse.{TemporalFormula, UppaalFormula, UppaalStFormula}
import hub.{DSL, HubAutomata, Utils}
import hub.backend.{Show, Simplify, Uppaal}
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
      Right("glyphicon glyphicon-refresh")-> (()=>reload(),"Check if the property holds (shift-enter)")//,
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
    // get a map from port number to shown name (from hub)
    val interfaces:Map[Int,String] = (hub.getInputs ++ hub.getOutputs).map(p=>p->hub.getPortName(p)).toMap

    // create an uppaal model for simple formulas
    val ta = Set(Uppaal.mkTimeAutomata(hub))

    // map each formula to a custom network of TA to verify such formula (simple formulas are maped to the same based TA
    val formulas2nta:List[(TemporalFormula,Set[Uppaal])] =
      formulas.map(f => if (f.hasUntil || f.hasBefore || f.hasEvery) (f,Uppaal.fromFormula(f,hub)) else  (f,ta))

    // get init location from main uppaal model (hub)
    val formulas2ntaInit = formulas2nta.map(f => (f._1,f._2,f._2.find(t=> t.name=="Hub").get.init))

    // map each formula to its expanded formula for uppaal
    val formula2nta2uf:List[(TemporalFormula,UppaalFormula,Set[Uppaal])] =
      formulas2ntaInit.map(f=>(f._1,Uppaal.toUppaalFormula(
        f._1,
        f._2.flatMap(ta=>ta.act2locs.map(a=> interfaces(a._1)->a._2)).toMap, interfaces.map(i => i._2->i._1),f._3),
        f._2))

    // simplify formulas and convert them to a string suitable for uppaal
    val formulasStr: List[(String,String,String)] =
      formula2nta2uf.map(f => (Show(f._1),Show(Simplify(f._2)),Uppaal(f._3)))

    // get groups of calls to verifyta (formulas, response)
    val f2res:List[(String,String)] = Utils.parseMultipleVerifyta(response)
//    println("Groups:\n"+f2res.mkString("\n"))
    // map each formula to its corresponding result (when the call succeeded, otherwise output error
    val parseResponses:Map[String,String] =
      f2res.flatMap(r => Utils.parseVerifytaResponse(r._2) match {
      case Left(err) => errorBox.error(err); List()
      case Right(res) => r._1.split("\n").zip(res).toList
    }).toMap


    // show in order
    var orderedResults:List[Option[Boolean]] = formulas.map(f=>
      if (parseResponses.isDefinedAt(Show(f)))
        Some(Utils.isSatisfiedVerifyta(parseResponses(Show(f))))
      else None)

    // show results with the extra information (show only results for ok verifyta call)

    val show = formulasStr.zip(orderedResults).filter(r=>r._2.isDefined)

    outputBox.setResults(show)

  } catch Box.checkExceptions(errorBox,"Temporal-Logic")


}
