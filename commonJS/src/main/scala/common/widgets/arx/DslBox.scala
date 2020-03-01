package common.widgets.arx

import common.widgets.{Box, CodeBox, OutputArea}

/**
  * Created by guillecledou on 2019-06-07
  */

class DslBox(globalReload: =>Unit, default: String, outputBox: OutputArea)
  extends Box[String]("ARx program", Nil) with CodeBox {

  override protected var input: String = default
  override protected val boxId: String = "newDsl"
  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
    List(
      Right("refresh") -> (() => reload, "Load the program (shift-enter)")
    )

  override protected val codemirror: String = "dsl"
  override protected val theme:String = "neat"

  override def reload(): Unit = {
    update()
    outputBox.clear()
    globalReload
  }

}