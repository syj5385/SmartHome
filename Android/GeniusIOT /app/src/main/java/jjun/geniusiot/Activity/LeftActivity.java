package jjun.geniusiot.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import jjun.geniusiot.CustomAdapter.CustomAdapter3.Custom3_Item;
import jjun.geniusiot.CustomAdapter.CustomAdapter3.CustomAdapter3;
import jjun.geniusiot.PublicRSS.ProcessXmlTask;
import jjun.geniusiot.AndroidDB.DeviceDB;
import jjun.geniusiot.Application.IoTDevice;
import jjun.geniusiot.PublicRSS.LocationService;
import jjun.geniusiot.Service.MainService;
import jjun.geniusiot.NetworkService.Command;
import jjun.geniusiot.NetworkService.GeniusProtocol;
import jjun.geniusiot.R;

/**
 * Created by comm on 2018-08-10.
 */

public class LeftActivity extends AppCompatActivity {
    private static final String TAG = "MenuActivity";

    public static final int REQUEST_LEFT = 10;
    private TextView addDevice;
    private ListView deviceList;
    private LinearLayout scanLayout ;
    private ImageView bt_refresh;
    private DeviceDB deviceDB;
    private AlertDialog dialog ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_left);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
    }

    private CustomAdapter3 scanAdapter;
    private static BluetoothAdapter btadapter = BluetoothAdapter.getDefaultAdapter();
    private ArrayList<BluetoothDevice> mLeDevice;
    private static final int SCAN_TIMEOVER = 10;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == SCAN_TIMEOVER){
                btadapter.stopLeScan(mLeScanCallback);
//                deviceList.setAdapter(scanAdapter);
                bt_refresh.setVisibility(View.VISIBLE);
            }
        }
    };

    private void DiscoverBLEDevice(){
        scanAdapter = new CustomAdapter3(LeftActivity.this);
        mLeDevice = new ArrayList<>();

        scanLeDevice(true);
    }

    private boolean scanning = false;
    private void scanLeDevice(final boolean enable){
        if(enable){
            scanning = false;
            btadapter.stopLeScan(mLeScanCallback);

            scanning = true;
            btadapter.startLeScan(mLeScanCallback);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scanLeDevice(false);
                            mHandler.obtainMessage(SCAN_TIMEOVER).sendToTarget();
                        }
                    },5000);
                }
            }).start();
        }
        else{
            scanning = false;
            btadapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!mLeDevice.contains(bluetoothDevice)) {
                        if(bluetoothDevice.getName() != null ) {
                            mLeDevice.add(bluetoothDevice);
                            scanAdapter.addItem(new Custom3_Item(getResources().getDrawable(R.drawable.device_image), bluetoothDevice.getName(), bluetoothDevice.getAddress()));
                            deviceList.setAdapter(scanAdapter);
                            Log.d(TAG,"Device : " + bluetoothDevice.getName());

                        }
                    }
                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        setFilter();
        Intent intent = new Intent(LeftActivity.this,MainService.class);
        bindService(intent,IoTConnection,Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        btadapter.stopLeScan(mLeScanCallback);
        unregisterReceiver(mReceiver);
        unbindService(IoTConnection);
    }

    //Service Connection
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(IoTDevice.SHAKING_OCCURED)){
                shaking_listen.setText("shaking");
                shaking_listen.setTextColor(Color.rgb(0xff,0x00,0x00));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        shaking_listen.setText("listening");
                        shaking_listen.setTextColor(Color.rgb(0x00,0x00,0x00));
                    }
                },1500);

            }
        }
    };

    protected MainService iotService;
    private final ServiceConnection IoTConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            iotService = ((MainService.MainBinder) arg1).getService();

            Log.d(TAG,"Service : " + String.valueOf(iotService));
            setScanDevice();
            setShakingOption();
            setSpeechDBView();
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
        filter.addAction(IoTDevice.SHAKING_OCCURED);

        registerReceiver(mReceiver,filter);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.move_from_right,R.anim.move_to_left);
    }

    private void setScanDevice(){
        addDevice = findViewById(R.id.adddevice);
        deviceList = findViewById(R.id.device_list);
        scanLayout = findViewById(R.id.scan_layout);
        bt_refresh = findViewById(R.id.bt_refresh);
        deviceDB = new DeviceDB(this,DeviceDB.DeviceTable);
        addDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(scanLayout.getVisibility()  != View.VISIBLE) {
                    scanLayout.setVisibility(View.VISIBLE);
//                    bt_refresh.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DiscoverBLEDevice();

                        }
                    }).start();
                }
                else{
                    bt_refresh.setVisibility(View.GONE);
                    scanAdapter.removeItem();
                    deviceList.setAdapter(null);
                    scanLayout.setVisibility(View.GONE);
                    btadapter.stopLeScan(mLeScanCallback);
                }
            }
        });
        bt_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanAdapter.removeItem();
                bt_refresh.setVisibility(View.INVISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DiscoverBLEDevice();

                    }
                }).start();
            }
        });
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, final int arg2, long l) {
                AlertDialog.Builder ab = new AlertDialog.Builder(LeftActivity.this);
                ab.setTitle("장치 이름 입력");
                final LinearLayout layout = (LinearLayout)View.inflate(LeftActivity.this, R.layout.device_name_request,null);
                ab.setView(layout);
                ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // register My IOT Device
//                        String name = ((Custom3_Item)adapterView.getAdapter().getItem(i)).getData()[0];
                        int device_type =0;
                        EditText name_text = layout.findViewById(R.id.device_name_request);
                        String type = ((Custom3_Item)adapterView.getAdapter().getItem(arg2)).getData()[0];
                        String address = ((Custom3_Item)adapterView.getAdapter().getItem(arg2)).getData()[1];
                        String name = name_text.getText().toString();
                        String encoded = "No";
                        try {
                            encoded = new String(name.getBytes("utf-8"));
                        }catch (Exception e){

                        }
                        if(type.contains("LED")){
                            device_type = GeniusProtocol.LED;
                        }
                        else if(type.contains("DOOR")){
                            device_type = GeniusProtocol.DOOR;
                        }
                        else if(type.contains("WINDOW")){
                            device_type = GeniusProtocol.WINDOW;
                        }
                        else if(type.contains("TEMP")){
                            device_type = GeniusProtocol.TEMP;
                        }
                        else if(type.contains("GAS")){
                            device_type = GeniusProtocol.GAS;
                        }
                        else if(type.contains("BATH")){
                            device_type = GeniusProtocol.BATH;
                        }
                        else{
                            Toast.makeText(LeftActivity.this,"Unknown",Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                        Log.d("IOT","name : " + name + "\taddress : " + address);


//                        iotService.getServer_conn().send(Command.request_addDevice_toServer(device_type,name,address));
                        iotService.sendTcpData(Command.request_addDevice_toServer(device_type,name,address));
                        scanAdapter.removeItem(arg2);
                        deviceList.setAdapter(scanAdapter);

                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.cancel();
                    }
                });

                dialog = ab.create();
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Toast.makeText(LeftActivity.this,"등록을 취소합니다",Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.show();
            }
        });
    }

    private TextView shakingOption,shaking_strength,shakingCount,shaking_listen;
    private LinearLayout shakingLayout;
    private Switch shakingEnable;
    private SeekBar strength,count;
    private void setShakingOption(){

        shakingOption = findViewById(R.id.shaking);
        shakingLayout = findViewById(R.id.shaking_layout);
        shakingEnable = findViewById(R.id.shaking_enabled);
        shakingEnable.setChecked(iotService.isEanble());
        strength = findViewById(R.id.strength);
        strength.setProgress((int)iotService.getStrength());
        shaking_strength = findViewById(R.id.shaking_strength);
        shaking_strength.setText(String.valueOf(iotService.getStrength()) + " [ 2(예민함) ~ 7(둔감함) ]");
        shakingCount = findViewById(R.id.shaking_count);
        shakingCount.setText(String.valueOf(iotService.getCompleteCount()) + "[ 4 ~ 12 ] ");
        count = findViewById(R.id.count);
        count.setProgress(iotService.getCompleteCount());
        shaking_listen = findViewById(R.id.shaking_sensor);

        shakingOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(shakingLayout.getVisibility() != View.VISIBLE){
                    shakingLayout.setVisibility(View.VISIBLE);
                }
                else{
                    shakingLayout.setVisibility(View.GONE);
                }
            }
        });

        shakingEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(TAG,"Sensor Enabled : " + b);
                iotService.enabledSensorListener(b);
            }
        });

        strength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                shaking_strength.setText(String.valueOf(seekBar.getProgress()) + " [ 2(예민함) ~ 7(둔감함) ]");
                iotService.setStrength(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        count.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                shakingCount.setText(String.valueOf(seekBar.getProgress()) + "[ 4 ~ 12 ] ");
                iotService.setCompleteCount(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private TextView speech_basic, speech_option;

    private static final int REQUEST_DB_VIEW_ACTIVITY = 10;
    private static final int REQUEST_OPTION_VIEW_ACTIVITY = 11;
    private void setSpeechDBView(){
        speech_basic = findViewById(R.id.speech_basic);
        speech_option =findViewById(R.id.speech_option);
        speech_basic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LeftActivity.this,SpeechDatabaseActivity.class);
                startActivityForResult(intent,REQUEST_DB_VIEW_ACTIVITY);
                overridePendingTransition(R.anim.move_from_left,R.anim.move_to_right);
            }
        });

        speech_option = findViewById(R.id.speech_option);
        speech_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LeftActivity.this,OptionDatabaseActivity.class);
                startActivityForResult(intent,REQUEST_OPTION_VIEW_ACTIVITY);
                overridePendingTransition(R.anim.move_from_left,R.anim.move_to_right);
            }
        });
    }



}
