package json

//import play.api.libs.json.{JsDefined, JsString, JsValue, Json}
import preo.ast._
import scala.util.parsing.combinator.RegexParsers

//import scala.util.parsing.json._

object Loader{

  type PreoReply =  Either[PreoReplyOK, PreoReplyKO]
  type PreoReplyOK =  (String, String, CoreConnector, Int,Option[String])
  type PreoReplyKO =  String

  private val rawParser = new Parser

  private class Parser extends RegexParsers {

    def parse(c:String): Map[String,Any] = parseAll(rawJSON, c) match {
      case Success(result, next) => result
      case _: NoSuccess => Map()
    }

    def rawJSON: Parser[Map[String, Any]] =
      "{"~>repsep(elemJSON,",")<~"}" ^^ ( ls => ls.toMap)

    def elemJSON: Parser[(String,Any)] =
      string~":"~valueJSON ^^ {
        case s~_~v => (s,v)
      }

    // only types needed for this case
    def valueJSON: Parser[Any] =
      rawJSON |
      string |
      int


    //  TODO: improve strings - allow \"
    val string: Parser[String] = """"[^"]*"""".r ^^ (s => s.drop(1).dropRight(1))
    val int: Parser[Int] = """[0-9]+""".r ^^ (s => s.toInt)
  }

    def apply(rawjs: String):  Either[(String, String, CoreConnector, Int,Option[String]), String] = {

      val parsed = rawParser.parse(rawjs)
        //JSON.parseFull(rawjs).get.asInstanceOf[Map[String, Any]]

      if (parsed.contains("error")) {
        Right(parsed("error").asInstanceOf[String])
      }
      else {
        val typ = if (parsed.contains("type")) parsed("type").asInstanceOf[String]
        else throw new RuntimeException(s"Type not found in ${parsed}.")
        val reducTyp = parsed("reducType").asInstanceOf[String]
        val con = convertCon(parsed("connector").asInstanceOf[Map[String, Any]])
        val id = parsed("id").asInstanceOf[Int]
        val warn = parsed.get("warning").asInstanceOf[Option[String]]
        Left((typ, reducTyp, con, id, warn))
      }
    }

  private def convertCon(raw:Map[String,Any]): CoreConnector = {

    raw("type").asInstanceOf[String] match {
      case "seq" => CSeq(convertCon(raw("c1").asInstanceOf[Map[String, Any]]), convertCon(raw("c2").asInstanceOf[Map[String, Any]]))
      case "par" => CPar(convertCon(raw("c1").asInstanceOf[Map[String, Any]]), convertCon(raw("c2").asInstanceOf[Map[String, Any]]))
      case "id" => CId(convertInterface(raw("i").asInstanceOf[String]))
      case "symmetry" => CSymmetry(convertInterface(raw("i").asInstanceOf[String]), convertInterface(raw("j").asInstanceOf[String]))
      case "trace" => CTrace(convertInterface(raw("i").asInstanceOf[String]), convertCon(raw("c").asInstanceOf[Map[String, Any]]))
      case "prim" => CPrim(raw("name").asInstanceOf[String], convertInterface(raw("i").asInstanceOf[String]),
          convertInterface(raw("j").asInstanceOf[String]), convertSet(raw("extra").asInstanceOf[String]))
      case "sub" => CSubConnector(raw("name").asInstanceOf[String], convertCon(raw("c").asInstanceOf[Map[String, Any]]), convertAnns(raw("ann").asInstanceOf[Map[String,Any]]))
      case _ => null
    }

  }

  private def convertInterface(i: String): CoreInterface = CoreInterface(i.toInt)
  private def convertSet(s:String): Set[Any] =
    if (s.isEmpty) Set() else s.split(",").toSet
  private def convertAnns(map: Map[String, Any]): List[Annotation] = map("type") match {
    case "Nil" => Nil
    case _ => convertAnn(map("head").asInstanceOf[Map[String,Any]]) :: convertAnns(map("tail").asInstanceOf[Map[String,Any]])
  }
  private def convertAnn(map: Map[String, Any]): Annotation = Annotation(map("name").asInstanceOf[String],None) // IGNORING VALUES

  def loadModalOutput(msg: String): Either[String, String] = {
//    val js = Json.parse(msg)
//    js \ "error" match {
//      case JsDefined(err) => Right(err.as[String])
//      case _              => Left(js("output").as[String])
//    }
    val parsed = rawParser.parse(msg)
                 // JSON.parseFull(msg).get.asInstanceOf[Map[String, Any]]

    if (parsed.contains("error")) {
      Right(parsed("error").asInstanceOf[String])
    }
    else{
      Left(parsed("output").asInstanceOf[String])
    }
  }
}

