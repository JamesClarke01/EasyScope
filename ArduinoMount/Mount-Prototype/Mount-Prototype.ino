#include <Stepper.h>
#include <Servo.h>
#include <SoftwareSerial.h>
#include <ArduinoJson.h>

//Stepper
#define STEP_STEP 8
#define STEP_SPEED 1
#define STEPS_PER_REV 2048
#define STEP_IN1 8
#define STEP_IN2 9
#define STEP_IN3 10
#define STEP_IN4 11

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
Stepper stepper(STEPS_PER_REV,STEP_IN1, STEP_IN3, STEP_IN2, STEP_IN4); 
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
        Serial.println(map(alt, 0, 90, 80, 180));
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
 
String jsonString;
int curlyCount = 0;


void loop()
{  
  
  if(BTSerial.available()) {
    char rChar = BTSerial.read();

    //Receiving JSON String
    if (rChar == '{' && curlyCount == 0) //Start of a JSON
    {
      curlyCount = 1;
      jsonString = "{";
    } 
    else if (curlyCount > 0) //adding to JSON
    {      
      jsonString += rChar;
      if(rChar == '{') 
      {
        curlyCount++;
      } else if(rChar == '}') 
      {
        curlyCount--;
        if(curlyCount == 0) { //Reached end of JSON                  
          //Serial.println(jsonString);
          processJSON(jsonString);
        }
      } 
    }
  }
  return 0;
}


void processJSON(String pJson) {
  direction.manualAzIncrease();
  delay(100);
  
  StaticJsonDocument<100> doc;
  char* jsonArray = new char[pJson.length()+1];

  strcpy(jsonArray, pJson.c_str()); 

  DeserializationError error = deserializeJson(doc, jsonArray);

  // Test if parsing succeeds.
  if (error) {
    Serial.print(F("deserializeJson() failed: "));
    Serial.println(error.f_str());
    return;
  }

  const char* instruction = doc["Instruction"];

  if(strcmp(instruction, "Slew") == 0) {     
    Serial.println("Slew");              
    slew(doc["Data"]["Altitude"], doc["Data"]["Azimuth"]);
  } else if(strcmp(instruction, "Manual") == 0) {
    Serial.println("Manual");
    manual(doc["Data"]["Direction"]);
  }

  delete[] jsonArray;
}

void slew(double alt, double az) {
  direction.setAlt(alt);
  direction.setAz(az);
}

void manual(String pDirection) {
  Serial.println(pDirection);
  if(pDirection == "Right") {    
    direction.manualAzIncrease();
  } else if(pDirection == "Left") {
    direction.manualAzDecrease();
  } else if(pDirection == "Up") {
    direction.manualAltIncrease();
  } else if(pDirection == "Down") {
    direction.manualAltDecrease();
  }
}