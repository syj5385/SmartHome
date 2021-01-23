package f6.iot_project.CustomAdapter.CustomAdatper2;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by jjunj on 2016-12-20.
 */

public class CustomAdapter2 extends BaseAdapter {
    private Context mContext;

    private List<Custom2_Item> mItems = new ArrayList<Custom2_Item>();

    public CustomAdapter2(Context context){
        mContext = context;
    }

    public int getCount(){
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {

        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Custom2_View itemView;
        if (convertView == null) {
            itemView = new Custom2_View(mContext, mItems.get(position));
        } else {
            itemView = (Custom2_View) convertView;
        }

        itemView.setIcon(mItems.get(position).getIcon());
        itemView.setText(0, mItems.get(position).getData(0));
        itemView.setText(1, mItems.get(position).getData(1));

        return itemView;
    }

    public void addItem(Custom2_Item item){
        mItems.add(item);
    }

    public void removeItem(){
        while(mItems.size()>0){
            mItems.remove(0);
        }
        Log.d(TAG,"remove size : " + mItems.size());
    }

}
