
#include <stdlib.h>
#include <stdio.h>
#include "packet.h"
#include "packetlist.h"

int success1 = 0; 
PACKET* makePacketUsingBuffer(char* readline){
	PACKET* packet = (PACKET*)malloc(sizeof(PACKET));
	if(!packet){
		printf("packet.c\t:D:\tFailed to allocate Packet\n");
		success1 = 0 ;
		return NULL;
	}
	packet -> parameter = create_list();
	if(!packet -> parameter){
		printf("packet.c\t:D:\tFailed to create parameter list\n");
		success1= 0 ;

		return NULL;
	}

	int index = 0;
	
	packet -> from = readline[3];
	packet -> to = readline[1];
	packet -> command = readline[4]; 
	packet -> sizeofdata = readline[5]; 
	index = 6;
	int i=0;
	//printf("packet.c\t:D:\tsizeofdata : %d\n",packet->sizeofdata);
	for (i=0 ; i<(int)packet->sizeofdata; i++){
		//unsigned char temp = (unsigned char)(readline[index++] & 0xff);

		unsigned char temp = (unsigned char)(readline[index++]);
//		printf("packet.c\t:D:\ttemp : %d\n",temp);
		add_node_at(packet->parameter,packet->parameter->count, temp);
		
	}

	printf("packet.c\t:D:\tFinished to make packet structure\n");
	printf("packet.c\t:D:\tFrom : %c\tto : %c\n",packet -> from, packet -> to);
	printf("packet.c\t:D:\tCommand: %d\tsize : %d\n", (int)packet ->command, (int)packet->sizeofdata);
	
	for(index=0; index<packet->sizeofdata; index++){
		unsigned char temp = get_data_at(packet->parameter,index);
		printf("packet.c\t:D:\tparameter %d : %02x\n",index, temp);
	}
	
	success1 = 0; 
	return packet;

}

