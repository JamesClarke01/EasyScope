// Adafruit Motor shield library
// copyright Adafruit Industries LLC, 2009
// this code is public domain, enjoy!

#include <AFMotor.h>
#include <Servo.h>

#define REV_STEPS 2048

int alt = 0;

// to motor port #2 (M3 and M4)
AF_Stepper motor(REV_STEPS, 2);
Servo leftServo, rightServo;

void moveLeftServo(int pAlt) {
  //for C577 servo
  //left range: 90 -> 0
  leftServo.write(map(pAlt, 0, 90, 90, 0));
}

void moveRightServo(int pAlt) {
  //for C577 servo
  //right range: 90 -> 180
  rightServo.write(map(pAlt, 0, 90, 90, 180));
}

void moveAlt(int pAlt) {
  int leftAngle, rightAngle;
  const int increment = 10;

  if (pAlt >= 0 && pAlt <= 90) {                      
    
    //Move each servo in steps to keep the two in sync
    if (pAlt > alt) {
                
      for(int i = increment; (alt + i) <= pAlt; i += increment) {                         
        moveLeftServo(alt+i);
        moveRightServo(alt+i);
        delay(100);
      }
    } else if (pAlt < alt) {          
      for(int i = increment; alt - i >= pAlt; i += increment) {
        moveLeftServo(alt-i);
        moveRightServo(alt-i);
        delay(100);
      } 
    }

    //Move the remainder
    moveLeftServo(pAlt);
    moveRightServo(pAlt);
    
    alt = pAlt; //update current alt
  }
}

void setup() {
  Serial.begin(9600);           // set up Serial library at 9600 bps
  Serial.println("Stepper test!");

  motor.setSpeed(10);  // 10 rpm   

  leftServo.attach(10);
  rightServo.attach(9);
}

void test1() {
  motor.step(REV_STEPS/2, FORWARD, INTERLEAVE); 
  delay(200);
  moveAlt(15);
  delay(1000);
  moveAlt(45);
  delay(1000);
  moveAlt(15);
  delay(1000);
  motor.step(REV_STEPS/2, BACKWARD, INTERLEAVE); 
  delay(200);
  moveAlt(15);
  delay(1000);
  moveAlt(45);
  delay(1000);
  moveAlt(15);
  delay(1000);
}

void fullRotation() {
  motor.step(REV_STEPS*2, FORWARD, INTERLEAVE); 
  delay(1000);
  motor.step(REV_STEPS*2, BACKWARD, INTERLEAVE); 
  delay(1000);
}

void loop() {

  //fullRotation();  
  test1();



}
