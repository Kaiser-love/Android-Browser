package com.example.lenovo.web.DownloadWindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lenovo.web.MainWindow.MainActivity;
import com.example.lenovo.web.R;

import java.util.HashMap;
import java.util.LinkedList;

public class DownloadActivity extends AppCompatActivity  implements View.OnClickListener {
    private HashMap<String, Integer> map = new HashMap<String, Integer>();
    private LinkedList<DownLoadBean> downLoadList = new LinkedList<DownLoadBean>();
    private LinkedList<DownLoadBean> finishList = new LinkedList<DownLoadBean>();
    private MyFragment f1;
    private MyFragment f2;
    private MyReceiver mReceiver;
    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String name = intent.getStringExtra("name");
            String progress = intent.getStringExtra("progress");
            //判断下载任务是否已经存在
            if (!map.containsKey(name)) {//不存在 则新建
                downLoadList.add(new DownLoadBean(name, progress));
                map.put(name, downLoadList.size()-1);
                Log.d("Thread", "不存在");
            } else {
                //存在  在更新下载进度
                downLoadList.get(map.get(name)).setProgress(progress);
                if(progress.equals("100"))//如果进度为100，则下载完成，移到完成列表中
                {
                    finishList.add(downLoadList.get(map.get(name)));
                    downLoadList.remove(map.get(name));
                    Log.d("Thread", " 开始完成:"+finishList.get(map.get(name)).print());
                }
            }
            initFragment1();
           // Toast.makeText(context, "我接受到广播啦！！！", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        init();
        bindView();
    }
    protected void init()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction("action");
        mReceiver = new MyReceiver();
        this.registerReceiver(mReceiver, filter);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
    protected  void bindView()
    {
        findViewById(R.id.btn_running).setOnClickListener(this);
        findViewById(R.id.btn_finised).setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_running:
                initFragment1();
                break;
            case R.id.btn_finised:
                initFragment2();
                break;
        }
    }
    //显示第一个fragment
    private void initFragment1(){
        //开启事务，fragment的控制是由事务来实现的
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        f1 = new MyFragment("正在下载",this,downLoadList);
        transaction.replace(R.id.main_frame_layout, f1);
        //隐藏所有fragment
        hideFragment(transaction);
        //显示需要显示的fragment
        transaction.show(f1);
        //提交事务
        transaction.commit();
    }
    //显示第二个fragment
    private void initFragment2(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        f2 = new MyFragment("下载完成",this,finishList);
        transaction.replace(R.id.main_frame_layout,f2);
        /*f2 = new MyFragment("下载完成",this,downLoadList);
        transaction.replace(R.id.main_frame_layout,f2);*/
        hideFragment(transaction);
        transaction.show(f2);
        transaction.commit();
    }
    //隐藏所有的fragment
    private void hideFragment(FragmentTransaction transaction){
        if(f1 != null){
            transaction.hide(f1);
        }
        if(f2 != null){
            transaction.hide(f2);
        }
    }
    public void onBackPressed() {
        Intent i=new Intent();
        i.setClass(DownloadActivity.this,MainActivity.class);
        setResult(1, i);
        DownloadActivity.this.finish();
    }
}
