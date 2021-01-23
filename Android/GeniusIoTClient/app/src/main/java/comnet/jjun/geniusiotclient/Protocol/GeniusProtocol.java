package comnet.jjun.geniusiotclient.Protocol;

/**
 * Created by comm on 2018-08-07.
 */

public class GeniusProtocol {
    // packet command MACRO

    // TO Server
    public static final int CONNECTION_CHECK = 61;
    public static final int BLE_DATA = 62;

    // FROM Server
    public static final int CONNECTION_OK = 81;
    public static final int UPDATE_DEVICE = 11;
    public static final int ADD_DEVICE = 13;
    public static final int REMOVE_DEVICE =14;


    // Devices type
    public static final int LED = 10;
    public static final int DOOR   = 20;
    public static final int WINDOW = 30;
    public static final int TEMP = 40;
    public static final int GAS = 50;
    public static final int BATH = 60;
    public static final int BATH_EXECUTION_REQUEST = 600;
    public static final int BATH_WATER_REQUEST = 601;
    public static final int BATH_TEMP_REQUEST = 602;
    public static final int BATH_EXECUTION_RESULT= 603;
    public static final int BATH_WATER_RESULT = 604;
    public static final int BATH_TEMP_RESULT = 605;
}
