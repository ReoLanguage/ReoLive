package common.frontend

import choreo.Agent
import choreo.backend.Show
import choreo.semantics.Pomset.{Event, Label, Labels, Order}
import choreo.semantics.{Pomset, PomsetFamily}

/**
  * Created by guillecledou on 04/11/2020
  */


object CytoscapePomset {

  private lazy val labelBackground = "#666"
  private lazy val clusterBackground = "#ececff"
  private lazy val pomsetBachground = "#f4f4f2"
  private lazy val edgeArrow = "triangle"

  private var seedId:Int = 0
  private def seed():Int = {seedId+=1;seedId-1}

  def apply(p: PomsetFamily, elementId: String): String = {
    seedId = 0
    s"""
       |var cy = cytoscape({
       |    container: document.getElementById('${elementId}'),
       |    elements: [${mkFamilyElements(p.pomsets)}],
       |    style: [ // the stylesheet for the graph
       |      { selector: 'node',
       |        style: { 'background-color': '${labelBackground}','label': 'data(label)'}
       |      },
       |      { selector: "node[^parent]",
       |        style: { 'background-color': '${pomsetBachground}', 'label': 'data(label)'}
       |      },
       |      { selector: "node[^cluster][parent]",
       |        style: { 'background-color': '${clusterBackground}', 'label': 'data(label)'}
       |      },
       |      { selector: 'edge',
       |        style: {
       |          'width': 1,
       |          'line-color': 'data(color)',
       |          'target-arrow-color': 'data(color)',
       |          'target-arrow-shape': '${edgeArrow}',
       |          'curve-style': 'bezier'
       |        }
       |      }
       |    ],
       |});
       |cy.elements(":childless,edge").layout({
       |  name:"dagre",
       |  rankDir:'LR',
       |  avoidOverlap: true,
       |  fit:true,
       |  nodeDimensionsIncludeLabels: true}).run();
       |""".stripMargin
  }

  //  def mkLayouts(p:Pomset):String =
  //    p.agents.map(mkAgentLayout).mkString("",";",";")
  //
  //  def mkAgentLayout(a:Agent):String =
  //    s"""cy.nodes().filter("[parent='${a.name}']").layout({name:"dagre",rankDir:'LR'}).run()"""

  def mkFamilyElements(pomsets: Set[Pomset]): String =
    pomsets.flatMap(p=>mkPomsetElements(p)(seed())).mkString(",")

  def mkPomsetElements(p: Pomset)(implicit id:Int): List[String] =
    nodes(p)++edges(p)

  def nodes(p: Pomset)(implicit id:Int): List[String] =
    s"""{data:{id:'p${id}', parent: undefined, label:''}}"""::
    p.agents.flatMap(a=>mkAgentNodes(a,p)).toList

  def mkAgentNodes(a: Agent, p: Pomset)(implicit id:Int): List[String] =
    s"""{data:{id:'p$id-${a.name}', parent: 'p$id', label:'${a.name}'}}"""::
      p.labelsOf(a).map((mkLabel)).toList

  def mkLabel(l:(Event,Label))(implicit id:Int):String =
    s"""{data:{id:'${l._1}', parent:'p$id-${l._2.active.name}', label:'${Show(l._2)}',cluster:'true'}}"""

  def edges(p:Pomset)(implicit id:Int):List[String] =
    p.order.map(o=>mkOrder(o,p.labels)).toList

  def mkOrder(o:Order,labels:Labels)(implicit id:Int):String =
    s"""{data: { id: '${o.left}${o.right}',
       |        source: '${o.left}',
       |        target: '${o.right}',
       |        color: '${mkEdgeColor(o, labels)}',
       |        parent: '${mkParent(o,labels)}',
       |        cluster: 'true'}}""".stripMargin

  def mkParent(o: Order,labels:Labels)(implicit id:Int):String =
    if (labels(o.right).active == labels(o.left).active) s"p$id-${labels(o.right).active.name}" else "p"+id.toString

  def mkEdgeColor(o: Order,labels:Labels):String = {
    if (labels(o.right).active == labels(o.left).active) "#c2a566"
    else if (labels(o.right).passive.contains(labels(o.left).active)) "black"
    else "orange"
  }
}
