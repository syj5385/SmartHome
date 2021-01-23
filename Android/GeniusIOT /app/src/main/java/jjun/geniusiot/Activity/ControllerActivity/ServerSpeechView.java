package jjun.geniusiot.Activity.ControllerActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import jjun.geniusiot.AndroidDB.ChatbotDB;
import jjun.geniusiot.R;

/**
 * Created by jjun on 2018. 7. 9..
 */

public class ServerSpeechView extends View {

    private int who;
    private String Chat;
    private String time;

    private float width,height;

    public ServerSpeechView(Context context,int who,String Chat, String time) {
        super(context);
        this.who = who;
        this.Chat = Chat;
        this.time = time;
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Log.d("ServerSpeechView","new view");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(width != 0 && height != 0){
            if(who == ChatbotDB.SERVER){
                Bitmap homeicon = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.home),(int)width/6, (int)width/6,false);
                canvas.drawBitmap(homeicon,10,10,null);

            }
            else if(who == ChatbotDB.USER){

            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(w>0 && h>0 ){
            width = w;
            height = h;
            invalidate();
        }
    }
}
