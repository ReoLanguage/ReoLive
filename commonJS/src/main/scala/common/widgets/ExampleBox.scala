package common.widgets

/**
  * Created by guillecledou on 03/11/2020
  */


abstract class ExampleBox(title:String, reload: => Unit, toSet: List[Setable[String]])
  extends Box[Unit](title, Nil) {

  protected val buttons: Seq[List[String]]

  override def get: Unit = ()

  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div     Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div: Block, visible: Boolean): Unit = {
    val buttonsDiv = super.panelBox(div,visible).append("div")
      .attr("id", "buttons")
      .attr("style","padding: 2pt;")

    buttonsDiv
      .style("display:block; padding:2pt")

    for (ops <- buttons ) yield genButton(ops,buttonsDiv)
  }


  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
  override def update(): Unit = ()

  protected def genButton(ss:List[String],buttonsDiv:Block): Unit = {
    ss match {
      case hd::tl =>
        val button = buttonsDiv.append("button")
          .textEl(hd)

        button.on("click", () => {
          toSet.zip(tl).foreach(pair => pair._1.setValue(pair._2))
          toSet.drop(tl.size).foreach(_.setValue(""))
          reload
        })
      case Nil =>
    }
  }

  /** Applies a button, if it exists.
    * @param button name of the button to be applied
    */
  def loadButton(button:String): Boolean = {
    buttons.find(l=>l.headOption.getOrElse(false) == button) match {
      case Some(_::fields) =>
        toSet.zip(fields).foreach(pair => pair._1.setValue(pair._2))
        toSet.drop(fields.size).foreach(_.setValue(""))
        reload
        true
      case _ => false
    }
  }

}
