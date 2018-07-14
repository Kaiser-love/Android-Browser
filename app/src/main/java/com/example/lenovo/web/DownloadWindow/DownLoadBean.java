package com.example.lenovo.web.DownloadWindow;

public class DownLoadBean {
    private String name;
    private String progress;
    public  DownLoadBean(String name,String progress)
    {
        this.name=name;
        this.progress=progress;
    }
    public String print()
    {
        return name+"------------------------下载进度： "+progress+"%";
    }
    public String getName()
    {
        return name;
    }
    public String getProgress()
    {
        return progress;
    }
    public void setProgress(String progress)
    {
        this.progress=progress;
    }
}
