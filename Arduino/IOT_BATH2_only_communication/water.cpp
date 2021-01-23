#include "water.h"
#include <arduino.h>

void initWater(){
  pinMode(L1,INPUT);
  pinMode(L2,INPUT);
  pinMode(L3,INPUT);
  pinMode(pumpC,OUTPUT);
  pinMode(pumpW,OUTPUT);
}

void pump_set(int cool,int hot){
  digitalWrite(pumpC,cool);
  delay(1);
  digitalWrite(pumpW,hot);
  delay(1);
}
