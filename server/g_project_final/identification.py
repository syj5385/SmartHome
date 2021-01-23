import socket
import MySQLdb

server_socket = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
server_socket.bind(('',5102))
server_socket.listen(1)

def processCommand(data, client):
	command = data.split(" ")
	if command[0] == "ADD":
		print "identify.py\t:D:\tAdd new Identification"
		db = MySQLdb.connect(host="155.230.15.88",user="jjun",passwd="1234",db="GeniusIOT")
		cursor = db.cursor()
		try:
			command = "INSERT INTO User VALUES (" + command[1] + "," + command[2] + ")"
			print "identify.py\t:D:\t" + command
			cursor.execute(command)
			db.commit()
		except:	
			print "identify.py\t:D:\tINSERT INTO DB"
		client.send("ADD_OK".encode())
	if command[0] == "IDENT":
	  	print "identify.py\t:D:\tidentification check"
	  	db = MySQLdb.connect(host="155.230.15.88",user="jjun",passwd="1234",db="GeniusIOT")
		cursor = db.cursor()
		cursor.execute("SELECT COUNT(*) FROM User WHERE id="+command[1]);
		count = cursor.fetchall()[0][0];
		cursor.execute("SELECT * FROM User WHERE id="+command[1]);
		passwd = cursor.fetchall();
		for i in range(count):	
			if command[2] == str(passwd[i][1]):
				print "identify.py\t:D:\tID : " + str(passwd[i][0]) + "\tPASS : " + str(passwd[i][1])
				client.send("IDENT_OK".encode())
				return

		client.send("IDENT_FAILED".encode())


while True:
	print "identify.py\t:D:\tStart Identification Python Process"
	client_socket,addr = server_socket.accept()
	data = client_socket.recv(65535)	
	processCommand(data,client_socket)
	client_socket.close()
