#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <pthread.h>
#include "udp.h"
#include "tcp.h"
#include "protocol.h"
#include "packet.h"
#include "information.h"
#include "device.h"

//function Proto type
int initializeFIFO(int* arfd, int* rafd);
void* requestRaspberryProcessing(void* client);
void* readIPS_FIFO(void* arg);
void sendAndroidConnectionTCP();
void sendAndroidDisConnectionTCP();
void evaluateFIFO_Command(unsigned char* fiforead);

//Extern variables
int* R_sock;
int *arfd, *rafd;
int* CR_sock;


Client* RPi ; 

pthread_mutex_t* client_mutex; 

int main(int argc, char** argv){
	
	printf("raspberry.c\t:D:\tRaspberry Server start\n");


//	RPi = (Client*)malloc(sizeof(Client));
	RPi = initializeTCPClient();
	

	initializeDatabase();
	initializeIOT_Device();

	char ra_buf[10];
	char ar_buf[10];
	int ra_n,ar_n ;

	arfd = (int*)malloc(sizeof(int));
	rafd = (int*)malloc(sizeof(int));

	pthread_t* fiforeadThread; 
	fiforeadThread = (pthread_t*)malloc(sizeof(pthread_t));

	pthread_t* raspberryReadThread;
	raspberryReadThread = (pthread_t*)malloc(sizeof(pthread_t));

	R_sock = (int*)malloc(sizeof(int));
	CR_sock = (int*)malloc(sizeof(int));
	client_mutex = (pthread_mutex_t*)malloc(sizeof(pthread_mutex_t));
	pthread_mutex_init(client_mutex, NULL);

	/////////////////////////////////////////////////

	struct sockaddr_in servAddr;
	struct sockaddr_in rasbAddr; 
	char readline[1000];

	int fifo; 
	/////////////////////////////////////////////////

	//////////////open FIFO//////////////////////////
	
	if((fifo=initializeFIFO(arfd, rafd)) != 0){
	
		printf("raspberry.c\t:E:\tError : Exit the raspberry Process\n");
		return 1; 
	}

	if(*(int*)initializeRaspberrySocket(R_sock,CR_sock,&servAddr) == -1){
		printf("raspberry.c\t:E:\tFailed to initialize RPi Socket\n");
		return 1;
	}
	
	printf("raspberry.c\t:D:\tSuccess to initialize RPi Socket\n");

	printf("raspberry.c\t:D:\tTest to FIFO communication\n");

	printf("raspberry.c\t:D:\tarfd : %d\trafd : %d\n",*arfd,*rafd);

	ar_n = read(*arfd,ar_buf, sizeof(ar_buf));
	printf("raspberry.c\t:D:\t<%s> from Android Process\n",ar_buf);
	if(ar_n < 0){
		printf("android.c\t:E:\tFailed to test fifo data\n");
		return 1;
	}
	printf("raspberry.c\t:D:\tSuccess to test fifo data : A->R\n");

	char* hello = "test";
	ra_n = write(*rafd,hello,sizeof(hello));	
	
	int n ; 
	
	//initializeIOT_Device();
	printf("raspberry.c\t:D:\tRaspberry TCP Lisening!!!!!\n");
	acceptClient(R_sock,CR_sock, RPi);	
	pthread_create(fiforeadThread, NULL,  readIPS_FIFO,NULL);
	//pthread_creat(raspbeIrryReadThread,NULL,readRaspberry,NULL);
	//pthread_detach(*fiforeadThread);

       	//int success = getDeviceUsingIDInDevice(102);
//	printf("success : %d\n",success);

	while(1){
		
		bzero(readline,sizeof(readline));

//		sendAndroidConnectionTCP();

		///////////////////////////////////////////////////////////////////////////////////////

		printf("raspberry.c\t:D:\tWait a packet from raspberry\n");
		n = recv(*CR_sock,readline,1000,0);
		//n = read(*CR_sock, readline, 1000);
		printf("raspberry.c\t:D:\trecv state : %d\n",n);
		if( n == 0){
			printf("raspberry.c\t:E:\tError : Disconnect Raspberry Client\n");
//			sendAndroidDisConnectionTCP();
			acceptClient(R_sock,CR_sock, RPi);
		}

		///////////////////////////////////////////////////////////////////////////////////////

		printf("\n");
		printf("==========================================================================\n");
		printf("==				Hello RPi				==\n");
		printf("==========================================================================\n");
		printTime();
		printf("raspberry.c\t:D:\tread line(TCP) -> %d-data read: %s\n",n,readline);
		pthread_mutex_lock(client_mutex);
		RPi -> id = 0 ;
		printf("raspberry.c\t:D:\tpacket Analysis\n");
		RPi -> packet  = makePacketUsingBuffer(readline);

		printf("raspberry.c\t:D:\taddress : %p\n",RPi -> client);
		printf("raspberry.c\t:D:\trequest Raspberry process Thread\n");

		pthread_create(RPi->thread, NULL,requestRaspberryProcessing,(void*)RPi);
		pthread_join(*RPi->thread, NULL);
		pthread_mutex_unlock(client_mutex);

	//	sendConnectionOKResultToRaspberry(*CR_sock, RPi);

		printf("\n");
		printf("==========================================================================\n");
		printf("==				Good Bye				==\n");
		printf("==				Raspberry				==\n");
		printf("==========================================================================\n\n");
	}

	close(*CR_sock);


	return 0;
}

int initializeFIFO(int* arfd, int* rafd){

	// 1. R : server / A : client

	unlink(FIFOAR); // delete exist fifo file
	if(mkfifo(FIFOAR, 0666) <0){
		printf("raspberry.c\t:D:\tFailed to create A->R fifo file\n");
		return 1;
	}
	printf("raspberry.c\t:D:\tSuccess to create A->R fifo file(R : server)\n");


	if((*arfd = open(FIFOAR, O_RDWR)) < 0){
		printf("raspberry.c\t:D:\tFailed to open fifo -> %d\n", *arfd);
		return 1;
	}

	printf("raspberry.c\t:D:\tSuccess to open A->R fifo file(R : Server)\n");
	
	/////////////////////////////////////////////////
	printf("raspberry.c\t:D:\tWait for android\n");
	sleep(2);
	printf("raspberry.c\t:D:\tStart raspberry open fifo\n");

	if((*rafd = open(FIFORA,O_WRONLY)) < 0){
		printf("raspberry.c\t:D:\tFailed to open fifo -> %d\n",*rafd);
		return 1;
	}
	printf("raspbery.c\t:D:\tSuccess to open R->A fifo file(A : Server)\n");



	return 0;
}


void* requestRaspberryProcessing(void* client){

	printf("raspberry.c\t:D:\tclient : %s\n",inet_ntoa(((Client*)client)->client->sin_addr));
	switch(((Client*)client)->packet->command){
		case CONNECTION_CHECK : 
			printf("raspberry.c\t:D:\tSend Connection Check Result to RaspberryPi\n");
			sendConnectionOKResultToRaspberry(*CR_sock, RPi);
			break;

		case BLE_DATA :
			printf("raspberry.c\t:D:\tReceived BluetoothLE data from Raspberry\n");
			Device* device; 
			int id =get_data_at(((Client*)client)->packet->parameter,0);
			int type =get_data_at(((Client*)client)->packet->parameter,1);
			int v1 =get_data_at(((Client*)client)->packet->parameter,2);
			int v2 =get_data_at(((Client*)client)->packet->parameter,3);
			unsigned char fifo_comm[2] = {BLE_DATA,(unsigned char)id};
			updateDevice(id,type,v1,v2);	
			int n = write(*rafd, fifo_comm,2);
			printf("raspberry.c.\t:D:\tDevice:%d, type:%d, v1:%d, v2:%d\n",id,type,v1,v2);
			break; 
	

	}
	pthread_exit("Bye");

}


void* readIPS_FIFO(void* arg){
	//unsigned char fromABuf[20];
	unsigned char* fromABuf = (unsigned char*)malloc(sizeof(unsigned char)*1024);

	int n;
	memset(fromABuf,'\0',sizeof(fromABuf));
	printf("raspberry.c\t:D:\tfinished to create Readfifo Thread\n");
	printf("raspberry.c\t:D:\tFinished to receive fifo data : %d\n", *arfd);


	while(1){
		memset(fromABuf,'\0',sizeof(fromABuf));
		//while((n = read(*arfd, fromABuf, sizeof(fromABuf))) > 0);

		n = read(*arfd, fromABuf,1024);
		printf("raspberry.c\t:D:\tReceived fifo data form android -> %d\n",n);
		//pthread_mutex_lock(client_mutex);
		evaluateFIFO_Command(fromABuf);
		//pthread_mutex_unlock(client_mutex);
	}

	pthread_exit("Bye");
}

void sendAndroidConnectionTCP(){
	char fifosend[2];
	fifosend[0] = CONNECTION_OK;
	fifosend[1] = 0;

	int n; 
	n = write(*rafd, fifosend, strlen(fifosend));	
	printf("android.c\t:D:\tsend connection ok using fifo\n");
	printf("android.c\t:D:\tfifo A->R Send %d-data\n", n);
}


void sendAndroidDisConnectionTCP(){
	char fifosend[2];
	fifosend[0] = CONNECTION_DISCONNECTED;
	fifosend[1] = 0;

	int n; 
	n = write(*rafd, fifosend, strlen(fifosend));	
	printf("android.c\t:D:\tfifo A->R Send %d-data\n", n);
}

void evaluateFIFO_Command(unsigned char* fiforead){
	
	unsigned char fifo_command = fiforead[0];
	int n ; 
	int i=0; 
	int index = 0;
	PACKET* packet = (PACKET*)malloc(sizeof(PACKET));
	packet->from = 'S';
	packet->to='R';
	switch(fifo_command){
		case UPDATE_DEVICE :
			packet->command = fiforead[index++] & 0xff; 
			packet->sizeofdata = fiforead[index++] & 0xff; 

			printf("raspberry.c\t:D:\tcommand : %d\n",(int)(packet->command& 0xff));
			printf("raspberry.c\t:D:\tparam size : %d\n",(int)(packet->sizeofdata  & 0xff));
			if((int)(packet->sizeofdata & 0xff)>0){
				packet->parameter = create_list();
			}
			for(i=0; i<(int)(packet->sizeofdata & 0xff);i++){
				unsigned char temp = (unsigned char)(fiforead[index++] & 0xff);; 
				add_node_at(packet->parameter, packet->parameter->count,temp);
			}


			sendPacketToRaspberry(*CR_sock,RPi, packet);
			printf("raspberry.c\t:D:\tRequested from android to update device\n");
			break; 

		case ADD_DEVICE : 
			printf("raspberry.c\t:D:\tRequested from android Process to add Device\n");
			packet->command = fiforead[index++] & 0xff;
			packet->sizeofdata = fiforead[index++] & 0xff;
			if((int)(packet->sizeofdata & 0xff)>0){
				packet->parameter = create_list();
			}
			for(i=0; i<(int)(packet->sizeofdata & 0xff);i++){
				unsigned char temp = (unsigned char)(fiforead[index++] & 0xff);; 
				add_node_at(packet->parameter, packet->parameter->count,temp);
				printf("!!!%d\n",(int)temp & 0xff);
			}

			printf("tcp.c\t\t:D:\tClient\t: %s\n",inet_ntoa(RPi->client->sin_addr));
			sendPacketToRaspberry(*CR_sock,RPi,packet);
			printf("raspberry.c\t:D:\tRequest from android to add device\n");

			break;

		case REMOVE_DEVICE : 
			printf("raspberry.c\t:D:\tRequested from android Process to remove Device\n");
			packet->command = REMOVE_DEVICE; 
			packet->sizeofdata = 1;
			index +=2 ;
			packet->parameter = create_list();
			add_node_at(packet->parameter,packet->parameter->count, fiforead[index++]);

			sendPacketToRaspberry(*CR_sock,RPi,packet);

			break; 

		default :

			break; 


	}
	printf("raspberry.c\t:D:\tEnd of Thread\n");

//	free(fiforead);
}	

