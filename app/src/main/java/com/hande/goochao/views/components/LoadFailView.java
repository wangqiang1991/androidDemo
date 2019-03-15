package com.hande.goochao.views.components;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.utils.WindowUtils;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * Created by Wangem on 2018/1/31.
 */

public class LoadFailView extends FrameLayout implements View.OnClickListener {

    @ViewInject(R.id.reload_btn)
    private Button reloadBtn;
    @ViewInject(R.id.reload_txt)
    private TextView reloadTxt;
    @ViewInject(R.id.second_txt)
    private TextView secondTxt;

    private OnReloadListener onReloadListener;

    private boolean doing;

    public LoadFailView(Context context) {
        super(context);
        init();
    }

    public LoadFailView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadFailView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LoadFailView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_load_fail, this, true);
        x.view().inject(this);
        reloadBtn.setOnClickListener(this);
    }

    public void setOnReloadListener(OnReloadListener onReloadListener) {
        this.onReloadListener = onReloadListener;
    }

    public void setFinishReload(){
        reloadBtn.setEnabled(true);
    }

    public void setReloading(){
        reloadBtn.setEnabled(false);
    }

    public OnReloadListener getOnReloadListener() {
        return onReloadListener;
    }

    @Override
    public void onClick(View view) {
        if (getOnReloadListener() != null) {
            getOnReloadListener().onReload();
        }
    }

    public interface OnReloadListener {
        void onReload();
    }

    public void setText(String string) {
        reloadTxt.setText(string);
        WindowUtils.setMargins(reloadTxt,0,WindowUtils.dpToPixels(getContext(),10),0,WindowUtils.dpToPixels(getContext(),10));
        secondTxt.setVisibility(GONE);
    }
}
