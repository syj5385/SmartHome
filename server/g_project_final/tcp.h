#ifndef _TCP_H_
#define _TCP_H_

#include <stdlib.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include "protocol.h"


Client* initializeTCPClient();
void* initializeRaspberrySocket(int* s_sock,int* c_sock, struct sockaddr_in* serverAddr);
void* acceptClient(int* s_sock,int* c_sock, Client* client);
void sendConnectionOKResultToRaspberry(int sockfd, Client* client);
void sendPacketToRaspberry(int sockfd,Client* client, PACKET* packet);
void* initializeAndroidTCPSocket(int* s_sock,int* c_sock, struct sockaddr_in* serverAddr);

#endif
