#include "Buzzer.h"
#include <arduino.h>

#define BUZZER  8

void buzzer(int choice){
  switch(choice){
    case 1 :
      for(int i=0; i<30; i++){
        digitalWrite(BUZZER,1);
        delay(1);
        digitalWrite(BUZZER,0);
        delay(1);
      }
  
     break; 
  
     case 2 :
      for(int i=0; i<30; i++){
        digitalWrite(BUZZER,1);
        delay(2);
        digitalWrite(BUZZER,0);
        delay(2);
      }
  
     break;
  
     case 3 :
      for(int i=0; i<200; i++){
        digitalWrite(BUZZER,1);
        delayMicroseconds(300);
        digitalWrite(BUZZER,0);
        delayMicroseconds(300);
      }
  
     break;
  
     case 4 :
      for(int i=0; i<150; i++){
        digitalWrite(BUZZER,1);
        delayMicroseconds(600);
        digitalWrite(BUZZER,0);
        delayMicroseconds(600);
        }
    
       break;
    
       case 5 :
        for(int i=0; i<150; i++){
          digitalWrite(BUZZER,1);
          delay(1);
          digitalWrite(BUZZER,0);
          delay(1);
        }
    
       break;

       case 6: 
       for(int i=0 ; i<400 ; i++){
        digitalWrite(BUZZER,1);
        delayMicroseconds(600);
        digitalWrite(BUZZER,0);
        delayMicroseconds(600);
       }
  }
}

void song(int delay_time, int loop){
  for(int i=0; i<loop; i++){
    digitalWrite(BUZZER,1);
    delayMicroseconds(delay_time);
    digitalWrite(BUZZER,0);
    delayMicroseconds(delay_time);
  }
}

void init_Effect(){
  song(488,120);
  delay(10);

  song(388,160);
  delay(10);

  song(326,175);
  delay(10);

  song(244,200);
  delay(10);
}

