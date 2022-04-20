package common.widgets.virtuoso

import common.widgets.{Box, CodeBox, OutputArea}

class VirtuosoBox(globalReload: =>Unit, default: String, outputBox: OutputArea)
  extends Box[String]("Hub Composer", Nil) with CodeBox {

    override protected var input: String = default
    override protected val boxId: String = "virtuosoInputArea"
    override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
      List(
        Right("help") -> (()=>
          common.Utils.goto("https://hubs.readthedocs.io/en/latest/tutorial.html#hub-composer"),
          "See documentation for this widget"),
        Right("refresh") -> (() => reload(), "Load the Hub (shift-enter)")
      )

    override protected val codemirror: String = "virtuoso"

    override def reload(): Unit = {
      update()
      outputBox.clear()
      globalReload
    }

}
