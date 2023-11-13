#include <Stepper.h>
#include <SoftwareSerial.h>
#include <Servo.h>;
 
/*
PINS
Stepper:
IN1: 11
IN2: 10
IN3: 9
IN4: 8

Servo:
pin 5

HC-05
RX: 6
TX: 7
*/


#define stepsPerRevolution 2048
#define ALT_SERVO_PIN 5
 
Stepper stepper(stepsPerRevolution, 8, 10, 9, 11); 
SoftwareSerial BTSerial(7, 6); //RX, TX
Servo altServo;
 
int direction = 1;
int position = 0; //North
int alt = 0;

void setup()
{
  Serial.begin(9600);
  BTSerial.begin(38400);
  stepper.setSpeed(10);
  altServo.attach(ALT_SERVO_PIN);
}
 
void loop()
{  
  if(BTSerial.available()) {
    char input = BTSerial.read();
    Serial.println(input);
    switch (input) {
      case 'n':
        Serial.println("North");
        
        stepper.step((0 - position) * 512);
        position = 0;
        break;

      case 's':
        Serial.println("East");
        stepper.step((1 - position) * 512);
        position = 1;
        break;

      case 'e':
        Serial.println("South");
        stepper.step((2 - position) * 512);
        position = 2;
        break;

      case 'w':
        Serial.println("West");
        stepper.step((3 - position) * 512);
        position = 3;
        break;
      
      case 'u':
        Serial.println("Up");
        if (alt+30 < 180) {
          alt += 30;
          altServo.write(alt);
        }
        break;

      case 'd':
        Serial.println("Down");
        if (alt-30 > 0) {
          alt -= 30;
          altServo.write(alt);
        } 
        break;

      default:
        Serial.println('Invalid');
        break;
    }

    return 0;
  }
}
 
