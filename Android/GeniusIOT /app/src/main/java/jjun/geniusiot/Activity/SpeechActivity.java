package jjun.geniusiot.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import jjun.geniusiot.AndroidDB.DeviceDB;
import jjun.geniusiot.Device.Device;
import jjun.geniusiot.Application.IoTDevice;
import jjun.geniusiot.Service.MainService;
import jjun.geniusiot.NetworkService.Command;
import jjun.geniusiot.R;
import jjun.geniusiot.SpeechRecognizer.MySpeech;
import jjun.geniusiot.SpeechRecognizer.OptionDB;
import jjun.geniusiot.SpeechRecognizer.OptionData;
import jjun.geniusiot.SpeechRecognizer.SpeechProcessManager;

public class SpeechActivity extends AppCompatActivity {

    private static final String TAG = "SpeechActivity";
    private MySpeech speech;

    private TextToSpeech tts;

    private DeviceDB deviceDB;
    private OptionDB optionDB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        tts = new TextToSpeech(SpeechActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                tts.setLanguage(Locale.KOREAN);
            }
        });

        deviceDB = new DeviceDB(SpeechActivity.this, DeviceDB.DeviceTable);
        optionDB = new OptionDB(SpeechActivity.this,mHandler,
                "optionDB");

        speech = new MySpeech(this,this);
        speech.startRecognizer();

    }


    private String listened;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(IoTDevice.ERROR_RECORD.equals(action)){
                Log.d(TAG,"Error while recording your voice");
                finish();
                return;
            }
            else if(IoTDevice.RESULT_RECORD.equals(action)){
                Log.d(TAG,"Receive Result Record");
                listened = intent.getStringArrayListExtra(IoTDevice.RESULT_RECORD).get(0);
                Log.d(TAG,"listened : " + listened);
                SpeechProcessManager speechProcessManager = new SpeechProcessManager(SpeechActivity.this,listened , mHandler);

            }
            else if(IoTDevice.END_RECORD.equals(action)){
                Log.d(TAG,"End Of speech");
//                finish();
//                return;
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        iotService.sendTcpData(Command.request_current_Device_to_Server());
        unbindService(IoTConnection);
        speech.exitTTS();
        if(tts != null) {
            tts.shutdown();
            tts = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFilter();

        Intent intent = new Intent(SpeechActivity.this,MainService.class);
        bindService(intent,IoTConnection,BIND_AUTO_CREATE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private void setFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(IoTDevice.RESULT_RECORD);
        filter.addAction(IoTDevice.END_RECORD);
        filter.addAction(IoTDevice.ERROR_RECORD);
        registerReceiver(mReceiver,filter);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case SpeechProcessManager.SPEECH_RESULT :
                    int result = msg.arg1;
                    switch(result){
                        case  SpeechProcessManager.RESULT_FAILED_PROCESS :
                            tts.speak("무슨 말씀이신지 이해할 수가 없네요..", TextToSpeech.QUEUE_FLUSH,null);;
//                            speechDialog.cancel();
                            break;

                        case SpeechProcessManager.RESULT_FAILED_SELECT_DEVICE :
                            tts.speak("어떤 장치를 동작 하실지 잘 모르겠네요.",TextToSpeech.QUEUE_FLUSH,null);
//                            speechDialog.cancel();
                            break;

                        case SpeechProcessManager.RESULT_OPTION_REQUEST:

                            int index = msg.arg2;
                            executeOption(index);
                            Log.d(TAG,"option number = "+ index);

                            break;

                        case SpeechProcessManager.RESULT_SUCCESS :
                            int arg2 = msg.arg2;
                            String ttsText = "";

                            if(arg2 == -1) {
                                ArrayList<Integer> obj = (ArrayList<Integer>) msg.obj;
                                int selected_command = obj.get(0);
                                int selected_id = obj.get(1);
                                ttsText += deviceDB.getDeviceUsingID(selected_id).getDevice_name() ;
                                ttsText += SpeechProcessManager.getTTS_String(deviceDB.getDeviceUsingID(selected_id),selected_command);
                                Log.d(TAG,"ttsText " +  ttsText);
                                tts.speak(ttsText, TextToSpeech.QUEUE_FLUSH, null);
                                executeSpeechRequest(selected_id,selected_command,listened);

                            }
                            else if(arg2 == SpeechProcessManager.REQUEST_SCAN_DEVICE){
////                                mDrawer.closeDrawer(GravityCompat.START);
//                                ttsText += "새로운 장치를 검색합니다.";
//                                tts.speak(ttsText,TextToSpeech.QUEUE_FLUSH,null);
//                                mydevice.setAdapter(null);
//                                isRequestedScan = true;
//                                new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        DiscoverBLEDevice();
//                                    }
//                                }).start();
//
//                                CustomAdapter3 adapter = new CustomAdapter3(IOT_Activity.this);
//                                adapter.addItem(new Custom3_Item(getResources().getDrawable(R.drawable.device_image),"Scanning...",""));
//                                mydevice.setAdapter(adapter);
                            }

//                            speechDialog.dismiss();
                            break;
                    }



                    break;
            }

            while(tts.isSpeaking()){
                try{
                    Thread.sleep(1);
                }catch (InterruptedException e){

                }
            }

            try{
                Thread.sleep(500);
            }catch (InterruptedException e){

            }
            finish();
        }
    };



    private void executeOption(final int index){
        if(optionDB == null){
            optionDB = new OptionDB(SpeechActivity.this, mHandler,"optionDB");
        }
        ArrayList<OptionData> temp = optionDB.getAllDeviceFromDB();
        Log.d(TAG,"OptionMenu : " + temp.size());
        final ArrayList<String> optionName = new ArrayList<>();
        final HashMap<Integer , ArrayList<OptionData>> eachOption = new HashMap<>();
        for(int k=0; k<temp.size() ; k++){
            if(!optionName.contains(temp.get(k).getName())){
                Log.d(TAG,"No contain -> " + temp.get(k).getName());
                optionName.add(temp.get(k).getName());
                ArrayList<OptionData> option = new ArrayList<>();
                option.add(new OptionData(temp.get(k).getName(),temp.get(k).getId(),temp.get(k).getV1(),temp.get(k).getV2(),temp.get(k).getColor()));
                eachOption.put(optionName.indexOf(temp.get(k).getName()),option);
            }
            else{
                Log.d(TAG,"contained");
                int key = optionName.indexOf(temp.get(k).getName());
                eachOption.get(key).add(new OptionData(temp.get(k).getName(),temp.get(k).getId(),temp.get(k).getV1(),temp.get(k).getV2(),temp.get(k).getColor()));
            }
        }
        Log.d(TAG,"optionString\n" + optionName);
        Log.d(TAG,"optionHash\n" + eachOption);
        tts.speak(optionName.get(index) + " 옵션을 수행합니다.",TextToSpeech.QUEUE_FLUSH,null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<OptionData> selected = eachOption.get(index);
                for(int j=0; j<selected.size(); j++){
                    Device tempDevice = deviceDB.getDeviceUsingID(selected.get(j).getId());
                    int type = tempDevice.getDeviceType();
                    if(type == Command.LED || type == Command.WINDOW) {
                        int[] value = new int[2];
                        value[0] = selected.get(j).getV1();
                        value[1] = selected.get(j).getV2();
                        tempDevice.setValue(value);
//                    sendUDP(command.request_execute_toServer(tempDevice));
                        iotService.sendTcpData(Command.request_execute_toServer(tempDevice));
                    }
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                }
//                        isBusy = false;
            }
        }).start();

        while(tts.isSpeaking()){
            try{
                Thread.sleep(1);
            }catch (InterruptedException e){

            }
        }
        try{
            Thread.sleep(500);
        }catch (InterruptedException e){

        }
        finish();
    }

    private void executeSpeechRequest(int selected_id, int selected_command,String sttText){
        Device device = deviceDB.getDeviceUsingID(selected_id);
        switch(selected_command){
            case SpeechProcessManager.REQUEST_LED_ON :
                if(device.getDeviceType() == Command.LED){
                    int[] value = {200,device.getvalue()[1]};
                    device.setValue(value);
//                    sendUDP(command.request_execute_toServer(device));
                    iotService.sendTcpData(Command.request_execute_toServer(device));
                }

                break ;

            case SpeechProcessManager.REQUEST_LED_OFF :
                if(device.getDeviceType() == Command.LED){
                    int[] value = {100,device.getvalue()[1]};
                    device.setValue(value);
//                    sendUDP(command.request_execute_toServer(device));
                    iotService.sendTcpData(Command.request_execute_toServer(device));
                }
                break ;

            case SpeechProcessManager.REQUEST_DOOR_ON :
//                if(device.getDeviceType() == Command.DOOR){
//                    tts = "장치를 열겠습니다.";
//                }
//                else{
//                    tts = "장치는 문이 아닙니다.";
//                }
                break ;

            case SpeechProcessManager.REQUEST_DOOR_OFF :
//                if(device.getDeviceType() == Command.DOOR){
//                    tts = "장치를 닫겠습니다..";
//                }
//                else{
//                    tts = "장치는 문이 아닙니다.";
//                }
                break ;

            case SpeechProcessManager.REQUEST_WINDOW_ON :
                if(device.getDeviceType() == Command.WINDOW){
                    int[] value = {200,device.getvalue()[1]};
                    device.setValue(value);
//                    sendUDP(command.request_execute_toServer(device));
                    iotService.sendTcpData(Command.request_execute_toServer(device));
                }
                break ;

            case SpeechProcessManager.REQUEST_WINDOW_OFF :
                if(device.getDeviceType() == Command.WINDOW){
                    int[] value = {100,device.getvalue()[1]};
                    device.setValue(value);
//                    sendUDP(command.request_execute_toServer(device));
                    iotService.sendTcpData(Command.request_execute_toServer(device));
                }
                break ;

            case SpeechProcessManager.REQUEST_LED_MORE_BRIGHT:
                if(device.getDeviceType() == Command.LED){
                    if(device.getvalue()[0] == 200){
                        int value_temp = device.getvalue()[1] + 20;
                        if(value_temp > 200)
                            value_temp = 200;

                        int[] value = {200, value_temp};
                        device.setValue(value);
//                        sendUDP(command.request_execute_toServer(device));
                        iotService.sendTcpData(Command.request_execute_toServer(device));
                    }
                }
                break;

            case SpeechProcessManager.REQUEST_LED_LESS_BRIGHT:
                if(device.getDeviceType() == Command.LED){
                    if(device.getvalue()[0] == 200){
                        int value_temp = device.getvalue()[1] - 20;
                        if(value_temp < 100)
                            value_temp = 100;

                        int[] value = {200, value_temp};
                        device.setValue(value);
//                        sendUDP(command.request_execute_toServer(device));
                        iotService.sendTcpData(Command.request_execute_toServer(device));
                    }
                }
                break;

            case SpeechProcessManager.REQUEST_WINDOW_MORE_OPEN:
                if(device.getDeviceType() == Command.WINDOW){
                    if(device.getvalue()[0] == 200){
                        int value_temp = device.getvalue()[1] + 20;
                        if(value_temp > 200)
                            value_temp = 200;

                        int[] value = {200, value_temp};
                        device.setValue(value);
//                        sendUDP(command.request_execute_toServer(device));
                        iotService.sendTcpData(Command.request_execute_toServer(device));
                    }
                }
                break;

            case SpeechProcessManager.REQUEST_WINDOW_MORE_CLOSE:
                if(device.getDeviceType() == Command.WINDOW){
                    if(device.getvalue()[0] == 200){
                        int value_temp = device.getvalue()[1] - 20;
                        if(value_temp < 100)
                            value_temp = 100;

                        int[] value = {200, value_temp};
                        device.setValue(value);
//                        sendUDP(command.request_execute_toServer(device));
                        iotService.sendTcpData(Command.request_execute_toServer(device));
                    }
                }
                break;

            case SpeechProcessManager.REQUEST_LED_SPECIFIED_BRIGHT:
                if(device.getDeviceType() == Command.LED) {
                    int[] value = new int[2];
                    value[0] = 200;
                    if (sttText.contains("1단계")) {
                        value[1] = 120;
                    } else if (sttText.contains("2단계")) {
                        value[1] = 140;
                    } else if (sttText.contains("3단계")) {
                        value[1] = 160;
                    } else if (sttText.contains("4단계")) {
                        value[1] = 180;
                    } else if (sttText.contains("5단계")) {
                        value[1] = 200;
                    } else {
                        value[0] = device.getvalue()[0];
                        value[1] = device.getvalue()[1];
                    }
                    device.setValue(value);
//                    sendUDP(command.request_execute_toServer(device));
                    iotService.sendTcpData(Command.request_execute_toServer(device));
                }
                break;

            case SpeechProcessManager.REQUEST_WINDOW_SPECIFIED_DEGREE:
                if(device.getDeviceType() == Command.WINDOW) {
                    int[] value = new int[2];
                    value[0] = 200;
                    if (sttText.contains("1단계")) {
                        value[1] = 120;
                    } else if (sttText.contains("2단계")) {
                        value[1] = 140;
                    } else if (sttText.contains("3단계")) {
                        value[1] = 160;
                    } else if (sttText.contains("4단계")) {
                        value[1] = 180;
                    } else if (sttText.contains("5단계")) {
                        value[1] = 200;
                    } else {
                        value[0] = device.getvalue()[0];
                        value[1] = device.getvalue()[1];
                    }
                    device.setValue(value);
//                    sendUDP(command.request_execute_toServer(device));
                    iotService.sendTcpData(Command.request_execute_toServer(device));
                }
                break;



//            case SpeechProcessManager.REQUEST_SCAN_DEVICE :
//                tts = "장치 검색을 시작하겠습니다. ";
//                break ;
        }

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade,R.anim.appear);
    }


    // Service
    //Service Connection
    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {

        Intent startService = new Intent(this, service);
        if (extras != null && !extras.isEmpty()) {
            Set<String> keys = extras.keySet();
            for (String key : keys) {
                String extra = extras.getString(key);
                startService.putExtra(key, extra);
            }
        }

        Intent bindingIntent = new Intent(this,service);
//        startService(startService);
        bindService(bindingIntent,serviceConnection, Context.BIND_AUTO_CREATE);
    }


    private MainService iotService;
    private final ServiceConnection IoTConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            iotService = ((MainService.MainBinder) arg1).getService();
            Log.d(TAG,"Service : " + String.valueOf(iotService));

            iotService.setFilter();
            sendBroadcast(new Intent(IoTDevice.IOT_ACTIVITY_START));
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            iotService = null;
            Log.e(TAG,"Service Disconnected");
        }
    };

}
