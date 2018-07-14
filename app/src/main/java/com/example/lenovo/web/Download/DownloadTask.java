package com.example.lenovo.web.Download;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.lenovo.web.Database.DBManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadTask extends AsyncTask<String, Integer, Void> {
    private Context context;            //程序的上下文对象
    private TextView progress;
    private int downloadedSize = 0;               //已下载的文件长度
    private int fileSize=0;
    private DBManager manager;
    private DownloadThread[] threads;        //根据线程数设置下载的线程池
    private File saveFile;              //数据保存到本地的文件中
    private HashMap<Integer, Integer> data = new HashMap<Integer, Integer>();  //缓存个条线程的下载的长度
    private int block;                            //每条线程下载的长度
    private String downloadUrl;                   //下载的路径
    private String filename;
    private Handler handler ;
    public DownloadTask(Context a,TextView progress,DBManager c,Handler handler)
    {
        context=a;
        manager=c;
        this.progress=progress;
        this.handler=handler;
    }
    protected Void doInBackground(String... strings) {
        init(strings[0]);
        download();
        return null;
    }
    private void init(String theurl)
    {
        try {
            downloadUrl = theurl;
            File saveDir = Environment.getExternalStorageDirectory();
            if (!saveDir.exists()) saveDir.mkdir();  //如果文件不存在的话指定目录,这里可创建多层目录
            threads = new DownloadThread[4];   //根据下载的线程数量创建下载的线程池
            URL url = new URL(downloadUrl);     //根据下载路径实例化URL

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();   //创建远程连接句柄,这里并未真正连接
            conn.setConnectTimeout(5000);      //设置连接超时事件为5秒
            conn.setRequestMethod("GET");      //设置请求方式为GET
            //设置用户端可以接收的媒体类型
            conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, " +
                    "image/pjpeg, application/x-shockwave-flash, application/xaml+xml, " +
                    "application/vnd.ms-xpsdocument, application/x-ms-xbap," +
                    " application/x-ms-application, application/vnd.ms-excel," +
                    " application/vnd.ms-powerpoint, application/msword, */*");

            conn.setRequestProperty("Accept-Language", "zh-CN");  //设置用户语言
            conn.setRequestProperty("Referer", downloadUrl);    //设置请求的来源页面,便于服务端进行来源统计
            conn.setRequestProperty("Charset", "UTF-8");    //设置客户端编码
            //设置用户代理
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; " +
                    "Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727;" +
                    " .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");

            conn.setRequestProperty("Connection", "Keep-Alive");  //设置connection的方式
            conn.connect();      //和远程资源建立正在的链接,但尚无返回的数据流

//对返回的状态码进行判断,用于检查是否请求成功,返回200时执行下面的代码
            if(conn.getResponseCode() == 200)
            {
                this.fileSize = conn.getContentLength();  //根据响应获得文件大小
                if(this.fileSize <= 0)throw new RuntimeException("不知道文件大小");  //文件长度小于等于0时抛出运行时异常
                filename = getFileName(conn);      //获取文件名称
                this.saveFile = new File(saveDir,filename);  //根据文件保存目录和文件名保存文件
                if(!saveFile.exists()) {
                    print("不存在");
                    saveFile.createNewFile();
                }
                Map<Integer,Integer> logdata = manager.getData(downloadUrl);    //获取下载记录
                //如果存在下载记录
                print(String.valueOf(logdata.size()));
                if(logdata.size() > 0)
                {
                    //遍历集合中的数据,把每条线程已下载的数据长度放入data中
                    for(Map.Entry<Integer, Integer> entry : logdata.entrySet())
                    {
                        print(entry.getKey()+" "+entry.getValue());
                        data.put(entry.getKey(), entry.getValue());
                    }
                }
                //如果已下载的数据的线程数和现在设置的线程数相同时则计算所有现场已经下载的数据总长度
                if(this.data.size() == this.threads.length)
                {
                    //遍历每条线程已下载的数据
                    for(int i = 0;i < this.threads.length;i++)
                    {
                        this.downloadedSize += this.data.get(i);
                    }
                    print("已下载的长度" + this.downloadedSize + "个字节");
                }
                //使用条件运算符求出每个线程需要下载的数据长度
                this.block = (this.fileSize % this.threads.length) == 0?
                        this.fileSize / this.threads.length:
                        this.fileSize / this.threads.length + 1;
                print(String.valueOf("每一块大小"+block));
            }else{
                //打印错误信息
                print("服务器响应错误:");
                // System.out.println("服务器响应错误:" + conn.getResponseCode() + conn.getResponseMessage());
                throw new RuntimeException("服务器反馈出错");
            }
        }
        catch (Exception e){print(e.toString());}
    }

    public void download()
    {
        try {
            print("开始下载"+this.data.size());
            print(saveFile.getName());
            RandomAccessFile randOut = new RandomAccessFile(this.saveFile, "rwd");
            print("打开文件"+this.threads.length);
            //设置文件大小
            if(this.fileSize>0) randOut.setLength(this.fileSize);
            randOut.close();    //关闭该文件,使设置生效
            URL url = new URL(this.downloadUrl);
            if(this.data.size() != this.threads.length){
                //如果原先未曾下载或者原先的下载线程数与现在的线程数不一致
                this.data.clear();
                //遍历线程池
                for (int i = 0; i < this.threads.length; i++) {
                    this.data.put(i, 0);//初始化每条线程已经下载的数据长度为0
                }
                manager.delete(this.downloadUrl);
                //如果存在下载记录，删除它们，然后重新添加
                manager.save(this.downloadUrl, this.data);
                //把下载的实时数据写入数据库中
                this.downloadedSize = 0;   //设置已经下载的长度为0
            }
            for (int i = 0; i < this.threads.length; i++) {//开启线程进行下载
                int downLength = this.data.get(i);
                //通过特定的线程id获取该线程已经下载的数据长度
                //判断线程是否已经完成下载,否则继续下载
                if(downLength < this.block && this.downloadedSize<this.fileSize){
                    //初始化特定id的线程
                    this.threads[i] = new DownloadThread(this, url, this.saveFile, this.block, this.data.get(i), i);
                    //设置线程优先级,Thread.NORM_PRIORITY = 5;
                    //Thread.MIN_PRIORITY = 1;Thread.MAX_PRIORITY = 10,数值越大优先级越高
                    this.threads[i].setPriority(7);
                    this.threads[i].start();    //启动线程
                }else{
                    this.threads[i] = null;   //表明线程已完成下载任务
                }
            }
            boolean notFinish = true;
            //下载未完成
            while (notFinish) {
                // 循环判断所有线程是否完成下载
                Thread.sleep(900);
                notFinish = false;
                //假定全部线程下载完成
                for (int i = 0; i < this.threads.length; i++){
                    if (this.threads[i] != null && !this.threads[i].isFinish()) {
                        //如果发现线程未完成下载
                        notFinish = true;
                    }
                }
            }
            //通知目前已经下载完成的数据长度
            print("下载完成");
            Message ms=new Message();
            progress.setText(filename);
            ms.what=2;
            handler.sendMessage(ms);
            if(downloadedSize == this.fileSize) manager.delete(this.downloadUrl);
            //下载完成删除记录
        } catch (Exception e) {
            print(e.toString());
        }
    }
    /**
     * 获取文件名
     * */
    private String getFileName(HttpURLConnection conn)
    {
        //从下载的路径的字符串中获取文件的名称
        String filename = this.downloadUrl.substring(this.downloadUrl.lastIndexOf('/') + 1);
        if(filename == null || "".equals(filename.trim())){     //如果获取不到文件名称
            for(int i = 0;;i++)  //使用无限循环遍历
            {
                String mine = conn.getHeaderField(i);     //从返回的流中获取特定索引的头字段的值
                if (mine == null) break;          //如果遍历到了返回头末尾则退出循环
                //获取content-disposition返回字段,里面可能包含文件名
                if("content-disposition".equals(conn.getHeaderFieldKey(i).toLowerCase())){
                    //使用正则表达式查询文件名
                    Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
                    if(m.find()) return m.group(1);    //如果有符合正则表达式规则的字符串,返回
                }
            }
            filename = UUID.randomUUID()+ ".tmp";//如果都没找到的话,默认取一个文件名
            //由网卡标识数字(每个网卡都有唯一的标识号)以及CPU时间的唯一数字生成的一个16字节的二进制作为文件名
        }
        return filename;
    }
    /**
     * 累计已下载的大小
     * 使用同步锁来解决并发的访问问题
     * */
    protected synchronized void append(int size)
    {
        //把实时下载的长度加入到总的下载长度中
        downloadedSize += size;
        publishProgress(downloadedSize) ;
    }
    /**
     * 更新指定线程最后下载的位置
     * @param threadId 线程id
     * @param pos 最后下载的位置
     * */
    protected synchronized void update(int threadId,int pos)
    {
        //把指定线程id的线程赋予最新的下载长度,以前的值会被覆盖掉
        this.data.put(threadId, pos);
        //更新数据库中制定线程的下载长度
        this.manager.update(this.downloadUrl, threadId, pos);
        print("更新:  线程 "+threadId+"   "+pos);
    }
    private static void print(String msg) {
        Log.i("Task", msg);
    }

    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values[0]);
        float num = (float)downloadedSize/fileSize;
        int result = (int)(num * 100);     //把获取的浮点数计算结果转换为整数
        //progress.setText("下载进度："+result+ "%");
        progress.setText(filename+" "+result);
        Message ms=new Message();
        ms.what=1;
        handler.sendMessage(ms);
    }
}
