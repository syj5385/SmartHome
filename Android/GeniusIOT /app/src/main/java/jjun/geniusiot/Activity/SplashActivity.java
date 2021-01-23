package jjun.geniusiot.Activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import jjun.geniusiot.Security.FIngerprintManager;
import jjun.geniusiot.Application.IoTDevice;
import jjun.geniusiot.NetworkService.Command;
import jjun.geniusiot.NetworkService.GeniusProtocol;
import jjun.geniusiot.NetworkService.Packet;
import jjun.geniusiot.NetworkService.TCP_Connection;
import jjun.geniusiot.NetworkService.UDP;
import jjun.geniusiot.R;
import jjun.geniusiot.Sound.SoundManager;


public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private ImageView loading;
    private TextView loading_text;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        loading = findViewById(R.id.loading);
        loading_text = findViewById(R.id.loading_text);
        soundManager = new SoundManager(SplashActivity.this);
    }

    private SplashThread mThread;
    private TCP_Connection conn;
    long start_t, current_t;
    @Override
    protected void onResume() {
        super.onResume();
        setFilter();
        if(checkPermission()){
            mThread = new SplashThread(loading);
            mThread.start();
            new Thread(new Runnable() {
                private UDP mUDP;
                private static final int TIME_OUT = 5000;
                @Override
                public void run() {

                    Log.d(TAG,"Send Hello android check message");
                    conn = new TCP_Connection(SplashActivity.this,5000);

                    conn.send(new Command().request_hello_android_to_Server());

                }
            }).start();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mThread != null)
            mThread.setRunning(false);

        unregisterReceiver(splashReceiver);
        if(conn != null) {
            conn.disconnect();
            while (conn.getState() != TCP_Connection.STATE_DISCONNECTED) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {

                }
            }
            conn.destroy();
            conn = null;
        }
    }

    private class SplashThread extends Thread{

        private ImageView loading_img;
        private float rotation = 0;
        private boolean running = false;

        public SplashThread(ImageView loading) {
            super();
            this.loading_img = loading;
            setRunning(true);

        }

        @Override
        public void run() {
            super.run();
            while(running){
                runOnUiThread(rotationRunnable);
                try {
                    this.sleep(15);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        public void setRunning(boolean running){
            this.running = running;
        }

        public boolean isRunning(){
            return running;
        }

        private Runnable rotationRunnable = new Runnable() {
            @Override
            public void run() {
                loading_img.setRotation(rotation+=3);
            }
        };

    }

    private BroadcastReceiver splashReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();

            if (action.equals(IoTDevice.TCP_RECEIVED_DATA)) {
                byte[] check = intent.getByteArrayExtra(IoTDevice.TCP_RECEIVED_DATA);
                Packet packet = Command.readPacketFromServer(check);
                if(packet.getCommand() == GeniusProtocol.RESULT_OK){

                    FIngerprintManager finger = new FIngerprintManager(SplashActivity.this,SplashActivity.this);
                    mThread.setRunning(false);
//                    loading.setRotation(0);
                    loading.setImageDrawable(getResources().getDrawable(R.drawable.finger_before));
                    loading_text.setText("지문을 확인해주세요.");


                }
            }
            else if(action.equals(IoTDevice.IOEXCEPTION)){
                loading_text.setText("서버와의 연결을 실패하였습니다.\n5초 뒤 어플리케이션을 종료합니다.\n다시 시도해 주세요.");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                },5000);
            }

            else if(action.equals(IoTDevice.FINGER_ERROR)){
                //loading_text.setText("지문인식을 실패하였습니다.\n어플리케이션을 종료합니다.");
                //Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                //vibrator.vibrate(1000);
                //new Handler().postDelayed(new Runnable() {
                 //   @Override
                //    public void run() {
                   //     finish();
                   // }
                //},1000);
            }

            else if(action.equals(IoTDevice.FINGER_FAILED)){
                loading_text.setText("등록되지 않은 지문입니다.");
                Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(100);
            }

            else if(action.equals(IoTDevice.FINGER_SUCCESS)){
                loading.setImageDrawable(getResources().getDrawable(R.drawable.finger_after));
                loading_text.setText("인식 성공");
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){

                }

                Intent nextIntent = new Intent(SplashActivity.this, IoT_Activity.class);
                startActivity(nextIntent);
                overridePendingTransition(R.anim.move_from_bottom,R.anim.move_to_top);
                finish();
            }

        }
    };

    private void setFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(UDP.UDP_RESULT);
        filter.addAction(UDP.UDP_EXCEPTION);
        filter.addAction(IoTDevice.TCP_RECEIVED_DATA);
        filter.addAction(IoTDevice.FINGER_ERROR);
        filter.addAction(IoTDevice.FINGER_FAILED);
        filter.addAction(IoTDevice.FINGER_SUCCESS);

        registerReceiver(splashReceiver,filter);
    }

    private boolean checkPermission(){
        boolean granted = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionResult = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            int permissionResult1= checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            int permissionResult2 = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            int permissionResult3 = checkSelfPermission(Manifest.permission.GET_ACCOUNTS);
            int permissionresult4 = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permissionresult5 = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permissionResult == PackageManager.PERMISSION_DENIED
                    || permissionResult1 == PackageManager.PERMISSION_DENIED
                    || permissionResult2 == PackageManager.PERMISSION_DENIED
                    || permissionResult3 == PackageManager.PERMISSION_DENIED
                    || permissionresult4 == PackageManager.PERMISSION_DENIED
                    || permissionresult5 == PackageManager.PERMISSION_DENIED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                        || shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
                        || shouldShowRequestPermissionRationale(Manifest.permission.GET_ACCOUNTS)
                        || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                        || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SplashActivity.this);
                    dialog.setTitle("권한이 필요합니다.").setMessage("이 기능을 사용하기 위해서는 단말기의 권한이 필요합니다. 계속 하시겠습니까")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{
                                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                                Manifest.permission.RECORD_AUDIO,
                                                Manifest.permission.GET_ACCOUNTS,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
                                    }
                                }
                            }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).create().show();

                } else {
                    requestPermissions(
                            new String[]{
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.GET_ACCOUNTS,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                            },1000);
                }
            }
            else{
                granted = true;
            }
        }
        return granted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1000){
            boolean result = true;
            for(int i=0; i<grantResults.length;i++){
                if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                    result = false;
                    break;
                }
            }
            if(!result){
                Toast.makeText(getApplicationContext(),"권한이 승인되지 않아 어플을 종료합니다.",Toast.LENGTH_LONG).show();
                finish();
            }
            else{
                mThread = new SplashThread(loading);
                mThread.start();
                new Thread(new Runnable() {
                    private UDP mUDP;
                    private static final int TIME_OUT = 5000;
                    @Override
                    public void run() {

                        Log.d(TAG,"Send Hello android check message");
                        conn = new TCP_Connection(SplashActivity.this,5000);

                        conn.send(new Command().request_hello_android_to_Server());

                    }
                }).start();
            }
        }
    }
}

