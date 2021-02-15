package common.widgets.choreography

import common.widgets.{Box, CodeBox, OutputArea}

/**
  * Created by guillecledou on 02/11/2020
  */


class ChoreoBox(globalReload: =>Unit, default: String, outputBox: OutputArea)
  extends Box[String]("Choreography", Nil) with CodeBox {

    override protected var input: String = default
    override protected val boxId: String = "choreoBox"
    override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
      List(
        Right("refresh") -> (() => reload(), "Load the choreography (shift-enter)")
      )

    override protected val codemirror: String = "choreo"
    override protected val theme:String = "neat"

    override def reload(): Unit = {
      update()
      outputBox.clear()
      globalReload
    }

}
