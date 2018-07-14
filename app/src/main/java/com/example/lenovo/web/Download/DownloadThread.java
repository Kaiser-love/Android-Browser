package com.example.lenovo.web.Download;

import com.example.lenovo.web.Download.DownloadTask;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadThread extends Thread{
    private boolean finish = false;         //该线程是否完成下载的标志
    private int downLength;                 //该线程已下载的数据长度
    private DownloadTask downloadTask;
    private URL downUrl;                    //下载的URL
    private int block;                      //每条线程下载的大小
    private int threadId = -1;              //初始化线程id设置
    private File saveFile;              //下载的数据保存到的文件
    public DownloadThread(DownloadTask downloadTask, URL downUrl, File saveFile, int block, int downLength, int id)
    {
        this.downloadTask=downloadTask;
        this.downUrl=downUrl;
        this.saveFile=saveFile;
        this.block=block;
        this.downLength=downLength;
        this.threadId=id;
    }
    public void run() {
        if (downLength < block) {//未下载完成
            try {
                HttpURLConnection http = (HttpURLConnection) downUrl.openConnection();
                http.setConnectTimeout(5 * 1000);
                http.setRequestMethod("GET");
                http.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
                http.setRequestProperty("Accept-Language", "zh-CN");
                http.setRequestProperty("Referer", downUrl.toString());
                http.setRequestProperty("Charset", "UTF-8");
                int startPos = block * threadId  + downLength;//开始位置 downLength为已经下载的长度
                int endPos = block * (threadId+1) - 1;//结束位置
                http.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);//设置获取实体数据的范围
                RandomAccessFile threadfile = new RandomAccessFile(this.saveFile, "rwd");
                threadfile.seek(startPos);
                http.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
                http.setRequestProperty("Connection", "Keep-Alive");

                InputStream inStream = http.getInputStream();     //获得远程连接的输入流
                byte[] buffer = new byte[1024];           //设置本地数据的缓存大小为1MB
                int offset = 0;                   //每次读取的数据量
                System.out.println("Thread " + this.threadId + " start download from position " + startPos);  //打印该线程开始下载的位置
                //用户没有要求停止下载,同时没有达到请求数据的末尾时会一直循环读取数据
                while ((offset = inStream.read(buffer, 0, 1024)) != -1) {
                    threadfile.write(buffer, 0, offset);          //直接把数据写入到文件中
                    downLength += offset;             //把新线程已经写到文件中的数据加入到下载长度中
                    downloadTask.update(this.threadId, downLength); //把该线程已经下载的数据长度更新到数据库和内存哈希表中
                    downloadTask.append(offset);            //把新下载的数据长度加入到已经下载的数据总长度中
                }
                threadfile.close();
                inStream.close();
                System.out.println("Thread " + this.threadId + " download finish");
                this.finish = true;                               //设置完成标记为true,无论下载完成还是用户主动中断下载
            } catch (Exception e) {
                this.downLength = -1;               //设置该线程已经下载的长度为-1
                System.out.println("Thread " + this.threadId + ":" + e);
            }
        }
    }

    /**
     * 已经下载的内容大小
     * @return 如果返回值为-1,代表下载失败
     */
    public long getDownLength() {
        return downLength;
    }
    /**
     * 下载是否完成
     * @return
     */
    public boolean isFinish() {
        return finish;
    }

}
