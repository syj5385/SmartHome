package f6.iot_project.Activity;

import android.annotation.SuppressLint;
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
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import f6.iot_project.Controller.LED_controller;
import f6.iot_project.Controller.Window_Controller;
import f6.iot_project.CustomAdapter.CustomAdapter1.Custom1_Item;
import f6.iot_project.CustomAdapter.CustomAdapter1.CustomAdapter1;
import f6.iot_project.CustomAdapter.CustomAdapter3.Custom3_Item;
import f6.iot_project.CustomAdapter.CustomAdapter3.CustomAdapter3;
import f6.iot_project.CustomAdapter.CustomAdatper2.Custom2_Item;
import f6.iot_project.CustomAdapter.CustomAdatper2.CustomAdapter2;
import f6.iot_project.Database.DeviceDB;
import f6.iot_project.Database.OptionDB;
import f6.iot_project.Database.OptionData;
import f6.iot_project.Database.SpeechDB;
import f6.iot_project.Database.SpeechProcessManager;
import f6.iot_project.GoogleSTT.MySpeech;
import f6.iot_project.IoT_Device.Command;
import f6.iot_project.IoT_Device.Device;
import f6.iot_project.IoT_Device.GeniusHome;
import f6.iot_project.IoT_Device.MyHome;
import f6.iot_project.IoT_Device.Packet;
import f6.iot_project.Network.UDP_Connection;
import f6.iot_project.NotificationService.NotificationService;
import f6.iot_project.R;

import static android.content.ContentValues.TAG;

/**
 * Created by comm on 2018-01-30.
 */

public class IOT_Activity extends AppCompatActivity{

    private static final int REQUEST_DB_VIEW_ACTIVITY = 10;
    private static final int SCAN_TIMEOVER = 100;

    private static final int REQUEST_ADD_OPTION = 10;

    private int numberOfDevice = 0;

    private MySpeech speech;
    private GeniusHome genius;

    // Drawer layout
    private DrawerLayout mDrawer;
    private ImageView addDevice, setting;
    private ListView drawer_list;
    private TextView speechText;
    private AlertDialog speechDialog;


    //Adapter
    private CustomAdapter3 basicAdapter;
    private CustomAdapter3 scanAdapter;
    private CustomAdapter3 controllerListAdapter;
    private CustomAdapter3 controllerlistEditAdapter;

    // UDP Connection
    private UDP_Connection udp;
    private Command command;

    // Service
    private NotificationService mNotificationService;

    // Activity View
    private Button[] menu = new Button[3];
    private ImageView drawer_opener;
    private ImageView voice;
    private LinearLayout content_display;
    private LinearLayout iot_display;
    private ListView mydevice;

    private TextToSpeech tts;
    private SpeechDB speechDB;
    private DeviceDB deviceDB;
    private MyHome myHome;

    // Other variable
    private boolean isFinishedScan = true;
    private boolean isRequestedScan = false;
    private boolean isRequestedEditController = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("IOT","????");
        initializeView();

        sendUDP(command.request_current_Device_to_Server());
        speechDB = new SpeechDB(IOT_Activity.this, mHandler,SpeechDB.SpeechTable);
        deviceDB = new DeviceDB(IOT_Activity.this,mHandler, DeviceDB.DeviceTable);

    }

    private final ServiceConnection NotificationConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mNotificationService = ((NotificationService.NotificationBinder)iBinder).getService();
            Log.d("IOT","Service!!!!");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        registerReceiver(mReceiver,filter);
        startService(NotificationService.class,NotificationConnection,null);
//        initializeNotification();

        Log.d("IOT","onResume");

        tts = new TextToSpeech(IOT_Activity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                tts.setLanguage(Locale.KOREA);
//                tts.setVoice());
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);

    }

    @Override
    protected void onStop() {
        super.onStop();
        tts.stop();
        tts.shutdown();
    }

    private void initializeView(){
        menu[0] = (Button)findViewById(R.id.menu1);
        menu[1] = (Button)findViewById(R.id.menu2);
        menu[2] = (Button)findViewById(R.id.menu3);
        for(int i=0;i<menu.length ; i++){
            menu[i].setOnClickListener(menuBtnClickListener);
        }

        mydevice = new ListView(this);
        content_display = (LinearLayout)findViewById(R.id.content_display);
        iot_display = (LinearLayout)findViewById(R.id.iot_monitor);

        mydevice.setOnItemClickListener(mOnItemClickListener);
        mydevice.setOnItemLongClickListener(mOnLongItemClickListener);
        content_display.addView(mydevice);


        //Drawer View initialize
        mDrawer = (DrawerLayout)findViewById(R.id.maindrawer);
        myHome = new MyHome(this, mHandler);
        iot_display.removeAllViews();
        iot_display.addView(myHome,new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        drawer_opener = (ImageView)findViewById(R.id.drawer_opener);
        drawer_opener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawer.openDrawer(GravityCompat.START);
                controllerListAdapter = new CustomAdapter3(IOT_Activity.this);
                for(int i =0; i<readDevice.size() ; i++) {
                    Log.d(TAG,"Check!!!!!!!!!!!");

                    if(readDevice.get(i).isControlllist()){
                        controllerListAdapter.addItem(new Custom3_Item(getResources().getDrawable(R.drawable.device_image), readDevice.get(i).getDevice_name(),String.valueOf(readDevice.get(i).getDeviceId())));
                    }
                    drawer_list.setAdapter(null);
                    drawer_list.setAdapter(controllerListAdapter);
                }

            }
        });

        voice = (ImageView)findViewById(R.id.voice);
        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speech = new MySpeech(IOT_Activity.this,mHandler);

                AlertDialog.Builder ab = new AlertDialog.Builder(IOT_Activity.this);
                LinearLayout layout = (LinearLayout)View.inflate(IOT_Activity.this,R.layout.voice_layout,null);
                speechText = layout.findViewById(R.id.speechText);
                ab.setView(layout);
                speechText.setText("말씀하세요.");

                speechDialog = ab.create();
                speechDialog.show();

                speech.startRecognizer();

            }
        });



        addDevice = (ImageView)findViewById(R.id.drawer_menu1);
        addDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawer.closeDrawer(GravityCompat.START);
                mydevice.setAdapter(null);
                isRequestedScan = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DiscoverBLEDevice();
                    }
                }).start();

                CustomAdapter3 adapter = new CustomAdapter3(IOT_Activity.this);
                adapter.addItem(new Custom3_Item(getResources().getDrawable(R.drawable.device_image),"Scanning...",""));
                mydevice.setAdapter(adapter);

            }
        });


        setting = (ImageView)findViewById(R.id.drawer_menu2);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(IOT_Activity.this);
                LinearLayout dialogView = (LinearLayout)View.inflate(IOT_Activity.this,R.layout.settingdialog,null);
                builder.setView(dialogView);

                ListView settinglist = dialogView.findViewById(R.id.settingList);
                CustomAdapter2 adapter = new CustomAdapter2(IOT_Activity.this);
                adapter.addItem(new Custom2_Item(getResources().getDrawable(R.drawable.voice),"명령 setting", "동작을 위한 명령을 설정할 수 있습니다."));
                settinglist.setAdapter(adapter);

                settinglist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        switch(i){
                            case 0 :
                                Intent intent = new Intent(IOT_Activity.this,SpeechDatabaseActivity.class);
                                startActivityForResult(intent, REQUEST_DB_VIEW_ACTIVITY);
                                overridePendingTransition(R.anim.fade, R.anim.hold);
                                break;
                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });


        udp = new UDP_Connection(IOT_Activity.this,IOT_Activity.this,mHandler);
        command = new Command();

        genius = (GeniusHome)this.getApplication();
        drawer_list = (ListView)findViewById(R.id.drawer_list);
        controllerListAdapter = new CustomAdapter3(this);
        controllerlistEditAdapter = new CustomAdapter3(this);

    }

    private LinearLayout menu2Temp;
    private View.OnClickListener menuBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            content_display.removeAllViews();
            iot_display.removeAllViews();
            for(int i=0; i<menu.length ; i++){
                menu[i].setTextColor(Color.BLACK);
            }
            menu[0].setText("menu1");
            menu[1].setText("menu2");
            menu[2].setText("menu3");
            switch(view.getId()){
                case R.id.menu1 :
                    content_display.addView(mydevice);
                    iot_display.addView(myHome,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    menu[0].setText(menu[0].getText()+"\n▼");
                    menu[0].setTextColor(getResources().getColor(R.color.colorSelected));
                    break;

                case R.id.menu2 :
                    menu2Temp = (LinearLayout)View.inflate(IOT_Activity.this,R.layout.menu2layout,null);
                    iot_display.addView(menu2Temp,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    implementationMenu2(menu2Temp);
                    menu[1].setText(menu[0].getText()+"\n▼");
                    menu[1].setTextColor(getResources().getColor(R.color.colorSelected));

                    break;

                case R.id.menu3 :
                    menu[2].setText(menu[0].getText()+"\n▼");
                    menu[2].setTextColor(getResources().getColor(R.color.colorSelected));
                    break;
            }
        }
    };

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(mDrawer.isDrawerOpen(GravityCompat.START)){
                 if(isRequestedEditController) {
                     isRequestedEditController = false;
                     drawer_list.setAdapter(controllerListAdapter);
                     TextView info = (TextView)findViewById(R.id.drawerinformation);
                     info.setText("Widget\nControl");
                 }
                 else {
                     mDrawer.closeDrawer(GravityCompat.START);
                 }
            }
            else{
                if(isRequestedScan){
                    if(scanning){
                        Toast.makeText(getApplicationContext(), "장치 검색 중입니다...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        isRequestedScan = false;
                        sendUDP(command.request_current_Device_to_Server());
                    }
                }


                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(IOT_Activity.this);
                    builder.setTitle("정말로 종료하시겠습니까?").setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            IOT_Activity.this.finish();
                        }
                    }).setNegativeButton("아니오",null).setIcon(getResources().getDrawable(R.drawable.icon)).create().show();

                }
            }
        }

        return true;
    }

    private ArrayList<String> bt_name = new ArrayList<String>();
    private ArrayList<String> bt_address = new ArrayList<String>();
    private Packet recvPacket;
    private ArrayList<Device> readDevice = new ArrayList<Device>();
    private ArrayList<Device> selectedDevice = new ArrayList<>();
    private ArrayList<Integer> favorlist = new ArrayList<>();
    private String listened;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        private String CurrentDevice;
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case UDP_Connection.UDP_RESULT :
                    byte[] message = (byte[])msg.obj;
                    if(message == null){
                        LinearLayout layout = (LinearLayout)View.inflate(IOT_Activity.this,R.layout.notconnected,null);
                        iot_display.removeAllViews();
                        iot_display.addView(layout);
                        ImageView retry = (ImageView)findViewById(R.id.retry);
                        retry.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendUDP(command.request_current_Device_to_Server());
                                iot_display.removeAllViews();
                            }
                        });

                    }else { // Result Received

                        recvPacket = new Command().readPacketFromServer(message);
//                    content.setText((String)msg.obj);

                        Log.d("IOT", "Command : " + recvPacket.getCommand() + "\tsize : " + recvPacket.getSizeofData() + "\nparams : " + new String(recvPacket.getParameter()));
                        switch (recvPacket.getCommand()) {
                            case Command.GET_DEVICE:
//                            for(int i=0; i<message.length;i++){
//                                Log.d("IOT",i + "\t" + String.valueOf(message[i]));
//                            }
                                basicAdapter = new CustomAdapter3(IOT_Activity.this);
                                deviceDB.updateDeviceFromServer(recvPacket);
                                readDevice = deviceDB.getAllDeviceFromDB();
                                Log.d(TAG,"readDevice size : " + readDevice.size());
//                                favorlist = deviceDB.selectFavorFromTable();
                                for (int i = 0; i < favorlist.size(); i++) {
                                    for (int j = 0; j < readDevice.size(); j++) {
                                        if (favorlist.get(i) == readDevice.get(j).getDeviceId()) {
                                            readDevice.get(j).setControlllist(true);
                                            selectedDevice.add(readDevice.get(j));
                                        }
                                    }
                                }
                                for (int i = 0; i < readDevice.size(); i++) {
                                    basicAdapter.addItem(new Custom3_Item(getResources().getDrawable(R.drawable.device_image), readDevice.get(i).getDevice_name(), String.valueOf(readDevice.get(i).getDeviceId())));
                                }
                                mydevice.setAdapter(basicAdapter);
                                genius.setSelectedDevice(selectedDevice);
                                genius.setReadDevice(readDevice);

                                Log.d(TAG, "IotActivity read : " + genius.getSelectedDevice().size());
                                break;

                            case Command.GET_ONE_DEVICE:
                                Log.d(TAG, "GET Selective Device\n");
                                Log.d(TAG, "read Device : " + readDevice.size());
                                readDevice = deviceDB.getAllDeviceFromDB();
                                break;

                            case Command.RESULT_OK:
//                            if(isRequestedScan){
//                                isRequestedScan = false;
//                                mydevice.setAdapter(null);
//                                sendUDP(command.request_current_Device_to_Server());
//                            }
                                Toast.makeText(getApplicationContext(),"Result OK",Toast.LENGTH_SHORT).show();
                                break;

                            case Command.RESULT_NOTCONNECTED:
                                LinearLayout layout = (LinearLayout) View.inflate(IOT_Activity.this, R.layout.notconnected, null);
                                iot_display.removeAllViews();
                                iot_display.addView(layout);
                                ImageView retry = (ImageView) findViewById(R.id.retry);
                                retry.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        sendUDP(command.request_current_Device_to_Server());
                                        iot_display.removeAllViews();
                                    }
                                });

                                break;

                            case Command.UPDATE_DEVICE:
                                int updated_id = ((int) recvPacket.getParameter()[0]) & 0xff;
                                Log.d(TAG, "updated id : " + updated_id);
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {

                                }
                                sendUDP(command.request_current_Device_to_Server());

                                break;
                        }
                    }
                break;

                case SCAN_TIMEOVER :
                    mydevice.setAdapter(null);
                    mydevice.setAdapter(scanAdapter);

                    break;

                case MySpeech.MYSPEECH :

                    switch(msg.arg1){
                        case MySpeech.RESULT_RECORD :
                            listened = ((ArrayList<String>)msg.obj).get(0);
                            speechText.setText(listened);
                            Log.d(TAG,"Listened : " + listened);
                            SpeechProcessManager speechmanager = new SpeechProcessManager(IOT_Activity.this,listened,mHandler);
                            break;

                        case MySpeech.ERROR_RECORD :
                            speechDialog.dismiss();
                            break;
                    }

                    break;


                case SpeechProcessManager.SPEECH_RESULT :
                    int result = msg.arg1;
                    switch(result){
                        case  SpeechProcessManager.RESULT_FAILED_PROCESS :
                            tts.speak("무슨 말씀이신지 이해할 수가 없네요..", TextToSpeech.QUEUE_FLUSH,null);;
                            speechDialog.cancel();
                            break;

                        case SpeechProcessManager.RESULT_FAILED_SELECT_DEVICE :
                            tts.speak("어떤 장치를 동작 하실지 잘 모르겠네요.",TextToSpeech.QUEUE_FLUSH,null);
                            speechDialog.cancel();
                            break;

                        case SpeechProcessManager.RESULT_OPTION_REQUEST:

                            int index = msg.arg2;
                            executeOption(index);
                            Log.d(TAG,"option number = "+ index);
                            speechDialog.dismiss();
                            break;

                        case SpeechProcessManager.RESULT_SUCCESS :
                            int arg2 = msg.arg2;
                            String ttsText = "";

                            if(arg2 == -1) {
                                ArrayList<Integer> obj = (ArrayList<Integer>) msg.obj;
                                int selected_command = obj.get(0);
                                int selected_id = obj.get(1);
                                ttsText += deviceDB.getDeviceUsingID(selected_id).getDevice_name() ;
                                ttsText += SpeechProcessManager.getTTS_String(deviceDB.getDeviceUsingID(selected_id),selected_command);
                                tts.speak(ttsText, TextToSpeech.QUEUE_FLUSH, null);
                                executeSpeechRequest(selected_id,selected_command,listened);

                            }
                            else if(arg2 == SpeechProcessManager.REQUEST_SCAN_DEVICE){
//                                mDrawer.closeDrawer(GravityCompat.START);
                                ttsText += "새로운 장치를 검색합니다.";
                                tts.speak(ttsText,TextToSpeech.QUEUE_FLUSH,null);
                                mydevice.setAdapter(null);
                                isRequestedScan = true;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DiscoverBLEDevice();
                                    }
                                }).start();

                                CustomAdapter3 adapter = new CustomAdapter3(IOT_Activity.this);
                                adapter.addItem(new Custom3_Item(getResources().getDrawable(R.drawable.device_image),"Scanning...",""));
                                mydevice.setAdapter(adapter);
                            }

                            speechDialog.dismiss();
                            break;
                    }

                    break;

            }
        }
    };
    private AlertDialog dialog ;
    private AdapterView.OnItemLongClickListener mOnLongItemClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d(TAG,"Long Click");
            final Device removed = readDevice.get(i);
            AlertDialog.Builder builder = new AlertDialog.Builder(IOT_Activity.this);
            builder.setTitle("Device Remove");
            builder.setMessage("정말로 '" + removed.getDevice_name() + "' 를 지우시겠습니까");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    sendUDP(command.request_removeDevice_toServer(removed.getDeviceId()));
                    deviceDB.deleteFavorFromTable(removed.getDeviceId());
                }
            }).setNegativeButton("NO",null);
            dialog = builder.create();
            dialog.show();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
//                    try{
//                        Thread.sleep(1000);
//                    }catch (InterruptedException e){};
                    sendUDP(command.request_current_Device_to_Server());
                }
            });
            return true;
        }
    };


    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(final AdapterView<?> adapterView, View view, final int arg2, long l) {
            if(isRequestedScan){
                AlertDialog.Builder ab = new AlertDialog.Builder(IOT_Activity.this);
                ab.setTitle("장치 이름 입력");
                final LinearLayout layout = (LinearLayout)View.inflate(IOT_Activity.this, R.layout.device_name_request,null);
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
                            device_type = Command.LED;
                        }
                        else if(type.contains("DOOR")){
                            device_type = Command.DOOR;
                        }
                        else if(type.contains("WINDOW")){
                            device_type = Command.WINDOW;
                        }
                        else{
                            Toast.makeText(IOT_Activity.this,"Unknown",Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                        Log.d("IOT","name : " + name + "\taddress : " + address);

                        sendUDP(command.request_addDevice_toServer(device_type,name,address));
                        scanAdapter.removeItem(arg2);
                        mydevice.setAdapter(scanAdapter);

                        if(scanAdapter.getCount() == 0){
                            try{
                                Thread.sleep(500);
                            }catch (InterruptedException e){}
                            sendUDP(command.request_current_Device_to_Server());
                            isRequestedScan = false;
                        }

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
                        Toast.makeText(IOT_Activity.this,"등록을 취소합니다",Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.show();

            }
            else{
                int type = readDevice.get(arg2).getDeviceType();

                if(type == Command.LED){
                    new LED_controller(IOT_Activity.this, IOT_Activity.this, readDevice.get(arg2), mHandler,udp, command);
                }
                else if(type == Command.DOOR){
                    new LED_controller(IOT_Activity.this, IOT_Activity.this, readDevice.get(arg2), mHandler,udp, command);
                }
                else if(type == Command.WINDOW){
                    new Window_Controller(IOT_Activity.this, IOT_Activity.this, readDevice.get(arg2), mHandler,udp, command);

                }

            }
        }
    };

    private BluetoothAdapter btadapter = BluetoothAdapter.getDefaultAdapter();
    private ArrayList<BluetoothDevice> mLeDevice;
    private void DiscoverBLEDevice(){
        scanAdapter = new CustomAdapter3(IOT_Activity.this);
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
                        if(bluetoothDevice.getName() != null) {
                            mLeDevice.add(bluetoothDevice);
                            scanAdapter.addItem(new Custom3_Item(getResources().getDrawable(R.drawable.device_image), bluetoothDevice.getName(), bluetoothDevice.getAddress()));
                        }
                    }
                }
            });
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        Log.d("IOT","request Start service");
        Intent startService = new Intent(this, service);
        if (extras != null && !extras.isEmpty()) {
            Set<String> keys = extras.keySet();
            for (String key : keys) {
                String extra = extras.getString(key);
                startService.putExtra(key, extra);
            }
        }

        Intent bindingIntent = new Intent(this,service);
        Log.d("IOT","bind service");
//        bindService(bindingIntent,serviceConnection, Context.BIND_AUTO_CREATE);
        startService(startService);

    }

    private void sendUDP(final byte[] data){
        if(!isBusy) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean success = udp.request_udpWrite(data);
                }
            }).start();
        }
        try{
            Thread.sleep(500);
        }catch (InterruptedException e){};
    }

    private void sendUDP(String data){
        sendUDP(data.getBytes());
    }

    private void executeSpeechRequest(int selected_id, int selected_command,String sttText){
        Device device = deviceDB.getDeviceUsingID(selected_id);
        switch(selected_command){
            case SpeechProcessManager.REQUEST_LED_ON :
                if(device.getDeviceType() == Command.LED){
                    int[] value = {200,device.getvalue()[1]};
                    device.setValue(value);
                    sendUDP(command.request_execute_toServer(device));
                }

                break ;

            case SpeechProcessManager.REQUEST_LED_OFF :
                if(device.getDeviceType() == Command.LED){
                    int[] value = {100,device.getvalue()[1]};
                    device.setValue(value);
                    sendUDP(command.request_execute_toServer(device));
                }
                break ;

            case SpeechProcessManager.REQUEST_DOOR_ON :
//                if(device.getDeviceType() == Command.DOOR){
//                    tts = "장치를 열겠습니다.";
//                }
//                else{
//                    tts = "장치는 문이 아닙니다.";
//                }
                break ;

            case SpeechProcessManager.REQUEST_DOOR_OFF :
//                if(device.getDeviceType() == Command.DOOR){
//                    tts = "장치를 닫겠습니다..";
//                }
//                else{
//                    tts = "장치는 문이 아닙니다.";
//                }
                break ;

            case SpeechProcessManager.REQUEST_WINDOW_ON :
                if(device.getDeviceType() == Command.WINDOW){
                    int[] value = {200,device.getvalue()[1]};
                    device.setValue(value);
                    sendUDP(command.request_execute_toServer(device));
                }
                break ;

            case SpeechProcessManager.REQUEST_WINDOW_OFF :
                if(device.getDeviceType() == Command.WINDOW){
                    int[] value = {100,device.getvalue()[1]};
                    device.setValue(value);
                    sendUDP(command.request_execute_toServer(device));
                }
                break ;

            case SpeechProcessManager.REQUEST_LED_MORE_BRIGHT:
                if(device.getDeviceType() == Command.LED){
                    if(device.getvalue()[0] == 200){
                        int value_temp = device.getvalue()[1] + 20;
                        if(value_temp > 200)
                            value_temp = 200;

                        int[] value = {200, value_temp};
                        device.setValue(value);
                        sendUDP(command.request_execute_toServer(device));
                    }
                }
                break;

            case SpeechProcessManager.REQUEST_LED_LESS_BRIGHT:
                if(device.getDeviceType() == Command.LED){
                    if(device.getvalue()[0] == 200){
                        int value_temp = device.getvalue()[1] - 20;
                        if(value_temp < 100)
                            value_temp = 100;

                        int[] value = {200, value_temp};
                        device.setValue(value);
                        sendUDP(command.request_execute_toServer(device));
                    }
                }
                break;

            case SpeechProcessManager.REQUEST_WINDOW_MORE_OPEN:
                if(device.getDeviceType() == Command.WINDOW){
                    if(device.getvalue()[0] == 200){
                        int value_temp = device.getvalue()[1] + 20;
                        if(value_temp > 200)
                            value_temp = 200;

                        int[] value = {200, value_temp};
                        device.setValue(value);
                        sendUDP(command.request_execute_toServer(device));
                    }
                }
                break;

            case SpeechProcessManager.REQUEST_WINDOW_MORE_CLOSE:
                if(device.getDeviceType() == Command.WINDOW){
                    if(device.getvalue()[0] == 200){
                        int value_temp = device.getvalue()[1] - 20;
                        if(value_temp < 100)
                            value_temp = 100;

                        int[] value = {200, value_temp};
                        device.setValue(value);
                        sendUDP(command.request_execute_toServer(device));
                    }
                }
                break;

            case SpeechProcessManager.REQUEST_LED_SPECIFIED_BRIGHT:
                if(device.getDeviceType() == Command.LED) {
                    int[] value = new int[2];
                    value[0] = 200;
                    if (sttText.contains("1단계")) {
                        value[1] = 120;
                    } else if (sttText.contains("2단계")) {
                        value[1] = 140;
                    } else if (sttText.contains("3단계")) {
                        value[1] = 160;
                    } else if (sttText.contains("4단계")) {
                        value[1] = 180;
                    } else if (sttText.contains("5단계")) {
                        value[1] = 200;
                    } else {
                        value[0] = device.getvalue()[0];
                        value[1] = device.getvalue()[1];
                    }
                    device.setValue(value);
                    sendUDP(command.request_execute_toServer(device));
                }
                break;

            case SpeechProcessManager.REQUEST_WINDOW_SPECIFIED_DEGREE:
                if(device.getDeviceType() == Command.WINDOW) {
                    int[] value = new int[2];
                    value[0] = 200;
                    if (sttText.contains("1단계")) {
                        value[1] = 120;
                    } else if (sttText.contains("2단계")) {
                        value[1] = 140;
                    } else if (sttText.contains("3단계")) {
                        value[1] = 160;
                    } else if (sttText.contains("4단계")) {
                        value[1] = 180;
                    } else if (sttText.contains("5단계")) {
                        value[1] = 200;
                    } else {
                        value[0] = device.getvalue()[0];
                        value[1] = device.getvalue()[1];
                    }
                    device.setValue(value);
                    sendUDP(command.request_execute_toServer(device));
                }
                break;



//            case SpeechProcessManager.REQUEST_SCAN_DEVICE :
//                tts = "장치 검색을 시작하겠습니다. ";
//                break ;
        }
    }

    private OptionDB optionDB;
    private boolean[] isOption = {false, false, false, false, false, false};
    private ArrayList<ArrayList<OptionData>> optionList;
    private void implementationMenu2(LinearLayout menu2) {
        if(optionDB == null){
            optionDB = new OptionDB(IOT_Activity.this, mHandler,"optionDB");
        }
        if(optionList == null){
            optionList = new ArrayList<>();
        }
        ListView optionListView = menu2.findViewById(R.id.optionListView);
        final TextView addOption = menu2.findViewById(R.id.addOption);
        addOption.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    addOption.setBackgroundColor(Color.rgb(0x5f,0x5f,0x5f));
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    addOption.setBackgroundColor(Color.rgb(0xaf,0xaf,0xaf));
                    Intent intent = new Intent(IOT_Activity.this, AddOptionActivity.class);
                    startActivityForResult(intent,REQUEST_ADD_OPTION);
                }

                return true;
            }
        });
        CustomAdapter1 adapter1 = new CustomAdapter1(IOT_Activity.this);

        int i=0;
        ArrayList<OptionData> temp = optionDB.getAllDeviceFromDB();
        Log.d(TAG,"OptionMenu : " + temp.size());
        final ArrayList<String> optionName = new ArrayList<>();
        final HashMap<Integer , ArrayList<OptionData>> eachOption = new HashMap<>();
        for(int k=0; k<temp.size() ; k++){
            if(!optionName.contains(temp.get(k).getName())){
                Log.d(TAG,"No contain -> " + temp.get(k).getName());
                optionName.add(temp.get(k).getName());
                ArrayList<OptionData> option = new ArrayList<>();
                option.add(new OptionData(temp.get(k).getName(),temp.get(k).getId(),temp.get(k).getV1(),temp.get(k).getV2()));
                eachOption.put(optionName.indexOf(temp.get(k).getName()),option);
            }
            else{
                Log.d(TAG,"contained");
                int key = optionName.indexOf(temp.get(k).getName());
                eachOption.get(key).add(new OptionData(temp.get(k).getName(),temp.get(k).getId(),temp.get(k).getV1(),temp.get(k).getV2()));
            }
        }
        Log.d(TAG,"optionString\n" + optionName);
        Log.d(TAG,"optionHash\n" + eachOption);

        for(int k=0 ; k<optionName.size() ; k++){
            adapter1.addItem(new Custom1_Item(getResources().getDrawable(R.drawable.enterhome),optionName.get(k)));
        }
        optionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                Toast.makeText(getApplicationContext(),optionName.get(i),Toast.LENGTH_SHORT).show();
//                isBusy = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<OptionData> selected = eachOption.get(i);
                        for(int j=0; j<selected.size(); j++){
                            Device tempDevice = deviceDB.getDeviceUsingID(selected.get(j).getId());
                            int[] value = new int[2];
                            value[0] = selected.get(j).getV1();
                            value[1] = selected.get(j).getV2();
                            tempDevice.setValue(value);
                            sendUDP(command.request_execute_toServer(tempDevice));
                            try{
                                Thread.sleep(1500);
                            }catch (InterruptedException e){
                                e.printStackTrace();
                            }
                        }
//                        isBusy = false;
                    }
                }).start();


            }
        });

        optionListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String query = "delete from optionDB WHERE OptionName = '" + optionName.get(i) + "'";
                optionDB.executeQuery(query);
                Toast.makeText(getApplicationContext(),optionName.get(i) + " 삭제", Toast.LENGTH_SHORT).show();

                try{
                    Thread.sleep(500);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                implementationMenu2(menu2Temp);
                return true;
            }
        });

        optionListView.setAdapter(adapter1);
    }

    private boolean isBusy = false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ADD_OPTION){
            implementationMenu2(menu2Temp);
//            optionDB.dropTable("optionDB");
        }
    }

    private void executeOption(final int index){
        if(optionDB == null){
            optionDB = new OptionDB(IOT_Activity.this, mHandler,"optionDB");
        }
        ArrayList<OptionData> temp = optionDB.getAllDeviceFromDB();
        Log.d(TAG,"OptionMenu : " + temp.size());
        final ArrayList<String> optionName = new ArrayList<>();
        final HashMap<Integer , ArrayList<OptionData>> eachOption = new HashMap<>();
        for(int k=0; k<temp.size() ; k++){
            if(!optionName.contains(temp.get(k).getName())){
                Log.d(TAG,"No contain -> " + temp.get(k).getName());
                optionName.add(temp.get(k).getName());
                ArrayList<OptionData> option = new ArrayList<>();
                option.add(new OptionData(temp.get(k).getName(),temp.get(k).getId(),temp.get(k).getV1(),temp.get(k).getV2()));
                eachOption.put(optionName.indexOf(temp.get(k).getName()),option);
            }
            else{
                Log.d(TAG,"contained");
                int key = optionName.indexOf(temp.get(k).getName());
                eachOption.get(key).add(new OptionData(temp.get(k).getName(),temp.get(k).getId(),temp.get(k).getV1(),temp.get(k).getV2()));
            }
        }
        Log.d(TAG,"optionString\n" + optionName);
        Log.d(TAG,"optionHash\n" + eachOption);
        tts.speak(optionName.get(index) + " 옵션을 수행합니다.",TextToSpeech.QUEUE_FLUSH,null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<OptionData> selected = eachOption.get(index);
                for(int j=0; j<selected.size(); j++){
                    Device tempDevice = deviceDB.getDeviceUsingID(selected.get(j).getId());
                    int[] value = new int[2];
                    value[0] = selected.get(j).getV1();
                    value[1] = selected.get(j).getV2();
                    tempDevice.setValue(value);
                    sendUDP(command.request_execute_toServer(tempDevice));
                    try{
                        Thread.sleep(1500);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
//                        isBusy = false;
            }
        }).start();

    }
}
