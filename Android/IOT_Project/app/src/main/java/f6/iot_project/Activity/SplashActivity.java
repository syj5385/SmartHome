package f6.iot_project.Activity;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import f6.iot_project.R;

/**
 * Created by comm on 2018-01-25.
 */

public class SplashActivity extends AppCompatActivity{

    private static final int REQUEST_PERMISSION = 0;
    private static final int PERMISSION_RESULT_OK = 1;

    private ImageView splashImage;
    private Handler mHandler;
    private int count;

    private Drawable[] ledDrawable = new Drawable[4];
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_splash);

        ledDrawable[0] = getResources().getDrawable(R.drawable.led0);
        ledDrawable[1] = getResources().getDrawable(R.drawable.led1);
        ledDrawable[2] = getResources().getDrawable(R.drawable.led2);
        ledDrawable[3] = getResources().getDrawable(R.drawable.led3);

//        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mHandler = new Handler();
        splashImage = (ImageView)findViewById(R.id.splash_image);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(count <4 ) {
//                    mHandler.postDelayed(updateImage, 500);

                    mHandler.post(updateImage);
                    try{
                        Thread.sleep(500);
                    }catch (InterruptedException e){};
                }

                mHandler.post(nextActivityRunnable);
            }
        }).start();
    }

    private Runnable updateImage = new Runnable() {
        @Override
        public void run() {
            if(splashImage != null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        splashImage.setImageDrawable(ledDrawable[count++]);
                    }
                });
            }
        }
    };

    private Runnable nextActivityRunnable = new Runnable() {
        @Override
        public void run() {

            Intent splash_intent;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                int permissionResult = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                int permissionResult2 = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                int permissionResult3 = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
                int permissionResult4 = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                int permissionResult5 = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

                if(permissionResult == PackageManager.PERMISSION_DENIED
                        || permissionResult2 == PackageManager.PERMISSION_DENIED
                        || permissionResult3 == PackageManager.PERMISSION_DENIED){
                    splash_intent = new Intent(SplashActivity.this,CheckPermissionActivity.class);
                    startActivityForResult(splash_intent,REQUEST_PERMISSION);
                    overridePendingTransition(R.anim.fade, R.anim.hold);
                    finish();
                }
                else{
                    Log.d("HANDLER","OK");
                    splash_intent = new Intent(SplashActivity.this,IOT_Activity.class);
                    startActivity(splash_intent);
                    overridePendingTransition(R.anim.fade, R.anim.hold);
                    finish();
                }

            }
            else{
                splash_intent = new Intent(SplashActivity.this,IOT_Activity.class);
                startActivity(splash_intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
                finish();
            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PERMISSION){
            if(resultCode == PERMISSION_RESULT_OK){
                Intent startIntent = new Intent(SplashActivity.this,IOT_Activity.class);
                startActivity(startIntent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
                finish();
            }

        }
    }


}
