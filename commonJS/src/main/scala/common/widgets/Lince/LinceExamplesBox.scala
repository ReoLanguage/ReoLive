package common.widgets.Lince

import common.widgets.{ButtonsBox, Setable}

class LinceExamplesBox(reload: => Unit, inputBox: Setable[String], descr: Setable[String], bounds: Setable[String])
  extends ButtonsBox(reload, List(bounds,inputBox,descr)){

  override protected val buttons: Seq[List[String]] = Seq(
    "Basic composition" -> "150 // maximum time in the plot" ->
      """v:=0; v'=1 for 2; v'=3 for 2;""" ->
      "Very simple example composing two basic atomic elements."
    , "Cruise control" ->
      //        """x:= -1; v:= 0; a:= 1;
      //          |repeat 2 {
      //          |  if x <= 0 then a:= 1 else a:=-1;
      //          |  x' = v, v' = a  & 0.5
      //          |}
      //          |""".stripMargin ->
      "15 // maximum time in the plot" ->
      """// Cruise control
        |p:=0; v:=2;
        |while true do {
        |  if v<=10
        |  then p'=v,v'=5  for 1;
        |  else p'=v,v'=-2 for 1;
        |}""".stripMargin ->
      descr("Cruise Control", "Maintain a velocity of 10, updating every time unit.")
    ////
    , "Adaptive cruise control" ->
      "30 // maximum time in the plot" ->
      """// Adaptive cruise control
        |// -- Follower --
        |p:=0; v:=0;     // position and velocity
        |// -- Leader --
        |pl:=50; vl:=10; // position and velocity
        |while true do{
        |  if (p+v+2.5 < pl+10) &&
        |  	 ((v-5)^2 +
        |      4*(p+v+2.5-pl-10) < 0)
        |  then p'=v,v'=5,pl'=10 for 1;
        |  else p'=v,v'=-2,pl'=10 for 1;
        |}""".stripMargin ->
      descr("Adaptive Cruise Control", "Maintain the distance to the car in front.")
    ////
    , "Traffic lights" ->
      "150 // maximum time in the plot" ->
      """// Alternate between two constant values.
        |l:=0;
        |repeat 4 {
        |   l:=0; wait 10 ;
        |   l:=1; wait 10 ;
        |}""".stripMargin ->
      descr("Traffic lights", "Alternating between two constant values.")
    ////
    , "Avoiding approx. error" ->
      "150 // maximum time in the plot" ->
      """// A naive evaluation would give an approximation
        |// error of the if-condition.
        |x := 1;
        |x' = -x for 40;
        |x' =  x for 40;
        |if x == 1 then x:= 2;
        |          else x:= 3;""".stripMargin ->
      ("Using approximated values, the value of x at 80 is slightly different " +
        "from 1, yielding a final value of 3 instead of 2. Using our symbolic evaluation, " +
        "Lince obtains the correct value of 2. Note that our experimental warning system, which " +
        "checks perturbations, detects that an approximation error can occur here at 80.")
    ////
    , "Trigonometric computation" ->
      "150 // maximum time in the plot" ->
      """// Solution not naively computed (precise solution involves sin/cos)
        |// Use the online version to use the precise solution.
        |p:=1;v:=1;
        |p'=v, v'=-p for 8;""".stripMargin ->
      ("When involving mutually dependent variables the naive numerical analysis does not work. " +
        "Using symbolic computations we plot precisely the functions with sin/cos.")
    ////
    ////
    , "Naive particle positioning" ->
      "150 // maximum time in the plot" ->
      """x:= -1; v:= 0; a:= 1;
        |while true do {
        |	if x <= 0 then a:= 1; else a:=-1;
        |    	x' = v, v' = a  for 0.5;
        |}""".stripMargin ->
      descr("Moving particle", "A naive approach for moving a particle to a position x.")

  ////
  , "Landing system" ->
    "150 // maximum time in the plot" ->
    """y := 10000; v := -1000; a:= 0; g:= 10;
      |while (y >= 1000) do {
      |	if v <= -100 then a := (100 - g);
      |              else a:= -g;
      |    y' = v, v' = a for 1;
      |}
      |while (y >= 25) do {
      |	if v <= -20 then a := (20 - g);
      |             else a:= -g;
      |    y' = v, v' = a  for 1;
      |}
      |while (y >= 1) do {
      |	if v <= -1 then a := (15 - g);
      |            else a:= -g;
      |    y' = v, v' = a  for 0.05;
      |}""".stripMargin ->
    descr("A Landing System", //"Experimental Event-Driven case-study. Not yet supported." +
      "Simulating a controller with 3 modes of approximation to land softly.")

  ///


  //    ,"Simple (ED)" ->
  //      """v:=0;
  //        |// jump every 0.01 until the condition holds
  //        |v'=2 until_0.01 v>4;
  //        |// jump every 0.1 until the condition holds,
  //        |// and then give smaller and smaller jumps
  //        |// until a precision of 10^-9
  //        |v'=-1 until_0.000000001,0.1 v<3""".stripMargin ->
  //      "Experimental event-driven example (using approximations)."
  /////
  , "Bouncing ball (ED)" ->
    "150 // maximum time in the plot" ->
    """// Bouncing ball example
      |v:=5; p:=10; c:=0;
      |while (c<4) do {
      |  v'=-9.8, p'=v until_0.01 p<0 && v<0;
      |  v:=-0.5*v; c:=c+1;
      |}""".stripMargin ->
    descr("Bouncing Ball", "Event-Driven (ED) example, using steps of 0.01. " +
      //"Not yet fully supported." +
      "A ball position and velocity as it bounces in the floor. " +
      "It includes an experimental feature: when using instead \"until_0.0001,0.1\" " +
      "it will check the condition every 0.1 until it holds, and it will then refine it "+
      "with a precision of 0.0001 by checking the condition at every middle point.")
  /////
  //    ,"Bouncing ball"->
  //       """// Bouncing ball example
  //         |v:=5; p:=10;
  //         |repeat 4 {
  //         |  v=-9.8, p=v & p<0 && v<0;
  //         |  v:=-0.5*v

  /////
  , "Bouncing ball 2 (ED)" ->
    "10 // maximum time in the plot" ->
    """// Bouncing ball example - variation
      |v:=0; p:=10; a:=-9.81;
      |vmin:=0.2; // minimum velocity to bounce back
      |repeat 20 {
      |  v'=a, p'=v // fall down
      |    until_0.01 p<0 || a==0;
      |  p:=0; // place ball back
      |  if -v > vmin
      |  then v:=-0.5*v; // bounce
      |  else a:=0;      // stop bouncing
      |}""".stripMargin ->
      descr("Bouncing Ball (variation)", "Event-Driven (ED) example, using steps of 0.01. " +
        //"Not yet fully supported." +
        "A ball position and velocity as it bounces in the floor. " +
        "It assumes the ball stops bouncing when it reaches a minimum speed.")
  /////
  , "Fireflies 2x (ED)" ->
    "150 // maximum time in the plot" ->
    """f1 := 1; f2 := 4;
      |repeat 8 {
      |  f1'=1, f2'=1 until_0.01
      |       f1>10 || f2>10;
      |  if f1>=10 && f2<10
      |    then { f1:=0; f2:=f2+2; }
      |    else if f2>=10 && f1<10
      |         then { f2:=0;f1 :=f1 +2; }
      |         else { f1:=0; f2 :=0; }
      |}""".stripMargin ->
    descr("Fireflies 2x", "Event-Driven (ED) example. " +
      "Every firefly has an internal clock that helps it to know when to flash: " +
      "when the clock reaches a threshold the firefly flashes and the clock’s value " +
      "is reset to zero. If other fireflies are nearby then they try to synchronise " +
      "their flashes in a decentralised way." +
      "This version synchronises 2 fireflies.")
  ////

  , "Fireflies 3x (ED)" ->
    "150 // maximum time in the plot" ->
    """f1 := 1; f2 := 4; f3 := 7;
      |repeat 8 {
      |  f1'=1, f2'=1, f3'=1
      |  until_0.01 f1>10 || f2>10 || f3>10;
      |  if f1>=10 && f2<10 && f3<10
      |    then { f1:=0; f2:=f2+2; f3:=f3+2; }
      |    else if f2>=10 && f1<10 && f3 < 10
      |         then { f2:=0;f1 :=f1 +2; f3:=f3+ 2; }
      |         else { f3:=0; f1 := f1 +2; f2:= f2 +2; }
      |}""".stripMargin ->
    descr("Fireflies 3x", "Event-Driven (ED) Example. " +
      "Every firefly has an internal clock that helps it to know when to flash: " +
      "when the clock reaches a threshold the firefly flashes and the clock’s value " +
      "is reset to zero. If other fireflies are nearby then they try to synchronise " +
      "their flashes in a decentralised way." +
      "This version synchronizes 3 fireflies")

  ).map(x => List(x._1._1._1, x._1._1._2, x._1._2, x._2))


}
