package jjun.geniusiot.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import jjun.geniusiot.Activity.SpeechActivity;
import jjun.geniusiot.Application.IoTDevice;
import jjun.geniusiot.AndroidDB.DeviceDB;
import jjun.geniusiot.Device.Device;
import jjun.geniusiot.NetworkService.Command;
import jjun.geniusiot.NetworkService.GeniusProtocol;
import jjun.geniusiot.NetworkService.Packet;
import jjun.geniusiot.NetworkService.TCP_Connection;

/**
 * Created by comm on 2018-08-10.
 */

public class MainService extends Service {

    private static final String TAG = "MainService";

    private boolean isService = false;
    private boolean isVibrate = false;

    private DeviceDB deviceDB;
    private IoTDevice iot;

    private boolean running = false;
    private Thread mThread ;


    public IBinder binder = new MainBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MainBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"Main Service start");
        setFilter();
        initializeObject();
        running = true;
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensorEventListener = new SensorListener();
        mSensorManager.registerListener(mSensorEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_UI);
        isService = true;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
//        server_conn.dismake makeconnect();
//        server_conn.destroy();
//        mSensorManager.unregisterListener(mSensorEventListener);
//        isService = false;
//        running =false;
        Log.d(TAG,"onDestroy()");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG,"Rebuilding Service");
        initializeObject();
        running = true;
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensorEventListener = new SensorListener();
        mSensorManager.registerListener(mSensorEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_UI);
        isService = true;

    }

    private Notification.Builder builder;
    private Notification notify;
    private NotificationManager manager;
    private static final int notifi_id = 548855;


    private void initializeObject(){
        Log.d(TAG,"Initialize Object");
        iot = (IoTDevice)this.getApplication();
        deviceDB = new DeviceDB(this,DeviceDB.DeviceTable);
        iot.setMyDevice(Device.initializeDevice());
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(IoTDevice.TCP_FINISHED_CONNECT)){
                Log.d(TAG,"Requset Current Device");
                try{
                    Thread.sleep(500);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                sendTcpData(Command.request_current_Device_to_Server());
            }
            else if(action.equals(IoTDevice.TCP_RECEIVED_DATA)){
                byte[] r_data = intent.getByteArrayExtra(IoTDevice.TCP_RECEIVED_DATA);
                Packet packet = Command.readPacketFromServer(r_data);
                executeCommand(packet);

                sendBroadcast(new Intent(IoTDevice.FINISHED_UPDATE_DEVICE));
            }
            else if(action.equals(IoTDevice.IOEXCEPTION)){
                Log.d(TAG,"IOEXCEPTION");
                try{
                    Thread.sleep(500);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            else if(action.equals(IoTDevice.FINISHED_UPDATE_DEVICE)){

            }
            else if(action.equals(IoTDevice.IOT_ACTIVITY_START)){
                Log.d(TAG,"Start Activity");
                sendTcpData(Command.request_current_Device_to_Server());
//                server_conn.send(Command.request_current_Device_to_Server());
            }
            else if(action.equals(IoTDevice.FCM_RECEIVED)){
                int id = intent.getIntExtra(IoTDevice.FCM_RECEIVED,-1);
                if(id != -1){
                    Device device = deviceDB.getDeviceUsingID(id);
                    if(device.getDeviceType() == Command.BATH){
                        int[] value = {device.getvalue()[0] ^= 0x80, device.getvalue()[1]};
                        device.setValue(value);
                        sendTcpData(Command.request_execute_toServer(device));
                    }
                    try{
                        Thread.sleep(500);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                sendTcpData(Command.request_current_Device_to_Server());
            }

        }
    };

    public void setFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(IoTDevice.TCP_FINISHED_CONNECT);
        filter.addAction(IoTDevice.TCP_RECEIVED_DATA);
        filter.addAction(IoTDevice.EXIT_APPLICATION);
        filter.addAction(IoTDevice.IOEXCEPTION);
        filter.addAction(IoTDevice.FINISHED_UPDATE_DEVICE);
        filter.addAction(IoTDevice.IOT_ACTIVITY_START);
        filter.addAction(IoTDevice.FCM_RECEIVED);


        registerReceiver(mReceiver,filter);
    }

    private boolean executeCommand(Packet packet){
        Log.d(TAG,"This command : " + packet.getCommand());
        switch(packet.getCommand()){
            case GeniusProtocol.GET_DEVICE :
                deviceDB.updateDeviceFromServer(packet);
                Log.d(TAG,"data : " + new String(packet.getParameter()));
                return true;


            case GeniusProtocol.UPDATE_DEVICE :
//                delay(500);
//                server_conn.send(Command.request_current_Device_to_Server());
                sendBroadcast(new Intent(IoTDevice.UPDATE_DEVICE_OK));
                return true;

            case GeniusProtocol.RESULT_OK :
                delay(500);
                sendTcpData(Command.request_current_Device_to_Server());
                return true;

            case GeniusProtocol.BLE_DATA :
                delay(500);
                int id = (int)packet.getParameter()[0] & 0xff;
                sendTcpData(Command.request_current_Device_to_Server());
                Log.d(TAG,"BLE DATA Update : " + id);
                return true;

            default :

                return false;
        }
    }
    private void delay(long millis){
        try{
            Thread.sleep(millis);
        }catch (InterruptedException e){

        }
    }


    private SensorManager mSensorManager;
    private SensorListener mSensorEventListener;
    private boolean isEanble = true;
    private long mShakeTime = 0;
    private int mShakeCount;
    private int continueShakeCount = 0;
    private static final int SHAKE_SKIP_TIME = 100;
    // 인식가능 흔들기 세기
    private float SHAKE_THRESHOLD_GRAVITY = 5.0F;
    private int TIMEOUT_FOR_SHAKING = 2000;
    private int completeCount = 8;
    private class SensorListener implements SensorEventListener{
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){

                float axisX = sensorEvent.values[0];
                float axisY = sensorEvent.values[1];
                float axisZ = sensorEvent.values[2];

                float gravityX = axisX / SensorManager.GRAVITY_EARTH;
                float gravityY = axisY / SensorManager.GRAVITY_EARTH;
                float gravityZ = axisZ / SensorManager.GRAVITY_EARTH;

                Float f = gravityX * gravityX + gravityY * gravityY + gravityZ * gravityZ;
                double squareD = Math.sqrt(f.doubleValue());
                float gForce = (float)squareD;
                if(gForce > SHAKE_THRESHOLD_GRAVITY){
                    long currentTime = System.currentTimeMillis();
                    if(mShakeTime + SHAKE_SKIP_TIME > currentTime){
                        return;
                    }
                    mShakeTime = currentTime;
                    mShakeCount ++;
                    continueShakeCount++;
                    if(mShakeCount == 1){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    Thread.sleep(TIMEOUT_FOR_SHAKING);
                                }catch (InterruptedException e){

                                }
                                continueShakeCount = 0;
                                mShakeCount  = 0;
                            }
                        }).start();
                    }
                    if(continueShakeCount == completeCount) {
                        continueShakeCount = 0;
                        Log.d(TAG, "onSensorChanged : Shake 발생 : " + mShakeCount);
                        Vibrator vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                        vib.vibrate(1000);
                        Intent intent = new Intent(MainService.this,SpeechActivity.class);
                        startActivity(intent);
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    public int getStrength(){
        return (int)SHAKE_THRESHOLD_GRAVITY;
    }

    public void setStrength(int strength){
        this.SHAKE_THRESHOLD_GRAVITY = (float)strength;
    }

    public  int getCompleteCount(){
        return completeCount;
    }

    public void setCompleteCount(int completeCount){
        this.completeCount = completeCount;
    }

    public void enabledSensorListener(boolean enable){
        if(enable){
            mSensorManager.registerListener(mSensorEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_UI);
            isEanble = true;
        }
        else{
            mSensorManager.unregisterListener(mSensorEventListener);
            isEanble = false;
        }

    }

    public boolean isEanble(){
        return isEanble;
    }

    public void sendTcpData(byte[] data){
        TCP_Connection conn = new TCP_Connection(this);
        conn.send(data);
    }

}
