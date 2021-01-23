package jjun.geniusiot.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import jjun.geniusiot.Device.Device;
import jjun.geniusiot.NetworkService.Command;
import jjun.geniusiot.PublicRSS.ProcessXmlTask;
import jjun.geniusiot.AndroidDB.DeviceDB;
import jjun.geniusiot.Application.IoTDevice;
import jjun.geniusiot.PublicRSS.LocationService;
import jjun.geniusiot.Service.MainService;
import jjun.geniusiot.R;
import jjun.geniusiot.SpeechRecognizer.OptionDB;
import jjun.geniusiot.SpeechRecognizer.OptionData;

/**
 * Created by comm on 2018-08-10.
 */

public class MenuActivity extends AppCompatActivity {
    private static final String TAG = "MenuActivity";

    public static int REQUEST_RIGHT = 12;

    private ArrayList<OptionData> option = new ArrayList<>();
    private HashMap<String, ArrayList<OptionData>> option_hash = new HashMap<>();
    private ArrayList<String> hashKey = new ArrayList<>();


    private TextView[] command_set = new TextView[10];
    private int[] colorSet = {
            Color.rgb(0xff, 0xa7,0xa7),
            Color.rgb(0xff,0xc1,0x9e),
            Color.rgb(0xff, 0xe0,0x8c),
            Color.rgb(0xfa,0xed,0x7d),
            Color.rgb(0xce,0xf2,0x79),
            Color.rgb(0xb7,0xf0,0xB1),
            Color.rgb(0xb2,0xeb,0xf4),
            Color.rgb(0xb2,0xcc,0xff)
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        initailizeCommandSetAndView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFilter();
        Intent intent = new Intent(MenuActivity.this,MainService.class);
        bindService(intent,IoTConnection,Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        unbindService(IoTConnection);
    }

    //Service Connection
    private DeviceDB deviceDB;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    protected MainService iotService;
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
        filter.addAction(LocationService.LOCATION_CURRENT);
        filter.addAction(ProcessXmlTask.XML_EXCEPTION);
        filter.addAction(LocationService.LOCATION_CURRENT);
        filter.addAction(IoTDevice.IOEXCEPTION);
        filter.addAction(IoTDevice.UPDATE_DEVICE_OK);
        filter.addAction(IoTDevice.FINISHED_UPDATE_DEVICE);

        registerReceiver(mReceiver,filter);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.move_from_left,R.anim.move_to_right);
    }

    private ImageView back;
    private OptionDB optionDB;


    private void initailizeCommandSetAndView(){

        optionDB = new OptionDB(MenuActivity.this,mHandler,"optionDB");
        deviceDB = new DeviceDB(MenuActivity.this,DeviceDB.DeviceTable);
        option = optionDB.getAllDeviceFromDB();
        classifyOption();
        Log.d(TAG,"option size : " + option.size());
        command_set[0] = findViewById(R.id.command0);
        command_set[1] = findViewById(R.id.command1);
        command_set[2] = findViewById(R.id.command2);
        command_set[3] = findViewById(R.id.command3);
        command_set[4] = findViewById(R.id.command4);
        command_set[5] = findViewById(R.id.command5);
        command_set[6] = findViewById(R.id.command6);
        command_set[7] = findViewById(R.id.command7);
        command_set[8] = findViewById(R.id.command8);
        command_set[9] = findViewById(R.id.command9);

        for(int i=0; i<command_set.length ;i++)
            command_set[i].setText("");
        int i =0;

        for(i=0; i<hashKey.size(); i++){
            final String key = hashKey.get(i);
            command_set[i].setText(key);
            command_set[i].setBackgroundColor(option_hash.get(key).get(0).getColor());
            command_set[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<OptionData> option = option_hash.get(key);
                    Log.d(TAG,option.get(0).getName() + "수행");
                    for(int i=0; i<option.size(); i++){
                        Device device = deviceDB.getDeviceUsingID(option.get(i).getId());
                        if (device != null){
                            if(device.getDeviceType() == Command.LED || device.getDeviceType() == Command.WINDOW) {
                                int[] value = {option.get(i).getV1(), option.get(i).getV2()};
                                device.setValue(value);

                                sendTCP(Command.request_execute_toServer(device));
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }


                        }

                    }

                }
            });
        }

        if(i < 10) {
            command_set[i].setText(" + ");
            command_set[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MenuActivity.this, "새로운 option 추가", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MenuActivity.this, AddOptionActivity.class);
                    startActivityForResult(intent, 0);
                    overridePendingTransition(R.anim.move_from_right, R.anim.move_to_left);

                }
            });
        }

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private boolean classifyOption(){
        for(int i=0; i<option.size() ;i++){
            OptionData optionTemp = option.get(i);
            if(!option_hash.containsKey(optionTemp.getName())){
                ArrayList<OptionData> optionArrayTemp = new ArrayList<>();
                option_hash.put(optionTemp.getName(),optionArrayTemp);
                optionArrayTemp.add(0,optionTemp);
                hashKey.add(optionTemp.getName());
            }
            else{
                ArrayList<OptionData> thisOptionArray = option_hash.get(optionTemp.getName());
                if(thisOptionArray != null){
                    thisOptionArray.add(thisOptionArray.size(), optionTemp);
                }
                else{
                    option_hash = null;
                    return false;
                }

            }
        }

        for(int i=0; i<hashKey.size(); i++){
            String key = hashKey.get(i);
            ArrayList<OptionData> temp = option_hash.get(key);
            Log.d(TAG,"<"+key+">");
            for(int j=0; j<temp.size(); j++){
                Log.d(TAG,"option : " + temp.get(i).getName());
            }
            Log.d(TAG,"----------------------------------------------------------");
        }
        return true;
    }

    private Handler mHandler = new Handler(){

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0){
            initailizeCommandSetAndView();
        }
    }

    protected void sendTCP(final byte[] payload){
        Log.d(TAG,"TCP Data Send");
        iotService.sendTcpData(payload);

    }
}
