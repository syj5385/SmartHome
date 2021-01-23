#ifndef _DEFINE_H_
#define _DEFINE_H_

#include <arduino.h>

typedef struct bath{
  uint8_t execution_request;
  uint8_t level_request;
  uint8_t temp_request;
  uint8_t execution_result;
  uint8_t level_result;
  uint8_t temp_result; 
}BATH; 



#endif
