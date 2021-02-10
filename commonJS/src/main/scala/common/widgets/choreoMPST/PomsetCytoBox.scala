package common.widgets.choreoMPST

//import choreo.DSL
//import common.frontend.CytoscapePomset
import common.widgets.{Box, OutputArea}

/**
  * Created by guillecledou on 05/11/2020
  */


class PomsetCytoBox(choreo: Box[String], errorBox: OutputArea)
  extends Box[String]("Pomset of the choreography via cytoscape.js", List(choreo)) {

  val pomset: String = ""
  private var box: Block = _

  override def get: String = pomset

  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div     Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div: Block, visible: Boolean): Unit =
    box = panelBox(div, visible, buttons = List(
      //Right("download")-> (() => Utils.downloadSvg("pomsetBox"), "Download SVG")
    )).append("div")
      .attr("class", "pomset")
      .attr("id", "pomsetCytoBox")
      .style("height","600px")
      .style("width","100%")
      .style("display","block")


  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
  override def update(): Unit = {
    try {
//      val (choreography, channels) = DSL.parseAndValidate(choreo.get)
//      val pomsets = DSL.semantics(choreography, channels)
//      val cyto = CytoscapePomset(pomsets,"pomsetCytoBox")
//      scalajs.js.eval(cyto)
    } catch Box.checkExceptions(errorBox)
  }
}