package com.example.lenovo.web.Database;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {
    private DBHelper helper;
    private SQLiteDatabase db;

    public DBManager(Context context) {
        helper = new DBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }
    public void addPage(WebPage page,boolean type) {
        //通过type标志判断是收藏还是历史功能   true为收藏  false为历史
        db.beginTransaction();  //开始事务
        try {
                ContentValues cv = new ContentValues();
                cv.put("name", page.getName());
                cv.put("url", page.getURL());
                //插入ContentValues中的数据
                if(type)
                    db.insert("pagemark", null, cv);
                else {
                    cv.put("data",page.getDate());
                    db.insert("collection", null, cv);
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
    }
    public void updateName(WebPage page,boolean type) {
        ContentValues cv = new ContentValues();
        cv.put("name", page.getName());
        if(type)
            db.update("pagemark", cv, "url = ?", new String[]{page.getURL()});
        else
            db.update("collection", cv, "url = ?", new String[]{page.getURL()});
    }

    public void deleteWebPage(WebPage page,boolean type) {
        if(type)
            db.delete("pagemark", "name = ?", new String[]{page.getName()});
        else
            db.delete("collection", "name = ?", new String[]{page.getName()});
    }

    public LinkedList<WebPage> query(boolean type) {
        LinkedList<WebPage> pages = new LinkedList<WebPage>();
        Cursor c = queryTheCursor(type);
        while (c.moveToNext()) {
            WebPage page = new WebPage();
            page.setName(c.getString(c.getColumnIndex("name")));
            page.setURL(c.getString(c.getColumnIndex("url")));
            if(!type)
                page.setData(c.getString(c.getColumnIndex("data")));
            pages.add(page);
        }
        c.close();
        return pages;
    }

    public Cursor queryTheCursor(boolean type) {
        Cursor c;
        if(type)
            c = db.rawQuery("SELECT * FROM pagemark", null);
        else
            c = db.rawQuery("SELECT * FROM collection", null);
        return c;
    }
    public void closeDB() {
        db.close();
    }

    /**
     * 获得指定URI的每条线程已经下载的文件长度
     * @param path
     * @return
     * */
    public Map<Integer, Integer> getData(String path)
    {
        //根据下载的路径查询所有现场的下载数据,返回的Cursor指向第一条记录之前
        Cursor cursor = db.rawQuery("select threadid, downlength from filedownlog where downpath=?",
                new String[]{path});
        //建立一个哈希表用于存放每条线程已下载的文件长度
        Map<Integer,Integer> data = new HashMap<Integer, Integer>();
        while(cursor.moveToNext())
        {
            //把线程id与该线程已下载的长度存放到data哈希表中
            data.put(cursor.getInt(cursor.getColumnIndex("threadid")),
                    cursor.getInt(cursor.getColumnIndex("downlength")));
        }
        cursor.close();//关闭cursor,释放资源;
        return data;
    }

    /**
     * 保存每条线程已经下载的文件长度
     * @param path 下载的路径
     * @param map 现在的di和已经下载的长度的集合
     */
    public void save(String path,Map<Integer,Integer> map)
    {
        //开启事务,因为此处需要插入多条数据
        db.beginTransaction();
        try{
            //使用增强for循环遍历数据集合
            for(Map.Entry<Integer, Integer> entry : map.entrySet())
            {
                //插入特定下载路径特定线程ID已经下载的数据
                ContentValues cv = new ContentValues();
                cv.put("downpath",path);
                cv.put("threadid",  entry.getKey());
                cv.put("downlength",entry.getValue());
                db.insert("filedownlog", null, cv);
                /*
                db.execSQL("insert into filedownlog(downpath, threadid, downlength) values(?,?,?)",
                        new Object[]{path, entry.getKey(), entry.getValue()});*/
            }
            //设置一个事务成功的标志,如果成功就提交事务,如果没调用该方法的话那么事务回滚
            //就是上面的数据库操作撤销
            db.setTransactionSuccessful();
        }finally{
            //结束一个事务
            db.endTransaction();
        }
    }
    //实时更新每条线程已经下载的文件长度
    public void update(String path,int threadId,int pos)
    {
       db.execSQL("update filedownlog set downlength=? where downpath=? and threadid=?",
                new Object[]{pos, path, threadId});
    }


    /**
     *当文件下载完成后，删除对应的下载记录
     *@param path
     */
    public void delete(String path)
    {
        db.execSQL("delete from filedownlog where downpath=? ", new Object[]{path});
    }

}