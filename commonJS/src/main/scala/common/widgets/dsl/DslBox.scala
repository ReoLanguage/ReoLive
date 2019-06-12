package common.widgets.dsl

import common.widgets.{Box, CodeBox, OutputArea}

/**
  * Created by guillecledou on 2019-06-07
  */


class DslBox(globalReload: =>Unit, default: String, outputBox: OutputArea)
  extends Box[String]("New DSL Syntax", Nil) with CodeBox {

  override protected var input: String = default
  override protected val boxId: String = "newDsl"
  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
    List(
      Right("glyphicon glyphicon-refresh") -> (() => reload, "Load the program (shift-enter)")
    )

  override protected val codemirror: String = "newDsl"

  override def reload(): Unit = {
    update()
    outputBox.clear()
    globalReload
  }

}