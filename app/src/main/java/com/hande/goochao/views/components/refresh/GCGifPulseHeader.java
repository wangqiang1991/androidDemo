package com.hande.goochao.views.components.refresh;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hande.goochao.R;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.components.LoadingView;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.getSize;
import static android.view.View.MeasureSpec.makeMeasureSpec;

/**
 * @author yanshen
 * @description 无
 * Created by yanshen on 2018/7/26.
 */
public class GCGifPulseHeader extends ViewGroup implements RefreshHeader {

    private SpinnerStyle mSpinnerStyle = SpinnerStyle.Translate;
    private GifImageView loadingView;
    private GifDrawable gifDrawable;
    private int gifWidth;
    private int gifHeight;

    public GCGifPulseHeader(Context context) {
        super(context);
        initView();
    }

    public GCGifPulseHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public GCGifPulseHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public GCGifPulseHeader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        gifWidth = WindowUtils.dpToPixels(getContext(), 60);
        gifHeight = WindowUtils.dpToPixels(getContext(), 60);
        loadingView = new GifImageView(getContext());
        try {
            gifDrawable = new GifDrawable(getResources(), R.drawable.loading_gray);
            gifDrawable.setLoopCount(0);
            gifDrawable.setSpeed(1.0f);
            loadingView.setImageDrawable(gifDrawable);
            gifDrawable.stop();
            addView(loadingView);
        } catch (IOException e) {
            AppLog.e(e.getMessage(), e);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpec = makeMeasureSpec(getSize(widthMeasureSpec), AT_MOST);
        int heightSpec = makeMeasureSpec(getSize(heightMeasureSpec), MeasureSpec.UNSPECIFIED);
        loadingView.measure(widthSpec, heightSpec);
        // 保存测量后的大小
        setMeasuredDimension(
                resolveSize((int) (gifWidth * 1.5), widthMeasureSpec),
                resolveSize((int) (gifHeight), heightMeasureSpec)
        );
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int p_width = getMeasuredWidth();
        int p_height = getMeasuredHeight();
        int c_width = gifWidth;
        int c_height = gifHeight;
        int left = p_width / 2 - c_width / 2;
        int top = p_height / 2 - c_height / 2;

        loadingView.layout(left, top, left + c_width, top + c_height);
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @NonNull
    @Override
    public SpinnerStyle getSpinnerStyle() {
        return mSpinnerStyle;
    }

    @Override
    public void setPrimaryColors(int... colors) {

    }

    @Override
    public void onInitialized(@NonNull RefreshKernel kernel, int height, int extendHeight) {

    }

    @Override
    public void onPulling(float percent, int offset, int height, int extendHeight) {
        System.out.println("on pulling");
    }

    @Override
    public void onReleasing(float percent, int offset, int height, int extendHeight) {
        System.out.println("on onReleasing");
    }

    @Override
    public void onReleased(RefreshLayout refreshLayout, int height, int extendHeight) {
        System.out.println("on onReleased");
    }

    @Override
    public void onStartAnimator(@NonNull RefreshLayout refreshLayout, int height, int extendHeight) {
        gifDrawable.start();
        System.out.println("on onStartAnimator");
    }

    @Override
    public int onFinish(@NonNull RefreshLayout refreshLayout, boolean success) {
        if (gifDrawable.isPlaying()) {
            gifDrawable.seekToFrame(0);
            gifDrawable.stop();
        }
        System.out.println("on onFinish");
        return 0;
    }

    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;
    }

    @Override
    public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {

    }
}
