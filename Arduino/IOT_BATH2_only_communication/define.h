#ifndef _DEFINE_H_
#define _DEFINE_H_

#include <arduino.h>

#define L1 A2
#define L2 A3
#define L3 A4

#define pumpC 5
#define pumpW 6
#define pumpB 11
#define tempB 4
#define tempC 7
#define tempW 8
#define BUTTON  13
#define LedONOF 12
#define LedC  10
#define LedW  9

#define SEN_CHK 200
#define BATH_OFF  0
#define BATH_W3   10

#define BATH_W2 25
#define BATH_W1 30
#define BATH_EE 35
#define BATH_C1 50
#define BATH_C2 45
#define BATH_C3 60

#define tempL 1
#define tempM 3
#define tempH 6

#define level0  0 
#define level1  1
#define level2  2
#define level3  3

#define BOUND 111

#define OFF_bath  LOW
#define ON_bath HIGH
#define LED_ON  HIGH
#define LED_OFF LOW

#define LOOPTIME 500

typedef struct{
  uint8_t request_exe;
  uint8_t request_level; 
  uint8_t request_temp;
  uint8_t my_exe;
  uint8_t my_level;
  uint8_t my_temp; 
}BOX;

typedef int state_t;
typedef boolean exe_t;
#endif /* _DEFINE_H_ */
