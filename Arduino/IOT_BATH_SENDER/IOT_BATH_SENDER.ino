#include <SoftwareSerial.h>
SoftwareSerial btSerial(3,2);

void setup() {
  Serial.begin(9600);
  btSerial.begin(9600);
  delay(1000);
  btSerial.print("AT+COND43639D84CDF");
  delay(1000);
}

void loop() {
  // put your main code here, to run repeatedly:
  if(btSerial.available()){
    uint8_t temp = btSerial.read();
    Serial.println(temp);
  }

}

String inputString;
void serialEvent(){
  while(Serial.available()){
    char temp = Serial.read();
    if(temp == '\n'){
      if(inputString.equals("START")){
        Serial.println("Start");
        btSerial.write(194);
        delay(1);
        btSerial.write(127);
        delay(1);
      }
      else if(inputString.equals("END")){
        Serial.println("Stop");
        btSerial.write(74);
        btSerial.write(127);
        delay(1);
      }
      inputString ="";
    }
    else{
      inputString += temp; 
    }
  }
}
