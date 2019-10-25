package common.widgets

import org.scalajs.dom
import org.scalajs.dom.html
import org.singlespaced.d3js.Selection
import org.scalajs.dom.{MouseEvent, XMLHttpRequest, html}

/**
  * Created by guillecledou on 2019-10-24
  */


class VerifytaOutputArea(errorBox:OutputArea) {

  type Block = Selection[dom.EventTarget]

  var output: Block = _

  def init(div: Block): Unit = output = div.append("div").attr("class","alertContainer")

  def setResults(results:List[((String,String,String),Option[Boolean])]):Unit = {
    // initialize output
    var out = output
      .append("div").attr("class", "verifyta-result")
      .append("ul").attr("class", "list-group list-group-flush mb-3")

    for ( (((o,uf,um),ok),i) <- results.zipWithIndex) {
      //var satisfied = res.replace("\\u001B"," ").stripMargin.endsWith("Formula is satisfied.")
      var satisfied = if (ok.isDefined) ok.get else false

      var li = out.append("li")
        .attr("class", "list-group-item lh-condensed")

      var div = li.append("div")
        .style("display", "flex")
        .style("justify-content", "space-between")

      var form = div.append("textarea")
        .attr("id", "formula" + i)
        .attr("class", "formula-result").text(o)

      var extras = div.append("span")

      if (ok.isDefined) {
        extras.append("span")
          .style("color", if (satisfied) "#008900" else "#972f65")
          .style("margin", "5px")
          .text(if (satisfied) "✓" else "✗")
      }
      extras.append("a")
        .style("margin", "5px")
        .attr("title", "Show expanded formula")
        .attr("data-toggle", "collapse")
        .attr("data-target", "#collapseFormula" + i)
        .attr("class", "expand-formula").text("+")
      extras.append("a")
        .style("margin", "5px")
        .attr("title", "Download Uppaal model used to verify this property")
        .attr("id", "model" + i).text("m")


      li.append("div")
        .attr("class", "collapse")
        .attr("id", "collapseFormula" + i)
        .append("span") //textearea
        .attr("id", "expandedFormula" + i)
        .attr("class", "temporal-formula text-muted")
        //        .style("height","10px")
        .text(uf)

      common.Utils.codemirror("formula" + i, "text/x-temporal")
      //Utils.codemirror("expandedFormula"+i,"text/x-temporal")

      var model = dom.document.getElementById("model" + i).asInstanceOf[html.Element]
        .onclick = { e: MouseEvent => common.Utils.download(um, s"uppaalModel${i}.xml",errorBox) }

    }
  }

  def clear(): Unit = output.text("")


}
