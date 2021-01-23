package jjun.geniusiot.Service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.net.InetSocketAddress;
import java.net.Socket;

import jjun.geniusiot.NetworkService.Command;
import jjun.geniusiot.NetworkService.TCP_Connection;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebase";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate " + TAG + "Instance ID Service");

    }

    @Override
    public void onTokenRefresh() {
//        super.onTokenRefresh();
        Log.d(TAG,"onTokenRefresh()");
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG,"Token : " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token){
        TCP_Connection tcp = new TCP_Connection(this,10000);
        Log.d(TAG,"Add new FCM Token");
        tcp.send(Command.request_add_new_FCM_Token(token));
    }
}
