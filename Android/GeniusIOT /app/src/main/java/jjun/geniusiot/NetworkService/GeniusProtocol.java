package jjun.geniusiot.NetworkService;

/**
 * Created by comm on 2018-08-07.
 */

public class    GeniusProtocol {
    // packet command MACRO

    // TO Server
    public static final int CONNECTION_CHECK = 61;

    // FROM Server
    public static final int CONNECTION_OK = 81;


    public static final int UPDATE_DEVICE = 11;
    public static final int GET_DEVICE = 12;
    public static final int ADD_DEVICE = 13;
    public static final int REMOVE_DEVICE = 14;
    public static final int GET_ONE_DEVICE = 15;
    public static final int HELLO_ANDROID = 16;

    public static final int RESULT_BUSY=30;
    public static final int RESULT_OK = 31;

    // Devices type
    public static final int LED = 10;
    public static final int DOOR   = 20;
    public static final int WINDOW = 30;
    public static final int TEMP = 40;
    public static final int GAS = 50;
    public static final int BATH = 60;
    public static final int BLE_DATA = 62;

}
