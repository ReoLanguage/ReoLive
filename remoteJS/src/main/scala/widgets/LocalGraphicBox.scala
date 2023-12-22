package widgets

import common.widgets.{Box, OutputArea}
import hprog.ast.Syntax
import Syntax._
import hprog.backend.TrajToJS
import hprog.frontend.CommonTypes.Warnings
import hprog.frontend.Deviator
import hprog.frontend.solver.{SimpleSolver, Solver, StaticSageSolver}

class LocalGraphicBox(reload:()=>Unit, program: Box[String], eps: Box[String], bounds: Box[String], errorBox: OutputArea)
    extends Box[Unit]("Trajectories (fast/numerical)", List(program)) {
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
       .attr("id", "localGraphic")

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
        val bs = getBounds(bounds.get)
        val traj = new hprog.frontend.Traj(syntax,solver,Deviator.dummy,bs)
        val js = TrajToJS(traj,"localGraphic",range,hideCont)
        scalajs.js.eval(js)
        errorBox.clear()
      case _ => errorBox.error("Nothing to redraw.")
    }
  }
  catch Box.checkExceptions(errorBox,"Trajectories (fast/numerical)")

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
    val bs = getBounds(bounds.get)
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

  private def getBounds(str:String): (Double,Int) = {
    val trimmed = "[^/]*".r.findFirstIn(str).getOrElse("")
    "[0-9]+(\\.[0-9]+)?".r.findAllIn(trimmed).toList match {
      case List(s) => (s.toDouble, 1000)
      case List(t, l) => (t.toDouble, l.toDouble.toInt)
      case _ => (100, 1000)
    }
  }

}


