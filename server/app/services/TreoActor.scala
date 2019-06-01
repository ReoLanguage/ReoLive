package services

import akka.actor._
import nl.cwi.reo.Compiler
import nl.cwi.reo.templates.{Protocol, Transition}

import scala.collection.JavaConverters._


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
          .map(kv => s"  ${kv._1} := ${if (kv._2==null) "*" else kv._2}")
          .sorted
          .mkString("\n")
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
    transition.getGuard.toString.drop(1).dropRight(1)+
      "  ->\n"+
      pp(transition.getOutput.asScala.toList)+
      pp(transition.getMemory.asScala.toList)
  }
  private def pp[A,B](l:List[(A,B)]): String =
    if (l.isEmpty) ""
    else l.map(p => s"     - ${p._1} := ${p._2}").mkString("\n") + "\n"



}
