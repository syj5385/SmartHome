
fcm.o:
	gcc -c fcm.c

main.o:
	gcc -c main.c

clean:
	rm -fr *.o
	rm -fr main


all: clean fcm.o main.o
	gcc -o main main.o fcm.o -L./fcm
