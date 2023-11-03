#include <Stepper.h>
 
#define stepsPerRevolution 2048
 
Stepper stepper(stepsPerRevolution, 8, 10, 9, 11); 
 
int direction = 1;
int position = 0; //North

void setup()
{
  Serial.begin(115200);
  stepper.setSpeed(10);
}
 
void loop()
{  
  if(Serial.available()) {
    char input = Serial.read();

    switch (input) {
      case 'w':
        Serial.println("North");
        
        stepper.step((0 - position) * 512);
        position = 0;
        break;

      case 'd':
        Serial.println("East");
        stepper.step((1 - position) * 512);
        position = 1;
        break;

      case 's':
        Serial.println("South");
        stepper.step((2 - position) * 512);
        position = 2;
        break;

      case 'a':
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
 
