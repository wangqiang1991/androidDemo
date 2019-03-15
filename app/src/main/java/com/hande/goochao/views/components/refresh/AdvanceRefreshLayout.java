package com.hande.goochao.views.components.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;

/**
 * Created by LMC on 2018/3/1.
 */

public class AdvanceRefreshLayout extends SmartRefreshLayout {

    private OnTouchListener onTouchListener;

    public AdvanceRefreshLayout(Context context) {
        super(context);
    }

    public AdvanceRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdvanceRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AdvanceRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public OnTouchListener getOnTouchListener() {
        return onTouchListener;
    }

    @Override
    public void setOnTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (onTouchListener != null) {
            onTouchListener.onTouch(this, event);
        }
        return super.dispatchTouchEvent(event);
    }
}
