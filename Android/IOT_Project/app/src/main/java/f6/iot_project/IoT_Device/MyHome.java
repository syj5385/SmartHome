package f6.iot_project.IoT_Device;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.hotspot2.pps.HomeSp;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.HashMap;

import f6.iot_project.R;

/**
 * Created by jjun on 2018. 5. 17..
 */

public class MyHome extends LinearLayout {
    private static final String TAG = "MyHOME";

    public static final int MYHOME = 110;

    private boolean clicked[] = {false, false, false, false};
    public static final int TOILET = 0;
    public static final int ROOM   = 1;
    public static final int LIVINGROOM = 2;
    public static final int KITCHEN = 3;
    public static final int HOME = 4;

    private Context context;
    private Handler mHandler;
    private float x, y;
    private float width, height;

    private int temperature = 20;

    private int display_state;

    public MyHome(Context context, Handler mHandler) {
        super(context);
        this.context = context;
        this.mHandler = mHandler;
        display_state = HOME;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(display_state == HOME) {
            drawHome(canvas);
        }

        if(display_state == TOILET){
            drawToilet(canvas);
        }
        if(display_state == ROOM){
            drawRoom(canvas);
        }
        if(display_state == LIVINGROOM){
            drawLivingRoom(canvas);
        }
        if(display_state == KITCHEN){
            drawKitchen(canvas);
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        x = width / 100;
        y = height / 80;
        Log.d(TAG,"Home display size : width : " + width + "\theight : " + height);
        Bitmap back = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new  Canvas(back);
        this.setBackground(new BitmapDrawable(back));
        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(display_state == HOME) {
                if (event.getX() >= 20 * x && event.getX() <= 50 * x) {
                    if (event.getY() >= 10 * y & event.getY() <= 30 * y) {// toilet
                        if (!clicked[TOILET]) {
                            for (int i = 0; i < clicked.length; i++)
                                clicked[i] = false;
                            clicked[TOILET] = true;

                        } else {
                            // execute TOILET Controller
                            clicked[TOILET] = false;
                            display_state = TOILET;
                            initializeLoop();
                        }
                    }
                }
                if (event.getX() >= 50 * x && event.getX() <= 80 * x) {
                    if (event.getY() >= 10 * y & event.getY() <= 40 * y) {// room
                        if (!clicked[ROOM]) {
                            for (int i = 0; i < clicked.length; i++)
                                clicked[i] = false;
                            clicked[ROOM] = true;
                            initializeLoop();
                        } else {
                            // execute TOILET Controller
                            clicked[ROOM] = false;
                            display_state = ROOM;
                            initializeLoop();

                        }
                    }
                }

                if (event.getX() >= 20 * x && event.getX() <= 45 * x) {
                    if (event.getY() >= 30 * y & event.getY() <= 75 * y) {// kitchen
                        if (!clicked[KITCHEN]) {
                            for (int i = 0; i < clicked.length; i++)
                                clicked[i] = false;
                            clicked[KITCHEN] = true;
                            initializeLoop();
                        } else {
                            // execute TOILET Controller
                            clicked[KITCHEN] = false;
                            display_state = KITCHEN;

                        }
                    }
                }
                if (event.getX() >= 55 * x && event.getX() <= 80 * x) {
                    if (event.getY() >= 40 * y & event.getY() <= 75 * y) {// kitchen
                        if (!clicked[LIVINGROOM]) {
                            for (int i = 0; i < clicked.length; i++)
                                clicked[i] = false;
                            clicked[LIVINGROOM] = true;
                            initializeLoop();
                        } else {
                            // execute TOILET Controller
                            clicked[LIVINGROOM] = false;
                            display_state = LIVINGROOM;

                        }
                    }
                }
            }
            else{
                if(event.getX() >= 2*x && event.getX() <= 18*x){
                    if(event.getY() >= 30*y & event.getY() <= 60*y){// kitchen
                        display_state = HOME;
                        invalidate();
                    }
                }
            }
        }
        invalidate();
        return true;
    }

    private void drawHome(Canvas canvas){

        Paint nonSelected = new Paint();
        nonSelected.setColor(Color.GRAY);
        nonSelected.setTextSize(5 * y);
        nonSelected.setTextAlign(Paint.Align.CENTER);

        Paint selected = new Paint();
        selected.setColor(Color.RED);
        selected.setTextSize(5 * y);
        selected.setTextAlign(Paint.Align.CENTER);


        float offset = nonSelected.getTextSize() / 2;

        Paint selectedRect = new Paint();
        selectedRect.setColor(context.getColor(R.color.yellowColor));

        HashMap<Integer, float[]> hash = new HashMap<>();
        float[] toilet = {35 * x, 20 * y + offset};
        hash.put(TOILET, toilet);

        float[] room = {65 * x, 25 * y + offset};
        hash.put(ROOM, room);

        float[] kitchen = {35 * x, 50 * y + offset};
        hash.put(KITCHEN, kitchen);

        float[] living = {68 * x, 55 * y + offset};
        hash.put(LIVINGROOM, living);

        String[] space = {"화장실", "방", "거실", "부엌"};

        for (int i = 0; i < 4; i++) {
            if (clicked[i]) {
                canvas.drawText(space[i], hash.get(i)[0], hash.get(i)[1], selected);
            } else {
                canvas.drawText(space[i], hash.get(i)[0], hash.get(i)[1], nonSelected);
            }
        }


        Paint temperaturePaint = new Paint();
        temperaturePaint.setColor(context.getColor(R.color.colorSelected));
        temperaturePaint.setTextSize(8 * y);
        temperaturePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(temperature), 87 * x, 10 * y + temperaturePaint.getTextSize() / 2, temperaturePaint);

        temperaturePaint.setTextSize(4 * y);
        canvas.drawText(" 'C", 87 * x + temperaturePaint.measureText(String.valueOf(temperature)) * 3 / 2,
                10 * y + temperaturePaint.getTextSize(), temperaturePaint);


        Paint wallPaint = new Paint();
        wallPaint.setStyle(Paint.Style.STROKE);
        wallPaint.setStrokeWidth(10);
        float paintOffset = wallPaint.getStrokeWidth() / 2;



        //wall
        canvas.drawLine(20 * x, 10 * y, 20 * x, 75 * y, wallPaint);
        canvas.drawLine(20 * x - paintOffset, 10 * y, 80 * x + paintOffset, 10 * y, wallPaint);
        canvas.drawLine(80 * x, 10 * y, 80 * x, 75 * y, wallPaint);

        // toilet wall
        canvas.drawLine(20 * x, 30 * y, 40 * x, 30 * y, wallPaint);

        // room wall
        canvas.drawLine(50 * x, 10 * y, 50 * x, 30 * y, wallPaint);
        canvas.drawLine(50 * x, 40 * y, 80 * x, 40 * y, wallPaint);

        canvas.drawLine(20 * x, 75 * y, 45 * x, 75 * y, wallPaint);
        canvas.drawLine(55 * x, 75 * y, 80 * x, 75 * y, wallPaint);

        wallPaint.setStrokeWidth(5);
        canvas.drawArc(45 * x, 65 * y, 65 * x, 85 * y, 180, 90, false, wallPaint);
        canvas.drawLine(55 * x, 65 * y, 55 * x, 75 * y, wallPaint);
    }

    private int alpha = 255;
    private float[] destination = {50*x, 5*y};
    private float movement = 0;
    private boolean looped = false;

    private void drawToilet(Canvas canvas){
        //BACK Button
        Bitmap back = BitmapFactory.decodeResource(context.getResources(),R.drawable.back);
        back = Bitmap.createScaledBitmap(back, (int)(15*x), (int)(15*x), false);
        canvas.drawBitmap(back, 10*x - back.getWidth()/2, 40*y-back.getHeight()/2,null);
        Paint backPaint = new Paint();
        backPaint.setTextSize(5*y);
        backPaint.setTextAlign(Paint.Align.CENTER);
        backPaint.setColor(Color.BLACK);
        canvas.drawText("Back",10*x, 50*y +backPaint.getTextSize()/2, backPaint);

        Paint wallPaint = new Paint();
        wallPaint.setStyle(Paint.Style.STROKE);
        wallPaint.setStrokeWidth(10);
        float paintOffset = wallPaint.getStrokeWidth() / 2;

        Paint wallInPaint = new Paint();
        wallInPaint.setStyle(Paint.Style.STROKE);
        wallInPaint.setStrokeWidth(10);
        wallInPaint.setAlpha(alpha);
        float paintInOffset = wallInPaint.getStrokeWidth() / 2;

        //wall
        canvas.drawLine(20 * x, 10 * y, 20 * x, 75 * y, wallPaint);
        canvas.drawLine(20 * x - paintOffset, 10 * y, 80 * x + paintOffset, 10 * y, wallPaint);
        canvas.drawLine(80 * x, 10 * y, 80 * x, 75 * y, wallPaint);

        canvas.drawLine(20* x, 75*y , current_x, 75 * y, wallPaint);


        // toilet wall
        canvas.drawLine(20 * x, 30 * y, 40 * x, 30 * y, wallInPaint);

        // room wall
        canvas.drawLine(50 * x, 10 * y, 50 * x, 30 * y, wallInPaint);
        canvas.drawLine(50 * x, 40 * y, 80 * x, 40 * y, wallInPaint);

        canvas.drawLine(20 * x, 75 * y, 45 * x, 75 * y, wallInPaint);
        canvas.drawLine(55 * x, 75 * y, 80 * x, 75 * y, wallInPaint);

        wallPaint.setStrokeWidth(5);
        canvas.drawArc(45 * x, 65 * y, 65 * x, 85 * y, 180, 90, false, wallInPaint);
        canvas.drawLine(55 * x, 65 * y, 55 * x, 75 * y, wallInPaint);


        Paint nonSelected = new Paint();
        nonSelected.setColor(Color.GRAY);
        nonSelected.setTextSize(5 * y);
        nonSelected.setTextAlign(Paint.Align.CENTER);
        nonSelected.setAlpha(alpha);

        Paint selected = new Paint();
        selected.setColor(Color.RED);
        selected.setTextSize(5 * y);
        selected.setTextAlign(Paint.Align.CENTER);


        float offset = nonSelected.getTextSize() / 2;
        HashMap<Integer, float[]> hash = new HashMap<>();


        float[] toilet = {50*x,5*y + offset};
        hash.put(TOILET, toilet);

        float[] room = {65 * x, 25 * y + offset};
        hash.put(ROOM, room);

        float[] kitchen = {35 * x, 50 * y + offset};
        hash.put(KITCHEN, kitchen);

        float[] living = {68 * x, 55 * y + offset};
        hash.put(LIVINGROOM, living);

        String[] space = {"화장실", "방", "거실", "부엌"};

        canvas.drawText(space[TOILET], hash.get(TOILET)[0], hash.get(TOILET)[1], selected);
        canvas.drawText(space[ROOM], hash.get(ROOM)[0], hash.get(ROOM)[1], nonSelected);
        canvas.drawText(space[LIVINGROOM], hash.get(LIVINGROOM)[0], hash.get(LIVINGROOM)[1], nonSelected);
        canvas.drawText(space[KITCHEN], hash.get(KITCHEN)[0], hash.get(KITCHEN)[1], nonSelected);

        looped = false;
        if(alpha > 0 ){
            looped = true;
            alpha -= 5;
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {

            }
        }

        if(toilet[0] < destination[0] || toilet[1] < destination[1]){
            movement += 5 ;
            looped = true;
        }

        if(looped) {
            invalidate();
            return;
        }
        looped = false;

        if(current_x <= 79*x){
            current_x += x;
            looped = true;
        }


        if(looped) {
            invalidate();
            return;
        }



    }
    private float current_x;

    private void drawRoom(Canvas canvas){
        //BACK Button
        Bitmap back = BitmapFactory.decodeResource(context.getResources(),R.drawable.back);
        back = Bitmap.createScaledBitmap(back, (int)(15*x), (int)(15*x), false);
        canvas.drawBitmap(back, 10*x - back.getWidth()/2, 40*y-back.getHeight()/2,null);
        Paint backPaint = new Paint();
        backPaint.setTextSize(5*y);
        backPaint.setTextAlign(Paint.Align.CENTER);
        backPaint.setColor(Color.BLACK);
        canvas.drawText("Back",10*x, 50*y +backPaint.getTextSize()/2, backPaint);

        Paint wallPaint = new Paint();
        wallPaint.setStyle(Paint.Style.STROKE);
        wallPaint.setStrokeWidth(10);
        float paintOffset = wallPaint.getStrokeWidth() / 2;


        Paint wallInPaint = new Paint();
        wallInPaint.setStyle(Paint.Style.STROKE);
        wallInPaint.setStrokeWidth(10);
        wallInPaint.setAlpha(alpha);
        float paintInOffset = wallInPaint.getStrokeWidth() / 2;

        //wall
        canvas.drawLine(20 * x, 10 * y, 20 * x, 75 * y, wallPaint);
        canvas.drawLine(20 * x - paintOffset, 10 * y, 80 * x + paintOffset, 10 * y, wallPaint);
        canvas.drawLine(80 * x, 10 * y, 80 * x, 75 * y, wallPaint);

        canvas.drawLine(20* x, 75*y , current_x, 75 * y, wallPaint);



        // toilet wall
        canvas.drawLine(20 * x, 30 * y, 40 * x, 30 * y, wallInPaint);

        // room wall
        canvas.drawLine(50 * x, 10 * y, 50 * x, 30 * y, wallInPaint);
        canvas.drawLine(50 * x, 40 * y, 80 * x, 40 * y, wallInPaint);

        canvas.drawLine(20 * x, 75 * y, 45 * x, 75 * y, wallInPaint);
        canvas.drawLine(55 * x, 75 * y, 80 * x, 75 * y, wallInPaint);

        wallPaint.setStrokeWidth(5);
        canvas.drawArc(45 * x, 65 * y, 65 * x, 85 * y, 180, 90, false, wallInPaint);
        canvas.drawLine(55 * x, 65 * y, 55 * x, 75 * y, wallInPaint);



        Paint nonSelected = new Paint();
        nonSelected.setColor(Color.GRAY);
        nonSelected.setTextSize(5 * y);
        nonSelected.setTextAlign(Paint.Align.CENTER);
        nonSelected.setAlpha(alpha);

        Paint selected = new Paint();
        selected.setColor(Color.RED);
        selected.setTextSize(5 * y);
        selected.setTextAlign(Paint.Align.CENTER);


        float offset = nonSelected.getTextSize() / 2;
        HashMap<Integer, float[]> hash = new HashMap<>();


        float[] toilet = {35*x,20*y + offset};
        hash.put(TOILET, toilet);

        float[] room = {50 * x, 5 * y + offset};
        hash.put(ROOM, room);

        float[] kitchen = {35 * x, 50 * y + offset};
        hash.put(KITCHEN, kitchen);

        float[] living = {68 * x, 55 * y + offset};
        hash.put(LIVINGROOM, living);

        String[] space = {"화장실", "방", "거실", "부엌"};

        canvas.drawText(space[TOILET], hash.get(TOILET)[0], hash.get(TOILET)[1], nonSelected);
        canvas.drawText(space[ROOM], hash.get(ROOM)[0], hash.get(ROOM)[1], selected);
        canvas.drawText(space[LIVINGROOM], hash.get(LIVINGROOM)[0], hash.get(LIVINGROOM)[1], nonSelected);
        canvas.drawText(space[KITCHEN], hash.get(KITCHEN)[0], hash.get(KITCHEN)[1], nonSelected);

        looped = false;
        if(alpha > 0 ){
            looped = true;
            alpha -= 5;
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {

            }
        }


        if(looped) {
            invalidate();
            return;
        }
        looped = false;

        if(current_x <= 79*x){
            current_x += x;
            looped = true;
        }


        if(looped) {
            invalidate();
            return;
        }

    }

    private void drawLivingRoom(Canvas canvas){
        //BACK Button
        Bitmap back = BitmapFactory.decodeResource(context.getResources(),R.drawable.back);
        back = Bitmap.createScaledBitmap(back, (int)(15*x), (int)(15*x), false);
        canvas.drawBitmap(back, 10*x - back.getWidth()/2, 40*y-back.getHeight()/2,null);
        Paint backPaint = new Paint();
        backPaint.setTextSize(5*y);
        backPaint.setTextAlign(Paint.Align.CENTER);
        backPaint.setColor(Color.BLACK);
        canvas.drawText("Back",10*x, 50*y +backPaint.getTextSize()/2, backPaint);

        Paint wallPaint = new Paint();
        wallPaint.setStyle(Paint.Style.STROKE);
        wallPaint.setStrokeWidth(10);
        float paintOffset = wallPaint.getStrokeWidth() / 2;

        Paint wallInPaint = new Paint();
        wallInPaint.setStyle(Paint.Style.STROKE);
        wallInPaint.setStrokeWidth(10);
        wallInPaint.setAlpha(alpha);
        float paintInOffset = wallInPaint.getStrokeWidth() / 2;

        //wall
        canvas.drawLine(20 * x, 10 * y, 20 * x, 75 * y, wallPaint);
        canvas.drawLine(20 * x - paintOffset, 10 * y, 80 * x + paintOffset, 10 * y, wallPaint);
        canvas.drawLine(80 * x, 10 * y, 80 * x, 75 * y, wallPaint);

        canvas.drawLine(20* x, 75*y , current_x, 75 * y, wallPaint);


        // toilet wall
        canvas.drawLine(20 * x, 30 * y, 40 * x, 30 * y, wallInPaint);

        // room wall
        canvas.drawLine(50 * x, 10 * y, 50 * x, 30 * y, wallInPaint);
        canvas.drawLine(50 * x, 40 * y, 80 * x, 40 * y, wallInPaint);

        canvas.drawLine(20 * x, 75 * y, 45 * x, 75 * y, wallInPaint);
        canvas.drawLine(55 * x, 75 * y, 80 * x, 75 * y, wallInPaint);

        wallPaint.setStrokeWidth(5);
        canvas.drawArc(45 * x, 65 * y, 65 * x, 85 * y, 180, 90, false, wallInPaint);
        canvas.drawLine(55 * x, 65 * y, 55 * x, 75 * y, wallInPaint);


        Paint nonSelected = new Paint();
        nonSelected.setColor(Color.GRAY);
        nonSelected.setTextSize(5 * y);
        nonSelected.setTextAlign(Paint.Align.CENTER);
        nonSelected.setAlpha(alpha);

        Paint selected = new Paint();
        selected.setColor(Color.RED);
        selected.setTextSize(5 * y);
        selected.setTextAlign(Paint.Align.CENTER);


        float offset = nonSelected.getTextSize() / 2;
        HashMap<Integer, float[]> hash = new HashMap<>();


        float[] toilet = {35*x,20*y + offset};
        hash.put(TOILET, toilet);

        float[] room = {65 * x, 25 * y + offset};
        hash.put(ROOM, room);

        float[] kitchen = {35 * x, 50 * y + offset};
        hash.put(KITCHEN, kitchen);

        float[] living = {50 * x, 5 * y + offset};
        hash.put(LIVINGROOM, living);

        String[] space = {"화장실", "방", "거실", "부엌"};

        canvas.drawText(space[TOILET], hash.get(TOILET)[0], hash.get(TOILET)[1], nonSelected);
        canvas.drawText(space[ROOM], hash.get(ROOM)[0], hash.get(ROOM)[1], nonSelected);
        canvas.drawText(space[LIVINGROOM], hash.get(LIVINGROOM)[0], hash.get(LIVINGROOM)[1], selected);
        canvas.drawText(space[KITCHEN], hash.get(KITCHEN)[0], hash.get(KITCHEN)[1], nonSelected);

        looped = false;
        if(alpha > 0 ){
            looped = true;
            alpha -= 5;
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {

            }
        }

        if(toilet[0] < destination[0] || toilet[1] < destination[1]){
            movement += 5 ;
            looped = true;
        }

        if(looped) {
            invalidate();
            return;
        }
        looped = false;

        if(current_x <= 79*x){
            current_x += x;
            looped = true;
        }


        if(looped) {
            invalidate();
            return;
        }



    }

    private void drawKitchen(Canvas canvas){
        //BACK Button
        Bitmap back = BitmapFactory.decodeResource(context.getResources(),R.drawable.back);
        back = Bitmap.createScaledBitmap(back, (int)(15*x), (int)(15*x), false);
        canvas.drawBitmap(back, 10*x - back.getWidth()/2, 40*y-back.getHeight()/2,null);
        Paint backPaint = new Paint();
        backPaint.setTextSize(5*y);
        backPaint.setTextAlign(Paint.Align.CENTER);
        backPaint.setColor(Color.BLACK);
        canvas.drawText("Back",10*x, 50*y +backPaint.getTextSize()/2, backPaint);

        Paint wallPaint = new Paint();
        wallPaint.setStyle(Paint.Style.STROKE);
        wallPaint.setStrokeWidth(10);
        float paintOffset = wallPaint.getStrokeWidth() / 2;

        Paint wallInPaint = new Paint();
        wallInPaint.setStyle(Paint.Style.STROKE);
        wallInPaint.setStrokeWidth(10);
        wallInPaint.setAlpha(alpha);
        float paintInOffset = wallInPaint.getStrokeWidth() / 2;

        //wall
        canvas.drawLine(20 * x, 10 * y, 20 * x, 75 * y, wallPaint);
        canvas.drawLine(20 * x - paintOffset, 10 * y, 80 * x + paintOffset, 10 * y, wallPaint);
        canvas.drawLine(80 * x, 10 * y, 80 * x, 75 * y, wallPaint);

        canvas.drawLine(20* x, 75*y , current_x, 75 * y, wallPaint);


        // toilet wall
        canvas.drawLine(20 * x, 30 * y, 40 * x, 30 * y, wallInPaint);

        // room wall
        canvas.drawLine(50 * x, 10 * y, 50 * x, 30 * y, wallInPaint);
        canvas.drawLine(50 * x, 40 * y, 80 * x, 40 * y, wallInPaint);

        canvas.drawLine(20 * x, 75 * y, 45 * x, 75 * y, wallInPaint);
        canvas.drawLine(55 * x, 75 * y, 80 * x, 75 * y, wallInPaint);

        wallPaint.setStrokeWidth(5);
        canvas.drawArc(45 * x, 65 * y, 65 * x, 85 * y, 180, 90, false, wallInPaint);
        canvas.drawLine(55 * x, 65 * y, 55 * x, 75 * y, wallInPaint);


        Paint nonSelected = new Paint();
        nonSelected.setColor(Color.GRAY);
        nonSelected.setTextSize(5 * y);
        nonSelected.setTextAlign(Paint.Align.CENTER);
        nonSelected.setAlpha(alpha);

        Paint selected = new Paint();
        selected.setColor(Color.RED);
        selected.setTextSize(5 * y);
        selected.setTextAlign(Paint.Align.CENTER);


        float offset = nonSelected.getTextSize() / 2;
        HashMap<Integer, float[]> hash = new HashMap<>();


        float[] toilet = {35*x,20*y + offset};
        hash.put(TOILET, toilet);

        float[] room = {65 * x, 25 * y + offset};
        hash.put(ROOM, room);

        float[] kitchen = {50 * x, 5 * y + offset};
        hash.put(KITCHEN, kitchen);

        float[] living = {68 * x, 55 * y + offset};
        hash.put(LIVINGROOM, living);

        String[] space = {"화장실", "방", "거실", "부엌"};

        canvas.drawText(space[TOILET], hash.get(TOILET)[0], hash.get(TOILET)[1], nonSelected);
        canvas.drawText(space[ROOM], hash.get(ROOM)[0], hash.get(ROOM)[1], nonSelected);
        canvas.drawText(space[LIVINGROOM], hash.get(LIVINGROOM)[0], hash.get(LIVINGROOM)[1], nonSelected);
        canvas.drawText(space[KITCHEN], hash.get(KITCHEN)[0], hash.get(KITCHEN)[1], selected);

        looped = false;
        if(alpha > 0 ){
            looped = true;
            alpha -= 5;
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {

            }
        }

        if(toilet[0] < destination[0] || toilet[1] < destination[1]){
            movement += 5 ;
            looped = true;
        }

        if(looped) {
            invalidate();
            return;
        }
        looped = false;

        if(current_x <= 79*x){
            current_x += x;
            looped = true;
        }


        if(looped) {
            invalidate();
            return;
        }



    }


    public boolean[] getClicked(){
        return clicked;
    }

    public int getDisplay_state(){
        return display_state;
    }

    public void setDisplay_state(int display_state){
        this.display_state = display_state;
    }

    public void initializeLoop(){
        current_x = 20*x;
        alpha = 255;
        movement = 0;
    }

}
