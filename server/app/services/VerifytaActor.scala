package services

import akka.actor.{Actor, ActorRef, Props}
import hub.{DSL, HubAutomata}
import hub.analyse.{TemporalFormula, UppaalFormula}
import hub.backend._
import play.api.libs.json._
import preo.ast.CoreConnector
import preo.backend.Automata
import preo.lang.ParserUtils
import services.UppaalBind

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
      case (None,_,_) => "error:Parser error: no connector found"
      case (_,None,_) => "error:Parser error: no temporal logic found"
      case _ => "error:Parser error: no operation found " + operation
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
    val formula = DSL.parseFormula(raw_form)
    val conn = ParserUtils.parseCoreConnector(raw_conn)
    (conn,formula) match {
      case (Right(c),Right(f)) => modelCheck(c,f)
      case (Left(msg),_) => s"error:$msg"
      case (_,Left(msg)) => s"error:$msg"
    }
  }

  private def modelCheck(conn: CoreConnector, formulas:List[TemporalFormula]): String = try {
    // get hub
    val hub = Automata[HubAutomata](conn).serialize.simplify
    // make verifyta calls
    val calls:Map[TemporalFormula,VerifytaCall] = Verifyta.createVerifytaCalls(formulas,hub)
    // accumulate responses from verifyta
    var responses:List[(TemporalFormula,String)] = List()
    // get current thread id
    val id = Thread.currentThread().getId

    // store models and formulas per type, call to verifyta and accumulate responses
    responses = for(((tf,c),i) <-calls.toList.zipWithIndex) yield {
      // create paths
      var modelPath = s"/tmp/uppaal${i}_$id.xml"
      var queryPath = s"/tmp/uppaal${i}_$id.q"
      // store models
      UppaalBind.storeInFile(Uppaal(c.um),modelPath) // store model in /tmp/uppaal$i_$id.xml
      UppaalBind.storeInFile(c.uf.map(f=> Show(f)).mkString("\n"),queryPath) // store model in /tmp/uppaal$i_$id.xml
      // call to verifyta
      var verifytaOut = UppaalBind.verifyta(modelPath,queryPath,"-q")
      //println(verifytaOut._2.trim)
      if (verifytaOut._1 == 0)
        (tf , "ok:"+verifytaOut._2.stripMargin)//"ok"+ verifytaOut._2)
      else
        (tf, "error:Verifyta failed:" +verifytaOut._2.stripMargin)//"error:Verifyta fail: " + verifytaOut._2)
    }

    // send responses
    val res = responses.map(r => Show(r._1) + "~" + r._2).mkString("§")
    // println(res)
    res
  } catch {
    case e: java.io.IOException => "error:IO exception: " + e.getMessage
    case e=> e.getMessage
  }

}
