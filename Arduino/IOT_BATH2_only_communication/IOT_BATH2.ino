#include "ble.h"
#include "define.h"
#include "water.h"
#include "temp.h"

BOX* box; 

state_t state;
exe_t isExe = false;  
int ble_send_count = 0;

int pW, pC, pB; 

void executeBox();

void setup() {
  initializeBLE();
  initWater();
  initTemp();

  box = (BOX*)malloc(sizeof(BOX));
  memset(box,0,sizeof(box));

  state = BATH_OFF;
  pW = OFF_bath;
  pC = OFF_bath;
  pB = OFF_bath; 
}

long prev_t, loop_t;

void loop() {
  uint8_t* buf = (uint8_t*)malloc(sizeof(uint8_t) *2);
  
  prev_t = millis();
  if(readBtData(buf) == true){
    makeBox(buf,box);
  }

  executeBox();
  free(buf);
  // Delay
  loop_t = millis()-prev_t;
  if((loop_t = LOOPTIME-loop_t) >= 0){
    delay(loop_t);
  }
}

void executeBox(){
  if(box -> request_exe == 0){
    if(isExe == false){
      Serial.println("Waiting for command");
      state = BATH_OFF;
      pW = OFF_bath; pC = OFF_bath; 
      pump_set(pC,pW);
    }
    else{
      Serial.println("Forced exited from server");
      state = BATH_OFF;
      pW = OFF_bath; pC = OFF_bath; 
      pump_set(pC,pW);
      isExe = false; 
    }
  }
  else{ /* Requested execution from Server / box -> request_exe == 1 */ 
    if(isExe == false){
      Serial.println("Start BATH");
      box -> my_exe = 1;
      ble_send_count = 0;
      isExe = true; 
    }
    else{
      Serial.println("Executing BATH");
      ble_send_count++;
      box -> my_exe = 20;
      // get Level and Temperature
    }
    if(ble_send_count++ >= 20){
      sendData(box);
      ble_send_count= 0; 
    }
  }
}
