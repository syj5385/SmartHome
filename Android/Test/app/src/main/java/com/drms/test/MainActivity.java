package com.drms.test;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    public Button button;
    public static final int HELLO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int i = 8;
        button = findViewById(R.id.text1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.setBackgroundColor(Color.rgb(0x00,0x00,0x00));
                button.setText("Click");
                button.setTextColor(getResources().getColor(R.color.yellowColor));

                

            }
        });

    }

}
