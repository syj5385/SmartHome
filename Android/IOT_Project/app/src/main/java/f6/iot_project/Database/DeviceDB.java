package f6.iot_project.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

import f6.iot_project.CustomAdapter.CustomAdapter3.Custom3_Item;
import f6.iot_project.FileManagement.DirectoryManager;
import f6.iot_project.IoT_Device.Device;
import f6.iot_project.IoT_Device.Packet;
import f6.iot_project.R;

/**
 * Created by comm on 2018-04-26.
 */
public class DeviceDB extends DatabaseManager{

    private static final String TAG = "DeviceDB";
    public static final String DeviceTable = "Device";
    public static final String FavorieTable = "favor";

    public static final int ID = 0;
    public static final int TYPE = 1;
    public static final int NAME = 2;
    public static final int VALUE1 = 3;
    public static final int VALUE2 = 4;
    public static final int FAVOR = 5;

    public DeviceDB(Context context, Handler mHandler, String tableName) {
        super(context, mHandler, tableName);
        createTable(tableName);
        createFavorTable(FavorieTable);
//        dropTable(tableName);
    }

    @Override
    protected void createTable(String name) {
        String query = "CREATE TABLE IF NOT EXISTS " + name + " (ID INTEGER,TYPE INTEGER ,NAME TEXT , V1 INTEGER, V2 INTEGER, FAVOR INTEGER)";
        mDB.execSQL(query);
    }

    private void createFavorTable(String name){
        String query = "CREATE TABLE IF NOT EXISTS " + name + " (FAVOR INTEGER)";
        mDB.execSQL(query);
    }

    public void insertFavorIntoTable(int favorid){
        String query = "INSERT INTO " + FavorieTable + " ";
        query += "(FAVOR) VALUES (" + favorid + ")";
        mDB.execSQL(query);
    }

    public void deleteFavorFromTable(int favorid){
        try {
            String query = "DELETE FROM " + FavorieTable + " WHERE FAVOR=" + favorid;
            mDB.execSQL(query);
        }catch (SQLiteException e){
            e.printStackTrace();
        }

    }

    public ArrayList<Integer> selectFavorFromTable(){
        String query ="SELECT * FROM " + FavorieTable;
        ArrayList<Integer> favor = new ArrayList<>();
        cursor = mDB.rawQuery(query,null);
        while(cursor.moveToNext()){
            favor.add(cursor.getInt(0));
        }

        return favor;
    }
    public void insertDataintoDataTable(int id, int type, String name, int[] value){
        String query = "INSERT INTO " + DeviceTable + " ";
        query += "(ID, TYPE, NAME, V1, V2, FAVOR) ";
        query += "VALUES (";
        query += id +", " + type + ", '" + name + "', " + value[0] + ", " + value[1] + ", " + 0 + ")";

        mDB.execSQL(query);
        Log.d(TAG,"query : " + query);
    }



    public int getFavorFromDBUsingID(int id){
        int result = 0;
        String query = "SELECT FAVOR FROM " + DeviceTable + " WHERE ID="+id;


        try{
            cursor = mDB.rawQuery(query, null);
        }catch (SQLiteException e){
            return -1;
        }

        return cursor.getInt(0);
    }

    public ArrayList<Device> getAllDeviceFromDB(){
        Log.d(TAG,"get All Device");
        ArrayList<Device> arr = new ArrayList<>();
        String query = "SELECT * FROM " + DeviceTable;
        cursor = mDB.rawQuery(query,null);
        int DeviceCount;

        while(cursor.moveToNext()){
            arr.add(new Device(cursor.getInt(ID),cursor.getInt(TYPE), cursor.getString(NAME), cursor.getInt(VALUE1),cursor.getInt(VALUE2),cursor.getInt(FAVOR)));
        }
        return arr;
    }

    public Device getDeviceUsingID(int id){
        if(id == -1)
            return null;
        String query = "SELECT * FROM " + DeviceTable + " WHERE ID="+id;
        cursor = mDB.rawQuery(query, null);
        cursor.moveToNext();
        return new Device(cursor.getInt(ID),cursor.getInt(TYPE), cursor.getString(NAME), cursor.getInt(VALUE1),cursor.getInt(VALUE2),cursor.getInt(FAVOR));
    }


    private DatabaseErrorHandler mErrorHandler = new DatabaseErrorHandler() {
        @Override
        public void onCorruption(SQLiteDatabase sqLiteDatabase) {

        }

    };

    public void removeAllDeviceInDB(){
        String query = "DELETE FROM "+ DeviceTable;
        mDB.execSQL(query);
    }

    public void updateDeviceFromServer(Packet recvPacket){
        removeAllDeviceInDB();
        byte charTemp;
        int index = 0;
        ArrayList<Byte> temp = new ArrayList<>();
        while(index < recvPacket.getParameter().length) {
            charTemp = recvPacket.getParameter()[index++];
            if(charTemp == '&'){
                if(temp.size() != 0) {
                    byte[] newDevice = new byte[temp.size()];
                    for (int j = 0; j < temp.size(); j++) {
                        newDevice[j] = temp.get(j);
                    }
                    Log.d(TAG,"DEVICE " + new String(newDevice));
                    Device newD = new Device(newDevice);
                    insertDataintoDataTable(newD.getDeviceId(), newD.getDeviceType(), newD.getDevice_name(),newD.getvalue());

                    temp.clear();
                }
            }
            else{
                temp.add(charTemp);
            }
        }
    }


}
