package widgets

import java.util.Base64

import common.widgets.{Box, OutputArea}
import ifta.{DSL, Feat, NIFTA}
import ifta.backend.{IftaAutomata, Show}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, XMLHttpRequest, html}
import preo.ast.CoreConnector
import preo.backend.Automata

/**
  * Created by guillerminacledou on 2019-04-03
  */


class RemoteUppaalNetBox(connector:Box[CoreConnector], errorBox:OutputArea)
  extends Box[String]("Uppaal Network of TA model",List(connector)) {

    private var box:Block = _
    private var uppaalAut:String = _
    private var iftaAut:IftaAutomata = _

    override def get: String = uppaalAut

    //todo: convert ifta with fancy names first (uppaal doesn't support identifiers starting with a number).
    /**
      * Executed once at creation time, to append the content to the inside of this box
      *
      * @param div     Placeholder that will receive the "append" with the content of the box
      * @param visible is true when this box is initially visible (i.e., expanded).
      */
    override def init(div: Block, visible: Boolean): Unit = {
      box = panelBox(div, visible,buttons=List(Left("&dArr;")-> (()=>download(), "Download IFTA model as a Network of TA with variability in Uppaal")))
        .append("div")
        .attr("id", "uppaalNetBox")
      dom.document.getElementById("Uppaal Network of TA model")
        .firstChild.firstChild.firstChild.asInstanceOf[html.Element]
        .onclick = {e: MouseEvent => if (!isVisible) solveModel() else deleteModel()}
    }

    /**
      * Block of code that should read the dependencies and:
      *  - update its output value, and
      *  - produce side-effects (e.g., redraw a diagram)
      */
    override def update(): Unit = if (isVisible) solveModel()

    /** Solve feature model to create uppaal model */
    def solveModel():Unit = {
      deleteModel()
      iftaAut = Automata[IftaAutomata](connector.get)

      var nifta:NIFTA = NIFTA(iftaAut.nifta)
//      var nifta = iftaAut.getRenamedNifta
      var fmInfo =  s"""{ "fm":     "${nifta.fm}", """ +
        s"""  "feats":  "${nifta.iFTAs.flatMap(i => i.feats).mkString("(",",",")")}" }"""

      RemoteBox.remoteCall("ifta", fmInfo, showModel)
    }

    /** show uppaal model for ifta flatten in a timed automata */
    def showModel(data:String):Unit = {
      val solutions = DSL.parseProducts(data)
      val renamedSolutions:Set[Set[String]] = solutions.map(p => p.map(f => iftaAut.getRenamedFe(Feat(f),true) match {
        case Feat(n) => n
        case fe => throw new RuntimeException(s"Expected Feat(n), found: ${fe}") // should never satisfied this
      }))

      uppaalAut = DSL.toUppaal(iftaAut.getRenamedNifta,renamedSolutions)

      box.append("textarea")
        .attr("id","uppaalNetModel")
        .style("white-space","pre-wrap")
        .text(uppaalAut)

      val codemirrorJS = scalajs.js.Dynamic.global.CodeMirror
      val lit = scalajs.js.Dynamic.literal(
        lineNumbers = true, matchBrackets = true, lineWrapping = true,
        readOnly = true, theme = "default", cursorBlinkRate = -1, mode="application/xml")
      codemirrorJS.fromTextArea(dom.document.getElementById("uppaalNetModel"),lit)
    }
    // todo: perhaps this should be a reusable method, e.g. in Utils, becuase many boxes use this.
    private def download(): Unit = {
      val enc = Base64.getEncoder.encode(get.toString.getBytes()).map(_.toChar).mkString
      val filename = "uppaalNTA.xml"
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

    def deleteModel():Unit =
      box.text("")


  }

