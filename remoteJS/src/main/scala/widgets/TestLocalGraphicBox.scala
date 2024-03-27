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
//  private var lastWarnings:Option[Warnings] = None

  override def get: Unit = {}


  override def init(div: Block, visible: Boolean): Unit = {
    box = super.panelBox(div,visible,
      buttons = List(
        Right("refresh")-> (()=>redraw(None,hideCont = true),"Reset zoom and redraw (shift-enter)"),
        Left("resample")  -> (() => resample(hideCont = true), "Resample: draw again the image, using the current zooming window"),
        Left("all jumps") -> (() => resample(hideCont = false),"Resample and include all boundary nodes")
//        Left("&dArr;")-> (() => saveSvg(),"Download image as SVG")
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
        val (axis, maxTime, maxIterations) = processParsedConfig(bounds.get)
        val bs = (maxTime,maxIterations) 
        val traj = new hprog.frontend.Traj(syntax,solver,Deviator.dummy,bs)
        val js = TrajToJSV2(traj,"testlocalGraphic",range,hideCont, axis)
        scalajs.js.eval(js)
        errorBox.clear()
      case _ => errorBox.error("Nothing to redraw.")
    }
  }
  catch Box.checkExceptions(errorBox,"Test Daniel (fast/numerical)")

  override def update(): Unit = {
    if (!isVisible) {
//      errorBox.message("traj. invisible")
      return
    }
//    else
//      errorBox.message("traj. visible - working")
    upd()
  }

  // alternative version that does NOT call Sage, and uses the numerical version instead
  private def upd()  = try {
    //errorBox.message("Using numerical version...")
    lastSyntax = Some(hprog.DSL.parse(program.get))    
    val (axis, maxTime, maxIterations) = processParsedConfig(bounds.get)
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
//    errorBox.message("Redrawing. Waiting for SageMath...")
//    RemoteBox.remoteCall("linceWS",s"§redraw $range, ${dependency.get}",draw)
  }

  private def getEps: Double = try {
    eps.get.toDouble
  }
  catch {
    case e: Throwable =>
      errorBox.error(e.getMessage)
      0.0
  }

   def processParsedConfig(s: String): (List[String], Double, Int) = {
    ParserConfig.parse(s) match {
      case ParserConfig.Success(result, _) =>
        val bounds = extractValues(result.asInstanceOf[hprog.ast.SyntaxConfig.SyntaxConfig])
        bounds
      case _ =>
        println("Failed to parse the configuration.")
        (List(), 20.0, 100)
    }
  }

  def extractValues(config: hprog.ast.SyntaxConfig.SyntaxConfig): (List[String], Double, Int) = {
    val axis = config.axis.v.map(_.v.replaceAll("\"", ""))
    val maxTime = config.maxTime.v
    val maxIterations = config.maxIterations.v.toInt

    (axis, maxTime, maxIterations)
  }

}


