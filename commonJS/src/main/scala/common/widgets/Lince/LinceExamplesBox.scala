package common.widgets.Lince

import common.widgets.{ButtonsBox, Setable}

class LinceExamplesBox(reload: => Unit, inputBox: Setable[String], descr: Setable[String])
  extends ButtonsBox(reload, List(inputBox,descr)){

  override protected val buttons: Seq[List[String]] = Seq(
    "Basic composition" -> """v:=0; v'=1 for 2; v'=3 for 2""" ->
      "Very simple example composing two basic atomic elements."
    ,"Cruise control"->
//        """x:= -1; v:= 0; a:= 1;
//          |repeat 2 {
//          |  if x <= 0 then a:= 1 else a:=-1;
//          |  x' = v, v' = a  & 0.5
//          |}
//          |""".stripMargin ->
       """// Cruise control
          |p:=0; v:=2;
          |repeat 15 {
          |  if v<=10
          |  then p'=v,v'=5  for 1
          |  else p'=v,v'=-2 for 1
          |}""".stripMargin ->
        descr("Cruise Control","Maintain a velocity of 10, updating every time unit.")
      ////
      ,"Traffic lights"->
        """// Alternate between two constant values.
          |l:=0;
          |repeat 4 {
          |   l:=0; wait 10 ;
          |   l:=1; wait 10
          |}""".stripMargin ->
        descr("Traffic lights","Alternating between two constant values.")
      ////
      ,"Avoiding approx. error"->
        """// A naive evaluation would give an approximation
          |// error of the if-condition.
          |x := 1;
          |x' = -x for 40;
          |x' =  x for 40;
          |if x == 1 then x:= 2
          |          else x:= 3""".stripMargin ->
      ( "Using approximated values, the value of x at 80 makes is slightly different " +
        "from 1, yielding a final value of 3 instead of 2. Using our symbolic computations, " +
        "the correct value of 2 is obtained. Note that our experimental warning system that " +
        "checks perturbations can detect that an approximation error can occur here at 80.")
      ////
      ,"Trigonometric computation"->
        """// Solution not naively computed (precise solution involves sin/cos)
          |// Use the online version to use the precise solution.
          |p:=1;v:=1;
          |p'=v, v'=-p for 8""".stripMargin ->
      ("When involving mutually dependent variables the naive numerical analysis does not work. " +
        "Using symbolic computations we plot precisely the functions with sin/cos.")
      ////
    ////
    ,"Naive particle position" ->
      """x:= -1; v:= 0; a:= 1; //t:= 0;
        |repeat 100 {
        |	//t:= 0;
        |	if x <= 0 then a:= 1 else {a:=-1 };
        |    	x' = v, v' = a  for 0.5  //, t' = 1 for t >= 0.5
        |}""".stripMargin ->
      descr("Moving particle", "A naive approach for moving a particle to a position x.")

    ////
    ,"Landing system" ->
      """y := 10000; v := -1000; a:= 0; g:= 10;
        |while (y >= 1000) {
        |	// t:= 0;
        |	if v <= -100 then { a := (100 - g) } else { a:= -g } ;
        |    y' = v, v' = a for 1//, t' = 1 &  t >= 1
        |} ;
        |while (y >= 25) {
        |	// t:= 0;
        |	if v <= -20 then { a := (20 - g) } else { a:= -g } ;
        |    y' = v, v' = a  for 1 //, t' = 1 &  t >= 1
        |} ;
        |while (y >= 1) {
        |	// t:= 0;
        |	if v <= -1 then { a := (15 - g) } else { a:= -g } ;
        |    y' = v, v' = a  for 0.05 // , t' = 1 &  t >= 0.05 \/ y<=0
        |}""".stripMargin ->
      descr("A Landing System", //"Experimental Event-Driven case-study. Not yet supported." +
        "Simulating a controller with 3 modes of approximation to land softly.")

    ///


    ,"Simple (ED)" ->
      """v:=0;
        |// jump every 0.01 until the condition holds
        |v'=2 until_0.01 v>4;
        |// jump every 0.1 until the condition holds,
        |// and then give smaller and smaller jumps
        |// until a precision of 10^-9
        |v'=-1 until_0.000000001,0.1 v<3""".stripMargin ->
      "Experimental event-driven example (using approximations)."
    /////
    ,"Bouncing ball (ED)"->
    """// Bouncing ball example
          |v:=5; p:=10; c:=0;
          |while (c<4) {
          |  v'=-9.8, p'=v until_0.001 p<0 /\ v<0;
          |  v:=-0.5*v; c:=c+1
          |}""".stripMargin ->
        descr("Bouncing Ball","Experimental Event-Driven example, using steps of 0.001. " +
    //"Not yet fully supported." +
    "A ball position and velocity as it bounces in the floor. " +
    "It includes an experimental feature: using a condition (p<0 /\\ v<0) " +
    "to end a trajectory using a naive search algorithm.")
    /////
    //    ,"Bouncing ball"->
    //       """// Bouncing ball example
    //         |v:=5; p:=10;
    //         |repeat 4 {
    //         |  v=-9.8, p=v & p<0 /\ v<0;
    //         |  v:=-0.5*v

    /////
    ,"Fireflies 2x (ED)"->
        """f1 := 1; f2 := 4;
        |repeat 8 {
        |  f1'=1, f2'=1 until_0.01
        |       f1>10 \/ f2>10;
        |  if f1>=10 /\ f2<10
        |    then { f1:=0; f2:=f2+2 }
        |    else if f2>=10 /\ f1<10
        |         then { f2:=0;f1 :=f1 +2 }
        |         else { f1:=0; f2 :=0 }
        |}""".stripMargin ->
      descr("Fireflies 2x","Experimental Event-Driven example. " +
        "Every firefly has an internal clock which helps " +
        "it to know when to flash: when the clock reaches a threshold the firefly " +
        "flashes and the clock’s value is reset to zero. The flash of a firefly " +
        "increases the internal clock’s value of all other fireflies nearby." +
        "This version synchronises 2 fireflies.")
    ////

    ,"Fireflies 3x (ED)"->
        """f1 := 1; f2 := 4; f3 := 7;
        |repeat 8 {
        |  f1'=1, f2'=1, f3'=1
        |  until_0.01 f1>10 \/ f2>10 \/ f3>10;
        |  if f1>=10 /\ f2<10 /\ f3<10
        |    then { f1:=0; f2:=f2+2; f3:=f3+2 }
        |    else if f2>=10 /\ f1<10 /\ f3 < 10
        |         then { f2:=0;f1 :=f1 +2; f3:=f3+ 2 }
        |         else { f3:=0; f1 := f1 +2; f2:= f2 +2 }
        |}""".stripMargin ->
      descr("Fireflies 3x","Experimental Event-Driven Example. " +
        "Every firefly has an internal clock which helps " +
        "it to know when to flash: when the clock reaches a threshold the firefly " +
        "flashes and the clock’s value is reset to zero. The flash of a firefly " +
        "increases the internal clock’s value of all other fireflies nearby. This version synchronizes 3 fireflies")

  ).map(x=>List(x._1._1,x._1._2,x._2))

}
