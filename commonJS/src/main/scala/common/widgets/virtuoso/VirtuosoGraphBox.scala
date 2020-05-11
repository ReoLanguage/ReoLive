package common.widgets.virtuoso

import common.frontend.GraphsToJS
import common.widgets.{Box, GraphBox, OutputArea}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import preo.ast.CoreConnector
import preo.backend.Circuit


/**
  * Created by guillecledou on 31/01/2019
  */


class VirtuosoGraphBox(dependency: Box[CoreConnector], errorBox: OutputArea)
  extends GraphBox(dependency,errorBox) {

  override def init(div: Block, visible: Boolean): Unit = {
    box = GraphBox.appendSvg(super.panelBox(div,visible,
      buttons = List(
        Right("help") -> (()=>
          common.Utils.goto("https://hubs.readthedocs.io/en/latest/tutorial.html#circuit-of-the-instance"),
          "See documentation for this widget"),
        Right("download")-> (() => saveSvg(),"Download image as SVG")
      )),"circuit")
    dom.document.getElementById(title).firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => update()}
  }

  override protected def drawGraph(): Unit = try{
    graph =  Circuit.connToVirtuosoGraph(dependency.get,true)//Graph.connToNodeGraph(dependency.get,true)
    val size = graph.nodes.size
    val factor = Math.sqrt(size * 10000 / (densityCirc * widthCircRatio * heightCircRatio))
    val width = (widthCircRatio * factor).toInt
    val height = (heightCircRatio * factor).toInt
    box.attr("viewBox", s"00 00 $width $height")
    //println("Drawing graph - source: "+dependency.get)
    //println("Drawing graph - produced: "+ graph)
//        toJs(graph)
        scalajs.js.eval(GraphsToJS.toVirtuosoJs(graph))
  }
  catch Box.checkExceptions(errorBox)
}
