package com.hande.goochao.views.widget.tablayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/12/6.
 */
public class RadiusFiveImageView extends android.support.v7.widget.AppCompatImageView {

    private int radius = 10;

    public RadiusFiveImageView(Context context) {
        this(context, null);
    }

    public RadiusFiveImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadiusFiveImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Path path = new Path();
        path.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), radius, radius, Path.Direction.CW);
        canvas.clipPath(path);//设置可显示的区域，canvas四个角会被剪裁掉
        super.onDraw(canvas);
    }
}

