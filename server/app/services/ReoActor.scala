package services


import akka.actor._
import preo.DSL
import preo.common.{GenerationException, TypeCheckException}
import preo.frontend.{Eval, Show}
import MCRL2Bind._


object ReoActor {
  def props(out: ActorRef) = Props(new ReoActor(out))
}

class ReoActor(out: ActorRef) extends Actor {
  /**
    * Reacts to messages containing a connector,
    * wraps each into a JSON (via process),
    * and forwards the result to the "out" actor.
    */
  def receive: PartialFunction[Any, Unit] = {
    case msg: String =>
      out ! process(msg)
  }

  /**
    * Gets a message, does the server processing, and returns the result as a string (using JSON here)
    * @param msgCleaned incomming message with the connector
    * @return type of the connector and an instance (type and core connector) using JSON
    */
  private def process(msgCleaned: String): String = {
    var warnings: List[String] = List()
    val msg = msgCleaned
      .replace("\\\\n","\\±")
      .replace("\\n","\n")
      .replace("\\±","\\\\n")
      .replace("\\\\","\\")
    try {
      DSL.parseWithError(msg) match {
        case Right(result) =>
          val typ = DSL.checkVerbose(result)
          var warn = ""


          val reduc = Eval.instantiate(result)
          val reducType = DSL.typeOf(reduc)
          val coreConnector = DSL.unfoldTreo(Eval.reduce(reduc),"dupl")

          try {
            val model = preo.frontend.mcrl2.Model(coreConnector)
            //println(model)
            println("MAs1:\n - "+model.getMultiActionsMap.keys.mkString("\n - "))
//            println("Multiactions in the model:\n"+model.getMultiActionsMap
//              .map(kv => "'" + kv._1 + "'" + ":" + kv._2.map("\n - " + _.mkString(", ")).mkString(""))
//              .mkString("\n"))
            storeInFile(model)
            //generateLPS (called by generateLTS)
            //generateLTS
          } catch {
            case e: GenerationException =>
              warn += s"Generation of mCRL2 failed: ${e.getMessage}"
          }

          //println(s"[ReoActor] returning ${Show(coreConnector)}")
          JsonCreater.create(typ, reducType, coreConnector, warn).toString
        //          val id=Thread.currentThread().getId
        //          val msg = common.messages.Message.ConnectorMsg(typ,reducType,coreConnector,id)
        //          msg.asJson.toString()

        //      case f@preo.lang.Parser.Failure(_,_) =>
        //        JsonCreater.createError("Parser failure: " + f.toString()).toString
        //              instanceInfo.append("p").text("-")
        //      case preo.lang.Parser.Error(msg,_) =>
        case Left(errorMsg) =>
          JsonCreater.createError("Parser error: " + errorMsg).toString
        //        instanceInfo.append("p").text("-")
      }
    }
    catch {
      // type error
      case e: TypeCheckException =>
        JsonCreater.createError("Type error: " + e.getMessage).toString

      case e: GenerationException =>
        JsonCreater.createError("Generation failed: " + e.getMessage).toString

      case e: java.io.IOException => // by generateLPS/LTS/storeInFile
        JsonCreater.createError("IO exception: " + e.getMessage).toString

      case e: Throwable => // by generateLPS/LTS/storeInFile
        JsonCreater.createError("Preo exception: " + e.getStackTrace.mkString("\n  ")).toString
    }
  }
}
