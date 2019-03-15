package com.hande.goochao.views.components;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.hande.goochao.R;

/**
 * Created by Wangenmao on 2018/3/8.
 */

public class CustomLoadingDialog extends ProgressDialog {


    private String loadingText;

    public CustomLoadingDialog(Context context) {
        this(context,R.style.CustomDialog);
    }

    public CustomLoadingDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init(getContext());
    }

    private void init(Context context) {
        //设置不可取消，点击其他区域不能取消，实际中可以抽出去封装供外包设置
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        setContentView(R.layout.loading_layout);

        if (this.loadingText != null) {
            TextView txtLoading = findViewById(R.id.tv_load_dialog);
            txtLoading.setText(this.loadingText);
        }

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
    }

    public String getLoadingText() {
        return loadingText;
    }

    public void setLoadingText(String loadingText) {
        this.loadingText = loadingText;
    }

    @Override
    public void show() {
        super.show();
    }
}
