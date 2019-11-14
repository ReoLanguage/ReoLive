package common.widgets.virtuoso

import java.util.Base64

import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, XMLHttpRequest, html}
import hub.{DSL, HubAutomata}
import common.widgets.{Box, OutputArea}
import preo.ast.CoreConnector
import preo.backend.Automata

/**
  * Created by guillecledou on 2019-09-30
  */


class VirtuosoUppaalBox(connector:Box[CoreConnector], errorBox:OutputArea)
  extends Box[String]("Uppaal Model",List(connector)) {

  private var box:Block = _
  private var uppaal:String = _
  private var hub:HubAutomata = _

  override def get: String = uppaal

  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div     Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible,buttons=List(Right("download")-> (()=>download(), "Download model as a TA in Uppaal")))
      .append("div")
      .attr("id", "uppaalHubBox")

    dom.document.getElementById("Uppaal Model")
      .firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if (!isVisible) showModel() else deleteModel()}
  }

  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
  override def update(): Unit = if (isVisible) showModel()

  /** Show uppaal model for composed hub as a timed automata */
  def showModel():Unit = {
    deleteModel()
    hub = Automata[HubAutomata](connector.get).serialize.simplify
    uppaal = DSL.toUppaal(hub)

    box.append("textarea")
      .attr("id","uppaalHubModel")
      .style("white-space","pre-wrap")
      .text(uppaal)

    val codemirrorJS = scalajs.js.Dynamic.global.CodeMirror
    val lit = scalajs.js.Dynamic.literal(
      lineNumbers = true, matchBrackets = true, lineWrapping = true,
      readOnly = true, theme = "default", cursorBlinkRate = -1, mode="application/xml")
    codemirrorJS.fromTextArea(dom.document.getElementById("uppaalHubModel"),lit)
  }

  // todo: perhaps this should be a reusable method, e.g. in Utils, because many boxes use this.
  private def download(): Unit = {
    val enc = Base64.getEncoder.encode(get.toString.getBytes()).map(_.toChar).mkString
    val filename = "virtuosoUppaal.xml"
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

  def deleteModel():Unit = {
    box.text("")
  }
}
