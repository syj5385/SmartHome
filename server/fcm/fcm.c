#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <arpa/inet.h>
#include <assert.h>
#include <errno.h>
#include <netinet/in.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <netdb.h>
#include <unistd.h>
#include "fcm.h"
#include "define.h"
#include <time.h>



extern int h_errno;

//const char *apikey = "AAAAld8VmKA:APA91bH2-uenORj8r10LPPhBXLEd40klN6yfERATwWc1IxPRIWl1qdY3rJzRiT_FbpA66lS_wty7NdpuLASxSg-oHRcF04j3qSSWP4RPcOVKk43ffprCOLyBWMKaGj2k5oz-Z5P2ybOX";

const char *apikey = "AIzaSyAAfZNXZoCxhMbohgcb130lzB4rVrgogXQ";
//const char *apikey = "AIzaSyCPDsonl9yzjYkkXUbBrGyLTUk8ryUMveY";

int process_http(int* sockfd, char* host, char* page, char* poststr){
	char sendline[MAXLINE + 1], recvline[MAXLINE+1];
	ssize_t n ;
	snprintf(sendline,MAXSUB,
			"POST %s HTTP/1.1\r\n"
			//"HOST: %s\r\n"
			"Content-Type: application/json\r\n"
			"Authorization:key=%s\r\n"
			"Content-Length: %d\r\n\r\n"
			"%s",page,apikey, (int)strlen(poststr),poststr);

	printf("sendline : \n%s\n",sendline);
	write(*sockfd, sendline, strlen(sendline));
	while((n = read(*sockfd, recvline,MAXLINE)) > 0){
		recvline[n] = '\0';
//		printf("%s\n",recvline);
	}

	printf("Receive line : %s\n",recvline);

	
	if(strstr(recvline,"HTTP/1.0 200 OK") != NULL){
		printf("gcm.c\t\t:D:\tSuccess to request GCM Server\n");
		return true;
	}
	else{
		printf("gcm.c\t\t:E:\tFailed to request GCM Server\n");
		return false;
	}
}

int writeGcmSendLog(char* device, char* content){
	FILE* file = fopen("/home/jjun/Desktop/g_project_new/gcm_log","a+");
	if(file == NULL)
		return false;

	fseek(file, 0,SEEK_END);

	struct tm *tm_ptr; 
	time_t m_time;
	char log_buf[300];
	time(&m_time);
	tm_ptr = gmtime(&m_time);
	bzero(log_buf,sizeof(log_buf));

	fprintf(file,"%d-%d-%d %d:%d:%d\t%s\t%s\n",
			tm_ptr -> tm_mday,
			tm_ptr -> tm_mon+1,
			tm_ptr -> tm_year + 1900,
			tm_ptr -> tm_hour + 9,
			tm_ptr -> tm_min,
			tm_ptr -> tm_sec, 
			device, content);
	
	fclose(file);
}

int isSendGcm(){
	FILE* file = fopen("./gcm_log","r");
	if(file == NULL){
		/* send GCM message because no gcm message is sent before */
		/* GCM Log file does not exist */ 
		return true; 
	}

	char readbuf[150];
	char backbuf[150];
	
	char* sPtr; 
	
	bzero(readbuf,sizeof(readbuf));

	
	while((sPtr = fgets(readbuf, 150, file)) != NULL){
		strcpy(backbuf,readbuf);
	}
	char* time_prev = strtok(backbuf, "\t");

	// get Log time	
	printf("gcm.c\t\t:D:\tLast Time : %s\n",time_prev);

	struct tm *tm_ptr; 
	time_t m_time;
	char log_buf[300];
	time(&m_time);
	tm_ptr = gmtime(&m_time);

	char* date = strtok(time_prev," ");
	char time_current[150];
	sprintf(time_current,"%d-%d-%d",tm_ptr -> tm_mday, tm_ptr -> tm_mon+1, tm_ptr -> tm_year+1900);
	if(strcmp(time_current, date) != 0){
		// Different date 
		// Send anroid gcm Message
		return true; 
	}
	printf("gcm.c\t\t:D:\tSame date\n");
	// time check
	
	date = strtok(NULL," ");
	sprintf(time_current,"%d:%d:%d",tm_ptr -> tm_hour+9, tm_ptr -> tm_min, tm_ptr -> tm_sec);
	
	int hour = atoi(strtok(date, ":"));
	int min = atoi(strtok(NULL, ":"));
	int sec = atoi(strtok(NULL, ":"));
	if(hour != (tm_ptr-> tm_hour+9))
		return true; 

	if( (tm_ptr -> tm_min) - min > 0){
		return true; 
	}

	else 
		return false; 
}

int sendGcmMessageToAndroid(char* title, char* text, char* who){

	int* sockfd = (int*)malloc(sizeof(int));
	struct sockaddr_in* servaddr = (struct sockaddr_in*)malloc(sizeof(struct sockaddr_in));
	char** pptr;
	char* hname = "fcm.googleapis.com";
	char* page = "https://fcm.googleapis.com/fcm/send";
	
	//char* page = "https://fcm.googleapis.com/v1/projects/tcpdatafcm/messages:send";
	//char* page = "https://fcm.googleapis.com/fcm/send";

	char poststr[MAXLINE];
	bzero(poststr,sizeof(poststr));
	sprintf(poststr,"{"
			"\"registration_ids\":\"%s\","
			"\"notification\": {"
				"\"title\":\"%s\""
			"}"
		"}",who,title);
	/*sprintf(poststr, "{ \"notification\": {"
	*	"\"title\": \"%s\","
	*	"\"content\": \"%s\" }"
	*	"\"to\" : \""
	*	"%s"
		"\"}\r\n",title,text,who);
	*/
	//printf("%s\n",poststr);
	printf("gcm.c\t\t:D:\ttitle : %s\n",title);
	printf("gcm.c\t\t:D:\ttext : %s\n",text);


	char str[50];
	struct hostent *hptr; 
	if((hptr = gethostbyname(hname)) == NULL){
       		printf("gcm.c\t\t:E:\tgethostbyname error for host : %s: %s",hname, hstrerror(h_errno));
		return false; 
	}

	printf("gcm.c\t\t:D:\thostname : %s\n", hptr -> h_name);
	if(hptr -> h_addrtype == AF_INET && (pptr= hptr->h_addr_list) != NULL){
		printf("gcm.c\t\t:D:\taddress : %s\n", inet_ntop(hptr->h_addrtype,*pptr, str, sizeof(str)));
	}
	else{
		printf("gcm.c\t\t:E:\tError call inet_nstop \n");
		return false; 
	}

	*sockfd = socket(AF_INET,SOCK_STREAM,0);
	if(*sockfd < 0)
		return false;

	bzero(servaddr, sizeof(servaddr));
	servaddr->sin_family = AF_INET;
	servaddr->sin_port = htons(80);
	inet_pton(AF_INET,str,&servaddr->sin_addr);

	printf("gcm.c\t\t:D:\tConnecting...\n");
	printf("Data : %s\n", poststr);
	if(connect(*sockfd, (struct sockaddr*)servaddr, sizeof(*servaddr)) < 0){
		printf("gcm.c\t\t:E:\tFailed to connect GCM Server\n");
		return false;
	}
	int success; 
	success = process_http(sockfd, hname, page, poststr);
	
	close(*sockfd);
	free(sockfd);
	free(servaddr);
	return success;
}


int addApiKey(char* apikey){
	FILE* file = fopen("./config","r+");
	if(file == NULL){
		printf("gcm.c\t\t:D:\tError : Failed to open config file\n");
		return false;
	}
	
	char* pStr; 
	char readbuf[1024];
	rewind(file);

	while((pStr = fgets(readbuf, 1024, file)) != NULL){
		if(strcmp(pStr,"<App keys>") == 0){
			break;
		}
		printf("%d\n",fseek(file,strlen(pStr),SEEK_CUR));
		
	}
	//fprintf(file,"%s\n",apikey);
	fputs(apikey, file);
}
