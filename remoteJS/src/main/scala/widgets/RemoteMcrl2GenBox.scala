package widgets

import common.widgets.{Box, OutputArea}
import org.scalajs.dom
import dom.{EventTarget, MouseEvent, html}
import org.scalajs.dom.raw.XMLHttpRequest

import java.io.File

/** Runs mCRL2 under the hood to verify properties. Two ways to use it:
  *  - by the `update` method, triggering the `getSpecAndProps` argument
  *  - by the `runMcrl2`, using instead the specifications in attachment
  *  */
class RemoteMcrl2GenBox(specBox:Box[(String,List[(String,String)])],
                        header:String = "mCRL2 Verification of Safety Requirements",
                        callback: (()=>Unit) = (()=>{}),
                        errorBox: OutputArea)
  extends Box[List[(String,(String,String))]](header, Nil){

  private val id = 0;
  private var box: Block = _
  private var mermaid = ""
  private var propNames: List[String] = Nil
  private var results: List[(String,(String,String))] = Nil

  override def get: List[(String,(String,String))] = results //: String = mermaid

  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible, List("padding-right" -> "90pt"),
        buttons = List(
//          Right("download") -> ((() => download(s"/model/$id"), "Download mCRL2 specification"))
  //        , Left("LPS") -> (() => download(s"/lps/$id"), "Download mCRL2 lps specification")
  //        , Left("LTS") -> (() => download(s"/lts/$id"), "Download mCRL2 lts specification")
        ))
      .append("div")
      .attr("id", "mcrl2GenBox")
      .style("white-space", "pre-wrap")


    dom.document.getElementById(title).firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { e: MouseEvent => if (!isVisible) checkMcrl2() else box.html("") }
  }

  /** Generic function ot download an URL via a dummy html reference that is clicked. */
//  private def download(url: String): Unit = {
//    val x = new XMLHttpRequest()
//    x.open("GET", url, async = true)
//    x.onload = e => {
//      if (x.status == 200) {
//        scalajs.js.eval(
//          s"""
//            let a = document.createElement("a");
//            a.style = "display: none";
//            document.body.appendChild(a);
//            a.href = "$url";
//            //programatically click the link to trigger the download
//            a.click();
//            //release the reference to the file by revoking the Object URL
//            window.URL.revokeObjectURL("$url");
//          """
//        )
//      }
//      else if (x.status == 404) {
//        errorBox.error(x.responseText)
//      }
//    }
//    x.send()
//  }

  /** When asked to update, if visible, ask to check property */
  override def update(): Unit = if(isVisible) checkMcrl2()
  def forceUpdate(): Unit = checkMcrl2()


  def runMcrl2(spec:String, formulas:List[(String,String)]): Unit = {
    if (spec=="") {
      box.html("")
      box.append("p").text("Empty specification")
      callback()
    }
    else {
      val (_propNames, propDefs) = formulas.unzip
      propNames = _propNames
      RemoteBox.remoteCall("checkMcrl2", spec + "§§§§" + propDefs.mkString("§§§"), processMcrl2Result)
    }
  }

  private def checkMcrl2(): Unit = {
    try {
      deleteMcrl2()
      results = Nil
      val (specs,props) = specBox.get
      if (specs!="") runMcrl2(specs,props)
//      RemoteBox.remoteCall("checkMcrl2", specs+"§§§§"+props.mkString("§§§"), processMcrl2Result)
      //      box.html(model.toString)
    } catch {
      case e: Throwable =>
        errorBox.error(e.getMessage)
    }
  }

  private def deleteMcrl2(): Unit = {
    box.html("")
    box.append("p").text("Waiting for mCRL2 to run at the server.")
  }



  private def processMcrl2Result(res: String): Unit = {
//    val solutions: Set[Set[String]] = DSL.parseProducts(data)
//    box.html(model.show(solutions))
    try {
      box.html("")
      var index:Int = 1
      val pairs = res.split("§§§§")
      results = for ((pair,name) <- pairs.toList.zip(propNames)) yield {
        val List(solved,mermaid) = pair.split("§§§").toList
        box.append("p").text(s"$name: $solved")
        index += 1
        solved -> (name -> mermaid)
      }
    }
    catch {
      case e: Throwable => box.append("p").text(s"Failed: $res")
    }

//    mermaid = res.dropWhile(_!='\n')
//    val valid = res.takeWhile(_!='\n')
//    results = List(valid -> mermaid)
//
//    box.html("")
//    box.append("p").text(res)

    callback()
  }




}
