
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include "fcm.h"

char* token = "fENunFXmjh0:APA91bG3Lox2lXJKwna81aFR5-t4yaqiHTVZSLHZpgYJ3ValThatoe3a6ZRAVDX_2SRuaR2qMyZpI7kqyI9J1wY1Ehc7jtYjIfpv4PoEoYjbmlD8LxaLiB222qZLKufmyr3U7JUK3Z8I";

//char* token = "cxHwYAby5Pw:APA91bGk7E48Siob6QFhIWpeO_G5_Uj5NsSrJyxT2nLI0K2EhYihGj167YHpUI3B_odqeDIt88h7W1ra8neM3zlU6g7AXdNuwj8Yc3FsouT1cHEDNtxzx6DzixJ-pJvIGW_wnFWXm0mD";
int main(int argc, char** argv){

	int result ; 

	result = sendGcmMessageToAndroid("hello","helloBye", token);

	printf("Result : %d\n", result);

	return 0;

}
