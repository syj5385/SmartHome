#ifndef _WATER_H_
#define _WATER_H_

#define MUX_KEY0  4
#define MUX_KEY1  5
#define MUX_KEY2  6

#define MUX_OUTPUT  A2

#define BOUNDARY_OK 100

int getState(int level);
int getLevel();
void initailizeWater();


#endif
