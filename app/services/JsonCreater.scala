package services


import play.api.libs.json._
import preo.ast._
import preo.backend._
import preo.frontend.Show

/**@
  * Provides the methods to convert objects into json. Used in socket communication
  */
object JsonCreater {

  def create(typ: Type, reductyp:Type, con:CoreConnector, graph: Graph, aut: Automata, model: String): JsValue =
    JsObject(Map(
      "type" -> convert(typ),
      "reducType" -> convert(reductyp),
      "connector" -> convert(con),
      "graph" -> convert(graph),
      "automata" -> convert(aut),
      "model" -> JsString(model)
    ))



  def create(error: String): JsValue = {
    JsObject(Map(
      "error" -> JsString(error))
    )
  }

  private def convert(connector: CoreConnector): JsValue = connector match{
    case CSeq(c1, c2) => {
      JsObject(Map(
        "type" -> JsString("seq"),
        "c1" -> convert(c1),
        "c2" -> convert(c2))
      )
    }
    case CPar(c1, c2) => {
      JsObject(Map(
        "type" -> JsString("par"),
        "c1" -> convert(c1),
        "c2" -> convert(c2)
      ))
    }
    case CId(i) => {
      JsObject(Map(
        "type" -> JsString("id"),
        "i" -> convert(i)
      ))
    }

    case CSymmetry(i,j) => {
      JsObject(Map(
        "type" -> JsString("symmetry"),
        "i" -> convert(i),
        "j" -> convert(j)
      ))
    }
    case CTrace(i,c) => {
      JsObject(Map(
        "type" -> JsString("trace"),
        "i" -> convert(i),
        "c" -> convert(c)
      ))
    }
    case CPrim(name,i,j,extra) => {
      JsObject(Map(
        "type" -> JsString("prim"),
        "name" -> JsString(name),
        "i" -> convert(i),
        "j" -> convert(j)
      ))
    }

    case CSubConnector(name, c, _) => {
      JsObject(Map(
        "type" -> JsString("sub"),
        "name" -> JsString(name),
        "c" -> convert(c)
      ))
    }
  }

  private def convert(i: CoreInterface): JsValue = convert(i.toInterface)

  private def convert(i: Interface): JsValue = JsString(Show(i))

  private def convert(t : Type): JsValue = JsString(t.toString)

  private def convert(graph: Graph): JsValue = JsObject(Map(
    "edges" -> convertEdges(graph.edges),
    "nodes" -> convertNodes(graph.nodes)
  ))

  private def convertEdges(edges: List[ReoChannel]): JsValue =
    JsArray(edges.map(x => convert(x)))

  private def convert(edge: ReoChannel): JsValue = {
    val start = arrowToString(edge.srcType)
    val end = arrowToString(edge.trgType)
    JsObject(
      Map(
        "source" -> JsNumber(edge.src),
        "target" -> JsNumber(edge.trg),
        "start" -> JsString(s"start${start}circuit"),
        "end" -> JsString(s"end${end}circuit"),
        "type" -> JsString(edge.name)
      )
    )
  }

  private def arrowToString(endType: EndType): String = endType match{
    case ArrowIn  => "arrowin"
    case ArrowOut => "arrowout"
    case NoArrow  => ""
  }


  private def convertNodes(nodes: List[ReoNode]): JsValue =
    JsArray(nodes.map(x => convert(x)))

  private def convert(node: ReoNode): JsValue = {
    val nodeGroup = typeToGroup(node.nodeType, node.style);
    JsObject(
      Map(
        "id"-> JsNumber(node.id),
        "group" -> JsString(nodeGroup)
      )
    )
  }

  private def typeToGroup(nodeType: NodeType, style: Option[String]):String = (nodeType, style) match{
    case (Source, Some(s)) => if(s.contains("component")) "0" else "1"
    case (Source, None) => "1"
    case (Sink, None) => "3"
    case (Sink, Some(s)) => if(s.contains("component")) "4" else "3"
    case (Mixed, _) => "2"
  }


  /**@
    * Converting automata. Very incomplete. I just used the function in AutomataToJS file
    * @param aut automata to convert
    * @tparam A portautomata suposibly
    * @return Json for the automata
    */
  private def convert[A<:Automata](aut: A): JsValue = JsObject(Map(
    "nodesautomata" -> getNodes(aut),
    "linksautomata" -> getLinks(aut)
  ))

  private def getNodes[A<:Automata](aut: A): JsValue =
    JsArray(aut.getTrans.flatMap(processNode(aut.getInit, _)).toSeq)

  private def getLinks[A<:Automata](aut: A): JsValue =
    JsArray(aut.getTrans.flatMap(processEdge).toSeq)

  private def processNode(initAut:Int,trans:(Int,Any,String,Int)): Set[JsValue] = trans match{
    case (from,lbl,id,to) =>
      val (gfrom,gto,gp1,gp2) = nodeGroups(initAut,from,to)
      Set(JsObject(Map("id"-> JsNumber(from), "group"-> JsString(gfrom))),
        JsObject(Map("id"-> JsNumber(to), "group" -> JsString(gto))),
        JsObject(Map("id"-> JsNumber(from-1-to-id.toInt), "group"-> JsString(gp1))),
        JsObject(Map("id"-> JsNumber(to-2-from-id.toInt), "group" -> JsString(gp2))))
  }

  private def nodeGroups(initAut:Int,from:Int,to:Int):(String,String,String,String) =
    (   if(from==initAut) "0" else "1"
      , if(to==initAut) "0" else "1"
      , "2" , "2"
    )

  private def processEdge(trans:(Int,Any,String,Int)): Set[JsValue] = trans match {
    case (from, lbl,id, to) => {
      Set(JsObject(
        Map("source" -> JsNumber(from), "target"-> JsNumber(from-1-to-id.toInt), "type"->JsString(""), "start"->JsString("start"),
          "end"-> JsString("end"))),
        JsObject(Map(
          "source"-> JsNumber(from-1-to-id.toInt), "target"-> JsNumber(to-2-from-id.toInt), "type"->JsString(lbl.toString),
          "start"->JsString("start"), "end"-> JsString("end"))),
        JsObject(Map(
          "source"-> JsNumber(to-2-from-id.toInt), "target"-> JsNumber(to), "type"->JsString(""), "start"->JsString("start"),
          "end"-> JsString("endarrowoutautomata")))
      )
    }
  }

  private def convert(mcrl2Model: String): JsValue = JsObject(Map("mcrl2" -> JsString(mcrl2Model)))

}