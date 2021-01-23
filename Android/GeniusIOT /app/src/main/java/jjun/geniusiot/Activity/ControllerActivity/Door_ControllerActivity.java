package jjun.geniusiot.Activity.ControllerActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jjun.geniusiot.Activity.IoT_Activity;
import jjun.geniusiot.Activity.SplashActivity;
import jjun.geniusiot.AndroidDB.ChatbotDB;
import jjun.geniusiot.Application.IoTDevice;
import jjun.geniusiot.Device.Device;
import jjun.geniusiot.NetworkService.Command;
import jjun.geniusiot.PublicRSS.LocationService;
import jjun.geniusiot.PublicRSS.ProcessXmlTask;
import jjun.geniusiot.R;
import jjun.geniusiot.Security.FIngerprintManager;
import jjun.geniusiot.Security.FingerPrintHandler;
import jjun.geniusiot.Service.MainService;

public class Door_ControllerActivity extends ControllerActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private ImageView door, fingerprint, device_image;
    private TextView  finger_info;

    @Override
    protected void initializeView() {
        super.initializeView();
        controllerLayout.removeAllViews();
        LinearLayout door_controller = (LinearLayout) View.inflate(this, R.layout.doorcontroller,null);
        door = door_controller.findViewById(R.id.door);
        fingerprint = door_controller.findViewById(R.id.finger);
        finger_info = door_controller.findViewById(R.id.finger_info);
        device_image = findViewById(R.id.device_image);
        device_image.setImageDrawable(getResources().getDrawable(R.drawable.door_open_small));

        if(device.getvalue()[0] == 200) {
            door.setImageDrawable(getResources().getDrawable(R.drawable.door_open));
            finger_info.setText("지문을 인식하면 문이 닫힙니다.");
        }
        else if(device.getvalue()[0] == 100) {
            door.setImageDrawable(getResources().getDrawable(R.drawable.door_close));
            finger_info.setText("지문을 인식하면 문이 열립니다.");
        }
        controllerLayout.addView(door_controller,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        new Thread(new Runnable() {
            @Override
            public void run() {
                FIngerprintManager manager = new FIngerprintManager(Door_ControllerActivity.this,Door_ControllerActivity.this) ;
            }
        }).start();


    }

    @Override
    protected void onResume() {
        super.onResume();
        setFilter();
        Intent intent = new Intent(Door_ControllerActivity.this,MainService.class);
        bindService(intent,IoTConnection, Context.BIND_AUTO_CREATE);
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

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(IoTDevice.FINGER_ERROR)){
                //finger_info.setText("지문인식을 실패하였습니다.\n어플리케이션을 종료합니다.");
                //Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                //vibrator.vibrate(1000);
               // new Handler().postDelayed(new Runnable() {
               //     @Override
                 //   public void run() {
                 //       finish();
                  //  }
               // },1000);
            }

            if(action.equals(IoTDevice.FINGER_FAILED)){
                finger_info.setText("등록되지 않은 지문입니다.\n 다시 시도해 주세요");
                Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                try{
                    Thread.sleep(500);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FIngerprintManager manager = new FIngerprintManager(Door_ControllerActivity.this,Door_ControllerActivity.this);
                    }
                }).start();

            }

            if(action.equals(IoTDevice.FINGER_SUCCESS)){
                fingerprint.setImageDrawable(getResources().getDrawable(R.drawable.finger_after));
                finger_info.setText("인식 성공");

                try{
                    Thread.sleep(1500);
                }catch (InterruptedException e){

                }
                int[] value = {device.getvalue()[0],180};
                if(device.getvalue()[0] == 100) {
                    insertChat(ChatbotDB.USER,device.getDevice_name() +"을(를) 열어줘",device.getDeviceId());
                    door.setImageDrawable(getResources().getDrawable(R.drawable.door_open));
                    finger_info.setText("지문을 인식하면 문이 닫힙니다.");
                    value[0] = 200;
                }
                else if(device.getvalue()[0] == 200) {
                    insertChat(ChatbotDB.USER,device.getDevice_name() +"을(를) 닫아줘",device.getDeviceId());
                    door.setImageDrawable(getResources().getDrawable(R.drawable.door_close));
                    finger_info.setText("지문을 인식하면 문이 열립니다.");
                    value[0] = 100;

                }
                device.setValue(value);
                sendTCP(Command.request_execute_toServer(device));

                try{
                    Thread.sleep(500);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FIngerprintManager manager = new FIngerprintManager(Door_ControllerActivity.this,Door_ControllerActivity.this);
                    }
                }).start();



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
        filter.addAction(IoTDevice.IOEXCEPTION);
        filter.addAction(IoTDevice.UPDATE_DEVICE_OK);
        filter.addAction(IoTDevice.FINGER_ERROR);
        filter.addAction(IoTDevice.FINGER_SUCCESS);
        filter.addAction(IoTDevice.FINGER_FAILED);

        registerReceiver(mReceiver,filter);
    }

}
