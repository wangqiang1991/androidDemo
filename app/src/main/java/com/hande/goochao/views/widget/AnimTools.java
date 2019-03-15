package com.hande.goochao.views.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;


/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/7/11.
 */
public class AnimTools {

    public static void anim(final HeightLayout view, int startHeight, int endHeight, int duration){
        ObjectAnimator anim = ObjectAnimator.ofInt(view, "view", startHeight,endHeight ).setDuration(duration);
        anim.start();
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int cVal = (int) valueAnimator.getAnimatedValue();
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
                lp.height = cVal;
                view.setLayoutParams(lp);
            }
        });
    }
}
