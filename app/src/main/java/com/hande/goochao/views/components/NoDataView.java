package com.hande.goochao.views.components;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.utils.WindowUtils;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * Created by Wangenmao on 2018/3/14.
 */

public class NoDataView extends LinearLayout {

    @ViewInject(R.id.imageView)
    private ImageView imageView;
    @ViewInject(R.id.textView)
    private TextView textView;

    public NoDataView(Context context) {
        super(context);
        init();
    }

    public NoDataView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoDataView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public NoDataView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_nodata, this, true);
        x.view().inject(this);
    }


    public void setImageAndText(int resourceId, String text) {
        imageView.setImageDrawable(getResources().getDrawable(resourceId));
        textView.setText(text);
        WindowUtils.boldMethod(textView);
    }

}
