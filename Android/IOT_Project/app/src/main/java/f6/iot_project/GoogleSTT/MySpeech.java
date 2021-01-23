package f6.iot_project.GoogleSTT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

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
    private Handler mHandler;

    //SpeechRecognizer Object
    private SpeechRecognizer stt;

    // TextToSpeech Object
    private TextToSpeech tts;

    private Intent sttIntent;

    public MySpeech(Context context, Handler mHandler) {
        this.context = context;
        this.mHandler = mHandler;

        // Initialize TextToSpeech
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                tts.setLanguage(Locale.KOREA);
            }
        });

        // Initialize SpeechToText Object
        sttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,context.getPackageName());
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");

        stt = SpeechRecognizer.createSpeechRecognizer(context);
        stt.setRecognitionListener(mRecognizerListeer);
    }

    private RecognitionListener mRecognizerListeer = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
//            Toast.makeText(context,"ready",Toast.LENGTH_SHORT).show();
            mHandler.obtainMessage(START_RECORD).sendToTarget();

            try{
                Thread.sleep(500);
            }catch (InterruptedException e){
                Log.d(TAG,"Wait for recognizer");
            }

//            tts.speak("불을 켜줘",TextToSpeech.QUEUE_FLUSH,null);

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
            mHandler.obtainMessage(END_RECORD).sendToTarget();
            Log.d(TAG,"onEndOfSpeech");
        }

        @Override
        public void onError(int i) {
            Log.d(TAG,"onError : " + i);
//            Toast.makeText(context,"onError",Toast.LENGTH_SHORT).show();
            mHandler.obtainMessage(MYSPEECH,ERROR_RECORD,-1).sendToTarget();
            stt.destroy();
        }

        @Override
        public void onResults(Bundle bundle) {
            Log.d(TAG,"onResults");
            ArrayList<String> text = (ArrayList<String>)bundle.get(SpeechRecognizer.RESULTS_RECOGNITION);
            mHandler.obtainMessage(MYSPEECH,RESULT_RECORD,-1,text).sendToTarget();
//            tts.speak("네, 알겠습니다.", TextToSpeech.QUEUE_FLUSH,null);
            stt.destroy();
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
        stt.startListening(sttIntent);
        Log.d(TAG, "Start Recognizer");
    }

    public void stopRecognizer(){
        stt.stopListening();
    }

    public void initializeRecognizer(){

    }


}