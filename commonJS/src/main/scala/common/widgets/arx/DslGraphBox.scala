package common.widgets.arx

import common.frontend.GraphsToJS
import common.widgets.GraphBox.saveSvg
import common.widgets.{Box, GraphBox, OutputArea}
import dsl.DSL
import dsl.analysis.semantics.SBContext
import dsl.analysis.types.Context
import dsl.backend.ArxNet.Edge
import dsl.backend.{ArxNet, Show}
import preo.ast.{CPrim, CoreInterface}
import preo.backend.Network.{Mirrors, Port}
import preo.backend.{Circuit, Network}

import scala.annotation.tailrec
import scala.collection.mutable

class DslGraphBox(codeBox: Box[String], errorBox: OutputArea, path: String=".")
  extends Box[(SBContext,Context,ArxNet,String=>Set[Int])](
            title = "Circuit of the program", List()) {
//extends GraphBox(null, errorBox, path, "Circuit of the program"){

//  var graph: Circuit = _
  var sbCtx:SBContext = _
  var types: Context = _
  var net:ArxNet = _
  var nodeIDs:String=>Set[Int] = _
  var box: Block = _

  protected val widthCircRatio = 7
  protected val heightCircRatio = 3
  protected val densityCirc = 0.5 // nodes per 100x100 px


  def get: (SBContext, Context, ArxNet, String => Set[Port]) =
    (sbCtx,types,net,nodeIDs)

  def init(div: Block, visible: Boolean): Unit = {
    box = GraphBox.appendSvg(super.panelBox(div,visible,
      buttons = List(
        Right("download")-> (() => saveSvg(),"Download image as SVG")
      )),"circuit", path=path)
    whenClickTitle(()=> if (!isVisible) updateCore())
  }

  private def updateCore(): Unit = {
    box.deleteAll("g")
    drawGraph()
  }

  def update(): Unit =
    if(isVisible) updateCore()

//  def drawGraph(): Unit = try{
//    clear()
//    val program = DSL.parse(codeBox.get)
//    println(s"[arx-prog] - Drawing graph - $program")
//    val (typedProgram,_) = typeCheck(program)
//    val (net1,maxPort1) = Net(typedProgram)
//    println(s"[arx-net] - Drawing graph (max=$maxPort1):\n$net1")
//    val (net2,maxPort2) = addNodes(net1,Some(maxPort1+1))
//    println(s"[arx-net] - Added Nodes (max=$maxPort2):\n$net2")
//    val preo1: Network = mkPreoNet(net2)
//    //println(s"[preo] - 'Got Network' - $preo1")
//    val ms = new Mirrors
//    val preo2 = Network.simplifyGraph(preo1,ms)
//    //println(s"[preo] - 'simplified' - $preo2")
//    val preo3 = Network.addRedundancy(preo2,ms,Some(maxPort2+1))
//    println(s"[arx-preo] - first preo - $preo1")
//    println(s"[arx-preo] - Simplified graph - $preo2")
//    println(s"[arx-preo] - with redundancy - $preo3")
//
//    val graph = Circuit(preo3,ms)//Circuit(dependency.get,true)
//    println(s"[arx-preo] - final mirrors - $ms")
//    val size = graph.nodes.size
//    val factor = Math.sqrt(size * 10000 / (densityCirc * widthCircRatio * heightCircRatio))
//    val width = (widthCircRatio * factor).toInt
//    val height = (heightCircRatio * factor).toInt
//    box.attr("viewBox", s"00 00 $width $height")
//    //println("Drawing graph - source: "+dependency.get)
//    //println("Drawing graph - produced: "+ graph)
//    //    toJs(graph)
//    scalajs.js.eval(GraphsToJS(graph))
//  }
//  catch Box.checkExceptions(errorBox)

  def drawGraph(): Unit = try{
    clear()
    val prog = DSL.parse(codeBox.get)
    //println(s"[DSLGr] - Drawing graph - $prog")
    val (tprog,tctx) = DSL.typeCheck(prog)
    sbCtx = DSL.encode(tprog,tctx)
    val (sb,_,_,newNet) = sbCtx("Program")
    //println(s"[DSLGr] got program: ${Show(sb)}  ==  $newNet")
    net = newNet
    net.clearMirrors()
    types = tctx

    // keeping track of node IDs
    val nIDs = mutable.Map[String,Int]()
    var fresh = 0
    def getNode(n:String): Port = nIDs.get(n) match {
      case Some(i) => i
      case None =>
        fresh += 1
        nIDs += n -> fresh
        fresh
    }

    addNodes(net,sb.inputs,sb.outputs)
    //println(s"[DSLGr] making net from ${sb.inputs} to ${sb.outputs}: $net")
    val preo1 = mkPreoNet(net,sb.inputs,sb.outputs,getNode)
    //println(s"[DSLGr] nodeIds $nIDs")
    //println(s"[DSLGr] - 'Got Network' - $preo1")

    val ms = new Mirrors
    val preo2 = Network.simplifyGraph(preo1,ms)
    // flip ms!
    ms.inverse()
    //println(s"[DSLGr] - 'simplified' - $preo2  ###  $ms")
    val preo3 = Network.addRedundancy(preo2,ms,Some(fresh+1))
    //println(s"[DSLGr] - with redundancy - $preo3  ###  $ms")

    val graph = Circuit(preo3,ms)//Circuit(dependency.get,true)
    //println(s"[DSLGr] - final graph - $graph  ###  $ms")
    val size = graph.nodes.size
    val factor = Math.sqrt(size * 10000 / (densityCirc * widthCircRatio * heightCircRatio))
    val width = (widthCircRatio * factor).toInt
    val height = (heightCircRatio * factor).toInt
    box.attr("viewBox", s"00 00 $width $height")

    nodeIDs = (port:String) => {
      nIDs.get(port) match {
        case Some(i) =>
//          println(s"!! ms(${i}/$port)=${ms(i).mkString(",")}")
          iterateMs(ms,Set(i))
        case None =>
//          println(s"!! ms($port)= _L")
          Set()
      }
    }

    @tailrec
    def iterateMs(ms:Mirrors, arg:Set[Int]): Set[Int] = {
      val res = arg.flatMap(i => ms(i) + i)
      if (res == arg) res
      else iterateMs(ms,res)
    }

    scalajs.js.eval(GraphsToJS(graph))
  }
  catch Box.checkExceptions(errorBox)


  protected def clear(): Unit = {
    //box.selectAll("g").html("")
    //// EXPERIMENTING TO DROP D3JS
    box.deleteAll("g")
  }

  /////////////////////
  /////////////////////

  private def addNodes(net:ArxNet, ins:Set[String], outs:Set[String]): Unit = {
    var ns = (
      ins.map( p => p -> (Set(p) , Set[String]())) ++
      outs.map(p => p -> (Set[String]() , Set(p))) ++
      (ins intersect outs).map(p =>  p -> (Set(p) , Set(p)))
    ).toMap
    for (e <- net.getEdges) {
      net -= e
      val (e2,ns2) = updEdge(e,ns,net)
      net += e2
      ns = ns2
    }
    for (n <- ns)
      net += Edge(n._2._1,n._2._2,"node")
  }

  private def updEdge(e:Edge, nodes:Map[String,(Set[String],Set[String])],net:ArxNet)
      : (Edge, Map[String,(Set[String],Set[String])]) = {
    var ns = nodes
    var seed = 0

    def upd(p:String,isIn:Boolean): String = {
      val intfs = ns.getOrElse(p,(Set[String](),Set[String]()))
      val newPort = s"§${e.prim}-${e.from.mkString(",")}-${e.to.mkString(",")}-$seed"
//      for (p <- e.from ++ e.to) {
        if (!isIn) {
          //println(s"±± adding $p -> $newPort to mirrors")
          net += (p,newPort)
        }
//      }
      ns += p -> (if (isIn) (intfs._1 + newPort,intfs._2)
                  else      (intfs._1,intfs._2 + newPort))
      seed += 1
      newPort
    }

    val ins = e.from.map(upd(_,isIn = false))
    val outs = e.to.map( upd(_,isIn = true))
    (Edge(ins,outs,e.prim),ns)
  }


//  /** For each connector c:ins->outs, replace boundary interfaces with nodes  */
//  private def addNodes(net:Net,newseed:Option[IPort]=None): (Net,IPort) = {
//    val seed = newseed match {
//      case Some(value) => value
//      case None =>
//        val io = net.prims.flatMap(p => p.ins++p.out)
//        val allPorts = io ++ net.ins ++ net.outs
//        allPorts.max + 1
//    }
//    addNodesAux(net,seed)
//  }
//
//  private def addNodesAux(net:Net,seed:IPort): (Net,IPort) = {
//    var conns = List[Connector]()
//    var sd = seed
//    val ins:  Map[Int,(Set[Int],Set[Int])] = net.ins.map( p => p -> (Set(p),Set[Int]())).toMap
//    val outs: Map[Int,(Set[Int],Set[Int])] = net.outs.map(p => p -> (Set[Int](),Set(p))).toMap
//    val inouts = (for (i <- ins.keySet.intersect(outs.keySet)) yield i -> (ins(i)._1,outs(i)._2)).toMap
//    var ns: Map[Int,(Set[Int],Set[Int])] = ins ++ outs ++ inouts
////    var (conns,sd,ns): (List[Connector],Int,Map[Int,(List[Int],List[Int])]) = (Nil,seed,ns2)
//    for (c<-net.prims) {
//      val res = addNodes(c)(sd,ns)
//      conns ::= res._1
//      sd = res._2
//      ns = res._3
//    }
//    val nodeConns = ns.map(kv => Connector("node",kv._2._1, kv._2._2))
//    (Net(conns++nodeConns,net.ins,net.outs) , sd)
//  }
//
//  private def addNodes(con:Connector)
//                      (implicit seed:Int, nodes:Map[Int,(Set[Int],Set[Int])])
//      : (Connector,Int,Map[Int,(Set[Int],Set[Int])]) = {
//    var ns = nodes
//    var sd = seed
//    def upd(p:Int,in:Boolean): Int = {
//      val intfs:(Set[Int],Set[Int]) = ns.getOrElse(p,(Set(),Set()))
//      ns += p -> (if (in) (intfs._1 + sd,intfs._2)
//                  else    (intfs._1,intfs._2 + sd))
//      sd += 1
//      sd-1
//    }
//    val ins = con.ins.map(upd(_,in = false))
//    val outs = con.out.map(upd(_,in = true))
//    (Connector(con.name,ins,outs),sd,ns)
//  }


  private def mkPreoNet(net: ArxNet,
                        ins:Iterable[String],
                        outs:Iterable[String],
                        getNode: String=>Port): Network = {
    val edges = net.getEdges.map(mkPreoPrim2(_,getNode)).toList
    Network(edges,ins.toList.map(getNode),outs.toList.map(getNode))
  }




  private def mkPreoPrim2(edge: ArxNet.Edge, nodeId: String => Int): Network.Prim = {
    val extra:Set[Any] =
      (if (edge.prim == "node" && edge.from.size>1) Set[Any]("mrg")  else Set()) ++
        (if (edge.prim == "node" && edge.to.size>1) Set("dupl") else Set()) ++
        (if (edge.prim.startsWith("±")) Set("box") else Set() ) ++
        (if (edge.prim == "BUILD" || edge.prim == "MATCH") Set("box") else
          if (edge.from.isEmpty && edge.to.size==1)    Set("box")  else Set())
    val name = if (edge.prim.startsWith("±")) edge.prim.drop(1) else edge.prim
    Network.Prim(CPrim(name,CoreInterface(edge.from.size),CoreInterface(edge.to.size),extra)
                , edge.from.map(nodeId).toList, edge.to.map(nodeId).toList, Nil)
  }

//  private def mkPreoNet(net: Net): Network =
//    Network(net.prims.map(mkPreoPrim),net.ins.toList, net.outs.toList)
//  private def mkPreoPrim(p:Net.Connector): Network.Prim = {
//    val extra:Set[Any] =
//      (if (p.name == "node" && p.ins.size>1) Set[Any]("mrg")  else Set()) ++
//      (if (p.name == "node" && p.out.size>1) Set("dupl") else Set()) ++
//      (if (p.name == "BUILD" || p.name == "MATCH") Set("box") else
//       if (p.ins.isEmpty && p.out.size==1)    Set("box")  else Set())
//    Network.Prim(CPrim(p.name,CoreInterface(p.ins.size),CoreInterface(p.out.size),extra)
//                , p.ins.toList, p.out.toList, Nil) // no parents yet.
//  }
}
