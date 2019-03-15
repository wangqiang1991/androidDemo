package com.hande.goochao.views.components;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.hande.goochao.R;
import com.hande.goochao.utils.AppLog;

/**
 * Created by Wangem on 2018/3/9.
 */

public class ZoomView extends LinearLayout {

    private boolean cancelled;
    private boolean zoomInWaiting;
    private float scaleValue;
    private int duration;
    private int zoomViewId;
    private View zoomView;

    private OnClickListener listener;

    private ValueAnimator zoomOut;
    private ValueAnimator zoomIn;

    public ZoomView(@NonNull Context context) {
        super(context);
        initAttrs(context, null);
    }

    public ZoomView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public ZoomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    public ZoomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(context, attrs);
    }

    /**
     * 初始化资源文件
     *
     * @param context
     * @param attrs
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ZoomView);
            scaleValue = array.getFloat(R.styleable.ZoomView_scale, 0.95f);
            duration = array.getInteger(R.styleable.ZoomView_duration, 150);
            zoomViewId = array.getResourceId(R.styleable.ZoomView_scaleView, 0);
            // 回收
            array.recycle();
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (zoomViewId != 0) {
            zoomView = findViewById(zoomViewId);

            zoomOut = ValueAnimator.ofFloat(1f, scaleValue);
            zoomOut.setDuration(duration);
            zoomOut.addListener(new AnimationListener(zoomOut));
            zoomOut.addUpdateListener(new AnimationChangeListener());

            zoomIn = ValueAnimator.ofFloat(scaleValue, 1f);
            zoomIn.setDuration(duration);
            zoomIn.addListener(new AnimationListener(zoomIn));
            zoomIn.addUpdateListener(new AnimationChangeListener());
        }
    }

    private void onScale(float scale) {
        zoomView.setScaleX(scale);
        zoomView.setScaleY(scale);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (zoomView == null) {
            return super.onTouchEvent(event);
        }
        AppLog.i("zoom " + event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 放大缩小动画未完成就不重复执行动画
                if (zoomOut.isRunning() || zoomIn.isRunning()) {
                    return false;
                }
                cancelled = false;
                zoomInWaiting = false;
                beginAnimation();
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelled = true;
            case MotionEvent.ACTION_UP: {
                if (zoomOut.isRunning()) {
                    // 放大动画等待执行
                    zoomInWaiting = true;
                } else {
                    endAnimation();
                }
                break;
            }
        }
        return true;
    }

    private void beginAnimation() {
        zoomOut.start();
    }

    private void endAnimation() {
        zoomIn.start();
    }

    class AnimationChangeListener implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float currentScale = (float) animation.getAnimatedValue();
            onScale(currentScale);
        }
    }

    class AnimationListener implements Animator.AnimatorListener {

        private ValueAnimator animator;

        public AnimationListener(ValueAnimator animator) {
            this.animator = animator;
        }

        @Override
        public void onAnimationStart(Animator animation, boolean isReverse) {
            AppLog.i("zoom start 1");
        }

        @Override
        public void onAnimationEnd(Animator animation, boolean isReverse) {
            AppLog.i("zoom end 1");
            if (animator == zoomOut) {
                // 缩小动画执行完毕
                if (zoomInWaiting) {
                    endAnimation();
                }
            } else {
                // 放大动画执行完毕
                if (!cancelled && listener != null) {
                    // 触发事件
                    listener.onClick(ZoomView.this);
                }
            }
        }

        @Override
        public void onAnimationStart(Animator animation) {
            AppLog.i("zoom start");
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            AppLog.i("zoom end");
            if (animator == zoomOut) {
                // 缩小动画执行完毕
                if (zoomInWaiting) {
                    endAnimation();
                }
            } else {
                // 放大动画执行完毕
                if (!cancelled && listener != null) {
                    // 触发事件
                    listener.onClick(ZoomView.this);
                }
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}
