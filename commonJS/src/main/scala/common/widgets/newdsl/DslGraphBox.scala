package common.widgets.newdsl

import common.frontend.GraphsToJS
import common.widgets.{Box, GraphBox, OutputArea}
import dsl.analysis.syntax.Program
import dsl.backend.Net
import dsl.backend.Net.Connector
import preo.ast.{CPrim, CoreInterface}
import preo.backend.Network.Mirrors
import preo.backend.{Circuit, Network}

class DslGraphBox(programBox: Box[Program], errorBox: OutputArea, path: String=".")
extends GraphBox(null, errorBox, path, "Circuit of the Connector"){

  override def init(div: Block, visible: Boolean): Unit = {

    super.init(div,visible)
  }
  override def drawGraph(): Unit = try{
    val program = programBox.get
    println(s"BB - Drawing graph - $program")
    val dslNet = Net(program)
    println(s"CC - Drawing graph - $dslNet")
    val dslNet2 = addNodes(dslNet)
    println(s"DD - Added Nodes - $dslNet2")
    val preoNet: Network = mkPreoNet(dslNet2)
    println(s"DD - 'Got Network' - $preoNet")
    val ms = new Mirrors
    val net2 = Network.simplifyGraph(preoNet,ms)
    println(s"DD - 'simplified' - $net2")
    val net3 = Network.addRedundancy(net2,ms)
    //println(s"EE - Simplified graph - $preoNet")
    println(s"EE - with redundancy - $net3")

    graph = Circuit(net3,ms)//Circuit(dependency.get,true)
    val size = graph.nodes.size
    val factor = Math.sqrt(size * 10000 / (densityCirc * widthCircRatio * heightCircRatio))
    val width = (widthCircRatio * factor).toInt
    val height = (heightCircRatio * factor).toInt
    box.attr("viewBox", s"00 00 $width $height")
    //println("Drawing graph - source: "+dependency.get)
    //println("Drawing graph - produced: "+ graph)
    //    toJs(graph)
    scalajs.js.eval(GraphsToJS(graph))
  }
  catch Box.checkExceptions(errorBox)


  /** For each connector c:ins->outs, replace boundary interfaces with nodes  */
  private def addNodes(net:Net): Net = {
    val io = net.prims.flatMap(p => p.ins++p.out)
    val allPorts = io ++ net.ins ++ net.outs
    val seed = allPorts.max + 1
    addNodes(net,seed)
  }

  private def addNodes(net:Net,seed:Int): Net = {
    var conns = List[Connector]()
    var sd = seed
    val ins:  Map[Int,(Set[Int],Set[Int])] = net.ins.map( p => p -> (Set(p),Set[Int]())).toMap
    val outs: Map[Int,(Set[Int],Set[Int])] = net.outs.map(p => p -> (Set[Int](),Set(p))).toMap
    val inouts = (for (i <- ins.keySet.intersect(outs.keySet)) yield i -> (ins(i)._1,outs(i)._2)).toMap
    var ns: Map[Int,(Set[Int],Set[Int])] = ins ++ outs ++ inouts
//    var (conns,sd,ns): (List[Connector],Int,Map[Int,(List[Int],List[Int])]) = (Nil,seed,ns2)
    for (c<-net.prims) {
      val res = addNodes(c)(sd,ns)
      conns ::= res._1
      sd = res._2
      ns = res._3
    }
    val nodeConns = ns.map(kv => Connector("node",kv._2._1, kv._2._2))
    Net(conns++nodeConns,net.ins,net.outs)
  }

  private def addNodes(con:Connector)
                      (implicit seed:Int, nodes:Map[Int,(Set[Int],Set[Int])])
      : (Connector,Int,Map[Int,(Set[Int],Set[Int])]) = {
    var ns = nodes
    var sd = seed
    def upd(p:Int,in:Boolean): Int = {
      val intfs:(Set[Int],Set[Int]) = ns.getOrElse(p,(Set(),Set()))
      ns += p -> (if (in) (intfs._1 + sd,intfs._2)
                  else    (intfs._1,intfs._2 + sd))
      sd += 1
      sd-1
    }
    val ins = con.ins.map(upd(_,false))
    val outs = con.out.map(upd(_,true))
    (Connector(con.name,ins,outs),sd,ns)
  }


  private def mkPreoNet(net: Net): Network =
    Network(net.prims.map(mkPreoPrim),net.ins.toList, net.outs.toList)
  private def mkPreoPrim(p:Net.Connector): Network.Prim = {
    val extra:Set[Any] =
      (if (p.name == "node" && p.ins.size>1) Set("mrg") else Set()) ++
      (if (p.name == "node" && p.out.size>1) Set("dupl") else Set())
    Network.Prim(CPrim(p.name,CoreInterface(p.ins.size),CoreInterface(p.out.size),extra)
                , p.ins.toList, p.out.toList, Nil) // no parents yet.
  }
}
