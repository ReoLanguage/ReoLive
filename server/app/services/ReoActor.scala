package services

import akka.actor._
import preo.DSL
import preo.backend.{Automata, Graph, PortAutomata}
import preo.common.{GenerationException, TypeCheckException}
import preo.frontend.Eval
import preo.frontend.mcrl2.Model

object ReoActor {
  def props(out: ActorRef) = Props(new ReoActor(out))
}

class ReoActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String =>
      out ! process(msg)
  }

  private def process(msg: String): String = {
    var warnings: List[String] = List()
    println(msg)
    DSL.parseWithError(msg) match {
      case preo.lang.Parser.Success(result,_) =>
        try {
          val typ = DSL.checkVerbose(result)


          val reduc = Eval.instantiate(result)
          val reducType = DSL.typeOf(reduc)
          val coreConnector = Eval.reduce(reduc)

          val model = preo.frontend.mcrl2.Model(coreConnector)

          model.storeInFile
          model.generateLPS
          model.generateLTS

          JsonCreater.create(typ, reducType, coreConnector).toString
        }
        catch {
          // type error
          case e: TypeCheckException =>
            JsonCreater.create("Type error: " + e.getMessage).toString

          case e: GenerationException =>
            JsonCreater.create("Generation failed: " + e.getMessage).toString
          }
      case preo.lang.Parser.Failure(msg,_) =>
        JsonCreater.create("Parser failure: " + msg).toString
      //        instanceInfo.append("p").text("-")
      case preo.lang.Parser.Error(msg,_) =>
        JsonCreater.create("Parser error: " + msg).toString
      //        instanceInfo.append("p").text("-")
    }

  }
}
