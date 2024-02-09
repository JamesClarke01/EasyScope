#include <SoftwareSerial.h>

//HC-05
#define BT_RX 7
#define BT_TX 6

SoftwareSerial BTSerial(BT_RX, BT_TX);

void setup() {
  Serial.begin(115200);
  BTSerial.begin(38400);
}

void loop() {
  if(BTSerial.available()) {
    char input = BTSerial.read();
    Serial.println(input);
  }
}
