#include <Stepper.h>
#include <SoftwareSerial.h>
 
#define STEPS_PER_REV 2048

//Pin Defines
//Stepper
#define STEP_IN1 11
#define STEP_IN2 10
#define STEP_IN3 9
#define STEP_IN4 8

//HC-05
#define BT_RX 7
#define BT_TX 6

Stepper stepper(STEPS_PER_REV, STEP_IN4, STEP_IN2, STEP_IN3, STEP_IN1); 
SoftwareSerial BTSerial(BT_RX, BT_TX); 
 
int direction = 1;
int position = 0; //North

void setup()
{
  Serial.begin(9600);
  BTSerial.begin(38400);
  stepper.setSpeed(10);
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

      default:
        Serial.println('Invalid');
        break;
    }

    return 0;
  }
}
 
