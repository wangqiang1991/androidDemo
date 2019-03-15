package com.hande.goochao.views.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListAdapter;

import com.hande.goochao.R;

import in.srain.cube.views.GridViewWithHeaderAndFooter;

/**
 * @author Wangenmao
 * @description /n
 * Created by Wangenmao on 2018/7/26.
 */

public class GoochaoGridView extends GridViewWithHeaderAndFooter {

    private View footerView;

    public GoochaoGridView(Context context) {
        super(context);
        addFooterView();
    }

    public GoochaoGridView(Context context, AttributeSet attrs) {

        super(context, attrs);
        addFooterView();
    }

    public GoochaoGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addFooterView();
    }

    private void addFooterView() {
        footerView = LayoutInflater.from(getContext()).inflate(R.layout.no_more_layout, null);
        footerView.setVisibility(View.GONE);
        addFooterView(footerView);
    }

    /**
     * 功能：设置FooterView隐藏显示
     * @param visibility
     */
    public void setFooterViewVisibility(int visibility) {
        footerView.setVisibility(visibility);
    }

//    public int getRowHeight() {
//        super.getRowHeight();
//        return 0;
//    }
}
