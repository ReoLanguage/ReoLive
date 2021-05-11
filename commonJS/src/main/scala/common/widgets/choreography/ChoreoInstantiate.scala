package common.widgets.choreography

import common.widgets.{Box, OutputArea}
import choreo.choreo2.syntax._
import choreo.choreo2.syntax.Choreo
import choreo.choreo2.syntax.Choreo._
import choreo.choreo2.DSL
/**
 * Created by guillecledou on 12/02/2021
 */

class ChoreoInstantiate(choreocode: Box[String], errorBox: OutputArea)
  extends Box[Choreo]("Parsed Choreography", List(choreocode)) {

  protected var choreography:Choreo = End

  override def get:Choreo = choreography
  override def init(div: Block, visible: Boolean=false): Unit = ()
  override def update():Unit =
    try {
      choreography = DSL.parse(choreocode.get)
    } catch Box.checkExceptions(errorBox)
}
