package f6.iot_project.Database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

import f6.iot_project.Activity.SpeechDatabaseActivity;

/**
 * Created by jjun on 2018. 6. 30..
 */

public class OptionDB extends DatabaseManager {

    private static final String TAG = "OptionDB";
    public String OptionTable = "";
    public static final String FavorieTable = "favor";

    public OptionDB(Context context, Handler mHandler, String tableName) {
        super(context, mHandler, tableName);
        OptionTable = tableName;
        createTable(tableName);
    }

    @Override
    protected void createTable(String name) {
        String query = "CREATE TABLE IF NOT EXISTS " + name + " (OptionName TEXT ,ID INTEGER, V1 INTEGER, V2 INTEGER)";
        mDB.execSQL(query);
    }

    public void insertDataintoSpeechTable(String name, int id, int v1, int v2){
        String query = "INSERT INTO " + OptionTable + " (OptionName ,ID, V1,V2) VALUES ('" +name + "', " +id + ", " + v1+ ", " + v2 + ")";
        mDB.execSQL(query);
        Log.d(TAG,"query : " + query);
//        mHandler.obtainMessage(SpeechDatabaseActivity.FINISHED_INSERT).sendToTarget();
    }

    public ArrayList<OptionData> getAllDeviceFromDB() {
        Log.d(TAG, "get All Device");
        ArrayList<OptionData> arr = new ArrayList<>();
        String query = "SELECT * FROM " + OptionTable;
        cursor = mDB.rawQuery(query, null);
        int DeviceCount;

        while (cursor.moveToNext()) {
            arr.add(new OptionData(cursor.getString(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3)));
        }
        return arr;
    }


    public ArrayList<OptionData> getOptionFromDB(int id) {
        Log.d(TAG, "get All Device");
        ArrayList<OptionData> arr = new ArrayList<>();
        String query = "SELECT * FROM " + OptionTable + " WHERE id =" + id;
        cursor = mDB.rawQuery(query, null);
        int DeviceCount;

        while (cursor.moveToNext()) {
            arr.add(new OptionData(cursor.getString(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3)));
        }
        return arr;
    }

    public static boolean isExistOptionDB(String tableName){
        boolean existed = false;
        String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (ID INTEGER, V1 INTEGER, V2 INTEGER)";
        SQLiteDatabase DB = SQLiteDatabase.openOrCreateDatabase(DatabaseDefine.GeniusHomeDirPath + "/" + DatabaseDefine.SpeechDBDirectory + "/" +DatabaseDefine.DB_name, null);
        try {
            DB.execSQL(query);
            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }


    }

    @Override
    public boolean dropTable(String name) {
        return super.dropTable(name);
    }

    @Override
    public void executeQuery(String query) {
        super.executeQuery(query);
    }
}
