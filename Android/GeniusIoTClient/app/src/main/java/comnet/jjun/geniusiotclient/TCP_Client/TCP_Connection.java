package comnet.jjun.geniusiotclient.TCP_Client;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import comnet.jjun.geniusiotclient.Define;
import comnet.jjun.geniusiotclient.IoTDevice;
import comnet.jjun.geniusiotclient.Protocol.Command;
import comnet.jjun.geniusiotclient.Protocol.GeniusProtocol;
import comnet.jjun.geniusiotclient.Protocol.Packet;

/**
 * Created by comm on 2018-08-07.
 */

public class TCP_Connection {

    private static final String TAG = "TCP_Connection";

    private Context context;

    private ConnectingThread connectingThread;
    private ConnectedThread connectedThread;
    private SendThread sendThread;

    private Socket socket;
    private OutputStream  outputStream;
    private InputStream inputStream;

    private int state  = STATE_NONE;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_NONE =2 ;
    public static final int STATE_FAILED  =3;
    public static final int STATE_DISCONNECTED =4 ;
    private int timeout;

    //MACRO

    public TCP_Connection(Context context) {
        this.context = context;

    }


    public TCP_Connection( Context context, int timeout) {
        this.context = context;
        this.timeout = timeout;

    }

    public void connectToServer(){
        connectingThread = new ConnectingThread();
        connectingThread.start();
    }

    public void send(byte[] data){
        SendThread mThread = new SendThread();
        mThread.send(data);
    }


    public void disconnect(){
        try{
            socket.close();
            state = STATE_DISCONNECTED;
        }catch (IOException e){
            e.printStackTrace();
            Log.d(TAG,"Disconnect to Server");
        }

    }

    public class SendThread extends Thread{

        private byte[] arr;

        public SendThread() {
            Log.d(TAG,"SendThread");
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
            if(outputStream != null){
                try{
                    outputStream.write(arr);
                }catch (SocketException e){
                    e.printStackTrace();
                    Log.d(TAG,"SocketException");
                    state = STATE_DISCONNECTED;
                    return;
                }
                catch (IOException e1){
                    e1.printStackTrace();
                    state = STATE_DISCONNECTED;
                    return;
                }
            }
            else{
                Log.d(TAG,"OutputStream null");
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

    private class ConnectedThread extends Thread{
        private long prev_t, current_t;

        private static final int READ_INTERVAL = 100;
        public ConnectedThread() {
            super();
        }

        @Override
        public void run() {
            super.run();
            Log.d(TAG,"Start TCP Read Thread");
            while (state == STATE_CONNECTED) {
                byte[] readData = new byte[1024];

                try{
                    socket.setSoTimeout(timeout);
                    while(inputStream.read(readData) <= 0 && state == STATE_CONNECTED){
                        try{
                            Thread.sleep(0,500);
                        }catch (InterruptedException e){

                        }
                    }
                }
                catch (SocketTimeoutException e1){
                    Log.d(TAG,"TCP Read Time out");
                    send(Command.request_connectionCheck());
                }
                catch (IOException e2){
                    e2.printStackTrace();
                    state = STATE_FAILED;
                    return;
                }
                try{
                    sleep(READ_INTERVAL);
                }
                catch (InterruptedException e2){
                    e2.printStackTrace();
                }
                Intent intent= new Intent(IoTDevice.TCP_RECEIVED_DATA);
                intent.putExtra(IoTDevice.TCP_RECEIVED_DATA,readData);
                context.sendBroadcast(intent);
            }
        }
    }

    private class ConnectingThread extends Thread{
        public ConnectingThread() {
            super();
            Log.d(TAG,"Start Connecting Thread");
        }

        @Override
        public void run() {
            super.run();
            socket = new Socket();
            SocketAddress address = new InetSocketAddress(Define.ServerAddress,Define.PORT);
            Log.d(TAG,"<Connect to Server>\n\tAddress : " + Define.ServerAddress + "\tPort : " + Define.PORT);
            try{
//                socket.setKeepAlive(true);
                if(timeout != 0)
                    socket.setSoTimeout(timeout);
                socket.connect(address);
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
            }catch (IOException e){
                e.printStackTrace();
                try {
                    socket.close();
                    state = STATE_FAILED;
                    return;
                }catch (IOException e2){
                    e2.printStackTrace();

                }
            }
            sendThread = new SendThread();
                try{
                    sleep(500);
                }catch (InterruptedException e){
                    e.printStackTrace();
                    state = STATE_FAILED;
                }

                // Test TCP Connection
                Command command = new Command();
                sendThread.send(command.requestPacket(GeniusProtocol.CONNECTION_CHECK));
                byte[] r_tmp = new byte[1024];
                try{
                    inputStream.read(r_tmp);
                }catch (IOException e){
                    e.printStackTrace();
                Log.d(TAG,"IOException");
                try {
                    socket.close();
                    state = STATE_FAILED;
                    return;
                }catch (IOException e2){
                    e2.printStackTrace();
                }
            }

            Packet packet_tmp = command.readPacketFromServer(r_tmp);
            Log.d(TAG,"Received command : " + packet_tmp.getCommand());
            if(packet_tmp.getCommand() == GeniusProtocol.CONNECTION_OK){
                Log.d(TAG,"TCP state -> connected");
                state = STATE_CONNECTED;
                connectedThread = new ConnectedThread();
                connectedThread.start();
            }

        }
    }
}
