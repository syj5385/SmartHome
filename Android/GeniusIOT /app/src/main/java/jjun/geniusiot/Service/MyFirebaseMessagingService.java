package jjun.geniusiot.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import jjun.geniusiot.AndroidDB.DeviceDB;
import jjun.geniusiot.Application.IoTDevice;
import jjun.geniusiot.Device.Device;
import jjun.geniusiot.NetworkService.Command;
import jjun.geniusiot.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MYFirebase";
    private DeviceDB device;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate " + TAG + "messaging Service");
        device = new DeviceDB(this, DeviceDB.DeviceTable);

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From : " + remoteMessage.getFrom());
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message payload : " + remoteMessage.getData());
            int id = Integer.parseInt(remoteMessage.getData().get("device"));
            if (id < 100 || id > 150)
                return;
            Device recv = device.getDeviceUsingID(id);
            if (recv == null)
                return;

            String text = remoteMessage.getData().get("content");
            sendNotification(recv, text);

//            device.getDeviceUsingID()
//            sendBroadcast(new Intent(IoTDevice.FCM_RECEIVED));

            Intent intent = new Intent(IoTDevice.FCM_RECEIVED);
            intent.putExtra(IoTDevice.FCM_RECEIVED,id);
            sendBroadcast(intent);
        }
    }

    private void sendNotification(Device device, String text) {
        Notification.Builder builder;
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("genius","update",NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("channel Description");
            channel.enableLights(true);
            channel.setLightColor(Color.YELLOW);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100,200,100,200});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(channel);
        }
        builder = new Notification.Builder(this);
        builder.setContentTitle(device.getDevice_name())
                .setContentText(text);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            builder.setVibrate(new long[]{100,200,100,200});
        }

        /* Device analysis */

        if(device.getDeviceType()== Command.DOOR){
            if(device.getvalue()[0] == 200)
                builder.setSmallIcon(R.drawable.door_open_small);
            else
                builder.setSmallIcon(R.drawable.door_close_small);
        }
        if(device.getDeviceType() == Command.BATH){
            builder.setSmallIcon(R.drawable.bath_on_small);
            sendBroadcast(new Intent(IoTDevice.FINISHED_UPDATE_DEVICE));
            Log.d(TAG,"FINISHED_UPDATE_DEVICE");
        }
        if(device.getDeviceType() == Command.GAS){
            builder.setSmallIcon(R.drawable.gas_image_small);
        }

        Notification notification = builder.build();
        notificationManager.notify(930316,notification);
    }
}

