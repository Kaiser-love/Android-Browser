package com.example.lenovo.web.MainWindow;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ClientCertRequest;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.web.Database.DBManager;
import com.example.lenovo.web.Database.WebPage;
import com.example.lenovo.web.Download.DownloadTask;
import com.example.lenovo.web.PopWindow.BottomMenu;
import com.example.lenovo.web.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String[][] MIME_MapTable={
            {".3gp",    "video/3gpp"},
            {".apk",    "application/vnd.android.package-archive"},
            {".asf",    "video/x-ms-asf"},
            {".avi",    "video/x-msvideo"},
            {".bin",    "application/octet-stream"},
            {".bmp",    "image/bmp"},
            {".c",  "text/plain"},
            {".class",  "application/octet-stream"},
            {".conf",   "text/plain"},
            {".cpp",    "text/plain"},
            {".doc",    "application/msword"},
            {".docx",   "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls",    "application/vnd.ms-excel"},
            {".xlsx",   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe",    "application/octet-stream"},
            {".gif",    "image/gif"},
            {".gtar",   "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h",  "text/plain"},
            {".htm",    "text/html"},
            {".html",   "text/html"},
            {".jar",    "application/java-archive"},
            {".java",   "text/plain"},
            {".jpeg",   "image/jpeg"},
            {".jpg",    "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log",    "text/plain"},
            {".m3u",    "audio/x-mpegurl"},
            {".m4a",    "audio/mp4a-latm"},
            {".m4b",    "audio/mp4a-latm"},
            {".m4p",    "audio/mp4a-latm"},
            {".m4u",    "video/vnd.mpegurl"},
            {".m4v",    "video/x-m4v"},
            {".mov",    "video/quicktime"},
            {".mp2",    "audio/x-mpeg"},
            {".mp3",    "audio/x-mpeg"},
            {".mp4",    "video/mp4"},
            {".mpc",    "application/vnd.mpohun.certificate"},
            {".mpe",    "video/mpeg"},
            {".mpeg",   "video/mpeg"},
            {".mpg",    "video/mpeg"},
            {".mpg4",   "video/mp4"},
            {".mpga",   "audio/mpeg"},
            {".msg",    "application/vnd.ms-outlook"},
            {".ogg",    "audio/ogg"},
            {".pdf",    "application/pdf"},
            {".png",    "image/png"},
            {".pps",    "application/vnd.ms-powerpoint"},
            {".ppt",    "application/vnd.ms-powerpoint"},
            {".pptx",   "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop",   "text/plain"},
            {".rc", "text/plain"},
            {".rmvb",   "audio/x-pn-realaudio"},
            {".rtf",    "application/rtf"},
            {".sh", "text/plain"},
            {".tar",    "application/x-tar"},
            {".tgz",    "application/x-compressed"},
            {".txt",    "text/plain"},
            {".wav",    "audio/x-wav"},
            {".wma",    "audio/x-ms-wma"},
            {".wmv",    "audio/x-ms-wmv"},
            {".wps",    "application/vnd.ms-works"},
            {".xml",    "text/plain"},
            {".z",  "application/x-compress"},
            {".zip",    "application/x-zip-compressed"},
            {"",        "*/*"}
    };
    private TextView txt;
    private EditText weburl;
    private WebView webHolder;
    private LinearLayout web_url_layout;
    private boolean isExit;
    private String current_url;
    //弹出菜单需要的变量
    private BottomMenu MyBottomMenu;
    private DBManager MyDBManager;
    private TextView progress;
    private HashMap<String,String> historyMap =new HashMap<String,String>();
    private HashMap<String,String> pagemarkMap =new HashMap<String,String>();
    private Handler handler;
    public class UIHander extends Handler{
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 1:
                    send();
                    break;
                case 2:
                    //openApk(MainActivity.this,saveFile);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("下载完成");
                    builder.setNegativeButton("取消", null);
                    builder.setPositiveButton("打开", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            File saveDir = Environment.getExternalStorageDirectory();
                            File saveFile = new File(saveDir,txt.getText().toString());
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction(Intent.ACTION_VIEW);
                            String type = getMIMEType(saveFile);
                            //设置intent的data和Type属性。
                            intent.setDataAndType(Uri.fromFile(saveFile), type);
                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(MainActivity.this,"您没有安装Office文件", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.show();
                    break;
            }
        }
    }
    public static String getMIMEType(File file) {
        String type ="*/*";
        String name = file.getName();
        int index = name.lastIndexOf('.');
        if (index < 0) {
            return type;
        }
        String end = name.substring(index,name.length()).toLowerCase();
        if (TextUtils.isEmpty(end)) return type;
        for (int i = 0;i < MIME_MapTable.length;i++) {
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }
    public static void openFile(Context context, File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type= getMIMEType(file);
        //设置intent的data和Type属性。
        intent.setDataAndType(Uri.fromFile(file), type);
        context.startActivity(intent);
    }
    public static void openApk(Context context,File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        bindViews();
        initWebView();
        MyBottomMenu=new BottomMenu(this);
        MyDBManager=new DBManager(this);
    }
    private class ButtonClickedListener implements View.OnClickListener {
        String url;
        public void onClick(View v) {
            switch(v.getId())
            {
                case R.id.btn_mark:
                    WebPage  k =getCurrentWebPage();
                    if(  k!=null &&  !pagemarkMap.containsKey(k.getName())) {
                        pagemarkMap.put(k.getName(), k.getURL());
                        MyDBManager.addPage(k,true);
                    }
                    else {
                        Toast.makeText(MainActivity.this,k.getName()+"已经存在",Toast.LENGTH_LONG).show();
                        break;
                    }
                    Toast.makeText(MainActivity.this,k.getName()+"添加成功",Toast.LENGTH_LONG).show();
                    break;
                case R.id.search_url:
                    url =  weburl.getText().toString();
                    if(URLUtil.isNetworkUrl(url) &&  URLUtil.isValidUrl(url))
                        webHolder.loadUrl(url);
                    else {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("警告")
                                .setMessage("不是有效的网址")
                                .create()
                                .show();
                    }
                    break;
                case R.id.btn_back:
                    webHolder.goBack();
                    break;
                case R.id.btn_move:
                    webHolder.goForward();
                    break;
                case R.id.btn_menu:
                    MyBottomMenu.show();
                    break;
                case R.id.btn_refresh:
                    webHolder.loadUrl(current_url);
                    Toast.makeText(MainActivity.this, "网页已经刷新", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btn_home:
                    webHolder.loadUrl("http://www.szu.edu.cn");
                    break;
            }
        }
    }
    public void  bindViews()
    {
        handler=new UIHander();
        progress=findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
        ButtonClickedListener buttonListener=new ButtonClickedListener();
        findViewById(R.id.btn_mark).setOnClickListener(buttonListener);
        findViewById(R.id.search_url).setOnClickListener(buttonListener);
        findViewById(R.id. btn_back).setOnClickListener(buttonListener);
        findViewById(R.id. btn_move).setOnClickListener(buttonListener);
        findViewById(R.id. btn_menu).setOnClickListener(buttonListener);
        findViewById(R.id. btn_refresh).setOnClickListener(buttonListener);
        findViewById(R.id. btn_home).setOnClickListener(buttonListener);
        txt=(TextView) findViewById(R.id.txt);
        weburl = (EditText) findViewById(R.id.web_url);
        weburl.setHorizontallyScrolling(true);
        current_url=weburl.getText().toString();
        webHolder = (WebView) findViewById(R.id.webshow);
        web_url_layout=(LinearLayout) findViewById(R.id.web_url_layout);
    }
    private void initWebView()
    {
        WebSettings websetting = webHolder.getSettings();
        websetting.setDomStorageEnabled(true);    //开启DOM形式存储
       // websetting.setDatabaseEnabled(true);   //开启数据库形式存储
       // String appCacheDir = getFilesDir().getAbsolutePath()+"myWeb";  //缓存数据的存储地址
       // websetting.setAppCachePath(appCacheDir);
       // websetting.setAppCacheEnabled(true);  //开启缓存功能
       // websetting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//缓存模式

        //启用JavaScript
        websetting.setJavaScriptEnabled(true);
        websetting.setJavaScriptCanOpenWindowsAutomatically(true);
        websetting.setSupportMultipleWindows(true);
        websetting.setLoadWithOverviewMode(true);   //自适应屏幕
        //启用WebView内置的缩放功能
        websetting.setBuiltInZoomControls(true);
        websetting.setDefaultTextEncodingName("gb2312");
        if (android.os.Build.VERSION.SDK_INT >=21) {
            websetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webHolder.setWebViewClient(new WebViewClient()
        {//支持超链接的功能
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                current_url=url;
                if(url.length()<50)
                weburl.setText(url);
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                WebPage k =getCurrentWebPage();
                if(  k!=null &&  !historyMap.containsKey(k.getName())) {
                    historyMap.put(k.getName(), k.getURL());
                    MyDBManager.addPage(k,false);
                }
                //隐藏地址栏
                //web_url_layout.setVisibility(View.GONE);
            }
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
            {
                handler.proceed();//接受证书
            }

        });
        webHolder.setWebChromeClient(new WebChromeClient() {
            //这里设置获取到的网站title
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if(title.length()<50)
                    txt.setText(title);
                else
                    txt.setText("网页名字");
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });

        webHolder.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadTask k=  new DownloadTask(MainActivity.this,progress,MyDBManager,handler);
                k.execute(url);
                Toast.makeText(MainActivity.this,"正在下载",Toast.LENGTH_SHORT).show();
            }
        });
        webHolder.loadUrl("http://www.szu.edu.cn");
    }
    public WebPage getCurrentWebPage()
    {
        if(current_url.length()>50  )   return null;
        String name=txt.getText().toString();
        return new WebPage(name,current_url,new SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(new Date()).toString());
    }
    // 回调的方式来获取指定Activity返回的结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0&&resultCode==0){
            Bundle bundle=data.getBundleExtra("bundle");
            String url= bundle.getString("url");
            webHolder.loadUrl(url);
            weburl.setText(url);
            System.out.println("回调的URL为"+bundle.getString("url"));
        }
        else if(requestCode==0&&resultCode==1)
            webHolder.loadUrl(current_url);
        super.onActivityResult(requestCode, resultCode, data);
    }
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    @Override
    public void onBackPressed() {
        //判断是否可后退，是则后退，否则按两次退出程序
        if(webHolder.canGoBack()){
            webHolder.goBack();
        }else{
            if(!isExit){
                isExit = true;
                Toast.makeText(getApplicationContext(), "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();
            }else{
                finish();
                System.exit(0);
            }
        }
    }
    protected void send() {
        String str[] = progress.getText().toString().split(" ");
        if(str.length<2)  return;
        String name = str[0];
        String pro = str[1];
        Intent intent = new Intent();
        intent.putExtra("name",name);
        intent.putExtra("progress",pro);
        intent.setAction("action");
        sendBroadcast(intent);
        Log.d("MainThread", "发送广播  当前进度"+pro);
    }
}
