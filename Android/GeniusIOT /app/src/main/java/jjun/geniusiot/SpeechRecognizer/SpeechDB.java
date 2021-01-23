package jjun.geniusiot.SpeechRecognizer;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

import jjun.geniusiot.AndroidDB.DatabaseManager;
import jjun.geniusiot.Activity.SpeechDatabaseActivity;


/**
 * Created by comm on 2018-04-26.
 */
public class SpeechDB extends DatabaseManager {

    public static final int ID = 0;
    public static final int SPEECH = 1;
    public static final int Command = 2;

    private static final String TAG = "SpeechDB";

    public static final String SpeechTable = "Speech";

    public SpeechDB(Context context, Handler mHandler, String tableName) {
        super(context, mHandler, tableName);

        createTable(tableName);
    }

    @Override
    protected void createTable(String name) {
        String query = "CREATE TABLE IF NOT EXISTS " + name + "(ID INTEGER ,SPEECH TEXT , Command INTEGER)";
        mDB.execSQL(query);

    }

    public void insertDataintoSpeechTable(int id, String speech, int command){
        String query = "INSERT INTO " + SpeechTable + " (ID, SPEECH,Command) VALUES (" +id + ", '" + speech+ "', " + command + ")";
        mDB.execSQL(query);
        Log.d(TAG,"query : " + query);
        mHandler.obtainMessage(SpeechDatabaseActivity.FINISHED_INSERT).sendToTarget();
    }

    @Override
    public void selectDataFromSpeechTable() {
        super.selectDataFromSpeechTable();
        mHandler.obtainMessage(SELECTION_RESULT,SpeechDB,-1,cursor).sendToTarget();
    }

    public ArrayList<SpeechData> getAllDeviceFromDB() {
        Log.d(TAG, "get All Device");
        ArrayList<SpeechData> arr = new ArrayList<>();
        String query = "SELECT * FROM " + SpeechTable;
        cursor = mDB.rawQuery(query, null);
        int DeviceCount;

        while (cursor.moveToNext()) {
            arr.add(new SpeechData(cursor.getInt(ID), cursor.getString(SPEECH), cursor.getInt(Command)));
        }
        return arr;
    }

    public void deleteSpeechFromTable(String speech){
        try {
            String query = "DELETE FROM " + SpeechTable + " WHERE SPEECH='" +speech + "'" ;
            mDB.execSQL(query);
        }catch (SQLiteException e){
            e.printStackTrace();
        }
        mHandler.obtainMessage(SpeechDatabaseActivity.FINISHED_DELETE).sendToTarget();
    }



    protected DatabaseErrorHandler mErrorHandler = new DatabaseErrorHandler() {
        @Override
        public void onCorruption(SQLiteDatabase sqLiteDatabase) {

        }

    };




}
