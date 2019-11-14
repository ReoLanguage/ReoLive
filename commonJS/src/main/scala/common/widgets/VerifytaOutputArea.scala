package common.widgets

import hub.analyse.TemporalFormula
import hub.backend.{Show, Uppaal, Verifyta, VerifytaCall}
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

  def setResults(results:List[(TemporalFormula,VerifytaCall,Option[Either[String,List[String]]])]):Unit = {
    // initialize output
    var out = output
      .append("div").attr("class", "verifyta-result")
      .append("ul").attr("class", "list-group list-group-flush mb-3")

    for ( ((tf,call,errOrRes),i) <- results.zipWithIndex) {

      var (satisfied,ufSatisfied) = if (errOrRes.isDefined) errOrRes.get match {
        case Right(res) => (Verifyta.isSatisfiedVerifyta(res),Right(res.map(Verifyta.isSatisfiedVerifyta)))
        case Left(err) => (false,Left(err))
      } else (false,Right(List()))

      var li = out.append("li")
        .attr("class", "list-group-item lh-condensed")

      var div = li.append("div")
        .style("display", "flex")
        .style("justify-content", "space-between")

      var form = div.append("textarea")
        .style("white-space","pre-wrap")
        .attr("id", "formula" + i)
        .attr("class", "formula-result").text(Show(tf))

      var extras = div.append("span")
      extras.style("text-align","right")

      if (errOrRes.isDefined) {
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

      val dwn = extras.append("a")
        .style("margin", "5px")
        .attr("title", "Download Uppaal model used to verify this property")
        .attr("id", "model" + i) //.text("m")
      Box.downloadSvg(dwn)

      var uppaalforms = li.append("div")
        .attr("class", "collapse")
        .attr("id", "collapseFormula" + i)
        .append("div")
        .attr("id", "expandedFormula" + i)
        .attr("class", "temporal-formula text-muted")

      call.uf.zip(1 to call.uf.size).foreach(f => {
        var uppaalform = uppaalforms.append("div")
          .style("display", "flex")
          .style("justify-content", "space-between")

        uppaalform.append("span")
          .text(f._2 + ". " + Show(f._1))

        if (errOrRes.isDefined && ufSatisfied.isRight) {
          uppaalform.append("span")
            .style("color", if (ufSatisfied.right.get(f._2-1)) "#008900" else "#972f65")
            .style("margin", "5px")
            .text(
                if (ufSatisfied.right.get(f._2-1)) "✓" else "✗"
            )}
        else if (errOrRes.isDefined && ufSatisfied.isLeft) {
          uppaalform.append("span")
            .style("color", "#972f65")
            .style("margin", "5px")
            .text("error")
          uppaalforms.append("span").text(ufSatisfied.left.get)
        }
      })

      common.Utils.codemirror("formula" + i, "text/x-temporal")
      //Utils.codemirror("expandedFormula"+i,"text/x-temporal")

      var model = dom.document.getElementById("model" + i).asInstanceOf[html.Element]
        .onclick = { e: MouseEvent => common.Utils.download(Uppaal(call.um,call.uf), s"uppaalModel${i}.xml",errorBox) }
    }
}

  def clear(): Unit = output.text("")


}
