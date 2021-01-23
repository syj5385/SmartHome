package jjun.geniusiot.Security;

import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Toast;

import jjun.geniusiot.Application.IoTDevice;

public class FingerPrintHandler extends FingerprintManager.AuthenticationCallback {

    private Context context;

    public FingerPrintHandler(Context context) {
        this.context = context;
    }

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject){
        CancellationSignal cancellationSignal = new CancellationSignal();
        manager.authenticate(cryptoObject,cancellationSignal,0,this,null);
//        Toast.makeText(context,"지문을 확인합니다.",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
//        Toast.makeText(context,"Authentication error",Toast.LENGTH_SHORT).show();
        context.sendBroadcast(new Intent(IoTDevice.FINGER_ERROR));
        Log.d("FingerPrintHandler",errString.toString());
    }


    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
        Log.d("FingerPrintHandler","onAuthenticationHelp");
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
//        Toast.makeText(context,"인식 성공",Toast.LENGTH_SHORT).show();
        context.sendBroadcast(new Intent(IoTDevice.FINGER_SUCCESS));
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
//        Toast.makeText(context,"등록되지 않은 지문입니다.",Toast.LENGTH_SHORT).show();
        context.sendBroadcast(new Intent(IoTDevice.FINGER_FAILED));
    }
}
