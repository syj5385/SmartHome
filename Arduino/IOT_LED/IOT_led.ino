#include <SoftwareSerial.h>
#include "Buzzer.h"

SoftwareSerial btSerial(3,2);

#define LED1  5 
#define LED2  6

void setup() {
  // put your setup code here, to run once:

  btSerial.begin(9600);
  Serial.begin(9600);

  pinMode(5,OUTPUT);
  pinMode(6,OUTPUT);
  pinMode(8,OUTPUT);  // Buzzer

  delay(1000);
  init_Effect();
  initializeDevice();

}

void loop() {
  // put your main code here, to run repeatedly:
  readDeviceData();
 
  
}

unsigned char* r_data;
void initializeDevice(){
   r_data = (unsigned char*)malloc(2* sizeof(unsigned char));
   r_data[0] = 100;
   r_data[1] = 100; 
}


void readDeviceData(){
  while(btSerial.available()){
   
    r_data[0] = btSerial.read();
    delay(1);
    r_data[1] = btSerial.read();
    delay(1);
    Serial.println("Read Data -->");
    Serial.print("index 0 :");
    Serial.print(r_data[0]);
    Serial.print("\t");
    Serial.print("index 1 :");
    Serial.print(r_data[1]);
    Serial.println("\n");
    executeDevice();
  }
  delay(1);
}

void executeDevice(){
  //LED
  if(r_data[0] == 200){
    int bright = (((int)r_data[1] & 0xff)-100) * 255 / 100;
    analogWrite(LED1,bright);
    delayMicroseconds(500);
    analogWrite(LED2,bright);
    delayMicroseconds(500);
  }
  else if(r_data[0] == 100){
    analogWrite(LED1,0);
    delayMicroseconds(500);
    analogWrite(LED2,0);
    delayMicroseconds(500);
  }
  delay(1);
}

void playOneBuzz(){
  
}

