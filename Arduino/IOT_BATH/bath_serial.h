#ifndef _BATH_SERIAL_H_
#define _BATH_SERIAL_H_

#include <arduino.h>
#include "define.h"
#include <SoftwareSerial.h>

void initializeSerial();
boolean readBTSerial(uint8_t* buf);
void createNewBathCommand(uint8_t* buf,BATH* bath);
void sendCommandToServer(BATH* bath);







#endif
