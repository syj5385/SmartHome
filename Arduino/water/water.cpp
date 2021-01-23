
#include "water.h"
#include <arduino.h>

void initailizeWater(){
  pinMode(MUX_KEY0,OUTPUT);
  pinMode(MUX_KEY1,OUTPUT);
  pinMode(MUX_KEY2,OUTPUT);

  pinMode(MUX_OUTPUT,INPUT);
}

void setMUX(int k0, int k1, int k2){
  digitalWrite(MUX_KEY0,k0);
  digitalWrite(MUX_KEY1,k1);
  digitalWrite(MUX_KEY2,k2);
}

int getState(int level){
  int result = 0; 
  if(level ==0){
    setMUX(0,0,0);
    
  }
  else if(level == 1){
    setMUX(0,0,1);
   
  }
  else if(level == 2){
    setMUX(0,1,0);
  }
  else if(level == 3){
    setMUX(0,1,1);
  }
  else{
    return -1;
  }
  result = analogRead(MUX_OUTPUT);
  return result; 
}

int getLevel(){
  if(getState(0) < BOUNDARY_OK)
    return 0;
  
  else if(getState(1) < BOUNDARY_OK)
    return 1;

  else if(getState(2) < BOUNDARY_OK)
    return 2;

  else if(getState(3) < BOUNDARY_OK)
    return 3;

  else
   return 4;
  
}
