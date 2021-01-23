package f6.iot_project.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import f6.iot_project.CustomAdapter.CustomAdapter3.Custom3_Item;
import f6.iot_project.CustomAdapter.CustomAdapter3.Custom3_View;
import f6.iot_project.CustomAdapter.CustomAdapter3.CustomAdapter3;
import f6.iot_project.Database.InsertSpeechDB_Dialog;
import f6.iot_project.Database.SpeechDB;
import f6.iot_project.Database.SpeechProcessManager;
import f6.iot_project.GoogleSTT.SpeechProcess;
import f6.iot_project.IoT_Device.Command;
import f6.iot_project.IoT_Device.Packet;
import f6.iot_project.R;

/**
 * Created by comm on 2018-04-26.
 */

public class SpeechDatabaseActivity extends AppCompatActivity {

    public static final int REQUEST_SCAN_DB = 10;
    public static final int REQUEST_UPDATE_DB = 11;

    public static final int REQUEST_ADD_SPEECH = 100;
    public static final int FINISHED_INSERT = 101;
    public static final int FINISHED_DELETE = 102;

    private SpeechDB speechDB;

    private LinearLayout addCommand, deleteCommand;
    private ListView speechlist;
    private CustomAdapter3 commandAdapter;

    private AlertDialog dbDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speechdb);

        speechDB = new SpeechDB(this,mHandler,SpeechDB.SpeechTable);
        commandAdapter = new CustomAdapter3(this);

        addCommand = findViewById(R.id.addCommand);
        addCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbDialog = new InsertSpeechDB_Dialog(SpeechDatabaseActivity.this,mHandler);
                dbDialog.show();
            }
        });

        speechlist = findViewById(R.id.speechlist);
        speechlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String speech = ((Custom3_Item)adapterView.getAdapter().getItem(i)).getData()[0];
                Log.d("SpeechDB",speech);
                AlertDialog.Builder builder = new AlertDialog.Builder(SpeechDatabaseActivity.this);
                builder.setTitle("정말로 '" + speech + "' 을(를) 지우시겠습니까?").setNegativeButton("아니오",null).setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                speechDB.deleteSpeechFromTable(speech);
                            }
                        }).start();
                    }
                }).create().show();


                return true;
            }
        });
        speechDB.selectDataFromSpeechTable();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case SpeechDB.SELECTION_RESULT :
                    Cursor cursor = (Cursor)msg.obj;
                    commandAdapter = new CustomAdapter3(SpeechDatabaseActivity.this);
                    Log.d("DBActivity","Received");
                    while(cursor.moveToNext()){
                        String commandText = cursor.getString(1);
                        int command = cursor.getInt(2);
                        Log.d("SpeechDB",commandText + " -> " + command);
                        commandAdapter.addItem(new Custom3_Item(getResources().getDrawable(R.drawable.voice),commandText,String.valueOf(command)));
                    }
                    speechlist.setAdapter(commandAdapter);

                    break;

                case REQUEST_ADD_SPEECH :
                    final String text = (String)msg.obj;
                    final int request = getCommandUsingExecution(msg.arg1);
                    Toast.makeText(SpeechDatabaseActivity.this,"text : " + text + "\nrequest : " + request,Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            speechDB.insertDataintoSpeechTable(0,text,request);
                        }
                    }).start();

                    break;

                case FINISHED_INSERT :case FINISHED_DELETE :
                    speechDB.selectDataFromSpeechTable();
                    break;

            }
        }
    };

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.hold,R.anim.appear);
    }

    private int getCommandUsingExecution(int execution){
        int command_result = -1;
        switch(execution){
            case 1 :
                command_result = SpeechProcessManager.REQUEST_SCAN_DEVICE;
                break;

            case 2 :
                command_result = SpeechProcessManager.REQUEST_LED_ON;
                break;

            case 3 :
                command_result = SpeechProcessManager.REQUEST_LED_OFF;
                break;

            case 4 :
                command_result = SpeechProcessManager.REQUEST_DOOR_ON;
                break;

            case 5 :
                command_result = SpeechProcessManager.REQUEST_DOOR_OFF;
                break;

            case 6 :
                command_result = SpeechProcessManager.REQUEST_WINDOW_ON;
                break;

            case 7 :
                command_result = SpeechProcessManager.REQUEST_WINDOW_OFF;
                break;

            case 8 :
                command_result = SpeechProcessManager.REQUEST_LED_MORE_BRIGHT;
                break;

            case 9 :
                command_result = SpeechProcessManager.REQUEST_LED_LESS_BRIGHT;
                break;

            case 10 :
                command_result = SpeechProcessManager.REQUEST_WINDOW_MORE_OPEN;
                break;

            case 11 :
                command_result = SpeechProcessManager.REQUEST_WINDOW_MORE_CLOSE;
                break;

            case 12 :
                command_result = SpeechProcessManager.REQUEST_LED_SPECIFIED_BRIGHT;
                break;

            case 13 :
                command_result = SpeechProcessManager.REQUEST_WINDOW_SPECIFIED_DEGREE;
                break;

            default :
                command_result = -1;
        }
        return command_result;

    }
}
