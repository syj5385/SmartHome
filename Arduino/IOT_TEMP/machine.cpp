#include "machine.h"


void Click(int pin,int time){
  digitalWrite(pin,HIGH);
  delay(time);
  digitalWrite(pin,LOW);
  delay(500);
}

void initializeMachine(Machine* machine){
  pinMode(LED_CON,OUTPUT);
  pinMode(STRENGTH_CON,OUTPUT);
  pinMode(POWER_CON,OUTPUT);
  digitalWrite(LED_CON,LOW);
  digitalWrite(STRENGTH_CON,LOW);
  digitalWrite(POWER_CON,LOW);

  machine -> power = OFF;  
  machine -> color = COL_NONE;
  machine -> strength = STR_NONE;
  
}
void powerMachine(Machine* machine, boolean power){
  int i; 
  if((machine -> power == ON && power)|| (machine -> power == OFF && !power)){
    return ;
  }
  if(power){
    for(i=0; i<4; i++){
      Click(POWER_CON,SHORT);
    }
    machine -> power = ON;
    machine -> color = COL_NONE;
    machine -> strength = STR_HIGH;
    strengthMachine(machine,STR_LOW);
    colorMachine(machine,COL_WHITE);
  }
  else{
      Click(POWER_CON,LONG);
      machine -> power = OFF;
      machine -> color = COL_NONE;
      machine -> strength = STR_NONE;
  }
}

void strengthMachine(Machine* machine, int str){
  if(machine -> power == OFF)
    return ;

  if(str - (machine->strength) == 0)
    return;  

  Click(STRENGTH_CON,SHORT);
  machine -> strength = str; 
}

void colorMachine(Machine* machine, int color){
  int target = color - (machine -> color);
  int i;
  if(target > 0){
    for(i=0; i<target; i++)
      Click(LED_CON,SHORT);

    machine -> color = color; 
  }
  else if(target < 0){
    target += 17; 
    for(i=0; i<target; i++)
      Click(LED_CON,SHORT);
      machine -> color = color; 

  }
  else return; 
}

void executeMachine(Machine* machine,float humidity){
  if(humidity > LEVEL2){
    powerMachine(machine,false);
    return ;
  }
  else
    powerMachine(machine,true);
    
  
  if(humidity >= 0 && humidity < 30){
    colorMachine(machine,COL_RED2);
    strengthMachine(machine,STR_HIGH);
  }
  else if(humidity >= 30 && humidity < 40){
    colorMachine(machine,COL_YG2);
    strengthMachine(machine,STR_HIGH);
  }
  else if(humidity >= 40 && humidity < 50){
    colorMachine(machine,COL_GREEN2);
    strengthMachine(machine, STR_HIGH);
  }
  else if(humidity >= 50 && humidity < 60){
    colorMachine(machine,COL_SKY2);
    strengthMachine(machine,STR_LOW);
  }
  else if(humidity >= 60 && humidity < 90){
    colorMachine(machine,COL_BLUE2);
    strengthMachine(machine,STR_LOW);
  }
  else{
    
  }
}
