#ifndef _UDP_H_
#define _UDP_H_

#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <pthread.h>
#include "packet.h"
#include "protocol.h"

#define HEADER		"#S>A"





Client* initializeAndroidClient();
void freeClient(Client* client);
void* initializeAndroidSocket(int* socket, struct sockaddr_in* serverAddr);
void* acceptClient(int* s_sock,int* c_sock, Client* client);
void sendBusyResultToClient(int sockfd, Client* client);
void sendNotConnectedResultToRaspberry(int sockfd, Client* client);
void sendPacketToClient(int sockfd, Client* client, PACKET* packet);
void sendResultOKResultToClient(int sockfd, Client* client);
void sendExecuteOKResultToClient(int sockfd, Client* client);
void sendCurrentDeviceToClient(int sockfd, Client* client);
void sendSpecificDeviceToClient(int sockfd, Client* client, int id);
void sendBleUpdateData(int sockfd, Client* client,int id);
#endif

