package jjun.geniusiot.AndroidDB;

import android.app.Activity;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jjun.geniusiot.Device.Device;
import jjun.geniusiot.R;


/**
 * Created by comm on 2018-04-26.
 */
public class ChatbotDB extends DatabaseManager{

    private static final String TAG = "ChatDB";
    public static final String ChatTable = "Chat";

    public static final int SERVER = 0;
    public static final int USER = 1;

    public static final int WHO = 0;
    public static final int NUM = 1;
    public static final int Chat = 2;
    public static final int TIME = 3;
    public static final int DEVICE = 4;

    private Activity activity;

    public ChatbotDB(Activity activity, Context context, String tableName) {
        super(context, tableName);
        this.activity = activity;
        createTable(tableName);
//        dropTable(tableName);
    }

    @Override
    protected void createTable(String name) {
        String query = "CREATE TABLE IF NOT EXISTS " + name + " (WHO INTEGER, NUM INTEGER, Chat TEXT, Time TEXT ,DEVICE INTEGER)";
        mDB.execSQL(query);
    }

    public void insertDataintoDataTable(int who, String data, int device_id){
        int num = getCountOfDB();
        long now = System.currentTimeMillis();
        Date time = new Date(now);
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd\nHH.mm.ss");
        String current = format.format(time);
        Log.d(TAG,"current : "+ current);
        String query = "INSERT INTO " + ChatTable + " ";
        query += "(WHO, NUM, Chat,Time, DEVICE) ";
        query += "VALUES (";
        query += String.valueOf(who) + "," + String.valueOf(num) + ",'" + data + "','" + current + "'," + String.valueOf(device_id) + ")";

        mDB.execSQL(query);
        Log.d(TAG,"query : " + query);
    }

    public int getCountOfDB(){
        String query = "SELECT * FROM " + ChatTable ;
        try{
            cursor = mDB.rawQuery(query, null);
        }catch (SQLiteException e){
            return -1;
        }

        Log.d(TAG,"count : " + cursor.getCount());
        return cursor.getCount();
    }

    public ArrayList<Device> getAllChatFromDB(){
        Log.d(TAG,"get All Device");
        ArrayList<Device> arr = new ArrayList<>();
        String query = "SELECT * FROM " + ChatTable;
        cursor = mDB.rawQuery(query,null);
        int DeviceCount;

//        while(cursor.moveToNext()){
//            arr.add(new Device(cursor.getInt(ID),cursor.getInt(TYPE), cursor.getString(NAME), cursor.getInt(VALUE1),cursor.getInt(VALUE2),cursor.getInt(FAVOR)));
//        }
        return arr;
    }

    public ArrayList<Chat> getChatUsingID(int id){
        ArrayList<Chat> temp = new ArrayList<>();
        if(id == -1)
            return null;
        String query = "SELECT * FROM " + ChatTable + " WHERE DEVICE="+id;
        cursor = mDB.rawQuery(query, null);

        while(cursor.moveToNext()){
            temp.add(new Chat(cursor.getInt(WHO), cursor.getInt(NUM), cursor.getString(Chat), cursor.getString(TIME), cursor.getInt(DEVICE)));
        }

        return temp;
    }


    private DatabaseErrorHandler mErrorHandler = new DatabaseErrorHandler() {
        @Override
        public void onCorruption(SQLiteDatabase sqLiteDatabase) {

        }

    };

    public void removeAllChatInDB(int id){
        String query = "DELETE FROM "+ ChatTable + " WHERE DEVICE = " + String.valueOf(id);
        mDB.execSQL(query);
    }

    public ArrayList<Chat> setConversation(final LinearLayout conversation, int id){
        conversation.removeAllViews();
        ArrayList<Chat> chat = getChatUsingID(id);
        int i;
        if(chat.size()<10)
            i = 0;
        else
            i = chat.size() -10;
//        Log.d(TAG,"Chat : " + chat);
        for(; i<chat.size() ;i++) {
            final Chat thisChat = chat.get(i);
            if(thisChat.getWho() == SERVER){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ConstraintLayout layout = (ConstraintLayout)View.inflate(context, R.layout.conversation_server, null);
                        TextView chatText = layout.findViewById(R.id.chat);
                        TextView dateText = layout.findViewById(R.id.time_text);
                        chatText.setText(thisChat.getData());
//                        Log.d(TAG,"chat : " + thisChat.getData());
                        dateText.setText(thisChat.getTime());
//                        Log.d(TAG,"date : " + thisChat.getTime());
                        conversation.addView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                });

            }
            else if(thisChat.getWho() == USER){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ConstraintLayout layout = (ConstraintLayout)View.inflate(context, R.layout.conversation_user, null);
                        TextView chatText = layout.findViewById(R.id.chat);
                        TextView dateText = layout.findViewById(R.id.time_text);
                        chatText.setText(thisChat.getData());
                        dateText.setText(thisChat.getTime());
                        conversation.addView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                });

            }
        }

        return chat;

    }

    private class Chat{
        private int who;
        private int num;
        private String data;
        private String time;
        private int id;

        public Chat(int who, int num, String data, String time, int id) {
            this.who = who;
            this.num = num;
            this.data = data;
            this.time = time;
            this.id = id;

        }

        public int getWho(){
            return who;
        }

        public String getData(){
            return data;

        }

        public String getTime(){
            return time;
        }

    }


}
