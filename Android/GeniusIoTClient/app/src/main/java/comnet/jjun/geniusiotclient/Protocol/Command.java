package comnet.jjun.geniusiotclient.Protocol;

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
    private static final String header = "#S<R";

    //Raspberry
    public static final int REQUEST_DEVICE_INFO = 90;
    public static final int RESULT_NO_CHANGE = 91;
    public static final int RESULT_CHANGE = 92;


    // packet command MACRO

    // Devices type
    public static final int LED = 10;
    public static final int DOOR   = 20;
    public static final int WINDOW = 30;



    public static byte[] request_send_BLE_data(byte[] data){
        Character[] payload = new Character[data.length];
        int j =0;
        for(byte b : data){
            payload[j++] = (char)b;
        }

        List<Byte> packet = requestPacket(GeniusProtocol.BLE_DATA,payload);
        byte[] arr = new byte[packet.size()];
        int i=0;
        for(byte b : packet){
            arr[i++] = b;
        }

        return arr;
    }

    public static byte[] request_connectionCheck(){

        List<Byte> packet = requestPacket(GeniusProtocol.CONNECTION_CHECK);
        byte[] arr = new byte[packet.size()];
        int i=0;
        for(byte b : packet){
            arr[i++] = b;
        }

        return arr;
    }
    public static List<Byte> requestPacket(int command) {
        return requestPacket(command, null);
    }

    public static List<Byte> requestPacket(int command, Character[] payload) {
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

    public static Packet readPacketFromServer(byte[] packet_byte){
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

    private static int read8(byte data){
        return data & 0xff;
    }
}
