/*
Program that will rotate the 28BYJ-48 motor 360 degrees
*/

#include <AccelStepper.h>
 
AccelStepper stepper(AccelStepper::FULL4WIRE, 8,9,10,11);

void setup()
{  
  stepper.setMaxSpeed(350);  //Max speed the motor can reliably spin at
  stepper.moveTo(2048); //Will set a target value of 2048 steps (motor has 2048 steps)     
}
 
void loop()
{  
  stepper.setSpeed(250);  //Must be frequently called
  stepper.runSpeedToPosition(); //Must be frequently called
}