package jjun.geniusiot.Application;

import android.app.Application;

import java.util.ArrayList;

import jjun.geniusiot.Device.Device;

/**
 * Created by comm on 2018-08-10.
 */

public class IoTDevice extends Application {

    //MACRO
    public static final String FINISH_GET_DEVICE = "FINISHED_GET_DEVICE";
    public static final String TCP_RECEIVED_DATA = "TCP_RECEIVED_DATA";
    public static final String TCP_FINISHED_CONNECT = "TCP_FINISHED_CONNECT";
    public static final String BLUETOOTH_RECEIVED_DATA = "BLUETOOTH_RECEIVED_DATA";
    public static final String UPDATE_DEVICE = "UPDATE_DEVICE";
    public static final String FINISHED_REMOVE_DEVICE = "FINISHED_REMOVE_DEVICE";
    public static final String FINISHED_ADD_DEVICE = "FINISHED_ADD_DEVICE";
    public static final String EXIT_APPLICATION = "EXIT_APPLICATION";
    public static final String FINISHED_UPDATE_DEVICE = "FINISHED_UPDATE_DEVICE";
    public static final String IOEXCEPTION = "IOEXCEPTION";
    public static final String UPDATE_DEVICE_OK="UPDATE_DEVICE_OK";
    public static final String SHAKING_OCCURED = "SHAKIING_OCCURED";
    public static final String IOT_ACTIVITY_START = "IOT_ACTIVITY_START";
    public static final String FCM_RECEIVED = "FCM_RECEIVED";

    public static final String START_RECORD = "jjun.geniusiot.START_RECORD";
    public static final String END_RECORD = "jjun.geniusiot.END_RECORD";
    public static final String RESULT_RECORD = "jjun.geniusiot.RESULT_RECORD";
    public static final String ERROR_RECORD = "jjun.geniusiot.ERROR_RECORD";

    public static final String FINGER_ERROR = "jjun.geniusiot.FINGER_ERROR";
    public static final String FINGER_SUCCESS = "jjun.geniusiot.FINGER_SUCCESS";
    public static final String FINGER_FAILED = "jjun.geniusiot.FINGER.FAILED";

    private ArrayList<Device> myDevice = new ArrayList<>();

    public ArrayList<Device> getMyDevice(){
        return myDevice;
    }

    public void setMyDevice(ArrayList<Device> myDevice){
        this.myDevice = myDevice;
    }


}
