package common.widgets.virtuoso

import common.widgets.{ButtonsBox, Setable}

class VirtuosoExamplesBox(reload: => Unit, inputBox: Setable[String],msgBox:Setable[String],csBox:Setable[String],logicBox:Setable[String])
  extends ButtonsBox(reload, List(msgBox, inputBox,csBox,logicBox)){

  override def init(div: Block, visible: Boolean): Unit = {
      val buttonsDiv = super.panelBox(div,visible,
        buttons = List(
          Right("help") -> (() =>
            common.Utils.goto("https://hubs.readthedocs.io/en/latest/tutorial.html#examples-widget"),
            "See documentation for this widget"))
        ).append("div")
        .attr("id", "buttons")
        .attr("style","padding: 2pt;")

      buttonsDiv
        .style("display:block; padding:2pt")

      for (ops <- buttons ) yield genButton(ops,buttonsDiv)
  }

  override protected val buttons: Seq[List[String]] = Seq(
    "Port"::("<p><strong>Port Hub</strong></p>Forwards data from its source to its sink, acting" +
      " as a synchronisation mechanism between two tasks."  +
      " There is no buffer capacity, i.e.data is transfer" +
      " directly between the two tasks.")::
      """port """::
      "//minimum number of context switches\n//to read twice \n " +
      "out^2".stripMargin::
      """// always executing in or nothing else executes
        |A[] in.doing or nothing
        |// out fires at the same time as in
        |A[] in imply out
        |// Is it possible to fire in and not out?
        |E<> in and not out""".stripMargin::Nil,
    "Port - 2 sources"
      ::"""<p><strong>Merging Port Hub</strong></p>
          | <p>Similar to the simple Port, but uses only one of its source points.</p>""".stripMargin
      :: "merger"::Nil,
    "Port - 2 sinks"
      :: ("<p><strong>XOR Port Hub</strong></p>"+
          "Similar to the simple Port, but uses only "+
          "one of its sink points.")
      :: "xor"::Nil,
    "Duplicator"
      ::("<p><strong>Duplicator</p></strong>" +
         "Similar to the simplr Port, duplicates incoming data to all of its sink poins." +
         " It can only receive data once all its sources are ready to receive data.")
      :: "dupl"
      ::"//minimum number of context switches\n//to write once\n" + "in"::
      """A[] out1 imply out2
        |in.doing --> out1 and out2
      """.stripMargin::Nil,
    "Semaphore"
      ::("<p><strong>Semaphore</strong></p>"+
      "Has two interaction points: to signal the semaphore and "+
      "increment its internal counter c, and to test if the "+
      "semaphore is set, i.e., c ≥ 0, in which case succeeds "+
      "and decrements its counter, otherwise it can wait. ")
      :: " semaphore "::Nil,
    "Event"
      :: ("<p><strong>Event</strong></p>" +
      "It has two waiting lists for two kind of requests: raise – signals the " +
      "occurrence of an event, and test – checks if an event happened, in which case " +
      "succeeds and deactivates the signal, otherwise it can wait.")
      :: "event "::Nil,
    "DataEvent"
      :: ("<p><strong>Data Event</strong></p>" +
      "Similar to the Event hub with the additional capacity to buffer " +
      "a data element sent with the raise signal. If the signal has been raised, the test " +
      "signal receives the buffered data and deactivates the signal, otherwise it can wait. " +
      "Data can be overridden, i.e.raise always succeeds. An additional waiting " +
      "lists for clear requests is used to clear the buffer, mainly to facilitate interfacing " +
      "with device drivers.taEvent" +
      "")
      :: "dataEvent "::Nil,
    "Resource"
      :: ("<p><strong>Resource</strong></p>" +
      "Has two waiting lists for two kind of requests: lock – signals the " +
      "Resource acquisition of a logical resource, which succeeds if the resource is free, otherwise " +
      "it can wait, and unlock – signals the release of an acquired resource, which " +
      "succeeds if the resource had been acquire by the same task that released it, " +
      "failing otherwise.")
      :: "resource"::Nil,
    "Fifo"
      ::("<p><strong>Fifo</strong></p>" +
      "Has two waiting lists for two kind of requests: enqueue – signals the " +
      "entering of some data into the queue, which succeeds if the queue is not full, " +
      "and dequeue – signals data leaving the queue, which succeeds if the queue is not " +
      "empty. The presented Fifo can store at most 1 element - a general Fifo can store up to a fixed number N of elements.")
      :: "fifo "::Nil,
    "Timer"
      ::("<p><strong>Timer</strong></p>" +
      "Has two waiting lists for two kind of requests: set – signals the " +
      "entering of some data into the buffer, which succeeds if the buffer is not full. " +
      "This resets a clock to 0 and specified a timeout in which the data can be read." +
      "and timeout – signals data leaving the buffer, which succeed if the buffer is not empty and the clock evolved to the specified timeout." +
      "Otherwise, the timer enters a deadlock.")
      :: "timer(5) "::""::
      """// after doing in
        |// and before doing any other action,
        |// clock cl cannot grow beyond 5
        |A[] in.doing imply cl <= 5
        |// For every in, out fires (before in again)
        |every in --> out
        |// every in, out executes
        |// and it waits at least 5 to do it
        |every in --> out after 5
        |// out waits at least 5 before refiring
        |A[] out refiresAfterOrAt 5""".stripMargin :: Nil,
        //|// For every in, out fires (before in again)
        //|in --> not(in) until out
    "Blackboard"
      ::("<p><strong>Blackboard</strong></p>" +
      "Acts like a protected shared data area. A update waiting list " +
      "is used to set its content (whereby a sequence number is incremented), a read " +
      "waiting list is used to read the data, and a count waiting list is used to obtain " +
      "the sequence number, allowing tasks to attest the freshness of the data. A special " +
      "data element, CLR, can be sent with the update signal to clear the buffer.")
      :: "blackboard "::Nil,
    "Alternator" ::
      ("<p><strong>Alternator</strong></p>" +
        "For every pair of values received by two waiting lists, it forwards them to the output. " +
        "It sends the values always in the same order, and stores at most 1 value.") ::
        "alt {\n  alt(in1?,in2?,out!) =\n    dupl(in1,d1,out)\n    dupl(in2,d2,f)\n    fifo(f,out)\n    drain(d1,d2)\n}"::Nil,
    "Alternator (no variables)" ::
      ("<p><strong>Alternator</strong></p>" +
        "For every pair of values received by two waiting lists, it forwards them to the output. " +
        "It sends the values always in the same order, and stores at most 1 value.") ::
      "dupl*dupl;\nfifo*drain*id;\nmerger"::Nil,
    "Sequencer"
      ::"Outputs a value alternating between 3 outputs"
      ::"""seq3 {
          | seq3 (x!,y!,z!) =
          |   event(a,b) event(c,d) eventFull(e,f)
          |   dupl(b,c,x) dupl(d,e,y) dupl(f,a,z)
          |}""".stripMargin::Nil,
    "Tasks in sequence (in paper)"
      ::"Two tasks executing in sequence, publishing data to an actuator task."
      ::"""mainW // try different scenarios
          |{
          |  dupl3(a?,b!,c!,d!) =
          |    dupl(a,b,ab) dupl(ab,c,d)
          |    ,
          |  seq(s1?,p1?,s2?,p2?,get!) =
          |    dupl3(p1,d11,d12,d13)  dupl3(p2,d21,d22,d23)
          |    drain(s1,d21) drain(s2,d11)
          |    drain(d12,d42) drain(d22,d32)
          |    dupl(e1,d41,d42) dupl(e2,d32,d31)
          |    eventFull(d31,e1) event(d41,e2)
          |    merger(d13,d23,get)
          |    ,
          |  // Scenarios
          |  mainW() = // waits (possibly for ever)
          |    task<t1>(W p1!,W s1!) //   'put1' goes first
          |    task<t2>(W s2!,W p2!) // 'start2' goes first
          |    task<act>(W get?)
          |    seq(s1,p1,s2,p2,get)
          |    ,
          |  mainWE() = // waits (possibly for ever)
          |    task<t2>(W s2!,W p2!) every 3
          |    task<t1>(W p1!,W s1!) every 3
          |    task<act>(W get?)
          |    seq(s1,p1,s2,p2,get)
          |    ,
          |  mainNW() = // more states!
          |    task<t1>(NW p1!,NW s1!) every 3
          |    task<t2>(NW s2!,NW p2!) every 3
          |    task<act>(W get?)
          |    seq(s1,p1,s2,p2,get)
          |    ,
          |  mainTO() = // with timeouts (timeout without 'every)
          |    task<t1>(3 p1!,3 s1!) every 6
          |    task<t2>(3 s2!,3 p2!) every 6
          |    task<act>(W get?)
          |    seq(s1,p1,s2,p2,get)
          |    ,
          |  mainDL() = // deadlock
          |    task<t1>(NW p1!,W s1!) every 2
          |    task<t2>( W s2!,W p2!) every 3
          |    task<act>(W get?)
          |    seq(s1,p1,s2,p2,get)
          |}""".stripMargin
      ::""
      ::"""// Task 2 can send data to the actuator
        |A[] s1 imply ((p1.t>=p2.t) and (s2.t>=p2.t))
        |// Task 2 can always start
        |A<> s2
        |// If start1 fires, start2 must eventually fire
        |s1 -->  s2
        |// Evertime start1 fires, start2 must fire (after >2)
        |every s1 --> s2 after 2
        |// Task 2 can only start 4 time units after
        |// finishing a previous round.
        |A[] s2 imply (p2.t >= 2) or not(p2.done)
        |// maximum time before repeating s2
        |A[] s2 refiresBeforeOrAt 9""".stripMargin::Nil,
    "2 tasks and semaphores"
      ::"Round robin between 2 tasks, without an actuator."
      ::"""rr1 {
          | rr1() =
          |   task<tk1>(W r1?, W w1!)
          |   task<tk2>(W w2!, W r2?)
          |   semaphore(w1,r2) semaphore(w2,r1)
          |   ,
          | rr2() =
          |   task<t1>(W tb?,NW p1!,10 pa!)
          |   task<t2>(10 pb!,W ta?,NW p2!)
          |   task<act>(W get?)
          |   semaphore(pb,tb) semaphore(pa,ta)
          |   merger(p1,p2,get)
          |}""".stripMargin::Nil,
  )

}