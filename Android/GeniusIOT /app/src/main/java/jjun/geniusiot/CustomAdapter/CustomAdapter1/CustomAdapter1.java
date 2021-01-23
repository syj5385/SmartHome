package jjun.geniusiot.CustomAdapter.CustomAdapter1;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jjunj on 2017-09-13.
 */

public class CustomAdapter1 extends BaseAdapter {
    private Context mContext;
    private List<Custom1_Item> IconBox = new ArrayList<Custom1_Item>();

    public CustomAdapter1(Context context) {
        mContext = context;
    }

    public int getCount(){
        return IconBox.size();
    }

    @Override
    public Object getItem(int i) {
        return IconBox.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public View getView(int position, View converView, ViewGroup parent){
        Custom1_View itembox;
        if(converView == null)
            itembox = new Custom1_View(mContext, IconBox.get(position));
        else
            itembox = (Custom1_View)converView;

        itembox.setIcon(IconBox.get(position).getIcon());
        itembox.setName(IconBox.get(position).getName());

        return itembox;
    }

    public void addItem(Custom1_Item item){
        IconBox.add(item);
    }
}