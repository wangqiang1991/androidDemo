package com.hande.goochao.views.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.hande.goochao.R;

/**
 * Created by LMC on 2018/3/7.
 */

public class MyDialog extends Dialog {
    //在构造方法里预加载我们的样式，这样就不用每次创建都指定样式了
    public MyDialog(Context context) {
        this(context, R.style.MyDialog);
    }

    public MyDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 预先设置Dialog的一些属性
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        dialogWindow.setAttributes(p);
    }
}