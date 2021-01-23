#ifndef _PACKET_H_
#define _PACKET_H_

#include <stdlib.h>
#include "packetlist.h"

typedef struct{
	char from;
	char to;
	unsigned char command;
	unsigned char sizeofdata; 
	LLIST* parameter;
}PACKET; 


PACKET* makePacketUsingBuffer(char* readline);


#endif
