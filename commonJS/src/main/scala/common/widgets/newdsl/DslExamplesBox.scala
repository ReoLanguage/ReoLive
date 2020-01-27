package common.widgets.newdsl

import common.widgets.{Box, Setable}
import org.scalajs.dom.EventTarget

import scala.scalajs.js.UndefOr

/**
  * Created by guillecledou on 2019-06-12
  */


class DslExamplesBox(reload: => Unit, toSet: List[Setable[String]]) extends Box[Unit]("DSL Examples", Nil) {
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
    "alt"::
      """def alt(i1,i2) = {
        |  a:=in1(i1) b:=in2(i2)
        |  drain(a, b)
        |  x:=a x:=fifo(b)
        |  out(x)
        |}
        |alt(x,y)
        |""".stripMargin::"Alternator"::Nil,
    "xor"::
      """def exRouter(in) = {
        |  out1 := lossy(in)
        |  out2 := lossy(in)
        |  m:=out1  m:=out2
        |  drain(in,m)
        |  out1 out2
        |}
        |exRouter(x)
        |""".stripMargin::
      "Exclusive router"::Nil,
    "merger"::
      """out:=in1 out:=in2
        |out
        |""".stripMargin::
      "Merger"::Nil,
    "dupl"::
      """out1:=in out2:=in
        |out1 out2
        |""".stripMargin::
      "Replicator"::Nil,
    "lossy"::
      """lossy(x)
        |""".stripMargin::
      "Lossy channel"::Nil,
    "lossy-fifo"::
      """y:=lossy(x)
        |fifo(y)
        |""".stripMargin::
      "lossy-fifo"::Nil,
    "sequence3"::
      """x1:=fifofull(x3) drain(o1,x1) out1(o1)
        |x2:=    fifo(x1) drain(o2,x2) out2(o2)
        |x3:=    fifo(x2) drain(o3,x3) out3(o3)
        |""".stripMargin::
      "Sequencer-3"::Nil,
    "build & Constr"::
      """x := SomeData
        |x := build(
        |    Zero,
        |    Cons(Zero,Nil))
        |x
        |""".stripMargin::
      "Build a streams of [Zero,Zero] and merge it with SomeData."::Nil,
    "counter"::
      """data Nat = Zero | Succ(Nat)
        |data Unit = U
        |
        |// counts ticks (to check)
        |def counter(tick): Nat = {
        |  drain(tick,n)
        |  succ:=build(U,n) // build<Nat>
        |  next:=fifo(succ)
        |  iter:=fifofull(next) // filled with Zero
        |  n,res:=xor(iter)
        |  zero:=Zero
        |  drain(res,zero)
        |  succ:=zero
        |  res
        |}
        |
        |counter(a)""".stripMargin::
      "Gets clicks, outputs number of clicks since last request."::Nil,
    ///////////////////
//    "fix"::
//      """def conn(y) = {
//        |   x := y
//        |   fifo(x) lossy(x)
//        |}
//        |
//        |y,o := conn(True)
//        |y o""".stripMargin::
//      "Fix: should not replace 'x'  by true -- link instead."::Nil,
//    "badInst"::
//      """def f(x) = {
//        |   fifo(x) lossy(x)
//        |}
//        |f(True)
//        |
//        |""".stripMargin::
//      "Fix: should not replace 'x'  by true -- link instead."::Nil,
//    "badInst2"::
//      """def f(x) = {
//        |   fifo(x)
//        |}
//        |a:=lossy(b)
//        |f(a)
//        |""".stripMargin::
//      "Fix: should link the fifo to the lossy."::Nil,
    ///////////////////////////
    "Misc data"::
      """data List<a> = Nil | Cons(a,List<a>)
        |data Bool = True | False
        |data Nat = Zero | Succ(Nat)
        |data Pair<a,b> = P(a,b)
        |data Either<a,b> = Left(a) | Right(b)
        |data Unit = U
        |
        |x := Cons(Zero,Nil)
        |y := Cons(Zero,x)
        |z := Cons(Succ(Succ(Zero)),y)
        |w := True
        |a,b,c := dupl3(x)
        |
        |def alt(i1,i2) = {
        |  a:=in1(i1) b:=in2(i2)
        |  drain(a, b)
        |  o:=a o:=fifo(b)
        |  o
        |}
        |// If Then Else
        |def ite(b:Bool,then:A,else:A): A = {
        |    t,f := match(b)
        |    drain(t,ok)
        |    drain(f,ko)
        |    ok
        |    ko
        |}
        |
        |// fibbonaci
        |def fib(): Nat = {
        |  b:=fifoFull_Succ_Zero(a)
        |  c:=fifo(b)
        |  a := add(b,c)
        |  a
        |}
        |
        |// counts ticks (to check)
        |def counter(tick): Nat = {
        |  drain(tick,n)
        |  succ:=build(nil,n)
        |  next:=fifo(succ)
        |  iter:=fifoFull_Zero(next)
        |  n,res:=xor(iter)
        |  zero:=Zero
        |  drain(res,zero)
        |  succ:=zero
        |  res
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
        |}""".stripMargin::""::Nil
//    "Various types"::
//      """data List<a> = Nil | Cons(a,List<a>)
//        |data Bool = True | False
//        |data Nat = Zero | Succ(Nat)
//        |data Pair<a,b> = P(a,b)
//        |data Either<a,b> = Left(a) | Right(b)
//        |data Unit = U
//        |
//        |x = Cons(Zero,Nil)
//        |y = Cons(Zero,x)
//        |z = Cons(Succ(Succ(Zero)),y)
//        |w = True""".stripMargin::""::Nil,
//    "Nil case 1"::
//      """data List<a> = Nil | Cons(a,List<a>)
//        |data Bool = True | False
//        |
//        |x = Nil
//        |y = Nil
//        |z = Cons(True,x)""".stripMargin::
//      """<strong>x</strong> wil be forced to be of type <strong> List&lt;Bool&gt; </strong>
//        |<strong>y</strong> will be of type <strong> List&lt;a&gt; </strong>
//      """.stripMargin::Nil,
//    "Nil case 2"::
//      """data List<a> = Nil | Cons(a,List<a>)
//        |data Bool = True | False
//        |data Nat = Zero | Succ(Nat)
//        |
//        |x = Nil
//        |z = Cons(True,x)
//        |w = Cons(Zero,x)""".stripMargin::
//      """fails because <strong>x</strong> was consider of type <strong>List&lt;Bool&gt; first</strong>""".stripMargin::Nil,
//    "Conn Def"::
//      """data List<a> = Nil | Cons(a,List<a>)
//        |data Bool = True | False
//        |data Nat = Zero | Succ(Nat)
//        |
//        |def conn = {
//        |	dupl;fifo*lossy
//        |}
//        |
//        |def alt = {
//        |	alt {
//        | alt(i1?,i2?,o!) =
//        |   in1(i1,a) in2(i2,b) drain(a, b)
//        |   sync(a, c) fifo(b, c) out(c,o)
//        |  }
//        |}
//        |
//        |x = True
//        |y,z = conn(Nil)
//        |w = alt(Nil,x)
//        |
//        |//
//        |// Some discussed ideas
//        |//
//        |// 1) where o1 and o2 serve as variables,
//        |// don't need to be specified before
//        |// if they are actual params for outputs:
//        |// z = conn(x,o1,o2)
//        |// 2) (NO) unspecified could mean some kind of Unit type -
//        |// no data is sent or don't care about data:
//        |// z = conn
//        |// 3) outputs are declared on the lhs,
//        |// inputs on the rhs
//        |// (o1,o2) = conn(x)""".stripMargin::
//      """Ideas for connectors expressions""".stripMargin::Nil,
//    "Conn Infer"::
//      """data List<a> = Nil | Cons(a,List<a>)
//        |data Bool = True | False
//        |data Nat = Zero | Succ(Nat)
//        |
//        |def conn = {
//        |	dupl;fifo*lossy
//        |}
//        |
//        |def alt = {
//        |	alt {
//        | alt(i1?,i2?,o!) =
//        |   in1(i1,a) in2(i2,b) drain(a, b)
//        |   sync(a, c) fifo(b, c) out(c,o)
//        |  }
//        |}
//        |
//        |def noOutput = {
//        |	(\x.fifo^x) ;
//        |    (\n.drain^n)
//        |}
//        |
//        |def closed = {
//        |	writer^8 ; merger! ;
//        |	merger! ; reader!
//        |}
//        |
//        |true = True
//        |zero = Zero
//        |nil = Nil
//        |y,o = conn(true)
//        |w = alt(true,zero)""".stripMargin::
//      """Checking some connectors expression""".stripMargin::Nil,
//    "Multi Assign"::
//      """data Bool = True | False
//        |data Nat = Zero | Succ(Nat)
//        |
//        |def conn(y) = {
//        |   x := y
//        |   fifo(x) lossy(x)
//        |}
//        |
//        |y,o := conn(True)
//        |o1,o2 := conn(True)
//        |o3,o4 := conn(Zero)
//        |z,p := conn(Zero,out1,out2)""".stripMargin::
//      """Exmple of multiple assignment""".stripMargin::Nil
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
