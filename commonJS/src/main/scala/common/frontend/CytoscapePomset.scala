package common.frontend

import choreo.Agent
import choreo.backend.Show
import choreo.semantics.Pomset.{Event, Label, Labels, Order}
import choreo.semantics.{Pomset, PomsetFamily}

/**
  * Created by guillecledou on 04/11/2020
  */


object CytoscapePomset {

  private lazy val labelBackground = "#666666"
  private lazy val clusterBackground = "#ECECFF"
  private lazy val pomsetBachground = "#F0F0F0"
  private lazy val edgeArrow = "triangle"

  private var seedId:Int = 0
  private def seed():Int = {seedId+=1;seedId-1}

  def apply(p: PomsetFamily, elementId: String): String = {
    seedId = 0
    s"""
       |var cy = cytoscape({
       |    container: document.getElementById('${elementId}'),
       |    elements: [${mkFamilyElements(p.pomsets)}],
       |    style: [
       |      { selector: 'node',
       |        style: { 'background-color': '${labelBackground}','label': 'data(label)'}
       |      },
       |      { selector: "node[^parent]",
       |        style: { 'background-color': '${pomsetBachground}', 'label': 'data(label)','font-weight':'bold'}
       |      },
       |      { selector: "node[cluster]",
       |        style: { 'background-color': '${clusterBackground}', 'label': 'data(label)', 'font-weight':'bold'}
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
       |cy.elements("[^parent]").layout({
       |  name:"dagre",
       |  rankDir:'LR',
       |  avoidOverlap: true,
       |  fit:true,
       |  nodeDimensionsIncludeLabels: true}).run();
       |cy.elements().renderedBoundingBox();
       |""".stripMargin
  }

  def mkFamilyElements(pomsets: Set[Pomset]): String =
    pomsets.flatMap(p=>mkPomsetElements(p)(seed())).mkString(",")

  def mkPomsetElements(p: Pomset)(implicit id:Int): List[String] =
    nodes(p)++edges(p)

  def nodes(p: Pomset)(implicit id:Int): List[String] =
    s"""{data:{id:'p${id}', parent: undefined, label:'P$id'}}"""::
    p.agents.flatMap(a=>mkAgentNodes(a,p)).toList

  def mkAgentNodes(a: Agent, p: Pomset)(implicit id:Int): List[String] =
    s"""{data:{id:'p$id-${a.name}', parent: 'p$id', label:'${a.name}',cluster:true}}"""::
      p.labelsOf(a).map((mkLabel)).toList

  def mkLabel(l:(Event,Label))(implicit id:Int):String =
    s"""{data:{id:'${l._1}', parent:'p$id-${l._2.active.name}', label:'${Show(l._2)}'}}"""

  def edges(p:Pomset)(implicit id:Int):List[String] =
    p.order.map(o=>mkOrder(o,p.labels)).toList

  def mkOrder(o:Order,labels:Labels)(implicit id:Int):String =
    s"""{data: { id: '${o.left}-${o.right}',
       |        source: '${o.left}',
       |        target: '${o.right}',
       |        color: '${mkEdgeColor(o, labels)}',
       |        parent: '${mkParent(o,labels)}'}}""".stripMargin

  def mkParent(o: Order,labels:Labels)(implicit id:Int):String =
    if (labels(o.right).active == labels(o.left).active) s"p$id-${labels(o.right).active.name}" else "p"+id.toString

  def mkEdgeColor(o: Order,labels:Labels):String = {
    if (labels(o.right).active == labels(o.left).active) "#c2a566"
    else if (labels(o.right).passive.contains(labels(o.left).active)) "black"
    else "orange"
  }
}
