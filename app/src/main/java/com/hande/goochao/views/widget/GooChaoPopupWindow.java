package com.hande.goochao.views.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.PopupWindow;

/**
 * @author Wangenmao
 * @description /n
 * Created by Wangenmao on 2018/7/26.
 */

public class GooChaoPopupWindow extends PopupWindow {


    public GooChaoPopupWindow(Context context) {
        super(context);
    }

    public GooChaoPopupWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GooChaoPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GooChaoPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public GooChaoPopupWindow() {
    }

    public GooChaoPopupWindow(View contentView) {
        super(contentView);
    }

    public GooChaoPopupWindow(int width, int height) {
        super(width, height);
    }

    public GooChaoPopupWindow(View contentView, int width, int height) {
        super(contentView, width, height);
    }

    public GooChaoPopupWindow(View contentView, int width, int height, boolean focusable) {
        super(contentView, width, height, focusable);
    }

    @Override
    public void showAsDropDown(View anchor) {
        if(Build.VERSION.SDK_INT == 24) {
            Rect rect = new Rect();
            anchor.getGlobalVisibleRect(rect);
            int h = anchor.getResources().getDisplayMetrics().heightPixels - rect.bottom;
            setHeight(h);
        }
        super.showAsDropDown(anchor);
    }
}
