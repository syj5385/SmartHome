package comnet.jjun.geniusiotclient;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import comnet.jjun.geniusiotclient.Protocol.Device;
import comnet.jjun.geniusiotclient.Protocol.GeniusProtocol;

/**
 * Created by comm on 2018-08-08.
 */

public class IoTDevice extends Application {

    private static final String TAG = "IoTDevice";
    // BroadCast Action
    public static final String FINISH_GET_DEVICE = "FINISHED_GET_DEVICE";
    public static final String BLUETOOTH_CONNECT ="BLUETOOTH_CONNECT";
    public static final String TCP_RECEIVED_DATA = "TCP_RECEIVED_DATA";
    public static final String BLUETOOTH_RECEIVED_DATA = "BLUETOOTH_RECEIVED_DATA";
    public static final String UPDATE_DEVICE = "UPDATE_DEVICE";
    public static final String FINISHED_REMOVE_DEVICE = "FINISHED_REMOVE_DEVICE";
    public static final String FINISHED_ADD_DEVICE = "FINISHED_ADD_DEVICE";
    public static final String FINISHED_UPDATE_DEVICE ="FINISHED_UPDATE_DEVICE";
    public static final String FAILED_UPDATE_DEVICE ="FAILED_UPDATE_DEVICE";
    public static final String REQUEST_UPDATE_DEVICE ="REQUEST_UPDATE_DEVICE";


    // my IOT Device
    public HashMap<Integer,ArrayList<Device>> allDevice = new HashMap<>();
    public ArrayList<Device> LED= new ArrayList<>();
    public ArrayList<Device> Window= new ArrayList<>();
    public ArrayList<Device> etc = new ArrayList<>();

    public static final int LED_DEVICE = 0;
    public static final int WINDOW_DEVICE = 1;
    public static final int ETC_DEVICE = 2;

    public boolean addDevice(Device device){
        //Duplicate test
        ArrayList<Device> this_dv;
        switch(device.getDeviceType()){
            case GeniusProtocol.LED :
                this_dv = LED;
                break;

            case GeniusProtocol.WINDOW:
                this_dv = Window;
                break;

            case GeniusProtocol.DOOR :
                this_dv = etc;
                break;

            case GeniusProtocol.TEMP:
                this_dv = etc;
                break;

            case GeniusProtocol.GAS :
                this_dv = etc;
                break;

            case GeniusProtocol.BATH :
                this_dv = etc;
                break;

            default :
                return false;
        }
        for(int i=0; i<this_dv.size(); i++) {
            if (device.getDevice_name() == this_dv.get(i).getDevice_name()){
                Log.d(TAG,"This device already exists");
                return false;
            }
        }
        this_dv.add(device);
        return true;
    }

    public void removeAllDevice(){
        while(LED.size() != 0){
            LED.remove(0);
        }

        while(Window.size() != 0){
            Window.remove(0);
        }

        while(etc.size() != 0){
            etc.remove(0);
        }
    }

    public void removeDevice(int id){
        for(int i=0; i<LED.size(); i++){
            if(LED.get(i).getDeviceId() == id){
                LED.remove(i);
                break;
            }
        }

        for(int i=0; i<Window.size(); i++){
            if(Window.get(i).getDeviceId() == id){
                Window.remove(i);
                break;
            }
        }
    }

    public HashMap<Integer, ArrayList<Device>> getAllDevice(){
        for(int i=0; i<allDevice.size(); i++){
            allDevice.remove(0);
        }
        allDevice.put(LED_DEVICE,LED);
        allDevice.put(WINDOW_DEVICE,Window);
        allDevice.put(ETC_DEVICE,etc);
        return allDevice;
    }

    public boolean removeDeviceWithID(int id){
        //Check LED
        for(int i=0; i<LED.size(); i++){
            if(LED.get(i).getDeviceId() == id){
                Log.d(TAG,"Remove Device : " + LED.get(i).getDevice_name());
                LED.get(i).removeDevice();
                LED.remove(i);
                return true;
            }
        }

        //Check Window
        for(int i=0; i<Window.size(); i++){
            if(Window.get(i).getDeviceId() == id){
                Log.d(TAG,"Remove Device : " + Window.get(i).getDevice_name());
                Window.get(i).removeDevice();
                Window.remove(i);
                return true;
            }
        }

        //Check etc
        for(int i=0; i<etc.size(); i++){
            if(etc.get(i).getDeviceId() == id){
                Log.d(TAG,"Remove Device : " + etc.get(i).getDevice_name());
                etc.get(i).removeDevice();
//                Device device_temp = etc.get(i);
//                device_temp.getBle().disconnect();
                etc.remove(i);
//                device_temp = null;
                return true;
            }
        }
        return false;
    }

    public Device getDeviceWithID(int type, int id){
        Log.d(TAG,"This type : " + type + "\tid : " + id);
        switch(type){
            case GeniusProtocol.LED :
                for(int i=0; i<LED.size(); i++){
                    if(LED.get(i).getDeviceId() == id){
                        return LED.get(i);
                    }
                }
                break;

            case GeniusProtocol.WINDOW :
                for(int i=0; i<Window.size(); i++){
                    if(Window.get(i).getDeviceId() == id){
                        return Window.get(i);
                    }
                }
                break;

            default :
                for(int i=0; i<etc.size(); i++){
                    if(etc.get(i).getDeviceId() == id){
                        return etc.get(i);
                    }
                }

                break;

//            case GeniusProtocol.TEMP || GeniusProtocol.GAS :
//                for(int i=0; i<etc.size(); i++){
//                    if(etc.get(i).getDeviceId() == id){
//                        return etc.get(i);
//                    }
//                }
//                break;
//
//            case GeniusProtocol.GAS :
//                for(int i=0; i<etc.size(); i++){
//                    if(etc.get(i).getDeviceId() == id){
//                        return etc.get(i);
//                    }
//                }
//                break;
        }
        return null;
    }

}
