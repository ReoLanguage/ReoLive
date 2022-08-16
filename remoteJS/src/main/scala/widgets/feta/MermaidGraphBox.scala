package widgets.feta

import common.Utils
import common.frontend.MermaidJS
import common.widgets.{Box, OutputArea}
import fta.eta.System.SysLabelComm
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import fta.{DSL, Specification}
import ifta.{DSL => FDSL}
import widgets.{RemoteBox, RemoteMcrl2GenBox}

import scala.util.matching.Regex

/**
  * Created by guillecledou on 21/04/2021
  */

class MermaidGraphBox(header: String, errorBox: OutputArea)
  extends Box[Unit](header, List()) {

//  val feta:String = "Feta"
  private var box:Block = _
  protected var mCRL2Box: RemoteMcrl2GenBox = _
  protected var spec:Specification = _
  protected var id = "I"+header.hashCode
  var feta: Option[fta.feta.FETA] = None // set externally by SafetyReqBox

  /** mCRL2 box MUST be set before updating. */
  def setMCRL2(m:RemoteMcrl2GenBox) = mCRL2Box=m

  override def get:Unit= ()

  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div     Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible, buttons = List(
      Right("download") -> (() => Utils.downloadSvg(s"svg${id}"), "Download SVG")
    ))
      .append("div")
      .style("display", "flex")
      .style("flex-flow", "row wrap")
      .style("align-items", "baseline")
      .style("align-content", "flex-start")
      //.style("justify-content", "flex-start")
      .style("padding", "5px 1px 5px 15px")

    waiting()

    dom.document.getElementById(title).firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { e: MouseEvent => if (!isVisible) showGraph() }
  }

  private def waiting(): Unit = {
    box.html ("") //clear
    box.append ("p").text ("Waiting for mCRL2's evidences")
  }

//  private def setUp(id:String): Unit = {
//    box
//      .append("div")
//      .attr("class", "mermaid")
//      .attr("id", s"${id}GraphBox")
//      .style("text-align", "center")
//      .append("div").attr("id", s"svg${id}")
//
//  }

  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
  override def update(): Unit = if(isVisible) showGraph()

  def showGraph():Unit = try {
    waiting()
    if (mCRL2Box.get.isEmpty || feta.isEmpty) return // nothing yet
    box.html("") //clear
    var index = 0
    for ((solved,(pName,mermaid)) <- mCRL2Box.get if mermaid != "") { // just processing the first message
      val fixed = fixMermaid(mermaid)
      val newId = id+index
      index += 1
      showOne(s"$pName: $solved", fixed, newId)
//      val mermaidJs = MermaidJS(fixed, s"${newId}GraphBox", s"svg${newId}")
//      box.append("p")
//        .style("text-align","center")
//        .append("strong")
//        .text(s"$pName: $solved")
//      setUp(newId)
//      scalajs.js.eval(mermaidJs)
    }
  } catch Box.checkExceptions(errorBox)


  private def showOne(title:String,mermaid:String,id:String) = {
    val mbox = box.append("div")
      .style("text-align", "center")
//      .style("min-width","50rem")
    mbox.append("h4")
      .style("margin-left", "1rem")
      .style("margin-right", "1rem")
      .text(s"${title}")
    mbox.append("div")
      .attr("class", "mermaid")
      .attr("id", s"${id}GraphBox")
      .style("text-align", "center")
      .append("div").attr("id", s"svg${id}")

    val mermaidJs = MermaidJS(mermaid, s"${id}GraphBox", s"svg${id}")
    scalajs.js.eval(mermaidJs)
  }

  private def fixMermaid(merm: String): String = {
    val re = "\"([^\"]+)\"".r
    re.replaceAllIn(merm, m => "\""+mkAction(m.group(1))+"\"")

  }

  private def mkAction(acts:String): String = try {
    val acts2 = acts // "comp1_act1|comp2_act2
      .split('|') // Array(comp1_act1, "comp2_act2")
      .map(a => a.split("_",2))
    val actNames = acts2.map(_.tail.head).toSet
    if (actNames.size!=1) return acts
    val actName = actNames.head
    val comps = acts2.map(_.head).toSet
    if (feta.get.communicating(actName)) {
      val ins = comps.filter(c => feta.get.s.components.exists(fca=>fca.name==c && fca.inputs(actName)))
      val outs = comps--ins
      s"{${outs.mkString(",")}} $actName {${ins.mkString(",")}}"
    } else
      s"$actName @ ${comps.map(_.toString).mkString(",")}"
  } catch {
    case _:Throwable => acts
  }

}
