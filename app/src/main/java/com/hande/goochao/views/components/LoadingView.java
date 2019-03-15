package com.hande.goochao.views.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.hande.goochao.R;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * @author Wangenmao
 * @description 加载提示View
 * Created by Wangenmao on 2018/7/25.
 */

public class LoadingView extends FrameLayout {

    @ViewInject(R.id.imageView)
    private GifImageView imageView;

    private GifDrawable gifDrawable;

    public LoadingView(@NonNull Context context) {
        super(context);
        init();
    }

    public LoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LoadingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    /**
     * 功能：初始化页面组件
     */
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_loading, this, true);
        x.view().inject(this);
        try {
            gifDrawable = new GifDrawable(getResources(), R.drawable.loading_gray);
            gifDrawable.setLoopCount(0);
            gifDrawable.setSpeed(1.0f);
            imageView.setImageDrawable(gifDrawable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            if (!gifDrawable.isPlaying()) {
                gifDrawable.start();
            }
        } else if (visibility == View.GONE){
            gifDrawable.stop();
        }
        super.setVisibility(visibility);
    }
}
