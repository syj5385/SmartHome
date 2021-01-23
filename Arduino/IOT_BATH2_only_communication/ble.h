#ifndef _BLE_H_
#define _BLE_H_

#include "define.h"
#include "arduino.h"


#define BAUDRATE  9600

void initializeBLE();
boolean readBtData(uint8_t* buf);
void makeBox(uint8_t* buf, BOX* box);
void sendData(BOX* box);

#endif /*_BLE_H */
