package widgets.Virtuoso

import java.util.Base64

import common.Utils
import common.widgets.{Box, CodeBox, OutputArea}
import hub.analyse.{TemporalFormula, UppaalFormula, UppaalStFormula}
import hub.{DSL, HubAutomata}
import hub.backend.{Show, Simplify, Uppaal}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, XMLHttpRequest, html}
import preo.ast.CoreConnector
import preo.backend.Automata
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
      Right("glyphicon glyphicon-refresh")-> (()=>reload(),"Check if the property holds (shift-enter)")//,
      //Left("&dArr;")-> (()=>download(), "Download query in temporal logic for Uppaal")
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
        mkOutput(receivedData)
//        println(receivedData)

     // println(rs)
//      outputBox.message(receivedData.drop(3) //drop ok:
//        //.replaceFirst("\\[2K","")
//        .replaceAll("\\[2K","")
//        .replaceAll("Verifying formula ([0-9]+) at \\/tmp\\/uppaal_([0-9]*)\\.q:[0-9]*","\n")
//        .replaceFirst("\\n","")
//        .split("\\n").zipWithIndex.map(f=> s"(${f._2+1}) ${f._1}").mkString("\n"))
    }
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
        outputBox.error(x.responseText)
      }
    }
    x.send()
  }

  private def mkOutput(response:String):Unit = {
    // parsed formulas
    var formulas = DSL.parseFormula(input) match {
      case Left(err) => expandedBox.error(err); List()
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
      formulas.map(f => if (f.hasUntil || f.hasBefore) (f,Uppaal.fromFormula(f,hub)) else  (f,ta))

    // map each formula to its expanded formula for uppaal
    val formula2nta2uf:List[(TemporalFormula,UppaalFormula,Set[Uppaal])] =
      formulas2nta.map(f=>(f._1,Uppaal.toUppaalFormula(
        f._1,
        f._2.flatMap(ta=>ta.act2locs.map(a=> interfaces(a._1)->a._2)).toMap, interfaces.map(i => i._2->i._1)),
        f._2))

    // simplify formulas and convert them to a string suitable for uppaal
    val formulasStr: List[(TemporalFormula,String,String)] =
      formula2nta2uf.map(f => (f._1,Show(Simplify(f._2)),Uppaal(f._3)))

    // seperate response in groups of calls to verifyta
    val groups:List[String] = response
      .replaceAll("\\[2K","")
      .split("§").toList
    println(groups)
    // get formulas and responses
    val fs2res:List[List[String]] = groups.map(g=> g.split("~").toList)
    println(fs2res)
    // match formula with response
    val rs:Map[String,String] =
      fs2res.flatMap(g=>
        (g.head.split("\n").toList
          .zip(g.last.stripMargin.split("Verifying formula ([0-9]*) at \\/tmp\\/uppaal([0-9]*)_([0-9]*)\\.q:[0-9]*").filterNot(_.matches("\\x1B"))))).toMap
    // show in order
    var results:List[String] = formulas.map(f=>rs(Show(f)))

    // show results with the extra information

    var out = outputBox.outputs
      .append("div").attr("class", "verifyta-result")
      .append("ul").attr("class","list-group list-group-flush mb-3")

    for ((((o, uf, um), res),i) <- formulasStr.zip(results).zipWithIndex) {
      //var satisfied = res.replace("\\u001B"," ").stripMargin.endsWith("Formula is satisfied.")
      var satisfied = !res.split(" ").contains("NOT")
      println("RES"+res)
      var li = out.append("li")
        .attr("class", "list-group-item lh-condensed")

      var div = li.append("div")
        .style("display","flex")
        .style("justify-content","space-between")

      var form = div.append("textarea")
        .attr("id","formula"+i)
        .attr("class", "formula-result").text(Show(o))

      var extras = div.append("span")

      //.style("align", "right")
      extras.append("span")
        .style("color",if(satisfied) "#008900" else "#972f65")
        .style("margin","5px")
        .text(if (satisfied) "✓" else "✗" )
      extras.append("a")
        .style("margin","5px")
        .attr("title","Show expanded formula")
        .attr("data-toggle","collapse")
        .attr("data-target","#collapseFormula"+i)
        .attr("class", "expand-formula").text("+")
      extras.append("a")
        .style("margin","5px")
        .attr("title","Download Uppaal model used to verify this property")
        .attr("id", "model"+i).text("m")



      li.append("div")
        .attr("class","collapse")
        .attr("id","collapseFormula"+i)
        .append("span") //textearea
        .attr("id","expandedFormula"+i)
        .attr("class","temporal-formula text-muted")
//        .style("height","10px")
        .text(uf)

      Utils.codemirror("formula"+i,"text/x-temporal")
      //Utils.codemirror("expandedFormula"+i,"text/x-temporal")

      var model = dom.document.getElementById("model"+i).asInstanceOf[html.Element]
        .onclick = {e: MouseEvent => download(um,s"uppaalModel${i}.xml")}

    }



//    box.append("textarea")
//      .attr("id","uppaalHubModel")
//      .style("white-space","pre-wrap")
//      .text(uppaal)

//    val codemirrorJS = scalajs.js.Dynamic.global.CodeMirror
//    val lit = scalajs.js.Dynamic.literal(
//      lineNumbers = false, matchBrackets = true, lineWrapping = true,
//      readOnly = true, theme = "default", cursorBlinkRate = -1, mode="text/x-temporal")
//    codemirrorJS.fromTextArea(dom.document.getElementsByClassName("temporal-formula"),lit)
  }


}
