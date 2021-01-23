package jjun.geniusiot.SpeechRecognizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import jjun.geniusiot.Application.IoTDevice;
import jjun.geniusiot.R;

public class MySpeech {

    private static final String TAG = "MySpeech";

    // Handler MACRO
    public static final int MYSPEECH = 20;
    public static final int START_RECORD = 0;
    public static final int END_RECORD = 1;
    public static final int RESULT_RECORD = 2;
    public static final int ERROR_RECORD = 3;

    private Activity activity;
    private Context context;

    //SpeechRecognizer Object
    private SpeechRecognizer stt;

    // TextToSpeech Object

    private Intent sttIntent;

    private AlertDialog dialog;

    public MySpeech(Activity activity, Context context) {
        this.context = context;
        this.activity = activity;


        // Initialize SpeechToText Object
        sttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,context.getPackageName());
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");

        stt = SpeechRecognizer.createSpeechRecognizer(context);
        stt.setRecognitionListener(mRecognizerListener);
    }

    private RecognitionListener mRecognizerListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            try{
                Thread.sleep(500);
            }catch (InterruptedException e) {
                Log.d(TAG, "Wait for recognizer");
            }
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG,"onBeginningOfSpeech");

        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG,"onEndOfSpeech");

        }

        @Override
        public void onError(int i) {
            Log.d(TAG,"onError : " + i);
            message.setText("에러 발생...");
            stopRecognizer();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.cancel();
                }
            },2000);
        }

        @Override
        public void onResults(Bundle bundle) {
            Log.d(TAG,"onResults");
            ArrayList<String> text = (ArrayList<String>)bundle.get(SpeechRecognizer.RESULTS_RECOGNITION);
            Log.d(TAG,"Text : " + text.get(0));
            Intent intent = new Intent(IoTDevice.RESULT_RECORD);
            intent.putStringArrayListExtra(IoTDevice.RESULT_RECORD,text);
            message.setText("처리 중...");
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e){

            }
            stopRecognizer();
            activity.sendBroadcast(intent);
            dialog.dismiss();
        }

        @Override
        public void onPartialResults(Bundle bundle) {
            Log.d(TAG,"onPartialResult");
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
            Log.d(TAG,"onEvent");
        }
    };

    public void startRecognizer(){
        executeSpeechDialog();
        stt.startListening(sttIntent);
        message.setText("듣고 있습니다...");
        Log.d(TAG, "Start Recognizer");
    }

    public void stopRecognizer(){

//        stt.stopListening();
        if(stt != null){
            stt.cancel();
            stt.destroy();
            stt = null;
        }

    }

    private TextView message ;
    private void executeSpeechDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LinearLayout voice = (LinearLayout)View.inflate(activity,R.layout.voice_layout,null);
        message = voice.findViewById(R.id.message);
        builder.setView(voice);
        builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                stopRecognizer();
                dialog.cancel();
            }
        });

        dialog = builder.create();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
//                stopRecognizer();
                try {
                    Thread.sleep(500);
                }catch (InterruptedException e){

                }
                activity.sendBroadcast(new Intent(IoTDevice.ERROR_RECORD));
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                activity.sendBroadcast(new Intent(IoTDevice.END_RECORD));
            }
        });

        dialog.show();
    }

    public void exitTTS(){

    }


}