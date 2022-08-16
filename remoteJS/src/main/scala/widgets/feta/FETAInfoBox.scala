package widgets.feta

import common.frontend.MermaidJS
import common.widgets.{Box, OutputArea}
import fta.{DSL, Specification}
import ifta.{DSL => FDSL}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import widgets.RemoteBox

/**
 * Created by guillecledou on 03/05/2021
 */

class FETAInfoBox(code: Box[String], errorBox: OutputArea)
  extends Box[Unit]("FETA Information", List(code)) {


  protected var box: Block = _
  protected var spec:Specification = _

  override def get: Unit = ()

  /**
   * Executed once at creation time, to append the content to the inside of this box
   *
   * @param div     Placeholder that will receive the "append" with the content of the box
   * @param visible is true when this box is initially visible (i.e., expanded).
   */
  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible, buttons = List())
      .append("div")
      .attr("id", "fetaInfoBox")
      .style("margin","10px")
    dom.document.getElementById("FETA Information").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { e: MouseEvent => if (!isVisible) show() }
  }


  /**
   * Block of code that should read the dependencies and:
   *  - update its output value, and
   *  - produce side-effects (e.g., redraw a diagram)
   */
  override def update(): Unit = if (isVisible) show()

  def show(): Unit =
    try {
      spec = DSL.parse(code.get)
      val fm = spec.fm
      val fmInfo = s"""{ "fm":     "${fm.simplify.toString}", """ +
        s"""  "feats":  "${spec.fcas.flatMap(f=>f.features).mkString("(",",",")")}" }"""

      import fta.view.Show.showFE
      errorBox.message(s"waiting to solve ${showFE(fm)}")
      RemoteBox.remoteCall("ifta", fmInfo, showInfo )

    } catch Box.checkExceptions(errorBox)

  def showInfo(data:String):Unit = try {
    errorBox.clear()
    val products = FDSL.parseProducts(data)
    val feta = DSL.interpretInServer(spec,products)

    box.text("")

    box.append("p")
      .append("strong")
      .text(s"Products: ${feta.products.size} \n")
    box.append("ul").html(feta.products.map(p=> p.mkString("{",",","}")).mkString("<li>",",","</li>"))

    box.append("p")
      .append("strong")
      .text(s"Featured System: \n")
    box.append("ul").html(
      s"""<li> # transitions: ${feta.s.trans.size}
        |<li> # states: ${feta.s.states.size}
        |""".stripMargin
    )

    box.append("p")
      .append("strong")
      .text(s"FETA: \n")
    box.append("ul").html(
      s"""<li> # transitions: ${feta.trans.size}
         |<li> # states: ${feta.states.size}
         |""".stripMargin
    )

    //val fsts = for (a <- feta.fst.st.keySet ; p <- feta.fst.st(a).st.keySet)
    //            yield s"fst(${p.mkString("{",",","}")})($a) = ${feta.fst.st(a).st(p)}"
    var fsts:List[String] = Nil
    for (a <- feta.fst.st.keySet)
      for (p <- feta.fst.st(a).st.keySet)
        fsts+:=s"fst(${p.mkString("{",",","}")})($a) = ${feta.fst.st(a).st(p)}"

    box.append("p")
      .append("strong")
      .text(s"FSTS: \n")
    box.append("ul").html(
      s"""<li>${fsts.mkString("<br>")}</li>""".stripMargin
    )


  } catch Box.checkExceptions(errorBox,"FETA-info")


}