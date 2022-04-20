package widgets.feta

import common.widgets.{ExampleBox, Setable}

/**
 * Created by guillecledou on 21/04/2021
 */

class FetaExamplesBox(reload: => Unit, toSet: List[Setable[String]])
  extends ExampleBox("FETA Examples",reload,toSet) {

  protected val buttons:Seq[List[String]] = Seq(
    "Auth"::
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
        |FS = (u1->user,u2->user,s->server)
        |
        |FM = s xor o
        |
        |FSTS = {
        | default = one to one // or 1..1 to 1..1
        | {o}:join,leave = many to one // or 1..* to 1..1
        |}""".stripMargin::"Auth"::Nil,
    "Chat"::
      """FCA user (confirmL,confirmJ,fwd)
        |         (join,msg,leave) = {
        | start 0
        | 0 --> 1 by join
        | 1 --> 2 by confirmJ
        | 2 --> 2 by msg
        | 2 --> 2 by fwd
        | 2 --> 3 by leave
        | 3 --> 0 by confirmL
        |}
        |
        |FCA server (leave,join,reject,grant,msg)
        |           (confirmL,confirmJ,ask,fwd) = {
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
        |FS = (u1->user,u2->user,s->server)
        |
        |FM = true
        |
        |FSTS = {
        | default = one to one // or 1..1 to 1..1
        | {}:fwd = one to any // or 1.1 to 0..*
        |}""".stripMargin::"Chat"::Nil
  )

}
