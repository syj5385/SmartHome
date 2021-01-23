#ifndef _MACHINE_H_
#define _MACHINE_H_

#include <arduino.h>

#define SHORT 500
#define LONG  1500

#define LED_CON       9
#define STRENGTH_CON  10
#define POWER_CON      11

#define LEVEL1  40
#define LEVEL2  90


typedef struct{
  uint8_t power; 
  uint8_t color; 
  uint8_t strength; 
}Machine; 

enum LED_STATE{
  ON,OFF
};

enum COLOR_STATE{
  COL_NONE,
  COL_CHANGE,
  COL_WHITE,
  COL_RED1,
  COL_RED2,
  COL_YG1,
  COL_YG2,
  COL_GREEN1,
  COL_GREEN2,
  COL_SKY1,
  COL_SKY2,
  COL_BLUE1,
  COL_BLUE2,
  COL_PUR1,
  COL_PUR2,
  COL_WHITE1,
  COL_WHITE2
};

enum STRENGTH_STATE{
  STR_NONE,STR_LOW,STR_HIGH
};

void initializeMachine(Machine* machine);
void powerMachine(Machine* machine,boolean power);
void strengthMachine(Machine* machine, int str);
void colorMachine(Machine* machine, int color);
void executeMachine(Machine* machine,float humidity);
#endif
