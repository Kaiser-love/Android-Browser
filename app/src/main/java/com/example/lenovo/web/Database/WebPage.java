package com.example.lenovo.web.Database;

public class WebPage{
    private String name;
    private String theurl;
    private String date="";
    public WebPage()
    {

    }
    public WebPage(String a,String b,String c)
    {
        name=a;
        theurl=b;
        date=c;
    }
    public String getName()
    {
        return name;
    }
    public String getURL()
    {
        return theurl;
    }
    public String getDate(){return date;}
    public void setName(String a)
    {
        name=a;
    }
    public void setURL(String a)
    {
        theurl=a;
    }
    public void setData(String a){date=a;}
}
