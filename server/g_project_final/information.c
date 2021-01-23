#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <time.h>
#include "information.h"




void* printCommandInformation(){
	printUsage();

	printf("Commands : \n");
	printf("\tstart\t\tstart iot server\n");
	printf("\tdevice\t\tmanagement Device\n");
	printf("\thelp\t\tDisplay help informationm\n");


	printf("\n");
	printf("For more information about commands\n");
	printf("\tiot <command> --help\n");
}

void* printEachCommandInformation(char* command){
	
}

void* printUsage(){
	printf("Usage : \n");
	printf("\tiot <command> [option] [parameter]\n\n");
}


void printTime(){
	struct tm *tm_ptr;
	time_t m_time;	
	char time_buf[255];
	time(&m_time);
	tm_ptr = gmtime(&m_time);
	bzero(time_buf, sizeof(time_buf));
	sprintf(time_buf,"%d-%d-%d %d:%d:%d",
			tm_ptr -> tm_mday,
			tm_ptr -> tm_mon+1,
			tm_ptr->tm_year+1900,
			tm_ptr -> tm_hour+9,
			tm_ptr -> tm_min, 
			tm_ptr -> tm_sec);

	printf("time.c\t\t:D:\tTime : %s\n", time_buf);

}
