#include <Servo.h>

#include <Stepper.h>
#include <AFMotor.h>
#include <SoftwareSerial.h>
#include <ArduinoJson.h>

//Stepper
#define STEPS_PER_REV 4096
#define STEPPER_PORT 2
#define STEPPER_SPEED 20 //rpm
#define MANUAL_STEPS 5

//Servo
#define SERVO_LEFT_PIN 10
#define SERVO_RIGHT_PIN 9
#define SERVO_STEP 1

//HC-05
#define BT_RX 2
#define BT_TX_UNUSED 0

//Bounds
#define ALT_LOW_BOUND 11
#define ALT_HIGH_BOUND 84

//Direction Bytes
#define MAN_UP 'w'
#define MAN_LEFT 'a'
#define MAN_DOWN 's'
#define MAN_RIGHT 'd'

#define TWEAK_UP 'i'
#define TWEAK_LEFT 'j'
#define TWEAK_DOWN 'k'
#define TWEAK_RIGHT 'l'

//Enums
enum ReceiveMode {MANUAL, JSON};
enum CoordType {ALT, AZ};

//Hardware components delcarations
AF_Stepper stepper(STEPS_PER_REV/2, STEPPER_PORT) ; 
Servo leftServo, rightServo;
SoftwareSerial BTSerial(BT_RX, BT_TX_UNUSED); 

class DirectionClass {
  //private:
  //  int alt;
  //  int az;

  public:
    int alt;
    int az;

    DirectionClass() {
      //Initialise values
      alt = ALT_LOW_BOUND;
      az = 0;

      //Move to initial positions
      changeAltOrientation(ALT_LOW_BOUND);
      changeAzOrientation(0);
    }

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

    void manualAltIncrease(void) {
      changeAltOrientation(alt+SERVO_STEP);
    }

    void manualAltDecrease(void) {
      changeAltOrientation(alt-SERVO_STEP);
    }

    void manualAzIncrease(void) {
      az += MANUAL_STEPS;
      stepper.step(MANUAL_STEPS, FORWARD, INTERLEAVE);  
    }

    void manualAzDecrease(void) {
      az -= MANUAL_STEPS;
      stepper.step(MANUAL_STEPS, BACKWARD, INTERLEAVE);  
    }

    void tweakAltIncrease(void) {
      moveVertically(alt+SERVO_STEP);
    }

    void tweakAltDecrease(void) {
      moveVertically(alt -= SERVO_STEP);
    }

    void tweakAzIncrease(void) {
      moveHorizontally(az += MANUAL_STEPS);
    }

    void tweakAzDecrease(void) {
      moveHorizontally(az -= MANUAL_STEPS);
    }    

    int moveVertically(int pAlt) {
      //Function Desc: Moves scope vertically but does not update alt value

      int leftAngle, rightAngle;
      const int increment = 10;

      if (pAlt < ALT_LOW_BOUND || pAlt > ALT_HIGH_BOUND) {                    
        return 1; //out of bounds
      }

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
      
      return 0;
    }

    int moveHorizontally(int pAz) {
      //Function Desc: Moves scope horizontally but does not update az value
      int direction;
      int degToMove = pAz-az;
      int stepsToMove;

      if (degToMove < 0) {
        direction = BACKWARD;
      } else {
        direction = FORWARD;
      }

      stepsToMove = map(abs(degToMove), 0, 360, 0, STEPS_PER_REV);

      stepper.step(stepsToMove, direction, INTERLEAVE);
      
      return 0;
    }

    void changeAltOrientation(int pAlt) {
      //Function Desc: Moves scope vertically and updates alt value
      if(moveVertically(pAlt) == 0) {
        alt = pAlt; //update current alt
      }
    }

    void changeAzOrientation(int pAz) {
      //Function Desc: Moves scope horizontally and updates az value
      if(moveHorizontally(pAz) == 0) {
        az = pAz;
      }
    }

    void setAzValue(double pAz) {
      az = pAz;
      Serial.print("New Azimuth Value: ");
      Serial.print(az);
      Serial.print("\n");
    }
};

//Global Vars
DirectionClass direction;
String altStr;
String azStr;
enum ReceiveMode receiveMode;
enum CoordType coordType;

//JSON
String jsonString;
int curlyCount = 0;

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
  stepper.setSpeed(STEPPER_SPEED);

  //Servo Setup  
  leftServo.attach(SERVO_LEFT_PIN);
  rightServo.attach(SERVO_RIGHT_PIN);
  
  //Flags
  receiveMode = MANUAL;
  coordType = ALT;
}
 
void loop()
{  
  
  //Serial.println(direction.alt);
  
  if(BTSerial.available()) {
    char rChar = BTSerial.read();
    //Serial.println(rChar);

    switch (receiveMode) {      
      case MANUAL:
        handleManualChar(rChar);
        break;
      case JSON:
        handleJsonChar(rChar);
        break;      
    }    
  }
  return 0;
}

int handleJsonChar(char rChar) {
  //Receiving JSON String
  
  jsonString += rChar;
  
  if (rChar == '{') {
    curlyCount++;
  } else if (rChar == '}') {
    curlyCount--;
    if (curlyCount == 0) {  //reached end of json
      receiveMode = MANUAL;
      processJSON(jsonString);
    }
  }
}

int handleManualChar(char rChar) {
  switch (rChar) {
    case MAN_UP:  
      direction.manualAltIncrease();
      break;
    case MAN_LEFT:  
      direction.manualAzDecrease();
      break;  
    case MAN_DOWN:  
      direction.manualAltDecrease();
      break;      
    case MAN_RIGHT:  
      direction.manualAzIncrease();
      break;    
    case TWEAK_UP:  
      direction.tweakAltIncrease();
      break;
    case TWEAK_LEFT:  
      direction.tweakAzDecrease();
      break;  
    case TWEAK_DOWN:  
      direction.tweakAltDecrease();
      break;      
    case TWEAK_RIGHT:  
      direction.tweakAzIncrease();
      break;                   
    case '{':  //Enter json Mode
      Serial.println(direction.alt);
      curlyCount = 1;
      jsonString = "{";
      receiveMode = JSON;
      break;
  }
}

void processJSON(String pJson) {
 
  //Serial.println("Processing JSON...");
  StaticJsonDocument<100> doc;
  char* jsonArray = new char[pJson.length()+1];  //allocate a new json array on the heap
  
  strcpy(jsonArray, pJson.c_str());  //Copy json string into json array

  DeserializationError error = deserializeJson(doc, jsonArray);  //deserialize json

  // Test if parsing succeeds.
  if (error) {
    Serial.print(F("deserializeJson() failed: "));
    Serial.println(error.f_str());
    return;
  }

  const char* instruction = doc["Instruction"];

  if(strcmp(instruction, "Slew") == 0) {                 
    slew(doc["Data"]["Altitude"], doc["Data"]["Azimuth"]);
  } else if(strcmp(instruction, "Calibrate") == 0) {
    calibrate(doc["Data"]["Azimuth"]);
  } else if(strcmp(instruction, "Reset") == 0) {
    reset();
  }

  delete[] jsonArray;  //Deallocate json array (important!)
}

void slew(double alt, double az) {
  //Serial.println("Slewing");     
  direction.changeAltOrientation(alt);
  direction.changeAzOrientation(az);
}

void calibrate(double pAz) {
  direction.setAzValue(pAz);
}

void reset() {
  direction.changeAltOrientation(ALT_LOW_BOUND);
}

