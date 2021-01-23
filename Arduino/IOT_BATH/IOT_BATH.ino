
#include "bath_serial.h"
#include "define.h"
#include <stdio.h>
#include <stdlib.h>

BATH* bath;
boolean isExecute = false;

// Function Proto type
void startBATH();
void stopBATH();
void readBathData();

void setup() {
  // put your setup code here, to run once:
  initializeSerial();
  bath = (BATH*)malloc(sizeof(BATH));
  bath -> execution_request = 0;
  bath -> level_request = 0;
  bath -> temp_request = 0;
  bath -> execution_result = 0;
  bath -> level_result = 0;
  bath -> temp_result = 0;
   

}

int BtCount = 0; 

void loop() {

  // Read Command
  uint8_t* buf = (uint8_t*)malloc(sizeof(uint8_t)*10);
  if(readBTSerial(buf))
    createNewBathCommand(buf,bath);
    
//  if(isExecute == false){ // Bath start Now
//    if(bath->execution_request == 1 && bath->execution_result == 1){
//      // Start Smart Bath
//      Serial.println("Start Bath");
//      startBATH();
//    }    
//  }
//  else{ // Bath is already started
//    if(bath->execution_request == 0 && bath->execution_result == 1){
//      Serial.println("Stop BAth");
//      stopBATH();
//    }
//    if(bath->execution_request == 1 && bath->execution_result == 1){
//      Serial.println("Executing Bath");
//      if(BtCount++ >= 1000){
//        readBathData();
//        
//      }
//      stopBATH();
//    }
//  }
  if(bath->execution_request == 1 && bath -> execution_result == 1){
    if(!isExecute){
      isExecute = true; 
      startBATH();
    }
    Serial.println("status1 - Executing");
    if(BtCount++ >= 10){
      BtCount = 0;
      readBathData(); 
    }
  }
  if(bath->execution_request == 1 && bath -> execution_result == 0){
    Serial.println("status2 - Finished");
    sendCommandToServer(bath);
    bath -> execution_request = 0;
   
    
  }
  if(bath->execution_request == 0 && bath -> execution_result == 1){
    Serial.println("status3 -> Exited by Application" );
    stopBATH();
  }
  if(bath->execution_request == 0 && bath -> execution_result == 0){
    Serial.println("status4 - Waiting");
  }
  free(buf);
  delay(500);
}

void startBATH(){
  if(bath != NULL){
    isExecute = true;
    bath -> execution_result = 1;
    bath -> level_result = 0;
    bath -> temp_result = 10; 
    sendCommandToServer(bath);
  }
  // Start BATH ㅇoutput & read data
}

void stopBATH(){
  if(bath != NULL){
    isExecute = false; 
    bath -> execution_result = 0;
    bath -> level_result = 0;
    bath -> temp_result = 0; 
    sendCommandToServer(bath);
  }
}

void readBathData(){
  boolean level = false;
  boolean temp = false; 
  if(level = (bath -> level_result < bath -> level_request)){
    bath -> level_result ++; 
  }
  if(temp =(bath -> temp_result < bath -> temp_request)){
    bath -> temp_result ++;
  }

  if(level == false && temp == false){
    bath -> execution_result = 0;
    isExecute = false; 
  }
  else{
    bath -> execution_result = 1; 
  }
  sendCommandToServer(bath);
  
}
