#ifndef _TEMPERATURE_H_
#define _TEMPERATURE_H_

#include <DHT.h>

void initializeDHT();
boolean getTH(float* humidity, float* temperature);

#endif /*TEMPTERATURE_H_*/

