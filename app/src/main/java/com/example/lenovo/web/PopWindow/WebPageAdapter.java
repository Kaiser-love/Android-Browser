package com.example.lenovo.web.PopWindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.lenovo.web.Database.WebPage;
import com.example.lenovo.web.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WebPageAdapter extends ArrayAdapter<WebPage> {
    private LinkedList<WebPage> mData;
    private int resourceId;
    private Context mContext;
    public WebPageAdapter(Context context, int resource, LinkedList<WebPage> objects) {
        super(context, resource, objects);
        mData=objects;
        mContext=context;
        resourceId = resource;
    }
    @Override
    public int getCount() {
        return mData.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WebPage page = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null) {
            view = LayoutInflater.from(mContext).inflate(resourceId,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView)view.findViewById(R.id.item_name);
            viewHolder.url = (TextView)view.findViewById(R.id.item_url);
            view.setTag(viewHolder);//将viewHolder存储在view中
        }else{
            view = convertView;
            viewHolder = (ViewHolder)view.getTag(); //重新获取viewHolder
        }
        viewHolder.name.setText(page.getName());
        viewHolder.url.setText(page.getURL()+"        "+page.getDate());
        return view;
    }
    class ViewHolder{
        TextView name;
        TextView url;
    }
}