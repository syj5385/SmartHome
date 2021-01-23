package jjun.geniusiot.Activity.ControllerActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import jjun.geniusiot.AndroidDB.ChatbotDB;
import jjun.geniusiot.Application.IoTDevice;
import jjun.geniusiot.Device.Device;
import jjun.geniusiot.NetworkService.Command;
import jjun.geniusiot.R;

public class Bath_ControllerActivity extends ControllerActivity {

    private ImageView bath;
    private Button[] water = new Button[3];
    private Button[] temp = new Button[5];
    private TextView info_top, info_bottom;


    private int water_level = -1;
    private int water_temp = -1;

    private BathThread thread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;


    }

    private HashMap<Integer,Integer> bath_value;
    @Override
    protected void initializeView() {
        super.initializeView();

        controllerLayout.removeAllViews();
        LinearLayout bath_controller = (LinearLayout) View.inflate(this, R.layout.bathcontroller,null);
        bath = bath_controller.findViewById(R.id.bath);
        water[0] = bath_controller.findViewById(R.id.b1);
        water[1] = bath_controller.findViewById(R.id.b2);
        water[2] = bath_controller.findViewById(R.id.b3);

        temp[0] = bath_controller.findViewById(R.id.t1);
        temp[1] = bath_controller.findViewById(R.id.t2);
        temp[2] = bath_controller.findViewById(R.id.t3);
        temp[3] = bath_controller.findViewById(R.id.t4);
        temp[4] = bath_controller.findViewById(R.id.t5);

        info_top = bath_controller.findViewById(R.id.bright);
        info_bottom = bath_controller.findViewById(R.id.depth);
        int value1 = device.getvalue()[0];
        int value2 = device.getvalue()[1];

        bath_value = Device.getBathInformation(value1,value2);
        Log.d(TAG,"Request exe : " + bath_value.get(Command.BATH_EXECUTION_REQUEST) + "\tresult : " + bath_value.get(Command.BATH_EXECUTION_RESULT));
        if(bath_value.get(Command.BATH_EXECUTION_REQUEST) == 1 ){
                bath.setImageDrawable(getResources().getDrawable(R.drawable.bath_on));
            LinearLayout bottom_layout = bath_controller.findViewById(R.id.bottom_menu);
            bottom_layout.setVisibility(View.GONE);
            info_top.setText("(현재 욕조 물의 양)");
            int target_water = bath_value.get(Command.BATH_WATER_REQUEST);
            for(int i=0; i<water.length; i++){
                water[i].setText(String.valueOf(i));
                if(i<target_water)
                    water[i].setBackgroundColor(getResources().getColor(R.color.statusColor));

            }
            info_bottom.setText("현재 물의 온도 : " + String.valueOf(bath_value.get(Command.BATH_TEMP_RESULT) + 19) + "˚C");
            if(thread == null){
                bath_running = true;
                thread = new BathThread();
                thread.start();
            }

            bath.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Bath_ControllerActivity.this);
                    builder.setTitle("스마트 욕조를 중지하시겠습니까?")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    bath_value.put(Command.BATH_EXECUTION_REQUEST,0
                                      );
                                    bath_value.put(Command.BATH_WATER_REQUEST,water_level);
                                    int target_temp = 5*(water_temp+4);
                                    Log.d(TAG,"temperature : " + target_temp);
                                    bath_value.put(Command.BATH_TEMP_REQUEST,  target_temp - 15);
                                    HashMap<Integer,Integer> temp = Device.getBathInformation(0,device.getvalue()[1]);
                                    bath_value.put(Command.BATH_EXECUTION_RESULT,temp.get(Command.BATH_EXECUTION_RESULT));
                                    bath_value.put(Command.BATH_WATER_RESULT,temp.get(Command.BATH_WATER_RESULT));
                                    bath_value.put(Command.BATH_TEMP_RESULT,temp.get(Command.BATH_TEMP_RESULT));
                                    int[] value = Device.setBathInformation(bath_value);
                                    Log.d(TAG,"value1 : " + Integer.toBinaryString(value[0]) + "\tvalue2 : " + Integer.toBinaryString(value[1]));
                                    device.setValue(value);
                                    insertChat(ChatbotDB.USER,"<스마트 욕조 중지>",device.getDeviceId()) ;
                                    water_level = -1;
                                    water_temp = -1;
                                    initializeView();
                                    sendTCP(Command.request_execute_toServer(device));
                                    if(thread != null) {
                                        thread.stopBath();
                                        thread = null;
                                    }

                                }
                            }).setNegativeButton("아니오",null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }
        else{
            bath.setImageDrawable(getResources().getDrawable(R.drawable.bath_off));
            info_top.setText("물을 얼마나 받을까요?");
            info_bottom.setText("물 온도를 어떻게 할까요?");
            water[0].setText(String.valueOf(50) + " %");water[1].setText(String.valueOf(75) + " %");water[2].setText(String.valueOf(100) + " %");
            temp[0].setText("25˚C");temp[1].setText("30˚C");temp[2].setText("35˚C");temp[3].setText("40˚C");temp[4].setText("45˚C");

            for(int i=0; i<water.length ; i++)
                water[i].setOnClickListener(bathClickListener);
            for(int i=0; i<temp.length; i++)
                temp[i].setOnClickListener(bathClickListener);

            bath.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(water_temp != -1 && water_level != -1){
                        AlertDialog.Builder builder = new AlertDialog.Builder(Bath_ControllerActivity.this);
                        builder.setTitle("스마트 욕조를 시작하시겠습니까?");
                        builder.setMessage("물의 양 : " + water_level+ "단계\n물의 온도 : " + (water_temp+4)*5 + "˚C")
                                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                bath_value.put(Command.BATH_EXECUTION_REQUEST,1);
                                bath_value.put(Command.BATH_WATER_REQUEST,water_level);
                                int target_temp = 5*(water_temp+4);
                                Log.d(TAG,"temperature : " + target_temp);
                                bath_value.put(Command.BATH_TEMP_REQUEST,  target_temp - 15);
                                bath_value.put(Command.BATH_EXECUTION_RESULT,0);
                                bath_value.put(Command.BATH_WATER_RESULT,3);
                                bath_value.put(Command.BATH_TEMP_RESULT,31);
                                int[] value = Device.setBathInformation(bath_value);
                                Log.d(TAG,"value1 : " + Integer.toBinaryString(value[0]) + "\tvalue2 : " + Integer.toBinaryString(value[1]));
                                device.setValue(value);
                                insertChat(ChatbotDB.USER,"<스마트 욕조 실행>\n온도 : "+target_temp + "˚C\n물 높이 : " + water_level + "단계" ,device.getDeviceId()) ;

                                initializeView();
                                sendTCP(Command.request_execute_toServer(device));
                                if(thread == null){
                                    bath_running = true;
                                    thread = new BathThread();
                                    thread.start();
                                }
                            }
                        }).setNegativeButton("아니오",null);
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
                    else {
                        Toast.makeText(getApplicationContext(),"물의 양과 물의 온도를 선택하세요",Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }


        controllerLayout.addView(bath_controller,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));

    }

    private View.OnClickListener bathClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            for(int i=0; i<water.length; i++)
                water[i].setBackgroundColor(Color.rgb(0xE1,0xF5,0xEA));
            for(int i=0; i<temp.length; i++)
                temp[i].setBackgroundColor(Color.rgb(0xE1,0xF5,0xEA));
            switch(view.getId()){
                case R.id.b1 :
                    water_level = 1;
                    break;

                case R.id.b2:
                    water_level =2;
                    break;

                case R.id.b3:
                    water_level = 3;
                    break;

                case R.id.t1:
                    water_temp = 1;
                    break;

                case R.id.t2:
                    water_temp = 2;
                    break;

                case R.id.t3:
                    water_temp = 3;
                    break;
                case R.id.t4:
                    water_temp = 4;
                    break;
                case R.id.t5:
                    water_temp = 5;

                    break;
            }
            if(water_level != -1) {
                water[water_level - 1].setBackgroundColor(getResources().getColor(R.color.yellowColor));

            }
            if(water_temp != -1) {
                temp[water_temp - 1].setBackgroundColor(getResources().getColor(R.color.yellowColor));

            }
        }
    };

    private boolean bath_running = false ;
    private class BathThread extends Thread{
        private static final int INTERVAL_BATH = 5000;

        public BathThread() {
            super();
            Log.d(TAG,"start Bath Thread");
        }

        public void stopBath(){
            bath_running = false;
        }
        @Override
        public void run() {
            super.run();
            while(bath_running){
                if(iotService != null)
                    iotService.sendTcpData(Command.request_current_Device_to_Server());
                Log.d(TAG,"Update Bath data\n");
                try{
                    sleep(INTERVAL_BATH);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver,new IntentFilter(IoTDevice.FINISHED_UPDATE_DEVICE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        bath_running = false;
        unregisterReceiver(mReceiver);
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(IoTDevice.FINISHED_UPDATE_DEVICE)){
                Log.d(TAG,"FINISHED_UPDATE_DEVICE");
                initializeView();
            }
        }
    };
}
