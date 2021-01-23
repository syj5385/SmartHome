#include "ble.h"

#include <SoftwareSerial.h>

SoftwareSerial btSerial(3,2);

void initializeBLE(){
  btSerial.begin(BAUDRATE);
  Serial.begin(BAUDRATE);
}

boolean readBtData(uint8_t* buf){
  int numOfBuffer = btSerial.available();
  int i; 
  if(numOfBuffer == 2){
    for(i=0; i<numOfBuffer;i++)
      buf[i] = btSerial.read();   
    return true; 
  }
  else{
    for(i=0; i<numOfBuffer;i++)
      btSerial.read();    
    return false; 
  }

}

void printBox(BOX* box){
  char string[100];
  sprintf(string,"R_EXE : %d\tR_LEV : %d\tR_TEMP : %d\tMY_EXE : %d\tMY_LEV : %d\tMY_TEMP : %d\n",
    box->request_exe, box->request_level,box->request_temp, box->my_exe,box->my_level, box->my_temp);
  Serial.print(string);
}

void makeBox(uint8_t* buf, BOX* box){
  box -> request_exe = (buf[0] >> 7) & 0x01;
  box -> request_level = (buf[0] >> 5) & 0x03;
  box -> request_temp = buf[0] & 0x1f;
  box -> my_exe = (buf[1] >> 7) & 0x01;
  box -> my_level = (buf[1] >> 5) & 0x03;
  box -> my_temp = buf[1] & 0x1f;
  printBox(box);
}

void sendData(BOX* box){
  uint8_t buf[2]; 
  buf[0] = (((box->request_exe)<<7) & 0x80) + (((box->request_level)<<5) & 0x60) + box -> request_temp;
  buf[1] = (((box->my_exe)<<7) & 0x80) + (((box->my_level)<<5) & 0x60) + box -> my_temp;

  btSerial.write(buf[0]);
  delayMicroseconds(500);
  btSerial.write(buf[1]);
  delay(1);
}
