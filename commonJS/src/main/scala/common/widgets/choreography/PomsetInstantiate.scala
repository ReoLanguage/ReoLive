package common.widgets.choreography

import common.widgets.{Box, OutputArea}

import choreo.choreo2.syntax._
import choreo.choreo2.syntax.Choreo
import choreo.choreo2.syntax.Choreo._
import choreo.choreo2.analysis.pomsets.Pomset
import choreo.choreo2.analysis.pomsets.Pomset._
import choreo.choreo2.DSL

/**
 * Created by guillecledou on 12/02/2021
 */

class PomsetInstantiate(choreographyBox: Box[Choreo], errorBox: OutputArea)
  extends Box[Pomset]("Interpreted Pomset", List(choreographyBox)) {

  protected var pomset:Pomset = Pomset.identity

  override def get:Pomset = pomset
  override def init(div: Block, visible: Boolean=false): Unit = ()
  override def update():Unit =
    try {
      pomset = DSL.pomset(choreographyBox.get)
    } catch Box.checkExceptions(errorBox)
}