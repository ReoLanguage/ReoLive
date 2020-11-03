package common.widgets.arx

import common.widgets.{Box, ExampleBox, Setable}
import org.scalajs.dom.EventTarget

import scala.scalajs.js.UndefOr

/**
  * Created by guillecledou on 2019-06-12
  */


class DslExamplesBox(reload: => Unit, toSet: List[Setable[String]]) extends
  ExampleBox("ARx Examples", reload,toSet) {

  /**
    * Each buttom is a list of strings: the first is the button name,
    * and each of the remaining are passed to the `toSet` objects by the same order.
    *
    */
  protected val buttons: Seq[List[String]] = Seq(
    "alt"::
      """drain(a,b)
        |x<-a
        |x<-fifo(b)
        |x""".stripMargin::"Alternator"::Nil,
    "xor"::
      """out1 <- lossy(in)
        |out2 <- lossy(in)
        |x<-out1  x<-out2
        |drain(in,x)
        |out1 out2""".stripMargin::
      "Exclusive router"::Nil,
    "def-alt"::
      """def alt(i1,i2) = {
        |  a<-in1(i1) b<-in2(i2)
        |  drain(a, b)
        |  x<-a x<-fifo(b)
        |  out(x)
        |}
        |alt(x,y)
        |""".stripMargin::"Alternator"::Nil,
      "alt2"::
        """// alternate a,b
          |def seqDrain(a,b) = {
          |  s1 <- fifofull(s2)
          |  s2 <- fifo(s1)
          |  drain(s1,a) drain(s2,b)
          |}
          |seqDrain(a,b)
          |m<-a  m<-b
          |fifo(m)""".stripMargin::"Alternator - variation"::Nil,
      "merger"::
        """out<-in1 out<-in2
          |out
          |""".stripMargin::
        "Merger"::Nil,
      "dupl"::
        """out1<-in out2<-in
          |out1 out2
          |""".stripMargin::
        "Replicator"::Nil,
      "lossy"::
        """lossy(x)
          |""".stripMargin::
        "Lossy channel"::Nil,
      "lossy-fifo"::
        """y<-lossy(x)
          |fifo(y)
          |""".stripMargin::
        "lossy-fifo"::Nil,
      "lossyFifoVar"::
        """b<-fifo(a)  c<-fifo(b)
          |drain(next,a)
          |next,out<-xor(c)
          |next<-fifofull(out)
          |out""".stripMargin::"Lossy Fifo - keeps the most recent value."::Nil,
      "sequence3"::
        """x1<-fifofull(x3) drain(o1,x1) out1(o1)
          |x2<-    fifo(x1) drain(o2,x2) out2(o2)
          |x3<-    fifo(x2) drain(o3,x3) out3(o3)
          |""".stripMargin::
        "Sequencer-3"::Nil,
      "counter"::
        """def counter(tick):Nat = {
          |  first,rest<-xor(tick)
          |  drain(first,zero) drain(rest,more)
          |  succ<-build(zero,more)
          |  buff<-fifo(succ)
          |  next<-fifofull<Zero>(buff)
          |  zero,more<-match(next)
          |  succ
          |}
          |counter(x)
          |""".stripMargin::
        "Gets clicks, outputs number of clicks since last request."::Nil,
      "matches"::
        """def natmatch(x:Nat) = {
          | z,s <- match(x)
          | z s
          | }
          |
          |def listmatch(y:List<a>) = {
          | n,e,r <- match(y)
          | n e r
          | }
          |
          |def boolmatch(z:Bool) = {
          | t,f <- match(z)
          | t f
          | }
          |
          |natmatch(w)""".stripMargin::
        ""::Nil,
      "display"::
        """def gui(sel:Bool,mouse,time) = {
          |  last <~ sel
          |  t,f <- match(last)
          |  drain(t,mouse) display <- mouse
          |  drain(f,time)  display <- time
          |  display
          |}
          |
          |gui(sm,mc,t)
        """.stripMargin::""::Nil,
      "sb sequencer"::
      """sb seq<m1,m2>(i1,i2) = {
         |  init m1:=U
         |  get(i1,m1),und(m2) -> m2:=m1, o1:=i1
         |  get(i2,m2),und(m1) -> m1:=m2, o2:=i2
         |  o1 o2
         |}
         |
         |seq(a,b)""".stripMargin::"Sequencer definition using stream builder notation"::Nil,
      "sb fifo"::
      """sb boolfifo<m:Bool>(a) = {
          | get(a),und(m) -> m:=a
          | get(m) -> b:=m
          | b
          |}
          |
          |o1<-boolfifo(in)
          |o2<-boolfifo(True)
          |o1 o2
          |""".stripMargin::"Boolean fifo definition using stream builder notation"::Nil
  )
}
