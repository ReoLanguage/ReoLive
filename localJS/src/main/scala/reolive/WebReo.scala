package reolive

import java.net.URLDecoder

import common.DomNode
import common.widgets._
import common.widgets.Ifta.{IFTABox, IftaInfoBox, IftaMcrl2Box}
import common.widgets.virtuoso.VirtuosoAutomataBox
import org.scalajs.dom.html
//import org.singlespaced.d3js.d3
import widgets._

import scala.scalajs.js.annotation.JSExportTopLevel


/**
  * Created by jose on 27/04/2017.
  */
object WebReo extends{

  var inputBox: PreoBox = _
  var descr: OutputArea = _
  var typeInfo: TypeBox = _
  var instanceInfo: InstanceBox = _
  var logicBox: LogicBox = _
  var errors: OutputArea = _
  var svg: GraphBox = _
  var svgAut: AutomataBox = _
  var visAut: AutomataVisBox = _
  var hubAut: VirtuosoAutomataBox = _
  var mcrl2Box: Mcrl2Box = _
  var mcrl2IftaBox: IftaMcrl2Box = _
  var outputLogic: OutputArea = _
  var ifta: IFTABox =_
  var iftaInfo:IftaInfoBox = _

  @JSExportTopLevel("reolive_WebReo_main")
  def main(content: html.Div): Unit = {

    //    // add header
    //    d3.select(content).append("div")
    //      .attr("id", "header")
    //      .append("h1").text("Reo Live - Connector Families")

    val contentDiv = DomNode.select(content).append("div") //d3.select(content).append("div")
      .attr("class", "content")

    val rowDiv = contentDiv.append("div")
//      .attr("class", "row")
        .attr("id", "mytable")

    val leftside = rowDiv.append("div")
    //      .attr("class", "col-sm-4")
        .attr("id", "leftbar")
        .attr("class", "leftside")
    leftside.append("div")
        .attr("id","dragbar")
        .attr("class", "middlebar")

    val rightside = rowDiv.append("div")
      //      .attr("class", "col-sm-8")
      .attr("id", "rightbar")
      .attr("class", "rightside")

    // configure defaults
    val search = scalajs.js.Dynamic.global.window.location.search.asInstanceOf[String]
    val args = common.Utils.parseSearchUri(search)
    val conn = args.getOrElse("c", "dupl  ;  fifo * lossy")
    val form = args.getOrElse("f", "<all*> <fifo> true")


    // add InputArea
    descr        = new OutputArea
    errors       = new OutputArea //(id="wr")
    inputBox     = new PreoBox(reload(), export(), conn, errors)
    outputLogic  = new OutputArea //(id="wrLog")
    typeInfo     = new TypeBox(inputBox, errors)
    instanceInfo = new InstanceBox(typeInfo, errors)
    logicBox     = new LogicBox(instanceInfo, form, outputLogic)
    val buttonsDiv = new ButtonsBox(softReload(), List(inputBox,logicBox,descr))
    svg          = new GraphBox(instanceInfo, errors)
    svgAut       = new AutomataBox(instanceInfo, errors)
    visAut       = new AutomataVisBox(instanceInfo, errors)
    hubAut       = new VirtuosoAutomataBox(instanceInfo,errors)
    mcrl2Box     = new Mcrl2Box(instanceInfo,errors)
    mcrl2IftaBox     = new IftaMcrl2Box(instanceInfo,errors)

    ifta = new IFTABox(instanceInfo,errors)
    iftaInfo = new IftaInfoBox(instanceInfo,errors)

    // place items
    inputBox.init(leftside,visible = true)
    errors.init(leftside)
    typeInfo.init(leftside,visible = true)
    instanceInfo.init(leftside,visible = false)
    buttonsDiv.init(leftside,visible = false)
    logicBox.init(leftside,visible = true)
    outputLogic.init(leftside)
    descr.init(leftside)
    iftaInfo.init(leftside, visible = false)

    svg.init(rightside,visible = true)
    svgAut.init(rightside,visible = false)
    visAut.init(rightside,visible = false)
    hubAut.init(rightside,visible = false)
    ifta.init(rightside,visible = false)
    mcrl2Box.init(rightside,visible = false)
    mcrl2IftaBox.init(rightside,visible = false)

    common.Utils.moreInfo(rightside,"https://github.com/ReoLanguage/Preo")

    // default button
    if (args.isEmpty && !buttonsDiv.loadButton("dupl;lossy*fifo")) {
      reload()
    }

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
    outputLogic.clear()
    inputBox.update()
    typeInfo.update()
    instanceInfo.update()

    svg.update()
    svgAut.update()
    visAut.update()
    hubAut.update()
    mcrl2Box.update()
    mcrl2IftaBox.update()

    iftaInfo.update()
    ifta.update()

//    val search = scalajs.js.Dynamic.global.window.location.search.asInstanceOf[String]
//    val args = common.Utils.parseSearchUri(search)
//    errors.message(s"-- $search")
//    errors.message(s"-- $args")
  }

  private def export(): Unit = {
    val loc = scalajs.js.Dynamic.global.window.location
    val ori = loc.origin.toString
    val path = loc.pathname.toString
    val hash = loc.hash.toString
    val search = common.Utils.buildSearchUri(List("c"->inputBox.get,"f"->logicBox.get))

    errors.clear()
    errors.message(ori+path+search+hash)
  }
}
