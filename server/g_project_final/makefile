all : udp tcp packetlist packet device database information android raspberry main
	rm *.o
	./iot

udp : udp.c
	gcc -c udp.c

tcp : tcp.c
	gcc -c tcp.c

packetlist : packetlist.c
	gcc -c packetlist.c

packet : packet.c
	gcc -c packet.c

database : database.c
	gcc -c database.c -lmysqlclient -L./device

device : device.c
	gcc -c device.c -lmysqlclient -I/usr/include/mysql/mysql -I./database -lpthread

information : information.c
	gcc -c information.c

android : android.o 
	gcc -o android android.o udp.o tcp.o packetlist.o packet.o device.o information.o database.o -L./udp -L./packetlist -L./packet -lpthread -L./information -L./device -L./database -lmysqlclient -L./tcp
	
android.o : android.c
	gcc -c android.c

raspberry : raspberry.o
	gcc -o raspberry raspberry.o database.o device.o tcp.o packetlist.o packet.o information.o -L./tcp -L./packetlist -L./packet -lpthread -L./information -L./device -lmysqlclient -L./database

raspberry.o : raspberry.c
	gcc -c raspberry.c

main : main.o
	gcc -o iot main.o information.o device.o database.o packetlist.o -lpthread -L./information -L./database -lmysqlclient -L./device -L./packetlist
	
main.o : main.c
	gcc -c main.c



