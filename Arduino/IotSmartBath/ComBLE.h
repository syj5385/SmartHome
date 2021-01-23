#ifndef _ComBLE_H_
#define _ComBLE_H_
#include <arduino.h>

typedef struct {
  /* temperature protocol 그대로 */
  int request_exe;
  int request_level;    
  int request_temp;       //protocol = real-15
  int my_exe;
  int my_level;
  int my_temp;            //protocol = real - 19 ( 20~49 :: 1~30 )
} BOX;

void initbt();
bool readData(uint8_t* buf);
void analysisCommand(uint8_t* buf,BOX* box);
void initialbox_my(BOX* box);
void sendData(BOX* box);
void print_box(BOX* box);






#endif
