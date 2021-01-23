
#include <string.h>
#include <stdlib.h>
#include "/usr/include/mysql/mysql.h"
#include "database.h"

void* connectDB(MYSQL* conn,char* dbname){
	
	char* server = "localhost";
	char* user = "jjun";
	char* pass = "1234";

	if((conn = mysql_init(NULL)) == NULL){
		printf("mysql.c\t\t:E:\tFailed to initialize DB\n");
		exit(1);
	}

	
	if(mysql_real_connect(conn,server,user,pass,dbname,0,NULL,0)== NULL){
		printf("mysql.c\t\t:E:\tFailed to connect %s\n",dbname);
		exit(0);
	}
	printf("database.c\t:D:\tSuccess to connect %s\n",dbname);


	//return conn;
}

void* disconnectDB(MYSQL* conn){
	mysql_close(conn);

}

void* createDB(MYSQL* conn, char* dbname){


	if(conn == NULL){
		printf("%s\n",mysql_error(conn));
		exit(0);
	}

	if(mysql_real_connect(conn, "localhost","jjun","1234",NULL,0,NULL,0) == NULL){
		//printf("%s\n",mysql_error(conn));
		mysql_close(conn);
		exit(0);

	}


	char* query_comm = "CREATE DATABASE ";
	char query[100];
	bzero(query, sizeof(query));
	
	sprintf(query,"%s%s",query_comm,dbname);

//	printf("mysql.c\t\t:D:\tQuery : %s\n",query);

	if(mysql_query(conn, query)){
		//printf("mysql.c\t\t:E:\t%s\n",mysql_error(conn));

		if(strstr(mysql_error(conn),"database exists")-mysql_error(conn)<= strlen(mysql_error(conn))){
			//printf("mysql.c\t\t:D:\talready Exists %s DB\n",dbname);
		}else{
			printf("mysql.c\t\t:E:\tError\n");
			mysql_close(conn);
			exit(1);
		}

	}
	printf("mysql\t\t:D:\tFinished check DB\n");
	mysql_close(conn);
	
}

int createDeviceTable(MYSQL* conn, char* tablename){
	printf("database.c\t:D:\tCheck the Device table -> %s\n",tablename);

	//Genius Home Data base Table
	char* query_comm = "CREATE TABLE ";
	char* variables = "(id INT, type INT, name TEXT, address TEXT, v1 INT, v2 INT)";

	char query[100];
	sprintf(query, "%s%s%s",query_comm, tablename, variables);
	//printf("mysql.c\t\t:D:\tcreate device Query : %s\n",query);
	if(mysql_query(conn,query)){
		//printf("mysql.c\t\t:E:\tFailed to create Table\n");
	//	printf("mysql.c\t\t:E:\t%s\n",mysql_error(conn));
		char* search = strstr(mysql_error(conn),"already exists");
		if(strcmp(search,"already exists") == 0){
			//printf("mysql.c\t\t:D:\tthis table is already exists\n");
			return 0;
		}
		return -1;
	}
	printf("mysql.c\t\t:D:\tSuccess to check table\n");
	return 0;
}

void showTable(MYSQL* conn){
	char query[150];
	sprintf(query, "SHOW TABLES");

	if(mysql_query(conn, query)){
		//printf("mysql.c\t\t:E:\tFailed to get Table\n");
		//printf("mysql.c\t\t:E:\t%s\n",mysql_error(conn));
	}

	printTableResult(conn);
}

// for GeniusHome 

int  getDeviceUsingID(MYSQL* conn,int id, Device* device){
	
	bzero(device,sizeof(device));
	MYSQL_RES* result;
	MYSQL_ROW row; 
	
	//printf("mysql.c\t\t:D:\tRequest -> getDevice\n");
	
	char query[100];
	bzero(query, sizeof(query));

	sprintf(query,"SHOW TABLES");
	if(mysql_query(conn, query)){
		printf("mysql.c\t\t:E:\tFailed to get Table\n");
	}


	result = mysql_store_result(conn);

	int i;
	int num_Field = mysql_num_fields(result);

	while((row = mysql_fetch_row(result))){
		for(i=0; i<num_Field ;i++){
			if(row[i] != "NULL"){
				sprintf(query,"SELECT * FROM %s WHERE id = %d",row[i],id);
				if(mysql_query(conn,query)){
					printf("mysql.c\t\t:E:\tError\n");
					printf("mysql.c\t\t:D:\t%s\n",mysql_error(conn));
				}
				else{	
					//printf("mysql.c\t\t:D:\tCheck -> %s\n",row[i]);
					int Numrow = getDevice(conn, device);	
					if(Numrow == 1){
						//printf("mysql.c\t\t:D:\tType : %s\n",row[i]);
						return 1;
					}
				}
			}

		}
	}	
	printf("mysql.c\t\t:D\tThere is No device anymore\n");
	return 0;
}


int updateDBusingID(MYSQL* conn, int id, int type, int value1, int value2){
	char* query_comm = "UPDATE";
	char* device; 
	if(type == 10)
		device = "LED";
	else if(type == 20)
		device = "Door";
	else if(type == 30)
		device = "Windows";
	else if(type == 40)
		device = "TEMP";
	else if(type == 50)
		device = "GAS";
	else
		printf("database.c\t:D:\tUnknown Device type\n");
	

	printf("database.c\t:D:\tUpdate device -> id : %d\t device : %s\n",id, device);


	char query[150];
	bzero(query, sizeof(query));

	sprintf(query,"%s %s SET v1=\"%d\" WHERE id=%d",query_comm,device,value1,id);
	printf("mysql.c.\t:D:\t%s\n",query);
	if(mysql_query(conn, query)){
		printf("mysql.c\t\t:E:\tFailed to get Table\n");
		printf("mysql.c\t\t:E:\t%s\n",mysql_error(conn));
		return 0;
	}



	sprintf(query,"%s %s SET v2=\"%d\" WHERE id=%d",query_comm,device,value2,id);

	if(mysql_query(conn, query)){
		printf("mysql.c\t\t:E:\tFailed to get Table\n");
		printf("mysql.c\t\t:E:\t%s\n",mysql_error(conn));
		return 0;
	}

	printf("mysql.c\t\t:D:\tSuccess to Update Device\n");
	
	return 1; 

}


int updateIDusingID(MYSQL* conn, unsigned char type ,unsigned char prevID, unsigned char newID){
	char* query_comm = "UPDATE";
	char* device; 
	if(type == 10)
		device = "LED";
	if(type == 20)
		device = "Door";
	if(type == 30)
		device = "Windows";
	if(type == 40)
		device = "TEMP";
	if(type == 50)
		device = "GAS";


	char query[150];
	bzero(query, sizeof(query));

	sprintf(query,"%s %s SET id=\"%d\" WHERE id=%d",query_comm,device ,((int)newID) & 0xff, ((int)prevID) & 0xff);
	
	if(mysql_query(conn, query)){
		printf("mysql.c\t\t:E:\tFailed to get Table\n");
		printf("mysql.c\t\t:E:\t%s\n",mysql_error(conn));
		return 0;
	}

	printf("mysql.c\t\t:D:\tSuccess to Update Device\n");

	return 1; 

}

int getDevice(MYSQL* conn, Device* device){
	
	MYSQL_RES* result_db = (MYSQL_RES*)malloc(sizeof(MYSQL_RES));
	result_db = mysql_store_result(conn);
	MYSQL_ROW row_db;

	int numRow = mysql_num_rows(result_db);
	int numField = mysql_num_fields(result_db);

	if(numRow == 0){
		return numRow; 
	}
	
	row_db = mysql_fetch_row(result_db);
	int j=0;

	//for(j=0; j<numField ; j++){
	//	if(row_db[j] != "NULL"){
	//		printf("\t%s\t|",row_db[j]);
	//	}
	//}

	// device id 
	device -> id = (unsigned char)atoi(row_db[0]);
	device -> type = (unsigned char)atoi(row_db[1]);
	sprintf(device->name,"%s",row_db[2]);
	//device -> name = row_db[2];
	sprintf(device->address,"%s",row_db[3]);
	//device -> address = row_db[3];
	*device -> v1 = atoi(row_db[4]);
	*device -> v2 = atoi(row_db[5]);
	
	return numRow; 

	//for(j=0; j<db_count; j++){
		//	if(row_db[j] != NULL){
	//		printf("%s\t",row_db[j]);
		//	}
	//}

}

void* insertDataToTable(MYSQL* conn, char* table, int id, int type, char* name, char* address, int v1, int v2){
	printf("mysql.c\t\t:D:\tRequested -> insert value to %s\n",table);
	char query[150];
	bzero(query,sizeof(query));

	sprintf(query,"INSERT INTO %s VALUES(%d,%d,'%s','%s',%d,%d)",table,id,type,name,address,v1,v2);

	//printf("mysql.c\t\t:D:\tQuery : query\n");

	if(mysql_query(conn,query)){
		printf("mysql.c\t\t:E:\tFailed to insert value to %s\n",table);
		printf("mysql.c\t\t:E:\t%s",mysql_error(conn));
	}
	
}


void* deleteDataFromTable(MYSQL* conn, Device* device){

	char* table; 
	switch(device->type){
		case 10 : // LED
			table = "LED";
			break; 

		case 20 : // DOOR
			table = "Door";
			break;

		case 30 : //WINDOW
			table = "Windows";
			break;

		case 40 : // TEMPERATURE
			table = "TEMP";
			break; 

		case 50 : // GAS 
			table = "GAS";
			break; 

		default : 
			printf("Unknown Device type\n");
			return NULL;

			break;
	}
	char query[150];
	bzero(query,sizeof(query));

	sprintf(query,"DELETE FROM %s WHERE id=%d\n",table, device->id);

	//printf("mysql.c\t\t:D:\tQuery : query\n");

	if(mysql_query(conn,query)){
		printf("mysql.c\t\t:E:\tFailed to insert value to %s\n",table);
		printf("mysql.c\t\t:E:\t%s",mysql_error(conn));
	
	}
}



void* printTableResult(MYSQL* conn){
	MYSQL_RES* result = (MYSQL_RES*)malloc(sizeof(MYSQL_RES*));
	result = mysql_store_result(conn);
	MYSQL_ROW row; 
	int i;
	int num_Field = mysql_num_fields(result);

	char query[100]; 

	while((row = mysql_fetch_row(result))){

		for(i=0; i<num_Field ;i++){
			if(row[i] != "NULL"){
				printf("\n\t< %s >\t\n",row[i]);
				sprintf(query,"SELECT * FROM %s",row[i]);
				if(mysql_query(conn,query)){
					printf("mysql.c\t:E:\tError\n");
				}
				else{
					printSelectResult(conn);
				}


			}

		}
		printf("\n");
	}	
	return result;

}


void* printSelectResult(MYSQL* conn){
	MYSQL_RES* result = (MYSQL_RES*)malloc(sizeof(MYSQL_RES*));
	result = mysql_store_result(conn);
	MYSQL_ROW row; 
	int i;
	int num_Field = mysql_num_fields(result);

	char query[100]; 

	printf("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
	while((row = mysql_fetch_row(result))){

		for(i=0; i<num_Field ;i++){
			if(row[i] != "NULL"){
				printf("  %s\t|",row[i]);
			}

		}
		printf("\n");
	}
	
	printf("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

	return result;

}
