package widgets.feta

import common.Utils
import common.frontend.MermaidJS
import common.widgets.{Box, OutputArea}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import fta.{DSL, Specification}
import ifta.{DSL => FDSL}
import widgets.RemoteBox

/**
 * Created by guillecledou on 21/04/2021
 */

class FetaGraphBox(code: Box[String], errorBox: OutputArea)
  extends Box[Unit]("FETA", List(code)) {

  val feta:String = ""
  private var box:Block = _
  var spec:Specification = _

  override def get:Unit= ()

  /**
   * Executed once at creation time, to append the content to the inside of this box
   *
   * @param div     Placeholder that will receive the "append" with the content of the box
   * @param visible is true when this box is initially visible (i.e., expanded).
   */
  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible, buttons = List(
      Right("download") -> (() => Utils.downloadSvg("svgFeta"), "Download SVG")
    )).append("div")
      .attr("class", "mermaid")
      .attr("id", "fetaGraphBox")
      .style("text-align", "center")
      .append("div").attr("id", "svgFeta")

    dom.document.getElementById("FETA").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { e: MouseEvent => if (!isVisible) showGraph() }
  }

    /**
     * Block of code that should read the dependencies and:
     *  - update its output value, and
     *  - produce side-effects (e.g., redraw a diagram)
     */
    override def update(): Unit = if(isVisible) showGraph()

    def showGraph():Unit = {
      try {

        spec = DSL.parse(code.get)
        val fm = spec.fm
        val fmInfo = s"""{ "fm":     "${fm.simplify.toString}", """ +
                     s"""  "feats":  "${spec.fcas.flatMap(f=>f.features).mkString("(",",",")")}" }"""

        RemoteBox.remoteCall("ifta", fmInfo, showGraph)

      } catch Box.checkExceptions(errorBox)
    }

    def showGraph(data:String):Unit = try {
      val products = FDSL.parseProducts(data)
      val feta = DSL.interpretInServer(spec,products)
      val mermaid = DSL.toMermaid(feta)
      val mermaidJs = MermaidJS(mermaid,"fetaGraphBox","svgFeta")
      scalajs.js.eval(mermaidJs)
    } catch Box.checkExceptions(errorBox)
  }
