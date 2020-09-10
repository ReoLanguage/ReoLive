//package common.widgets.arx
// DEPRECATED
//import common.widgets.{Box, CodeBox, OutputArea}
//
///**
//  * Created by guillerminacledou on 30/07/2020
//  */
//
//
//class SBComposerBox(globalReload: =>Unit, default: String, outputBox: OutputArea)
//  extends Box[String]("SB composer", Nil) with CodeBox {
//
//  override protected var input: String = default
//  override protected val boxId: String = "sbcomposer"
//  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
//    List(
//      Right("refresh") -> (() => reload, "Load the stream builder (shift-enter)")
//    )
//
//  override protected val codemirror: String = "dsl"
//  override protected val theme:String = "neat"
//
//  override def reload(): Unit = {
//    update()
//    outputBox.clear()
//    globalReload
//  }
//}
