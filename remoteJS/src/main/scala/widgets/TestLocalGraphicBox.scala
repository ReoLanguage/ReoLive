package widgets

import hprog.lang.{ParserConfig, Parser}
import hprog.ast.SyntaxConfig
import hprog.ast.SyntaxConfig._
import common.widgets.{Box, OutputArea}
import hprog.ast.Syntax
import hprog.ast.Syntax._
import hprog.ast.Syntax.GetSyntax
import hprog.backend.TrajToJSV2
import hprog.frontend.CommonTypes.Warnings
import hprog.frontend.Deviator
import hprog.frontend.solver.{SimpleSolver, Solver, StaticSageSolver}

class TestLocalGraphicBox(reload:()=>Unit, program: Box[String],  ax: Box[String], maxT: Box[String], maxI: Box[String], gType: Box[String], eps: Box[String], errorBox: OutputArea)
    extends Box[Unit]("Custom Trajectories (approximated)", List(program)) {
  var box : Block = _
  private var lastSolver:Option[Solver] = None
  private var lastSyntax: List[Syntax] = List()
  private var bounds: String = ""
  override def get: Unit = {}

  override def init(div: Block, visible: Boolean): Unit = {
    box = super.panelBox(div,visible,
      buttons = List(
        Right("refresh")-> (()=>redraw(None,hideCont = true),"Reset zoom and redraw (shift-enter)"),
        Left("resample")  -> (() => resample(hideCont = true), "Resample: draw again the image, using the current zooming window"),
        Left("all jumps") -> (() => resample(hideCont = false),"Resample and include all boundary nodes")
      ))
    box.append("div")
       .attr("id", "testlocalGraphic")

    toggleVisibility(visible = ()=>{
//      println("reloading...")
      reload()
      upd()
    }, invisible = ()=>{
//      println("hiding")
    })
  }

  private def redraw(range: Option[(Double,Double)],hideCont:Boolean): Unit = try {
    errorBox.message("Redrawing")

    var js: String = ""
    var traceNames: List[String] = List()
    var graph_names: List[String] = List()
    var simulationName: String = ""
    var x_Title: String = ""
    var y_Title: String = ""
    var z_Title: String = ""
    var counter: Int = 0
    var simCount: Int = 0    

    (lastSyntax,lastSolver) match {
      case (syntax,Some(solver)) =>
        val (axis, maxTime, maxIterations, graphType, perturbationUpTo) = processParsedConfig(bounds)
        val bs = (maxTime,maxIterations) 

        syntax.foreach { element =>        
          if (syntax.length == 1) {
            simulationName = ""
          } else {
            simulationName = " - Sim " + simCount.toString
          }          
          simCount += 1
          val traj = new hprog.frontend.Traj(element, solver, Deviator.dummy, bs)
          val (jsCode, graphNames, warningsNames, xTitle, yTitle, zTitle, count) = TrajToJSV2(traj, "testlocalGraphic", range, hideCont, axis, graphType, simulationName, counter)
          js += jsCode
          traceNames = traceNames ++ graphNames ++ warningsNames
          graph_names = graph_names ++ graphNames
          x_Title = xTitle
          y_Title = yTitle
          z_Title = zTitle
          counter += 1
        }               

        if (z_Title.isEmpty){
          //val (markers, markersNames, movingPart) = createMovingObjects2D(graph_names, "testlocalGraphic", graphType)
          //traceNames = markersNames ++ traceNames
          //js += markers
          js += s"\nvar data = ${traceNames.mkString("[",",","]")};"   
          js += s"""var layout = {hovermode:'closest', xaxis: {title: "$x_Title"}, yaxis: {title: "$y_Title"}};"""
          js += s"\nPlotly.newPlot('testlocalGraphic', data, layout, {showSendToCloud: true});" 
          //js += movingPart    
        } else{
          js += s"var data = ${traceNames.mkString("[",",","]")};"  
          js += s"""\n var layout = {hovermode:'closest', scene: {xaxis: {title: "$x_Title"}, yaxis: {title: "$y_Title"}, zaxis: {title: "$z_Title"}}};"""
          js += s"\nPlotly.newPlot('testlocalGraphic', data, layout, {showSendToCloud: true});"
        }   
//        println(js)
        scalajs.js.eval(js)
        errorBox.clear()
      case _ => errorBox.error("Nothing to redraw.")
    }
  }
  catch Box.checkExceptions(errorBox,"Custom Trajectories (approximated)")

  override def update(): Unit = {
    if (!isVisible) {
      return
    }
    upd()
  }

  // alternative version that does NOT call Sage, and uses the numerical version instead
  private def upd(): Unit = try {
    bounds = buildBounds(ax.get, maxT.get, maxI.get, gType.get, eps.get)
    val (axis, maxTime, maxIterations, graphType, perturbationUpTo) = processParsedConfig(bounds)
    lastSyntax = processParsedSyntax(program.get)  
    val bs = (maxTime,maxIterations) 
    lastSolver = Some(new SimpleSolver(bs._1))
    redraw(None, hideCont = true)
  }
  catch Box.checkExceptions(errorBox, "Parsing and solving (fast/numerical)")


  def resample(hideCont:Boolean): Unit = {
    var range:String = ""
    try range = scalajs.js.Dynamic.global.layout.xaxis.range.toString
    catch Box.checkExceptions(errorBox, "Drawing trajectories (fast/numerical)")

    range.split(",", 2) match {
      case Array(v1, v2) =>
          redraw(Some(v1.toDouble, v2.toDouble),hideCont)
      case Array() => redraw(None,hideCont)
      case _ => errorBox.error(s"Error: Unexpected range: $range.")
    }
  }

  private def getEps: Double = try {
    eps.get.toDouble
  }
  catch {
    case e: Throwable =>
      errorBox.error(e.getMessage)
      0.0
  }

  def buildBounds(axis: String, maxTime: String, maxIterations: String, graphType: String, perturbation: String): String = {
    var bounds: String = ""
    if(!axis.isEmpty){
      bounds = "Axis:" + axis + ","
    }
    if(!maxTime.isEmpty){
      bounds = bounds + "maxTime:" + maxTime + ","
    }
    if(!maxIterations.isEmpty){
      bounds = bounds + "maxIterations:" + maxIterations + ","
    }
    if(!graphType.isEmpty){
      bounds = bounds + "graphType:" + graphType + ","
    }
    if(!perturbation.isEmpty){
      bounds = bounds + "perturbationUpTo:" + perturbation
    }
    if (bounds.endsWith(",")) {
    bounds = bounds.substring(0, bounds.length - 1)
    }  
    bounds
  }

  def processParsedSyntax(s: String): List[Syntax] = {    
    Parser.parse(s) match {
      case Parser.Success(result, _) =>
        val syntaxList = GetSyntax.allSyntax
        syntaxList
      case _ => sys.error(s"Failed to parse $s")
    }
  }  

   /**
  * Processes the parsed configuration string to extract axis, max time, and max iterations values.
  *
  * @param s The configuration string.
  * @return  A tuple containing the axis, max time, max iterations and graphType values.
  */
  def processParsedConfig(s: String): (List[(String, String, Option[String])], Double, Int, String, Double) = {
    ParserConfig.parse(s) match {
      case ParserConfig.Success(result, _) =>
        val (axis, maxTime, maxIterations, graphType, perturbationUpTo) = extractValues(result.asInstanceOf[hprog.ast.SyntaxConfig.SyntaxConfig])
        (axis, maxTime, maxIterations, graphType, perturbationUpTo)
      case _ =>
        println("Failed to parse the configuration.")
        (List(), 20.0, 100, "Scatter", 0.0)
    }
  }
  
  /**
  * Extracts axis, max time, and max iterations values from the provided configuration.
  *
  * @param config The parsed configuration.
  * @return       A tuple containing the axis, max time, max iterations and graphType values.
  */
  def extractValues(config: hprog.ast.SyntaxConfig.SyntaxConfig): 
                    (List[(String, String, Option[String])], Double, Int, String, Double) = {
    val graphType = config.getGraphType.v

    val axis = graphType match {
      case "scatter" =>
        config.getAxis.v.flatMap {
          case SyntaxConfig.SingleVar(v) =>
            List(("t", "_" + v.replaceAll("\"", ""), None))
          case SyntaxConfig.PairVar(v1, v2) =>
            List(("_" + v1.replaceAll("\"", ""), "_" + v2.replaceAll("\"", ""), None))
          case SyntaxConfig.TripleVar(v1, v2, v3) =>            
            throw new Exception("Wrong axis definition")
        }
      case "scatter3d" =>
        config.getAxis.v.flatMap {
          case SyntaxConfig.SingleVar(v) =>           
            throw new Exception("Wrong axis definition")
          case SyntaxConfig.PairVar(v1, v2) =>
            List(("t", "_" + v1.replaceAll("\"", ""), Some("_" + v2.replaceAll("\"", ""))))
          case SyntaxConfig.TripleVar(v1, v2, v3) =>
            List(("_" + v1.replaceAll("\"", ""), "_" + v2.replaceAll("\"", ""), Some("_" + v3.replaceAll("\"", ""))))
        }
      case _ =>
         throw new Exception("Wrong graph type definition. Choose between scatter and scatter3d")
    }

    val maxTime = config.getMaxTime.v
    val maxIterations = config.getMaxIterations.v
    val perturbationUpTo = config.getPerturbationUpTo.v
    
    (axis, maxTime, maxIterations, graphType, perturbationUpTo)
  }   
  
  private def createMovingObjects2D(graphNames: List[String], divName:String, graphType: String): (String, List[String], String) = {
    var markers: String = ""
    var movingPart: String = ""
    var markersNames: List[String] = List()
    val markersJS = graphNames.map { 
      case (graphName) => {
        markersNames = markersNames ++ List(s"""marker_${graphName}""")
        markers += s"""
                |var marker_${graphName} = {
                |  x: [${graphName}.x[0]],
                |  y: [${graphName}.y[0]],
                |  mode: 'markers',
                |  marker: { color: 'rgb(136, 136, 136)', size: 10 },
                |  showlegend: false,
                |  type: '$graphType'
                |};""".stripMargin
      }
    }
    movingPart += "\nvar count = 0; \nsetInterval(function() {"
    val movingPartJS = graphNames.zipWithIndex.map { 
      case (graphName, idx) => 
      movingPart += s"""
              |var marker_${graphName}_x = ${graphName}.x[count % ${graphName}.x.length];
              |var marker_${graphName}_y = ${graphName}.y[count % ${graphName}.y.length];
              |Plotly.restyle('${divName}', {
              |      x: [[marker_${graphName}_x]],
              |      y: [[marker_${graphName}_y]]
              |   }, [${idx}]);""".stripMargin      
    }
    movingPart += s"""\ncount++;}, 100);"""
    (markers,markersNames, movingPart)
  }
}



