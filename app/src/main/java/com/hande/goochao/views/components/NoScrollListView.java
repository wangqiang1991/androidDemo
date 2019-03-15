package com.hande.goochao.views.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;

/**
 * Created by yanshen on 2018年3月15日.
 */
public class NoScrollListView extends ListView {

    public NoScrollListView(Context context) {
        super(context);

    }

    public void setView(int height){
        this.setLayoutParams(new ViewGroup.LayoutParams(this.getWidth() , height));
    }

    public NoScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }

}
