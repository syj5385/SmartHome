package f6.iot_project.CustomAdapter.CustomAdapter1;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import f6.iot_project.R;


/**
 * Created by jjunj on 2017-09-13.
 */

public class Custom1_View extends LinearLayout {
    ImageView icon;
    TextView name;

    public Custom1_View(Context context, Custom1_Item iconbox) {
        super(context);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.custom1_item,this,true);

        icon = (ImageView)findViewById(R.id.icon);
        icon.setImageDrawable(iconbox.getIcon());

        name = (TextView)findViewById(R.id.name);
        name.setText(iconbox.getName());

    }

    public void setName( String name){
        this.name.setText(name);
    }

    public void setIcon(Drawable icon){
        this.icon.setImageDrawable(icon);
    }
}
