package jjun.geniusiot.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import jjun.geniusiot.Activity.ControllerActivity.Bath_ControllerActivity;
import jjun.geniusiot.Activity.ControllerActivity.ControllerActivity;
import jjun.geniusiot.Activity.ControllerActivity.Door_ControllerActivity;
import jjun.geniusiot.Activity.ControllerActivity.Lamp_ControllerActivity;
import jjun.geniusiot.Activity.ControllerActivity.Temp_ControllerActivity;
import jjun.geniusiot.Activity.ControllerActivity.Window_ControllerActivity;
import jjun.geniusiot.PublicRSS.MyCity;
import jjun.geniusiot.PublicRSS.ProcessXmlTask;
import jjun.geniusiot.PublicRSS.XMLHandler;
import jjun.geniusiot.AndroidDB.ChatbotDB;
import jjun.geniusiot.AndroidDB.DeviceDB;
import jjun.geniusiot.Device.Device;
import jjun.geniusiot.Application.IoTDevice;
import jjun.geniusiot.PublicRSS.LocationService;
import jjun.geniusiot.Service.MainService;
import jjun.geniusiot.NetworkService.Command;
import jjun.geniusiot.NetworkService.GeniusProtocol;
import jjun.geniusiot.NetworkService.Packet;
import jjun.geniusiot.NetworkService.UDP;
import jjun.geniusiot.R;
import jjun.geniusiot.Service.MyFirebaseInstanceIDService;
import jjun.geniusiot.Service.MyFirebaseMessagingService;
import jjun.geniusiot.Sound.SoundManager;

/**
 * Created by jjun on 2018. 7. 8..
 */

public class IoT_Activity extends AppCompatActivity {
    private static final String TAG = "IoT_Activity";

    //Object
    private LocationService mLocationService ;
    private ArrayList<Device> myDevice;
    private DeviceDB deviceDB;
    private ChatbotDB chatDB;
    private UDP mUDP;
    private List<Address> HomeAddress;
    private ProcessXmlTask xmlTask;
    private SoundManager soundManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_iot);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initializeView();
        initialieObject();
//        registerGcm();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int location1Permission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            int location2Permission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if(location1Permission == PackageManager.PERMISSION_GRANTED && location2Permission == PackageManager.PERMISSION_GRANTED){
                mLocationService = new LocationService(IoT_Activity.this);
                HomeAddress = mLocationService.getAddress((double)35.8875175,(double)128.6129114);
                myHomeAddress.setText(HomeAddress.get(0).getAddressLine(0));
                Log.d(TAG,HomeAddress.get(0).getAddressLine(0));
            }
        }
        else{
            mLocationService = new LocationService(IoT_Activity.this);
            HomeAddress = mLocationService.getAddress((double)35.8875175,(double)128.6129114);
           // Log.d(TAG,HomeAddress.get(0).getAddressLine(0));
//            myHomeAddress.setText(HomeAddress.get(0).getAddressLine(0));
        }
        if(HomeAddress != null)
            myCity = MyCity.getCityCode(HomeAddress.get(0));
        if(myCity == -1){
            Log.d(TAG,"myCity : " + myCity);
        }
        else {
            myHandler = new XMLHandler(mHandler);
            xmlTask = new ProcessXmlTask(this, myHandler);
            xmlTask.execute(mRSSURL);
//            initializeView();
        }

        Log.d(TAG,"Finished onCreate");
//        FIngerprintManager finger = new FIngerprintManager(this,this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFilter();
//        startService(new Intent(IoT_Activity.this, MainService.class));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startService(new Intent(IoT_Activity.this, MainService.class));
        }
        startService(MainService.class,IoTConnection,null);

//        startService(new Intent(IoT_Activity.this,MyFirebaseInstanceIDService.class));
//        startService(new Intent(IoT_Activity.this,MyFirebaseMessagingService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(IoTConnection);
        startService(new Intent(this,MainService.class));
//        Intent intent = new Intent(IoT_Activity.this,MainService.class);
//        startService(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
//        iotService.getServer_conn().disconnect();
//        stopService(new Intent(IoT_Activity.this,MainService.class));
    }

    private AlertDialog dialog;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(LocationService.LOCATION_CURRENT)) {
                double[] gps = intent.getDoubleArrayExtra(LocationService.LOCATION_CURRENT);
                List<Address> address = mLocationService.getAddress(gps[0],gps[1]);
            }
            else if(action.equals(UDP.UDP_RESULT)){
                byte[] deviceMessage = intent.getByteArrayExtra(UDP.UDP_RESULT);
                Log.d(TAG,"Get new Device From Server");
                Packet get_device_packet = Command.readPacketFromServer(deviceMessage);
                deviceDB.updateDeviceFromServer(get_device_packet);
                LinearLayout device_list_layout = findViewById(R.id.device_list);
                if(device_list_layout != null){
                    setDeviceList(device_list_layout);
                }
            }
            else if(action.equals(ProcessXmlTask.XML_EXCEPTION)){
                LinearLayout eachlayout = findViewById(R.id.eachStatelayout);
                TextView text = new TextView(IoT_Activity.this);
                Log.d(TAG,"미세먼지 데이터를 불러오는데 실패하였습니다.");
                text.setText("미세 먼지 데이터를 불러오는데 실패하였습니다.");
                text.setTextSize(50);
                text.setTextColor(Color.rgb(0xef, 0xef, 0xef));
                text.setGravity(View.TEXT_ALIGNMENT_CENTER);
                eachlayout.addView(text,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            }
            else if(action.equals(IoTDevice.FINISHED_UPDATE_DEVICE)){
                LinearLayout device_list_layout = findViewById(R.id.device_list);
                if(device_list_layout != null){
                    Log.d(TAG,"update device");
                    setDeviceList(device_list_layout);
                }
            }
        }
    };

    private ImageView openDrawer, menuopen;
    private DrawerLayout drawer;
    private TextView myHomeAddress, temperature,humidity;
    private int[] backColor;
    private int[] backColorPoint;
    private LinearLayout myHome;
    private LinearLayout myhome;

    private void initializeView() {
        menuopen = findViewById(R.id.menu);
        menuopen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent menuIntent = new Intent(IoT_Activity.this,MenuActivity.class);
                startActivityForResult(menuIntent,MenuActivity.REQUEST_RIGHT);
                overridePendingTransition(R.anim.move_from_right,R.anim.move_to_left);
            }
        });
        openDrawer = findViewById(R.id.openDrawer);
        openDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent menuIntent = new Intent(IoT_Activity.this,LeftActivity.class);
                startActivityForResult(menuIntent,LeftActivity.REQUEST_LEFT);
                overridePendingTransition(R.anim.move_from_left,R.anim.move_to_right);
            }
        });
        myHomeAddress = findViewById(R.id.homeAddress);

        myHome = findViewById(R.id.myHome);
        myhome = (LinearLayout)View.inflate(IoT_Activity.this,R.layout.myhomelayout,null);
        temperature = myhome.findViewById(R.id.temperature);
        humidity = myhome.findViewById(R.id.humid);
        temperature.setText("25");
        humidity.setText("100");

        myHome.addView(myhome,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Color Setting
        backColor = new int[8];
        backColor[0] = getResources().getColor(R.color.level0Color);
        backColor[1] = getResources().getColor(R.color.level1Color);
        backColor[2] = getResources().getColor(R.color.level2Color);
        backColor[3] = getResources().getColor(R.color.level3Color);
        backColor[4] = getResources().getColor(R.color.level4Color);
        backColor[5] = getResources().getColor(R.color.level5Color);
        backColor[6] = getResources().getColor(R.color.level6Color);
        backColor[7] = getResources().getColor(R.color.level7Color);

        backColorPoint = new int[8];
        backColorPoint[0] = getResources().getColor(R.color.level0SetColor);
        backColorPoint[1] = getResources().getColor(R.color.level1SetColor);
        backColorPoint[2] = getResources().getColor(R.color.level2SetColor);
        backColorPoint[3] = getResources().getColor(R.color.level3SetColor);
        backColorPoint[4] = getResources().getColor(R.color.level4SetColor);
        backColorPoint[5] = getResources().getColor(R.color.level5SetColor);
        backColorPoint[6] = getResources().getColor(R.color.level6SetColor);
        backColorPoint[7] = getResources().getColor(R.color.level7SetColor);
    }

    private void initialieObject(){
//        mUDP = new UDP(IoT_Activity.this, IoT_Activity.this,5000);
        deviceDB = new DeviceDB(IoT_Activity.this,DeviceDB.DeviceTable);
        chatDB = new ChatbotDB(IoT_Activity.this, IoT_Activity.this,ChatbotDB.ChatTable);
//        myDevice = Device.initializeDevice();
//
//        soundManager = new SoundManager(IoT_Activity.this);
//
//        try{
//            Thread.sleep(300);
//        }catch (InterruptedException e){
//            e.printStackTrace();
//        }
//

    }

    private void setDeviceList(LinearLayout list){
        list.removeAllViews();
        myDevice = deviceDB.getAllDeviceFromDB();
        Log.d(TAG,"The number Of Device = " + myDevice.size());
        for(int i=0; i<myDevice.size(); i++){
            LinearLayout temp = (LinearLayout)View.inflate(IoT_Activity.this,R.layout.iot_device_list,null);
            temp.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ImageView deviceImage = temp.findViewById(R.id.device_image);
            TextView device_name = temp.findViewById(R.id.device_name);

            final Device thisDevice = myDevice.get(i);
            device_name.setText(thisDevice.getDevice_name());
            if(thisDevice.getDeviceType() == Command.LED){
                temp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        soundManager.play(0);
                        Intent intent = new Intent(IoT_Activity.this, Lamp_ControllerActivity.class);
                        Log.d(TAG,"Device : " + thisDevice.getDeviceId());
                        intent.putExtra(ControllerActivity.CONTROLLER,thisDevice.getDeviceId());
                        startActivityForResult(intent, Lamp_ControllerActivity.REQUEST_CONTROLLER);
                        overridePendingTransition(R.anim.move_from_bottom,R.anim.move_to_top);
                    }
                });
                if(thisDevice.getvalue()[0] == 100) {
                    deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.light_off_small));
                }
                else if(thisDevice.getvalue()[0] == 200){
                    deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.light_on_small));
                }
            }
            else if(thisDevice.getDeviceType() == Command.WINDOW){
                temp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(IoT_Activity.this, Window_ControllerActivity.class);
                        Log.d(TAG,"Device : " + thisDevice.getDeviceId());
                        intent.putExtra(ControllerActivity.CONTROLLER,thisDevice.getDeviceId());
                        startActivityForResult(intent, Lamp_ControllerActivity.REQUEST_CONTROLLER);
                        overridePendingTransition(R.anim.move_from_bottom,R.anim.move_to_top);
                    }
                });
                if(thisDevice.getvalue()[0] == 100) {
                    deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.window_image_close));
                }
                else if(thisDevice.getvalue()[0] == 200){
                    deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.window_image_open));
                }
            }
            else if(thisDevice.getDeviceType() == Command.DOOR){
                temp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(IoT_Activity.this, Door_ControllerActivity.class);
                        Log.d(TAG,"Device : " + thisDevice.getDeviceId());
                        intent.putExtra(ControllerActivity.CONTROLLER,thisDevice.getDeviceId());
                        startActivityForResult(intent, Lamp_ControllerActivity.REQUEST_CONTROLLER);
                        overridePendingTransition(R.anim.move_from_bottom,R.anim.move_to_top);
                    }
                });
                if(thisDevice.getvalue()[0] == 100) {
                    deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.door_close));
                }
                else if(thisDevice.getvalue()[0] == 200){
                    deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.door_open));
                }
            }

            else if(thisDevice.getDeviceType() == GeniusProtocol.TEMP){
                Log.d(TAG,"temp update : " + thisDevice.getvalue()[0] + "to : " + temperature);
                if(temperature != null) {
                    temperature.setText(String.valueOf(thisDevice.getvalue()[0]));
                }
                if(humidity != null){
                    humidity.setText(String.valueOf(thisDevice.getvalue()[1]));
                }
                deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.temp_small));
                temp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(IoT_Activity.this, Temp_ControllerActivity.class);
                        Log.d(TAG,"Device : " + thisDevice.getDeviceId());
                        intent.putExtra(ControllerActivity.CONTROLLER,thisDevice.getDeviceId());
                        startActivityForResult(intent, Temp_ControllerActivity.REQUEST_CONTROLLER);
                        overridePendingTransition(R.anim.move_from_bottom,R.anim.move_to_top);
                    }
                });
            }
            else if(thisDevice.getDeviceType() == GeniusProtocol.BATH){
                Log.d(TAG,"bath update ");
                if(Device.getBathInformation(thisDevice.getvalue()[0], thisDevice.getvalue()[1]).get(Command.BATH_EXECUTION_REQUEST) ==1){
                    deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.bath_on_small));
                }
                else if(Device.getBathInformation(thisDevice.getvalue()[0], thisDevice.getvalue()[1]).get(Command.BATH_EXECUTION_REQUEST) == 0){
                    deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.bath_off_small));
                }
                temp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(IoT_Activity.this, Bath_ControllerActivity.class);
                        Log.d(TAG,"Device : " + thisDevice.getDeviceId());
                        intent.putExtra(ControllerActivity.CONTROLLER,thisDevice.getDeviceId());
                        startActivityForResult(intent, Lamp_ControllerActivity.REQUEST_CONTROLLER);
                        overridePendingTransition(R.anim.move_from_bottom,R.anim.move_to_top);
                    }
                });
            }

            else if(thisDevice.getDeviceType() == GeniusProtocol.GAS){
//                Log.d(TAG,"gas update : " + thisDevice.getvalue()[0] + "to : " + temperature);
//                if(temperature != null) {
//                    temperature.setText(String.valueOf(thisDevice.getvalue()[0]));
//                }
                deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.gas_image_small));
            }

            temp.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(IoT_Activity.this);
                    builder.setTitle("\""+thisDevice.getDevice_name()+"\" 삭제");
                    builder.setMessage("정말로 \""+thisDevice.getDevice_name() + "\"를 삭제하시겠습니까?").setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d(TAG,"REMOVE DEVICE -> " + thisDevice.getDeviceId());
                            deviceDB.deleteDeviceFromTable(thisDevice.getDeviceId());
                            chatDB.removeAllChatInDB(thisDevice.getDeviceId());
//                    iotService.getServer_conn().send(Command.request_removeDevice_toServer(thisDevice.getDeviceId()));
                            iotService.sendTcpData(Command.request_removeDevice_toServer(thisDevice.getDeviceId()));
                        }
                    }).setNegativeButton("아니오",null);
                    builder.show();
                    return true;
                }
            });
            list.addView(temp);
            Space space = new Space(IoT_Activity.this);
            space.setLayoutParams(new ViewGroup.LayoutParams(50, ViewGroup.LayoutParams.MATCH_PARENT));
            if(i != myDevice.size()-1)
                list.addView(space);
        }
    }

    //DAEGU Dusty Handler

    private HashMap<Integer, HashMap<Integer,Float>> xml_result_hash ;
    private boolean receivedCityInfo = false;
    private HashMap<Integer,Float> cityValue;
    private int myCity = -1;
    private XMLHandler myHandler;
    private static final String mRSSURL = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnMesureSidoLIst?sidoName=%EB%8C%80%EA%B5%AC&searchCondition=DAILY&pageNo=1&numOfRows=8&ServiceKey=LB8QqqtHZuD1gseaiioD1WQ3INSHdeUM37X2i7wsN8XmfZsbsMLXXtFBC3VLeuvo6ZizVVsSOkeOQXEGpCxHYw%3D%3D";
    private int bigDustState = -1;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case XMLHandler.XML_RESULT :
                    xml_result_hash = (HashMap<Integer, HashMap<Integer,Float>>)msg.obj;
                    for(int i=0 ; i<xml_result_hash.size(); i++){
                        Log.d(TAG,"city : " + xml_result_hash.get(i));
                    }
                    receivedCityInfo = true;
                    cityValue = xml_result_hash.get(myCity);
                    Log.d(TAG,"city : " + myCity + "\t" + cityValue);

                    xmlTask.cancel(true);
                    while(!xmlTask.isCancelled());
                    updateAirState(myCity,cityValue);

//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mUDP.request_udpWrite(Command.request_current_Device_to_Server());
//                        }
//                    }).start();

                    break;

                case XMLHandler.XML_TIME :
                    Log.d(TAG,"time : " + msg.obj);
//                    nowInfoString = (String)msg.obj;
//                    measure_t.setText("   측정 시각 : " +nowInfoString);

                    break;
            }
        }
    };

    private void updateAirState(int cityCode, HashMap<Integer,Float> city){
        // 1. update each Air state
        setEachAirState(cityCode,city);
//        setBigAirState(bigDustState);
        setBackground(bigDustState);
    }


    private void setEachAirState(int cityCode,HashMap<Integer,Float> city){
        Bitmap[] level_Bitmap = {
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.level0_image_big),400,400,false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.level1_image_big),400,400,false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.level2_image_big),400,400,false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.level3_image_big),400,400,false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.level4_image_big),400,400,false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.level5_image_big),400,400,false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.level6_image_big),400,400,false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.level7_image_big),400,400,false),
        };
        LinearLayout eachlayout = findViewById(R.id.eachStatelayout);
        eachlayout.removeAllViews();

        for(int i=0; i<city.size(); i++){
            LinearLayout each = (LinearLayout) View.inflate(IoT_Activity.this,R.layout.eachstatelayout,null);
            eachlayout.addView(each,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView topic = each.findViewById(R.id.topic);
            switch(i){
                case XMLHandler.SO2VALUE :
                    topic.setText("이산화황");
                    break;

                case XMLHandler.COVALUE :
                    topic.setText("일산화탄소");
                    break;

                case XMLHandler.O3VALUE :
                    topic.setText("오존");
                    break;

                case XMLHandler.NO2VALUE :
                    topic.setText("이산화질소");
                    break;

                case XMLHandler.PM10VALUE :
                    topic.setText("미세먼지");
                    break;

                case XMLHandler.PM25VALUE :
                    topic.setText("초미세먼지");
                    break;
            }
            TextView value = each.findViewById(R.id.value);
            value.setText(String.valueOf(city.get(i)));
            TextView state = each.findViewById(R.id.stateString);
            ImageView stateFace = each.findViewById(R.id.stateFace);
            String[] result = MyCity.getCurrentStateString(i,city.get(i));
            int index = Integer.parseInt(result[0]);
            if(index == -1){
                state.setText("-");
                stateFace.setImageDrawable(new BitmapDrawable(level_Bitmap[3]));
                value.setText("-");
                continue;
            }
            state.setText(result[1]);

            stateFace.setImageDrawable(new BitmapDrawable(level_Bitmap[Integer.parseInt(result[0])]));
            if(i == XMLHandler.PM10VALUE || i == XMLHandler.PM25VALUE){
                if(Integer.parseInt(result[0]) >= bigDustState){
                    bigDustState = Integer.parseInt(result[0]);
                }
            }
        }
    }

    private void setBackground(int bigState){
        LinearLayout back = findViewById(R.id.mainView);
        LinearLayout[] point = {
                findViewById(R.id.point1),
                findViewById(R.id.point2)
        };
        getWindow().setStatusBarColor(backColor[bigState]);
        back.setBackgroundColor(backColor[bigState]);
        for(int i=0; i<point.length ;i++) {
            point[i].setBackgroundColor(backColorPoint[bigState]);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ControllerActivity.REQUEST_CONTROLLER){
//            iotService.getServer_conn().send(Command.request_current_Device_to_Server());
            iotService.sendTcpData(Command.request_current_Device_to_Server());
        }
        if(requestCode == LeftActivity.REQUEST_LEFT){
            iotService.sendTcpData(Command.request_current_Device_to_Server());
        }
        if(requestCode == MenuActivity.REQUEST_RIGHT){
            iotService.sendTcpData(Command.request_current_Device_to_Server());
        }
    }


    //Service Connection
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
//        startService(startService);
        bindService(bindingIntent,serviceConnection, Context.BIND_AUTO_CREATE);
    }


    private MainService iotService;
    private final ServiceConnection IoTConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            iotService = ((MainService.MainBinder) arg1).getService();
            Log.d(TAG,"Service : " + String.valueOf(iotService));

            iotService.setFilter();
            sendBroadcast(new Intent(IoTDevice.IOT_ACTIVITY_START));
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            iotService = null;
            Log.e(TAG,"Service Disconnected");
        }
    };

    private void setFilter(){
        IntentFilter filter = new IntentFilter();
//        filter.addAction(IoTDevice.FINISH_GET_DEVICE);
//        filter.addAction(IoTDevice.FINISHED_REMOVE_DEVICE);
//        filter.addAction(IoTDevice.FINISHED_ADD_DEVICE);
//        filter.addAction(IoTDevice.UPDATE_DEVICE);
        filter.addAction(LocationService.LOCATION_CURRENT);
        filter.addAction(UDP.UDP_EXCEPTION);
        filter.addAction(UDP.UDP_RESULT);
        filter.addAction(ProcessXmlTask.XML_EXCEPTION);

        filter.addAction(IoTDevice.FINISHED_UPDATE_DEVICE);

        registerReceiver(mReceiver,filter);
    }

//    private int registerCount =0;
//    private void registerGcm(){
////        registerReceiver(new GcmBroadcastReceiver(),new IntentFilter());
//
//        Log.d(TAG,"Start GCM registration");
//        GCMRegistrar.checkDevice(this);
//        GCMRegistrar.checkManifest(this);
//        final String regid = GCMRegistrar.getRegistrationId(this);
//        if(regid.equals("")){
//            Log.d(TAG,"register Key");
//            GCMRegistrar.register(this,"813949817844");
//            if(registerCount++ < 1){
//                registerGcm();
//            }
//        }
//        else{
//            Log.d("ID of GCM",regid);
//        }
//    }


}
