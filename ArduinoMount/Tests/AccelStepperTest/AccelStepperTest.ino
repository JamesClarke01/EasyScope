/*
Program that will rotate the 28BYJ-48 motor 360 degrees
*/

#include <AccelStepper.h>
#include <SoftwareSerial.h>
 

AccelStepper stepper(AccelStepper::FULL4WIRE, 8,9,10,11);

void setup()
{  
  stepper.setMaxSpeed(500);  //Max speed the motor can reliably spin at
  //stepper.moveTo(2048); //Will set a target value of 2048 steps (motor has 2048 steps) 

  Serial.begin(9600);

  moveMotor(1024);
  delay(1000);
  moveMotor(512);
}
 
void loop()
{  
  /*
  stepper.setSpeed(300);  //Must be frequently called
  //stepper.runSpeed();
  stepper.runSpeedToPosition(); //Must be frequently called
  */
}

void moveMotor(int pPosition) {

  stepper.moveTo(pPosition);

  while (stepper.distanceToGo() != 0) {  
    
    
    stepper.setSpeed(300);
   
    stepper.runSpeedToPosition();
  }    

}
