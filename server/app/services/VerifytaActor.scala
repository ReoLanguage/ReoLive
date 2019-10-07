package services

import akka.actor.{Actor, ActorRef, Props}
import hub.backend.Uppaal
import play.api.libs.json._
import preo.lang.ParserUtils

/**
  * Created by guillecledou on 2019-10-05
  */


object VerifytaActor {
  def props(out: ActorRef) = Props(new VerifytaActor(out))
}

class VerifytaActor(out:ActorRef) extends Actor {

  /* Reacts to messages containing a JSON with a connector (string) and a temporal formula (string),
  * produces a uppaal model and a proper expanded formula from the connector,
  * calls verifyta to verify the formula,
  * wraps each into a new JSON (via process),
  * and forwards the result to the "out" actor to generate an info (or error) box.
  */
  def receive = {
    case msg: String =>
      out ! process(msg)
  }

  /**
    * Get a request to model check a raw connector hub (string) and a raw formula (string)
    * @param msg
    * @return
    */
  private def process(msg: String): String = {
    val jsmsg = Json.parse(msg)
    val rawConnector  = parseMsg(jsmsg,"connector")
    val rawFormula    = parseMsg(jsmsg,"query")
    val operation     = parseMsg(jsmsg,"operation")
    (rawConnector,rawFormula,operation) match {
      case (Some(c),Some(f),Some("check")) => modelCheck(c, f)
      case (None,_,_) => error("Parser error: no connector found")
      case (_,None,_) => error("Parser error: no temporal logic found")
      case _ => error("Parser error: no operation found " + operation)
    }
  }

  /**
    * Parses a raw message, and returns a particular tag, if it exists
    * @param msg
    * @return information associated to tag
    */
  private def parseMsg(jsMsg:JsValue,tag:String):Option[String] = {
    val rawLookFor = jsMsg \ tag
    rawLookFor match{
      case JsDefined(x) => Some(x.asInstanceOf[JsString].value)
      case undefined => None
    }
  }

  /**
    * Get a request to model check a raw connector (string) and a raw formula (string)
    * @param raw_conn
    * @param raw_form
    * @param warning
    * @return result from verifyta
    */
  private def modelCheck(raw_conn: String, raw_form: String): String = {
//    ParserUtils.parseAndHide(raw_conn, raw_form) match {
//      case Right((model, mcrl2form)) => modelCheck(model, mcrl2form, warning)
//      case Left(err) => error(err)
//    }
  ""
  }

  private def modelCheck(uppaal: Uppaal, formula:String): String = {
//    try {
//      val id = Thread.currentThread().getId
//      storeInFile(model) // create model_id.mcrl2
//      minimiseLTS()      // create model_id.lts
//
//      val file = new File(s"/tmp/modal_$id.mu")
//      file.setExecutable(true)
//      val pw = new PrintWriter(file)
//      pw.write(form)
//      pw.close()
//
//      val save_output = savepbes()
//      if(save_output._1 == 0)
//        JsonCreater.create(solvepbes(),warning).toString
//      else
//        error("Modal Logic failed: " + save_output._2+
//          "\n when parsing\n"+form)
//    }
//    catch {
//      case e: java.io.IOException => // by solvepbes/savepbes/storeInFile/generateLTS
//        error("IO exception: " + e.getMessage)
//    }
    ""
  }

  /**
    * Creates an error to return to the caller
    * @param e error msg
    * @return json string with error tag
    */
  private def error(e:String):String = JsObject(Map("error" -> JsString(e))).toString()
}
