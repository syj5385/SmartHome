package f6.iot_project.IoT_Device;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by comm on 2018-01-25.
 */

public class Command {

    // F6IOT packet
    // <header> <direction> <sizeofparameter> <command> <parameter> <checksum>

    // packet header
    private static final String header = "#S<A";

    //Raspberry
    public static final int REQUEST_DEVICE_INFO = 90;
    public static final int RESULT_NO_CHANGE = 91;
    public static final int RESULT_CHANGE = 92;


    // packet command MACRO
    public static final int BLE_SCAN =      100;
    public static final int BLE_ADD =       101;
    public static final int BLE_REMOVE =    102;
    public static final int BLE_EXECUTE =   103;
    public static final int BLE_SCAN_CHECK = 104;
    public static final int BLE_NOTING      = 105;
    public static final int BLE_SCANNING    =106;
    public static final int BLE_ADD_CHECK   = 107;
    public static final int BLE_BUSY_RESULT = 108;
    public static final int BLE_ADDING =        109;
    public static final int BLE_REQUEST_DEVICE = 11;
    public static final int BLE_NO_DEVICE = 111;


    public static final int SCAN_DEVICE = 10;
    public static final int UPDATE_DEVICE = 11;
    public static final int GET_DEVICE = 12;
    public static final int ADD_DEVICE = 13;
    public static final int REMOVE_DEVICE = 14;
    public static final int GET_ONE_DEVICE = 15;

    public static final int RESULT_BUSY =   30;
    public static final int RESULT_OK = 31;
    public static final int RESULT_NOTCONNECTED = 32;

    // Devices type
    public static final int LED = 10;
    public static final int DOOR   = 20;
    public static final int WINDOW = 30;


    private ArrayList<Character> payload;

    public byte[] request_blescan_toServer(){
        List<Byte> packet = requestPacket(BLE_SCAN);
        Log.d("Command","packet size  : " + packet.size());

        byte[] arr = new byte[packet.size()];

        int i=0;
        for(byte b : packet){
            arr[i++] = b;
        }

        Log.d("Command",new String(arr));

        return arr;
    }

    public byte[] request_isFinished_BleScan_toServer(){
        List<Byte> packet = requestPacket(BLE_SCAN_CHECK);
        Log.d("Command","packet size  : " + packet.size());

        byte[] arr = new byte[packet.size()];

        int i=0;
        for(byte b : packet){
            arr[i++] = b;
        }

        Log.d("Command",new String(arr));

        return arr;
    }

    public byte[] request_addDevice_toServer(int device_type,String name, String mac){
        byte[] encoded_name = null;
        try {
            encoded_name = name.getBytes("utf-8");
        }catch (Exception e){

        }
        Character[] payload = new Character[encoded_name.length + mac.length() + 4];
        Log.d("Command", "mac Size : " + mac.length());
        if(mac.length() != 17){
            return null;
        }
        Log.d("Command", "passed condition");

        int index =0 ;

        payload[index++] = (char)(device_type & 0xff);
        payload[index++] = '%';

        for(int i=0; i<encoded_name.length; i++){
            payload[index++] = (char)encoded_name[i];
        }

        payload[index++] = '%';

        for(int j=0; j<mac.length() ; j++){
            payload[index++] = mac.charAt(j);
        }

        payload[index++] = '%';

        List <Byte> packet = requestPacket(Command.ADD_DEVICE,payload);
        byte[] arr = new byte[packet.size()];

        int i=0;
        for(byte b : packet){
            arr[i++] = b;
        }

        return arr;
    }

    public byte[] request_removeDevice_toServer(int removed_ID){
        Character[] payload = new Character[1];

        int index =0 ;

        payload[index++] = (char)(removed_ID & 0xff);

        List <Byte> packet = requestPacket(Command.REMOVE_DEVICE,payload);
        byte[] arr = new byte[packet.size()];
        int i=0;
        for(byte b : packet){
            arr[i++] = b;
        }
        return arr;

    }

    public byte[] request_isFinished_AddDevice_toServer(){
        List<Byte> packet = requestPacket(BLE_ADD_CHECK);
        Log.d("Command","packet size  : " + packet.size());

        byte[] arr = new byte[packet.size()];

        int i=0;
        for(byte b : packet){
            arr[i++] = b;
        }

        Log.d("Command",new String(arr));

        return arr;
    }

    public byte[] request_current_Device_to_Server(){
        List<Byte> packet = requestPacket(GET_DEVICE);
        Log.d("Command","packet size  : " + packet.size());

        byte[] arr = new byte[packet.size()];

        int i=0;
        for(byte b : packet){
            arr[i++] = b;
        }

        Log.d("Command",new String(arr));

        return arr;
    }

    public byte[] request_current_Device_to_Server(int id){

        Character[] payload = new Character[2];
        payload[0] = (char)(id & 0xff);
        payload[1] = (char)(id & 0xff);

        List<Byte> packet = requestPacket(GET_ONE_DEVICE,payload);
        Log.d("Command","packet size  : " + packet.size());

        byte[] arr = new byte[packet.size()];

        int i=0;
        for(byte b : packet){
            arr[i++] = b;
        }

        Log.d("Command",new String(arr));

        return arr;
    }


    public byte[] request_execute_toServer(Device device){
        Character[] payload = new Character[7];
        payload[0] = (char)(device.getDeviceId()& 0xff);
        payload[1] = '%';
        payload[2] = (char)(device.getDeviceType() & 0xff);
        payload[3] = '%';
        payload[4] = (char)(device.getvalue()[0] & 0xff);
        payload[5] = '%';
        payload[6] = (char)(device.getvalue()[1] & 0xff);

        List<Byte> packet = requestPacket(UPDATE_DEVICE,payload);
        byte[] arr = new byte[packet.size()];

        int i=0;
        for(byte b : packet){
            arr[i++] = b;
        }
        return arr;

    }

    public List<Byte> requestPacket(int command) {
        return requestPacket(command, null);
    }

    public List<Byte> requestPacket(int command, Character[] payload) {
        byte checksum = 0;

        List<Byte> command_data = new LinkedList<Byte>();

        for (byte c : header.getBytes()) {
            command_data.add(c);
        }

        command_data.add((byte) (command & 0xFF));
        checksum ^= (command & 0xFF);

        byte sizeOfpayload = (byte) ((payload != null ? (int) (payload.length) : 0) & 0xFF);
        command_data.add(sizeOfpayload);
        checksum ^= (sizeOfpayload & 0xFF);



        if (payload != null) {
            for (char c : payload) {
                command_data.add((byte) (c & 0xFF));
                checksum ^= (c & 0xFF);
            }
        }

        command_data.add(checksum);

        return command_data;
    }

    public Packet readPacketFromServer(byte[] packet_byte){
        Packet packet = new Packet();

        int index = 0;
        String header = "";
        //header
        for(int i=0; i<4; i++){
            header += (char)packet_byte[index++];
        }
        packet.setHeader(header);
        Log.d("Command","header :  " +header);

        // command
        packet.setCommand(read8(packet_byte[index++]));
        Log.d("Command","command : " + packet.getCommand());

        // sizeofdata
        packet.setSizeofData(read8(packet_byte[index++]));
        Log.d("Command","size of data " + packet.getSizeofData());

        //prameter
        byte[] para_temp = new byte[packet.getSizeofData()];
        for(int i=0; i<packet.getSizeofData() ; i++){
            para_temp[i] = packet_byte[index++];
        }
        packet.setParameter(para_temp);

        packet.setChecksum(packet_byte[index++]);

        return packet;
    }

    private int read8(byte data){
        return data & 0xff;
    }
}
