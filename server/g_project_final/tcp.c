#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>

#include <arpa/inet.h>
#include "tcp.h"
#include "packet.h"
#include "packetlist.h"

int success; 


Client* initializeTCPClient(){
	Client* client = (Client*)malloc(sizeof(Client));
	if(!client){
		return NULL;
	}
	client -> client_socket = (int*)malloc(sizeof(int));
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

void* initializeRaspberrySocket(int* s_sock,int* c_sock, struct sockaddr_in* serverAddr){
	success = 0; 
	serverAddr = (struct sockaddr_in*)malloc(sizeof(struct sockaddr_in));
	*s_sock = socket(AF_INET, SOCK_STREAM,0);
	if(*s_sock == 0 ){
		success = -1; 
		return &success;
	}
	//int flag = 120;
	//socklen_t size = sizeof(flag); 
	//if(setsockopt(*s_sock,SOL_SOCKET,SO_KEEPALIVE,&flag, size)){
	//	printf("tcp.c\t\t:E:\tFailed to set keepalive\n");
	//	exit(0);
	//}
	bzero(serverAddr, sizeof(*serverAddr));

	serverAddr -> sin_family = AF_INET;
	serverAddr -> sin_addr.s_addr = htonl(INADDR_ANY);
	serverAddr -> sin_port = htons(RASPBERRY_PORT);
	bind(*s_sock, (struct sockaddr*)serverAddr, sizeof(*serverAddr));

	
	return &success;
}

void* initializeAndroidTCPSocket(int* s_sock,int* c_sock, struct sockaddr_in* serverAddr){
	success = 0;
	serverAddr = (struct sockaddr_in*)malloc(sizeof(struct sockaddr_in));
	*s_sock = socket(AF_INET, SOCK_STREAM,0);
	if(*s_sock == 0 ){
		success = -1;
		return &success;						        
	}
	//int flag = 120;
	//socklen_t size = sizeof(flag);
	//if(setsockopt(*s_sock,SOL_SOCKET,SO_KEEPALIVE,&flag, size)){
	//	 printf("tcp.c\t\t:E:\tFailed to set keepalive\n");	
	//	 exit(0);
	//}
	bzero(serverAddr, sizeof(*serverAddr));
	serverAddr -> sin_family = AF_INET;
	serverAddr -> sin_addr.s_addr = htonl(INADDR_ANY);
	serverAddr -> sin_port = htons(ANDROID_PORT);
	int bind_result = bind(*s_sock, (struct sockaddr*)serverAddr, sizeof(*serverAddr));
	printf("tcp.c\t\t:D:\tAndroid Bind result = %d\n",bind_result);
	return &success;

}


void* acceptClient(int* s_sock,int* c_sock, Client* client){
	int listened;
	listened = listen(*s_sock,3);
	printf("tcp.c\t\t:D:\tTCP listening : %d\n", listened);
	if(listened < 0){
		printf("tcp.c\t\t:E:\tFailed to listening a client\n");
		return NULL;
	}

	*(client->len) = sizeof(client->client);
	printf("tcp.c\t\t:D:\tWait Client!!!!!\n");
	*c_sock = accept(*s_sock, (struct sockaddr*)client->client, client->len);
	printf("tcp.c\t\t:D:\tTCP connection Accepted : %s\n",inet_ntoa(client->client->sin_addr));

}


void sendConnectionOKResultToRaspberry(int sockfd, Client* client){
	PACKET* packet_send = (PACKET*)malloc(sizeof(PACKET));
	packet_send -> from = 'S';
	packet_send -> to = 'R';
	packet_send -> command = CONNECTION_OK;
	packet_send -> sizeofdata = 0;
	packet_send -> parameter = NULL;

	sendPacketToRaspberry(sockfd, client, packet_send);

}





void sendPacketToRaspberry(int sockfd, Client* client,PACKET* packet){
	char sendline[BUFSIZ];
	memset(sendline,'\0',sizeof(sendline));
	unsigned char checksum = 0; 
	// header
	sendline[0] = '#';
	sendline[1] = 'S'; 
	sendline[2] = '>';
	sendline[3] = packet->to;
	sendline[4] = (packet -> command & 0xff);
	checksum ^= (packet->command & 0xff);
	sendline[5] =( packet -> sizeofdata & 0xff);
	checksum ^= (packet -> sizeofdata & 0xff);

	int index =6; 
	if(packet ->sizeofdata != 0){
		int i;
		for(i=0; i<packet->sizeofdata; i++){
			unsigned char c = (unsigned char)(get_data_at(packet->parameter, i) & 0xff);
			sendline[index++] = c;

			checksum ^= c;
		}
	}
	sendline[index++] = checksum;
	sendline[index++] = '\0';
	printf("tcp.c\t\t:D:\tClient\t: %s\n",inet_ntoa(client->client->sin_addr));
	printf("tcp.c\t\t:D:\tfrom\t: %c\tto\t\t: %c\n",packet->from, packet->to);
	printf("tcp.c\t\t:D:\tcommand : %d\tparamSize\t: %d\n",packet->command, packet->sizeofdata);
	
	//sendto(sockfd,sendline,BUFSIZ, 0,(struct sockaddr*)client->client, *(client->len));
	send(sockfd, sendline,strlen(sendline),0);

	//free Packet
	if(packet->sizeofdata != 0)
		delete_all_list(packet->parameter);


	free(packet);
	
}



