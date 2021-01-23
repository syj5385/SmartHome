

#include <SoftwareSerial.h>
SoftwareSerial BTSerial(3,2);


const int gasPin = A0 ;
int led1 = 8;
int motor = 9;

void setup() {
  // put your setup code here, to run once:
Serial.begin(9600);
BTSerial.begin(9600);
pinMode(8, OUTPUT);
pinMode(9, OUTPUT);
pinMode(10, OUTPUT);
}


void loop() {
  // put your main code here, to run repeatedly:
Serial.println(analogRead(gasPin)*2);
int reg = analogRead(gasPin)*2;
if(reg > 1000){
  analogWrite(motor, 255);
  digitalWrite(led1,HIGH);
  //to server
  uint8_t value[2];
  value[0] = 200;
  value[1] = 100;
  BTSerial.write(value[0]);
  delayMicroseconds(100);
  BTSerial.write(value[1]);
  delay(1);
  //to server

}
  else if(reg < 700){
  digitalWrite(led1,LOW);
  analogWrite(motor,0);
  
}
else{
  digitalWrite(led1,LOW);
  analogWrite(motor, 255);

}
delay(5000);
}
