package services

import java.io.FileInputStream
import java.util.Properties

import akka.actor._
import hprog.ast.SymbolicExpr.SyExprAll
import hprog.ast.SVal
import hprog.ast.Syntax.Syntax
import hprog.backend.Show
import hprog.common.{ParserException, TimeOutOfBoundsException, TimeoutException}
import hprog.frontend.CommonTypes.Valuation
import hprog.frontend.Traj.Logger
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

    val props: Properties = new Properties
    props.load(new FileInputStream("global.properties"))
    val sagePath = props.getProperty("sagePath")

    val res = callSage(cleanMsg,sagePath)
//    println(s"sending reply: $res")
    res
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
      progAndEps.split("§",4) match {
        case Array(command,boundsStr,input,prog) =>

          //println(s"command: $command, bounds: $boundsStr, input: $input")
          val bounds = getBounds(boundsStr)
          //println(s"new bounds: $bounds")
          val syntax = hprog.DSL.parse(prog)
          solver = new LiveSageSolver(sagePath)

          command match {
            case "G" => getSolver(bounds,input.toDouble,solver,syntax)
            case "E" => getEval(bounds,input,solver,syntax)
            case _ => s"Error: unrecognised argument ${command}"
          }
        case _ =>
          s"Error: unrecognised message by Lince's server: $progAndEps"

      }



//      val arg = splitted(0) //.toDouble
//      val bounds = splitted(1)
//      val prog = splitted(2)
//

//      solver = new LiveSageSolver(sagePath)

//      arg.split("§",2) match {
//        case Array(hd,tl) if hd.headOption.contains('G') =>
//          getSolver(arg.drop(1).toDouble,solver,syntax)
//      }

//      arg.headOption match {
//        case Some('G') => getSolver(arg.drop(1).toDouble,solver,syntax)
//        case Some('E') => getEval(arg.drop(1),solver,syntax)
//        case _ => s"Error: unrecognised argument ${arg}"
//      }

    }
    catch {
      case p:ParserException =>
        //debug(()=>"failed parsing: "+p.toString)
        if (solver != null) {solver.closeWithoutWait()}
        s"Error: When parsing $progAndEps - ${p.toString}"
      case t:TimeoutException =>
        if (solver != null) {solver.closeWithoutWait()}
        s"Error: ${t.getMessage}"
      case t:TimeOutOfBoundsException =>
        s"Error: ${t.getMessage}"
      case e:Throwable =>
        if (solver != null) {solver.closeWithoutWait()}
//        "Error "+e.toString +" # "+ e.getMessage +" # "+ e.getStackTrace.mkString("\n")
        "Error "+e.toString +" # "+ e.getMessage
    }
  }

  private def getSolver(bounds: (Double,Int), eps:Double, solver: LiveSageSolver, syntax: Syntax): String = {

    val traj = new Traj(syntax, solver, new Distance(eps),bounds)
//    println("=== Running Sage to get solver")
    traj.doFullRun // fill caches of the solver + warnings & notes
//    println("=== Done Running Sage to get solver")

    traj.getWarnings
        .foreach(warns => solver.addWarnings(warns.toList))

    val replySage = solver.exportAll

    debug(()=>s"exporting: \n${replySage.split("§§§")
      .map(" - "+_)
      .mkString("\n")}")

    solver.closeWithoutWait()
//    println("=== Done wrapping Sage - replying")
    replySage
  }

  private def getEval(bounds: (Double,Int), expr:String, solver: LiveSageSolver, syntax: Syntax): String = {

    val traj = new Traj(syntax,solver, new Distance(0.0),bounds)
    val sexpr = hprog.DSL.parseExpr(expr)
//    val dur = traj.getDur

    val t    = Eval(sexpr,0,Map())
    val texp = Eval.update(sexpr, SVal(0), Map():Valuation)
//    val d    = dur.map(Eval(_))

    if (t < 0.0) { // || (d.nonEmpty && d.get < t)) { // comparisons still not in the Solver
      solver.closeWithoutWait()
      s"Error: time value $t must be non-negative."
    }
    else {
      val logger = new Logger()
      val point = traj.eval(texp,logger).get //  traj(texp)(solver)

      debug(()=>s"sexpr/t/point = ${
        Show(sexpr)}, ${
        Show(texp)}, ${
        Show(point._1)}")

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
      val x = point._1
      val tc = point._2
      val res = x.map(kv =>
        " - " + kv._1 +
          s"§${Show(kv._2)}" + more(kv._1,s" ~ ${Eval(kv._2)} ~ ${
//      ~ ${
//            Show(traj.fun.getOrElse(kv._1,SVal(0)))}]")
//            Show.pp(
              //Eval.simplifyMan(sol.getOrElse(kv._1,SVal(0)))
              //traj.eval(texp)
//            )
//        }"))
        if(tc.e.isEmpty) "Open value at the end of the trajectory."
        else
        Show(tc.e.getOrElse(kv._1,SVal(0))).replace("_t_","t") +
          " @ " + Show(tc.t)
        }"))
        .mkString("§§")
      val res2 = res+"§§§"+logger.getWarnings.map(wr => s"${Eval(wr._1)}§${wr._2}").mkString("§§")
      debug(() => s"exporting eval result: \n$res2")

      solver.closeWithoutWait()
      res2
    }
  }

  private def getBounds(str:String): (Double,Int) = {
    val trimmed = "[^/]*".r.findFirstIn(str).getOrElse("")
    "[0-9]+(\\.[0-9]+)?".r.findAllIn(trimmed).toList match {
      case List(s) => (s.toDouble,1000)
      case List(t,l) => (t.toDouble,l.toDouble.toInt)
      case _ => (100,1000)
    }
  }

  private def debug(str: () => String): Unit = {
    //println("[Server] "+str())
  }

}
