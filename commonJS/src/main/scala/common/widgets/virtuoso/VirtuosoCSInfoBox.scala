package common.widgets.virtuoso

import common.widgets.{Box, OutputArea, Setable}
import hub.analyse.ContextSwitch
import hub.{DSL, Guard, HubAutomata, Update}
import org.scalajs.dom
import org.scalajs.dom.html
import preo.ast.CoreConnector
import preo.backend.{Automata, Network}

/**
  * Created by guillecledou on 07/03/2019
  */


class VirtuosoCSInfoBox(connector:Box[CoreConnector], errorBox: OutputArea)
  extends Box[String]("Context Switch Analysis", List(connector)) with Setable[String] {

  var pattern: String = ""
  var csInfo: Block = _
  var patternInput:html.TextArea = _
  var info:Block =_

  var boxID = title+"_id"

  override def get: String = pattern

  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div     Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div: Block, visible: Boolean): Unit = {
//    csInfo = panelBox(div, visible).append("div")
//      .attr("id", "csInfoBox")
    csInfo = panelBox(div,visible,
      buttons = List(
        Right("refresh") -> (() => update(), "Estimate minimum number of context switches for a sequence of actions (Shift-enter)")
      ))
      //.append("div")
      //.attr("id", "csInfoBox")

    toggleVisibility(visible = ()=>{update()})

    val output = csInfo.append("div")
      .attr("id", "csInfoBox")
      info = csInfo.append("div")
      .attr("id","csInfoOut")

    output.append("div")
      .attr("style","color: blue; display: inline; vertical-align: top; line-height: 20pt;")
      .text("Pattern: ")
    val inputArea = output.append("textarea")
      .attr("id", boxID)
      .attr("name", boxID)
      .attr("class","my-textarea prettyprint lang-java")
      .attr("rows", "3")
      .attr("style", "width: 75%; ")
      //      .attr("placeholder", input)
      .text(pattern)

    patternInput = dom.document.getElementById(boxID).asInstanceOf[html.TextArea]

    patternInput.onkeydown = {e: dom.KeyboardEvent =>
      if(e.keyCode == 13 && e.shiftKey){e.preventDefault(); update()}
      else ()
    }
  }

  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
  override def update(): Unit = {
      if (info != null) {
        clear()
        try {
//          patternInput = dom.document.getElementById(boxID).asInstanceOf[html.TextArea]
          //var pattern = DSL.parsePattern(dependency.get)
          if (patternInput.value != "") {
            var pattern = DSL.parsePattern(patternInput.value)
            var aut = Automata[HubAutomata](connector.get).serialize.simplify
            val (found, trace, cs) = ContextSwitch(aut, pattern)
            if (!found)
              errorBox.message(s"""Pattern not found""".stripMargin)
            else {
              // CS
              info.append("p")
                .append("strong")
                .text(s"Context Switches: ${cs} cs (minimum)")

              var listt = info.append("ul")
                .attr("style", "margin-bottom: 20pt;")
              //              .append("li")
              //                .text(s"${trace.size} transition(s) involved \n")
              trace.foreach(t =>
                listt.append("li")
                  .text(s"${t._1} --> ${t._2._1} by ${t._2._2.map(p => aut.getPortName(p)).mkString(",")}: ${t._2._2.size * 2} cs ")
              )

              // Pattern used
              info.append("p")
                .append("strong")
                .text(s"Pattern used:")

              info.append("ul")
                .attr("style", "margin-bottom: 20pt;")
                .append("li")
                .text(s"${pattern.mkString(",")} \n")
            }
          }
      } catch Box.checkExceptions(errorBox)
    }
  }

  def clear():Unit = {
    errorBox.clear()
    //csInfo.text("")
    info.text("")
  }

  /**
    * sets the value of a given widget, e.g., content text.
    *
    * @param value
    */
  override def setValue(value: String): Unit = {
    patternInput.value = value
  }
}
