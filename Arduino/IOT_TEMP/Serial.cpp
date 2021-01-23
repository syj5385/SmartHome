

#include <stdio.h>
#include <arduino.h>
#include <SoftwareSerial.h>
#include "Serial.h"

SoftwareSerial btSerial(BT_RX,BT_TX);

void initializeBtSerial(){
  btSerial.begin(9600);
}

void sendTH_To_Server(int humidity, int temperature){
  uint8_t humid = (uint8_t)(humidity & 0xff);
  uint8_t temp = (uint8_t)(temperature & 0xff);

  btSerial.write(temp);
  delayMicroseconds(100);
  btSerial.write(humid);
  delayMicroseconds(100);
  Serial.print("<Bluetooth>\n");
  Serial.print("T : ");
  Serial.print(temp);
  Serial.print("\tH : ");
  Serial.println(humid);
  Serial.println();
  
}


