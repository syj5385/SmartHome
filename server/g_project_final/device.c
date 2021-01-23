#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <string.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "device.h"
#include "database.h"
#include "protocol.h"
#include "packet.h"
#include "packetlist.h"

static MYSQL* mysql;

void initSem(){
	int fd_test = 0;
	FILE* file; 
	printf("device.c\t:D:\tInitialize mysql Semaphore\n");
	if((file = fopen("./mysql_sem","r+")) == NULL){
		printf("device.c\t:D:\tInitialize Semaphore (Create)\n");
		FILE* fd = fopen("./mysql_sem","w+");
		int sem = 1; 
		fprintf(fd,"%d",sem);
		fclose(fd);
		return;
	}

	int sem = 1; 
	fprintf(file,"%d",sem);
	fclose(file);

}

void enterSem(){
	FILE* sem_f; 

	sem_f = fopen("./mysql_sem","r+");
	int sem_val = 0;
	if(sem_f == NULL){
		printf("device.c\t:D:\tFailed to open Sem\n");
		return;
	}
	do{
		fscanf(sem_f,"%d",&sem_val);
	}while(sem_val == 0);
	int new_sem = 0;
	fprintf(sem_f,"%d",new_sem);
	printf("----------------------------------------------------------------\n");
	printf("			Enter Semaphore				\n");
	printf("----------------------------------------------------------------\n");
	fclose(sem_f);
		

}

void exitSem(){

	FILE* sem_f; 

	sem_f = fopen("./mysql_sem","r+");
	int sem_val = 0;
	if(sem_f == NULL){
		printf("device.c\t:D:\tFailed to open Sem\n");
		return;
	}

	printf("----------------------------------------------------------------\n");
	printf("			Exit Semaphore				\n");
	printf("----------------------------------------------------------------\n");

	int new_sem = 1;
	fprintf(sem_f,"%d",new_sem);
	fclose(sem_f);

}


int initializeDatabase(){

	enterSem();
	printf("device.c\t:D:\tInitialize Database and mysql mutex\n");
	mysql = (MYSQL*)malloc(sizeof(MYSQL));
	exitSem();
}

int initializeIOT_Device(){
	enterSem();

	mysql = mysql_init(NULL);

	// 1. create GeniusIOT Database
	createDB(mysql,DBNAME);

	// 2. connect to IOT Database
	connectDB(mysql, "GeniusIOT");
	printf("device.c\t:D:\tFinished to connect DB\n");

	// 3. create Table
	if(createDeviceTable(mysql, "LED")==-1){
//		printf("device.c\t:E:\tError : Failed to initialize LED device\n");
		return -1;
	}
	if(createDeviceTable(mysql, "Windows")==-1){
//		printf("device.c\t:E:\tError : Failed to initialize window device\n");
		return -1;
	}
	if(createDeviceTable(mysql, "Door")==-1){
//		printf("device.c\t:E:\tError : Failed to initialize door device\n");
		return -1;
	}
	if(createDeviceTable(mysql, "TEMP")==-1){
//		printf("device.c\t:E:\tError : Failed to initialize temperature device\n");
		return -1;
	}
	if(createDeviceTable(mysql, "GAS")== -1){
//		printf("device.c\t:E:\tError : FAiled to initialzie gas device\n");
		return -1;
	}

	printf("device.c\t:D:\tFinished to initialize IOT database\n");

	exitSem();
	return 1;

}

void freeDevice(Device* device){
	free(device->name);
	free(device->address);
	free(device->v1);
	free(device->v2);
	free(device);
}

int getDeviceUsingIDInDevice(int id, Device* device){
	device -> name = (char*)malloc(30);
	device -> address = (char*)malloc(18);
	device -> id = 0; 
	device -> type = 0 ;
	device -> v1 = (unsigned char*)malloc(sizeof(unsigned char));
	device -> v2 = (unsigned char*)malloc(sizeof(unsigned char));
	//int* success = (int*)malloc(sizeof(int));
	//*success = getDeviceUsingID(mysql,id, device);
	/*
	printf("fuc ss : %d\n",*success);
	
	if(*success = 1){
		char device_buf[100];
		bzero(device_buf, sizeof(device_buf));
		printf("address : %s\n",device->address);
	}
	freeDevice(device); 
	free(success);*/
	enterSem();
	int result = getDeviceUsingID(mysql,id,device);
	exitSem();

	return result;
}

void* showIOTTables(){
	
	enterSem();

	showTable(mysql);
	
	exitSem();

}

int addDeviceFromAndroid(PACKET* packet){
	printf("device.c\t:D:\tstart to add device in DB\n");
	int type; 
	char name[30];
	bzero(name,sizeof(name));
	char address[18] ;
	bzero(address,sizeof(address));

	int index = 0; 
	char temp; 
	// get device type
	while((temp = get_data_at(packet->parameter,index++)) != '%'){
		type = (int)temp & 0xff; 
	}

	// get device name
	int i =0; 
	while((temp = get_data_at(packet->parameter,index++)) != '%'){
	
		name[i++] = temp; 
	}

	i=0;
	while((temp = get_data_at(packet->parameter,index++)) != '%'){
		address[i++] = temp; 
	}

	printf("device.c\t:D:\tname : %s\t\ttype : %d\taddress : %s\n",name, type, address);
	enterSem();
	int result = addDeviceInDB(type,name,address);
	exitSem();

	return result; 

}

void removeDeviceFromAndroid(PACKET* packet){
	printf("device.c\t:D:\tstart to remove device in DB\n");
	int id = ((int)get_data_at(packet->parameter,0)) & 0xff;
	printf("device.c\t:D:\tRemove id = %d\n",id);

	removeDeviceInDB(id);
}

int addDeviceInDB(int type, char* name, char* MAC){
	// 1. allocate Device ID
	//initializeIOT_Device();
	
	enterSem();
	connectDB(mysql,"GeniusIOT");
	int id = 100;	
	char* table;
	int* exists ;

	while(1){
		//exists= getDeviceUsingIDInDevice(id);A

		Device* device = (Device*)malloc(sizeof(Device));
		device -> name = (char*)malloc(30);
		device -> address = (char*)malloc(18);
		device -> id = 0; 
		device -> type = 0 ;
		
		device -> v1 = (unsigned char*)malloc(sizeof(unsigned char));
		device -> v2 = (unsigned char*)malloc(sizeof(unsigned char));
		int* success = (int*)malloc(sizeof(int));
		int msuccess = getDeviceUsingID(mysql,id, device);
		if(msuccess == 0){
			break; 
		}
		id ++ ; 


		freeDevice(device);
		free(success);
	}
	printf("device.c\t:D:\tAllocated id = %d\n",id);
	switch(type){
		case 10 : // LED
			table = "LED";
			break; 

		case 20 : // DOOR
			table = "Door";
			break;

		case 30 : //WINDOW
			table = "Windows";
			break;

		case 40 : //Temperature
			table = "TEMP";
			break; 
			
		case 50 : //Gas sensor
			table = "GAS";
			break; 

		default : 
			printf("Unknown Device type\n");
			return -1;

			break;
	}


	printf("device.c\t:D:\t(before insert)name : %s\t\ttype : %d\taddress : %s\n",name, type, MAC);
	insertDataToTable(mysql, table, id, type,name, MAC, 100, 120);

	//disconnectDB(mysql);
	//
	exitSem();
	return id;

}

void* removeDeviceInDB(int id){
	//initializeIOT_Device();
	//
	enterSem();
	connectDB(mysql,"GeniusIOT");


	Device* device = (Device*)malloc(sizeof(Device));
	device -> name = (char*)malloc(15);
	device -> address = (char*)malloc(18);
	device -> id = 0; 
	device -> type = 0 ;
	
	device -> v1 = (unsigned char*)malloc(sizeof(unsigned char));
	device -> v2 = (unsigned char*)malloc(sizeof(unsigned char));
	int* success = (int*)malloc(sizeof(int));
	*success = getDeviceUsingID(mysql,id, device);
	if(*success == 0){
		printf("device.c\t:D:\tThis device does not exists\n");
		return NULL;
	}
	printf("device.c\t:D:\tTrying to remove device\n");


	deleteDataFromTable(mysql, device);
	
	updateDeviceIDAfterRemoving(id);
	exitSem();

	freeDevice(device);
	free(success);
	//disconnectDB(mysql);
}


void* updateDevice(int id, int type, int value1, int value2){

	enterSem();
	connectDB(mysql,"GeniusIOT");
	printf("device.c\t:D:\tmysql connect -> %p\n",mysql);
	updateDBusingID(mysql, id,  type, value1, value2);
	exitSem();

	showIOTTables();

	//disconnectDB(mysql);
}

int getSpecificDeviceFromDBtoString(PACKET* packet, int id){
	
	if(initializeIOT_Device() == -1){
		return -1;
	}
	char buffer[BUFSIZ];
	int success; 

	Device* device = (Device*)malloc(sizeof(Device));
	device -> name = (char*)malloc(30*sizeof(char));
	device -> address = (char*)malloc(18*sizeof(char));;
	device -> id = 0; 
	device -> type = 0 ;
	device -> v1 = (unsigned char*)malloc(sizeof(unsigned char));
	device -> v2 = (unsigned char*)malloc(sizeof(unsigned char));
	memset(device->name,'\0',sizeof(device->name));
	enterSem();
	success = getDeviceUsingID(mysql,id,device);
	exitSem();
	if(success == 0){
		printf("device.c\t:D:\tNo Device\b");
		return -1;
	
	}
	printf("device.c\t:D:\tWrite the device into packet\n");
	add_node_at(packet->parameter,packet->parameter->count, '&');


	//device id
	add_node_at(packet->parameter,packet->parameter->count, (unsigned char)device->id);
	add_node_at(packet->parameter,packet->parameter->count, '%');

	//device type
	add_node_at(packet->parameter, packet->parameter->count,device->type);
	add_node_at(packet->parameter,packet->parameter->count, '%');
		
	//device name
	int i=0; 
	for(i=0; i<30; i++){
		unsigned char name_temp = device->name[i];
		if(name_temp != '\0')
			add_node_at(packet->parameter, packet->parameter->count, device->name[i]);
		else{
			break; 
		}
	}

	add_node_at(packet->parameter,packet->parameter->count, '%');
	//device value
	//printf("device.c\t:D:\tvalue : %d \t|\t %d\n",*device->v1,*device->v2);

	unsigned char v1_Temp = (*device->v1);
	add_node_at(packet->parameter, packet->parameter->count, v1_Temp);
	add_node_at(packet->parameter,packet->parameter->count, '%');
	unsigned char v2_Temp = (*device->v2);
	add_node_at(packet->parameter, packet->parameter->count, v2_Temp);
	add_node_at(packet->parameter,packet->parameter->count, '%');
	free(device);

	add_node_at(packet->parameter,packet->parameter->count, '&');

	return 0;

}


int getAllDeviceFromDBtoString(PACKET* packet){
	enterSem();
	connectDB(mysql, "GeniusIOT");
	if(initializeIOT_Device() == -1){
		return -1;
	}

	char buffer[BUFSIZ]; 
	int id = 100;

	while(1){
		Device* device = (Device*)malloc(sizeof(Device));
		device -> name = (char*)malloc(30*sizeof(char));
		device -> address = (char*)malloc(18*sizeof(char));;
		device -> id = 0; 
		device -> type = 0 ;
		device -> v1 = (unsigned char*)malloc(sizeof(unsigned char));
		device -> v2 = (unsigned char*)malloc(sizeof(unsigned char));
		int* success = (int*)malloc(sizeof(int));
		memset(device->name,'\0',sizeof(device->name));
		//printf("device.c\t:D:\tdevice id = %d\n",id);
		*success = getDeviceUsingID(mysql,id++,device);
		if(*success == 0)
			break; 
		
	
		// new Device
		add_node_at(packet->parameter,packet->parameter->count, '&');


		//device id
		add_node_at(packet->parameter,packet->parameter->count, (unsigned char)device->id);
		add_node_at(packet->parameter,packet->parameter->count, '%');

		//device type
		add_node_at(packet->parameter, packet->parameter->count,device->type);
		add_node_at(packet->parameter,packet->parameter->count, '%');
		
		//device name
		int i=0; 
		for(i=0; i<30; i++){
			unsigned char name_temp = device->name[i];
			if(name_temp != '\0')
				add_node_at(packet->parameter, packet->parameter->count, device->name[i]);
			else{
				break; 
			}
		}

		add_node_at(packet->parameter,packet->parameter->count, '%');
		//device value
		//printf("device.c\t:D:\tvalue : %d \t|\t %d\n",*device->v1,*device->v2);

		unsigned char v1_Temp = (*device->v1);
		add_node_at(packet->parameter, packet->parameter->count, v1_Temp);

		add_node_at(packet->parameter,packet->parameter->count, '%');
		unsigned char v2_Temp = (*device->v2);
		add_node_at(packet->parameter, packet->parameter->count, v2_Temp);


		add_node_at(packet->parameter,packet->parameter->count, '%');


		freeDevice(device);
		free(success);
	}
	add_node_at(packet->parameter,packet->parameter->count,'&');
	exitSem();
	//disconnectDB(mysql);
	//
	return 0;
	//printf("Wow\n");


}

void updateDeviceIDAfterRemoving(int indexOfRemove){
	unsigned char count = (unsigned char)indexOfRemove+1;

	enterSem();
	Device* temp = (Device*)malloc(sizeof(Device));
	int success;

	while(1){
		if((success = getDeviceUsingIDInDevice(count,temp)) == 0){
			printf("device.\t:D:\tFinished!!!\n");
			break; 
		}
		printf("device.c\t:D\tsuccess : %d\n",success);
		updateIDusingID(mysql,temp->type,temp->id, count-1);
		count ++;
	}
	
	exitSem();

	showIOTTables();
}
