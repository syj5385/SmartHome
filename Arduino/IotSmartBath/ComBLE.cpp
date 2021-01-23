#include "ComBLE.h"
#include "INIT.h"
#include <arduino.h>
#include <SoftwareSerial.h>

SoftwareSerial btSerial(3,2);
//3 tx,2rx

void initbt(){  
  btSerial.begin(9600);
}

bool readData(uint8_t* buf){
  memset(buf,0,sizeof(buf));
  int index =0;
  if(btSerial.available()>=2){
    buf[index++] = btSerial.read();
    delayMicroseconds(500);
    buf[index++] = btSerial.read();
    delay(1);
    /* print */
    Serial.print("!RECEIVE data! :");
    Serial.print(buf[0]);
    Serial.print("  ");
    Serial.println(buf[1]);
    return true;
    }
  return false;
}


void analysisCommand(uint8_t* buf ,BOX* box){
  // request
  box -> request_exe = (buf[0] >> 7) & 0x01; 
  box -> request_level = ( (buf[0] >> 5) & 0x03 ); 
  box -> request_temp = buf[0] & 0x1f;
  box -> my_exe = (buf[1] >> 7) & 0x01; 
  box -> my_level = (buf[1] >> 5) & 0x03; 
  box -> my_temp = buf[1] & 0x1f;
  
}
void initialbox_my(BOX* box){
  if( box->request_exe == 1 ){
    box->my_exe = 1;
  } else {
    box -> my_exe = 0;
  }
  box -> my_level = 0;
  box -> my_temp = 1;
}


void sendData(BOX* box){
  uint8_t buf[2]; 
  buf[0] = (((box->request_exe)<<7) & 0x80) + (((box->request_level) << 5) & 60) + box->request_temp; 
  buf[1] = (((box->my_exe)<<7) & 0x80) + (((box->my_level) << 5) & 60) + box->my_temp; 
  
  btSerial.write(buf[0]);
  delayMicroseconds(500);
  btSerial.write(buf[1]);
  /* print */
  Serial.print("!SENDING data! :");
  Serial.print(buf[0]);
  Serial.print("  ");
  Serial.println(buf[1]);
  delay(1);
}



void print_box(BOX* box){
  Serial.print("\t\t");
  Serial.print("< REQUEST > exe : ");     Serial.print(box->request_exe);
  Serial.print("\t\twater level : ");       Serial.print(box->request_level);
  Serial.print("\t\twater temperature : "); Serial.println(box->request_temp+15);
  Serial.print("\t\t");
  Serial.print("< MY_BATH > exe : ");     Serial.print(box->my_exe);
  Serial.print("\t\twater level : ");       Serial.print(box->my_level);
  Serial.print("\t\twater temperature : "); Serial.println(box->my_temp+19);
}
