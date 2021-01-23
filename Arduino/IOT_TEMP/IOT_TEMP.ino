#include "define.h"
#include "temperature.h"
#include "OLED.h"
#include "Serial.h"
#include "machine.h"
#include <stdlib.h>

Machine* machine; 
void setup() {
  Serial.println("Genius Home Temperature and Humidity");
  // put your setup code here, to run once:
  Serial.begin(9600);
  initializeBtSerial();
  initializeDHT();
  init_display();
  machine = (Machine*)malloc(sizeof(Machine));
  initializeMachine(machine);
  delay(1000);
  analogWrite(MOTOR,255);
}

int readTHCount = 0; 
int btSendCount = 0; 
void loop() {
  // put your main  code here, to run repeatedly:
  if(readTHCount == 25){
    float* humidity = (float*)malloc(sizeof(float));
    float* temperature = (float*)malloc(sizeof(float));
    cleardisplay();
    if(getTH(humidity, temperature) == true){
       Serial.print("H : ");
       Serial.println(*humidity);
       Serial.print("T : " );
       Serial.println(*temperature);
       Serial.println();
       printTH(*humidity, *temperature);
       printMachine(machine);
    }
    if(btSendCount % 5  == 0){
      executeMachine(machine,*humidity);
    }
    
    if(btSendCount >= 20){
      Serial.println("Send TH Data to Server\n");
      btSendCount = 0; 
      sendTH_To_Server(*humidity, *temperature);
      
    }
  
    free(humidity);
    free(temperature);
    btSendCount ++;
    readTHCount = 0; 
  }
  readTHCount++; 
  
  delay(20);
}

String input ="";

void executeCommand(){
  Serial.print("command : ");
  Serial.println(input);
  if(input.equals("POWER_ON")){
    powerMachine(machine, true);
  }
  else if(input.equals("POWER_OFF")){
    powerMachine(machine, false);
  }
  else if(input.equals("STR_HIGH")){
    strengthMachine(machine, STR_HIGH);
  }
  else if(input.equals("STR_LOW")){
    strengthMachine(machine, STR_LOW);
  }
  else if(input.equals("RED2")){
    colorMachine(machine,COL_RED2);
  }
  else if(input.equals("GREEN2")){
    colorMachine(machine,COL_GREEN2);
  }
  else if(input.equals("PUR2")){
    colorMachine(machine,COL_PUR2);
  }
  else return;
}

void serialEvent(){
  if(Serial.available()){
    char temp = Serial.read();
    if(temp == '\n'){
      executeCommand();      
      input ="";
    }
    else
      input += temp;
      
  }
  
}
