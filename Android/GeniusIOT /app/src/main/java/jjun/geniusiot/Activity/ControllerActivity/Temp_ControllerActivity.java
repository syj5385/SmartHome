package jjun.geniusiot.Activity.ControllerActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import jjun.geniusiot.R;


public class Temp_ControllerActivity extends ControllerActivity {

    private TextView temperature, humidity;
    private Button[] working = new Button[3];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initializeView() {
        super.initializeView();
        chatScroll.removeAllViews();
        controllerLayout.removeAllViews();
        TextView device_n = findViewById(R.id.machine);
        device_n.setText("가습기");
        device_image.setImageDrawable(getResources().getDrawable(R.drawable.temp_small));
        delete.setVisibility(View.INVISIBLE);

        LinearLayout temp_layout = (LinearLayout)View.inflate(this,R.layout.templayout,null);
        temp_layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        chatScroll.addView(temp_layout);

        temperature = temp_layout.findViewById(R.id.temp_value);
        humidity = temp_layout.findViewById(R.id.hum_value);
        temperature.setText(String.valueOf(device.getvalue()[0]));
        humidity.setText(String.valueOf(device.getvalue()[1]));

        LinearLayout temp_controller = (LinearLayout)View.inflate(this,R.layout.humidcontroller,null);
        temp_controller.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        controllerLayout.addView(temp_controller);

        int humidity = device.getvalue()[1];
        working[0] = temp_controller.findViewById(R.id.b1);
        working[1] = temp_controller.findViewById(R.id.b2);
        working[2] = temp_controller.findViewById(R.id.b3);

        for(int i=0; i<3; i++)
            working[i].setBackgroundColor(Color.rgb(0xE1, 0xF5, 0xEA));
        if(humidity >= 90){
            working[0].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
        }
        else if(humidity >= 50 && humidity < 90){
            working[1].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
        }
        else{
            working[2].setBackgroundColor(Color.rgb(0xFF, 0xC1, 0x9E));
        }
    }
}
