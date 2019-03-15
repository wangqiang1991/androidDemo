package com.hande.goochao.views.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.wang.avi.AVLoadingIndicatorView;

/**
 * 解决AVLoadingIndicatorView在王恩茂那个华为手机上第一个点点在重复渲染的时候没有清理完，留了左边一部分没被清理掉
 * 修改invalidateDrawable方法中调用invalidate的left和top参数都设置为0就可以了
 * @author yanshen
 * @description 修改AVLoadingIndicatorView的bug
 * Created by yanshen on 2018/4/11.
 */
public class GCLoadingIndicatorView extends AVLoadingIndicatorView {
    public GCLoadingIndicatorView(Context context) {
        super(context);
    }

    public GCLoadingIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GCLoadingIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GCLoadingIndicatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void invalidateDrawable(Drawable dr) {
        if (verifyDrawable(dr)) {
            final Rect dirty = dr.getBounds();
            final int scrollX = getScrollX() + getPaddingLeft();
            final int scrollY = getScrollY() + getPaddingTop();
            invalidate(0, 0,
                    dirty.right + scrollX, dirty.bottom + scrollY);
        } else {
            super.invalidateDrawable(dr);
        }
    }
}
