package f6.iot_project.Controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import f6.iot_project.Database.DeviceDB;
import f6.iot_project.IoT_Device.Command;
import f6.iot_project.IoT_Device.Device;
import f6.iot_project.IoT_Device.GeniusHome;
import f6.iot_project.NotificationService.NotificationService;
import f6.iot_project.R;
import f6.iot_project.Sound.SoundManager;
import f6.iot_project.Network.UDP_Connection;

import static android.content.ContentValues.TAG;

/**
 * Created by comm on 2018-04-25.
 */

public class Controller {
    protected Activity activity ;
    protected Context context;
    protected Device device;
    protected Handler mHandler;

    protected LinearLayout temp;    // root Layout

    protected AlertDialog dialog;
    protected TextView Device_name;
    protected ImageView favoriteImg;
    protected LinearLayout fav_layout;
    protected LinearLayout controller;
    protected LinearLayout exit;

    protected GeniusHome genius;
    protected UDP_Connection udp;
    protected Command command;


    protected SoundManager soundManager;

    protected DeviceDB deviceDB;
    protected boolean isFavorited = false;

    public Controller(final Activity activity, Context context, Device device, Handler mHandler, UDP_Connection udp, Command command) {
        this.activity = activity;
        this.context = context;
        this.device = device;
        this.udp = udp;
        this.command = command;
        this.mHandler= mHandler;
        soundManager = new SoundManager(context);
        genius = (GeniusHome)activity.getApplication();
        deviceDB = new DeviceDB(context, mHandler,DeviceDB.DeviceTable);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        temp = (LinearLayout) View.inflate(context, R.layout.controllerlayout,null);

        Device_name = (TextView)temp.findViewById(R.id.device_name);
        Device_name.setText(device.getDevice_name());
        fav_layout = (LinearLayout)temp.findViewById(R.id.fav_layout);
        fav_layout.setOnClickListener(mFavClickListener);
        favoriteImg = (ImageView)temp.findViewById(R.id.favorite_img);
        controller = (LinearLayout)temp.findViewById(R.id.controller);
        exit = (LinearLayout)temp.findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundManager.play(0);
                dialog.dismiss();
            }
        });
        builder.setView(temp);

        dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
//                sendUDP(Controller.this.command.request_current_Device_to_Server());
            }
        });

        dialog.show();

//        initializeController(device);
//
//        implementationController();
    }

    private View.OnClickListener mFavClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            soundManager.play(0);
            if(isFavorited){
                isFavorited = false;
                favoriteImg.setImageDrawable(context.getResources().getDrawable(R.drawable.favor_off));
//                genius.removeControlledDevice(device.getDeviceId());
                deviceDB.deleteFavorFromTable(device.getDeviceId());
                device.setControlllist(false);

            }
            else{
                isFavorited = true;
                favoriteImg.setImageDrawable(context.getResources().getDrawable(R.drawable.favor_on));
                deviceDB.insertFavorIntoTable(device.getDeviceId());
                device.setControlllist(true);
            }
        }
    };

    protected void initializeController(Device device){
        ArrayList<Integer> favorlist = deviceDB.selectFavorFromTable();

        for(int i=0; i<favorlist.size(); i++){
            if(favorlist.get(i) == device.getDeviceId()){
                isFavorited = true;
                favoriteImg.setImageDrawable(context.getResources().getDrawable(R.drawable.favor_on));
            }
        }
    }

    protected void implementationController(){

    }
    protected void sendUDP(final byte[] data){
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = udp.request_udpWrite(data);
            }
        }).start();

        Log.d(TAG,"send BroadCast");
        Intent intent = new Intent(NotificationService.REQUEST_NEW_DEVICE);
//        activity.sendBroadcast(intent);
    }

    protected void sendUDP(String data){
        sendUDP(data.getBytes());
    }
}
