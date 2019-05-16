package widgets

import common.widgets.{Box, OutputArea}
import ifta.{DSL, FExp}
import ifta.analyse.mcrl2.IftaModel
import ifta.backend.Show
import org.sat4j.specs.TimeoutException
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import org.scalajs.dom.raw.XMLHttpRequest
import preo.ast.CoreConnector
import preo.frontend.mcrl2.Model

/**
  * Created by guillecledou on 2019-05-13
  */


class RemoteMcrl2IftaBox(connector: Box[CoreConnector], errorBox: OutputArea)
  extends Box[Model]("mCRL2 of the IFTA instance", List(connector)){

  var id: Long = 0
  private var box: Block = _
  private var model: IftaModel = _

  override def get: Model = model

  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible, List("padding-right"->"90pt"),
      buttons=List(Left("&dArr;")-> (()=>download(s"/model/$id"),"Download mCRL2 specification")
        ,Left("LPS")   -> (()=>download(s"/lps/$id"),"Download mCRL2 lps specification")
        ,Left("LTS")   -> (()=>download(s"/lts/$id"),"Download mCRL2 lts specification")
        //                  ,Left("MA")   -> (()=> debugNames)
      ))
      .append("div")
      .attr("id", "mcrl2iftaBox")
      .style("white-space","pre-wrap")


    dom.document.getElementById("mCRL2 of the IFTA instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { e: MouseEvent => if (!isVisible) produceMcrl2() else deleteMcrl2()}
  }

  //  private def debugNames(): Unit = {
  //    errorBox.clear()
  //    errorBox.warning(model.getMultiActionsMap
  //      .map(kv => kv._1+":"+kv._2.map("\n - "+_).mkString(""))
  //      .mkString("\n"))
  //  }

  private def download(url: String): Unit = {
    val x = new XMLHttpRequest()
    x.open("GET", url, async = true)
    x.onload = e => {
      if(x.status == 200){
        scalajs.js.eval(
          s"""
            let a = document.createElement("a");
            a.style = "display: none";
            document.body.appendChild(a);
            a.href = "$url";
            //programatically click the link to trigger the download
            a.click();
            //release the reference to the file by revoking the Object URL
            window.URL.revokeObjectURL("$url");
          """
        )
      }
      else if(x.status == 404){
        errorBox.error(x.responseText)
      }
    }
    x.send()

  }
  override def update(): Unit = if(isVisible) produceMcrl2()

  private def produceMcrl2(): Unit = {
    try {
      deleteMcrl2()
      model = Model[IftaModel](connector.get)
      var fmInfo =
        s"""{ "fm":     "${model.fm}", """ +
        s"""  "feats":  "${model.feats.mkString("(",",",")")}" }"""

      RemoteBox.remoteCall("ifta", fmInfo, produceMcrl2WithSols)
      //      box.html(model.toString)
    } catch {
      case e:Throwable =>
        errorBox.error(e.getMessage)
    }
  }

  private def deleteMcrl2(): Unit = {
    box.html("")
  }

  private def produceMcrl2WithSols(data:String):Unit ={
    val solutions:Set[Set[String]] = DSL.parseProducts(data)
    box.html(model.show(solutions))
  }


}

