#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <pthread.h>
#include <semaphore.h>
#include "device.h"


#include "information.h"

void usage(){
	printf("Usage : iot <command> [optionm] [parameters]\n");
	printf("\tmore information -> iot help\n");
}

void* keyboardInput(void* arg);

int main(int argc,char** argv){

	/////////////////////////////// prepare child process //////////////////////////////
	char* r_comm = "./raspberry";
	char* r_arg[2]; 
	r_arg[0] = "./raspberry";
	r_arg[1] = (char*)0;
	int r_status, a_status; 

	char* a_comm = "./android";
	char* a_arg[2]; 
	a_arg[0] = "./android";
	a_arg[1] = (char*)0;

	char* iden_comm = "/usr/bin/python";
	char* i_arg[3];
	i_arg[0] = "/usr/bin/python";
	i_arg[1] = "./identification.py";
	i_arg[2] = (char*)0;
	/////////////////////////////////////////////////////////////////////////////////////

	pthread_t keyboardThread; 

	printf("\n\n");

	//pthread_create(&keyboardThread, NULL,keyboardInput,NULL);
	//pthread_detach(keyboardThread);

	

	//////////////////////////////// Prepare child Process ///////////////////////////////

	int raspberry_pid, android_pid, identification_pid; 

	//////////////////////////////////////////////////////////////////////////////////////
	printf("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~IOT information~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");

	if(argc == 1){
		// print Usage 
		usage();


	}
	else if(argc == 2){

		if(strcmp(argv[1],"start")== 0){
			sem_t* sem = (sem_t*)malloc(sizeof(sem_t));
			
			initSem();

			
			printf("main.c\t\t:D:\tStart IOT server\n");
			raspberry_pid = fork();
			if(raspberry_pid < 0){
				printf("main.c\t\t:D:\tFailed to create raspberry process\n");
				return 1;
			}
			else if(raspberry_pid == 0){
				printf("main.c\t\t:D:\tsuccess to create Raspberry process\n");
				execv(r_comm, r_arg);
			}
			
			sleep(1);
			android_pid = fork();
			if(android_pid < 0){
				printf("main.c\t\t:D:\tFailed to create android process\n");
				return 1;
			}
			else if(android_pid == 0){
				printf("main.c\t\t:D:\tsuccess to create android process\n");
				execv(a_comm, a_arg);
			}

			
			printf("main.c\t\t:D:\tFinished to allocate database\n");
			
			
			identification_pid = fork();
			if(identification_pid < 0){
				printf("main.c\t\t:D:\tFailed to create identification process\n");
				return 1;
			}
			else if(identification_pid == 0){
				printf("main.c\t\t:D:\tsuccess to create identification process\n");
				execv(iden_comm, i_arg);
			}
			//initializeDatabase();
			//initializeIOT_Device();
			while(1);
			wait(&a_status);
			wait(&r_status);

		}

		else if(strcmp(argv[1],"device")==0){
			printf("Device Command\n\n");
			printf("\tiot device [option] [parameter]\n");
			printf("\tIf you want more information for device command\n");
			printf("\tiot device --help\n");
		}
		else if(strcmp(argv[1],"help") == 0){	
			//evaluateArgument(argc,argv)
			printCommandInformation();
		}
		//if(strstr(argv[1],"--help") && strlen(argv[1]) == strlen("--help")){
		//	printf("IOT Server information\n");
		//}
	}
	else{	
		// multiple argument
		// 1. iot device --add [type] [name] [address]  -> Device value is automatically set [v1 = 100] [v2 = 100]
		// 2. iot device --remove [device_id] 
		// 3. iot device --update [device_id] [value1] [value2] 
		// 4. iot device --list		-> print my iot device list
		// 5. iot device --help 	-> more information for device command

		if(strcmp(argv[1],"device") == 0){
			if(strcmp(argv[2],"--help") == 0){
				printf("<Device Command>\n\n");
				printf("Usage :\n\tiot device [option]\n\n");
				printf("option :\n"); 
				printf("\t--add\t\tCreate new Device & add Device data to IOT Database\n");
				printf("\t\t\t-> iot device --add [type] [name] [MAC address]\n\n");
				printf("\t--remove\tRemove my IOT Device in Database\n");
				printf("\t\t\t-> iot device --remove [device id]\n\n");
				printf("\t--update\tUpdate Device state(value)\n");
				printf("\t\t\t-> iot device --update [device id] [value1] [value2]\n\n");
				printf("\t--list\t\tShow my IOT Device DB tables\n");
				printf("\t\t\t-> iot device --list\n\n");
				printf("\t--help\t\tShow information for device command\n");
				printf("\t\t\t-> iot device --help\n");
				
			}
			if(strcmp(argv[2],"--list") == 0){
				// print device command
				initializeIOT_Device();
				showIOTTables();
			}
			if(strcmp(argv[2],"--add") == 0){
				if(argc != 6){
					printf("<Device Command - Add Device Failed>\n");
					printf("\tUnknown type!!!!!!!!\n");
					printf("\tShow iot device --help\n");
				}else{
					printf("Add Device in Database\n");
					int type = atoi(argv[3]);
					char* name = argv[4];
					char* MAC = argv[5];

					printf("<New Device>\n");
					printf("type : %d\tname : %s\taddress : %s\n", type, name, MAC);

					addDeviceInDB(type, name,MAC);

					showIOTTables();
					
				}


			}
			
			if(strcmp(argv[2],"--remove") == 0){
				if(argc != 4){
					printf("<Device Command - Add Device Failed>\n");
					printf("\tUnknown type!!!!!!!!\n");
					printf("\tShow iot device --help\n");
				}
				else{
					printf("Remove Device in Database\n");

					removeDeviceInDB(atoi(argv[3]));

					showIOTTables();
				}
			}

			if(strcmp(argv[2],"--update") == 0){

			}

					

		}

	}

	

	printf("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~End of Server~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");
	
	return 0;
}


void* keyboardInput(void* arg){
	char keyboardLine[1000];
	int n; 
	

	while((n = read(0,keyboardLine,sizeof(keyboardLine))) <= 0){}
	
	keyboardLine[n] = '\0';
	printf("Keyboard Line : %s\n",keyboardLine);
	if(strcmp(keyboardLine,"exit")!=0){
		exit(0);
	}

	pthread_exit("Bye");
}

