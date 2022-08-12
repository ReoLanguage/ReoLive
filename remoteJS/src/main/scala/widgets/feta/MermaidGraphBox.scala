package widgets.feta

import common.Utils
import common.frontend.MermaidJS
import common.widgets.{Box, OutputArea}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import fta.{DSL, Specification}
import ifta.{DSL => FDSL}
import widgets.{RemoteBox, RemoteMcrl2GenBox}

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

    waiting()

    dom.document.getElementById(title).firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { e: MouseEvent => if (!isVisible) showGraph() }
  }

  private def waiting(): Unit = {
    box.html ("") //clear
    box.append ("p").text ("Waiting...")
  }

  private def setUp(id:String): Unit = {
    box
      .append("div")
      .attr("class", "mermaid")
      .attr("id", s"${id}GraphBox")
      .style("text-align", "center")
      .append("div").attr("id", s"svg${id}")

  }

  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
  override def update(): Unit = if(isVisible) showGraph()

  def showGraph():Unit = try {
    waiting()
    if (mCRL2Box.get.isEmpty) return // nothing yet
    box.html("") //clear
    var index = 0
    for ((solved,(pName,mermaid)) <- mCRL2Box.get if mermaid != "") { // just processing the first message
      val newId = id+index
      index += 1
      val mermaidJs = MermaidJS(mermaid, s"${newId}GraphBox", s"svg${newId}")
      box.append("p")
        .style("text-align","center")
        .append("strong")
        .text(s"$pName: $solved (total: ${mCRL2Box.get.size})")
      setUp(newId)
      scalajs.js.eval(mermaidJs)
    }
  } catch Box.checkExceptions(errorBox)
}
