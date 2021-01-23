#ifndef _DATABASE_H_
#define _DATABASE_H_

#include <stdlib.h>
#include "/usr/include/mysql/mysql.h"
#include "protocol.h"


void* connectDB(MYSQL* conn, char* dbname);
void* disconnectDB(MYSQL* conn);
void* createDB(MYSQL* conn, char* dbname);
int createDeviceTable(MYSQL* conn, char* tablename);
void showTable(MYSQL* mysql);
void* printTableResult(MYSQL* conn);
void* printSelectResult(MYSQL* conn);
int  getDeviceUsingID(MYSQL* conn,int id,  Device* device);
int getDevice(MYSQL* conn, Device* device);
int updateDBusingID(MYSQL* mysql, int id, int type, int value1, int value2);

int updateIDusingID(MYSQL* conn, unsigned char type ,unsigned char prevID, unsigned char newID);


//for GeniusHome
void* insertDataToTable(MYSQL* conn, char* table, int id, int type, char* name, char* address, int v1, int v2);
void* deleteDataFromTable(MYSQL* conn, Device* device);




#endif
