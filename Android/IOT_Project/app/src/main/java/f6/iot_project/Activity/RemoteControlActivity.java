package f6.iot_project.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import f6.iot_project.Database.DeviceDB;
import f6.iot_project.IoT_Device.Command;
import f6.iot_project.IoT_Device.Device;
import f6.iot_project.IoT_Device.GeniusHome;
import f6.iot_project.NotificationService.NotificationService;
import f6.iot_project.IoT_Device.Packet;
import f6.iot_project.R;
import f6.iot_project.Network.UDP_Connection;

import static android.content.ContentValues.TAG;

/**
 * Created by comm on 2018-04-21.
 */

public class RemoteControlActivity extends AppCompatActivity {

    public static final String REQUEST_DEVICE = "device";

    public static final String REQUEST_DEVICE0 = "device0";
    public static final String REQUEST_DEVICE1 = "device1";
    public static final String REQUEST_DEVICE2 = "device2";

    public static final int REQUEST_REMOTE_CONTROLLER = 10;

    private UDP_Connection udp;
    private Command command;
    private Packet recvPacket;
    private ArrayList<Device> selectedDevice;
    private HashMap<LinearLayout,Device> controller_hash;
    private GeniusHome genius;

    private LinearLayout controller_item;
    private ArrayList<Switch> switch_list ;
    private HashMap<Switch,Device> switchToDevice;

    private DeviceDB deviceDB;

    private Context ctx;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.remotecontrolleractivity);

        genius = (GeniusHome)RemoteControlActivity.this.getApplication();
        deviceDB = new DeviceDB(RemoteControlActivity.this,mHandler,DeviceDB.DeviceTable);

        controller_item = (LinearLayout)findViewById(R.id.controller_item);
        controller_item.removeAllViews();
        switch_list = new ArrayList<>();
        switchToDevice = new HashMap<>();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    @Override
    protected void onResume() {
        super.onResume();
        udp = new UDP_Connection(this,this,mHandler);
        command = new Command();

        new Thread(new Runnable() {
            @Override
            public void run() {
                sendUDP(command.request_current_Device_to_Server());
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){

        private String CurrentDevice;

        @Override
        public void handleMessage(Message msg) {

            switch(msg.what){
                case UDP_Connection.UDP_RESULT :
                    byte[] message = (byte[])msg.obj;
                    //Packet recvPacket = new Packet();

                    recvPacket = new Command().readPacketFromServer(message);
//                    content.setText((String)msg.obj);

                    Log.d("IOT","Command : " + recvPacket.getCommand() + "\tsize : " + recvPacket.getSizeofData() + "\nparams : " + new String(recvPacket.getParameter()));
                    switch (recvPacket.getCommand()){
                        case Command.GET_DEVICE :
//                            for(int i=0; i<message.length;i++){
//                                Log.d("IOT",i + "\t" + String.valueOf(message[i]));
//                            }
                            ArrayList<Device> readDevice;
                            ArrayList<Integer> favorlist = deviceDB.selectFavorFromTable();
                            selectedDevice = new ArrayList<>();

                            readDevice = deviceDB.getAllDeviceFromDB();
                            byte charTemp;
                            int index = 0;


                            for(int i=0; i<favorlist.size() ;i++){
                                for(int j=0; j<readDevice.size(); j++){
                                    if(favorlist.get(i) == readDevice.get(j).getDeviceId()){
                                        readDevice.get(j).setControlllist(true);
                                        selectedDevice.add(readDevice.get(j));
                                    }
                                }
                            }

                            for(int i=0; i<selectedDevice.size() ;i++) {
                                LinearLayout tempView;
                                if(selectedDevice.get(i).getDeviceType() == Command.LED) {
                                    tempView = (LinearLayout) View.inflate(RemoteControlActivity.this, R.layout.led_small_controller, null);
                                    TextView device_name = (TextView) tempView.findViewById(R.id.device_name);
                                    device_name.setText(selectedDevice.get(i).getDevice_name());
                                    Switch led_switch = (Switch)tempView.findViewById(R.id.led_switch);
                                    if(selectedDevice.get(i).getvalue()[0] == 200){
                                        led_switch.setChecked(true);
                                    }else if(selectedDevice.get(i).getvalue()[0] == 100){
                                        led_switch.setChecked(false);
                                    }
                                    switch_list.add(led_switch);
                                    switchToDevice.put(led_switch,selectedDevice.get(i));
                                    led_switch.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Device selective = switchToDevice.get(view);
                                            if(selective != null) {
                                                if (((Switch) view).isChecked()) {
                                                    int[] value = {200, selective.getvalue()[1]};
                                                    selective.setValue(value);
                                                } else {
                                                    int[] value = {100, selective.getvalue()[1]};
                                                    selective.setValue(value);
                                                }
                                                sendUDP(command.request_execute_toServer(selective));
                                            }
                                            else{
                                                Log.e(TAG,"Error : null devices");
                                            }
                                        }
                                    });
                                }
                                else if(selectedDevice.get(i).getDeviceType() == Command.WINDOW){
                                    tempView = (LinearLayout) View.inflate(RemoteControlActivity.this, R.layout.window_small_controller, null);
                                    TextView device_name = (TextView) tempView.findViewById(R.id.device_name);
                                    device_name.setText(selectedDevice.get(i).getDevice_name());
                                    Switch window_switch = (Switch)tempView.findViewById(R.id.window_switch);
                                    if(selectedDevice.get(i).getvalue()[0] == 200){
                                        window_switch.setChecked(true);
                                    }else if(selectedDevice.get(i).getvalue()[0] == 100){
                                        window_switch.setChecked(false);
                                    }
                                    switch_list.add(window_switch);
                                    switchToDevice.put(window_switch,selectedDevice.get(i));
                                    window_switch.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Device selective = switchToDevice.get(view);
                                            if(selective != null) {
                                                if (((Switch) view).isChecked()) {
                                                    int[] value = {200, selective.getvalue()[1]};
                                                    selective.setValue(value);
                                                } else {
                                                    int[] value = {100, selective.getvalue()[1]};
                                                    selective.setValue(value);
                                                }
                                                sendUDP(command.request_execute_toServer(selective));
                                            }
                                            else{
                                                Log.e(TAG,"Error : null devices");
                                            }
                                        }
                                    });
                                }
                                else{
                                    tempView = (LinearLayout) View.inflate(RemoteControlActivity.this, R.layout.led_small_controller, null);
                                }
                                controller_item.addView(tempView);
                            }
                            break;
                    }
                    break;

            }
        }
    };


    private void sendUDP(final byte[] data){
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = udp.request_udpWrite(data);
            }
        }).start();
        try{
            Thread.sleep(500);
        }catch (InterruptedException e){}

        sendBroadcast(new Intent(NotificationService.REQUEST_NEW_DEVICE));
    }

    private void sendUDP(String data){
        sendUDP(data.getBytes());
    }

}
