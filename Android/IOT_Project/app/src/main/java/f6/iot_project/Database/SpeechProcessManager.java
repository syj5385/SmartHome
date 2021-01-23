package f6.iot_project.Database;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import f6.iot_project.IoT_Device.Command;
import f6.iot_project.IoT_Device.Device;

import static android.content.ContentValues.TAG;

/**
 * Created by comm on 2018-05-03.
 */

public class SpeechProcessManager {

    // Execution Command
    public static final int REQUEST_SCAN_DEVICE = 5;
    public static final int REQUEST_LED_ON = 10;
    public static final int REQUEST_LED_OFF = 11;
    public static final int REQUEST_DOOR_ON = 12;
    public static final int REQUEST_DOOR_OFF = 13;
    public static final int REQUEST_WINDOW_ON = 14;
    public static final int REQUEST_WINDOW_OFF = 15;
    public static final int REQUEST_LED_MORE_BRIGHT = 16;
    public static final int REQUEST_LED_LESS_BRIGHT = 17;
    public static final int REQUEST_WINDOW_MORE_OPEN = 18;
    public static final int REQUEST_WINDOW_MORE_CLOSE = 19;
    public static final int REQUEST_LED_SPECIFIED_BRIGHT = 20;
    public static final int REQUEST_WINDOW_SPECIFIED_DEGREE = 21;

    // result of Process
    public static final int SPEECH_RESULT = 101;
    public static final int RESULT_FAILED_PROCESS = 150;
    public static final int RESULT_FAILED_SELECT_DEVICE = 151;
    public static final int RESULT_SUCCESS = 152;
    public static final int RESULT_OPTION_REQUEST = 153;

    //TAG
    private static final String TAG = "SpeechProcessManager";

    //Variable
    private String speech;

    //Object
    private SpeechDB speechDB;
    private DeviceDB deviceDB;
    private OptionDB optionDB;
    private Handler mHandler;
    private Context context;

    private int selected_command ;
    private int selected_id;

    // List
    private ArrayList<SpeechData> list = new ArrayList<>();
    private ArrayList<Device> d_list = new ArrayList<>();
    private ArrayList<String> optionName;
    public SpeechProcessManager(Context context, String speech, Handler mHandler) {
        super();
        this.speech = speech;
        this.mHandler = mHandler;
        this.context = context;
        this.speechDB = new SpeechDB(context,mHandler,SpeechDB.SpeechTable);
        optionDB = new OptionDB(context, null, "optionDB");
        ArrayList<OptionData> temp = optionDB.getAllDeviceFromDB();
        Log.d(TAG,"OptionMenu : " + temp.size());
        optionName = new ArrayList<>();
        for(int k=0; k<temp.size() ; k++){
            if(!optionName.contains(temp.get(k).getName())){
                Log.d(TAG,"No contain -> " + temp.get(k).getName());
                optionName.add(temp.get(k).getName());
            }
        }
        Log.d(TAG,"optionString\n" + optionName);


        list = speechDB.getAllDeviceFromDB();
        Log.d(TAG, "SpeechDB size : " + list.size());
        if((selected_command = processRequestCommand(speech)) == -1){
            mHandler.obtainMessage(SPEECH_RESULT,RESULT_FAILED_PROCESS,-1).sendToTarget();
        }
        else{
            if(selected_command == REQUEST_SCAN_DEVICE){
                mHandler.obtainMessage(SPEECH_RESULT,RESULT_SUCCESS,REQUEST_SCAN_DEVICE).sendToTarget();
            }
            else{
                Log.d(TAG,"This command : " + selected_command);
                deviceDB = new DeviceDB(context,mHandler,DeviceDB.DeviceTable);
                d_list = deviceDB.getAllDeviceFromDB();
                if((selected_id = processRequestDevice(speech)) == -1){
                    int optionRequest;
                    if((optionRequest = processOptionRequest(speech) )!= -1){
                        mHandler.obtainMessage(SPEECH_RESULT, RESULT_OPTION_REQUEST, optionRequest).sendToTarget();
                    }
                    else {
                        mHandler.obtainMessage(SPEECH_RESULT, RESULT_FAILED_SELECT_DEVICE, -1).sendToTarget();
                    }
                }
                else{
                    ArrayList<Integer> obj = new ArrayList<>();
                    obj.add(selected_command);
                    obj.add(selected_id);
                    mHandler.obtainMessage(SPEECH_RESULT,RESULT_SUCCESS,-1,obj).sendToTarget();
                }
            }

        }
    }

    private int processRequestCommand(String speech){
        String trial1 = speech.replaceAll(" ", "");
        Log.d(TAG,speech);
        int command = -1;
        speech = speech.replaceAll(" ","");
//        String[] speech_arr = speech.split("");
        Log.d(TAG,"length : " + speech.length());

        double[] similarity = new double[list.size()];
        double max_value = 0;
        int max_index = 0;

        for(int i=0; i<list.size(); i++){
            String compared = list.get(i).getText();
            compared = compared.replaceAll(" ","");
            String[] compared_arr = compared.split("");
            int count = 0;
            for(int j=0; j<compared.length(); j++){
                if(speech.contains(compared_arr[j])){
                    count ++;
                }
            }
            similarity[i] = (double)count / (double)speech.length();
            if(similarity[i] > max_value){
                max_value = similarity[i];
                max_index = i;
            }
        }

        Log.d(TAG,"Evaluated Command Text : " + list.get(max_index).getText() + "\tCommand : " + list.get(max_index).getCommand() + "\tsimilarity : " + (float)similarity[max_index] * 100 + " %");
        if(similarity[max_index] < 0.15){
            return -1;
        }
        else{
            command = list.get(max_index).getCommand();
        }
        return command;
    }

    private int processOptionRequest(String speech){
        String trial1 = speech.replaceAll(" ", "");
        Log.d(TAG,speech);
        int command = -1;
        speech = speech.replaceAll(" ","");
//        String[] speech_arr = speech.split("");
        Log.d(TAG,"length : " + speech.length());

        double[] similarity = new double[list.size()];
        double max_value = 0;
        int max_index = 0;

        for(int i=0; i<optionName.size(); i++){
            String compared = optionName.get(i);
            compared = compared.replaceAll(" ","");
            String[] compared_arr = compared.split("");
            int count = 0;
            for(int j=0; j<compared.length(); j++){
                if(speech.contains(compared_arr[j])){
                    count ++;
                }
            }
            similarity[i] = (double)count / (double)speech.length();
            if(similarity[i] > max_value){
                max_value = similarity[i];
                max_index = i;
            }
        }

        Log.d(TAG,"Evaluated Command Text : " + list.get(max_index).getText() + "\tCommand : " + list.get(max_index).getCommand() + "\tsimilarity : " + (float)similarity[max_index] * 100 + " %");
        if(similarity[max_index] < 0.15){
            return -1;
        }
        else{
            command = max_index;
        }
        return command;
    }

    private int processRequestDevice(String speech) {
        int device_id = -1;
        speech = speech.replaceAll(" ","");
        int count = 0;
        for(int i=0; i<d_list.size(); i++){
            if (speech.contains(d_list.get(i).getDevice_name())) {
                device_id = d_list.get(i).getDeviceId();
                count++;
            }
        }
        if(count != 1){
            device_id = -1;
        }

        return device_id;
    }

    public static String getTTS_String(Device device,int command){
        String tts ="";
        switch(command){
            case REQUEST_LED_ON :
                if(device.getDeviceType() == Command.LED){
                    tts = "장치를 켜곘습니다.";
                }
                else{
                    tts = "장치는 LED가 아닙니다.";
                }

                break ;

            case REQUEST_LED_OFF :
                if(device.getDeviceType() == Command.LED){
                    tts = "장치를 끄겠습니다.";
                }
                else{
                    tts = "장치는 LED가 아닙니다.";
                }
                break ;

            case REQUEST_DOOR_ON :
                if(device.getDeviceType() == Command.DOOR){
                    tts = "장치를 열겠습니다.";
                }
                else{
                    tts = "장치는 문이 아닙니다.";
                }
                break ;

            case REQUEST_DOOR_OFF :
                if(device.getDeviceType() == Command.DOOR){
                    tts = "장치를 닫겠습니다..";
                }
                else{
                    tts = "장치는 문이 아닙니다.";
                }
                break ;

            case REQUEST_WINDOW_ON :
                if(device.getDeviceType() == Command.WINDOW){
                    tts = "장치를 열겠습니다.";
                }
                else{
                    tts = "장치는 창문이 아닙니다.";
                }
                break ;

            case REQUEST_WINDOW_OFF :
                if(device.getDeviceType() == Command.WINDOW){
                    tts = "장치를 닫겠습니다.";
                }
                else{
                    tts = "장치는 창문이 아닙니다.";
                }
                break ;

            case REQUEST_SCAN_DEVICE :
                tts = "장치 검색을 시작하겠습니다. ";
                break ;

            case REQUEST_LED_MORE_BRIGHT :
                if(device.getDeviceType() == Command.LED) {
                    tts = "를 조금 더 밝게 조정하겠습니다 ";
                }
                else{
                    tts = "는 전등이 아닙니다";
                }
                break;

            case REQUEST_LED_LESS_BRIGHT :
                if(device.getDeviceType() == Command.LED) {
                    tts = "를 조금 더 어둡게 조정하겠습니다.";
                }
                else{
                    tts = "는 전등이 아닙니다";
                }
                break;

            case REQUEST_LED_SPECIFIED_BRIGHT :
                if(device.getDeviceType() == Command.LED){
                    tts = "를 요청하신 밝기로 켜겠습니다";
                }
                else{
                    tts = "는 전등이 아닙니다.";
                }
                break;

            case REQUEST_WINDOW_SPECIFIED_DEGREE :
                if(device.getDeviceType() == Command.WINDOW){
                    tts = "를 요청하신 만큼 창문을 조정하겠습니다.";
                }
                else{
                    tts = "는 창문이 아닙니다.";
                }
                break;



        }
         return tts;
    }
}
