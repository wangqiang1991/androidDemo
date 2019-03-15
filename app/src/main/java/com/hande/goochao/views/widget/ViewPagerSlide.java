package com.hande.goochao.views.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/7/17.
 */
public class ViewPagerSlide extends ViewPager {
    //是否可以进行滑动
    private boolean isSlide = false;

    private boolean isAnimation = true;

    public void setSlide(boolean slide) {
        isSlide = slide;
    }

    public void setAnimation(boolean animation){
        isAnimation = animation;
    }

    public ViewPagerSlide(Context context) {
        super(context);
    }

    public ViewPagerSlide(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isSlide;
    }

    //去除页面切换时的滑动翻页效果
    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        // TODO Auto-generated method stub
        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    public void setCurrentItem(int item) {
        if (isAnimation){
            super.setCurrentItem(item, true);
        }else {
            // TODO Auto-generated method stub
            super.setCurrentItem(item, false);
        }
    }
}
