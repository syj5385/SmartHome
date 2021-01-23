package jjun.geniusiot.NetworkService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static android.content.ContentValues.TAG;

/**
 * Created by comm on 2018-01-24.
 */

public class UDP {

    // Action
    public static final String UDP_RESULT = "jjun.geniusiot.UDP_RESULT";
    public static final String UDP_EXCEPTION = "jjun.geniusiot.UDP_EXCEPTION";

    private Context context;
    private Activity activity;
    private Handler mHandler;
    private int timeout = 3000;    // TIME OUT DEFAULT : 3000 [ms]
    public byte[] data;

//    public static final int UDP_RESULT = 10;

    private static final String IP = "155.230.15.88";
    private static final int PORT = 11113;


    public UDP(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    public UDP(Context context) {
        this.context = context;
    }

    public UDP(Activity activity, Context context, int timeout) {
        this.activity = activity;
        this.context = context;
        this.timeout = timeout;
    }

    public boolean request_udpWrite(byte[] data){
        return requestUDP(data);
    }

    private boolean requestUDP(byte[] data){
        boolean success = true;
        for(int i=0; i<data.length ;i++){
            Log.d(TAG,"index " + i + " : " + (((int)data[i]) & 0xff));
        }

        try{
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
            InetAddress serverAddr = InetAddress.getByName(IP);

            DatagramPacket packet = new DatagramPacket(data,data.length,serverAddr,PORT);

            socket.send(packet);

            byte[] buffer = new byte[1000];
            DatagramPacket r_packet = new DatagramPacket(buffer,buffer.length);
            socket.receive(r_packet);

            if(context != null){
                Intent intent = new Intent(UDP_RESULT);
                intent.putExtra(UDP_RESULT,buffer);
                Log.d(TAG,"Received UDP Datagram from Server");
                context.sendBroadcast(intent);
            }

            success = true;
            socket.close();

        }catch (Exception e){
            Log.e("UDP", "Exception");
            e.printStackTrace();
            success = false;
            context.sendBroadcast(new Intent(UDP_EXCEPTION));

        }

        return success;
    }
}
