#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <pthread.h>
#include <errno.h>

#include "protocol.h"
#include "udp.h"
#include "tcp.h"
#include "packetlist.h"
#include "packet.h"
#include "information.h"
#include "device.h"

//function Proto type
int initializeFIFO(int* rafd, int* arfd);
int requestAndroidProcessing(void* client);
void* sendRequestToRaspberry(PACKET* packet);
void* readIPS_FIFO(void* arg);
void* readTCP(void* arg);
void removeClient(Client* client);
void printClient();
int isConnectedTCP = 0;

// Extern Variables

int A_sock ;
int* CA_sock;
int *rafd, *arfd;

Client* android;

int numberOfClient = 0;
pthread_mutex_t mutex; 
pthread_mutex_t update_mutex; 

pthread_t tcp_readThread;
int* id; 
Client** client_list;
int main(int argc, char** argv){
	printf("android.c\t:D:\tAndroid Server start\n");
	id = (int*)malloc(sizeof(int));
	*id = 0; 
	/////////////////////////////////////////////////
	android = (Client*)malloc(sizeof(Client));
	android = initializeTCPClient();
	client_list = (Client**)malloc(sizeof(Client*) * 10);

	int ra_n, ar_n;
	char ar_buf[10];
	char ra_buf[10];

	struct sockaddr_in* servAddr = (struct sockaddr_in*)malloc(sizeof(struct sockaddr_in));
	struct sockaddr_in* andrAddr = (struct sockaddr_in*)malloc(sizeof(struct sockaddr_in));

	//char readline[BUFSIZ];
	unsigned char readline[1000];
//	readline = (unsigned char*)malloc(sizeof(BUFSIZ));
	
	rafd = (int*)malloc(sizeof(int));
	arfd = (int*)malloc(sizeof(int));
	CA_sock = (int*)malloc(sizeof(int));

	pthread_mutex_init(&mutex,NULL);
	pthread_mutex_init(&update_mutex,NULL);
	
	int fifo;
	/////////////////////////////////////////////////a

	if(fifo = initializeFIFO(rafd, arfd) != 0){
		printf("android.c\t:E:\tError : Exit the android Process\n");
		return 1;
	}
	
	printf("android.c\t:D:\tSuccess to initialize Android Socket\n");

	char* hello = "Test A";
	ar_n = write(*arfd, hello,strlen(hello));

	ra_n = read(*rafd, ra_buf,sizeof(ra_buf));
	printf("android.c\t:D:\t<%s> from Raspberry process\n",ra_buf);
	if(ra_n < 0){
		printf("android.c\t:D:\tFailed to test fifo communication\n");
		return 1;
	}
	printf("android.c\t:D:\tSuccess to test fifo data : R->A\n");

	pthread_t* fiforeadThread = (pthread_t*)malloc(sizeof(pthread_t));
	pthread_create(fiforeadThread,NULL,readIPS_FIFO,NULL);
	//if(*(int*)initializeAndroidTCPSocket(&A_sock,CA_sock,servAddr) == -1){
	//	printf("android.c\t:E:\tFailed to initialize Android Socket\n");
	//	return 1;
	//}
	socklen_t len; 
	
	int* android_sock = (int*)malloc(sizeof(int));
	int* client_sock = (int*)malloc(sizeof(int));
	struct sockaddr_in* serverAddr;

	if(*(int*)initializeAndroidTCPSocket(android_sock,client_sock,serverAddr) == -1){
		printf("android.c\t:D:\tFailed to initialize Android Socket in Loop\n");			
		exit(1);
	}

	while(1){
		printf("android.c\t:D:\tWait for new Client\n");
		struct sockaddr_in* clientAddr = (struct sockaddr_in*)malloc(sizeof(struct sockaddr_in));
		Client* client = initializeTCPClient();
		acceptClient(android_sock,client->client_socket,client);
		pthread_mutex_lock(&mutex);
		client -> id = *id;
		printf("android.c\t:D:\tThis Client id : %d\n",client->id);
		client_list[*id] = client;
		*id = (*id) + 1;
		pthread_mutex_unlock(&mutex);
		printf("android.c\t:D:\taddress : %p\tid : %d\n",client->client,client->id);
		pthread_create(client->thread, NULL,readTCP,(void*)client);
		pthread_detach(*client->thread);
		printf("android.c\t:D:\trequest Android process Thread\n");
		int result;
	}

	pthread_mutex_destroy(&mutex);
	return 0;
}


void* readTCP(void* arg){
	printf("android.c\t:D:\tTCP readThread\n");
	Client* new = (Client*)arg;

	char readbuf[BUFSIZ];
	*(new -> len) = sizeof(new->client);
	////////////////////////////////////////////////////////////////////////////
	//n = recvfrom(A_sock,readline,BUFSIZ,0,(struct sockaddr*)new -> client,new->len);
	while(1){
		printf("android.c\t:D:\tRead TCP Thread Wait\n");
		bzero(readbuf,sizeof(readbuf));
		printf("android.c\t:D:\tWait a packet from %s\n",inet_ntoa(new->client->sin_addr));
		int n = recv(*(new->client_socket),readbuf,BUFSIZ,0);
		printf("android.c\t:D:\tRead -> %d\n",n);
		if(n == 0)
			break;
		new->packet = makePacketUsingBuffer(readbuf);
		int result = requestAndroidProcessing((void*)new);		
		printf("android.c\t:D:\tProcess Result : %d\n",result);
	}

	removeClient(new);
	printf("android.c\t:D:\tExit read TCP Thread\n");
}
int initializeFIFO(int* rafd, int* arfd){
	// 1. R : server / A : client
	unlink(FIFORA); // delete exist fifo file
	if(mkfifo(FIFORA, 0666) <0){
		printf("android.c\t:D:\tFailed to create R->A fifo file(A : Server)\n");
		return 1;
	}
	printf("android.c\t:D:\tSuccess to create R->A fifo file(A : Server)\n");

	if((*rafd = open(FIFORA, O_RDWR)) < 0){
		printf("android.c\t:D:\tFailed to open fifo -> %d\n", *rafd);
		return 1;
	}

	printf("android.c\t:D:\tSuccess to open R->A fifo file(A : Server)\n");
	/////////////////////////////////////////////////
	if((*arfd = open(FIFOAR,O_WRONLY)) < 0){
		printf("android.c\t:E:\tFailed to open fifo -> %d\n",*arfd);
		return 1;
	}
	printf("android.c\t:D:\tSuccess to open A->R fifo file(R : Server)\n");

	return 0;
}
	
int requestAndroidProcessing(void* client){
	Client* new = (Client*)client; 
	int result = 1; 
	printf("--------------------------------------------------------------------------\n");
	printf("--				 Hello					--\n");
	printf("--				 Client					--\n");
	printf("--------------------------------------------------------------------------\n\n");
	char ar_buf[1000];
	char ra_buf[1000];
	
	char readline[10];
	int i=0 ;
	for(i=0; i<9; i++){
		readline[i] = 'A';
	}
	readline[9] = '\0';

	int isSend; 
	socklen_t len = sizeof(new->client);

	printf("android.c\t:D:\tStart Android Process(Thread)\n");
	printf("android.c\t:D:\tClient : %s\n", inet_ntoa(((Client*)client)->client->sin_addr));
	
	unsigned char command = new->packet->command;
	unsigned char sizeofdata;
	
	pthread_mutex_lock(&update_mutex);
	switch(new->packet->command){
		case UPDATE_DEVICE : 
			printf("android.c\t:D:\ttry Update Device(Thread)\n");

			command = new ->packet->command;
			sizeofdata = new->packet->sizeofdata;
			int i=0; 
			printf("android.c\t:D:\tcommand : %d\n",(int)(command& 0xff));
			printf("android.c\t:D:\tsize	: %d\n",(int)(sizeofdata& 0xff));
			for(i=0; i<(int)(new -> packet->sizeofdata&0xff); i++){
				int temp = (int)((get_data_at(new->packet->parameter,i)) & 0xff);		
			}
			printf("android.c\t:D:\tSuccess to enter the critical region\n");
			int id = (int)get_data_at(new ->packet->parameter,0) & 0xff;
			int type = (int)get_data_at(new->packet->parameter,2) & 0xff;
			int value1 = (int)get_data_at(new->packet->parameter,4) & 0xff;
			int value2 = (int)get_data_at(new->packet->parameter,6) & 0xff;
			updateDevice(id, type, value1, value2);
			sendRequestToRaspberry(new-> packet);
			sendExecuteOKResultToClient(*new->client_socket, new);
			break; 

		case GET_DEVICE : 
			printf("android.c\t:D:\tRequest Current Device from Android smart Phone\n");
			sizeofdata = new->packet->sizeofdata;
			sendCurrentDeviceToClient(*new->client_socket, new);
			break; 

		case ADD_DEVICE : 
			printf("android.c\t:D:\tRequest to add device from android smart phone.\n");
			int id_Add = addDeviceFromAndroid(new->packet);
			printf("Add id : %d\n",id_Add);
			new->packet->sizeofdata += 2;
			add_node_at(new->packet->parameter,0,(unsigned char)id_Add);
			add_node_at(new->packet->parameter,1,(unsigned char)'%');
			sendRequestToRaspberry(new->packet);
			sendResultOKResultToClient(A_sock, (Client*)client);
			break; 

		case REMOVE_DEVICE :
			printf("android.c\t:D:\tRequest to remove device from android smartphone\n");
			sendRequestToRaspberry(new->packet);
			removeDeviceFromAndroid(new->packet);
			sendResultOKResultToClient(*new->client_socket, new);
			break; 

		case HELLO_ANDROID : 
			printf("androidc.c\t:D:\tHello Android Smart Phone\n");
			sendResultOKResultToClient(*new->client_socket,new);
			result = 2;
			break; 


		default : 
			printf("android.c\t:E:\tUnknown Command type(Thread)\n");
			result = -1;
			break; 
	}
	pthread_mutex_unlock(&update_mutex);
	
	printf("\n");
	printf("--------------------------------------------------------------------------\n");
	printf("--				Good Bye				--\n");
	printf("--				 Client					--\n");
	printf("--------------------------------------------------------------------------\n\n");
	return result;
}

void* sendRequestToRaspberry(PACKET* packet){

	// <Command> <sizeofData> <parameters> 

	unsigned char pi_Buf[50]; 
	memset(pi_Buf,'\0',sizeof(pi_Buf));
	pi_Buf[0] = packet->command & 0xff; 
	pi_Buf[1] = packet->sizeofdata & 0xff; 
	int index = 2;
	int i =0; 
	for (i=0; i<(int)(packet->sizeofdata & 0xff) ; i++){
		unsigned char temp = get_data_at(packet->parameter,i) & 0xff; 
		pi_Buf[index++] = temp; 
	}
	
	
	int n;

	n = write(*arfd,pi_Buf,50) ; 
	//while((n = write(*arfd, pi_Buf, 20)) > 0){
	printf("android.c\t:D:\tfifo A->R Send %d-data\n", n);
	//}
}

void* readIPS_FIFO(void* arg){
	unsigned char fromABuf[20];
	int n;
	memset(fromABuf,'\0',sizeof(fromABuf));
	printf("android.c\t:D:\tfinished to create Readfifo Thread\n");
	printf("android.c\t:D:\tFinished to receive fifo data : %d\n", *arfd);


	while(1){
		//while((n = read(*arfd, fromABuf, sizeof(fromABuf))) > 0);

		n = read(*rafd, fromABuf,sizeof(fromABuf));
		printf("android.c\t:D:\tn = %d\n",n);
		if(fromABuf[0] == BLE_DATA){
			int id = ((int)fromABuf[0]&0xff);
			printf("android.c\t:D:\tBLE data updated -> %d\n",id);
			int i=0;
			for(i=0; i<10; i++){
				if(client_list[i] != NULL)
					sendBleUpdateData(*(client_list[i]->client_socket), client_list[i],((int)fromABuf[1])&0xff);
			}
		}
		else if(fromABuf[0] == CONNECTION_DISCONNECTED){
			printf("android.c\t:D:\tDisconneted to raspberry TCP Connection\n");

		}

	
	}

	pthread_exit("Bye");
}

void removeClient(Client* client){
	printf("android.c\t:D:\tRemove ID : %d\n",client->id);
	int index = 0; 
	int i=0; 

	for(int k=0 ; k<10; k++){
		if(client_list[k] != NULL){
			printf("%d : %d\n",k,client_list[k] -> id);
		}
	}

	// First remove
	for(i=0; i<10; i++){
		if(client_list[i]->id == client->id){
			client_list[i] = 0;
			break;
		}
	}
	int j=0;

	pthread_mutex_lock(&mutex);
	*id = (*id) - 1; 
	
	client_list[i] = client_list[(*id)];
	client_list[*id] = 0;
	pthread_mutex_unlock(&mutex);
}

