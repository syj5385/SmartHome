#ifndef _PROTOCOL_H_
#define _PROTOCOL_H_

#include <stdlib.h>
#include "packet.h"
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#define FIFOAR	"fifoar"
#define FIFORA	"fifora"


#define RASPBERRY_PORT	11111
#define ANDROID_PORT	11112


typedef struct{
	unsigned char id;
	unsigned char type;
	char* name;
	char* address;
	unsigned char* v1; 
	unsigned char* v2;
}Device; 

typedef struct{
	int* client_socket;
	socklen_t* len; 
	int id; 
	struct sockaddr_in* client;
	PACKET* packet;
	pthread_t* thread;
}Client; 

// Request from Android		(11 ~ 30)	S <- A
#define		UPDATE_DEVICE	11
#define 	GET_DEVICE 	12
#define		ADD_DEVICE 	13
#define 	REMOVE_DEVICE 	14
#define		GET_ONE_DEVICE	15
#define		HELLO_ANDROID 	16

// Result to Android		(31 ~ 50)	S -> A

#define		RESULT_BUSY	30
#define		RESULT_OK	31
#define 	RESULT_NOTCONNECTED 32




// Request from Raspberry Pi	(61 ~ 80)	S <- R

#define		CONNECTION_CHECK	61
#define		BLE_DATA		62


// Result to Raspberry Pi	(81 ~ 100)	S -> R

#define 	CONNECTION_OK		81
#define 	CONNECTION_DISCONNECTED 82



#endif

