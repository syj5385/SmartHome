package jjun.geniusiot.SpeechRecognizer;

/**
 * Created by jjun on 2018. 6. 30..
 */

public class OptionData {

    private String name;
    private int id;
    private int v1;
    private int v2;
    private int color;

    public OptionData(String optionName, int id, int v1, int v2,int color) {
        this.name = optionName;
        this.id = id;
        this.v1 = v1;
        this.v2 = v2;
        this.color = color;
    }

    public String getName(){
        return name;
    }
    public int getId(){
        return id;
    }

    public int getV1(){
        return v1;
    }

    public int getV2(){
        return v2;
    }

    public int getColor(){return color;}
}
