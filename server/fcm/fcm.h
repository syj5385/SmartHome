#ifndef _GCM_H_
#define _GCM_H_


#include <stdlib.h>
#include <arpa/inet.h>
#include <assert.h>
#include <errno.h>
#include <netinet/in.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <netdb.h>
#include "define.h"


#define MAXLINE	4096
#define MAXSUB 	500

#define LISTENQ	1024


int sendGcmMessageToAndroid(char* title, char* text, char* who);
int addApiKey(char* apikey);
int isSendGcm();

#endif

