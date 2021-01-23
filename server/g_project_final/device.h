#ifndef _DEVICE_H_
#define _DEVICE_H_

#include <stdlib.h>
#include "/usr/include/mysql/mysql.h"
#include "database.h"
#include "protocol.h"
#include "packetlist.h"
#include "packet.h"

#define DBNAME	"GeniusIOT"


void initSem();
int initializeDatabase();
void freeDevice(Device* device);
int initializeIOT_Device();
int getDeviceUsingIDInDevice(int id,Device* device);
void* showIOTTables();
int addDeviceFromAndroid(PACKET* packet);
int addDeviceInDB(int type, char* name, char* MAC);
void removeDeviceFromAndroid(PACKET* packet);
void* removeDeviceInDB(int id);
void* updateDevice(int id,int type, int value1, int value2);
int getAllDeviceFromDBtoString(PACKET* packet);

int getSpecificDeviceFromDBtoString(PACKET* packet, int id);

void updateDeviceIDAfterRemoving(int indexOfRemove);


#endif
