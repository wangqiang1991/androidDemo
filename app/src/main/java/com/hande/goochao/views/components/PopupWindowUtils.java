package com.hande.goochao.views.components;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;

import com.hande.goochao.R;

/**
 * Created by LMC on 2018/2/2.
 */

public class PopupWindowUtils {

    /**  底部弹窗
     * k : listview ID
     * i : listview所处布局文件
     * g : popupwindow父布局（弹出布局）
     */
    private Context context;
    private PopupWindow window;
    private ListView listView;

    public void showPopupwindow(int i,int k,int g) {

        final View contentview = LayoutInflater.from(context).inflate(i, null);
        window = new PopupWindow(contentview,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        window.setContentView(contentview);
        window.setAnimationStyle(R.style.mypopwindow_anim_style);

        listView = contentview.findViewById(k);

        View rootview = LayoutInflater.from(context).inflate(g, null);
        window.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);
    }

    public void getView(){

    }

}


