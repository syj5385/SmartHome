package jjun.geniusiot.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

import jjun.geniusiot.CustomAdapter.CustomAdapter3.Custom3_Item;
import jjun.geniusiot.CustomAdapter.CustomAdapter3.CustomAdapter3;
import jjun.geniusiot.R;
import jjun.geniusiot.SpeechRecognizer.OptionDB;
import jjun.geniusiot.SpeechRecognizer.OptionData;

public class OptionDatabaseActivity extends AppCompatActivity {


    public static final int REQUEST_SCAN_DB = 10;
    public static final int REQUEST_UPDATE_DB = 11;

    public static final int REQUEST_ADD_SPEECH = 100;
    public static final int FINISHED_INSERT = 101;
    public static final int FINISHED_DELETE = 102;

    private OptionDB optionDB;

    private LinearLayout addCommand, deleteCommand;
    private ListView speechlist;
    private CustomAdapter3 commandAdapter;

    private AlertDialog dbDialog;

    private ArrayList<OptionData> option ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speechdb);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        optionDB = new OptionDB(this,mHandler,"optionDB");

        commandAdapter = new CustomAdapter3(this);

        addCommand = findViewById(R.id.addCommand);

        addCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OptionDatabaseActivity.this,AddOptionActivity.class);
                startActivityForResult(intent,0);
                overridePendingTransition(R.anim.move_from_left,R.anim.move_to_right);

            }
        });

        speechlist = findViewById(R.id.speechlist);
        speechlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                final String optionName = ((Custom3_Item)adapterView.getAdapter().getItem(i)).getData()[0];
                Log.d("OptionDatabaseActivity","deleted option name : " + optionName);
                AlertDialog.Builder builder = new AlertDialog.Builder(OptionDatabaseActivity.this);
                builder.setTitle("옵션 삭제");
                builder.setMessage("정말로 '" + optionName + "' 옵션을 삭제하시겠습니까?");
                builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        optionDB.deleteOption(optionName);
                        setAllOptionInList();
                    }
                }).setNegativeButton("아니오",null).show();

                return true;
            }
        });
        setAllOptionInList();
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

            }
        }
    };

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.move_from_right,R.anim.move_to_left);
    }

    private void setAllOptionInList(){
        commandAdapter.removeItem();
        option = optionDB.getAllDeviceFromDB();
        for(int i=0; i<option.size(); i++){
            String message = option.get(i).getName() + "\nv1 : "+String.valueOf(option.get(i).getV1()) + "    v2 : " + String.valueOf(option.get(i).getV2());
            commandAdapter.addItem(new Custom3_Item(getResources().getDrawable(R.drawable.voice),message,String.valueOf(option.get(i).getId())));
        }
        speechlist.setAdapter(commandAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0){
            setAllOptionInList();
        }
    }
}
