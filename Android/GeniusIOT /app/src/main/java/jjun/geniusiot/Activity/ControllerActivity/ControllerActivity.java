package jjun.geniusiot.Activity.ControllerActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import jjun.geniusiot.Activity.IoT_Activity;
import jjun.geniusiot.NetworkService.Command;
import jjun.geniusiot.PublicRSS.ProcessXmlTask;
import jjun.geniusiot.AndroidDB.ChatbotDB;
import jjun.geniusiot.AndroidDB.DeviceDB;
import jjun.geniusiot.Device.Device;
import jjun.geniusiot.Application.IoTDevice;
import jjun.geniusiot.PublicRSS.LocationService;
import jjun.geniusiot.Service.MainService;
import jjun.geniusiot.NetworkService.UDP;
import jjun.geniusiot.R;

/**
 * Created by jjun on 2018. 7. 9..
 */

public class ControllerActivity extends AppCompatActivity {

    protected static final String TAG ="ControllerActivity";

    public static final String CONTROLLER = "CONTROLLER";
    public static final int REQUEST_CONTROLLER = 10;
    protected ImageView device_image;
    int deviceID;

    //Object
    protected DeviceDB db;
    protected Device device;
    protected UDP mUDP;
    protected ChatbotDB chatDB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        deviceID = getIntent().getIntExtra(CONTROLLER,-1);
        Log.d(TAG,"DeviceId : " + deviceID);
        if(deviceID == -1){
            finish();
        }
        else{
            db = new DeviceDB(ControllerActivity.this,DeviceDB.DeviceTable);
            device = db.getDeviceUsingID(deviceID);
        }
        initializeView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFilter();
        Intent intent = new Intent(ControllerActivity.this,MainService.class);
        bindService(intent,IoTConnection,Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        unbindService(IoTConnection);
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.move_from_top,R.anim.move_to_bottom);
    }

    private TextView device_name ;
    protected ImageView deviceImage;
    protected LinearLayout chatLayout;
    protected LinearLayout controllerLayout;
    protected ImageView delete, back;

    protected void initializeView(){
        device_name = findViewById(R.id.device_name);
        device_name.setText(device.getDevice_name());

        device_image = findViewById(R.id.device_image);
        chatLayout = findViewById(R.id.conversation_window);
        controllerLayout = findViewById(R.id.controllerLayout);
        chatScroll = findViewById(R.id.chatScroll);
        delete = findViewById(R.id.delete);
        back = findViewById(R.id.back);


        chatDB = new ChatbotDB(ControllerActivity.this, ControllerActivity.this,ChatbotDB.ChatTable);
//        chatDB.insertDataintoDataTable(ChatbotDB.SERVER, "Hello",deviceID);
//        chatDB.insertDataintoDataTable(ChatbotDB.USER,"OK",deviceID);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ControllerActivity.this);
                builder.setTitle("장치 제어 기록 삭제");
                builder.setMessage("정말로 삭제하시겠습니까?\n한 번 삭제된 내용은 복구 할 수 없습니다.").setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        chatDB.removeAllChatInDB(deviceID);
                        chatDB.setConversation(chatLayout,device.getDeviceId());
                    }
                }).setNegativeButton("아니오",null);
                builder.show();

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatDB.setConversation(chatLayout,deviceID);
                    }
                });

            }
        }).start();


        mHandler = new Handler();
        scrollDown(1000);
    }

    protected ScrollView chatScroll;
    private Handler mHandler;

    protected void insertChat(int who, String data, int id){
        chatDB.insertDataintoDataTable(who,data,id);
        chatDB.setConversation(chatLayout,device.getDeviceId());
        scrollDown();

    }

    private void scrollDown(){
        mHandler = new Handler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                chatScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void scrollDown(int delay){
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                chatScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        },delay);
    }

    protected void sendTCP(final byte[] payload){
        Log.d(TAG,"TCP Data Send");
        iotService.sendTcpData(payload);

    }
    private DeviceDB deviceDB;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(IoTDevice.UPDATE_DEVICE_OK)){
                insertChat(ChatbotDB.SERVER,"요청하신 동작을 수행하였습니다.",deviceID);
//                initializeView();
            }
            if (action.equals(IoTDevice.IOEXCEPTION)) {
                insertChat(ChatbotDB.SERVER,"요청하신 동작을 수행하지 못하였습니다.",deviceID);
            }




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

}
