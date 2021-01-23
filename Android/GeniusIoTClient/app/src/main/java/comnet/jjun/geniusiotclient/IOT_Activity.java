package comnet.jjun.geniusiotclient;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import comnet.jjun.geniusiotclient.MySQL.MYSQL_UpdateDevice;
import comnet.jjun.geniusiotclient.Protocol.Device;
import comnet.jjun.geniusiotclient.Protocol.GeniusProtocol;

public class IOT_Activity extends AppCompatActivity {

    private static final String TAG = "IOT_Activity";

    private IoTDevice iot;
    private MainService iotService;
    private ListView info_list;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iot);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        iot = (IoTDevice)this.getApplication();
        info_list = findViewById(R.id.info_list);

    }

    private LinearLayout device_container1;
    private LinearLayout device_container2;
    private TextView temperature, humidity;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private LinearLayout controller;

    private void initializeDeviceView(){
        device_container1 = findViewById(R.id.device_list1);
        device_container2 = findViewById(R.id.device_list2);
        device_container1.removeAllViews();
        device_container2.removeAllViews();
        final HashMap<Integer,ArrayList<Device>> device = iot.getAllDevice();
        builder = new AlertDialog.Builder(IOT_Activity.this);

        // LED Device & Window Device
        ArrayList<Device> led = device.get(IoTDevice.LED_DEVICE);
        Log.d(TAG,"LED Device size : " + led.size());
        for(int i=0; i<led.size(); i++){
            LinearLayout temp = (LinearLayout) View.inflate(IOT_Activity.this,R.layout.eachdevice,null);
            final ImageView image = temp.findViewById(R.id.device_image);
            final TextView name = temp.findViewById(R.id.device_name);
            final TextView status = temp.findViewById(R.id.status);
            final TextView status2 = temp.findViewById(R.id.status2);

            final Device d = led.get(i);
            name.setText(d.getDevice_name());
            if(d.getvalue()[0] == 200){
                image.setImageDrawable(getResources().getDrawable(R.drawable.light_on));
                status.setText("켜짐");
                int level = (d.getvalue()[1] - 100)/20;
                status2.setText(level + " 단계");
            }
            else if(d.getvalue()[0] == 100){
                image.setImageDrawable(getResources().getDrawable(R.drawable.light_off));
                status.setText("꺼짐");
                status2.setText("");
            }
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    implementationController(d,d.getDeviceType());
                }
            });
            device_container1.addView(temp);
        }

        ArrayList<Device> window = device.get(IoTDevice.WINDOW_DEVICE);
        Log.d(TAG,"Window Device size : " + window.size());
        for(int i=0; i<window.size(); i++){
            LinearLayout temp = (LinearLayout) View.inflate(IOT_Activity.this,R.layout.eachdevice,null);
            final ImageView image = temp.findViewById(R.id.device_image);
            final TextView name = temp.findViewById(R.id.device_name);
            final TextView status = temp.findViewById(R.id.status);
            final TextView status2 = temp.findViewById(R.id.status2);

            final Device d = window.get(i);
            name.setText(d.getDevice_name());
            if(d.getvalue()[0] == 200){
                image.setImageDrawable(getResources().getDrawable(R.drawable.window_image_open));
                status.setText("열림");
                int level = (d.getvalue()[1] - 100)/20;
                status2.setText(level + " 단계");
            }
            else if(d.getvalue()[0] == 100){
                image.setImageDrawable(getResources().getDrawable(R.drawable.window_image_close));
                status.setText("닫힘");
                status2.setText("");
            }

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    implementationController(d,d.getDeviceType());
                }
            });

            device_container1.addView(temp);
        }

        ArrayList<Device> etc = device.get(IoTDevice.ETC_DEVICE);
        Log.d(TAG,"etc Device size : " + etc.size());
        for(int i=0; i<etc.size(); i++){
            LinearLayout temp = (LinearLayout) View.inflate(IOT_Activity.this,R.layout.eachdevice,null);
            final ImageView image = temp.findViewById(R.id.device_image);
            final TextView name = temp.findViewById(R.id.device_name);
            final TextView status = temp.findViewById(R.id.status);
            final TextView status2 = temp.findViewById(R.id.status2);

            final Device d = etc.get(i);
            name.setText(d.getDevice_name());

            if(d.getDeviceType() == GeniusProtocol.DOOR) {
                if (d.getvalue()[0] == 200) {
                    image.setImageDrawable(getResources().getDrawable(R.drawable.door_open));
                    status.setText("열림");
                    status2.setText("");
                } else if (d.getvalue()[0] == 100) {
                    image.setImageDrawable(getResources().getDrawable(R.drawable.door_close));
                    status.setText("닫힘");
                    status2.setText("");
                }
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int[] device_value = new int[2];;
                        device_value[1] = d.getvalue()[1];
                        if(d.getvalue()[0] == 100){
                            image.setImageDrawable(getResources().getDrawable(R.drawable.door_close));
                            status.setText("닫힘");
                            status2.setText("");
                            device_value[0] = 200;
                        }
                        else if(d.getvalue()[0] == 200){
                            image.setImageDrawable(getResources().getDrawable(R.drawable.door_open));
                            status.setText("열림");
                            status2.setText("");
                            device_value[0] = 100;
                        }
                        d.setValue(device_value);
                        updateDevice(d);
                    }
                });
            }
            else if(d.getDeviceType() == GeniusProtocol.TEMP) {
                image.setImageDrawable(getResources().getDrawable(R.drawable.temp_big));
                status.setText(d.getvalue()[0] + " ℃ / " + d.getvalue()[1] + " %" );
                status2.setText("");
                temperature = findViewById(R.id.temperature);
                humidity = findViewById(R.id.humidity);
                temperature.setText("실내 온도 : " + d.getvalue()[0] + " ℃");
                humidity.setText("실내 습도 : " + d.getvalue()[1] + " %");
            }
            else if(d.getDeviceType() == GeniusProtocol.GAS){
                image.setImageDrawable(getResources().getDrawable(R.drawable.gas_image_big));
                status.setText("가스 감지 중...");
                status2.setText(String.valueOf(d.getvalue()[0]));
            }
            else if(d.getDeviceType() == GeniusProtocol.BATH){
                HashMap<Integer,Integer> bath = Device.getBathInformation(d.getvalue()[0], d.getvalue()[1]);
                if(bath.get(GeniusProtocol.BATH_EXECUTION_REQUEST) == 1 && bath.get(GeniusProtocol.BATH_EXECUTION_RESULT) == 1){
                    image.setImageDrawable(getResources().getDrawable(R.drawable.bath_on));
                    status.setText("ON...");
                    status2.setText("현재 물 온도 : " + (bath.get(GeniusProtocol.BATH_TEMP_RESULT) + 15));
                }
                else if(bath.get(GeniusProtocol.BATH_EXECUTION_REQUEST) == 0 && bath.get(GeniusProtocol.BATH_EXECUTION_RESULT) == 1){
                    image.setImageDrawable(getResources().getDrawable(R.drawable.bath_off));
                    status.setText("OFF");
                    status2.setText("");
                }
                else if(bath.get(GeniusProtocol.BATH_EXECUTION_REQUEST) == 0 && bath.get(GeniusProtocol.BATH_EXECUTION_RESULT) == 0) {
                    image.setImageDrawable(getResources().getDrawable(R.drawable.bath_off));
                    status.setText("OFF");
                    status2.setText("");
                    MYSQL_UpdateDevice conn = new MYSQL_UpdateDevice(iotService,this,d.getDeviceId(),d.getDeviceType(),new int[]{d.getvalue()[0], d.getvalue()[1]});
                }
                else{
                    if(bath.get(GeniusProtocol.BATH_WATER_RESULT) == 3 && bath.get(GeniusProtocol.BATH_TEMP_RESULT) == 31) {
                        image.setImageDrawable(getResources().getDrawable(R.drawable.bath_off));
                        status.setText("Requested...");
                        status2.setText("");
                    }
                    else{
                        image.setImageDrawable(getResources().getDrawable(R.drawable.bath_off));
                        status.setText("Finished");
                        status2.setText("");
                    }

                }
            }
            device_container2.addView(temp);
        }

    }

    private void implementationController(final Device d,int type){
        controller = (LinearLayout)View.inflate(IOT_Activity.this,R.layout.ledcontroller,null);
        final TextView title = controller.findViewById(R.id.controller_title);
        final TextView info = controller.findViewById(R.id.bright);
        final ImageView image = controller.findViewById(R.id.bulb);
        final Button[] bright = new Button[5];
        bright[0] = controller.findViewById(R.id.b1);
        bright[1] = controller.findViewById(R.id.b2);
        bright[2] = controller.findViewById(R.id.b3);
        bright[3] = controller.findViewById(R.id.b4);
        bright[4] = controller.findViewById(R.id.b5);
        if(d.getvalue()[0] == 200)
            setBright(bright,d.getvalue()[1]);
        int index = (d.getvalue()[1] - 100) / 20;

        // LED
        if(type == GeniusProtocol.LED){
            if(d.getvalue()[0] == 100){
                image.setImageDrawable(getResources().getDrawable(R.drawable.light_off));
            }
            else if(d.getvalue()[0] == 200){
                image.setImageDrawable(getResources().getDrawable(R.drawable.light_on));
            }
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int[] device_value = new int[2];

                    if(d.getvalue()[0] == 100){
                        image.setImageDrawable(getResources().getDrawable(R.drawable.light_on));
                        device_value[0] = 200;
                        setBright(bright,d.getvalue()[1]);
                    }
                    else if(d.getvalue()[0] == 200){
                        image.setImageDrawable(getResources().getDrawable(R.drawable.light_off));
                        for(int i=0; i<bright.length ;i++){
                            bright[i].setBackgroundColor(Color.rgb(0xe1,0xf5,0xea));
                        }
                        device_value[0] = 100;
                    }
                    device_value[1] = d.getvalue()[1];
                    d.setValue(device_value);
                    updateDevice(d);
                }
            });
            for(int i=0; i<bright.length; i++){
                bright[i].setOnClickListener(new View.OnClickListener() {
                    int[] device_value = new int[2];
                    @Override
                    public void onClick(View view) {
                        for(int j=0; j<bright.length ; j++)
                            bright[j].setBackgroundColor(Color.rgb(0xe1,0xf5,0xea));
                        view.setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                        device_value[0] = 200;
                        if(d.getvalue()[0] == 100){
                            image.setImageDrawable(getResources().getDrawable(R.drawable.light_on));

                        }
                        int bright = 100;
                        switch(view.getId()){
                            case R.id.b1 :
                                bright = 120;
                                break;

                            case R.id.b2 :
                                bright = 140;
                                break;

                            case R.id.b3 :
                                bright = 160;
                                break;

                            case R.id.b4 :
                                bright = 180;
                                break;

                            case R.id.b5 :
                                bright = 200;
                                break;
                        }
                        device_value[1] = bright;
                        d.setValue(device_value);
                        updateDevice(d);
                    }
                });
            }
        }
        //Window
        else if(type == GeniusProtocol.WINDOW){
            if(d.getvalue()[0] == 100){
                image.setImageDrawable(getResources().getDrawable(R.drawable.window_image_close));
            }
            else if(d.getvalue()[0] == 200){
                image.setImageDrawable(getResources().getDrawable(R.drawable.window_image_open));
            }
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int[] device_value = new int[2];

                    if(d.getvalue()[0] == 100){
                        image.setImageDrawable(getResources().getDrawable(R.drawable.window_image_open));
                        device_value[0] = 200;
                        setBright(bright,d.getvalue()[1]);
                    }
                    else if(d.getvalue()[0] == 200){
                        image.setImageDrawable(getResources().getDrawable(R.drawable.window_image_close));
                        for(int i=0; i<bright.length ;i++){
                            bright[i].setBackgroundColor(Color.rgb(0xe1,0xf5,0xea));
                        }
                        device_value[0] = 100;
                    }
                    device_value[1] = d.getvalue()[1];
                    d.setValue(device_value);
                    updateDevice(d);
                }
            });
            for(int i=0; i<bright.length; i++){
                bright[i].setOnClickListener(new View.OnClickListener() {
                    int[] device_value = new int[2];
                    @Override
                    public void onClick(View view) {
                        for(int j=0; j<bright.length ; j++)
                            bright[j].setBackgroundColor(Color.rgb(0xe1,0xf5,0xea));
                        view.setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                        device_value[0] = 200;
                        if(d.getvalue()[0] == 100){
                            image.setImageDrawable(getResources().getDrawable(R.drawable.window_image_open));

                        }
                        int bright = 100;
                        switch(view.getId()){
                            case R.id.b1 :
                                bright = 120;
                                break;

                            case R.id.b2 :
                                bright = 140;
                                break;

                            case R.id.b3 :
                                bright = 160;
                                break;

                            case R.id.b4 :
                                bright = 180;
                                break;

                            case R.id.b5 :
                                bright = 200;
                                break;
                        }
                        device_value[1] = bright;
                        d.setValue(device_value);
                        updateDevice(d);
                    }
                });
            }
        }

        builder.setNegativeButton("닫기",null).setView(controller);
        dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
//                iotService.getDeviceFromDB();
                initializeDeviceView();
            }
        });
        dialog.show();
    }

    private void setBright(Button[] brightBtn, int bright){
        switch(bright){
            case 120:
                brightBtn[0].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                break;
            case 140 :
                brightBtn[1].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                break;
            case 160 :
                brightBtn[2].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                break;
            case 180 :
                brightBtn[3].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                break;
            case 200 :
                brightBtn[4].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                break;
        }
    }

    private void updateDevice(Device d){
        Toast.makeText(IOT_Activity.this,"Update Device",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(IoTDevice.REQUEST_UPDATE_DEVICE);
        intent.putExtra("ID",d.getDeviceId());
        intent.putExtra("TYPE",d.getDeviceType());
        intent.putExtra("VALUE",d.getvalue());
        Log.d(TAG,"ID : " + d.getDeviceId()+"\nvalue1 : " + d.getvalue()[0] + "\nvalue2 : " + d.getvalue()[1]);
        sendBroadcast(intent);
    }

    //Main Service
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(IoTDevice.FINISH_GET_DEVICE)){
                initializeDeviceView();
            }
            else if(action.equals(IoTDevice.FINISHED_ADD_DEVICE )|| action.equals(IoTDevice.FINISHED_REMOVE_DEVICE)){
                initializeDeviceView();
            }

            else if(action.equals(IoTDevice.UPDATE_DEVICE)){
                initializeDeviceView();
            }
            else if(action.equals(IoTDevice.BLUETOOTH_RECEIVED_DATA)){
                int device_id = intent.getIntExtra("id",-1);
                int device_type =-1;
                if(device_id == -1)
                    return;
                Log.d(TAG,"Bluetooth Received -> from " + device_id);

                byte[] data = intent.getByteArrayExtra(IoTDevice.BLUETOOTH_RECEIVED_DATA);

                Device device;
                //test Device type
                if((device = iot.getDeviceWithID(GeniusProtocol.LED,device_id) )!= null) {
                    device_type = GeniusProtocol.LED;
                }
                else if((device = iot.getDeviceWithID(GeniusProtocol.WINDOW,device_id) )!= null) {
                    device_type = GeniusProtocol.WINDOW;
                }
                else if((device = iot.getDeviceWithID(0,device_id) )!= null) {
                    Log.d(TAG,"This device : " +device.getDeviceType());
                    device_type = device.getDeviceType();
                }

                if(device_type == GeniusProtocol.TEMP){
                    int temperature_v = (int)data[0] & 0xff;
                    int humidity_v = (int)data[1] & 0xff;
                    temperature.setText("실내 온도 : " + temperature_v + " ℃");
                    humidity.setText("실내 습도 : " + humidity_v + " %");
                    initializeDeviceView();
                }

                else if(device_type == GeniusProtocol.BATH){
//                       initializeDeviceView();
//                       iotService.getDeviceFromDB();
                       HashMap<Integer,Integer> bath = Device.getBathInformation((int)data[0] & 0xff,(int)data[1] & 0xff);
                       if(bath.get(GeniusProtocol.BATH_EXECUTION_REQUEST)==1 && bath.get(GeniusProtocol.BATH_EXECUTION_RESULT) == 0){
                           // Finished to bath
                           Log.d(TAG,"Finish BATH");
                           try{
                               Thread.sleep(1000);
                           }catch (InterruptedException e){
                               e.printStackTrace();
                           }
                       }
                }
            }

        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        Toast.makeText(getApplicationContext(),"onStop",Toast.LENGTH_SHORT).show();
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
        filter.addAction(IoTDevice.BLUETOOTH_RECEIVED_DATA);

        registerReceiver(mReceiver,filter);
    }
}
