package services

import akka.actor._
import hprog.ast.SymbolicExpr.SyExprAll
import hprog.ast.{SVal, Syntax}
import hprog.backend.Show
import hprog.common.{ParserException, TimeoutException}
import hprog.frontend.CommonTypes.Valuation
import hprog.frontend.solver.LiveSageSolver
import hprog.frontend.{Distance, Eval, Traj}
import hprog.lang.SageParser



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

    callSage(cleanMsg,"/home/jose/Applications/SageMath")
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
      case t:TimeoutException =>
        if (solver != null) {solver.closeWithoutWait()}
        s"Error: ${t.toString}"
      case e:Throwable =>
        if (solver != null) {solver.closeWithoutWait()}
        "Error "+e.toString +" # "+ e.getMessage +" # "+ e.getStackTrace.mkString("\n")
    }
  }

  private def getSolver(eps:Double, solver: LiveSageSolver, syntax: Syntax): String = {

    val traj = new Traj(syntax, solver, new Distance(eps))
    traj.doFullRun() // fill caches of the solver + warnings & notes

    traj.getWarnings
        .foreach(warns => solver.addWarnings(warns.toList))

    val replySage = solver.exportAll

    debug(()=>s"exporting: \n${replySage.split("§§§")
      .map(" - "+_)
      .mkString("\n")}")

    solver.closeWithoutWait()
    replySage
  }

  private def getEval(expr:String, solver: LiveSageSolver, syntax: Syntax): String = {

    val traj = new Traj(syntax,solver, new Distance(0.0))
    val sexpr = hprog.DSL.parseExpr(expr)
    val dur = traj.getDur

    val t    = Eval(sexpr,0,Map())
    val texp = Eval.update(sexpr, SVal(0), Map():Valuation)
    val d    = dur.map(Eval(_))

    if (t < 0.0 || (d.nonEmpty && d.get < t)) { // comparisons still not in the Solver
      solver.closeWithoutWait()
      s"Error: time value $t out of bounds."
    }
    else {
      val point = traj.eval(texp).get //  traj(texp)(solver)

      debug(()=>s"sexpr/t/point = ${
        Show(sexpr)}, ${
        Show(texp)}, ${
        Show(point)}")

      // experimental: simplifying long expressions in Sage
      def simplifySageExpr(e:SyExprAll): SyExprAll =
        SageParser.parseExpr(solver.askSage(e).getOrElse("{")) match {
          case SageParser.Success(newExpr, _) =>
            newExpr
          case _: SageParser.NoSuccess =>
            throw new ParserException(s"Failed to parse Sage reply when simplifying ${Show(e)}'.")
        }

      def more(s:String,msg:String): String = {
        val id1 = s"tr1_$s"
        val id2 = s"tr2_$s"
        def showHide(i1:String,i2:String): String =
          s"""document.getElementById('$i1').style.display = 'inline';"""+
          s"""document.getElementById('$i2').style.display = 'none';"""

        s"""<a id="$id1" onclick="${showHide(id2,id1)}" style='display: inline;'> (+) </a>
           |<a id="$id2" onclick="${showHide(id1,id2)}" style='display: none;'>$msg</a>""".stripMargin
      }

      //val sol = traj.fun(texp)(solver)

      val res = point.map(kv =>
        " - " + kv._1 +
          s"§${Show(kv._2)}" + more(kv._1,s" ~ ${Eval(kv._2)}"))
//      ~ ${
//            Show(traj.fun.getOrElse(kv._1,SVal(0)))}]")
//            Show.pp(
              //Eval.simplifyMan(sol.getOrElse(kv._1,SVal(0)))
              //traj.eval(texp)
//            )
//        }"))
        .mkString("§§")
      debug(() => s"exporting eval result: \n$res")

      solver.closeWithoutWait()
      res
    }
  }

  private def debug(str: () => String): Unit = {
    //println("[Server] "+str())
  }

}
