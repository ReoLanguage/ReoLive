package services

import akka.actor.{Actor, ActorRef, Props}
import ifta.backend.Show
import ifta.common.ParseException
import ifta.{DSL, FExp, Feat}
import play.api.libs.json.{JsDefined, JsString, JsValue, Json}

import java.io.{File, FileInputStream, PrintWriter}
import java.util.Properties
import scala.sys.process.ProcessLogger
import sys.process._


/**
  * Created by guille on 16/01/2019
  */

object Mcrl2Actor {
  def props(out:ActorRef) = Props(new Mcrl2Actor(out))
}


class Mcrl2Actor(out:ActorRef) extends Actor {

  private val props: Properties = new Properties
  props.load(new FileInputStream("global.properties"))
  private val mcrl2Path = props.getProperty("mcrl2Path") + "/"
  private val timeout = props.getProperty("timeoutCmd") + " 30" // 30s timeout


  override def receive = {
    case msg: String =>
      out ! process(msg)
  }

  private def process(msg:String):String = {
    val (spec,props) = parseMsg(msg)
    //s"Processing result for $spec + [${props.mkString(" / ")}]"

    ///

    try {
      val file:File = File.createTempFile("spec-", ".mcrl2")
      val fPath = file.getAbsolutePath
//      val fName = file.getName
//      file.setExecutable(true)
      val pw = new PrintWriter(file)
      pw.write(spec)
      pw.close
//      println(s"Wrote $fPath")
      var res: List[(String,String)] = Nil // (true/false, mermaidEvidence)
      ///
      for (prop <- props) {
        val fileP:File = File.createTempFile("prop-", ".mcf")
        val fPathP = fileP.getAbsolutePath
//        val fNameP = fileP.getName
        fileP.setExecutable(true)
        val pw = new PrintWriter(fileP)
        pw.write(prop)
        pw.close
        //println(s"Wrote $fPathP")

        s"$timeout ${mcrl2Path}mcrl22lps $fPath ${fPath}.lps".!
//        println("wrote LPS")
        s"$timeout ${mcrl2Path}lps2pbes $fPath.lps -c --formula=$fPathP $fPath.pbes".!
//        val status = s"${mcrl2Path}lps2pbes $fPath.lps --formula=$fPathP $fPath.pbes".!(ProcessLogger(stdout append _, stderr append _))
//        if (status == 0) println(s"Generated PBES: ${(status, stdout.toString)}")
//        else println(s"Generated PBES with error: ${(status, stderr.toString)}")

        val solved = s"$timeout ${mcrl2Path}pbessolve -q --file=${fPath}.lps ${fPath}.pbes".!!.dropRight(1)

//        for (typ <- List("aut","fsm","dot")) {
//          res ::= s" --- $typ ---"
//          s"${mcrl2Path}lps2lts $fPath.pbes.evidence.lps $fPath.evidence.$typ".!
//          val ev = s"cat $fPath.evidence.$typ".!!
//          res ::= ev
//          if (typ == "aut") {
//            res ::= " --- Mermaid ---"
//            res ::= aut2mermaid(ev)
//          }
//        }

        s"$timeout ${mcrl2Path}lps2lts $fPath.pbes.evidence.lps $fPath.evidence.aut".!
        val mermaidEvidence = aut2mermaid(s"cat $fPath.evidence.aut".!!)

        res ::= solved -> mermaidEvidence // add another pair
      }
      // serialise
      res.reverse.map(xy=>s"${xy._1}§§§${xy._2}").mkString("§§§§")
    }
    catch {
      case e:Throwable => s"Some error: $e"
    }
  }

  private def aut2mermaid(aut:String): String = {
    var res = "flowchart\n" // stateDiagram-v2
    val re = "[(]([0-9]+),\"([^\n]*)\",([0-9]+)[)]".r
    for (mt <- re.findAllMatchIn(aut)) {
//      res += s"  ${mt.group(1)} --> ${mt.group(3)}: ${mt.group(2)}\n"
      res += s"""  ${mt.group(1)}(( )) --"${mt.group(2)}"--> ${mt.group(3)}(( ))\n"""
//      res += s"""  ${mt.group(1)}(( )) --AAA--> ${mt.group(3)}(( ))\n"""
    }
    res += "  0(( ))\n  style 0 fill:#5f5,stroke:#333,stroke-width:4px"
    res
  }

  private def parseMsg(msg:String):(String,List[String]) = {
    try{
      val r1 = msg.split("§§§§")
      val r2 = r1(1).split("§§§")
      (r1(0).replaceAll("\\\\n","\n"),r2.toList.map(_.replaceAll("\\\\n","\n")))
    }
    catch {
      case e: Throwable => (s"Failed to parse message: $msg",Nil)
    }
//    val res:JsValue = Json.parse(msg)
//    val fm:String = (res \ "fm").get.asInstanceOf[JsString].value
//    val feats:String = (res \ "feats").get.asInstanceOf[JsString].value
//    (fm,feats)
  }

}
