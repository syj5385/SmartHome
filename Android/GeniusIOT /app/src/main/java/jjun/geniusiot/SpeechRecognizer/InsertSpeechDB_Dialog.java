package jjun.geniusiot.SpeechRecognizer;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import jjun.geniusiot.R;
import jjun.geniusiot.Activity.SpeechDatabaseActivity;


/**
 * Created by comm on 2018-05-03.
 */

public class InsertSpeechDB_Dialog extends AlertDialog {

    private Handler mHandler;
    private Context context;
    private LinearLayout dialogView;

    private CheckBox[] execCheck = new CheckBox[24];
    private EditText speech;
    private Button save;
    private int index;

    public InsertSpeechDB_Dialog(Context context, final Handler mHandler) {
        super(context);
        this.context = context;
        this.mHandler = mHandler;

        dialogView = (LinearLayout) View.inflate(context, R.layout.insertspeechdb,null);
        setView(dialogView);

        execCheck[0] = dialogView.findViewById(R.id.exe1);
        execCheck[1] = dialogView.findViewById(R.id.exe2);
        execCheck[2] = dialogView.findViewById(R.id.exe3);
        execCheck[3] = dialogView.findViewById(R.id.exe4);
        execCheck[4] = dialogView.findViewById(R.id.exe5);
        execCheck[5] = dialogView.findViewById(R.id.exe6);
        execCheck[6] = dialogView.findViewById(R.id.exe7);
        execCheck[7] = dialogView.findViewById(R.id.exe8);
        execCheck[8] = dialogView.findViewById(R.id.exe9);
        execCheck[9] = dialogView.findViewById(R.id.exe10);
        execCheck[10] = dialogView.findViewById(R.id.exe11);
        execCheck[11] = dialogView.findViewById(R.id.exe12);
        execCheck[12] = dialogView.findViewById(R.id.exe13);
        execCheck[13] = dialogView.findViewById(R.id.exe14);
        execCheck[14] = dialogView.findViewById(R.id.exe15);
        execCheck[15] = dialogView.findViewById(R.id.exe16);
        execCheck[16] = dialogView.findViewById(R.id.exe17);
        execCheck[17] = dialogView.findViewById(R.id.exe18);
        execCheck[18] = dialogView.findViewById(R.id.exe19);
        execCheck[19] = dialogView.findViewById(R.id.exe20);
        execCheck[20] = dialogView.findViewById(R.id.exe21);
        execCheck[21] = dialogView.findViewById(R.id.exe22);
        execCheck[22] = dialogView.findViewById(R.id.exe23);
        execCheck[23] = dialogView.findViewById(R.id.exe24);

        for(int i=0; i<execCheck.length; i++){
            execCheck[i].setOnClickListener(mCheckBoxClickListener);
        }

        speech = dialogView.findViewById(R.id.keyword);
        save = dialogView.findViewById(R.id.save);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String keyword = speech.getText().toString();
                if(keyword.length() ==0){
                    Toast.makeText(InsertSpeechDB_Dialog.this.context, "키워드를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                else {
                    index = 0;
                    while (index < execCheck.length) {
                        if (execCheck[index++].isChecked()) {
                            mHandler.obtainMessage(SpeechDatabaseActivity.REQUEST_ADD_SPEECH, index, -1, keyword).sendToTarget();
                            dismiss();
                            break;
                        }

                        if(index >= execCheck.length){
                            Toast.makeText(InsertSpeechDB_Dialog.this.context, "수행할 동작을 선택하세요.", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }
        });

        final Button cancel = dialogView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }

    private View.OnClickListener mCheckBoxClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            for(int i=0; i<execCheck.length;i++){
                execCheck[i].setChecked(false);
            }
            ((CheckBox)view).setChecked(true);
        }
    };

}
