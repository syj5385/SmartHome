package f6.iot_project.NotificationService;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

import f6.iot_project.Activity.RemoteControlActivity;
import f6.iot_project.Activity.SplashActivity;
import f6.iot_project.Database.DeviceDB;
import f6.iot_project.IoT_Device.Command;
import f6.iot_project.Controller.LED_controller;
import f6.iot_project.IoT_Device.Device;
import f6.iot_project.IoT_Device.GeniusHome;
import f6.iot_project.GoogleSTT.MySpeech;
import f6.iot_project.GoogleSTT.SpeechProcess;
import f6.iot_project.IoT_Device.Packet;
import f6.iot_project.R;
import f6.iot_project.Network.UDP_Connection;


/**
 * Created by comm on 2018-04-21.
 */

public class NotificationService extends Service{

    private static final int REQUEST_APPLICATION = 100;

    public static final String REQUEST_NEW_DEVICE = "f6.iot_project.request_new_device";

    private static final String TAG = "NotificationService";
    public IBinder binder = new NotificationBinder();

    private UDP_Connection udp;
    private Command command;
    private GeniusHome genius;

    private static final int notifi_id = 548854;

    private NotificationThread mthread;

    private DeviceDB deviceDB;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"bind Service in NotificationService");
        return binder;
    }

    public class NotificationBinder extends Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        registerReceiver(mReceiver,filter);
        Log.d(TAG,"start Service in NotificationService");

        udp = new UDP_Connection(NotificationService.this,mHandler);
        command = new Command();
        genius = (GeniusHome)this.getApplication();
        deviceDB = new DeviceDB(NotificationService.this,mHandler,DeviceDB.DeviceTable);

        initializeNotification();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(REQUEST_NEW_DEVICE);
        registerReceiver(mReceiver,intentFilter);

        mthread = new NotificationThread(selectedDevice);
        mthread.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(genius.isNotification()){
                    sendUDP(command.request_current_Device_to_Server());
                    try{
                        Thread.sleep(genius.getNotificationNewDevice());
                    }catch (InterruptedException e){};
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private Notification.Builder builder;
    private Notification notifi;
    private NotificationManager manager;
    private void initializeNotification(){
        Log.d(TAG,"Notification start in Service");
        manager = (NotificationManager)getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        Intent notificationView = new Intent(NotificationService.this,RemoteControlActivity.class);
        notificationView.putExtra(RemoteControlActivity.REQUEST_DEVICE,RemoteControlActivity.REQUEST_DEVICE0);
        PendingIntent notificationClick = PendingIntent.getActivity(NotificationService.this,RemoteControlActivity.REQUEST_REMOTE_CONTROLLER,notificationView,0);

        builder = new Notification.Builder(NotificationService.this);
        builder.setSmallIcon(R.drawable.car);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(notificationClick);
        builder.setOngoing(true);
        builder.setLargeIcon(BitmapFactory.decodeResource(NotificationService.this.getResources(),R.drawable.light_on));

        notifi = builder.build();

        manager.notify(notifi_id,notifi);

//        builder.setSubText("업데이트" );
//            notifi = builder.build();

    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(REQUEST_NEW_DEVICE)) {
                Log.d(TAG,"update device information");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sendUDP(command.request_current_Device_to_Server());
                            }
                        }).start();

//                       updateNotification();
                    }
                }).start();

            }
        }
    };

    private Packet recvPacket;
    private ArrayList<Device> readDevice = new ArrayList<Device>();
    private ArrayList<Device> selectedDevice = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        private String CurrentDevice;
        @Override
        public void handleMessage(Message msg) {

            switch(msg.what){
                case UDP_Connection.UDP_RESULT :
                    byte[] message = (byte[])msg.obj;
                    recvPacket = new Command().readPacketFromServer(message);
                    Log.d("IOT","Command : " + recvPacket.getCommand() + "\tsize : " + recvPacket.getSizeofData() + "\nparams : " + new String(recvPacket.getParameter()));
                    switch (recvPacket.getCommand()){
                        case Command.GET_DEVICE :
                            readDevice = deviceDB.getAllDeviceFromDB();
                            while(selectedDevice.size() !=0){
                                selectedDevice.remove(0);
                            }
                            ArrayList<Integer> favorlist = deviceDB.selectFavorFromTable();
                            for(int i=0; i<favorlist.size() ;i++) {
                                for (int j = 0; j < readDevice.size(); j++) {
                                    if (favorlist.get(i) == readDevice.get(j).getDeviceId()) {
                                        readDevice.get(j).setControlllist(true);
                                        selectedDevice.add(readDevice.get(j));
                                    }
                                }
                            }
                            updateNotification();
                            break;

                        case Command.RESULT_OK :
//                            if(isRequestedScan){
//                                isRequestedScan = false;
//                                mydevice.setAdapter(null);
//                                sendUDP(command.request_current_Device_to_Server());
//                            }
                            break;

                    }
                    break;

                case MySpeech.RESULT_RECORD :
//                    String listened = ((ArrayList<String>)msg.obj).get(0);
//                    speechText.setText(listened);
//                    SpeechProcess process = new SpeechProcess(NotificationService.this,speechDialog,listened,mHandler);
                    break;

                case SpeechProcess.SPEECH_PROCESS :
                    int command = msg.arg1;
                    switch(command){
                        case SpeechProcess.SCAN_DEVICE :


                            break;
                    }

                    break;

            }
        }
    };

    private void sendUDP(final byte[] data){
        boolean success = udp.request_udpWrite(data);

    }

    private void sendUDP(String data){
        sendUDP(data.getBytes());
    }


    private class NotificationThread extends Thread{
        private int loopTime = genius.getNotificationLoop();

        private ArrayList<Device> selectedDevice = new ArrayList<>();


        public NotificationThread(ArrayList<Device> arrayList) {
            super();
            genius.setIsControlled(true);
        }

        @Override
        public void run() {
            super.run();
            while(genius.isNotification()){
               mHandler.post(udpSendRunnable);
               try{
                   Thread.sleep(loopTime);
               }catch (InterruptedException e){
                   Log.e(TAG,"InterruptedException in Notification Service");
                   e.printStackTrace();
               }

            }
        }

        private Runnable udpSendRunnable = new Runnable() {
            @Override
            public void run() {
                readDevice = deviceDB.getAllDeviceFromDB();

                updateNotification();
            }
        };
    }

    private int index = 0;
    private void updateNotification(){
//        Log.d(TAG,"Device count : " + genius.getSelectedDevice().size());
        ArrayList<Integer> favorlist = deviceDB.selectFavorFromTable();
        Log.d(TAG,"Favorite Device : " + favorlist.size());
        if(favorlist.size() == 0){
            builder.setContentTitle("즐겨찾기에 추가 된 장치가 없습니다.");
            builder.setContentText("클릭하면 어플리케이션이 실행됩니다.");
            builder.setContentIntent(
                    PendingIntent.getActivity(NotificationService.this,REQUEST_APPLICATION,new Intent(NotificationService.this,SplashActivity.class),0)
            );
        }
        else if(favorlist.size()>0){
            builder.setSubText("Genius Home");
            Device current = new Device(0,0,"Loading...",0,0,0);
            index++;
            if(index >=favorlist.size())
                index = 0;
            for(int i=0; i<readDevice.size(); i++) {
                if(readDevice.get(i).getDeviceId() == favorlist.get(index)){
                    current= readDevice.get(i);
                    break;
                }
            }

            builder.setContentTitle("장치 이름 : " + current.getDevice_name());
            if(current.getDeviceType() == Command.LED){
                if(current.getvalue()[0] == 100) {
                    String contentText = "꺼짐";
                    builder.setLargeIcon(BitmapFactory.decodeResource(NotificationService.this.getResources(), R.drawable.light_off));
                    builder.setContentText(contentText);
                }
                else if(current.getvalue()[0] == 200) {
                    String contentText = "";
                    builder.setLargeIcon(BitmapFactory.decodeResource(NotificationService.this.getResources(), R.drawable.light_on));
                    int bright = current.getvalue()[1];
                    contentText += "밝기 단계 : ";
                    int level = LED_controller.getBright_level(bright);
                    contentText += String.valueOf(level);
                    builder.setContentText(contentText);
                }
                builder.setContentIntent(
                        PendingIntent.getActivity(NotificationService.this,REQUEST_APPLICATION,new Intent(NotificationService.this,RemoteControlActivity.class),0)
                );


            }
            else if(current.getDeviceType() == Command.WINDOW){
                if(current.getvalue()[0] == 100) {
                    String contentText = "닫힘";
                    builder.setLargeIcon(BitmapFactory.decodeResource(NotificationService.this.getResources(), R.drawable.window_image_close));
                    builder.setContentText(contentText);
                }
                else if(current.getvalue()[0] == 200) {
                    String contentText = "";
                    builder.setLargeIcon(BitmapFactory.decodeResource(NotificationService.this.getResources(), R.drawable.window_image_open));
                    int bright = current.getvalue()[1];
                    contentText += "문 열림 단계 : ";
                    int level = LED_controller.getBright_level(bright);
                    contentText += String.valueOf(level);
                    builder.setContentText(contentText);
                }
                builder.setContentIntent(
                        PendingIntent.getActivity(NotificationService.this,REQUEST_APPLICATION,new Intent(NotificationService.this,RemoteControlActivity.class),0)
                );
            }
            else if(current.getDeviceType() == Command.DOOR){

            }

        }
        manager.notify(notifi_id,notifi);
    }
}
