#include <arduino.h>
#include "OLED.h"

Adafruit_SSD1306 display(OLED_RESET);

void cleardisplay(){
  display.clearDisplay();
}

void logo_display(){
  display.clearDisplay();
  int init_index = -102; 
  for(;init_index <= 18; init_index += 3){
    display.clearDisplay();
    display.setCursor(init_index,0);
    display.print(" Genius ");
    if(init_index +10 <= 30){
      display.setCursor(init_index+30, 18);
      display.print("IOT");
    }

    display.display();
    delay(10);
  }
}

void init_display(){
  display.begin(SSD1306_SWITCHCAPVCC,0x3C);
//  display.display();
  display.clearDisplay();

  display.setTextSize(2);
  display.setTextColor(WHITE);
  logo_display();

}

void printTH(float humidity, float temperature){
  display.clearDisplay();
  display.setTextSize(1);

  int h_index_x = 18; 
  int h_index_y = 0;
  int t_index_x = 75; 
  int t_index_y = 0; 

  display.setCursor(h_index_x, h_index_y);
  display.print("H u m");

  display.setCursor(t_index_x, t_index_y);
  display.print("T e m p");

  display.drawLine(0,8, 128,8,WHITE);
  display.drawLine(64,0,64,32,WHITE);

  display.setTextSize(1);
  display.setCursor(23,11);
  display.print((int)humidity);
  display.print(" %");
  display.drawLine(0,20, 64,20,WHITE);

  display.setTextSize(2);
  display.setCursor(72,13);
  display.print((int)temperature);
  display.print(" c");

  display.setCursor(108,13);
  display.drawRect(100,13,4,4,WHITE);
  display.drawRect(101,14,2,2,WHITE);

  display.drawLine(0,31,128,31,WHITE);
}

void printMachine(Machine* machine){
  display.setCursor(8,23);
  display.setTextSize(1);
  if(machine -> power == ON){
    display.print("ON");
    display.drawRect(35,28,4,2,WHITE);
    display.drawRect(36,28,2,2,WHITE);
    
    display.drawRect(40,26,4,4,WHITE);
    display.drawRect(41,26,2,4,WHITE);
    if(machine -> strength == STR_HIGH){
      display.drawRect(45,24,4,6,WHITE);
      display.drawRect(46,24,2,6,WHITE);
      display.drawRect(50,22,4,8,WHITE);
      display.drawRect(51,22,2,8,WHITE);
    }
    
  }
  else if(machine -> power == OFF){
    display.print("OFF");
    display.drawLine(35,26,50,26,WHITE);
    
  }
  
  display.display();
}
