package reolive


import common.widgets._
import common.widgets.virtuoso._
import org.scalajs.dom.html
import org.singlespaced.d3js.d3
import widgets.Virtuoso.{RemoteUppaalBox, RemoteVerifytaBox}

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * Created by guillecledou on 2019-09-30
  */


object RemoteVirtuoso extends {

  var inputBox: VirtuosoBox = _
  var graphics: GraphBox = _

  var instantiate: VirtuosoInstantiate = _

  var infoBox: VirtuosoInfoBox = _

  var examples: VirtuosoExamplesBox = _
  var errors: OutputArea = _
  var descr: OutputArea = _

  var aut: VirtuosoAutomataBox = _

  var uppaal:RemoteUppaalBox = _

  var csBox:VirtuosoCSInputBox = _
  var csInfoBox:VirtuosoCSInfoBox = _
  var outputCs:OutputArea = _

  var verifyta:RemoteVerifytaBox = _
  var verifytaOut:OutputArea =_
  var verifytaExpanded:OutputArea =_

  @JSExportTopLevel("reolive.RemoteVirtuoso.main")
  def main(content: html.Div): Unit = {


    // Creating outside containers:
    val contentDiv = d3.select(content).append("div")
      .attr("class", "content")

    val rowDiv = contentDiv.append("div")
      //      .attr("class", "row")
      .attr("id", "mytable")

    val leftColumn = rowDiv.append("div")
      //      .attr("class", "col-sm-4")
      .attr("id", "leftbar")
      .attr("class", "leftside")

    leftColumn.append("div")
      .attr("id","dragbar")
      .attr("class", "middlebar")

    val rightColumn = rowDiv.append("div")
      //      .attr("class", "col-sm-8")
      .attr("id", "rightbar")
      .attr("class", "rightside")

    errors = new OutputArea

    descr = new OutputArea {
      override def setValue(msg: String): Unit = {clear(); super.setValue(msg)}
    }

    verifytaOut = new OutputArea
    verifytaExpanded = new OutputArea

    inputBox = new VirtuosoBox(reload(),"port",errors)
    instantiate = new VirtuosoInstantiate(inputBox,errors)
    graphics = new VirtuosoGraphBox(instantiate,errors)
    aut = new VirtuosoAutomataBox(instantiate,errors)
    infoBox = new VirtuosoInfoBox(instantiate,errors)
    csBox = new VirtuosoCSInputBox(reloadCsInfo())
    outputCs = new OutputArea
    csInfoBox = new VirtuosoCSInfoBox(csBox,instantiate,outputCs)
    examples = new VirtuosoExamplesBox(softReload(),inputBox,descr,csBox)
    uppaal = new RemoteUppaalBox(instantiate,errors)
    verifyta = new RemoteVerifytaBox(instantiate,inputBox,verifytaExpanded,verifytaOut,"")

    inputBox.init(leftColumn,true)
    errors.init(leftColumn)
    descr.init(leftColumn)
    examples.init(leftColumn,true)
    graphics.init(rightColumn,visible = true)
    aut.init(rightColumn,false)
    uppaal.init(rightColumn,false)
    csBox.init(leftColumn,true)
    outputCs.init(leftColumn)
    csInfoBox.init(leftColumn,visible = true)
    infoBox.init(leftColumn,false)
    verifyta.init(leftColumn,true)
    verifytaExpanded.init(leftColumn)
    verifytaOut.init(leftColumn)


    reload()

  }

  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def reload(): Unit = {
    descr.clear()
    softReload()
  }
  private def softReload(): Unit = {
    errors.clear()
    csInfoBox.clear()
    inputBox.update()
    instantiate.update()
    graphics.update()
    aut.update()
    uppaal.update()
    infoBox.update()
    verifytaOut.clear()
    verifytaExpanded.clear()
    verifyta.update()

  }

  private def reloadCsInfo():Unit = {
    csInfoBox.update()
  }

  private def export():Unit = {}
}