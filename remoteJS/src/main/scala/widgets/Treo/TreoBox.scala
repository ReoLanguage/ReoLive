package widgets.Treo

import common.widgets.{Box, CodeBox, OutputArea}
import widgets.RemoteBox

class TreoBox(globalReload: =>Unit, default: String, outputBox: OutputArea)
  extends Box[String]("Treo Program", Nil) with CodeBox {

  override protected var input: String = default
  override protected val boxId: String = "treoInputArea"
  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
    List(
      Right("refresh") -> (() => reload, "Load the Treo program (shift-enter)")
//      Left("MA") -> (() => debugNames, "Map actions in the formula to sets of actions in the mCRL2 specification")
    )

  override protected val codemirror: String = "treo"

  override def reload(): Unit = {
    update()
    outputBox.clear()
    globalReload
  }

}
