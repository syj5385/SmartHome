package comnet.jjun.geniusiotclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import comnet.jjun.geniusiotclient.BluetoothLeManager.BluetoothLeManager;
import comnet.jjun.geniusiotclient.MySQL.MySQL_Connection;
import comnet.jjun.geniusiotclient.Protocol.Device;
import comnet.jjun.geniusiotclient.Protocol.GeniusProtocol;
import comnet.jjun.geniusiotclient.TCP_Client.TCP_Connection;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";

    private MainService iotService;
    private IoTDevice iot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        iot = (IoTDevice)this.getApplication();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(IoTDevice.FINISH_GET_DEVICE)){
                initializeDeviceView();
            }
            else if(action.equals(IoTDevice.BLUETOOTH_CONNECT)){
                String address =  intent.getStringExtra("address");
                Log.d(TAG,"Finished to create BLE Connection with " + address);
//                iot.getAllDevice().get(IoTDevice.LED_DEVICE).get(0).getBle().write("Hello".getBytes());
            }
            else if(action.equals(IoTDevice.UPDATE_DEVICE)){
                if(led_container != null && window_container != null && etc_container != null){
                    led_container.removeAllViews();
                    window_container.removeAllViews();
                    etc_container.removeAllViews();
                    initializeDeviceView();
                }
                else{
                    Toast.makeText(getApplicationContext(),"업데이트 중 오류 발생",Toast.LENGTH_SHORT).show();
                }
            }
            else if(action.equals(IoTDevice.FINISHED_REMOVE_DEVICE) || action.equals(IoTDevice.FINISHED_ADD_DEVICE)){
                if(led_container != null && window_container != null && etc_container != null){
                    led_container.removeAllViews();
                    window_container.removeAllViews();
                    etc_container.removeAllViews();
                    initializeDeviceView();
                }
                else{
                    Toast.makeText(getApplicationContext(),"업데이트 중 오류 발생",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private LinearLayout led_container, window_container, etc_container;

    private void initializeDeviceView(){
        led_container = findViewById(R.id.led_container);
        window_container = findViewById(R.id.window_container);
        etc_container = findViewById(R.id.etc_container);
        HashMap<Integer,ArrayList<Device>> mydevice = iot.getAllDevice();

        // First Implementation LED device
        ArrayList<Device> led = mydevice.get(IoTDevice.LED_DEVICE);
        if(led.size() == 0) {
            LinearLayout led_temp = (LinearLayout) View.inflate(this, R.layout.each_device, null);
            final TextView device_name = led_temp.findViewById(R.id.device_name);
            final ImageView device_image = led_temp.findViewById(R.id.device_image);
            device_name.setVisibility(View.GONE);
            device_image.setImageDrawable(new BitmapDrawable(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.plus), 100, 100, false)));
            Log.d(TAG, "No Device");
            led_container.addView(led_temp);
        }

        for(int i=0; i<led.size(); i++){
            LinearLayout led_temp = (LinearLayout) View.inflate(this,R.layout.each_device,null);
            final TextView device_name = led_temp.findViewById(R.id.device_name);
            final ImageView device_image = led_temp.findViewById(R.id.device_image);
            device_name.setText(led.get(i).getDevice_name());
            if(led.get(i).getvalue()[0] == 100)
                device_image.setImageDrawable(new BitmapDrawable(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.light_off),100,100,false)));

            else if(led.get(i).getvalue()[0] == 200)
                device_image.setImageDrawable(new BitmapDrawable(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.light_on),100,100,false)));


            Log.d(TAG,"insert Device : " + led.get(i).getDevice_name());
            led_container.addView(led_temp);
        }

        // Second Implementation Window device
        ArrayList<Device> window = mydevice.get(IoTDevice.WINDOW_DEVICE);
        if(window.size() == 0) {
            LinearLayout window_temp = (LinearLayout) View.inflate(this, R.layout.each_device, null);
            final TextView device_name = window_temp.findViewById(R.id.device_name);
            final ImageView device_image = window_temp.findViewById(R.id.device_image);
            device_name.setVisibility(View.GONE);
            device_image.setImageDrawable(new BitmapDrawable(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.plus), 100, 100, false)));
            Log.d(TAG, "No Device");
            window_container.addView(window_temp);
        }

        for(int i=0; i<window.size(); i++){
            LinearLayout window_temp = (LinearLayout) View.inflate(this,R.layout.each_device,null);
            final TextView device_name = window_temp.findViewById(R.id.device_name);
            final ImageView device_image = window_temp.findViewById(R.id.device_image);
            device_name.setText(window.get(i).getDevice_name());
            if(window.get(i).getvalue()[0] == 100)
                device_image.setImageDrawable(new BitmapDrawable(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.window_image_close),100,100,false)));

            else if(window.get(i).getvalue()[0] == 200)
                device_image.setImageDrawable(new BitmapDrawable(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.window_image_open),100,100,false)));


            Log.d(TAG,"insert Device" + window.get(i).getDevice_name());
            window_container.addView(window_temp);
        }

        // third Implementation etc device
        ArrayList<Device> etc = mydevice.get(IoTDevice.ETC_DEVICE);
        if(etc.size() == 0) {
            LinearLayout etc_temp = (LinearLayout) View.inflate(this, R.layout.each_device, null);
            final TextView device_name = etc_temp.findViewById(R.id.device_name);
            final ImageView device_image = etc_temp.findViewById(R.id.device_image);
            device_name.setVisibility(View.GONE);
            device_image.setImageDrawable(new BitmapDrawable(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.plus), 100, 100, false)));
            Log.d(TAG, "No Device");
            etc_container.addView(etc_temp);
        }

        for(int i=0; i<etc.size(); i++){
            LinearLayout etc_temp = (LinearLayout) View.inflate(this,R.layout.each_device,null);
            final TextView device_name = etc_temp.findViewById(R.id.device_name);
            final ImageView device_image = etc_temp.findViewById(R.id.device_image);
            device_name.setText(etc.get(i).getDevice_name());
            if(etc.get(i).getDeviceType() == GeniusProtocol.TEMP)
                device_image.setImageDrawable(new BitmapDrawable(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.temp_small),100,100,false)));
            else if(etc.get(i).getDeviceType() == GeniusProtocol.GAS)
                device_image.setImageDrawable(new BitmapDrawable(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.gas_image_small),100,100,false)));
            else if(etc.get(i).getDeviceType() == GeniusProtocol.DOOR){
                int value0 = etc.get(i).getvalue()[0];
                if(value0 == 200){
                    device_image.setImageDrawable(new BitmapDrawable(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.door_open_small),100,100,false)));
                }
                else if(value0 == 100){
                    device_image.setImageDrawable(new BitmapDrawable(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.door_close_small),100,100,false)));
                }
            }


            Log.d(TAG,"insert Device" + etc.get(i).getDevice_name());
            etc_container.addView(etc_temp);
        }

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BluetoothLeManager.REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this,"어플리케이션을 종료합니다.",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setFilter();
        startService(MainService.class,IoTConnection,null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(IoTConnection);
    }

    private static Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };



    // Service Creation
    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {

        Intent startService = new Intent(this, service);
        if (extras != null && !extras.isEmpty()) {
            Set<String> keys = extras.keySet();
            for (String key : keys) {
                String extra = extras.getString(key);
                startService.putExtra(key, extra);
            }
        }

        Intent bindingIntent = new Intent(this,service);
        bindService(bindingIntent,serviceConnection, Context.BIND_AUTO_CREATE);
    }



    private final ServiceConnection IoTConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            iotService = ((MainService.MainBinder) arg1).getService();

            Log.d(TAG,"Service : " + String.valueOf(iotService));

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            iotService = null;
            Log.e(TAG,"Service Disconnected");
        }
    };

    private void setFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(IoTDevice.FINISH_GET_DEVICE);
        filter.addAction(IoTDevice.FINISHED_REMOVE_DEVICE);
        filter.addAction(IoTDevice.FINISHED_ADD_DEVICE);
        filter.addAction(IoTDevice.UPDATE_DEVICE);

        registerReceiver(mReceiver,filter);
    }

}
