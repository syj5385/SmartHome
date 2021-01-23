#include <SoftwareSerial.h>
#include <Servo.h>

SoftwareSerial btSerial(5,4);
Servo SV;
int* currentV; 
void setup() {
 
  SV.attach(6,800,2400);
  btSerial.begin(9600);
  Serial.begin(9600);

  initializeWindow();
  currentV = (int*)malloc(sizeof(int));
  *currentV = 0;
  Serial.print("Current Window : " );
  Serial.println(*currentV);
  
}


void loop() {
  
  if(btSerial.available() >= 2){
    unsigned char temp;
   
    while((temp = btSerial.read()) < 100);
    unsigned char switch1 = temp;
    unsigned char analog = btSerial.read();
    
    Serial.print("sw : ");
    Serial.println(switch1);
    Serial.print("analog : ");
    Serial.println(analog);

    if(switch1 == 100){
      moveWindow(*currentV, 3);
      Serial.println("close");
      *currentV = 3; 

    }
    else if(switch1 == 200){
      
      analog = (analog - 100) * 90 / 100; 
//      *currentV = (speed1 - 100) * 90 / 100;
      Serial.print("analog to angle : ");
      Serial.println(analog);
      Serial.println("open");
      Serial.print("current : ");
      Serial.println(*currentV);
      Serial.print("to : ");
      Serial.println(analog);
      moveWindow(*currentV, analog);
      
      *currentV = analog; // update 
      
    }
  }
  
}

void initializeWindow(){
  SV.write(5); 
}

void moveWindow(int from, int to){
  if(from < 0)
    return;
   else if(to > 90)
    return; 

    //
  if(from > to){
    for (int i = from; i > to; --i){
      SV.write(i);
      delay(30);
    }
  }
  else if(from < to){
    for (int i = from; i < to; ++i){
      SV.write(i);
      delay(30);
    }
  }
  else{
    return; 
  }
  
  
}

