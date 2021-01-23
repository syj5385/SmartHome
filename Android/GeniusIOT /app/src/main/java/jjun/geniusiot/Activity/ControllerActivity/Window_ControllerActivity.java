package jjun.geniusiot.Activity.ControllerActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jjun.geniusiot.AndroidDB.ChatbotDB;
import jjun.geniusiot.NetworkService.Command;
import jjun.geniusiot.R;

/**
 * Created by jjun on 2018. 7. 9..
 */

public class Window_ControllerActivity extends ControllerActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private ImageView onoff , device_image;
    private Button[] angle_Btn ;
    private boolean isOnOff = false;
    private int angle = 0;

    @Override
    protected void initializeView() {
        super.initializeView();
        LinearLayout led_controller = (LinearLayout) View.inflate(this, R.layout.ledcontroller,null);
        TextView info = led_controller.findViewById(R.id.bright);
        info.setText("창문을 얼마나 열까요?");
        onoff = led_controller.findViewById(R.id.bulb);
        device_image = findViewById(R.id.device_image);
        device_image.setImageDrawable(getResources().getDrawable(R.drawable.window_image_open));
        if(device.getvalue()[0] == 100){
            onoff.setImageDrawable(getResources().getDrawable(R.drawable.window_image_close_big));

        }
        else if(device.getvalue()[0] == 200){
            onoff.setImageDrawable(getResources().getDrawable(R.drawable.window_image_open_big));
        }

        onoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(device.getvalue()[0] == 100){
                    int[] valueTemp = {200,angle};
                    device.setValue(valueTemp);
                    onoff.setImageDrawable(getResources().getDrawable(R.drawable.window_image_open_big));
                    insertChat(ChatbotDB.USER,device.getDevice_name() + "을(를) 열어줘.",device.getDeviceId());
                    setBright(angle);
                }
                else if(device.getvalue()[0] == 200){
                    int[] valueTemp = {100,angle};
                    device.setValue(valueTemp);
                    onoff.setImageDrawable(getResources().getDrawable(R.drawable.window_image_close_big));
                    if(angle_Btn != null) {
                        for (int i = 0; i < angle_Btn.length; i++)
                            angle_Btn[i].setBackgroundColor(Color.rgb(0xE1, 0xF5, 0xEA));
                    }
                    insertChat(ChatbotDB.USER,device.getDevice_name() + "을(를) 닫아줘.",device.getDeviceId());

                }
                sendTCP(Command.request_execute_toServer(device));
//                chatScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        angle = device.getvalue()[1];
        angle_Btn = new Button[5];
        angle_Btn[0] = led_controller.findViewById(R.id.b1);
        angle_Btn[1] = led_controller.findViewById(R.id.b2);
        angle_Btn[2] = led_controller.findViewById(R.id.b3);
        angle_Btn[3] = led_controller.findViewById(R.id.b4);
        angle_Btn[4] = led_controller.findViewById(R.id.b5);
        for(int i=0; i<angle_Btn.length ; i++)
            angle_Btn[i].setOnClickListener(levelListener);
        if(device.getvalue()[0] == 200){
            setBright(angle);

        }
        controllerLayout.addView(led_controller,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private int level_Text = 0;
    private View.OnClickListener levelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(device.getvalue()[0] == 200) {
                for (int i = 0; i < angle_Btn.length; i++)
                    angle_Btn[i].setBackgroundColor(Color.rgb(0xE1, 0xF5, 0xEA));
                switch (view.getId()) {
                    case R.id.b1:
                        angle_Btn[0].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                        angle = 120;
                        level_Text = 1;
                        break;
                    case R.id.b2:
                        angle_Btn[1].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                        angle = 140;
                        level_Text = 2;
                        break;
                    case R.id.b3:
                        angle_Btn[2].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                        angle = 160;
                        level_Text = 3;
                        break;
                    case R.id.b4:
                        angle_Btn[3].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                        angle = 180;
                        level_Text = 4;
                        break;
                    case R.id.b5:
                        angle_Btn[4].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                        angle = 200;
                        level_Text = 5;
                        break;
                }
                int[] valueTemp = {200, angle};
                device.setValue(valueTemp);
                insertChat(ChatbotDB.USER,device.getDevice_name() + "(을)를 " + String.valueOf(level_Text) + "단계로 열어줘.",device.getDeviceId());
//                chatScroll.fullScroll(ScrollView.FOCUS_DOWN);
                sendTCP(Command.request_execute_toServer(device));
            }
            else{
                insertChat(ChatbotDB.SERVER,device.getDevice_name() + "이(가) 닫혀 있습니다.",device.getDeviceId());
//                chatScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }
    };

    private void setBright(int bright){
        switch(bright){
            case 120:
                angle_Btn[0].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                break;
            case 140 :
                angle_Btn[1].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                break;
            case 160 :
                angle_Btn[2].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                break;
            case 180 :
                angle_Btn[3].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                break;
            case 200 :
                angle_Btn[4].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                break;
        }
    }
}
