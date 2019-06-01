package widgets.Treo

import common.widgets.{Box, OutputArea}
import widgets.RemoteBox

class TreoResultBox(program: Box[String], errorBox: OutputArea)
    extends Box[Unit]("Treo result", List(program)) {
  var box : Block = _

  override def get: Unit = {}

//  private val widthCircRatio = 7
//  private val heightCircRatio = 3
//  private val densityCirc = 0.5 // nodes per 100x100 px


  override def init(div: Block, visible: Boolean): Unit = {
    box = super.panelBox(div,visible,
      buttons = List(
//        Right("glyphicon glyphicon-refresh")-> (()=>update(),"Reload Treo program (shift-enter)")
//        Left("&dArr;")-> (() => saveSvg(),"Download image as SVG")
      ))
      .append("div")
      .attr("id", "treoRes")
      .style("white-space","pre-wrap")

//    traj = Trajectory.hprogToTraj(Map(),dependency.get)._1

    //    dom.document.getElementById("Circuit of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
//      .onclick = {e: MouseEvent => if(!isVisible) drawGraph() else deleteDrawing()}
  }

//  override def update(): Unit = if(isVisible) {
//    deleteDrawing()
//    drawGraph()
//  }

  override def update(): Unit = {
    box.html("")
    errorBox.message("Waiting for Treo...")
    RemoteBox.remoteCall("treoWS",program.get,reload)
  }

  def reload(s:String) = {
    errorBox.clear()
    if (s startsWith "Error")
      errorBox.error(s)
    else
      box.html(s)
//    result.clear()
//    result.message(s)
  }


}


