package widgets.feta

import common.Utils
import common.frontend.MermaidJS
import common.widgets.{Box, OutputArea}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import fta.{DSL, Specification}
import ifta.{DSL => FDSL}
import widgets.RemoteBox
import fta.Specification.FSTSpec

/**
 * Created by guillecledou on 21/04/2021
 */

class FSysGraphBox(code: Box[String], errorBox: OutputArea)
  extends FetaGraphBox(code,errorBox) {

  override val title: String = "(F)System diagram"
  override val feta:String = "FSys"


  override def showGraph(data:String):Unit = try {
    val products = FDSL.parseProducts(data)
    //
    val fSys = DSL.interpretInServer(spec.ignoreSyncTypes, products)
    val mermaid = DSL.toMermaid(fSys)
    val mermaidJs = MermaidJS(mermaid,s"${feta}GraphBox",s"svg$feta")
    scalajs.js.eval(mermaidJs)
  } catch Box.checkExceptions(errorBox)
}
