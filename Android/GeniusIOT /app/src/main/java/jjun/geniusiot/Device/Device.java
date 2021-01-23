package jjun.geniusiot.Device;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import jjun.geniusiot.NetworkService.Command;
import jjun.geniusiot.NetworkService.GeniusProtocol;

/**
 * Created by comm on 2018-02-04.
 */

public class Device {

    private static final int DEVICE_NAME = 12;
    private static final int DEVICE_ID = 34;
    private static final int DEVICE_TYPE = 49;
    private static final int DEVICE_ADDRESS = 67;
    private static final int DEVICE_VALUE1 = 93;
    private static final int DEVICE_VALUE2 = 104;

    private String Device_name = "";
    private int Device_id = 0;
    private int Device_type = 0;
    private String Device_address = "";
    private int value1 = 0;
    private int value2 = 0;
    private int favor = 0;
    private boolean isControlllist = false;

    public Device(byte[] devicePacket) {
        requestDevice(devicePacket);
    }

    public Device(int id, int type, String name, int value1, int value2, int favor) {
        this.Device_id = id;
        this.Device_type = type;
        this.Device_name = name;
        this.value1 = value1;
        this.value2 = value2;
        this.favor = favor;
        Log.d("Device", "id : " + Device_id + "\tname : " + Device_name + "\ttype : " + Device_type + "\tv1 : " + value1 + "\tv2 : " + value2);
    }

    public static ArrayList<Device> initializeDevice() {
        ArrayList<Device> deviceList = new ArrayList<>();
        return deviceList;
    }

    private void requestDevice(byte[] devicePacket) {

        int pos = 0;
        String temp = new String(devicePacket);
//        String[] deviceString = temp.split("%");

//        Log.d("Device" , "Num of properties : " + deviceString.length);

        Device_id = read8(devicePacket[0]);
        Device_type = read8(devicePacket[2]);

        pos = 4;
        int count = 0;
        char readT;
        while ((readT = (char) devicePacket[pos++]) != '%') {
            count++;
        }
        byte[] device_Temp = new byte[count];
        Log.d("Device", "Count : " + count);
        int pos_temp = 4;
        for (int i = 0; i < count; i++) {
            device_Temp[i] = devicePacket[pos_temp + i];
        }
        Device_name = new String(device_Temp);
        Log.d("Device", "pos : " + pos);
        if(getDeviceType() != GeniusProtocol.TEMP) {
            value1 = read8(devicePacket[pos]);
            pos += 2;
            value2 = read8(devicePacket[pos]);
        }
        else{
            value1 = read8(devicePacket[pos])-100;
            pos += 2;
            value2 = read8(devicePacket[pos])-100;
        }

        favor = 0;
        Log.d("Device", "id : " + Device_id + "\tname : " + Device_name + "\ttype : " + Device_type + "\tv1 : " + value1 + "\tv2 : " + value2);

    }

    private int read8(byte int8_t) {
        return int8_t & 0xff;
    }


    public int read16(byte int_16_1, byte int_16_2) {
        return ((int_16_1 & 0xff) + ((int_16_2 & 0xff) << 8));
    }

    public String getDevice_name() {
        return Device_name;
    }

    public int getDeviceId() {
        return Device_id;
    }

    public int getDeviceType() {
        return Device_type;
    }

    public String getDevice_address() {
        return Device_address;
    }

    public int[] getvalue() {
        int[] value = {value1, value2};

        return value;
    }

    public void setValue(int[] value) {
        this.value1 = value[0];
        this.value2 = value[1];

    }

    public void setControlllist(boolean isControlllist) {
        this.isControlllist = isControlllist;
    }

    public boolean isControlllist() {
        return isControlllist;
    }

    public void setFavor(int favor) {
        this.favor = favor;
    }

    public int getFavor() {
        return favor;
    }


    public static HashMap<Integer, Integer> getBathInformation(int value1, int value2) {
        HashMap<Integer, Integer> bath = new HashMap<>();

        // 1.request
        int execution_request = ((value1) & 0b10000000) >> 7;
        bath.put(Command.BATH_EXECUTION_REQUEST, execution_request);
        int water_request = ((value1 ) & 0b01100000) >> 5;
        bath.put(Command.BATH_WATER_REQUEST, water_request);
        int temp_request = (value1 ) & 0b00011111;
        bath.put(Command.BATH_TEMP_REQUEST, temp_request);

        // 1.result
        int execution_result = ((value2 ) & 0b10000000) >> 7;
        bath.put(Command.BATH_EXECUTION_RESULT, execution_result);
        int water_result = ((value2) & 0b01100000) >> 5;
        bath.put(Command.BATH_WATER_RESULT, water_result);
        int temp_result = (value2 ) & 0b00011111;
        bath.put(Command.BATH_TEMP_RESULT, temp_result);

        return bath;

    }

    public static int[] setBathInformation(HashMap<Integer,Integer> bath){
        int[] bath_value = new int[2];
        bath_value[0] = 0;
        bath_value[1] = 0;

        //request
        int execution_request = bath.get(Command.BATH_EXECUTION_REQUEST);
        int water_request = bath.get(Command.BATH_WATER_REQUEST);
        int temp_request = bath.get(Command.BATH_TEMP_REQUEST);
        int execution_result = bath.get(Command.BATH_EXECUTION_RESULT);
        int water_result = bath.get(Command.BATH_WATER_RESULT);
        int temp_result = bath.get(Command.BATH_TEMP_RESULT);

        bath_value[0] = (((execution_request & 0x01)<< 7) | ((water_request & 0b00000011) << 5) | (temp_request & 0b00011111));
        bath_value[1] = (((execution_result & 0x01)<< 7) | ((water_result & 0b00000011) << 5) | (temp_result & 0b00011111));
        Log.d("device ", "value 1: " +Integer.toBinaryString(bath_value[0]));
        Log.d("device ", "value 2: " +Integer.toBinaryString(bath_value[1]));



        return bath_value;
    }
}
