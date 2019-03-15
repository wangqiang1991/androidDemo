package com.hande.goochao.views.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import com.hande.goochao.utils.AppLog;

/**
 * Created by Wangem on 2018/3/2.
 */

public class InsideWebView extends WebView {

    public InsideWebView(Context context) {
        super(context);
        init();
    }

    public InsideWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InsideWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public InsideWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public InsideWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
        init();
    }

    public void init() {
        requestDisallowInterceptTouchEvent(true);
//        this.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP)
//                    getParent().requestDisallowInterceptTouchEvent(false);
//                else
//                    getParent().requestDisallowInterceptTouchEvent(true);
//                return false;
//            }
//        });
    }

    /*@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        super.onInterceptTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return false;
    }*/
}
