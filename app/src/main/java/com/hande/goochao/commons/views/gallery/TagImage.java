package com.hande.goochao.commons.views.gallery;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.hande.goochao.R;
import com.hande.goochao.utils.WindowUtils;

import java.util.ArrayList;
import java.util.List;

import static android.animation.ValueAnimator.REVERSE;

/**
 * Created by Wangem on 2018/3/12.
 */

@SuppressLint("AppCompatCustomView")
public class TagImage extends ImageView {

    public TagImage(Context context) {
        super(context);
    }

    public TagImage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TagImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TagImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 购买标签
     */
    public void setGoodsMode() {
        int size = WindowUtils.dpToPixels(getContext(), 33);
        this.setImageResource(R.mipmap.gm);
        this.setPivotX(size * 0.3197f);
        this.setPivotY(size * 0.0476f);
        ObjectAnimator animatorRotation = ObjectAnimator.ofFloat(this, "rotation", -18, 18);
        animatorRotation.setDuration(1500);
        animatorRotation.setRepeatCount(-1);
        animatorRotation.setRepeatMode(REVERSE);
        animatorRotation.start();
    }

    /**
     * 点赞标签
     */
    public void setPraiseMode() {
        this.setImageResource(R.mipmap.dz);
        ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(this, "scaleX", 0.8f, 1f);
        animatorScaleX.setRepeatCount(-1);
        animatorScaleX.setRepeatMode(REVERSE);
        animatorScaleX.setDuration(1500);
        animatorScaleX.start();

        ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(this, "scaleY", 0.8f, 1f);
        animatorScaleY.setRepeatCount(-1);
        animatorScaleY.setRepeatMode(REVERSE);
        animatorScaleY.setDuration(1500);
        animatorScaleY.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        this.clearAnimation();
        super.onDetachedFromWindow();
    }
}
