#include <Stepper.h>
#include <Servo.h>
#include <SoftwareSerial.h>

 

//Stepper
#define STEP_STEP 8
#define STEP_SPEED 1
#define STEPS_PER_REV 2048

#define STEP_IN1 11
#define STEP_IN2 10
#define STEP_IN3 9
#define STEP_IN4 8

//Servo
#define SERVO_PIN 5
#define SERVO_STEP 1


//HC-05
#define BT_RX 7
#define BT_TX 6

Stepper stepper(STEPS_PER_REV, STEP_IN4, STEP_IN2, STEP_IN3, STEP_IN1); 
Servo servo;
SoftwareSerial BTSerial(BT_RX, BT_TX); 
 
int servoAngle = 80;


void setup()
{
  Serial.begin(9600);
  BTSerial.begin(38400);
  stepper.setSpeed(STEP_STEP);
  servo.attach(SERVO_PIN);
  servo.write(servoAngle);
}
 
void loop()
{  
  if(BTSerial.available()) {
    char input = BTSerial.read();
    //Serial.println(input);
    switch (input) {
      case 'r':
        stepper.step(STEP_STEP);
        break;

      case 'l':
        stepper.step(-STEP_STEP);
        break;
      
      case 'u':
        if (servoAngle + SERVO_STEP < 180) {
          servoAngle += SERVO_STEP;
          servo.write(servoAngle);
        }
        break;
      
      case 'd':
        if (servoAngle - SERVO_STEP > 80) {
          servoAngle -= SERVO_STEP;
          Serial.println(servoAngle);
          servo.write(servoAngle);
        }
        break;      
    }
    return 0;
  }
}
 
