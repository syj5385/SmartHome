package f6.iot_project.Controller;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import f6.iot_project.IoT_Device.Command;
import f6.iot_project.IoT_Device.Device;
import f6.iot_project.Network.UDP_Connection;

/**
 * Created by comm on 2018-05-10.
 */

public class Door_controller extends Controller {

    public Door_controller(Activity activity, Context context, Device device, Handler mHandler, UDP_Connection udp, Command command) {
        super(activity, context, device, mHandler, udp, command);
    }
}
