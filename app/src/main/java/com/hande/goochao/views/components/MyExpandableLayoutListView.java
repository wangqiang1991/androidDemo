package com.hande.goochao.views.components;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.andexert.expandablelayout.library.ExpandableLayoutItem;
import com.andexert.expandablelayout.library.ExpandableLayoutListView;
import com.hande.goochao.utils.WindowUtils;

/**
 * @author yanshen
 * @description 无
 * Created by yanshen on 2018/8/13.
 */
public class MyExpandableLayoutListView extends ExpandableLayoutListView {
    private AdapterView.OnItemClickListener onItemClickListener;

    public MyExpandableLayoutListView(Context context) {
        super(context);
    }

    public MyExpandableLayoutListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyExpandableLayoutListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public OnItemClickListener getOnItemClickListenerOverride() {
        return onItemClickListener;
    }

    @Override
    public boolean performItemClick(View view, final int position, long id) {
        boolean res = super.performItemClick(view, position, id);
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(this, view, position, id);
        }
        return res;
    }

}
