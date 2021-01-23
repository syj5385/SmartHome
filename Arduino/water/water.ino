#include "water.h"

void setup() {
  // put your setup code here, to run once:
 
  initailizeWater();
  Serial.begin(9600);
  pinMode(6,OUTPUT);
  analogWrite(6,255);
}

void loop() {
  // put your main code here, to run repeatedly:
  Serial.print("level0 : ");
  Serial.print(getState(0));
  Serial.print("\tlevel1 : ");
  Serial.print(getState(1));
  Serial.print("\tlevel2 : ");
  Serial.print(getState(2));
  Serial.print("\tlevel3 : ");
  Serial.print(getState(3));
  Serial.print("\tlevel : " );
  Serial.println(getLevel());
  
  if(getLevel() <=3){
    analogWrite(6,255);
  }
  else{
    analogWrite(6,0);
  }

delay(100);
  
  
}
