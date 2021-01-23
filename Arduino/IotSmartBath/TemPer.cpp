#include "TemPer.h"
#include "INIT.h"
#include <arduino.h>
#include <OneWire.h>

OneWire TB(tempB);
OneWire TC(tempC);
OneWire TW(tempW);

void inittemp(){
  pinMode(tempB, INPUT);
  pinMode(tempC, INPUT);
  pinMode(tempW, INPUT);
}

void print_temp(int bath, float warm, float cool){
  Serial.print("\t\t");
  Serial.print("< TEMPER > bath : ");     Serial.print(bath);
  Serial.print("\t\tcool water : ");      Serial.print((int)cool);
  Serial.print("\t\twarm water : ");      Serial.println((int)warm);
}

float getTemp_Bath(){
  byte data[12];
  byte addr[8];
  if ( !TB.search(addr))
  {
      TB.reset_search();
      return -1000;
  }

  if ( OneWire::crc8( addr, 7) != addr[7])
  {
      Serial.println("CRC is not valid!");
      return -1000;
  }
  if ( addr[0] != 0x10 && addr[0] != 0x28)
  {
      Serial.print("Device is not recognized");
      return -1000;
  }

  TB.reset();
  TB.select(addr);
  TB.write(0x44,1); 

  byte present = TB.reset();
  TB.select(addr);    
  TB.write(0xBE);
  
  for (int i = 0; i < 9; i++)
  { 
    data[i] = TB.read();
  }
  
  TB.reset_search();
  
  byte MSB = data[1];
  byte LSB = data[0];

  float tempRead = ((MSB << 8) | LSB); 
  float TemperatureSum = tempRead / 16;
  
  return TemperatureSum; 
}
float getTemp_Cool(){
  byte data[12];
  byte addr[8];
  if ( !TC.search(addr))
  {
      TC.reset_search();
      return -1000;
  }

  if ( OneWire::crc8( addr, 7) != addr[7])
  {
      Serial.println("CRC is not valid!");
      return -1000;
  }
  if ( addr[0] != 0x10 && addr[0] != 0x28)
  {
      Serial.print("Device is not recognized");
      return -1000;
  }

  TC.reset();
  TC.select(addr);
  TC.write(0x44,1); 

  byte present = TC.reset();
  TC.select(addr);    
  TC.write(0xBE);
  
  for (int i = 0; i < 9; i++)
  { 
    data[i] = TC.read();
  }
  
  TC.reset_search();
  
  byte MSB = data[1];
  byte LSB = data[0];

  float tempRead = ((MSB << 8) | LSB); 
  float TemperatureSum = tempRead / 16;
  
  return TemperatureSum; 
}
float getTemp_Warm(){
  byte data[12];
  byte addr[8];
  if ( !TW.search(addr))
  {
      TW.reset_search();
      return -1000;
  }

  if ( OneWire::crc8( addr, 7) != addr[7])
  {
      Serial.println("CRC is not valid!");
      return -1000;
  }
  if ( addr[0] != 0x10 && addr[0] != 0x28)
  {
      Serial.print("Device is not recognized");
      return -1000;
  }

  TW.reset();
  TW.select(addr);
  TW.write(0x44,1); 

  byte present = TW.reset();
  TW.select(addr);    
  TW.write(0xBE);
  
  for (int i = 0; i < 9; i++)
  { 
    data[i] = TW.read();
  }
  
  TW.reset_search();
  
  byte MSB = data[1];
  byte LSB = data[0];

  float tempRead = ((MSB << 8) | LSB); 
  float TemperatureSum = tempRead / 16;
  
  return TemperatureSum; 
}
