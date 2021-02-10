package common.widgets.choreoMPST

//import choreo.DSL
//import choreo.backend.Dot.DotOps
//import choreo.backend.DotVisPomsets._
//import common.Utils
//import common.frontend.CytoscapePomset
import common.widgets.{Box, OutputArea}

/**
  * Created by guillecledou on 03/11/2020
  */


class PomsetBox(choreo: Box[String], errorBox: OutputArea)
  extends Box[String]("Pomset of the choreography via dot for Vis.js", List(choreo)) {

  val pomset:String = ""
  private var box:Block = _

  override def get: String = pomset

  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div     Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div: Block, visible: Boolean): Unit =
    box = panelBox(div, visible,buttons=List(
      //Right("download")-> (() => Utils.downloadSvg("pomsetBox"), "Download SVG")
    )).append("div")
      .attr("class","pomset")
      .attr("id", "pomsetBox")


  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
  override def update(): Unit = {
    try {
//      val (choreography,channels) = DSL.parseAndValidate(choreo.get)
//      val pomsets = DSL.semantics(choreography,channels)
//      val initDot =
//        s"""
//           |  var display = document.getElementById('pomsetBox');
//           |  var dot = `
//           |    ${pomsets.toDot}
//           |  `
//           |  var network = vis.parseDOTNetwork(dot);
//           |
//           |  var options = network.options;
//           |
//           |  options.height = '600px'; //Math.round($$(window).height() * 0.45) + 'px';
//           |  options.hierarchicalLayout = {
//           |    enabled : true,
//           |    direction : "LR",
//           |    layout : "direction"
//           |  }
//           |
//           |  var graph = new vis.Network(display,network,options);
//            """.stripMargin
//      scalajs.js.eval(initDot)
    } catch Box.checkExceptions(errorBox)
  }

}
