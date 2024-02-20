package widgets

import common.widgets.{Box, OutputArea}
import hprog.ast.Syntax
import Syntax._
import hprog.backend.TrajToJS
import hprog.frontend.Deviator
import hprog.frontend.CommonTypes.Warnings
import hprog.frontend.solver.{SimpleSolver, Solver, StaticSageSolver}

class TestDanielGraphicBox(reload:()=>Unit,program: Box[String], eps: Box[String], bounds: Box[String], errorBox: OutputArea)
    extends Box[Unit]("Trajectories Test Daniel (symbolic)", List(program)) {
  var box : Block = _
  private var lastSolver:Option[Solver] = None
  private var lastSyntax:Option[Syntax] = None
//  private var lastWarnings:Option[Warnings] = None

  override def get: Unit = {}

//  private val widthCircRatio = 7
//  private val heightCircRatio = 3
//  private val densityCirc = 0.5 // nodes per 100x100 px


  override def init(div: Block, visible: Boolean): Unit = {
    box = super.panelBox(div,visible,
      buttons = List(
        Right("refresh")-> (()=>redraw(None,hideCont = true),"Reset zoom and redraw (shift-enter)"),
        Left("resample")  -> (() => resample(hideCont = true), "Resample: draw again the image, using the current zooming window"),
        Left("all jumps") -> (() => resample(hideCont = false),"Resample and include all boundary nodes")
//        Left("&dArr;")-> (() => saveSvg(),"Download image as SVG")
      ))
    box.append("div")
       .attr("id", "testGraphicBox")

    toggleVisibility(visible = ()=>{
      println("reloading...")
      reload()
      callSage()
    }, invisible = ()=>{
      println("hiding")
    })

//    traj = Trajectory.hprogToTraj(Map(),dependency.get)._1

    //    dom.document.getElementById("Circuit of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
//      .onclick = {e: MouseEvent => if(!isVisible) drawGraph() else deleteDrawing()}
  }

//  override def update(): Unit = if(isVisible) {
//    deleteDrawing()
//    drawGraph()
//  }

  def draw(sageReply: String): Unit = {
    errorBox.clear()
    errorBox.message("Got reply. Drawing")
//    errorBox.message(s"got reply: ${sageReplyAndWarns}")
    if (sageReply startsWith "Error")
      errorBox.error(sageReply)
    else try {
      //println(s"got reply from sage: ${sageReply}. About to parse ${dependency.get}.")
      // repeating parsing work done at the server
      val syntax = hprog.DSL.parse(program.get)
      //println("parsed...")
      lastSyntax = Some(syntax)

//      val eqs = hprog.frontend.Utils.getDiffEqs(syntax)
      //println("got diffEqs")
//      val splitted = sageReplyAndWarns.split("§§§",2)
//      val sageReply = splitted(0).split('§')
//      val warnings = parseWarnings(splitted(1))
//      lastWarnings = Some(warnings)

      val solver = new StaticSageSolver()
      solver.importAll(sageReply)
      //println("got static solver")
      lastSolver = Some(solver)

      redraw(None,hideCont = true)
//      val prog = hprog.frontend.Semantics.syntaxToValuation(syntax,solver)
//      val traj = prog.traj(Map())
//      val js = TrajToJS(traj)        
//      scalajs.js.eval(js)
    }
    catch Box.checkExceptions(errorBox, "Trajectories Test Daniel (symbolic)")

//    sageReply.split("§").toList match {
//      case js::rest =>
//        if (js.startsWith("Error")) errorBox.error(js)
//        else scala.scalajs.js.eval(js)
//        rest match {
//          case sages :: _ => errorBox.warning("Results from SageMath:\n"+sages)
//          case _ =>
//        }
//      //    println("after eval")
//      case x =>
//        errorBox.error(s"unexpected reply from LinceWS: $x")
//    }
  }

//  private def parseWarnings(str: String): Warnings = {
//    val entries = str.split("§§")
//    val msgs = entries.filter(_!="").map(entry => {
//      val esplit = entry.split(" ",2)
//      val dbl = esplit(0).toDouble
//      val set = esplit(1).split("§").toSet
//      dbl -> set
//    }).toMap
//    msgs.mapValues(ms => (ms,Set()))
//  }


  private def redraw(range: Option[(Double,Double)],hideCont:Boolean): Unit = try {
    errorBox.message("Redrawing")
    (lastSyntax,lastSolver) match {
      case (Some(syntax),Some(solver)) =>
//        val e = getEps
//        val prog = hprog.frontend.Semantics.syntaxToValuation(syntax,solver, Deviator.dummy)
//        val traj = prog.traj(Map())
//          .addWarnings(solver.getWarnings)

        val bs = getBounds(bounds.get)
        //println(s"[GBox] bounds = $bs")
        val traj = new hprog.frontend.Traj(syntax,solver,Deviator.dummy,bs)
        solver match {
          case ssolver: StaticSageSolver => traj.addWarnings(ssolver.getWarnings)
          case _ => {}
        }

//        val traj = traj1.addWarnings(_ => warnings) // TODO: replace the warnings       
        val js = TrajToJS(traj,"testGraphicBox",range,hideCont)
        println("#######################################################")        
        scalajs.js.eval(js)
        errorBox.clear()
      case _ => errorBox.error("Nothing to redraw.")
    }
  }
  catch Box.checkExceptions(errorBox,"Trajectories Test Daniel (symbolic)")

  override def update(): Unit = {
    if (!isVisible) {
//      errorBox.message("traj. invisible")
      return
    }
//    else
//      errorBox.message("traj. visible - working")
    callSage()
  }

  // alternative version that does NOT call Sage, and uses the numerical version instead
  private def callSageNumerical() = {
    errorBox.message("Using numerical version...")
    lastSyntax = Some(hprog.DSL.parse(program.get))
    val bs = getBounds(bounds.get)
    lastSolver = Some(new SimpleSolver(bs._1))
    redraw(None, hideCont = true)
  }

  private def callSage() = {
    errorBox.message("Waiting for SageMath...")
    RemoteBox.remoteCall("linceWS",s"G§${bounds.get}§${eps.get}§${program.get}",draw)
  }

  def resample(hideCont:Boolean): Unit = {
    var range:String = ""
    try range = scalajs.js.Dynamic.global.layout.xaxis.range.toString
    catch Box.checkExceptions(errorBox, "testGraphicBox")

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


