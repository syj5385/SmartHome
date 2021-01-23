package jjun.geniusiot.NetworkService;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;

import jjun.geniusiot.Device.MySQL_Define;
import jjun.geniusiot.Application.IoTDevice;


/**
 * Created by comm on 2018-08-07.
 */

public class TCP_Connection {

    private static final String TAG = "TCP_Connection";

    private Context context;

    private ConnectingThread connectingThread;
    private SendThread sendThread;

    private Socket socket;
    private OutputStream  outputStream;
    private InputStream inputStream;

    private int state  = STATE_NONE;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_NONE =2 ;
    public static final int STATE_FAILED  =3;
    public static final int STATE_DISCONNECTED =4 ;

    private int timeout = 0;

    public TCP_Connection(Context context) {
        this.context = context;
    }

    public TCP_Connection( Context context, int timeout) {
        this.context = context;
        this.timeout = timeout;
    }

    public void connectToServer(){

    }

    public void disconnect(){
        Log.d(TAG,"TCP Connection disconnect");
        try{
            socket.close();
            state = STATE_DISCONNECTED;
        }catch (IOException e){
            e.printStackTrace();
            Log.d(TAG,"Disconnect to Server");
        }
    }

    public void destroy(){
//        outputStream = null;
//        inputStream = null;
        socket = null;
    }

    public void send(byte[] data){
        connectingThread = new ConnectingThread(data);
        connectingThread.start();
    }

    public int getState(){
        return state;
    }
    private boolean requestWrite = false;

    public class SendThread extends Thread{

        private byte[] arr;

        public SendThread() {
            Log.d(TAG,"SendThread");
            requestWrite = true;
        }

        public void send(List<Byte> data){
            arr = requestData(data);
            start();
        }

        public void send(byte[] data){
            arr = data;
            start();
        }

        @Override
        public void run() {
            super.run();
            if(socket.isConnected()){
                try{
                    outputStream.write(arr);
                }catch (IOException e){
                    e.printStackTrace();
                    Log.d(TAG,"IOException");
                    context.sendBroadcast(new Intent(IoTDevice.IOEXCEPTION));
                    state = STATE_DISCONNECTED;
                    return;
                }
                try {
                    byte[] readData = new byte[1024];
                    try{
                        inputStream.read(readData);
//                    inputStream.read
                        requestWrite = false;
                        Log.d(TAG,"Received Data!!!");
                    }catch (IOException e){
                        e.printStackTrace();
                        context.sendBroadcast(new Intent(IoTDevice.IOEXCEPTION));
                        return;
                    }
                    Intent intent= new Intent(IoTDevice.TCP_RECEIVED_DATA);
                    intent.putExtra(IoTDevice.TCP_RECEIVED_DATA,readData);
                    Log.d(TAG,"Broadcast Received data");
                    context.sendBroadcast(intent);

                    socket.close();

                }catch (IOException e){
                    e.printStackTrace();
                }

            }
            else{
                Log.d(TAG,"socket is not connected");
                return;
            }
        }

        private byte[] requestData(List<Byte> data){
            byte[] tmp = new byte[data.size()];
            int i=0;
            for(byte b : data){
                tmp[i++] = b;
            }
            return tmp;
        }
    }


    int count;

    private class ConnectingThread extends Thread{
        private byte[] data ;
        public ConnectingThread(byte[] data) {
            super();
            Log.d(TAG,"Start Connecting Thread");
            this.data = data;
        }

        @Override
        public void run() {
            super.run();
            socket = new Socket();
            SocketAddress address = new InetSocketAddress(MySQL_Define.ServerAddress, MySQL_Define.PORT);
            Log.d(TAG,"<Connect to Server>\n\tAddress : " + MySQL_Define.ServerAddress + "\tPort : " + MySQL_Define.PORT);
            try {
                if (timeout != 0)
                    socket.setSoTimeout(timeout);
                socket.connect(address);
            }catch (IOException e1) {
                e1.printStackTrace();
                try{
                    socket.close();
                    context.sendBroadcast(new Intent(IoTDevice.IOEXCEPTION));
                }catch (IOException e2){
                    e2.printStackTrace();
                }
                return;
            }
            try{
                socket.setKeepAlive(true);
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
            }catch (IOException e){
                e.printStackTrace();
                try {
                    Log.d(TAG,"Socket Closed After sending data");
                    socket.close();
                    state = STATE_FAILED;
                    socket = null;
                    return;
                }catch (IOException e2){
                    e2.printStackTrace();
                    return;
                }
            }

            try{
                sleep(500);
            }catch (InterruptedException e){
                e.printStackTrace();
                state = STATE_FAILED;
            }

            count++;
            Log.d(TAG,"Count : " + count);
            Log.d(TAG,"TCP state -> connected");
            state = STATE_CONNECTED;

            SendThread sendThread = new SendThread();
            sendThread.send(data);
        }
    }
}
