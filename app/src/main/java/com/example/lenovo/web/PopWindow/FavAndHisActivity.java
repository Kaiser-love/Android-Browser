package com.example.lenovo.web.PopWindow;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lenovo.web.Database.DBManager;
import com.example.lenovo.web.Database.WebPage;
import com.example.lenovo.web.MainWindow.MainActivity;
import com.example.lenovo.web.R;

import java.util.Collections;
import java.util.LinkedList;

public class FavAndHisActivity extends AppCompatActivity {
    private LinkedList<WebPage> webList = new LinkedList<WebPage>();
    private WebPageAdapter adapterListView;
    private ListView listView;
    private Button favouriters;
    private Button history;
    private DBManager MyDBManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_layout);
        initData(true);
        setListener();
    }
    private class ButtonClickedListener implements View.OnClickListener {
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.favorites:
                    Toast.makeText(FavAndHisActivity.this,"点击了收藏",Toast.LENGTH_LONG).show();
                    initData(true);
                    break;
                case R.id.history:
                    Toast.makeText(FavAndHisActivity.this,"点击了历史",Toast.LENGTH_LONG).show();
                    initData(false);
                    break;
            }
        }
    }
    public void setListener()
    {
        ButtonClickedListener MyButtonListener=new ButtonClickedListener();
        favouriters=findViewById(R.id.favorites);
        history=findViewById(R.id.history);
        favouriters.setOnClickListener(MyButtonListener);
        history.setOnClickListener(MyButtonListener);

    }
    public void initData(boolean type)
    {
        MyDBManager= new DBManager(this);
        webList= MyDBManager.query(type);
        Collections.reverse(webList);
        adapterListView = new WebPageAdapter(FavAndHisActivity.this,R.layout.item_layout,webList);
        listView= (ListView)findViewById(R.id.favoritesAndHisotry_content);
        listView.setAdapter(adapterListView);
        //设置listview点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                WebPage page = webList.get(i);
                open(page.getURL());
                Toast.makeText(FavAndHisActivity.this,"点击了"+page.getURL(),Toast.LENGTH_LONG).show();
                FavAndHisActivity.this.finish();
            }
        });
    }
    public void open(String url)
    {
        Intent i=new Intent();
        i.setClass(FavAndHisActivity.this,MainActivity.class);
        Bundle bundle   = new Bundle();
        bundle.putString("url", url);
        i.putExtra("bundle", bundle);
        setResult(0, i);
    }
    public void onBackPressed() {
        Intent i=new Intent();
        i.setClass(FavAndHisActivity.this,MainActivity.class);
        setResult(1, i);
        FavAndHisActivity.this.finish();
    }
}
