package f6.iot_project.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import f6.iot_project.FileManagement.DirectoryManager;

/**
 * Created by comm on 2018-04-27.
 */

public abstract class DatabaseManager {

    private static final String TAG = "DatabaseManager";

    public static final String GeniusHomeDirPath = Environment.getExternalStorageDirectory() +"/GeniusHome";
    public static final String SpeechDBDirectory = "Database";
    public static final String DB_name ="genius.db";

    public static final int SELECTION_RESULT = 0;
    protected static final int SpeechDB = 10;
    protected static final int DeviceDB = 11;

    protected SQLiteDatabase mDB;
    protected Context context;
    protected Handler mHandler;

    protected String tableName;

    public DatabaseManager(Context context, Handler mHandler, String tableName) {
        this.context = context;
        this.mHandler = mHandler;
        this.tableName = tableName;

        boolean success = DirectoryManager.mkdir(DatabaseDefine.GeniusHomeDirPath,DatabaseDefine.SpeechDBDirectory);
        try {
            mDB = SQLiteDatabase.openOrCreateDatabase(DatabaseDefine.GeniusHomeDirPath + "/" + DatabaseDefine.SpeechDBDirectory + "/" +DatabaseDefine.DB_name, null);
            Log.d(TAG,"Success to open " + mDB.getPath());
        }catch (SQLiteException e){
            e.printStackTrace();
        }
//
    }



    protected boolean dropTable(String name){
        String query = "DROP TABLE " + name;
        mDB.execSQL(query);
        return true;
    }

    protected Cursor cursor;
    protected void selectDataFromSpeechTable(){
        String query = "SELECT * FROM " + tableName;
        cursor = mDB.rawQuery(query, null);

//        mHandler.obtainMessage(SELECTION_RESULT,cursor).sendToTarget();
    }

    protected void executeQuery(String query){
        try{
            mDB.execSQL(query);
        }catch (SQLiteException e){
            e.printStackTrace();
        }

    }

    protected void createTable(String name){

    }




}
