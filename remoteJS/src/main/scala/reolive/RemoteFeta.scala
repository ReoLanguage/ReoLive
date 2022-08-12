package reolive

import common.DomNode
import common.widgets.OutputArea
import fta.{DSL, TeamLogic}
import widgets.feta.{FCABox, FETAInfoBox, FSysGraphBox, FetaBox, FetaExamplesBox, FetaGraphBox, MermaidGraphBox, SafetyReqBox}
import org.scalajs.dom.html
import widgets.RemoteMcrl2GenBox
import fta.backend.MmCRL2._
import fta.features.FExp

import scala.scalajs.js.annotation.JSExportTopLevel

/**
 * Created by guillecledou on 21/04/2021
 */

object RemoteFeta {

  var errorArea: OutputArea = _
  var descriptionArea: OutputArea = _
  var fetaBox: FetaBox = _
  var fetaGraphBox: FetaGraphBox = _
  var fSysGraphBox: FSysGraphBox = _
  var examples:FetaExamplesBox = _
  var fcaBox: FCABox = _
  var fetaInfo:FETAInfoBox = _
  var safetyReq:SafetyReqBox = _
  var mcrl2:RemoteMcrl2GenBox = _
  var evGraphBox:MermaidGraphBox = _

  @JSExportTopLevel("reolive_RemoteFeta_main")
  def main(content: html.Div): Unit = {


    // Creating outside containers:
    val contentDiv = DomNode.select(content).append("div")
      .attr("class", "content")

    val rowDiv = contentDiv.append("div")
      //      .attr("class", "row")
      .attr("id", "mytable")

    val leftColumn = rowDiv.append("div")
      //      .attr("class", "col-sm-4")
      .attr("id", "leftbar")
      .attr("class", "leftside")

    leftColumn.append("div")
      .attr("id", "dragbar")
      .attr("class", "middlebar")

    val rightColumn = rowDiv.append("div")
      //      .attr("class", "col-sm-8")
      .attr("id", "rightbar")
      .attr("class", "rightside")

    descriptionArea = new OutputArea
    errorArea = new OutputArea
    fetaBox = new FetaBox(reload(), "", errorArea)
    fetaGraphBox = new FetaGraphBox(fetaBox, errorArea)
    fSysGraphBox = new FSysGraphBox(fetaBox, errorArea)
    examples = new FetaExamplesBox(softReload(),List(fetaBox,descriptionArea))
    fcaBox = new FCABox(fetaBox,errorArea)
    fetaInfo = new FETAInfoBox(fetaBox,errorArea)

    safetyReq = new SafetyReqBox(fetaBox,errorArea)
    evGraphBox = new MermaidGraphBox("View mCRL2 evidence",errorArea)
    def mermaidCallback(): Unit =
      if (evGraphBox.isVisible) evGraphBox.update()
    mcrl2 = new RemoteMcrl2GenBox(safetyReq,header="Verification in mCRL2",callback = mermaidCallback, errorBox=errorArea)
    //
    safetyReq.setMCRL2(mcrl2)
    safetyReq.setMermaid(evGraphBox)
    evGraphBox.setMCRL2(mcrl2)

//    def getMcrl2Specs: (String,List[String]) = {
////      s"""{ "fm":     "${fm.simplify.toString}", """ +
////        s"""  "feats":  "${spec.fcas.flatMap(f => f.features).mkString("(", ",", ")")}" }"""
//      val spec = DSL.parse(fetaBox.get)
//      /// from IftaActor, to solve the feature model
//      val fm: FExp = spec.fm.simplify
//      val feats: Set[String] = spec.fcas.flatMap(f => f.features)
////      val strResult = fm.products(feats).map(p => p.mkString("(", ",", ")")).mkString("(", ",", ")")
//      ///
//
//      val feta = DSL.interpretInServer(spec, fm.products(feats))
//      val par = parallel(feta.s.components.map(toParamProcess(_)))
//      val all = wrapAllowFSys(feta, par)
//      val forms = List(toMuFormula(TeamLogic.getReceptivenesReq(feta.s, feta.fst)))
//      all.code -> forms
//    }

    fetaBox.init(leftColumn, true)
    errorArea.init(leftColumn)
    descriptionArea.init(leftColumn)
    fetaGraphBox.init(rightColumn, false)
    fSysGraphBox.init(rightColumn, false)
    examples.init(leftColumn,true)
    fcaBox.init(rightColumn,false)
    fetaInfo.init(leftColumn,false)
    evGraphBox.init(rightColumn,false)
    mcrl2.init(rightColumn,true)
    safetyReq.init(rightColumn,true)

    common.Utils.moreInfo(rightColumn, "https://github.com/arcalab/team-a")

    // load default button
    if (!examples.loadButton("Race (ETA)")) {
      reload()
    }
  }

  /**
   * Function that parses the expressions written in the input box and
   * tests if they're valid and generates the output if they are.
   */
  private def reload(): Unit = {
    descriptionArea.clear()
    softReload()
  }

  private def softReload(): Unit = {
    errorArea.clear()
    fetaBox.update()
    fetaGraphBox.update()
    fSysGraphBox.update()
    fcaBox.update()
    fetaInfo.update()
    safetyReq.update()
    mcrl2.update()
    evGraphBox.update()
  }

}
