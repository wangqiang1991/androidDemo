package com.hande.goochao.views.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/7/11.
 */
public class HeightLayout extends LinearLayout {
    public HeightLayout(Context context) {
        super(context);
    }

    public HeightLayout(Context context , AttributeSet attrs) {
        super(context , attrs);
    }

    public void setView(int height){
        this.setLayoutParams(new LinearLayout.LayoutParams(this.getWidth() , height));
    }
}
