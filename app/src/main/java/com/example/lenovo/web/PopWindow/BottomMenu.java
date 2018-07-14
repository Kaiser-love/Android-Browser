package com.example.lenovo.web.PopWindow;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import com.example.lenovo.web.DownloadWindow.DownloadActivity;
import com.example.lenovo.web.R;

public class BottomMenu implements View.OnClickListener,View.OnTouchListener {
    //弹出菜单需要的变量
    private PopupWindow popupWindow;
    private View contentView;
    private Activity mContext;
    public BottomMenu(Activity context)
    {
        mContext=context;
        showPopwindow();
        bindViews();
    }
    private void bindViews()
    {
        contentView.findViewById(R.id.btn_page).setOnClickListener(this);
        contentView.findViewById(R.id.btn_sendmail).setOnClickListener(this);
        contentView.findViewById(R.id.btn_download).setOnClickListener(this);
    }
    private void showPopwindow()
    {
        //加载弹出框的布局
        contentView = LayoutInflater.from(mContext).inflate(
                R.layout.menu_layout, null);
        popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);// 取得焦点
        //点击外部消失
        popupWindow.setOutsideTouchable(true);
        //设置可以点击
        popupWindow.setTouchable(true);
        //进入退出的动画，指定刚才定义的style
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        // 按下android回退物理键 PopipWindow消失解决
    }
   public void show()
   {
       //将PopupWindow显示在容器的最底部居中
       popupWindow.showAtLocation(contentView, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
   }
    @Override
    public void onClick(View v) {
        Intent intent;
            switch (v.getId()) {
                case R.id.btn_page:
                    intent=new Intent(mContext,FavAndHisActivity.class);
                    //启动CitysActivity 请求码为0
                    mContext.startActivityForResult(intent,0);
                    break;
                case R.id.btn_sendmail:
                    intent=new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain"); //模拟器请使用这行
                    intent.putExtra(Intent.EXTRA_EMAIL,
                            new String[] { "FxMarginTrading@feib.com.tw" });
                    intent.putExtra(Intent.EXTRA_SUBJECT, "您的建议");
                    intent.putExtra(Intent.EXTRA_TEXT, "我们很希望能得到您的建议！！！");
                    //intent.setType("message/rfc822"); // 真机上使用这行
                    mContext.startActivity(Intent.createChooser(intent, "请选择邮件类应用"));
                    break;
                case R.id.btn_download:
                    intent=new Intent(mContext, DownloadActivity.class);
                    mContext.startActivityForResult(intent,0);
                    break;
            }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int height = contentView.findViewById(R.id.pop_layout).getTop();
        int y=(int) event.getY();
        if(event.getAction()==MotionEvent.ACTION_UP){
            if(y<height){
                popupWindow. dismiss();
            }
        }
        return true;
    }
}
