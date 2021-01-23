package jjun.geniusiot.Activity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import jjun.geniusiot.CustomAdapter.CustomAdapter1.Custom1_Item;
import jjun.geniusiot.CustomAdapter.CustomAdapter1.CustomAdapter1;
import jjun.geniusiot.AndroidDB.DeviceDB;
import jjun.geniusiot.Device.Device;
import jjun.geniusiot.NetworkService.Command;
import jjun.geniusiot.R;
import jjun.geniusiot.SpeechRecognizer.OptionDB;


/**
 * Created by jjun on 2018. 6. 30..
 */

public class AddOptionActivity extends AppCompatActivity {

    private DeviceDB db;
    private ArrayList<Device> deviceList;
    private CustomAdapter1 adapter1;
    private LinearLayout mainView;


    private ListView deviceListView;

    private String optionName;

    private OptionDB optionDB;

    private int[] colorSet = {
            Color.rgb(0xff, 0xa7,0xa7),
            Color.rgb(0xff,0xc1,0x9e),
            Color.rgb(0xff, 0xe0,0x8c),
            Color.rgb(0xfa,0xed,0x7d),
            Color.rgb(0xce,0xf2,0x79),
            Color.rgb(0xb7,0xf0,0xB1),
            Color.rgb(0xb2,0xeb,0xf4),
            Color.rgb(0xb2,0xcc,0xff)
    };

    private TextView[] color = new TextView[8];
    int color_current = colorSet[0];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addoptio_activity);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        db = new DeviceDB(AddOptionActivity.this,DeviceDB.DeviceTable);
        optionDB = new OptionDB(AddOptionActivity.this,null,"optionDB");
        deviceList = db.getAllDeviceFromDB();
        mainView = findViewById(R.id.main_view);
        Log.d("AddOption","size : " + deviceList.size());

        color[0] = findViewById(R.id.color0);
        color[1] = findViewById(R.id.color1);
        color[2] = findViewById(R.id.color2);
        color[3] = findViewById(R.id.color3);
        color[4] = findViewById(R.id.color4);
        color[5] = findViewById(R.id.color5);
        color[6] = findViewById(R.id.color6);
        color[7] = findViewById(R.id.color7);
        mainView.setBackgroundColor(color_current);

        for(int i=0; i<color.length ;i++){
            color[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int id = view.getId();

                    switch(id){
                        case R.id.color0 :
                            color_current = colorSet[0];
                            break;

                        case R.id.color1 :
                            color_current = colorSet[1];
                            break;

                        case R.id.color2 :
                            color_current = colorSet[2];
                            break;

                        case R.id.color3 :
                            color_current = colorSet[3];
                            break;

                        case R.id.color4 :
                            color_current = colorSet[4];
                            break;

                        case R.id.color5 :
                            color_current = colorSet[5];
                            break;

                        case R.id.color6 :
                            color_current = colorSet[6];
                            break;

                        case R.id.color7 :
                            color_current = colorSet[7];
                            break;

                        default :
                            return;

                    }
                    mainView.setBackgroundColor(color_current);
                }
            });
        }

        adapter1 = new CustomAdapter1(AddOptionActivity.this);
        deviceListView = findViewById(R.id.devicelist);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int type = deviceList.get(i).getDeviceType();
                if(type == Command.LED || type == Command.WINDOW) {
                    implementationControlDialog(i);
                }
            }
        });

        for(int i=0; i<deviceList.size() ;i++){
            adapter1.addItem(new Custom1_Item(getResources().getDrawable(R.drawable.device_image),deviceList.get(i).getDevice_name()));
            int[] value = {100,140};
            deviceList.get(i).setValue(value);
        }

        deviceListView.setAdapter(adapter1);

        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText optionText = findViewById(R.id.optionName);
                optionName = optionText.getText().toString();
                if(optionName.length() < 2 || optionName.length() > 16){
                    Toast.makeText(getApplicationContext(),"옵션명의 글자 수를 확인해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }
                for(int i=0; i<deviceList.size(); i++){
                    optionDB.insertDataintoSpeechTable(optionName,deviceList.get(i).getDeviceId(),
                            deviceList.get(i).getvalue()[0],deviceList.get(i).getvalue()[1],color_current);
                    try{
                        Thread.sleep(100);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }

                finish();

            }
        });




    }

    private boolean isonoff = false;
    int level = 100;
    private AlertDialog dialog;
    private void implementationControlDialog(final int index){
        AlertDialog.Builder builder = new AlertDialog.Builder(AddOptionActivity.this);
        LinearLayout temp = (LinearLayout)View.inflate(AddOptionActivity.this,R.layout.addoption_dialog,null);
        temp.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        builder.setView(temp);
        dialog = builder.create();
        dialog.show();

        TextView deviceName = temp.findViewById(R.id.device_name);
        deviceName.setText(deviceList.get(index).getDevice_name());

        ImageView deviceImage = temp.findViewById(R.id.device_image);
        if(deviceList.get(index).getDeviceType() == Command.LED){
            deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.led0));
        }
        else if(deviceList.get(index).getDeviceType() == Command.WINDOW){
            deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.window_image_open));
        }

        final TextView[] onoff = new TextView[2];
        final LinearLayout levelControl = temp.findViewById(R.id.levelControl);
        onoff[0] = temp.findViewById(R.id.off);
        onoff[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onoff[0].setBackgroundColor(getResources().getColor(R.color.yellowColor));
                onoff[1].setBackgroundColor(Color.rgb(0xef,0xef,0xef));
                levelControl.setVisibility(View.GONE);
                isonoff = false;
            }
        });
        onoff[1] = temp.findViewById(R.id.on);
        onoff[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onoff[0].setBackgroundColor(Color.rgb(0xef,0xef,0xef));
                onoff[1].setBackgroundColor(getResources().getColor(R.color.yellowColor));
                levelControl.setVisibility(View.VISIBLE);
                isonoff = true;
            }
        });

        final TextView[] levelButton = new TextView[5];
        levelButton[0] = temp.findViewById(R.id.level1);
        levelButton[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=0; i<levelButton.length ;i++)
                    levelButton[i].setBackgroundColor(Color.rgb(0xef,0xef,0xef));
                levelButton[0].setBackgroundColor(getResources().getColor(R.color.yellowColor));
                level = 120;
            }
        });
        levelButton[1] = temp.findViewById(R.id.level2);
        levelButton[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=0; i<levelButton.length ;i++)
                    levelButton[i].setBackgroundColor(Color.rgb(0xef,0xef,0xef));
                levelButton[1].setBackgroundColor(getResources().getColor(R.color.yellowColor));
                level = 140;
            }
        });
        levelButton[2] = temp.findViewById(R.id.level3);
        levelButton[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=0; i<levelButton.length ;i++)
                    levelButton[i].setBackgroundColor(Color.rgb(0xef,0xef,0xef));
                levelButton[2].setBackgroundColor(getResources().getColor(R.color.yellowColor));
                level = 160;
            }
        });
        levelButton[3] = temp.findViewById(R.id.level4);
        levelButton[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=0; i<levelButton.length ;i++)
                    levelButton[i].setBackgroundColor(Color.rgb(0xef,0xef,0xef));
                levelButton[3].setBackgroundColor(getResources().getColor(R.color.yellowColor));
                level = 180;
            }
        });
        levelButton[4] = temp.findViewById(R.id.level5);
        levelButton[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=0; i<levelButton.length ;i++)
                    levelButton[i].setBackgroundColor(Color.rgb(0xef,0xef,0xef));
                levelButton[4].setBackgroundColor(getResources().getColor(R.color.yellowColor));
                level = 200;
            }
        });

        final TextView store = temp.findViewById(R.id.store);
        store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] value = new int[2];
                if(!isonoff){
                    value[0] = 100;
                    value[1] = 140;
                }
                else{
                    value[0] = 200;
                    value[1] = level;
                }

                deviceList.get(index).setValue(value);
                dialog.dismiss();
            }
        });


    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.move_from_left,R.anim.move_to_right);
    }

}
