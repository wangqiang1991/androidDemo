package com.hande.goochao.views.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;


/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/11/9.
 */
public class RoundImageView extends android.support.v7.widget.AppCompatImageView {


    float width,height;

    public RoundImageView(Context context) {
        this(context, null);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (Build.VERSION.SDK_INT < 18) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (width > 20 && height > 20) {
            Path path = new Path();
            path.moveTo(20, 0);
            path.lineTo(width - 0, 0);
            path.quadTo(width, 0, width, 0);  // 右上
            path.lineTo(width, height - 0);
            path.quadTo(width, height, width - 0, height);  //右下
            path.lineTo(20, height);
            path.quadTo(0, height, 0, height - 20);
            path.lineTo(0, 20);
            path.quadTo(0, 0, 20, 0);
            canvas.clipPath(path);
        }

        super.onDraw(canvas);
    }

}
