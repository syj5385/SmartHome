package f6.iot_project.Database;

import android.hardware.SensorManager;

/**
 * Created by comm on 2018-05-03.
 */

public class SpeechData {
    private int id ;
    private int SpeechDataType;
    private String text;
    private int Command;

    public SpeechData(int id, String speech, int command) {
        super();
        this.id = id;
        this.text = speech;
        this.Command = command;

    }


    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void setSpeechDataType(int SpeechDataType){
        this.SpeechDataType = SpeechDataType;
    }

    public int getSpeechDataType(){
        return SpeechDataType;
    }

    public void setText(String text){
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setCommand(int Command){
        this.Command = Command;
    }

    public int getCommand(){
        return Command;
    }


}
