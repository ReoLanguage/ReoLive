package common.widgets.newdsl

import common.widgets.{Box, Setable}
import org.scalajs.dom.EventTarget

import scala.scalajs.js.UndefOr

/**
  * Created by guillerminacledou on 2020-01-27
  */


class DslLibBox(reload: => Unit, toSet: List[Setable[String]]) extends Box[Unit]("DSL Library", Nil) {
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

  override def update(): Unit = ()

  private def genButton(ss:List[String],buttonsDiv:Block): Unit = {
    ss match {
      case hd::tl =>
        val button = buttonsDiv.append("button")
          .text(hd)

        button.on("click",{(_: EventTarget, _: Int, _:UndefOr[Int])=> {
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
    "Types"::
      """data List<a> = Nil | Cons(a,List<a>)
        |data Bool = True | False
        |data Nat = Zero | Succ(Nat)
        |data Pair<a,b> = P(a,b)
        |data Either<a,b> = Left(a) | Right(b)
        |data Unit = U""".stripMargin::"Primitive Data Types"::Nil,
    "Conn.Prim"::
      """fifo:A->A
        |fifofull<A>:A->A
        |sync:A->A
        |id:A->A
        |lossy:A->A
        |drain:A*A->()
        |dupl:A->A*A
        |merger:A*A->A
        |xor:A->A*A
        |writer:()->A
        |reader:A->()""".stripMargin::
      "Primitive Connectors"::Nil,
    "Conn.Math"::
      """import Types.{Nat,Unit}
        |import Conn.Prim
        |
        |// counts ticks (to check)
        |def counter(tick): Nat = {
        |    drain(tick,n)
        |    succ:=build(U,n) // build<Nat>
        |    next:=fifo(succ)
        |    iter:=fifofull(next) // filled with Zero
        |    n,res:=xor(iter)
        |    zero:=Zero
        |    drain(res,zero)
        |    succ:=zero
        |    res
        |}
        |
        |// Addition of naturals (to check)
        |def add(a, b): Nat = {
        |  drain(a,b)
        |  lockAll:=fifo(a)
        |  lockA:=fifo(a)
        |  waitB:=fifo(b)
        |  next:=a
        |  toMatch:=fifo(next)
        |  zero,succ:=match(toMatch)
        |  next:=fifo(succ)
        |  res:=counter(succ)
        |  aDone,bDone:=xor(zero)
        |  drain(aDone,lockA)
        |  drain(aDone,waitB)
        |  next:=waitB
        |  lockB:=fito(waitB)
        |  drain(bDone,lockB)
        |  drain(bDone,lockAll)
        |  drain(bDone,res)
        |  res
        |}
        |
        |// fibbonaci TODO
        |def fib(): Nat = {
        |  b:=fifoFull<Succ(Zero)>(a)
        |  c:=fifo(b)
        |  a := add(b,c)
        |  a
        |}""".stripMargin::
      "Math Connectors"::Nil,
    "Conn.ControlFlow"::
      """import Types.{Bool,Unit}
        |import Conn.Prim
        |// If Then Else
        |def ite(b:Bool,then:A,else:A): A = {
        |    t,f := match(b)
        |    drain(t,ok)
        |    drain(f,ko)
        |    ok
        |    ko
        |}
        |
        |def alt(i1,i2) = {
        |  a:=in1(i1) b:=in2(i2)
        |  drain(a, b)
        |  o:=a o:=fifo(b)
        |  o
        |}""".stripMargin::
      "Control Flow Connectors"::Nil
  )
}
