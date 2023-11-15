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

//Enums
enum ReceiveMode {MANUAL, COORD};
enum CoordType {ALT, AZ};

//Hardware components delcarations
Stepper stepper(STEPS_PER_REV, STEP_IN4, STEP_IN2, STEP_IN3, STEP_IN1); 
Servo servo;
SoftwareSerial BTSerial(BT_RX, BT_TX); 


class DirectionClass {
  private:
    int alt;
    int az;

  public:

    DirectionClass() {
      setAlt(0);
      setAz(0);
    }

    void setAlt(int pAlt) {
      if (pAlt >= 0 && pAlt <= 90) {
        alt = pAlt;
        servo.write(map(alt, 0, 90, 80, 180));
      }
    }

    void manualAltIncrease(void) {
      setAlt(alt+SERVO_STEP);
    }

    void manualAltDecrease(void) {
      setAlt(alt-SERVO_STEP);
    }

    void setAz(int pAz) {
      stepper.step(map(pAz - az, 0, 360, 0, 2048));     
      az = pAz;
    }

    manualAzIncrease(void) {
      az += STEP_STEP;
      stepper.step(STEP_STEP);  
    }

    manualAzDecrease(void) {
      az -= STEP_STEP;
      stepper.step(-STEP_STEP);  
    }
    
    

    
};

//Global Vars
DirectionClass direction;
String altStr;
String azStr;
enum ReceiveMode receiveMode;
enum CoordType coordType;

//Function Headers
int handleManualChar(char input);
int handleCoordChar(char input);


void setup()
{
  //Serial Setup
  Serial.begin(115200);

  //BT Setup
  BTSerial.begin(38400);

  //Stepper Setup
  stepper.setSpeed(STEP_STEP);

  //Servo Setup
  servo.attach(SERVO_PIN);
  
  //Flags
  receiveMode = MANUAL;
  coordType = ALT;
}
 
void loop()
{  
  if(BTSerial.available()) {
    char input = BTSerial.read();
    Serial.println(input);

    switch (receiveMode) {
      case MANUAL:
        handleManualChar(input);
        break;
      case COORD:
        handleCoordChar(input);
        break;
    }
  }
  return 0;
}

int handleManualChar(char input) {
  switch (input) {
    case 'r':  //Move Right
      direction.manualAzIncrease();
      break;
    case 'l':  //Move Left
      direction.manualAzDecrease();
      break;          
    
    case 'u':  //Move Up
      direction.manualAltIncrease();
      break;
    case 'd':  //Move Down
      direction.manualAltDecrease();
      break;
    
    case '(':  //Enter Coord Mode
      receiveMode = COORD;
      altStr = "";
      azStr = "";
      coordType = ALT;
      break;
  }
}

int handleCoordChar(char input) {
  switch (input) {
    case ')':  //Switch to manual mode              
      direction.setAlt(altStr.toInt());
      direction.setAz(azStr.toInt());
      altStr = "";
      azStr = "";
      receiveMode = MANUAL;
      break;
    case ',':  //Start taking for az value
      coordType = AZ;
      break;
    default:
      switch(coordType) {
        case ALT:
          altStr += input;
          break;
        case AZ: 
          azStr += input;
          break;
      }
  }
}


