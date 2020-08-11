package common.widgets.arx

import common.frontend.GraphsToJS
import common.widgets.{Box, GraphBox, OutputArea}
import dsl.DSL._
import dsl.analysis.syntax.Program
import dsl.backend.Net
import dsl.backend.Net.{Connector, IPort}
import preo.ast.{CPrim, CoreInterface}
import preo.backend.Network.Mirrors
import preo.backend.{Circuit, Network}

class DslGraphBox(programBox: Box[Program], errorBox: OutputArea, path: String=".")
extends GraphBox(null, errorBox, path, "Circuit of the program"){

  override def init(div: Block, visible: Boolean): Unit = {

    super.init(div,visible)
  }
  override def drawGraph(): Unit = try{
    clear()
    val program = programBox.get
    //println(s"[prog] - Drawing graph - $program")
    val (typedProgram,_) = typeCheck(program)
    val (net1,maxPort1) = Net(typedProgram)
//    println(s"[net] - Drawing graph (max=$maxPort1):\n$net1")
    val (net2,maxPort2) = addNodes(net1,Some(maxPort1+1))
    //println(s"[net] - Added Nodes (max=$maxPort2):\n$net2")
    val preo1: Network = mkPreoNet(net2)
    //println(s"[preo] - 'Got Network' - $preo1")
    val ms = new Mirrors
    val preo2 = Network.simplifyGraph(preo1,ms)
    //println(s"[preo] - 'simplified' - $preo2")
    val preo3 = Network.addRedundancy(preo2,ms,Some(maxPort2+1))
    //println(s"EE - Simplified graph - $preo1")
    //println(s"[preo] - with redundancy - $preo3")

    graph = Circuit(preo3,ms)//Circuit(dependency.get,true)
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

  protected def clear(): Unit = {
    box.selectAll("g").html("")
  }

  /** For each connector c:ins->outs, replace boundary interfaces with nodes  */
  private def addNodes(net:Net,newseed:Option[IPort]=None): (Net,IPort) = {
    val seed = newseed match {
      case Some(value) => value
      case None =>
        val io = net.prims.flatMap(p => p.ins++p.out)
        val allPorts = io ++ net.ins ++ net.outs
        allPorts.max + 1
    }
    addNodesAux(net,seed)
  }

  private def addNodesAux(net:Net,seed:IPort): (Net,IPort) = {
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
    (Net(conns++nodeConns,net.ins,net.outs) , sd)
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
      (if (p.name == "node" && p.ins.size>1) Set("mrg")  else Set()) ++
      (if (p.name == "node" && p.out.size>1) Set("dupl") else Set()) ++
      (if (p.name == "BUILD" || p.name == "MATCH") Set("box") else
       if (p.ins.isEmpty && p.out.size==1)    Set("box")  else Set())
    Network.Prim(CPrim(p.name,CoreInterface(p.ins.size),CoreInterface(p.out.size),extra)
                , p.ins.toList, p.out.toList, Nil) // no parents yet.
  }
}
