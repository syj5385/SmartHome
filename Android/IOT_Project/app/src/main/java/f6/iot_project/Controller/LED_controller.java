package f6.iot_project.Controller;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import f6.iot_project.Database.DeviceDB;
import f6.iot_project.IoT_Device.Command;
import f6.iot_project.IoT_Device.Device;
import f6.iot_project.R;
import f6.iot_project.Network.UDP_Connection;

import static android.content.ContentValues.TAG;

/**
 * Created by comm on 2018-04-25.
 */

public class LED_controller extends Controller {

    protected LinearLayout ledController ;
    protected LinearLayout root;
    protected Button[] b = new Button[5];


    protected ImageView bulb;
    protected int bright_level;

    protected int bright;



    public LED_controller(Activity activity, Context context, Device device, Handler mHandler, UDP_Connection udp, Command command) {
        super(activity, context, device, mHandler, udp, command);
        initializeController(device);
        implementationController();
    }

    @Override
    protected void initializeController(Device device) {
        super.initializeController(device);

        bright = device.getvalue()[1];
        bright_level = getBright_level(bright);

        root = (LinearLayout)temp.findViewById(R.id.controller);
        ledController = (LinearLayout) View.inflate(context, R.layout.led_cont_layout,null);
        root.addView(ledController);
        bulb = (ImageView)ledController.findViewById(R.id.bulb);

        b[0] = (Button)ledController.findViewById(R.id.b1);
        b[1] = (Button)ledController.findViewById(R.id.b2);
        b[2] = (Button)ledController.findViewById(R.id.b3);
        b[3] = (Button)ledController.findViewById(R.id.b4);
        b[4] = (Button)ledController.findViewById(R.id.b5);


        Log.d(TAG,"switch : " + device.getvalue()[0]);
        if(device.getvalue()[0] == 100){
            bulb.setImageDrawable(context.getResources().getDrawable(R.drawable.light_off));
        }
        else if(device.getvalue()[0] == 200){
            bulb.setImageDrawable(context.getResources().getDrawable(R.drawable.light_on));
            bright_level = getBright_level(bright);
//            b[bright_level-1].setBackgroundColor(context.getResources().getColor(R.color.selectedColor));
            updateBrightButton(true, bright_level);
        }
    }

    @Override
    protected void implementationController() {
        super.implementationController();

        bulb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundManager.play(0);
                // bulb Switch
                if(device.getvalue()[0] == 100){
                    int[] value = {200, device.getvalue()[1]};
                    device.setValue(value);
                    bulb.setImageDrawable(context.getResources().getDrawable(R.drawable.light_on));
                    updateBrightButton(true, bright_level);

                }
                else if(device.getvalue()[0] == 200){
                    int[] value = {100, device.getvalue()[1]};
                    device.setValue(value);
                    bulb.setImageDrawable(context.getResources().getDrawable(R.drawable.light_off));
                    updateBrightButton(false, bright_level);
                }
                sendUDP(command.request_execute_toServer(device));
            }
        });

        for(int i=0; i<b.length ;i++){
            b[i].setOnClickListener(mbClickListener);
        }
    }

    protected View.OnClickListener mbClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            soundManager.play(0);
            if(device.getvalue()[0] == 200) {
                switch (view.getId()) {
                    case R.id.b1:
                        bright_level = 1;
                        bright = 120;
                        break;

                    case R.id.b2:
                        bright_level = 2;
                        bright = 140;
                        break;

                    case R.id.b3:
                        bright_level = 3;
                        bright = 160;
                        break;

                    case R.id.b4:
                        bright_level = 4;
                        bright = 180;
                        break;

                    case R.id.b5:
                        bright_level = 5;
                        bright = 200;
                        break;
                }
                int[] value = {device.getvalue()[0], bright};
                device.setValue(value);
                sendUDP(command.request_execute_toServer(device));
                updateBrightButton(true, bright_level);
            }
            else{
                Toast.makeText(context,"전등이 꺼져 있습니다.",Toast.LENGTH_SHORT).show();
            }
        }
    };

    protected void updateBrightButton(boolean onoff,int bright_level){

        for(int i=0; i<b.length ;i++){
            b[i].setBackgroundColor(context.getResources().getColor(R.color.nonselectedColor));
        }
        if(onoff)
            b[bright_level-1].setBackgroundColor(context.getResources().getColor(R.color.selectedColor));
    }

    public static int getBright_level(int bright){
        int temp =0 ;
        if(bright >= 100 && bright <= 120){
            temp = 1;
        }
        else if(bright == 140){
            temp = 2;
        }
        else if(bright == 160){
            temp = 3;
        }
        else if(bright == 180){
            temp = 4;
        }
        else if(bright == 200){
            temp = 5;
        }
        return temp;
    }
}
