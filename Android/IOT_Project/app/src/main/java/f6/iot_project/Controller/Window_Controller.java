package f6.iot_project.Controller;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import f6.iot_project.IoT_Device.Command;
import f6.iot_project.IoT_Device.Device;
import f6.iot_project.Network.UDP_Connection;
import f6.iot_project.R;

import static android.content.ContentValues.TAG;

/**
 * Created by jjun on 2018. 6. 29..
 */

public class Window_Controller extends LED_controller {

    private TextView text;
    public Window_Controller(Activity activity, Context context, Device device, Handler mHandler, UDP_Connection udp, Command command) {
        super(activity, context, device, mHandler, udp, command);
    }

    @Override
    protected void initializeController(Device device) {
        bright = device.getvalue()[1];
        bright_level = getBright_level(bright);

        root = (LinearLayout)temp.findViewById(R.id.controller);
        ledController = (LinearLayout) View.inflate(context, R.layout.led_cont_layout,null);
        root.addView(ledController);
        bulb = (ImageView)ledController.findViewById(R.id.bulb);
        text = ledController.findViewById(R.id.bright);
        text.setText("문을 얼마나 열까요?");
        b[0] = (Button)ledController.findViewById(R.id.b1);
        b[1] = (Button)ledController.findViewById(R.id.b2);
        b[2] = (Button)ledController.findViewById(R.id.b3);
        b[3] = (Button)ledController.findViewById(R.id.b4);
        b[4] = (Button)ledController.findViewById(R.id.b5);


        Log.d(TAG,"switch : " + device.getvalue()[0]);
        if(device.getvalue()[0] == 100){
            bulb.setImageDrawable(context.getResources().getDrawable(R.drawable.window_image_close));
        }
        else if(device.getvalue()[0] == 200){
            bulb.setImageDrawable(context.getResources().getDrawable(R.drawable.window_image_open));
            bright_level = getBright_level(bright);
            b[bright_level-1].setBackgroundColor(context.getResources().getColor(R.color.selectedColor));
        }    }

    @Override
    protected void implementationController() {
        bulb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundManager.play(0);
                // bulb Switch
                if(device.getvalue()[0] == 100){
                    int[] value = {200, device.getvalue()[1]};
                    device.setValue(value);
                    bulb.setImageDrawable(context.getResources().getDrawable(R.drawable.window_image_open));
                    updateBrightButton(true, bright_level);

                }
                else if(device.getvalue()[0] == 200){
                    int[] value = {100, device.getvalue()[1]};
                    device.setValue(value);
                    bulb.setImageDrawable(context.getResources().getDrawable(R.drawable.window_image_close));
                    updateBrightButton(false, bright_level);
                }
                sendUDP(command.request_execute_toServer(device));
            }
        });

        for(int i=0; i<b.length ;i++){
            b[i].setOnClickListener(mbClickListener);
        }
    }
}
