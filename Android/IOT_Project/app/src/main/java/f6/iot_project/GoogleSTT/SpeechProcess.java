package f6.iot_project.GoogleSTT;


import android.content.Context;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import f6.iot_project.Database.SpeechDB;


import java.util.Locale;

/**
 * Created by comm on 2018-03-17.
 */

public class SpeechProcess {

    public static final int SPEECH_PROCESS = 101;
    public static final int SCAN_DEVICE    =   10;
    public static final int UPDATE_DEVICE   =     11;

    private AlertDialog dialog;
    private String listened;
    private Context context;
    private Handler mHandler;

    private int command;

    private TextToSpeech tts;
    private SpeechDB database;


    public SpeechProcess(Context context, AlertDialog dialog , String mesg,Handler mHandler) {
        this.dialog = dialog;
        this.listened = mesg;
        this.context = context;
        this.mHandler = mHandler;

        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                tts.setLanguage(Locale.KOREA);
            }
        });

        //  1st 기본 명령어 체크
        command = checkSpeechCommand();
        if(command == SCAN_DEVICE){
            mHandler.obtainMessage(SPEECH_PROCESS,command,0).sendToTarget();
        }


    }

    private int checkSpeechCommand(){
        int command_Temp = 0;
        if(listened.contains("찾아") || listened.contains("검색")){
            command_Temp = SCAN_DEVICE;

        }
        if(listened.contains("켜줘")){
            command_Temp = UPDATE_DEVICE;
//            tts.speak("장치 검색을 시작합니다.",TextToSpeech.QUEUE_FLUSH,null);
        }
        if(listened.contains("꺼줘")){
            command_Temp = UPDATE_DEVICE;
//            tts.speak("장치 검색을 시작합니다.",TextToSpeech.QUEUE_FLUSH,null);
        }

        return command_Temp;

    }
}