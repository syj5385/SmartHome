package f6.iot_project.Network;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by comm on 2018-01-24.
 */

public class UDP_Connection {
    private Context context;
    private Activity activity;
    private Handler mHandler;
    public byte[] data;

    public static final int UDP_RESULT = 10;

    private static final String IP = "155.230.15.88";
    private static final int PORT = 11113;


    public UDP_Connection(Activity activity,Context context, Handler mHandler) {
        this.activity = activity;
        this.context = context;
        this.mHandler = mHandler;
    }

    public UDP_Connection(Context context, Handler mHandler) {
        this.context = context;
        this.mHandler = mHandler;

//        mThread = new SendData();
//        mThread = new SendData(this.data);
//        mThread.start();
    }


    public UDP_Connection(Activity activity, Context context, byte[] data, Handler mHandler) {
        this.activity = activity;
        this.context = context;
        this.data = data;
        this.mHandler = mHandler;

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
            InetAddress serverAddr = InetAddress.getByName(IP);

            DatagramPacket packet = new DatagramPacket(data,data.length,serverAddr,PORT);

            socket.send(packet);

            byte[] buffer = new byte[1000];
            DatagramPacket r_packet = new DatagramPacket(buffer,buffer.length);
            socket.receive(r_packet);

            mHandler.obtainMessage(UDP_RESULT, buffer).sendToTarget();

            success = true;
            socket.close();

        }catch (Exception e){
            Log.e("UDP", "Exception");
            success = false;
            mHandler.obtainMessage(UDP_RESULT,null).sendToTarget();

        }

        return success;
    }
}
