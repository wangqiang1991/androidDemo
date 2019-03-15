package com.hande.goochao.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;

/**
 * @author Wangenmao
 * @description Created by Wangenmao on 2018/7/12.
 */

public class HiddenAnimUtils {

    private int mHeight;//伸展高度

    private int mEndHeight; //结束高度

    private View destView;//需要展开隐藏的布局

    private AnimateEvent animateEvent = null;

    public interface AnimateEvent {
        void animateEnd();
        void animateCancel();
    }

    /**
     * 获取实例(可根据自己需要修改传参)
     *
     * @param height   控件的高度
     * @param destView 需要隐藏或显示的布局view
     */
    public static HiddenAnimUtils getInstance(int endHeight, int height, View destView, AnimateEvent event) {
        return new HiddenAnimUtils(endHeight, height, destView, event);
    }

    private HiddenAnimUtils(int endHeight, int height, View hideView, AnimateEvent event) {
        this.animateEvent = event;
        this.destView = hideView;
        this.mHeight = height;
        this.mEndHeight = endHeight;
    }


    public void openWithAnim() {
        destView.setVisibility(View.VISIBLE);
        ValueAnimator animator = createDropAnimator(destView, mEndHeight, mHeight);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (animateEvent != null) {
                    animateEvent.animateEnd();
                }
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (animateEvent != null) {
                    animateEvent.animateCancel();
                }
                super.onAnimationCancel(animation);
            }
        });
        animator.start();
    }

    public void closeWithAnimate() {
        ValueAnimator animator = createDropAnimator(destView, mHeight, mEndHeight);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                destView.setVisibility(View.GONE);
                if (animateEvent != null) {
                    animateEvent.animateEnd();
                }
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (animateEvent != null) {
                    animateEvent.animateCancel();
                }
                super.onAnimationCancel(animation);
            }
        });
        animator.start();
    }

    private ValueAnimator createDropAnimator(final View v, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                int value = (int) arg0.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.height = value;
                v.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    public static void slideview(final float fromXDelta, final float toXDelta, final View view) {
        TranslateAnimation animation = new TranslateAnimation(fromXDelta, toXDelta, 0, 0);
//        animation.setInterpolator(new OvershootInterpolator());
        animation.setDuration(300);
//        animation.setStartOffset(0);
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                int left = view.getLeft() + (int) (toXDelta - fromXDelta);
//                int top = view.getTop();
//                int width = view.getWidth();
//                int height = view.getHeight();
//                view.layout(left, top, left + width, top + height);
            }
        });
        view.startAnimation(animation);
    }
}
