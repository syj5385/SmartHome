#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <pthread.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <errno.h>
#include <errno.h>
#include "udp.h"
#include "protocol.h"
#include "packetlist.h"
#include "device.h"

int success = 0; 
void* initializeAndroidSocket(int* sock, struct sockaddr_in* serverAddr){
	success = 0; 
	serverAddr = (struct sockaddr_in*)malloc(sizeof(struct sockaddr_in));
	*sock = socket(AF_INET, SOCK_STREAM,0);
	if(*sock == 0 ){
		success = -1; 
		return &success;
	}
	int flag = 120;
	socklen_t size = sizeof(flag);
	if(setsockopt(*sock,SOL_SOCKET,SO_KEEPALIVE,&flag, size)){
		printf("tcp.c\t\t:E:\tFailed to set keepalive\n");
		exit(1);
	}
	bzero(serverAddr, sizeof(*serverAddr));

	serverAddr -> sin_family = AF_INET;
	serverAddr -> sin_addr.s_addr = htonl(INADDR_ANY);
	serverAddr -> sin_port = htons(ANDROID_PORT);
	bind(*sock, (struct sockaddr*)serverAddr, sizeof(*serverAddr));

	
	return &success;
}


Client* initializeAndroidClient(){
	Client* client = (Client*)malloc(sizeof(Client));
	if(!client){
		return NULL;
	}
	client -> client = (struct sockaddr_in*)malloc(sizeof(struct sockaddr_in));
	client -> thread = (pthread_t*)malloc(sizeof(pthread_t));
	client -> len = (int*)malloc(sizeof(int));

	if(!client->client){
		free(client);
		return NULL;
	}
	if(!client -> thread){
		free(client->client);
		free(client);
		return NULL;
	}

	if(!client -> len){
		free(client->client);
		free(client->thread);
		free(client);
		return NULL;
	}

	return client;

}


void freeClient(Client* client){
	free(client->len);
	free(client->client);
	delete_all_list(client->packet->parameter);
	free(client->packet);
	free(client->thread);
	printf("udp.c\t\t:D:\tSuccess to free Client\n");

}


void sendBusyResultToClient(int sockfd, Client* client){

	PACKET* packet = (PACKET*)malloc(sizeof(PACKET));
	packet->from = 'S';
	packet->to = 'A';
	packet->command = RESULT_BUSY;
	packet->sizeofdata = 0;
	packet->parameter = NULL;
	sendPacketToClient(sockfd, client, packet);
}

void sendNotConnectedResultToRaspberry(int sockfd, Client* client){
	PACKET* packet_send = (PACKET*)malloc(sizeof(PACKET));
	packet_send -> from = 'S';
	packet_send -> to = 'R';
	packet_send -> command = RESULT_NOTCONNECTED;
	packet_send -> sizeofdata = 0;
	packet_send -> parameter = NULL;

	sendPacketToClient(sockfd, client, packet_send);
}

void sendResultOKResultToClient(int sockfd, Client* client){
	printf("udp.c.\t\t:D:\tsend ResultOK to android\n");
	PACKET* packet_send = (PACKET*)malloc(sizeof(PACKET));
	packet_send -> from = 'S';
	packet_send -> to = 'A';
	packet_send -> command = RESULT_OK;
	packet_send -> sizeofdata = 0;
	packet_send -> parameter = NULL;

	sendPacketToClient(sockfd, client, packet_send);
}

void sendBleUpdateData(int sockfd, Client* client,int id){

	PACKET* packet_send = (PACKET*)malloc(sizeof(PACKET));
	packet_send -> from = 'S';
	packet_send -> to = 'A';
	packet_send -> command = BLE_DATA;
	packet_send -> sizeofdata = 1;
	packet_send -> parameter = create_list();
	add_node_at(packet_send->parameter, 0,id);
	sendPacketToClient(sockfd, client, packet_send);
}

void sendExecuteOKResultToClient(int sockfd, Client* client){
	PACKET* packet_send = (PACKET*)malloc(sizeof(PACKET));
	packet_send -> from = 'S';
	packet_send -> to = 'A';
	packet_send -> command = UPDATE_DEVICE;
	packet_send -> sizeofdata = 1;
	packet_send -> parameter = create_list();
	unsigned char up_id = get_data_at(client->packet->parameter, 0);
	add_node_at(packet_send->parameter, 0,up_id);
	sendPacketToClient(sockfd, client, packet_send);
}
void sendPacketToClient(int sockfd, Client* client, PACKET* packet){
	unsigned char sendline[BUFSIZ];
	bzero(sendline,sizeof(sendline));
	unsigned char checksum = 0; 
	// header
	sendline[0] = '#';
	sendline[1] = 'S'; 
	sendline[2] = '>';
	sendline[3] = packet->to;
	sendline[4] = packet -> command;
	checksum ^= packet->command;
	sendline[5] = packet -> sizeofdata;
	checksum ^= packet -> sizeofdata; 

	int index =6; 
	if(packet ->sizeofdata != 0){
		int i;
		for(i=0; i<packet->sizeofdata; i++){
			unsigned char c = get_data_at(packet->parameter, i);
			sendline[index++] = c;

			checksum ^= c;
		}
	}
	sendline[index++] = checksum;
	sendline[index++] = '\0';
	//printf("udp.c\t\t:D:\tClient\t: %s\n",inet_ntoa(client->client->sin_addr));
	//printf("udp.c\t\t:D:\tfrom\t: %c\tto\t\t: %c\n",packet->from, packet->to);
	//printf("udp.c\t\t:D:\tcommand : %d\tparamSize\t: %d\n",packet->command, packet->sizeofdata);
	send(sockfd,sendline,strlen(sendline),0);
	//free Packet
	if(packet->sizeofdata != 0)
		delete_all_list(packet->parameter);	
}

void sendCurrentDeviceToClient(int sockfd, Client* client){

	printf("udp.c\t\t:D:\tPrepare Current device\n");
	PACKET* packet_send = (PACKET*)malloc(sizeof(PACKET));
	packet_send -> from = 'S';
	packet_send -> to = 'A';
	packet_send -> command = GET_DEVICE;
	packet_send -> parameter= create_list();

	
	getAllDeviceFromDBtoString(packet_send);
	packet_send -> sizeofdata = packet_send->parameter->count;
	sendPacketToClient(sockfd,client,packet_send);
}

void sendSpecificDeviceToClient(int sockfd, Client* client, int id){

	printf("udp.c\t\t:D:\tPrepare Specific device\n");
	PACKET* packet_send = (PACKET*)malloc(sizeof(PACKET));
	packet_send -> from = 'S';
	packet_send -> to = 'A';
	packet_send -> command = GET_ONE_DEVICE;
	packet_send -> parameter= create_list();

	
	if(getSpecificDeviceFromDBtoString(packet_send,id)==-1){
		printf("device.c\t:E:\tFailed to getDevice\n");
		return;
	}
	packet_send -> sizeofdata = packet_send->parameter->count;
	printf("udp.c\t\t:D:\tPrepared Specific device\n");
	sendPacketToClient(sockfd,client,packet_send);
}
