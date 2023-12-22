package common.widgets.Lince

import common.widgets.{ButtonsBox, Setable}

class LinceExamplesBox(reload: => Unit, inputBox: Setable[String], descr: Setable[String], bounds: Setable[String])
  extends ButtonsBox(reload, List(bounds,inputBox,descr)){

  override protected val buttons: Seq[List[String]] = Seq(
    "Basic composition" ->  "150 // maximum time in the plot" ->
      """v:=0; v'=1 for 2; v'=3 for 2;""" ->
      "Very simple example composing two basic atomic elements."
    ,"Numerical derivative" ->
      "50 // maximum time in the plot" ->
      """// Initial values
y:=0;
dy:=0;
dyi:=0;
yi:=y;
yi1:=yi;
yi2:=yi1;

h:=0.1; //sampling time
aux:=0; 

repeat 100 {
yi2:=yi1;
yi1:=yi;
yi:=y;
if (aux>=2) then { 
dyi:=(3*yi-4*yi1+yi2)/(2*h);
y'=dy, dy'=2 for h; 
aux:=aux+1;} 
else {
  if (aux==0) then {
  y'=dy,dy'=2 for h; 
  aux:=aux+1;} 
  else {
  dyi:=(yi-yi1)/(h);
  y'=dy, dy'=2 for h; 
  aux:=aux+1;}
  }
}""" ->
("Numerical derivative at 3 points.")
////

 ,"Numerical integral" ->
      "50 // maximum time in the plot" ->
      """//Initial values
y:=0;
dy:=0;
inty:=0;
yi1:=y;
yi2:=yi1;

h:=0.1; //Sampling time
aux:=0;

repeat 100 {
yi2:=yi1;
yi1:=y;
if (aux>=1) then { 
  inty:=inty+(h/2)*(yi1+yi2);
  y'=dy,dy'=2 for h; 
  aux:=aux+1;} 
else {
  y'=dy,dy'=2 for h; 
  aux:=aux+1;
  }
}
""" ->
("Numerical integral based on the compound trapezoidal rule.")

    ,"Cruise control"->
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
        descr("Cruise Control","Maintain a velocity of 10, updating every time unit.")
      ////
      ,"Adaptive cruise control" ->
      "60 // maximum time in the plot" ->
      """// Adaptive cruise control
p:=0; v:=0; aA:=5; aT:=-2; pl:=50; vl:=10;aL:=0; sampling_time:=1;
while (true) do{
  if ((p + v*sampling_time + aA/2*sampling_time^2 < pl + vl*sampling_time+aL/2*sampling_time^2) && (((v-vl + (aA-aL)*sampling_time)^2 - 4*(p-pl + (v-vl)*sampling_time + (aA-aL)/2*sampling_time^2)*(aT-aL)/2 ) <0))
  then p'=v,v'=aA,pl'=vl,vl'=aL for sampling_time;
  else p'=v,v'=aT,pl'=vl,vl'=aL for sampling_time;
}""" ->
descr("Adaptive Cruise Control","Maintain the distance to the car in front")
////
      ,"Automatic braking system" ->
      "8 // maximum time in the plot" ->
      """// Automatic braking system
p:=0; v:=17.5; reaction_time:=0.001; pl:=50; vl:=0; aT:=-9.8; aA:=6;sampling_time:=0.1;
while v>0 do{
  // Initially the system detects if there is a possibility of a collision occurring
  if ((p + v*sampling_time + aA/2*sampling_time^2 < pl+vl*sampling_time ) && (((v+aA*sampling_time-vl)^2 - 4*(p+v*sampling_time+aA/2*(sampling_time^2)-(pl +vl*sampling_time))*(aT/2)) <0))
  then p'=v,v'=aA for sampling_time;// if not, the car mantains your movement
  else {  
          // if yes:
          p'=v,v'=0 for reaction_time; // time needed for the system to start braking
          p'=v,v'=aT until_0.001 (v<=0);} // slow the car until it stops
          
}""" ->
("This  program checks every 0.1 seconds if there are the possibility of occurring a collision between the car and the object at 50 meters.\n"+
"If not exist the possibility of occurring a collision the car travel with aA acceleration, if yes, the car maintains the movement during ‘reaction_time’ seconds (time needed for the system to start braking) and then brakes the car with an acceleration of aT until it stops.\n\n"+

"If not exist this system of automatic braking, the ‘reaction_time’ must be 0.3 seconds (the average time needed for a healthy human to react varies between 0.15 and 0.45 seconds) and the collision occurs (try yourself !).")
////
 ,"Autonomous driving with fixed reference" ->
      "50 // maximum time in the plot" ->
      """//----- Autonomous driving ----- Fixed reference

// Initial position and velocity of the vehicle
p:=0; v:=17.5; 
// Braking and accelerating acceleration of the vehicle
aT:=-9.8; aA:=6;

// Initial position, velocity and accelaration of the reference 
pl:=50; vl:=0; al:=0;

// Reaction time
reaction_time:=0.0001;

// Sampling time
sampling_time:=0.1;


while (v>0) do{

  // Initially the system detects if there is a possibility of a collision occurring
  if ((p + v*sampling_time + aA/2*sampling_time^2 < pl+vl*sampling_time+al/2*sampling_time^2 ) && (((v+aA*sampling_time-vl-al*sampling_time)^2 - 4*(p+v*sampling_time+aA/2*(sampling_time^2)-(pl +vl*sampling_time+al/2*(sampling_time^2)))*(aT/2-al/2)) <0))
  then {  
          // If not, the car maintains its movement because of the reaction time of the automatic acelaration system and the reference accelarates with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=0,pl'=vl,vl'=al for reaction_time;
          //Then, the car accelarates with accelaration ‘aA’ and velocity ‘v’, and the reference with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=aA,pl'=vl,vl'=al for sampling_time; 
        }
  else {  
         // If yes, the car maintains its movement because of the reaction time of the automatic braking system and the reference accelarates with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=0,pl'=vl,vl'=al for reaction_time; 

        // Then, the car bracks with acelaration ‘aT’ and velocity ‘v’, and the reference accelarates with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=aT,pl'=vl,vl'=al until_0.001 (v<=0); 
          
}
}""" ->
("Nowadays there are several vehicles that can drive autonomously. This type of system retains a set of information that, depending on its values, will regulate the actuators required for the vehicle to move at the correct position/speed.\n"+

"Based on autonomous driving, the goal of these hybrid programs is to model the position of the vehicle so that it is as close as possible to the reference position (but without exceeding it). In turn, the reference may indicate several cases, such as a fixed obstacle, a moving vehicle or simply the position that the vehicle needs to obtain over time for autonomous driving to be successfully performed.\n"+

"However, the hybrid program in this example aims to approximate the vehicle's position in relation to a fixed/stopped reference, which basically simulates its ability to brake automatically without colliding with the reference. Thus, we began by establishing the initial values of the variables used in this program (where the initial speed and acceleration of the reference are zero), then created a cycle was then created which initially checks whether the vehicle position is less than the reference position at the end of the sampling time (the time the system takes to retain the new data from the vehicle and the reference) if the vehicle accelerates with acceleration 'aA' and checks that the binomial discriminant of the equation (p+v*sampling_time+aA/2* (sampling_time^2))+(v+aA*sampling_time)*t+aT/2*(t^2)=(pl+vl*sampling_time+al/2* (sampling_time^2))+(vl+al*sampling_time)*t+al/2*(t^2) is less than zero (no solutions). For the first condition, it was sufficient to check that the equation of motion of the vehicle position at the end of the sampling time (if it accelerates) was less than the equation of motion of the reference position at the end of the same time interval: p + v*sampling_time + aA/2*sampling_time^2 < pl+vl*sampling_time+al/2*sampling_time^2, for the second condition the equations of motion of the vehicle position (in case of braking) and of the reference were equalized, where the initial position and velocity of the vehicle corresponds to its equations of motion at the end of the sampling time if the vehicle accelerates and the initial position and velocity of the reference corresponds to its equations of motion at the end of the same time interval, from that equation the respective binomial discriminant was taken to verify if it was less than zero, that is if the position of the vehicle would never intercept the reference's position even if in the following time interval it starts braking: (v+aA*sampling_time-vl-al*sampling_time)^2 - 4*(p+v*sampling_time+aA/2*(sampling_time^2)-(pl +vl*sampling_time+al/2*(sampling_time^2))<0.\n"+

"If both checks are true, then it means that the vehicle can accelerate during the sampling time without intercepting positions. To do this we initially run the system of differential equations: p'=v,v'=0,pl'=vl,vl'=al for reaction_time, which causes the vehicle to keep its speed constant and the reference to accelerate with acceleration 'al' (which in this example is zero, along with her speed) during the reaction time 'reaction_time'. This system of differential equations is to try and portray what would happen to the vehicle and the reference during the time it takes the system to react.After running the previous system of differential equations, it is necessary to run the system of differential equations that simulates the dynamics of the vehicle if it accelerates with acceleration 'aA' and of the reference if it accelerates with acceleration 'al' (which is zero) during the 'sampling_time': p'=v,v'=aA,pl'=vl,vl'=al for sampling_time;\n"+

"If one or both conditions are false, then it means that the position of the vehicle has intersected the position of the reference at the end of the sampling time, or even if it does not intersect at the end of that time, it will eventually collide in the following instants even if braking with acceleration 'aT' is performed by the vehicle. In the same way as before, the system of differential equations concerning the reaction time of the system is run: p'=v,v'=0,pl'=vl,vl'=al for reaction_time, and then run the system of differential equations that simulates the dynamics of the vehicle if it slows down with acceleration 'aT' and of the reference if it accelerates with acceleration 'al' (which is zero) until the vehicle speed is zero (vehicle stopped): p'=v,v'=aT,pl'=vl,vl'=al until_0. 001 (v<=0);\n"+ 

"If we run this example we can verify that the position of the vehicle does not intersect with the reference, but if we change the value of the reaction time to 0.1 (reaction time of a healthy human) the positions will intersect, showing the efficiency of autonomous systems compared to manuals in these circumstances.")
////

      ,"Autonomous driving with constant velocity reference" ->
      "20 // maximum time in the plot" ->
      """//----- Autonomous driving ----- Constant velocity reference

// Initial position and velocity of the vehicle
p:=0; v:=17.5; 
// Braking and accelerating acceleration of the vehicle
aT:=-9.8; aA:=6;

// Initial position, velocity and accelaration of the reference 
pl:=50; vl:=10; al:=0;

// Reaction time
reaction_time:=0.0001;

// Sampling time
sampling_time:=0.1;


while true do{

  // Initially the system detects if there is a possibility of a collision occurring
  if ((p + v*sampling_time + aA/2*sampling_time^2 < pl+vl*sampling_time+al/2*sampling_time^2 ) && (((v+aA*sampling_time-vl-al*sampling_time)^2 - 4*(p+v*sampling_time+aA/2*(sampling_time^2)-(pl +vl*sampling_time+al/2*(sampling_time^2)))*(aT/2-al/2)) <0))
  then {  
          // If not, the car maintains its movement because of the reaction time of the automatic acelaration system and the reference accelarates with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=0,pl'=vl,vl'=al for reaction_time;
          
          //Then, the car accelarates with accelaration ‘aA’ and velocity ‘v’, and the reference with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=aA,pl'=vl,vl'=al for sampling_time; 
        }
  else {  
         // If yes, the car maintains its movement because of the reaction time of the automatic braking system and the reference accelarates with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=0,pl'=vl,vl'=al for reaction_time; 
          
        // Then, the car bracks with acelaration ‘aT’ and velocity ‘v’, and the reference accelarates with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=aT,pl'=vl,vl'=al for sampling_time; 
          
}
}""" ->
("Nowadays there are several vehicles that can drive autonomously. This type of system retains a set of information that depending on its values will regulate the actuators required for the vehicle to move at the correct position/speed.\n"+

"Based on autonomous driving, the goal of these hybrid programs is to model the position of the vehicle so that it is as close as possible to the reference position (but without exceeding it). In turn, the reference may indicate several cases, such as a stationary obstacle, a moving vehicle or simply the position that the vehicle needs to obtain over time for autonomous driving to be successfully performed.\n"+

"However, the hybrid program in this example aims to approximate the position of the vehicle in relation to a reference moving at a constant speed, which basically simulates the ability of the vehicle to follow a given trajectory. Thus, we began by establishing the initial values of the variables used in this program (where the initial acceleration of the reference is zero), then created a cycle was then created which initially checks whether the vehicle position is less than the reference position at the end of the sampling time (the time the system takes to retain the new data from the vehicle and the reference) if the vehicle accelerates with acceleration 'aA' and checks that the binomial discriminant of the equation (p+v*sampling_time+aA/2* (sampling_time^2))+(v+aA*sampling_time)*t+aT/2*(t^2)=(pl+vl*sampling_time+al/2* (sampling_time^2))+(vl+al*sampling_time)*t+al/2*(t^2) is less than zero (no solutions). For the first condition, it was sufficient to check that the equation of motion of the vehicle position at the end of the sampling time (if it accelerates) was less than the equation of motion of the reference position at the end of the same time interval: p + v*sampling_time + aA/2*sampling_time^2 < pl+vl*sampling_time+al/2*sampling_time^2, for the second condition the equations of motion of the vehicle position (in case of braking) and of the reference were equalized, where the initial position and velocity of the vehicle corresponds to its equations of motion at the end of the sampling time if the vehicle accelerates and the initial position and velocity of the reference corresponds to its equations of motion at the end of the same time interval, from that equation the respective binomial discriminant was removed to verify if it was less than zero, that is if the position of the vehicle would never intercept the reference's position even if in the following time interval it starts braking: (v+aA*sampling_time-vl-al*sampling_time)^2 - 4*(p+v*sampling_time+aA/2*(sampling_time^2)-(pl +vl*sampling_time+al/2*(sampling_time^2))<0.\n"+

"If both checks are true, then it means that the vehicle can accelerate during the sampling time without intercepting positions. To do this we initially run the system of differential equations: p'=v,v'=0,pl'=vl,vl'=al for reaction_time, which causes the vehicle to keep its speed constant and the reference to accelerate with acceleration 'al' (which in this example is zero) during the 'reaction_time'. This system of differential equations is used to try to portray what would happen to the vehicle and the reference during the time it takes the system to react.After running the previous system of differential equations, it is necessary to run the system of differential equations that simulates the dynamics of the vehicle if it accelerates with acceleration 'aA' and of the reference if it accelerates with acceleration 'al' (which is zero) during the sampling_time: p'=v,v'=aA,pl'=vl,vl'=al for sampling_time;\n"+

"If one or both conditions are false, then it means that the position of the vehicle has intersected the position of the reference at the end of the sampling time, or even if it does not intersect at the end of that time, it will eventually collide in the following instants even if braking with acceleration 'aT' is performed by the vehicle. In the same way as before, the system of differential equations concerning the reaction time of the system is run: p'=v,v'=0,pl'=vl,vl'=al for reaction_time, and then we run the system of differential equations that simulates the dynamics of the vehicle if it slows down with acceleration 'aT' and of the reference if it accelerates with acceleration 'al' (which is zero) during the sampling time: p'=v,v'=aT,pl'=vl,vl'=al for sampling_time;\n"+ 

"If we run this example we can verify that the position of the vehicle does not intersect with the position of the reference, but if we change the value of the reaction time to 0.1 (reaction time of a healthy human) the positions will intersect, showing the efficiency of autonomous systems against the manual ones in these circumstances.")
////

      
,"Autonomous vehicle with constant aceleration reference" ->
      "20 // maximum time in the plot" ->
      """//----- Autonomous driving ----- Constante aceleration reference

// Initial position and velocity of the vehicle
p:=0; v:=17.5; 
// Braking and accelerating acceleration of the vehicle
aT:=-9.8; aA:=6;

// Initial position, velocity and accelaration of the reference 
pl:=50; vl:=10; al:=2;

// Reaction time
reaction_time:=0.0001;

// Sampling time
sampling_time:=0.1;


while true do{

  // Initially the system detects if there is a possibility of a collision occurring
  if ((p + v*sampling_time + aA/2*sampling_time^2 < pl+vl*sampling_time+al/2*sampling_time^2 ) && (((v+aA*sampling_time-vl-al*sampling_time)^2 - 4*(p+v*sampling_time+aA/2*(sampling_time^2)-(pl +vl*sampling_time+al/2*(sampling_time^2)))*(aT/2-al/2)) <0))
  then {  
          // If not, the car maintains its movement because of the reaction time of the automatic acelaration system and the reference accelarates with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=0,pl'=vl,vl'=al for reaction_time;
          
          //Then, the car accelarates with accelaration ‘aA’ and velocity ‘v’, and the reference with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=aA,pl'=vl,vl'=al for sampling_time; 
        }
  else {  
         // If yes, the car maintains its movement because of the reaction time of the automatic braking system and the reference accelarates with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=0,pl'=vl,vl'=al for reaction_time; 
          
        // Then, the car bracks with acelaration ‘aT’ and velocity ‘v’, and the reference accelarates with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=aT,pl'=vl,vl'=al for sampling_time; 
          
}
}
""" ->
("Nowadays there are several vehicles that can drive autonomously. This type of system retains a set of information that depending on its values will regulate the actuators required for the vehicle to move at the correct position/speed.\n"+

"Based on autonomous driving, the goal of these hybrid programs is to model the position of the vehicle so that it is as close as possible to the reference position (but without exceeding it). In turn, the reference may indicate several cases, such as a stationary obstacle, a moving vehicle or simply the position that the vehicle needs to obtain over time for autonomous driving to be successfully performed.\n"+

"However, the hybrid program in this example aims to approximate the position of the vehicle against a reference that moves at a constant acceleration, which basically simulates the ability of the vehicle to follow a given trajectory. So, we began by establishing the initial values of the variables used in this program, then created Then a cycle was created, which initially checks whether the position of the vehicle is less than the reference at the end of the sampling time (time that the system takes to retain the new data from the vehicle and the reference) if the vehicle accelerates with acceleration 'aA' and checks that the binomial discriminant of the equation (p+v*sampling_time+aA/2* (sampling_time^2))+(v+aA*sampling_time)*t+aT/2*(t^2)=(pl+vl*sampling_time+al/2* (sampling_time^2))+(vl+al*sampling_time)*t+al/2*(t^2) is less than zero (no solutions). For the first condition, it was sufficient to check that the equation of motion of the vehicle position at the end of the sampling time (if it accelerates) was less than the equation of motion of the reference position at the end of the same time interval: p + v*sampling_time + aA/2*sampling_time^2 < pl+vl*sampling_time+al/2*sampling_time^2, for the second condition the equations of motion of the vehicle position (in case of braking) and of the reference were equalized, where the initial position and velocity of the vehicle corresponds to its equations of motion at the end of the sampling time if the vehicle accelerates and the initial position and velocity of the reference corresponds to its equations of motion at the end of the same time interval, from that equation the respective binomial discriminant was removed to verify if it was less than zero, that is if the position of the vehicle would never intercept the reference's position even if in the following time interval it starts braking: (v+aA*sampling_time-vl-al*sampling_time)^2 - 4*(p+v*sampling_time+aA/2*(sampling_time^2)-(pl +vl*sampling_time+al/2*(sampling_time^2))<0.\n"+

"If both checks are true, then it means that the vehicle can accelerate during the sampling time without intercepting positions. To do this we initially run the system of differential equations: p'=v,v'=0,pl'=vl,vl'=al for reaction_time, which causes the vehicle to keep its speed constant and the reference to accelerate with acceleration 'al' during 'reaction_time'. This system of differential equations is to try and portray what would happen to the vehicle and the reference during the time it takes the system to react.After running the previous system of differential equations, it is necessary to run the system of differential equations that simulates the dynamics of the vehicle if it accelerates with acceleration 'aA' and of the reference if it accelerates with acceleration 'al' during the sampling_time: p'=v,v'=aA,pl'=vl,vl'=al for sampling_time;\n"+

"If one or both conditions are false, then it means that the position of the vehicle has intersected the position of the reference at the end of the sampling time, or even if it does not intersect at the end of that time, it will eventually collide in the following instants even if braking with acceleration 'aT' is performed by the vehicle. As before, we run the system of differential equations relating to the reaction time of the system: p'=v,v'=0,pl'=vl,vl'=al for reaction_time, and then we run the system of differential equations which simulates the dynamics of the vehicle if it slows down with acceleration 'aT' and of the reference if it accelerates with acceleration 'al' during the sampling time: p'=v,v'=aT,pl'=vl,vl'=al for sampling_time;\n"+ 

"If we run this example we can verify that the position of the vehicle does not intersect with the position of the reference, but if we change the value of the reaction time to 0.1 (reaction time of a healthy human) the positions will intersect, showing the efficiency of autonomous systems against the manual ones in these circumstances.")
////

      

,"Autonomous vehicle with constant aceleration reference and uncertainties" ->
      "10 // maximum time in the plot" ->
      """//----- Autonomous driving ----- Constante aceleration reference and uncertainties

// Initial position and velocity of the vehicle
p:=0; v:=17.5; 
// Braking and accelerating acceleration of the vehicle
aT:=-9.8; aA:=6;

// Initial position, velocity and accelaration of the reference 
pl:=50; vl:=10; al:=2;

// Reaction time
reaction_time:=0.0001;

// Sampling time
sampling_time:=0.1;

//pattern deviation
error:=1;

while true do{

  // Initially the system detects if there is a possibility of a collision occurring
  if ((p + v*sampling_time + aA/2*sampling_time^2 < (pl+error)+(vl+error)*sampling_time+(al+error)/2*sampling_time^2 ) && (((v+aA*sampling_time-(vl+error)-(al+error)*sampling_time)^2 - 4*(p+v*sampling_time+aA/2*(sampling_time^2)-((pl+error) +(vl+error)*sampling_time+(al+error)/2*(sampling_time^2)))*(aT/2-(al+error)/2)) <0))
  then {  
          // If not, the car maintains its movement because of the reaction time of the automatic acelaration system and the reference accelarates with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=0,pl'=vl,vl'=al for reaction_time;
          
          //Then, the car accelarates with accelaration ‘aA’ and velocity ‘v’, and the reference with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=aA,pl'=vl,vl'=al for sampling_time; 
        }
  else {  
         // If yes, the car maintains its movement because of the reaction time of the automatic braking system and the reference accelarates with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=0,pl'=vl,vl'=al for reaction_time; 
          
        // Then, the car bracks with acelaration ‘aT’ and velocity ‘v’, and the reference accelarates with accelaration ‘al’ and velocity ‘vl’
          p'=v,v'=aT,pl'=vl,vl'=al for sampling_time; 
          
}
}

""" ->
("This program is the same as the hybrid program of the example 'Autonomous vehicle with constant acceleration reference', but in the verification conditions, the acceleration, velocity and position of the reference have a positive deviation of 1 unit, resulting in the vehicle position intersecting the reference position. This situation portrays the impact of sensor inaccuracy, i.e., in real life, the sensors responsible for detecting the position, velocity and acceleration of the reference present deviations from the real value, which can completely condemn the designed systems. Due to this reality, it is necessary to adapt the systems to support some imprecision by the sensors involved and use sensors with high precision so that the system behaves as desired and use sensors with high precision.")
////
    ,"Projetc motion without air effect" ->
      "20 // maximum time in the plot" ->
      """// Projetc motion without air effect
theta:=pi()/2; // angle
v0:=50; //magnitude of initial velocity
x:=0; // initial value of x coordinate
vx:=v0*cos(theta); // initial velocity of x coordinate
y:=0; //initial value of y coordinate
vy:=v0*sin(theta); // initial velocity of y coordinate
g:=-9.8; // acceleration of gravity

x'=vx,y'=vy,vy'=g until_0.01 (y<0);""" ->
("In this example, a ball is launched at the position (0,0) with an initial velocity of (v0*cos(theta),v0*sin(theta)) and an angle of theta.\n"+ 

"Using the equations of motion of the kinematics through differential equations and the initial conditions mentioned in the previous paragraph, it was possible to simulate the variation of the x-coordinate and the y-coordinate over time.")
////
      ,"Damped Harmonic Oscillator" ->
      "30 // maximum time in the plot" ->
      """//Damped harmonic oscillator in subcritical regime (lambda/2<w0)-->xsc
//Damped harmonic oscillator in supercritical regime (lambda/2>w0)-->xSc
//Damped harmonic oscillator in critical regime (lambda/2=w0)-->xc

m:=1; // mass of the object
k:=2.32; // Spring constant

// Constants and initial values of the subcritical regime
b_sc:=0.6; // Damping constant in the subcritical regime
w0_sc:=sqrt(k/m);
w0_sc2:=pow(w0_sc,2);
lambda_sc:=b_sc/m;
xsc:=2; //Initial position
vsc:=0; //Initial velocity

// Constants and initial values of the supercritical regime
b_Sc:=3.5; //Damping constant in the supercritical regime
w0_Sc:=sqrt(k/m);
w0_Sc2:=pow(w0_Sc,2);
lambda_Sc:=b_Sc/m;
xSc:=2; //Initial position
vSc:=0; //Initial velocity

// Constants and initial values of the critical regime
w0_c:=sqrt(k/m);
w0_c2:=pow(w0_c,2);
b_c:=w0_c*2; //Damping constant in the critical regime
lambda_c:=b_c/m;
xc:=2; //Initial position
vc:=0; //Initial velocity

// Diferential equations
xsc'=vsc,vsc'=-xsc*w0_sc2-vsc*lambda_sc,
xSc'=vSc,vSc'=-xSc*w0_Sc2-vSc*lambda_Sc,
xc'=vc,vc'=-xc*w0_c2-vc*lambda_c for 20;""" ->
("Damped hamornic oscillator represented in three regimes: subcritical, supercritical and critical.\n"+
"In the subcritical regime k=2.32 N/m, m=1kg, b=0.6 N.s/m, w0=sqrt(k/m)=sqrt(58)/5 and lambda=b/m=0.6\n"+
"In the supercritical regime k=2.32 N/m, m=1kg, b=3.5 N.s/m, w0=sqrt(k/m)=sqrt(58)/5 and lambda=b/m=3.5\n"+
"At the critical regime k=2.32 N/m, m=1kg, b=(sqrt(58)/5)*2 N.s/m, w0=sqrt(k/m)=sqrt(58)/5 and lambda=b/m=(sqrt(58)/5)*2 ")
      ////
      ,"Series RLC circuit" ->
      "30 // maximum time in the plot" ->
      """r_rac:=2;
r_rsa:=0.5;
r_rSa:=4;

l:=0.047;
c:=0.047;

//Initial conditions of the  critically damped system
vc_rac:=0;
dvc_rac:=0;

//Initial conditions of the  under damped system
vc_rsa:=0;
dvc_rsa:=0;

//Initial conditions of the  over damped system
vc_rSa:=0;
dvc_rSa:=0;

//Initial conditions of the voltage source
vs:=10;


// This repeat instruction is used for charge the capacitor up to 10V, for 0.002 seconds
repeat 2 {
vc_rac'=dvc_rac, dvc_rac'=-(dvc_rac*r_rac*(l)^(-1))-vc_rac*(l*c)^(-1)+vs*(l*c)^(-1),
vc_rsa'=dvc_rsa, dvc_rsa'=-(dvc_rsa*r_rsa*(l)^(-1))-vc_rsa*(l*c)^(-1)+vs*(l*c)^(-1),
vc_rSa'=dvc_rSa, dvc_rSa'=-(dvc_rSa*r_rSa*(l)^(-1))-vc_rSa*(l*c)^(-1)+vs*(l*c)^(-1)
for 0.3;
}

//Power off the voltage source
vs:=0;

// This repeat instruction is used to discharge the capacitor
repeat 2 {
vc_rac'=dvc_rac, dvc_rac'=-(dvc_rac*r_rac*(l)^(-1))-vc_rac*(l*c)^(-1)+vs*(l*c)^(-1),
vc_rsa'=dvc_rsa, dvc_rsa'=-(dvc_rsa*r_rsa*(l)^(-1))-vc_rsa*(l*c)^(-1)+vs*(l*c)^(-1),
vc_rSa'=dvc_rSa, dvc_rSa'=-(dvc_rSa*r_rSa*(l)^(-1))-vc_rSa*(l*c)^(-1)+vs*(l*c)^(-1)
for 0.3;
}""" ->
("This example shows the variation of voltage at the capacitor in 3 regimes in a series RLC circuit.\n"+


"To determine the differential equation governing the voltage variation in the capacitor, it was necessary to use Kirchoff's law and Ohm's law. We know from Kirchoff's law that vs=vr+vl+vc, where 'vs' is the source voltage, 'vr' is the resistance voltage, 'vl' is the bobbin voltage and 'vc' is the capacitor voltage. Already by Ohm's law, we know that vr=R*ir (where 'R' is the value of the resistance and 'ir' is the current passing through it), that ic=C*dvc/dt (where 'C' is the capacitance of the capacitor and 'dvc/dt' is the time derivative of the capacitor voltage), vl=L*dil/dt (where L is the inductance of the bobbin and dil/dt is the derivative of the current passing through it) and ic=il=ir=i since it is a series circuit. Having these equations we obtained : vs=R*i+Ldi/dt+vc <=> vs=RC(dvc/dt )+LC(d²vc/dt²)+vc <=> d²vc/dt²=-(R/L)dvc/dt - vc/(LC)+vs/(LC) which is the differential equation we wanted to obtain.\n"+

"Already with the differential equation determined, it was established the value of capacitance for C=47mF and the value of inductance for L=47mH, already the value of resistance 'R' varied according to the regime. Let alpha=R/2L and w0=1/sqrt(LC), the critical damping regime happens when alpha=w0, the under-damping regime happens when alpha<w0 and the over-damping regime happens when alpha>w0. To analyse how the capacitor voltage varies with the damping regime, we defined the variable 'vc_rac' for the critical damping regime (R=2 Ohm), the variable 'vc_rsa' for the under-damping regime (R=0.5 Ohm) and the variable 'vc_rSa' for the over-damping regime (R=4 Ohms).\n"+


"To simulate the variation of the capacitor voltage in the 3 regimes mentioned above, we switch on the voltage source for 0.6 seconds until the capacitor charges up to 10V (voltage of the voltage source) and then switch off the voltage source for the next 0.6 seconds so that the capacitor discharges.\n"+

"NOTE: We recommend that you disable all variables in the plot except 'vc_rac', 'vc_rsa' and 'vc_rSa'.")
////
      ,"Water tanks" ->
      "150 // maximum time in the plot" ->
      """a1:=1; //Area of tank 1
a2:=1; // Area of tank 2
r1:=1; //Resistance applied to the water flow at the water exit tap of tank 1.
r2:=10; //Resistance applied to the water flow at the water exit tap of tank 2.
h1_p:=10; // initial water level of tank 1 in the aligned  configuration
h2_p:=0; // initial water level of tank 2 in the aligned  configuration.
h1_v:=10; //initial water level of tank 1 in vertical configuration.
h2_v:=0; //initial water level of tank 2 in vertical  configuration.



// Open tap of the tank 1 and close the tap of the tank 2
qe1:=1;
qe2:=0;

// Differential equations simulating the variation of the water level in the two tanks, in each configuration, after 50 seconds.
h1_p'=-pow(a1*r1,-1)*h1_p+pow(a1*r1,-1)*h2_p+pow(a1,-1)*qe1,
h2_p'=pow(a2*r1,-1)*h1_p-pow(a2*r1,-1)*h2_p+pow(a2,-1)*qe2-pow(a2*r2,-1)*h2_p,
h1_v'=-pow(a1*r1,-1)*h1_v+pow(a1,-1)*qe1,
h2_v'=pow(a2*r1,-1)*h1_v-pow(r2*a2,-1)*h2_v + pow(a2,-1)*qe2 for 40;


// Open tap of the tank 2 and close the tap of the tank 1
qe1:=0;
qe2:=1;

// Differential equations simulating the variation of the water level in the two tanks, in each configuration, after 50 seconds.
h1_p'=-pow(a1*r1,-1)*h1_p+pow(a1*r1,-1)*h2_p+pow(a1,-1)*qe1,
h2_p'=pow(a2*r1,-1)*h1_p-pow(a2*r1,-1)*h2_p+pow(a2,-1)*qe2-pow(a2*r2,-1)*h2_p,
h1_v'=-pow(a1*r1,-1)*h1_v+pow(a1,-1)*qe1,
h2_v'=pow(a2*r1,-1)*h1_v-pow(r2*a2,-1)*h2_v + pow(a2,-1)*qe2 for 40;


//Open both
qe1:=1;
qe2:=1;

// Differential equations simulating the variation of the water level in the two tanks, in each configuration, after 50 seconds.
h1_p'=-pow(a1*r1,-1)*h1_p+pow(a1*r1,-1)*h2_p+pow(a1,-1)*qe1,
h2_p'=pow(a2*r1,-1)*h1_p-pow(a2*r1,-1)*h2_p+pow(a2,-1)*qe2-pow(a2*r2,-1)*h2_p,
h1_v'=-pow(a1*r1,-1)*h1_v+pow(a1,-1)*qe1,
h2_v'=pow(a2*r1,-1)*h1_v-pow(r2*a2,-1)*h2_v + pow(a2,-1)*qe2 for 40;

//Close both
qe1:=0;
qe2:=0;

// Differential equations simulating the variation of the water level in the two tanks, in each configuration, after 50 seconds.
h1_p'=-pow(a1*r1,-1)*h1_p+pow(a1*r1,-1)*h2_p+pow(a1,-1)*qe1,
h2_p'=pow(a2*r1,-1)*h1_p-pow(a2*r1,-1)*h2_p+pow(a2,-1)*qe2-pow(a2*r2,-1)*h2_p,
h1_v'=-pow(a1*r1,-1)*h1_v+pow(a1,-1)*qe1,
h2_v'=pow(a2*r1,-1)*h1_v-pow(r2*a2,-1)*h2_v + pow(a2,-1)*qe2 for 40;""" ->
("This program has the objective of simulating the variation of the water level in tank 1 and 2, in two different configurations.\n"+

"In the vertical configuration, tank 1 is above tank 2, and in it there is a tap introducing water with a flow rate 'qe1' and another tap pouring water with a resistance to the flow rate equal to R1 (higher resistance, less water comes out). Tank 2 on its turn also has a tap introducing water with a flow rate 'qe2' and another one pouring water with a resistance to the flow rate equal to R2, however, the water that was poured from tank 1 is introduced in tank 2.\n"+

"In the aligned configuration, what differs is that the tanks are aligned and the tap that was pouring water from tank 1 is now connecting both tanks. This set up causes the water to flow through the tap of resistance R1 into the tank that contains a lower height of water.\n"+


"To construct the differential equations governing these two programmes, it was necessary to take into account that the incoming water equals the outgoing water plus the accumulated water. The accumulated water is associated to the derivative of the water volume (dV/dt) where the water volume is equal to the tank area times the water height (A*dh/dt). The outgoing water is associated to a flow resistance and to the water height: h/R (higher water height, higher the outgoing water flow; higher R, lower the outgoing water flow).In the case of the aligned configuration, the tap that connects both tanks has a flow rate of (h1-h2)/R1, as it will depend on which tank has more water height.\n"+

"With these considerations, the differential equations of the vertical configuration are:\n"+ 
"Tank 1: dh1/dt=-h1/(A1*R1)+Qe1/A1 \n"+
"Tank 2: dh2/dt= h1/(R1*A2)-h2/(R2*A2)+Qe2/A2 \n\n"+

"And the differential equations for the aligned configuration are:\n"+
"Tank 1: dh1/dt=Qe1/A1-h1/(A1*R1)+h2/(A1*R1)\n"+
"Tank 2: dh2/dt=Qe2/A_2+h1/(A2*R1)-h2/(A2*R1)-h2/(R2*A2) \n\n"+

"Having the differential equations of both configurations, a hybrid program was developed that consists in varying which of the two taps is open (qe1 and qe2) and visualizing the effect in both configurations. To do that, we started by declaring the necessary variables with their initial values, then we opened the tap that fills tank 1 (qe1) and closed the tap that fills tank 2 (qe2), letting the system evolve for 40 seconds. Then the tap that fills tank 2 (qe2) was opened and the tap that fills tank 1 (qe1) was closed, letting the system evolve for 40 seconds. The next step is to open the two taps, letting the system evolve for 40 seconds. Finally, both taps are turned off, also allowing the system to evolve for 40 seconds.\n"+

"Running the program, we can see how the height of the water evolves in both configurations. In the vertical configuration, since tank 1 pours its water into tank 2 and tank 2 loses less water than tank 1, tank 2 tends to store more water, regardless of the tap being turned on. The aligned configuration always tends to a steady state where the levels tend to be close, since they share a water outlet that allows the passage of a high water flow, facilitating the exchange of water between the tanks and causing water levels close, this proximity is also favored due to the fact that the tap that pours water to the outside of tank 2 does not allow the passage of a high flow, reducing losses to the outside")
      
   
      ,"Traffic lights"->
        "150 // maximum time in the plot" ->
        """// Alternate between two constant values.
          |l:=0;
          |repeat 4 {
          |   l:=0; wait 10 ;
          |   l:=1; wait 10 ;
          |}""".stripMargin ->
        descr("Traffic lights","Alternating between two constant values.")
      ////
      ,"Avoiding approx. error"->
        "150 // maximum time in the plot" ->
        """// A naive evaluation would give an approximation
          |// error of the if-condition.
          |x := 1;
          |x' = -x for 40;
          |x' =  x for 40;
          |if x == 1 then x:= 2;
          |          else x:= 3;""".stripMargin ->
      ( "Using approximated values, the value of x at 80 is slightly different " +
        "from 1, yielding a final value of 3 instead of 2. Using our symbolic evaluation, " +
        "Lince obtains the correct value of 2. Note that our experimental warning system, which " +
        "checks perturbations, detects that an approximation error can occur here at 80.")
      ////
      ,"Trigonometric computation"->
      "150 // maximum time in the plot" ->
      """// Solution not naively computed (precise solution involves sin/cos)
          |// Use the online version to use the precise solution.
          |p:=1;v:=1;
          |p'=v, v'=-p for 8;""".stripMargin ->
      ("When involving mutually dependent variables the naive numerical analysis does not work. " +
        "Using symbolic computations we plot precisely the functions with sin/cos.")
      ////
    ////
    ,"Naive particle positioning" ->
      "150 // maximum time in the plot" ->
      """x:= -1; v:= 0; a:= 1;
        |while true do {
        |	if x <= 0 then a:= 1; else a:=-1;
        |    	x' = v, v' = a  for 0.5;
        |}""".stripMargin ->
      descr("Moving particle", "A naive approach for moving a particle to a position x.")

    ////
    ,"Landing system" ->
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
    ,"Bouncing ball (ED)"->
    "150 // maximum time in the plot" ->
    """// Bouncing ball example
          |v:=5; p:=10; c:=0;
          |while (c<4) do {
          |  v'=-9.8, p'=v until_0.01 p<0 && v<0;
          |  v:=-0.5*v; c:=c+1;
          |}""".stripMargin ->
        descr("Bouncing Ball","Event-Driven (ED) example, using steps of 0.001. " +
    //"Not yet fully supported." +
    "A ball position and velocity as it bounces in the floor. " +
    "It includes an experimental feature: using a condition (p<0 /\\ v<0) " +
    "to end a trajectory using a naive search algorithm.")
    /////
    //    ,"Bouncing ball"->
    //       """// Bouncing ball example
    //         |v:=5; p:=10;
    //         |repeat 4 {
    //         |  v=-9.8, p=v & p<0 && v<0;
    //         |  v:=-0.5*v

    /////
    ,"Fireflies 2x (ED)"->
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
      descr("Fireflies 2x","Event-Driven (ED) example. " +
        "Every firefly has an internal clock that helps it to know when to flash: " +
        "when the clock reaches a threshold the firefly flashes and the clock’s value " +
        "is reset to zero. If other fireflies are nearby then they try to synchronise " +
        "their flashes in a decentralised way." +
        "This version synchronises 2 fireflies.")
    ////

    ,"Fireflies 3x (ED)"->
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
      descr("Fireflies 3x","Event-Driven (ED) Example. " +
        "Every firefly has an internal clock that helps it to know when to flash: " +
        "when the clock reaches a threshold the firefly flashes and the clock’s value " +
        "is reset to zero. If other fireflies are nearby then they try to synchronise " +
        "their flashes in a decentralised way." +
        "This version synchronizes 3 fireflies")

  ).map(x=>List(x._1._1._1,x._1._1._2,x._1._2,x._2))

}
