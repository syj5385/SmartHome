package jjun.geniusiot.Activity.ControllerActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import jjun.geniusiot.AndroidDB.ChatbotDB;
import jjun.geniusiot.NetworkService.Command;
import jjun.geniusiot.R;

/**
 * Created by jjun on 2018. 7. 9..
 */

public class Lamp_ControllerActivity extends ControllerActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private ImageView onoff ;
    private Button[] brightBtn ;
    private boolean isOnOff = false;
    private int bright = 0;

    @Override
    protected void initializeView() {
        super.initializeView();
        LinearLayout led_controller = (LinearLayout) View.inflate(this, R.layout.ledcontroller,null);
        onoff = led_controller.findViewById(R.id.bulb);
        if(device.getvalue()[0] == 100){
            onoff.setImageDrawable(getResources().getDrawable(R.drawable.light_off));

        }
        else if(device.getvalue()[0] == 200){
            onoff.setImageDrawable(getResources().getDrawable(R.drawable.light_on));
        }

        onoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(device.getvalue()[0] == 100){
                    int[] valueTemp = {200,bright};
                    device.setValue(valueTemp);
                    onoff.setImageDrawable(getResources().getDrawable(R.drawable.light_on));
                    insertChat(ChatbotDB.USER,device.getDevice_name() + "을(를) 켜 줘.",device.getDeviceId());
                    setBright(bright);
                }
                else if(device.getvalue()[0] == 200){
                    int[] valueTemp = {100,bright};
                    device.setValue(valueTemp);
                    onoff.setImageDrawable(getResources().getDrawable(R.drawable.light_off));
                    if(brightBtn != null) {
                        for (int i = 0; i < brightBtn.length; i++)
                            brightBtn[i].setBackgroundColor(Color.rgb(0xE1, 0xF5, 0xEA));
                    }
                    insertChat(ChatbotDB.USER,device.getDevice_name() + "을(를) 꺼 줘.",device.getDeviceId());

                }
                sendTCP(Command.request_execute_toServer(device));
//                chatScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        bright = device.getvalue()[1];
        brightBtn = new Button[5];
        brightBtn[0] = led_controller.findViewById(R.id.b1);
        brightBtn[1] = led_controller.findViewById(R.id.b2);
        brightBtn[2] = led_controller.findViewById(R.id.b3);
        brightBtn[3] = led_controller.findViewById(R.id.b4);
        brightBtn[4] = led_controller.findViewById(R.id.b5);
        for(int i=0; i<brightBtn.length ; i++)
            brightBtn[i].setOnClickListener(levelListener);
        if(device.getvalue()[0] == 200){
            setBright(bright);

        }
        controllerLayout.addView(led_controller,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private int level_Text = 0;
    private View.OnClickListener levelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(device.getvalue()[0] == 200) {
                for (int i = 0; i < brightBtn.length; i++)
                    brightBtn[i].setBackgroundColor(Color.rgb(0xE1, 0xF5, 0xEA));
                switch (view.getId()) {
                    case R.id.b1:
                        brightBtn[0].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                        bright = 120;
                        level_Text = 1;
                        break;
                    case R.id.b2:
                        brightBtn[1].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                        bright = 140;
                        level_Text = 2;
                        break;
                    case R.id.b3:
                        brightBtn[2].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                        bright = 160;
                        level_Text = 3;
                        break;
                    case R.id.b4:
                        brightBtn[3].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                        bright = 180;
                        level_Text = 4;
                        break;
                    case R.id.b5:
                        brightBtn[4].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                        bright = 200;
                        level_Text = 5;
                        break;
                }
                int[] valueTemp = {200, bright};
                device.setValue(valueTemp);
                insertChat(ChatbotDB.USER,device.getDevice_name() + "의 밝기를 " + String.valueOf(level_Text) + "단계로 조정해줘.",device.getDeviceId());
//                chatScroll.fullScroll(ScrollView.FOCUS_DOWN);
                sendTCP(Command.request_execute_toServer(device));
            }
            else{
                insertChat(ChatbotDB.SERVER,device.getDevice_name() + "이(가) 꺼져있습니다.",device.getDeviceId());
//                chatScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }
    };

    private void setBright(int bright){
        switch(bright){
            case 120:
                brightBtn[0].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                break;
            case 140 :
                brightBtn[1].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                break;
            case 160 :
                brightBtn[2].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                break;
            case 180 :
                brightBtn[3].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                break;
            case 200 :
                brightBtn[4].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
                break;
        }
    }
}
