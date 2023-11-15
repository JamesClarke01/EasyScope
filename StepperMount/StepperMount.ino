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

//Global Vars
int alt;
int az;
String altStr;
String azStr;
enum ReceiveMode receiveMode;
enum CoordType coordType;

//Function Headers
int handleManualChar(char input);
int handleCoordChar(char input);
int updateServo(void);
int updateStepper(void);

class Direction {
  private:
    int alt;
    int az;

    Direction() {
      alt = 0;
      az = 0;
    }
  
  public:
    void setAlt(int pAlt) {
      alt = pAlt;
    }
    
    void setAz(int pAz) {
      alt = pAz;
      
    }
};


void setup()
{
  //Serial Setup
  Serial.begin(115200);

  //BT Setup
  BTSerial.begin(38400);

  //Stepper Setup
  stepper.setSpeed(STEP_STEP);

  //Servo Setup
  alt = 0;
  az = 0;
  servo.attach(SERVO_PIN);
  updateServo();

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
      stepper.step(STEP_STEP);
      break;
    case 'l':  //Move Left
      stepper.step(-STEP_STEP);
      break;          
    /*
    case 'u':  //Move Up
      if (servoAngle + SERVO_STEP < 180) {
        servoAngle += SERVO_STEP;
        servo.write(servoAngle);
      }
      break;
    case 'd':  //Move Down
      if (servoAngle - SERVO_STEP > 80) {
        servoAngle -= SERVO_STEP;
        Serial.println(servoAngle);
        servo.write(servoAngle);
      }
      break;
    */
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
      alt = altStr.toInt();
      az = azStr.toInt();
      updateServo();
      updateStepper();
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


int updateStepper(void) {
  stepper.step(map(az, 0, 360, 0, 2048));

}
 
int updateServo(void) {
  servo.write(map(alt, 0, 90, 80, 180));
}
