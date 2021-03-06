package common

import java.net.URLDecoder
import java.util.Base64

import common.widgets.Box.Block
import common.widgets.OutputArea
import org.scalajs.dom
import org.scalajs.dom.{EventTarget, XMLHttpRequest, document}
//import org.singlespaced.d3js.Selection

object Utils {

  /**
    * Parses the "search" attribute of a uri, e.g., "?q=abc&p=2"
    * @param str text to be parsed
    * @return mapping from parameter names to their values
    */
  def parseSearchUri(str: String): Map[String,String] = {
    var res = Map[String,String]()
    if (str.startsWith("?")) {
      for (pair <- str.tail.split("&"))
        pair.split("=",2) match {
          case Array(name, value) =>
            val v2 = value.replaceAll("%BE","&")
            res += name ->  URLDecoder.decode(v2,"UTF8")
          case _ =>
        }
    }
    res
  }

  /**
    * Builds the URL part defining a given set of attributes.
    * E.g., from Map("a"->"b") builds "?a=b"
    */
  def buildSearchUri(keys:Iterable[(String,String)]): String = {
    if (keys.isEmpty) ""
    else "?"+keys.map(p=>s"${p._1}=${p._2
        .replaceAll("\n","%0A")
        .replaceAll(" ","%20")
        .replaceAll("&","%BE")
      }").mkString("&")
  }

  def download(content:String,fileName:String,errorBox:OutputArea): Unit = {
    val enc = Base64.getEncoder.encode(content.getBytes()).map(_.toChar).mkString
    val filename = fileName
    val url = "data:application/octet-stream;charset=utf-16le;base64," + enc

    //
    val x = new XMLHttpRequest()
    x.open("GET", url, true)
    x.onload = e => {
      if (x.status == 200) {
        scalajs.js.eval(
          s"""
            let a = document.createElement("a");
            a.style = "display: none";
            document.body.appendChild(a);
            a.href = "$url";
            a.download="$filename";
            a.text = "hidden link";
            //programatically click the link to trigger the download
            a.click();
            //release the reference to the file by revoking the Object URL
            window.URL.revokeObjectURL("$url");
          """
        )
      }
      else if (x.status == 404) {
        errorBox.error(x.responseText)
      }
    }
    x.send()
  }

  def codemirror(textAreaId:String,modeType:String,lineNum:Boolean=false,readOnly:Boolean=true,cursorBlick:Int=(-1)):Unit = {
    val codemirrorJS = scalajs.js.Dynamic.global.CodeMirror

    val lit = scalajs.js.Dynamic.literal(
      lineNumbers = lineNum, matchBrackets = true, lineWrapping = true,
      readOnly = readOnly, theme = "neat", cursorBlinkRate = cursorBlick, mode=modeType)
    codemirrorJS.fromTextArea(dom.document.getElementById(textAreaId),lit)
  }


 def moreInfo(block: DomNode,ref: String): Unit = {
//   val more2 = document.createElement("div")
//   more2.setAttribute("class","panel-group")
//   val p2 = document.createElement("p")
//   more2.appendChild(p2)
//   block.appendChild(more2)
//   p2.setAttribute("style","...")
//
     ////////
    val more = block.append("div")
        .attr("class","panel-group")
        .append("p")
    more
      .style("font-size","larger")
      .style("text-align","center")
      .style("padding-top","18px")
    more
      .text("More information on the project: ")
      .append("a")
        .attr("href",ref)
        .attr("target","#")
        .text(ref)
 }

  def temporaryInfo(block: DomNode,title:String,ref: String): Unit = {
    val more = block.append("div")
      .attr("class","panel-group")
      .append("p")
    more
      .style("font-size","larger")
      .style("text-align","center")
      .style("padding-top","0px")
    more
      .text(title)
      .append("a")
      .attr("href",ref)
      .attr("target","#")
      .text(ref)
  }

  /**
    * Opens a url in the browser
    * @param url the url to open
    */
  def goto(url:String):Unit = {
    org.scalajs.dom.window.open(url)
  }

  def downloadSvg(element:String): Unit = {
    scalajs.js.eval(
      s"""svgEl = document.getElementById("$element");
        |name = "circuit.svg";
        |
        |svgEl.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        |var svgData = svgEl.outerHTML;
        |
        |// Firefox, Safari root NS issue fix
        |svgData = svgData.replace('xlink=', 'xmlns:xlink=');
        |// Safari xlink NS issue fix
        |//svgData = svgData.replace(/NS\\d+:href/gi, 'xlink:href');
        |svgData = svgData.replace(/NS\\d+:href/gi, 'href');
        |// drop "stroke-dasharray: 1px, 0px;"
        |svgData = svgData.replace(/stroke-dasharray: 1px, 0px;/gi, '');
        |
        |var preface = '<?xml version="1.0" standalone="no"?>\\r\\n';
        |var svgBlob = new Blob([preface, svgData], {type:"image/svg+xml;charset=utf-8"});
        |var svgUrl = URL.createObjectURL(svgBlob);
        |var downloadLink = document.createElement("a");
        |downloadLink.href = svgUrl;
        |downloadLink.download = name;
        |document.body.appendChild(downloadLink);
        |downloadLink.click();
        |document.body.removeChild(downloadLink);
      """.stripMargin)
  }
}
