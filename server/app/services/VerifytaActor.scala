package services

import akka.actor.{Actor, ActorRef, Props}
import hub.{DSL, HubAutomata}
import hub.analyse.{TemporalFormula, UppaalFormula}
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
      case (Right(c),Right(f)) => newModelCheck(c,f)
      case (Left(msg),_) => s"error:$msg"
      case (_,Left(msg)) => s"error:$msg"
    }
  }

  private def modelCheck(conn: CoreConnector, formulas:List[TemporalFormula]): String = {
    try {
      // get hub
      val hub = Automata[HubAutomata](conn).serialize.simplify
      // converted to a network of Uppaal Ta
      val tas = Uppaal.fromFormula(formulas.head,hub)
      // get uppaal model
      val uppaal = Uppaal(tas)
      // get a map from port number to shown name (from hub)
      val interfaces:Map[Int,String] = (hub.getInputs ++ hub.getOutputs).map(p=>p->hub.getPortName(p)).toMap
      // get a map from port names to the locations after the port executed (based on uppaal ta (only the main hub will have maps)
      val act2locs = tas.flatMap(ta=>ta.act2locs.map(a => interfaces(a._1)-> a._2)).toMap
      // expand formulas (remove syntactic sugar)
      //val expandedFormulas = formulas.map(f=>Uppaal.expandTemporalFormula(f,act2locs,interfaces.map(i => i._2->i._1)))
      val expandedFormulas = formulas.map(f=>Uppaal.toUppaalFormula(f,act2locs,interfaces.map(i => i._2->i._1)))
      // simplify formulas and convert them to string suitable fro uppaal
      val formulasStr = expandedFormulas.map(f => Show(Simplify(f))).mkString("\n")
      println("Expanded Formulas:\n"+formulasStr)

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

  private def newModelCheck(conn: CoreConnector, formulas:List[TemporalFormula]): String = try {
    // get hub
    val hub = Automata[HubAutomata](conn).serialize.simplify
    // get a map from port number to shown name (from hub)
    val interfaces:Map[Int,String] = (hub.getInputs ++ hub.getOutputs).map(p=>p->hub.getPortName(p)).toMap

    // create an uppaal model for simple formulas
    val ta = Set(Uppaal.mkTimeAutomata(hub))

    // map each formula to a custom network of TA to verify such formula (simple formulas are maped to the same based TA
    val formulas2nta:List[(TemporalFormula,Set[Uppaal])] =
      formulas.map(f => if (f.hasUntil || f.hasBefore) (f,Uppaal.fromFormula(f,hub)) else  (f,ta))

    // map each formula to its expanded formula for uppaal
    val formula2nta2uf:List[(TemporalFormula,UppaalFormula,Set[Uppaal])] =
      formulas2nta.map(f=>(f._1,Uppaal.toUppaalFormula(
        f._1,
        f._2.flatMap(ta=>ta.act2locs.map(a=> interfaces(a._1)->a._2)).toMap, interfaces.map(i => i._2->i._1)),
        f._2))

    // simplify formulas and convert them to a string suitable for uppaal
    val formulasStr: List[(TemporalFormula,String,String)] =
      formula2nta2uf.map(f => (f._1,Show(Simplify(f._2)),Uppaal(f._3)))

    // group formulas by model
    val orderedFormulasStr:List[(String,List[(TemporalFormula,String,String)])] = formulasStr.groupBy(_._3).toList

    // accumulate responses from verifyta
    var responses:List[(List[TemporalFormula],String)] = List()
    // get current thread id
    val id = Thread.currentThread().getId
    // store models and formulas per type, call to verifyta and accumulate responses
    responses = for(((model,list),i) <-orderedFormulasStr.zipWithIndex) yield {
      // create paths
      var modelPath = s"/tmp/uppaal${i}_$id.xml"
      var queryPath = s"/tmp/uppaal${i}_$id.q"
      // store models
      UppaalBind.storeInFile(model,modelPath) // store model in /tmp/uppaal$i_$id.xml
      UppaalBind.storeInFile(list.map(l=> l._2).mkString("\n"),queryPath) // store model in /tmp/uppaal$i_$id.xml
      // call to verifyta
      var verifytaOut = UppaalBind.verifyta(modelPath,queryPath,"-q")
      //println(verifytaOut._2.trim)
      if (verifytaOut._1 == 0)
        (list.map(_._1) , "ok:"+verifytaOut._2.stripMargin)//"ok"+ verifytaOut._2)
      else
        (list.map(_._1), "error:Verifyta failed:" +verifytaOut._2.stripMargin)//"error:Verifyta fail: " + verifytaOut._2)
    }

//    responses.map(r=> )
//    responses.map(r=> println(r._1.map(Show(_)).mkString("\n")) +"\n"+ println(r._2.toString))
//    responses.map(r=> println(r._2.split("Verifying formula ([0-9]*) at \\/tmp\\/uppaal([0-9]*)_([0-9]*)\\.q:[0-9]*").mkString("\n")))
    // map formulas to their response
//    var tf2res:Map[TemporalFormula,String] =
//      (for ((list,rs) <- responses) yield {
////        println("Full res:"+rs)
//        var listRes:List[String] = rs//.replaceAll("\\[2K","")
//            .split("Verifying formula ([0-9]*) at \\/tmp\\/uppaal_([0-9]*)\\.q:[0-9]*")
////          .replaceAll("Verifying formula ([0-9]+) at \\/tmp\\/uppaal_([0-9]*)\\.q:[0-9]*","\n")
////          .replaceFirst("\n","")
////          .split("\n").toList
//            .filterNot(s=> s.isBlank).toList
////        println("Res 2 list:\n"+listRes.mkString("\n"))
//        val zip = list.zip(listRes)
////        println("Zip:\n"+ zip.map(z=> Show(z._1) +"---->"+z._2).mkString("\n"))
//        zip
//      }).flatten.toMap
//
//    // for each formula, return its response (in order)
//    val res = formulas.map(f=> tf2res(f)).mkString("\n")
//    // send responses
    val res = responses.map(r => r._1.map(Show(_)).mkString("\n") + "~" + r._2).mkString("§")
//    println(res)
    res
    //responses.map(r => "Formulas:\n" + r._1.mkString("\n") + "Response:\n" + r._2).mkString("\n")
  } catch {
    case e: java.io.IOException => "error:IO exception: " + e.getMessage
  }

}
