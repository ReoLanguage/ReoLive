package services

import akka.actor.{Actor, ActorRef, Props}
import hub.{DSL, HubAutomata}
import hub.analyse.TemporalFormula
import hub.backend.{Show, Simplify, Uppaal}
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

  private def modelCheck(conn: CoreConnector, formulas:List[TemporalFormula]): String = {
    try {
      // get hub
      val hub = Automata[HubAutomata](conn).serialize.simplify
      // converted to Uppaal Ta
      val ta = Uppaal.mkTimeAutomata(hub)
      // get uppaal model
      val uppaal = Uppaal(ta)
      // get a map from port number to shown name (from hub)
      val interfaces:Map[Int,String] = (hub.getInputs ++ hub.getOutputs).map(p=>p->hub.getPortName(p)).toMap
      // get a map from port name to the locations after the port executed (based on uppaal ta)
      val act2locs = ta.act2locs.map(a => interfaces(a._1)-> a._2)
      // expand formulas (remove syntactic sugar
      val expandedFormulas = formulas.map(f=>Uppaal.expandTemporalFormula(f,act2locs,interfaces.map(i => i._2->i._1)))
      // simplify formulas and convert them to string suitable fro uppaal
      val formulasStr = expandedFormulas.map(f => Show(Simplify(f))).mkString("\n")

      // store model and formulas
      val id = Thread.currentThread().getId
      val modelPath = s"/tmp/uppaal_$id.xml"
      val queryPath = s"/tmp/uppaal_$id.q"
      UppaalBind.storeInFile(uppaal,modelPath) // store model in /tmp/uppaal_$id.xml
      UppaalBind.storeInFile(formulasStr,queryPath) // store model in /tmp/uppaal_$id.xml

      // call to verifity
      val verifytaOut = UppaalBind.verifyta(modelPath,queryPath,"-q")
      if (verifytaOut._1 == 0) "ok:"+ verifytaOut._2
      else "error:Verifyta fail: " + verifytaOut._2
    }catch {
      case e: java.io.IOException => "error:IO exception: " + e.getMessage
    }
  }

}
