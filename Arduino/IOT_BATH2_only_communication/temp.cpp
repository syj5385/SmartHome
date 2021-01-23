#include "temp.h"
#include <OneWire.h>

OneWire TB(tempB);
OneWire TC(tempC);
OneWire TW(tempW);

void initTemp(){
 pinMode(tempB,INPUT);
 pinMode(tempC,INPUT);
 pinMode(tempW,INPUT); 
}
