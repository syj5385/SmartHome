package jjun.geniusiot.CustomAdapter.CustomAdapter1;

import android.graphics.drawable.Drawable;

/**
 * Created by jjunj on 2017-09-13.
 */

public class Custom1_Item {
    private Drawable icon;
    private String name;


    public Custom1_Item(Drawable icon, String name) {
        this.icon = icon ;
        this.name = name;
    }

    public Drawable getIcon(){
        return icon;
    }
    public String getName(){
        return name;
    }


    public void setName(String name){
        this.name = name;
    }

    public void setIcon(Drawable icon){
        this.icon = icon;
    }
}
