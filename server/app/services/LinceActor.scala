package services

import java.util.Properties

import akka.actor._
import hprog.ast.{SVal, Syntax}
import hprog.backend.{Show, TrajToJS}
import hprog.common.ParserException
import hprog.frontend.Semantics.{Valuation, Warnings}
import hprog.frontend.solver.{LiveSageSolver, Solver, StaticSageSolver}
import hprog.frontend.{Distance, Eval, Traj}
import hprog.lang.SageParser

import sys.process._



object LinceActor {
  def props(out: ActorRef) = Props(new LinceActor(out))
}

class LinceActor(out: ActorRef) extends Actor{
  /**
    * Reacts to messages containing a JSON with a connector (string) and a modal formula (string),
    * produces a mcrl2 model and its LPS from the connector,
    * calls mcrl2 to verify the formula,
    * wraps each into a new JSON (via process),
    * and forwards the result to the "out" actor to generate an info (or error) box.
    */
  def receive = {
    case msg: String =>
      out ! process(msg)
  }

  /**
    * Get a request to produce a graphic of a hprog.
    * @param msg
    * @return
    */
  private def process(msg: String): String = {
    val cleanMsg = msg.replace("\\\\", "\\")
      .replace("\\n", "\n")

    callSage(cleanMsg,"/home/vhaslab/Applications/SageMath")

//    if (cleanMsg.startsWith("§redraw ")) {
//      cleanMsg.drop(8).split(",", 3) match {
//        case Array(v1, v2, rest) =>
//          draw(rest, Some(v1.toDouble, v2.toDouble))
//        case _ => s"Error: Unexpected message: ${msg.drop(8)}."
//      }
//    }
//    else {
//      draw(cleanMsg, None)
//    }
  }

  private def callSage(progAndEps: String, sagePath:String): String = {
    var solver: LiveSageSolver = null
    try {
      /////
      // needs to be updated.
      // Create lazy solver - only this can "callSagesolver"
      // Reply should be
      //   - a cache of expressions solved (? sageReplies?)
      //   - a cache of diff.eqs solved (sageReplies?)
      //   - a set of warnings computed.


      // first part is the epsilon
      val splitted = progAndEps.split("§",2)
      val arg = splitted(0) //.toDouble
      val prog = splitted(1)

      val syntax = hprog.DSL.parse(prog)
      //    val eqs = hprog.frontend.Utils.getDiffEqs(syntax)
      //    val replySage = LiveSageSolver.callSageSolver(eqs,sagePath,timeout=20)

      // re-evaluating trajectory to get warnings
      //    val solver = new StaticSageSolver(eqs,replySage)

      solver = new LiveSageSolver(sagePath)

      arg.headOption match {
        case Some('G') => getSolver(arg.drop(1).toDouble,solver,syntax)
        case Some('E') => getEval(arg.drop(1),solver,syntax)
        case _ => s"Error: unrecognised argument ${arg}"
      }

    }
    catch {
      case p:ParserException =>
        //debug(()=>"failed parsing: "+p.toString)
        if (solver != null) {solver.closeWithoutWait()}
        s"Error: When parsing $progAndEps - ${p.toString}"
      case e:Throwable =>
        if (solver != null) {solver.closeWithoutWait()}
        "Error "+e.toString //+" # "+ e.getMessage +" # "+ e.getStackTrace.mkString("\n")
    }
  }

  private def getSolver(eps:Double, solver: LiveSageSolver, syntax: Syntax): String = {

    val traj = hprog.frontend.Semantics
      .syntaxToValuation(syntax,solver,new Distance(eps))
      .traj(Map())
    traj.warnings.foreach(solver += _)

    val replySage = solver.exportAll

    debug(()=>s"exporting: \n${replySage.split("§§§")
      .map(" - "+_)
      .mkString("\n")}")

    solver.closeWithoutWait()
    replySage
  }

  private def getEval(expr:String, solver: LiveSageSolver, syntax: Syntax): String = {

    val traj = hprog.frontend.Semantics
      .syntaxToValuation(syntax,solver,new Distance(0.0))
      .traj(Map())
    val sexpr = hprog.DSL.parseExpr(expr)
    val dur = traj.dur

    val t = Eval(sexpr,0,Map())
    val d = dur.map(Eval(_))

    if (t < 0.0 || (d.nonEmpty && d.get < t)) { // comparisons still not in the Solver
      solver.closeWithoutWait()
      s"Error: time value $t out of bounds."
    }
    else {
      val point = traj(Eval.update(sexpr, SVal(0), Map()))(solver)

      debug(()=>s"sexpr/t/point = ${Show(sexpr)}, ${Show(Eval.update(sexpr, SVal(0), Map()))}, ${Show(point)}")

      val res = point.map(kv => " - " + kv._1 + "§" + Show(kv._2)).mkString("§§")
      debug(() => s"exporting eval result: \n$res")

      solver.closeWithoutWait()
      res
    }
  }

  private def debug(str: () => String): Unit = {
    //println("[Server] "+str())
  }


  //  private def buildWarnings(warns: Warnings): String = {
//    // "double" space "strings splitted by §"
//    // all splitted by §§
//    val s = warns.map(kv => s"${kv._1} ${kv._2._1.mkString("§")}").mkString("§§")
//    val toCheck = warns.values.flatMap(_._2)
//    toCheck.foreach(elem =>
////      println(s"Check:\n ${elem._1} knowning ${elem._2}")
//      println("--> "+LiveSageSolver.genSage(elem._1,elem._2))
//    )
//    //println(s"warnings: $s")
//    s
//  }

//  private def draw(msg: String, range: Option[(Double,Double)]): String =
//    try {
//        //println(s"building trajectory with range $range from $msg")
//        val syntax = hprog.DSL.parse(msg)
//        //      println("a")
//        //      val (traj,_) = hprog.ast.Trajectory.hprogToTraj(Map(),prog)
//        val prog = hprog.frontend.Semantics.syntaxToValuation(syntax)
//
//        /////
//        // tests: to feed to Sage
//        //      var sages = List[String]()
//        //      val systems = Solver.getDiffEqs(syntax)
//        //      val solver = new SageSolver("/home/jose/Applications/SageMath")
//        //      solver.batch(systems)
//        //      for ((eqs,repl) <- solver.cached) {
//        //        sages ::= s"## Solved(${eqs.map(Show(_)).mkString(", ")})"
//        ////        sages ::= repl.mkString(",")
//        //      }
//
//        val traj = prog.traj(Map())
//
//        TrajToJS(traj,range)
//    }
//    catch {
//      case p:ParserException =>
//        //println("failed parsing: "+p.toString)
//        "Error: "+p.toString
//      case e:Throwable => "Error "+e.toString
//    }


}
