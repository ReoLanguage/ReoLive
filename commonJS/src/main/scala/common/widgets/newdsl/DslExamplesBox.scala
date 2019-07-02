package common.widgets.newdsl

import common.widgets.{Box, Setable}
import org.scalajs.dom.EventTarget

import scala.scalajs.js.UndefOr

/**
  * Created by guillecledou on 2019-06-12
  */


class DslExamplesBox(reload: => Unit, toSet: List[Setable[String]]) extends Box[Unit]("Dsl Examples", Nil) {
  override def get: Unit = Unit

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

  override def update: Unit = ()

  private def genButton(ss:List[String],buttonsDiv:Block): Unit = {
    ss match {
      case hd::tl =>
        val button = buttonsDiv.append("button")
          .text(hd)

        button.on("click",{(e: EventTarget, a: Int, b:UndefOr[Int])=> {
          toSet.zip(tl).foreach(pair => pair._1.setValue(pair._2))
          toSet.drop(tl.size).foreach(_.setValue(""))
          reload
        }} : button.DatumFunction[Unit])
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

  /**
    * Each buttom is a list of strings: the first is the button name,
    * and each of the remaining are passed to the `toSet` objects by the same order.
    *
    */
  protected val buttons: Seq[List[String]] = Seq(
    "Various types"::
      """data List<a> = Nil | Cons(a,List<a>)
        |data Bool = True | False
        |data Nat = Zero | Succ(Nat)
        |data Pair<a,b> = P(a,b)
        |data Either<a,b> = Left(a) | Right(b)
        |data Unit = U
        |
        |x = Cons(Zero,Nil)
        |y = Cons(Zero,x)
        |z = Cons(Succ(Succ(Zero)),y)
        |w = True""".stripMargin::""::Nil,
    "Nil case 1"::
      """data List<a> = Nil | Cons(a,List<a>)
        |data Bool = True | False
        |
        |x = Nil
        |y = Nil
        |z = Cons(True,x)""".stripMargin::
      """<strong>x</strong> wil be forced to be of type <strong> List&lt;Bool&gt; </strong>
        |<strong>y</strong> will be of type <strong> List&lt;a&gt; </strong>
      """.stripMargin::Nil,
    "Nil case 2"::
      """data List<a> = Nil | Cons(a,List<a>)
        |data Bool = True | False
        |data Nat = Zero | Succ(Nat)
        |
        |x = Nil
        |z = Cons(True,x)
        |w = Cons(Zero,x)""".stripMargin::
      """fails because <strong>x</strong> was consider of type <strong>List&lt;Bool&gt; first</strong>""".stripMargin::Nil,
    "Conn Def"::
      """data List<a> = Nil | Cons(a,List<a>)
        |data Bool = True | False
        |data Nat = Zero | Succ(Nat)
        |
        |def conn = {
        |	dupl;fifo*lossy
        |}
        |
        |def alt = {
        |	alt {
        | alt(i1?,i2?,o!) =
        |   in1(i1,a) in2(i2,b) drain(a, b)
        |   sync(a, c) fifo(b, c) out(c,o)
        |  }
        |}
        |
        |x = True
        |y,z = conn(Nil)
        |w = alt(Nil,x)
        |
        |//
        |// Some discussed ideas
        |//
        |// 1) where o1 and o2 serve as variables,
        |// don't need to be specified before
        |// if they are actual params for outputs:
        |// z = conn(x,o1,o2)
        |// 2) (NO) unspecified could mean some kind of Unit type -
        |// no data is sent or don't care about data:
        |// z = conn
        |// 3) outputs are declared on the lhs,
        |// inputs on the rhs
        |// (o1,o2) = conn(x)""".stripMargin::
      """Ideas for connectors expressions""".stripMargin::Nil,
    "Conn Infer"::
      """data List<a> = Nil | Cons(a,List<a>)
        |data Bool = True | False
        |data Nat = Zero | Succ(Nat)
        |
        |def conn = {
        |	dupl;fifo*lossy
        |}
        |
        |def alt = {
        |	alt {
        | alt(i1?,i2?,o!) =
        |   in1(i1,a) in2(i2,b) drain(a, b)
        |   sync(a, c) fifo(b, c) out(c,o)
        |  }
        |}
        |
        |def noOutput = {
        |	(\x.fifo^x) ;
        |    (\n.drain^n)
        |}
        |
        |def closed = {
        |	writer^8 ; merger! ;
        |	merger! ; reader!
        |}
        |
        |true = True
        |zero = Zero
        |nil = Nil
        |y,o = conn(true)
        |w = alt(true,zero)""".stripMargin::
      """Checking some connectors expression""".stripMargin::Nil,
    "Multi Assign"::
      """data Bool = True | False
        |data Nat = Zero | Succ(Nat)
        |
        |def conn = {
        |	dupl;fifo*lossy
        |}
        |
        |y,o = conn(True)
        |o1,o2 = conn(True)
        |o3,o4 = conn(Zero)
        |z,p = conn(Zero,out1,out2)""".stripMargin::
      """Exmple of multiple assignment""".stripMargin::Nil
//    "Virtuoso Data"::
//      """
//        |data Unit = U
//        |data Nat = Zero | Succ(Nat)
//        |// for virtuoso a package has a task id and some data
//        |// data Package = Pkg(Nat,a) // a should be changed to a predefined Any where all types
//        |
//        |pkg1 = Pkg(Zero,U)
//        |pkg2 = Pkg(Succ(Zero),Zero)
//        |
//        |// example1
//        |//con = writer(pkg1)*writer(pkg2);alt;reader(x)
//        |// type of con and x ?
//        |
//      """.stripMargin::"" ::Nil
  )
}
