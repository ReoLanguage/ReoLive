package widgets.feta

import common.widgets.{Box, CodeBox, OutputArea}

/**
 * Created by guillecledou on 21/04/2021
 */

class FetaBox(globalReload: =>Unit, default: String, outputBox: OutputArea)
  extends Box[String]("(F)ETA Specification", Nil) with CodeBox {

  override protected var input: String = default
  override protected val boxId: String = "fetaBox"
  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
    List(
      Right("refresh") -> (() => reload(), "Load the specification (shift-enter)")
    )

  override protected val codemirror: String = "feta"
  override protected val theme:String = "neat"

  override def reload(): Unit = {
    update()
    outputBox.clear()
    globalReload
  }

}
