package common.widgets.newdsl

import common.frontend.GraphsToJS
import common.widgets.{Box, GraphBox, OutputArea}
import dsl.analysis.syntax.Program
import dsl.backend.Net
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
    //println(s"BB - Drawing graph - $program")
    val dslNet = Net(program)
    //println(s"CC - Drawing graph - $dslNet")
    val preoNet: Network = mkPreoNet(dslNet)
    //println(s"DD - Drawing graph - $preoNet")
    val ms = new Mirrors
    //val net2 = Network.simplifyGraph(preoNet,ms)
    val net3 = Network.addRedundancy(preoNet,ms)
    //println(s"EE - Simplified graph - $preoNet")

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


  private def mkPreoNet(net: Net): Network =
    Network(net.prims.map(mkPreoPrim),net.ins, net.outs)
  private def mkPreoPrim(p:Net.Connector): Network.Prim =
    Network.Prim(CPrim(p.name,CoreInterface(p.ins.size),CoreInterface(p.out.size))
                , p.ins, p.out, Nil) // no parents yet.
}
