#include "bath_serial.h"
#include <stdlib.h>

SoftwareSerial btSerial(3,2);

void initializeSerial(){
  Serial.begin(9600);
  btSerial.begin(9600);
}

boolean readBTSerial(uint8_t* buf){
  memset(buf,'\0',sizeof(buf));
  
  if(btSerial.available()>=2){
    buf[0] = btSerial.read();
    delayMicroseconds(500);
    buf[1] = btSerial.read();
    delay(1);
    Serial.print("Read data : " );
    Serial.print(buf[0]);
    Serial.print("\t");
    Serial.println(buf[1]);
    return true;  
  }
  delay(1);
  return false; 
}

void createNewBathCommand(uint8_t* buf, BATH* bath){
  if(bath == NULL)
    return;

  // 1. request 
  bath -> execution_request = (buf[0] & 0b10000000)>>7;
  bath -> level_request = ((buf[0] & 0b01100000) >>5) ; 
  bath -> temp_request = (buf[0] & 0b00011111); 

  Serial.print("execution request : ");
  Serial.println(bath->execution_request);
  Serial.print("level request : ");
  Serial.println(bath->level_request);
  Serial.print("temp request : ");
  Serial.println(bath->temp_request);

  // 1. result 
  bath -> execution_result = (buf[1] & 0b10000000)>>7;
  bath -> level_result = ((buf[1] & 0b01100000) >>5) ; 
  bath -> temp_result = (buf[1] & 0b00011111); 

  Serial.print("execution result : ");
  Serial.println(bath->execution_request);
  Se:wq
  rial.print("level result : ");
  Serial.println(bath->level_result);
  Serial.print("temp result : ");
  Serial.println(bath->temp_result);


}

void sendCommandToServer(BATH* bath){
  uint8_t value1 = 0; 
  uint8_t value2 = 0;
  value1 = (bath->execution_request << 7) | (bath->level_request) << 5 | bath->temp_request;
  value2 = (bath->execution_result << 7) | (bath->level_result) << 5 | bath->temp_result;
  btSerial.write(value1);
  delayMicroseconds(500);
  btSerial.write(value2);
  delay(1);
  Serial.println("Send Bath data to Home Controller");
  Serial.print("execution request : ");
  Serial.println(bath->execution_request);
  Serial.print("level request : ");
  Serial.println(bath->level_request);
  Serial.print("temp request : ");
  Serial.println(bath->temp_request);
  Serial.print("execution result : ");
  Serial.println(bath->execution_request);
  Serial.print("level result : ");
  Serial.println(bath->level_result);
  Serial.print("temp result : ");
  Serial.println(bath->temp_result);
}
