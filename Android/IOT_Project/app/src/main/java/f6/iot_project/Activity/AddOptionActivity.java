package f6.iot_project.Activity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import f6.iot_project.CustomAdapter.CustomAdapter1.Custom1_Item;
import f6.iot_project.CustomAdapter.CustomAdapter1.CustomAdapter1;
import f6.iot_project.Database.DeviceDB;
import f6.iot_project.Database.OptionDB;
import f6.iot_project.IoT_Device.Command;
import f6.iot_project.IoT_Device.Device;
import f6.iot_project.R;

/**
 * Created by jjun on 2018. 6. 30..
 */

public class AddOptionActivity extends AppCompatActivity {

    private DeviceDB db;
    private ArrayList<Device> deviceList;
    private CustomAdapter1 adapter1;

    private ListView deviceListView;

    private String optionName;

    private OptionDB optionDB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addoptio_activity);

        db = new DeviceDB(AddOptionActivity.this,null,DeviceDB.DeviceTable);
        optionDB = new OptionDB(AddOptionActivity.this,null,"optionDB");
        deviceList = db.getAllDeviceFromDB();
        Log.d("AddOption","size : " + deviceList.size());

        adapter1 = new CustomAdapter1(AddOptionActivity.this);
        deviceListView = findViewById(R.id.devicelist);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                implementationControlDialog(i);

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
                            deviceList.get(i).getvalue()[0],deviceList.get(i).getvalue()[1]);
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


}
