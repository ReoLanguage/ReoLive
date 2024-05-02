package widgets

import hprog.lang.ParserConfig
import hprog.ast.SyntaxConfig
import hprog.ast.SyntaxConfig._
import common.widgets.{Box, OutputArea}
import hprog.ast.Syntax
import Syntax._
import hprog.backend.TrajToJSV2
import hprog.frontend.CommonTypes.Warnings
import hprog.frontend.Deviator
import hprog.frontend.solver.{SimpleSolver, Solver, StaticSageSolver}

class TestLocalGraphicBox(reload:()=>Unit, program: Box[String], eps: Box[String], bounds: Box[String], errorBox: OutputArea)
    extends Box[Unit]("Test Daniel (fast/numerical)", List(program)) {
  var box : Block = _
  private var lastSolver:Option[Solver] = None
  private var lastSyntax:Option[Syntax] = None

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
      println("reloading...")
      reload()
      upd()
    }, invisible = ()=>{
      println("hiding")
    })
  }

  private def redraw(range: Option[(Double,Double)],hideCont:Boolean): Unit = try {
    errorBox.message("Redrawing")
    (lastSyntax,lastSolver) match {
      case (Some(syntax),Some(solver)) =>
        val (axis, maxTime, maxIterations, graphType) = processParsedConfig(bounds.get)
        val bs = (maxTime,maxIterations)         
        val traj = new hprog.frontend.Traj(syntax,solver,Deviator.dummy,bs)
        val js = TrajToJSV2(traj,"testlocalGraphic",range,hideCont, axis, graphType)        
        scalajs.js.eval(js)
        errorBox.clear()
      case _ => errorBox.error("Nothing to redraw.")
    }
  }
  catch Box.checkExceptions(errorBox,"Test Daniel (fast/numerical)")

  override def update(): Unit = {
    if (!isVisible) {
      return
    }
    upd()
  }

  // alternative version that does NOT call Sage, and uses the numerical version instead
  private def upd()  = try {
    lastSyntax = Some(hprog.DSL.parse(program.get))    
    val (axis, maxTime, maxIterations, graphType) = processParsedConfig(bounds.get)
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

  /**
  * Processes the parsed configuration string to extract axis, max time, and max iterations values.
  *
  * @param s The configuration string.
  * @return  A tuple containing the axis, max time, max iterations and graphType values.
  */
  def processParsedConfig(s: String): (List[(String, String, Option[String])], Double, Int, String) = {
    ParserConfig.parse(s) match {
      case ParserConfig.Success(result, _) =>
        val (axis, maxTime, maxIterations, graphType) = extractValues(result.asInstanceOf[hprog.ast.SyntaxConfig.SyntaxConfig])
        (axis, maxTime, maxIterations, graphType)
      case _ =>
        println("Failed to parse the configuration.")
        (List(), 20.0, 100, "Scatter")
    }
  }
  
  /**
  * Extracts axis, max time, and max iterations values from the provided configuration.
  *
  * @param config The parsed configuration.
  * @return       A tuple containing the axis, max time, max iterations and graphType values.
  */
  def extractValues(config: hprog.ast.SyntaxConfig.SyntaxConfig): (List[(String, String, Option[String])], Double, Int, String) = {
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
    
    (axis, maxTime, maxIterations, graphType)
  }     
}


