package widgets.feta

import common.widgets.{Box, OutputArea}
import fta.backend.MmCRL2._
import fta.features.FExp
import fta.{DSL, Specification, TeamLogic}
import ifta.{DSL => FDSL}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import widgets.{RemoteBox, RemoteMcrl2GenBox}

class SafetyReqBox(code: Box[String],
//                   var mCRL2Box: RemoteMcrl2GenBox,
                   errorBox: OutputArea)
  extends Box[(String,List[(String,String)])]("Safety Requirements Characterisation in mCRL2", List(code)) {


  protected var box: Block = _
  protected var spec: Specification = _
  protected var mCRL2: String = ""
  protected var formulas: List[(String,String)] = Nil // name and formula
  protected var mCRL2Box: RemoteMcrl2GenBox = _
  protected var mermaidBox: MermaidGraphBox = _


  def setMCRL2(m:RemoteMcrl2GenBox) = mCRL2Box=m
  def setMermaid(m:MermaidGraphBox) = mermaidBox=m

  /** Produces the mCRL2 specification code and the mCRL2 formulas to be verified. */
  override def get: (String,List[(String,String)]) = (mCRL2,formulas)

  /**
   * Executed once at creation time, to append the content to the inside of this box
   *
   * @param div     Placeholder that will receive the "append" with the content of the box
   * @param visible is true when this box is initially visible (i.e., expanded).
   */
  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible, buttons = List())
      .append("div")
      .attr("id", "safetyReqBox")
      .style("margin", "10px")
    dom.document.getElementById(title).firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { e: MouseEvent => if (!isVisible) show() }
  }


  /**
   * Block of code that should read the dependencies and:
   *  - update its output value, and
   *  - produce side-effects (e.g., redraw a diagram)
   */
  override def update(): Unit = if (isVisible || mCRL2Box.isVisible || mermaidBox.isVisible) show()

  def forceUpdate(): Unit = show() // will also trigger the checkMcrl2

  def setMcrl2(_mCRL2Box: RemoteMcrl2GenBox) =
    mCRL2Box = _mCRL2Box

  def show(): Unit =
    try {
      mCRL2 = ""
      formulas = Nil
      mermaidBox.feta = None
      spec = DSL.parse(code.get)
      val fm = spec.fm
      val fmInfo =
        s"""{ "fm":     "${fm.simplify.toString}", """ +
          s"""  "feats":  "${spec.fcas.flatMap(f => f.features).mkString("(", ",", ")")}" }"""

      RemoteBox.remoteCall("ifta", fmInfo, showInfo)

    } catch Box.checkExceptions(errorBox)

  def showInfo(data: String): Unit = {
    val products = FDSL.parseProducts(data)
    val feta = DSL.interpretInServer(spec, products)
    mermaidBox.feta = Some(feta)

    if (products.isEmpty) {
      errorBox.error("No products found.")
      return
    }
    val prod: FExp.Product = products.head
    if (products.size == 1 && products.head.nonEmpty)
      errorBox.message(s"Only 1 product found: {${products.head.mkString(",")}}.")
    if (products.size>1)
      errorBox.message(s"${products.size} products found. Using product {${prod.mkString(",")}} in the Safety Requirements Characterisation in mCRL2.")

    val par = parallel(feta.s.components.map(toParamProcess(_,Some(prod))))
    val all = wrapAllowFSys(feta, par)
    mCRL2 = all.code
    val formula1 = "Receptiveness" -> toMuFormula(TeamLogic.getReceptivenesReq(feta.s, feta.fst, prod))
    val formula2 = "Weak Receptiveness" -> toMuFormula(TeamLogic.getWeakReceptivenesReq(feta.s, feta.fst, prod))
    val formula3 = "Responsiveness" -> toMuFormula(TeamLogic.getResponsivenesReq(feta.s, feta.fst, prod))
    val formula4 = "Weak Responsiveness" -> toMuFormula(TeamLogic.getWeakResponsivenesReq(feta.s, feta.fst, prod))

    formulas = List(formula1,formula3,formula2,formula4)

    // got all information
    // 1. call the mCRL2Box to update
    if (mermaidBox.isVisible)
         mCRL2Box.forceUpdate() // run mCRL2 even if invisble
    else mCRL2Box.update() // run mCRL2 based on its visibility
//    mCRL2Box.runMcrl2(mCRL2,List(formula))

    // 2. display information

    box.text("")

    box.append("p")
      .append("strong")
      .text(s"Receptiveness: \n")
    box.append("pre")
      .text(formula1._2)

    box.append("p")
      .append("strong")
      .text(s"Responsiveness: \n")
    box.append("pre")
      .text(formula3._2)

    box.append("p")
      .append("strong")
      .text(s"Weak Receptiveness: \n")
    box.append("pre")
      .text(formula2._2)

    box.append("p")
      .append("strong")
      .text(s"Weak Responsiveness: \n")
    box.append("pre")
      .text(formula4._2)

    box.append("p")
      .append("strong")
      .text(s"mCRL2 full system: \n")
    box.append("pre")
      .text(mCRL2)


    ////

//     box.append("p")
//      .append("strong")
//      .text(s"-- Experiments from here --\n")


//    box.append("p")
//      .append("strong")
//      .text(s"Weak Receptiveness Debug: \n")
//    box.append("pre")
//      .text(TeamLogic.getWeakReceptivenesReq(feta.s, feta.fst, prod).toString+"\n\n\n"+
//              toMuFormula(TeamLogic.getWeakReceptivenesReq(feta.s, feta.fst, prod)))

//    box.append("p")
//      .append("strong")
//      .text(s"Receptiveness Debug: \n")
//    box.append("pre")
//      .text(TeamLogic.getReceptivenesReq(feta.s, feta.fst, prod).toString)


//    box.append("p")
//      .append("strong")
//      .text(s"Receptiveness Debug: \n")
//    box.append("pre")
//      .text(TeamLogic.getReceptivenesReq(feta.s, feta.fst, prod).toString)

//    box.append("p")
//      .append("strong")
//      .text(s"mCRL2 experiments: \n")
////    box.append("pre")
////      .text(MmCRL2Spec(
////        Map("P1"-> ((Act("a1") > Proc("P2")) + (Act("a2") > Proc("P1"))),
////            "P2"-> Proc("0")),
////        Proc("P1")
////      ).code)
//    for (fca<-feta.s.components) {
//      box.append("p")
//        .text(s"CA ${fca.name}: \n")
//      box.append("pre")
//        .text(toProcess(fca, None).code)
//    }
////    val par = parallel(feta.s.components.map(toParamProcess(_)))
////    val all = wrapAllowFSys(feta,par)
//    val justFeta = wrapAllowFETA(feta,prod,par)
//    val extendedFeta = wrapAllowFETAExtended(feta,prod,par)

//    // Note to self: getting Allowed transitions requires traversing (several times) throughout all
//    // of the transitions of the FSystem (taus + comm actions alone + communicating actions that match)

//    // Note 2: this "extendedFeta" optimisation seems not as great as it sounded originally: avoids longer
//    // list of "allowed" in mCRL2 in the presence of longer number of combinations, but before producing mCRL2
//    // this list still needs to produce all combinations (before filtering to the allowed ones). But this means
//    // that probably AllowFeta and AllowFetaExtended can be further optimised (but not for here).

//    box.append("p")
//      .text(s"Full LTS (lts(S)): \n")
//    box.append("pre")
//      .text(all.code)

//    box.append("p")
//      .text(s"ETA (st(S)): \n")
//    box.append("pre")
//      .text(justFeta.code)

//    box.append("p")
//      .text(s"ETA++ (extended with single senders/receivers): \n")
//    box.append("pre")
//      .text(extendedFeta.code)
  }
}