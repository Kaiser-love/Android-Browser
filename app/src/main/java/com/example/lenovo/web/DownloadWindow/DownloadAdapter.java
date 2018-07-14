package com.example.lenovo.web.DownloadWindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.lenovo.web.R;
import java.util.LinkedList;

public class DownloadAdapter extends ArrayAdapter<DownLoadBean> {
    private LinkedList<DownLoadBean> mData;
    private int resourceId;
    private Context mContext;
    public DownloadAdapter(Context context, int resource,LinkedList<DownLoadBean> objects) {
        super(context, resource, objects);
        mContext=context;
        resourceId=resource;
        mData=objects;
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
        DownLoadBean page = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null) {
            view = LayoutInflater.from(mContext).inflate(resourceId,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView)view.findViewById(R.id.item_name);
            viewHolder.progress = (TextView)view.findViewById(R.id.item_url);
            view.setTag(viewHolder);//将viewHolder存储在view中
        }else{
            view = convertView;
            viewHolder = (ViewHolder)view.getTag(); //重新获取viewHolder
        }
        viewHolder.progress.setText("下载进度："+page.getProgress());
        viewHolder.name.setText(page.getName());
        return view;
    }
    class ViewHolder{
        TextView name;
        TextView progress;
    }
}
