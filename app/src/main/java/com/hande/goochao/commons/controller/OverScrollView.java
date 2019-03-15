package com.hande.goochao.commons.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

/**
 * 阻尼效果
 * 弹簧效果
 * Created by limengyi on 16/3/20.
 */
public class OverScrollView extends ScrollView {

    private int mMaxYOverscrollDistance;
    private int mLastMotionY;
    private int mTouchSlop;

    public OverScrollView(Context context) {
        super(context);
        initBounceListView(context);
    }

    public OverScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBounceListView(context);
    }

    public OverScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBounceListView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OverScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initBounceListView(context);
    }


    private void initBounceListView(Context context)
    {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop() * 10;

        setOverScrollMode(OVER_SCROLL_ALWAYS);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float density = metrics.density;

        mMaxYOverscrollDistance = (int) (density * 100);
    }


    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int
            scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX,
                mMaxYOverscrollDistance, isTouchEvent);
    }

    /*@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean mIsBeingDragged = super.onInterceptTouchEvent(ev);
        Log.i("edasApp", ev.getAction() + "  onInterceptTouchEvent  " + mIsBeingDragged);
        int action = ev.getAction();
        if(action == MotionEvent.ACTION_DOWN) {
            mLastMotionY = (int) ev.getY();
        }
        if (!mIsBeingDragged) {
            final int y = (int) ev.getY();
            final int yDiff = Math.abs(y - mLastMotionY);
            Log.i("edasApp", "onInterceptTouchEvent  " + yDiff);
            if(yDiff > 50) {
                Log.i("edasApp", "o0000");
                return true;
            }
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            Field field = getClass().getSuperclass().getDeclaredField("mIsBeingDragged");
            field.setAccessible(true);
            field.set(this, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("edasApp", "", e);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e("edasApp", "", e);
        }
        return super.onTouchEvent(ev);
    }*/
}
