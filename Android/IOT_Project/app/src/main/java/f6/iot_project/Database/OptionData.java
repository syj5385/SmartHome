package f6.iot_project.Database;

/**
 * Created by jjun on 2018. 6. 30..
 */

public class OptionData {

    private String name;
    private int id;
    private int v1;
    private int v2;

    public OptionData(String optionName, int id, int v1, int v2) {
        this.name = optionName;
        this.id = id;
        this.v1 = v1;
        this.v2 = v2;
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
}
