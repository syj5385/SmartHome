/*
 * ADT_list.h
 *
 *  Created on: 2016. 10. 28.
 *      Author: jjunj
 */

#ifndef _PACKETLIST_H
#define _PACKETLIST_H

#include <stdio.h>
#include <stdlib.h>


// LIST_node
typedef struct node{
	unsigned char* params;
	struct node* next;
}NODE;


// LINKED LIST
typedef struct list{
	int count;
	NODE* front;
	NODE* rear;
	NODE* pos;
}LLIST;


// INTERFACE
LLIST* create_list();
int add_node_at(LLIST* list, unsigned int index, unsigned char in);
int del_node_at(LLIST* list, unsigned int index);
unsigned char get_data_at(LLIST* list, unsigned int index);
void delete_all_list(LLIST* list);

#endif /* _PACKETLIST_H_ */
