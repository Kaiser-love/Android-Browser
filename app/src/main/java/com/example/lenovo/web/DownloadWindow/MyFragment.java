package com.example.lenovo.web.DownloadWindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.example.lenovo.web.R;
import java.util.LinkedList;

public class MyFragment extends Fragment {
    private String name;
    private Context mContext;
    private DownloadAdapter adapterDownload;
    private ListView listView;
    private LinkedList<DownLoadBean> downLoadList;
    public  MyFragment()
    {

    }
    @SuppressLint("ValidFragment")
    public  MyFragment(String name, Context mContext , LinkedList<DownLoadBean> downLoadList)
    {
        this.name=name;
        this.mContext=mContext;
        this.downLoadList=downLoadList;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_layout,container,false);
        adapterDownload = new DownloadAdapter(mContext,R.layout.item_layout,downLoadList);
        listView= view.findViewById(R.id.download_content);
        listView.setAdapter(adapterDownload);
        //设置listview点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DownLoadBean page = downLoadList.get(i);
                Log.d("Thread",page.print());
            }
        });
        return view;
    }
}
