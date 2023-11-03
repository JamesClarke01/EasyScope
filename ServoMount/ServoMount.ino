// Include the servo library:
#include <Servo.h>;

Servo altServo;
Servo azServo;

#define ALT_SERVO_PIN 11
#define AZ_SERVO_PIN 13

int alt = 0;
int az = 90;

void setup() {
  Serial.begin(9600);
  
  altServo.attach(ALT_SERVO_PIN);
  altServo.write(alt);
  
  azServo.attach(AZ_SERVO_PIN);
  azServo.write(az);
}

void loop() {

  if(Serial.available()) {
    char ch = Serial.read();
    
    if(ch == 'a') {
      if (az+10 < 180) {
        az += 10;
      }
    } else if (ch == 'd') {
      if (az-10 > 0) {
        az -= 10;
      }
    } else if (ch == 'w') {
      if (alt+10 < 180) {
        alt += 10;
      }
    } else if (ch == 's') {
      if (alt-10 > 0) {
        alt -= 10;
      }
    }

    altServo.write(alt);
    azServo.write(az);  
  }
  
}