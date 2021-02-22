package common.widgets.choreography

import common.widgets.{Box, ExampleBox, Setable}
import choreo.choreo2.Examples
import choreo.choreo2.syntax.Choreo


/**
  * Created by guillecledou on 03/11/2020
  */

class ChoreoExamplesBox(reload: => Unit, toSet: List[Setable[String]])
  extends ExampleBox("Choreography Examples",reload,toSet) {

  protected val buttons:Seq[List[String]] = {
    (for ((n,e) <- Examples.examples2show) //Examples.all.toSeq.sortBy(_._1)
      yield n::e.toString::n::Nil).toSeq
  }

//  protected val buttons: Seq[List[String]] = Seq(
//    "streaming"::
//    """def send<w>(dp,kp)(k) = {
//      | get(dp) -> w:=dp
//      | get(w,kp) -> k:={w,kp}
//      |}
//      |
//      |def notify<x,y,z>(k)(c1,c2) = {
//      | get(k) -> x:=k
//      | get(x) -> y:=x
//      | get(y) -> z:=y
//      | get(z) -> c1:=z, c2:=z
//      |}
//      |
//      |def check<z>(m)(m)={
//      | get(z) -> m:=z, z:=z
//      |}
//      |
//      |(
//      | dataProd,keyProd >send(w)> kernel ;
//      | kernel > notify(x,y,z)>cons1,cons2
//      |)*
//      ||| (monitor>check(z)>monitor)*
//      |""".stripMargin::"Streaming Protocol"::Nil,
//    "fifo"::
//    """def fifo<m>(i)(o) = {
//      | get(i),und(m) -> m:=i
//      | get(m) -> o:=m
//      |}
//      |
//      |a>fifo(m)>b
//      |""".stripMargin::"Fifo"::Nil,
//    "fifoOverride"::
//      """def ofifo<m>(i)(o) = {
//        | get(i) -> m:=i
//        | get(m) -> o:=m
//        |}
//        |
//        |a>ofifo(m)>b
//        |""".stripMargin::"Override-Fifo"::Nil,
//    "fifo-sync"::
//      """def fifo<m>(i)(o) = {
//        | get(i),und(m) -> m:=i
//        | get(m) -> o:=m
//        |}
//        |
//        |def sync(i)(o) = {
//        | get(i) -> o:=i
//        |}
//        |
//        |a>fifo(m)>b ; a>sync>c
//        |""".stripMargin::"Fifo-Sync"::Nil,
//    "reader"::
//      """def reader<m>(a)() = {
//        | get(a),und(m) -> m:=a
//        |}
//        |
//        |a>reader(m)>
//        |""".stripMargin::"Fifo"::Nil
//  )
}
