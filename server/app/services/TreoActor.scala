package services

import java.io.PrintWriter

import akka.actor._
import nl.cwi.reo.Compiler
import nl.cwi.reo.semantics.predicates._
import nl.cwi.reo.templates.{Protocol, Transition}

import scala.collection.JavaConverters._
import scala.sys.process._

object TreoActor {
  def props(out: ActorRef) = Props(new TreoActor(out))
}

class TreoActor(out: ActorRef) extends Actor{
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
//

    var writer: java.io.PrintWriter = null
    var out: Stream[String] = Stream()
    val calcCommand = "bc"
    // strings are implicitly converted to ProcessBuilder
    // via scala.sys.process.ProcessImplicits.stringToProcess(_)
    val calcProc = calcCommand.run(new ProcessIO(
      // Handle subprocess's stdin
      // (which we write via an OutputStream)
      in => {
        writer = new java.io.PrintWriter(in)
        writer.println("1 + 2")
        writer.flush()
        writer.println("3 + 4")
        writer.flush()
      },
      // Handle subprocess's stdout
      // (which we read via an InputStream)
      out => {
//        var is = Stream.continually(out.read)
//        while(is.nonEmpty)
//          is.headOption match {
//            case Some(value) => println("next answer: "+value)
//            case None => println("end of stream")
//          }
        val src = scala.io.Source.fromInputStream(out)
        for (line <- src.getLines()) {
          println("Answer: " + line)
        }
        src.close()
      },
      // We don't want to use stderr, so just close it.
      _.close()
    ))
    writer.println(" 7 + 7")
    writer.close()

//    new ProcessIO(
//      (in: java.io.OutputStream => Unit) => {
//        val wr = new PrintWriter(in)
//        wr.println("1+2")
//        wr.println("3+4")
//        wr.close()
//      }
//      ,
//    (out: java.io.OutputStream => Unit) => {
//        val src = scala.io.Source.fromInputStream(out)
//
//      }
//    )


    val v1 = getRBS(cleanMsg,true)
    if (v1.startsWith("Error"))
      v1
    else {
      val v2 = getRBS(cleanMsg, false)
      s"## With restriction:\n$v1\n## With all ports:\n$v2"
    }
  }

  private def getRBS(msg:String, restrict:Boolean): String = {
    try {
      var res = ""
      val protocols = Compiler.compileText(msg, "main", restrict)
        .asScala
      protocols.foreach(p => {
        val trans = p.transitions
          .asScala
          .map(t => "  " + pp(t))
          .toList.sorted
          .mkString("")
        val init = p.getInitial
          .asScala
          .toList
          .map(kv => s"  ${kv._1}:=${if (kv._2==null) "*" else kv._2}")
          .sorted
          .mkString(", ")
        res += s"  ${p.getPorts.asScala.mkString(" ")}"
        if (!p.getInitial.isEmpty)
          res += s"\n  --Init--\n$init"
        if (!p.transitions.isEmpty)
          res += s"\n  --Trans--\n$trans"
      })
      res = res.replaceAll("String", "")
//      println(s"got $res")
//      println(s"default pp: ${protocols.head}")
      res
    }
    catch {
      case e: Throwable => s"Error: ${e.getMessage}"
    }
  }

  private def pp(transition: Transition): String = {
    pp(transition.getGuard) +
//    transition.getGuard.toString.drop(1).dropRight(1)+
      "  -->\n"+
      pp(transition.getOutput.asScala.toList)+
      pp(transition.getMemory.asScala.toList)
  }
  private def pp[A,B](l:List[(A,B)]): String =
    if (l.isEmpty) ""
    else l.map(p => s"     - ${p._1}:=${p._2}").mkString("\n") + "\n"

  private def pp(formula: Formula): String = formula match {
    case f:Conjunction => f.getClauses.asScala.map(pp).mkString("  ∧  ")
    case f:Disjunction => f.getClauses.asScala.map(pp).mkString("  \\/  ")
    case f:Equality => s"${f.getLHS}==${f.getRHS}"
    case f:Existential => f.toString
    case _ => formula.toString
  }


}
