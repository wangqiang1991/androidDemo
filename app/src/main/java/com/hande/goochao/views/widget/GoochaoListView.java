package com.hande.goochao.views.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.hande.goochao.R;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/7/26.
 */
public class GoochaoListView extends ListView {

    private View footView;

    public GoochaoListView(Context context) {
        super(context);
        addFootView();
    }

    public GoochaoListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addFootView();
    }

    public GoochaoListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addFootView();
    }

    public GoochaoListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        addFootView();
    }

    private void addFootView(){
        footView = LayoutInflater.from(getContext()).inflate(R.layout.no_more_layout,null);
        footView.setVisibility(View.GONE);
        addFooterView(footView);
    }

    /**
     * 功能：设置FooterView隐藏显示
     * @param visibility
     */
    public void setFooterViewVisibility(int visibility) {
        footView.setVisibility(visibility);
    }
}
