
#ifndef _OLED_H_
#define _OLED_H_

#include <arduino.h>
#include <SPI.h>
#include "machine.h"
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

#define OLED_RESET  4

#define LOGO16_GLCD_HEIGHT  16
#define LOGO16_GLCD_WIDTH   16

#if(SSD1306_LCDHEIGHT != 32)
#error("Height incorrect, please fix Adafruit_SSD1306.h!");
#endif

void cleardisplay();

void init_display();

void printTH(float humidity, float temperature);

void printMachine(Machine* machine);
//void logo_display();


#endif /* _OLED_H_ */
