package widgets.feta

import common.widgets.{ExampleBox, Setable}

/**
 * Created by guillecledou on 21/04/2021
 */

class FetaExamplesBox(reload: => Unit, toSet: List[Setable[String]])
  extends ExampleBox("(F)ETA Examples",reload,toSet) {

  protected val buttons:Seq[List[String]] = Seq(
    "Simple (ETA)" ::
      """CA one (a)(b) = {
        | start 0
        | 0 --> 1 by a
        | 1 --> 0 by b
        |}
        |
        |CA two (b)(a) = {
        | start 0
        |  0 --> 1 by b
        |  1 --> 0 by a
        |}
        |
        |CA three (a)() = {
        | start 0
        | 0 --> 0 by a
        |}
        |
        |CA four ()(a) = {
        | start 0
        |  0 --> 0 by a
        |}
        |
        |FS = (c1:one, c2:two, c3:three, c4:four)
        |
        |STS = {
        | default = one to one
        |}""".stripMargin :: "Simple" :: Nil,
    "Race (ETA)" ::
      """//Race example
        |CA runner (start)(finish) = {
        | start 0
        | 0 --> 1 by start
        | 1 --> 2 by run
        | 2 --> 0 by finish
        |}
        |
        |CA controller (finish)(start) = {
        | start 0
        | 0 --> 1 by start
        | 1 --> 2 by finish
        | 2 --> 0 by finish
        |}
        |
        |FS = (r1:runner, r2:runner, c:controller)
        |
        |STS = {
        | default = 1 to 1 // or "one to one", or "1..1 to 1..1"
        | start = 1 to 2
        |}""".stripMargin :: "Race example" :: Nil,
    "Chat (ETA)"::
      """CA user (confirmL,confirmJ,fwd)
        |         (leave,join,msg) = {
        | start 0
        | 0 --> 1 by join
        | 1 --> 2 by confirmJ
        | 2 --> 2 by msg
        | 2 --> 2 by fwd
        | 2 --> 3 by leave
        | 3 --> 0 by confirmL
        |}
        |
        |CA server (leave,join,msg)
        |           (confirmL,confirmJ,fwd) = {
        | // internal: reject, grant, ask
        | start 0
        |  0 --> 1 by join
        |  1 --> 0 by confirmJ
        |  0 --> 2 by leave
        |  2 --> 0 by confirmL
        |  0 --> 3 by msg
        |  3 --> 4 by ask
        |  4 --> 5 by grant
        |  4 --> 0 by reject
        |  5 --> 0 by fwd
        |}
        |
        |FS = (u1:user,u2:user,s:server)
        |
        |STS = {
        | default = one to one // or 1..1 to 1..1
        | fwd = one to any // or 1.1 to 0..*
        |}""".stripMargin::"Chat"::Nil,

    "Auth (FETA)" ::
      """FCA user (confirm)(join,leave) = {
        |  start 0
        |  0 --> 1 by join if s
        |  1 --> 2 by confirm if s
        |  0 --> 2 by join if o
        |  2 --> 0 by leave
        |}
        |
        |FCA server (join,leave)(confirm) = {
        |  start 0
        |  0 --> 1 by join if s
        |  1 --> 0 by confirm if s
        |  0 --> 0 by join if o
        |  0 --> 0 by leave
        |}
        |
        |FS = (u1:user,u2:user,s:server)
        |
        |FM = s xor o
        |
        |FSTS = {
        | default = one to one // or 1..1 to 1..1
        | {o}:join,leave = many to one // or 1..* to 1..1
        |}""".stripMargin :: "Auth" :: Nil
  )

}
