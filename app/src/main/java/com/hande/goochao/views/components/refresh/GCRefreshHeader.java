package com.hande.goochao.views.components.refresh;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hande.goochao.R;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.getSize;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author wangqinag
 * @description 无
 * Created by wangqiang on 2018/4/9.
 */

public class GCRefreshHeader extends ViewGroup implements RefreshHeader {

    private LinearLayout view;
    private ImageView animateImageView;
    private int spaceHeight = 150;
    private int index = 0;

    private AnimationDrawable drawable;


    private final int[] refreshSources = new int[99];

    public GCRefreshHeader(@NonNull Context context) {
        super(context);
        initView(context, null, 0);
    }

    public GCRefreshHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0);
    }

    public GCRefreshHeader(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        view = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.refresh_head_view,this,false);
        animateImageView = view.findViewById(R.id.animate_image);
        animateImageView.setVisibility(INVISIBLE);
        addView(view, WRAP_CONTENT, spaceHeight);
        setMinimumHeight(DensityUtil.dp2px(60));

        //drawable = (AnimationDrawable) getResources().getDrawable(R.drawable.refresh_animate);

        String drawableId = "refresh_0000";
        for (int i = 0; i <= 98; i++) {
            String s = (String) drawableId + i;
            refreshSources[i] = getResources().getIdentifier(s,"drawable",context.getPackageName());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpec = makeMeasureSpec(getSize(widthMeasureSpec), AT_MOST);
        int heightSpec = makeMeasureSpec(getSize(heightMeasureSpec), MeasureSpec.UNSPECIFIED);

        view.measure(widthSpec, spaceHeight);
        setMeasuredDimension(
                resolveSize(view.getMeasuredWidth(), widthMeasureSpec),
                resolveSize(spaceHeight, heightMeasureSpec) + DensityUtil.dp2px(0)
        );
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int p_width = getMeasuredWidth();
        int p_height = getMeasuredHeight();
        int c_width = view.getMeasuredWidth();
        int c_height = view.getMeasuredHeight();
        int left = p_width / 2 - c_width / 2;
        int top = p_height / 2 - c_height / 2;

        view.layout(left, 0, left + c_width, top + c_height);
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @NonNull
    @Override
    public SpinnerStyle getSpinnerStyle() {
        return null;
    }

    @Override
    public void setPrimaryColors(int... colors) {

    }

    @Override
    public void onInitialized(@NonNull RefreshKernel kernel, int height, int extendHeight) {

    }

    @Override
    public void onPulling(float percent, int offset, int height, int extendHeight) {
        animateImageView.setVisibility(VISIBLE);

        if( index >= refreshSources.length -1){
            index = 0;
        }

        if( percent < 1 ){
            LayoutParams lp = animateImageView.getLayoutParams();
            lp.height = (int) (spaceHeight * percent);
            animateImageView.setLayoutParams(lp);
        }

        index++;
        animateImageView.setImageResource(refreshSources[index]);
    }

    @Override
    public void onReleasing(float percent, int offset, int height, int extendHeight) {
        if (percent >= 1) {
            percent = 1;
        }
        LayoutParams lp = animateImageView.getLayoutParams();
        lp.height = (int) (spaceHeight * percent);
        animateImageView.setLayoutParams(lp);
        if( percent <= 0.5){
            animateImageView.setVisibility(INVISIBLE);
            index = 0;
        }
    }

    @Override
    public void onReleased(RefreshLayout refreshLayout, int height, int extendHeight) {
//        drawable.stop();
//        animateImageView.setImageDrawable(drawable);
//        drawable.start();
    }

    @Override
    public void onStartAnimator(@NonNull RefreshLayout refreshLayout, int height, int extendHeight) {

    }

    @Override
    public int onFinish(@NonNull RefreshLayout refreshLayout, boolean success) {

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
