package widgets.Treo

import common.widgets.{ButtonsBox, Setable}

class TreoExamplesBox(reload: => Unit, inputBox: Setable[String], descr: Setable[String])
  extends ButtonsBox(reload, List(inputBox,descr)){

  override protected val buttons: Seq[List[String]] = Seq(
    "sync"->
      """import reo.sync;
        |main(a, b) {
        |  sync(a,b)
        |}""".stripMargin ->
      descr("Sync",""),
    "lossy"->
      """import reo.lossy;
        |main(a, b) {
        |  lossy(a,b)
        |}""".stripMargin ->
      descr("Lossy",""),
    "fifo1"->
      """import reo.fifo1;
        |main(a, b) {
        |  fifo1(a,b)
        |}""".stripMargin ->
      descr("fifo1",""),
    "fifo2"->
      """import reo.fifo1;
        |main(a, c) {
        |  fifo1(a,b)
        |  fifo1(b,c)
        |}""".stripMargin ->
      descr("fifo2",""),
    "fifo3"->
      """import reo.fifo1;
        |main(a, d) {
        |  fifo1(a,b)
        |  fifo1(b,c)
        |  fifo1(c,d)
        |}""".stripMargin ->
      descr("fifo3",""),
    "fifo4"->
      """import reo.fifo1;
        |main(a, e) {
        |  fifo1(a,b)
        |  fifo1(b,c)
        |  fifo1(c,d)
        |  fifo1(d,e)
        |}""".stripMargin ->
      descr("fifo4",""),
    "merger"->
      """import reo.sync;
        |main(a, b,c) {
        |  sync(a,c)
        |  sync(b,c)
        |}""".stripMargin ->
      descr("Merger",""),
    "dupl"->
      """import reo.sync;
        |main(a, b,c) {
        |  sync(a,b)
        |  sync(a,c)
        |}""".stripMargin ->
      descr("Duplicator",""),
    "fifofull"->
      """import reo.fifofull;
        |main(a, b) {
        |  fifofull<"hi">(a,b)
        |}""".stripMargin ->
      descr("fifofull",""),
    "Alternator"->
      """import reo.sync;
        |import reo.syncdrain;
        |import reo.fifo1;
        |
        |main(a, b, c) {
        |  syncdrain(a,b)
        |  sync(b,x)
        |  fifo1(x,c)
        |  sync(a,c)
        |}""".stripMargin ->
      descr("Alternator",""),
    "Alternator Full"->
      """import reo.sync;
        |import reo.syncdrain;
        |import reo.fifofull;
        |
        |main(a, b, c) {
        |  syncdrain(a,b)
        |  sync(b,x)
        |  fifofull<42>(x,c) sync(a,c)
        |}""".stripMargin ->
      descr("Alternator full",""),
    "Sequencer Spout"->
      """import reo.sync;
        |import reo.fifo1;
        |import reo.fifofull;
        |
        |main(o1,o2,o3) {
        |  fifo1(a,b)
        |  fifo1(b,c)
        |  fifofull<0>(c,a)
        |  sync(a,o1)
        |  sync(b,o2)
        |  sync(c,o3)
        |}""".stripMargin ->
      descr("Sequencer spout","")
  ).map(x=>List(x._1._1,x._1._2,x._2))

}
