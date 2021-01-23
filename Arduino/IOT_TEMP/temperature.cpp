#include "temperature.h"
#include "define.h"

DHT dht(DHTPIN,DHTTYPE);

void initializeDHT(){
  dht.begin();
}

boolean getTH(float* humidity, float* temperature){
  *humidity = dht.readHumidity();
  *temperature = dht.readTemperature();
  if(isnan(*humidity) || isnan(*temperature))
    return false; 

  else 
    return true; 
  
}

