package com.hande.goochao.views.components.refresh;

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
 * @author LMC
 * @description 无
 * Created by LMC on 2018/7/24.
 */
public class NoDataTwoLineView extends LinearLayout{

    @ViewInject(R.id.imageView_two_line)
    private ImageView imageView;
    @ViewInject(R.id.textView_two_line_first)
    private TextView textView1;
    @ViewInject(R.id.textView_two_line_second)
    private TextView textView2;

    public NoDataTwoLineView(Context context) {
        super(context);
        init();
    }

    public NoDataTwoLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoDataTwoLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public NoDataTwoLineView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_nodata_two_line, this, true);
        x.view().inject(this);
    }


    public void setImageAndText(int resourceId, String text1, String text2) {
        imageView.setImageDrawable(getResources().getDrawable(resourceId));
        textView1.setText(text1);
        textView2.setText(text2);
        WindowUtils.boldMethod(textView1);
        WindowUtils.boldMethod(textView2);
    }

}
