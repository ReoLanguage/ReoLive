package common.frontend

import choreo.Agent
import choreo.backend.Show
import choreo.semantics.Pomset.{Event, Label, Labels, Loops, Order}
import choreo.semantics.{Pomset, PomsetFamily}

/**
  * Created by guillecledou on 04/11/2020
  */


object CytoscapePomset {

  private lazy val labelBackground = "#666666"
  private lazy val clusterBackground = "#ECECFF"
  private lazy val pomsetBackground = "#F0F0F0"
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
       |        style: {
       |          'background-color': '${pomsetBackground}', 'label': 'data(label)',
       |          'font-weight':'bold','shape':'round-rectangle'}
       |      },
       |      { selector: "node[class='cluster']",
       |        style: {
       |          'background-color': '${clusterBackground}', 'label': 'data(label)',
       |          'font-weight':'bold','shape':'round-rectangle'}
       |      },
       |      { selector: "node[class='invisible']",
       |        style: { 'visibility':'hidden','label':'data(label)','width':'5em','height':'5em'}
       |      },
       |      { selector: "node[class='loop']",
       |        style: {
       |          'background-opacity': '0', 'border-color':'#999999', 'opacity':'0.35',
       |          'label': 'data(label)','z-compound-depth':'top','shape':'round-rectangle'}
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
       |cy.nodes("[class='invisible']").map(function(node){
       |  cy.$$("#"+node.data('id')).position(cy.$$('#'+node.data('mirror')).position());
       | });
       |cy.fit(20);
       |""".stripMargin
  }

  def mkFamilyElements(pomsets: Set[Pomset]): String =
    pomsets.flatMap(p=>mkPomsetElements(p)(seed())).mkString(",")

  def mkPomsetElements(p: Pomset)(implicit id:Int): List[String] =
    nodes(p)++edges(p)++loops(p)

  def nodes(p: Pomset)(implicit id:Int): List[String] =
    s"""{data:{id:'p${id}', parent: undefined, label:'P$id'}}"""::
    p.agents.flatMap(a=>mkAgentNodes(a,p)).toList

  def mkAgentNodes(a: Agent, p: Pomset)(implicit id:Int): List[String] =
    s"""{data:{id:'p$id-${a.name}', parent: 'p$id', label:'${a.name}',class:'cluster'}}"""::
      p.labelsOf(a).map((mkLabel)).toList

  def mkLabel(l:(Event,Label))(implicit id:Int):String =
    s"""{data:{id:'${l._1}', parent:'p$id-${l._2.active.name}', label:'${Show(l._2)}'}}"""

  def loops(p:Pomset)(implicit id:Int):List[String] =
    p.loops.flatMap(l=>mkLoop(l,p.labels,id)(seed())).toList

  def mkLoop(loop:Set[Event],labels:Labels,pomId:Int)(implicit loopId:Int):List[String] =
    s"""{data:{id:'loop$loopId', parent: 'p$pomId', label:'loop', class:'loop'}}"""::
    loop.map(l=>mkLoopEvent(l,labels)).toList

  def mkLoopEvent(e:Event,labels:Labels)(implicit loopId:Int):String =
    s"""{data:{id:'${e}bis-$loopId',
       |       parent:'loop$loopId',
       |       label:'${Show(labels(e))}',
       |       class:'invisible',
       |       mirror:'$e'}}""".stripMargin

  def edges(p:Pomset)(implicit id:Int):List[String] =
    p.order.map(o=>mkOrder(o,p.labels)).toList

  def mkOrder(o:Order,labels:Labels)(implicit id:Int):String =
    s"""{data: { id: '${o.left}-${o.right}',
       |        source: '${o.left}',
       |        target: '${o.right}',
       |        color: '${mkEdgeColor(o, labels)}',
       |        parent: '${mkParent(o,labels)}'}}""".stripMargin

  def mkParent(o: Order,labels:Labels)(implicit id:Int):String =
    if (labels(o.right).active == labels(o.left).active) s"p$id-${labels(o.right).active.name}"
    else "p"+id.toString

  def mkEdgeColor(o: Order,labels:Labels):String = {
    if (labels(o.right).active == labels(o.left).active) "#c2a566"
    else if (labels(o.right).passive.contains(labels(o.left).active)) "black"
    else "orange"
  }
}
