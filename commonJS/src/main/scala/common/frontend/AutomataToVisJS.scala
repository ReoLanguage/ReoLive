package common.frontend

import dsl.analysis.semantics.{SBAutomata, SBState, SBTrans}
import hub.{CCons, HubAutomata}
import ifta.analyse.Simplify
import ifta.backend.{IftaAutomata, Show}
import ifta.{CTrue, ClockCons}
import preo.backend.Network.Mirrors
import preo.backend._

import scala.collection.mutable
import scala.util.Try


//todo: add rectangle colision colision
object AutomataToVisJS {

  //todo: Maybe create a special abstract automata that has time so that ifta and hub can inherit from it
  def apply[A<:Automata](aut: A, mirrors: Mirrors, boxName:String, portNames:Boolean=false): String = {
    generateClearJS() ++
    generateUpdJS(getNodes(aut),getLinks(aut,boxName,portNames,mirrors))
    //generateCanvasJS(getNodes(aut).mkString(","),getLinks(aut,boxName,portNames,mirrors).mkString(","))
  }

  /*todo: refactor in different methods or classes to avoid booolean virtuoso, or pass automata for
   * better customization for each type of automata, reo, ifta, hub,sb*/
//  private def generateJS(nodes: String, edges: String, name:String): String = {
   def generateCanvasJS(nodes:String, edges:String): String = {
    // println(nodes)
    // println(edges)
    s"""
    // need to load vis-network.min.js, and create div "visnetwork"

    // create an array with nodes
    var nodes = new vis.DataSet([
        $nodes
        //{id: 1},
        //{id: 2}
    ]);

    // create an array with edges
    var edges = new vis.DataSet([
        $edges
        //{from: 1, to: 2, label: 'fifo\\u{2193}.lossy\\u{2193}.node(dupl)\\u{2195}', font: {align: 'middle'}, arrows: {to: {enabled:true}}},
        //{from: 1, to: 2, label: 'fifo\\u{2193}.lossy\\u{2195}.node(dupl)\\u{2195}', font: {align: 'middle'}, arrows: {to: {enabled:true}}},
        //{from: 2, to: 1, label: 'fifo\\u{2191}', font: {align: 'middle'}, arrows: {to: {enabled:true}}}
    ]);

    // create a network
    var container = document.getElementById('visnetwork');
    var data = {
        nodes: nodes,
        edges: edges
    };

    var options = {
      interaction:{hover:true},
      edges: {arrows: 'to', font: {align: 'middle'}},
      manipulation: {
        enabled: false
      },
      physics: {
                    forceAtlas2Based: {
                        gravitationalConstant: -26,
                        centralGravity: 0.005,
                        springLength: 230,
                        springConstant: 0.18
                    },
                    maxVelocity: 146,
                    solver: 'forceAtlas2Based',
                    timestep: 0.35,
                    stabilization: {
                        enabled:true,
                        iterations:2000,
                        updateInterval:25
                    }
                }
    };
    var network = new vis.Network(container, data, options);
    // entering edge
    network.on("hoverEdge", function (params) {
        edges.update([{id:params.edge, font: {align: 'middle', size:24, color:'black'}}]);
        // highlight the ports on the circuit
        var ports = edges.get(params.edge).note.split("~");
        //ports.shift();
        ports.forEach(function(el) {
          var p = document.getElementById("gr_"+el);
          //console.log("port "+el);
          if (p!=null && p.style.fill!="#00aaff") {
            p.style.backgroundColor = p.style.fill;
            p.style.fill = "#00aaff";
            p.style.fontWeight = "bold";
          }
        });
    });
    network.on("blurEdge", function (params) {
        edges.update([{id:params.edge, font: {align: 'middle', size:14, color:'black'}}]);
        // de-highlight the nodes on the circuit
        var ports = edges.get(params.edge).note.split("~");
        //ports.shift();
        ports.forEach(function(el) {
          var p = document.getElementById("gr_"+el);
          //console.log("port "+el);
          if (p!=null) {
            if (p.style.backgroundColor == "")
              {p.style.fill = "black";}
            else
              {p.style.fill = p.style.backgroundColor;}
            p.style.fontWeight = "normal";
           }
        });
    });
      """
  }

  def generateClearJS(): String =
    """nodes.clear(); edges.clear();"""

  def generateUpdJS(nodes: Set[String], edges: Set[String]): String = {
    val nodesStr = nodes.map(x=>s"nodes.add($x);")
    val edgesStr = edges.map(x=>s"edges.add($x);")
    (nodesStr++edgesStr).mkString("")++"network.stabilize();"
  }


  private def getNodes[A<:Automata](aut: A): Set[String] =
    aut.getTrans().flatMap(processNode(aut.getInit, _))

  private def getLinks[A<:Automata](aut: A,name:String,portNames:Boolean=false, ms: Mirrors): Set[String] = {
    val loops = mutable.Map[Int,Int]().withDefaultValue(0)
    aut.getTrans(portNames).map(t => processEdge(expandMirrors(t,ms),loops,name))
  }

  private def processNode(initAut:Int,trans:(Int,Any,String,Int),nodeInvariant:Map[Int,ClockCons]=Map()): Set[String] = trans match {
    case (from, _, id, to) => Set(
      s"""{id: '$from'${if (from == initAut) ", color: {background: 'white'}" else ""}}""",
      s"""{id: '$to'${if (to == initAut) ", color: {background: 'white'}" else ""}}"""
    )
  }


  private def expandMirrors(trans: (Int, Any, String, Int), mirrors: Network.Mirrors): (Int,String,String,Int) =
  trans match {
    case (from,lbl,id,to) => (from, expandMirrors(lbl.toString,mirrors),id,to)
  }
  private def expandMirrors(string: String,mirrors: Mirrors): String = {
    val res = string.split("§").toList match {
      case Nil => string
      case head :: tl =>
        val sync = head //.split("~").mkString(" / ") // TODO: uncomment to use Animation from PortAutomata
        val ints = collectInts(tl,mirrors)
//        val expanded = ints.flatMap(mirrors(_))
        (sync::ints.map(_.toString).toList).mkString("~")
    }
    //println(s"- expanding $string --> $res")
    res
  }
  private def collectInts(strings: List[String],mirrors: Mirrors): Set[Int] = {
    strings.toSet.flatMap((s:String) => Try(s.toInt).toOption match {
      case Some(i:Int) => mirrors(i) + i
      case _ => Set[Int]()
    })
  }


  private def processEdge(trans:(Int,String,String,Int), loops:mutable.Map[Int,Int], name:String): String = trans match {
    case (from, l,id, to) =>
      if (from == to)
        //println(s"loop found #${loops(to)} (around $to)")
        loops(to) = loops(to)+1
      val rotation = if (from==to) s", selfReference: {angle: Math.PI * 100*${loops(to)}/180 }" // 100ª
                     else ""
      val splitted = l.split("~",2)
      val note = splitted.applyOrElse(1,(x:Int)=>"")
      val lbl = splitted.headOption.getOrElse("")
        .replaceAll("↑","\\\\u{2191}")
        .replaceAll("↓","\\\\u{2193}")
        .replaceAll("↕","\\\\u{2195}")
      s"""{from: '$from', to: '$to', note:'$note', label: '$lbl'$rotation}"""
  }

}
