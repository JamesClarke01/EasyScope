#include <SoftwareSerial.h>

//HC-05
#define BT_RX 2
#define BT_TX 13

SoftwareSerial BTSerial(BT_RX, BT_TX);

void setup() {
  Serial.begin(115200);
  BTSerial.begin(38400);
}

void loop() {
  if(BTSerial.available()) {
    char input = BTSerial.read();
    Serial.print(input);
  }
}
