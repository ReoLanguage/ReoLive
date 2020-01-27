package common.widgets.newdsl

import common.widgets.Setable
import org.scalajs.dom
import org.singlespaced.d3js.Selection

/**
  * Created by guillecledou on 2020-01-27
  */


class DslLibOutputArea  extends Setable[String] {
  type Block = Selection[dom.EventTarget]

  var output: Block = _

  def init(div: Block): Unit = output = div.append("div").attr("class","alertContainer")

  def clear(): Unit = output.text("")

  /**
    * sets the value of a given widget, e.g., content text.
    *
    * @param value
    */
  override def setValue(value: String): Unit = {
    clear()
    var out = output
      .append("div")
      .attr("class", "dsllib-show panel panel-default")

    //header
    out.append("div")
      .attr("class","panel-heading my-panel-heading")
      .append("h5").text("Library Information")
      .style("margin","0px")

    // body
    var body = out.append("div")
      .attr("class","panel-body")
      .style("padding","0px")

//    body.append("h5").attr("class","card-title")

    var form = body.append("textarea")
      .style("white-space","pre-wrap")
      .attr("id", "library")
      .attr("class", "library-show").text(value)

    common.Utils.codemirror("library", "text/x-dsl",lineNum = true)


//    <div class="card bg-light mb-3" style="max-width: 18rem;">
    //  <div class="card-header">Header</div>
    //  <div class="card-body">
    //    <h5 class="card-title">Light card title</h5>
    //    <p class="card-text">Some quick example text to build on the card title and make up the bulk of the card's content.</p>
    //  </div>
    //</div>
//    output.text(value)
  }
}
