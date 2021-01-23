#include "LeVel.h"
#include "INIT.h"
#include <arduino.h>

void initlevel(){
  pinMode(L1, INPUT);
  pinMode(L2, INPUT);
  pinMode(L3, INPUT);
  pinMode(pumpC, OUTPUT);
  pinMode(pumpW, OUTPUT);
}
void initONOF(){
  pinMode(pumpB,OUTPUT);
  pinMode(BUTTON, INPUT);
  pinMode(LedONOF, OUTPUT);
  pinMode(LedC,OUTPUT);
  pinMode(LedW,OUTPUT);
}
int chk_level(int* L){
  int i;
  for( i=3; i>0; i--){
    if( L[i]>BOUND ){
      return i;     //nowlevel
    }
  }
  return 0;         //lowlevel
}


void pump_set(int cool, int hot){
  digitalWrite(pumpC,cool);
  digitalWrite(pumpW,hot);
}
