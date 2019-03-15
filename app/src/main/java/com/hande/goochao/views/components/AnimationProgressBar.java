/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan. 
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna. 
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus. 
 * Vestibulum commodo. Ut rhoncus gravida arcu. 
 */

package com.hande.goochao.views.components;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.hande.goochao.R;
import com.wang.avi.AVLoadingIndicatorView;

/**
 * 三点花动画
 */
public class AnimationProgressBar extends Dialog {

    private AsyncTask task;
    private AVLoadingIndicatorView loading;
    private RelativeLayout animationProgress;

    private int offsetX, offsetY;

    public AnimationProgressBar(Context context) {
        this(context, R.style.ProgressDialogTheme);
    }

    public AnimationProgressBar(Context context, int theme) {
        super(context, theme);
    }

    protected AnimationProgressBar(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_animation_progress, null); // 加载自己定义的布局
        loading = (AVLoadingIndicatorView) view.findViewById(R.id.loading);
        animationProgress = (RelativeLayout) view.findViewById(R.id.animation_progress);
        setContentView(view);// 为Dialoge设置自己定义的布局
        setCancelable(true);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.dimAmount = 0.0f; // 去背景遮盖  
        lp.alpha = 1f;

        lp.x = offsetX;
        lp.y = offsetY;

        getWindow().setAttributes(lp);

        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (task != null) task.cancel(true);
            }
        });
    }

    /**
     * 设置偏移量
     * @param offsetX
     * @param offsetY
     */
    public void setOffset(int offsetX, int offsetY){
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void hideAnimationBar () {
        animationProgress.setVisibility(View.GONE);
    }

    public void showAnimationBar () {
        animationProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void show() {
        super.show();
    }

    public <X, Y, Z> void show(AsyncTask<X, Y, Z> task, X... params) {
        show();
        this.task = task;
        task.execute(params);
    }
}
