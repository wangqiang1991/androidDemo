package com.hande.goochao.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.hande.goochao.R;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;

import org.xutils.view.annotation.ViewInject;

/**
 * Created by Wangenmao on 2018/3/13.
 */

public class SingleInputActivity extends ToolBarActivity implements View.OnClickListener {

    private String titleValue;
    private String leftValue;
    private String rightValue;
    private String placeHolder;
    private String inputValue;
    private boolean require;
    private String requireTxt;

    @ViewInject(R.id.txtValue)
    private EditText txtValue;
    @ViewInject(R.id.layClear)
    private View layClear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singleinput);
        titleValue = getIntent().getStringExtra("titleValue");
        if (TextUtils.isEmpty(titleValue)) {
            titleValue = "文本输入";
        }
        leftValue = getIntent().getStringExtra("leftValue");
        if (TextUtils.isEmpty(leftValue)) {
            leftValue = "取消";
        }
        rightValue = getIntent().getStringExtra("rightValue");
        if (TextUtils.isEmpty(rightValue)) {
            rightValue = "完成";
        }
        placeHolder = getIntent().getStringExtra("placeHolder");
        if (TextUtils.isEmpty(placeHolder)) {
            placeHolder = "请输入文本值";
        }
        inputValue = getIntent().getStringExtra("inputValue");
        require = getIntent().getBooleanExtra("require", false);
        requireTxt = getIntent().getStringExtra("requireTxt");

        setTitle(titleValue);
        txtValue.setText(inputValue);
        txtValue.setHint(placeHolder+"(最多输入16位)");

        layClear.setOnClickListener(this);

        hideBack();
        setCloseText("取消");
        setSubmitText("完成");
    }

    @Override
    protected void onCloseClickLister() {
        super.onCloseClickLister();
        finish();
    }

    @Override
    protected void onSubmitClickLister() {
        super.onSubmitClickLister();
        String value = txtValue.getText().toString();
        value = value.trim();
        if (require && TextUtils.isEmpty(value)) {
            AlertManager.showErrorToast(this, requireTxt, false);
            return;
        }
        Intent data = new Intent();
        data.putExtra("inputValue", value);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v == layClear) {
            txtValue.setText(null);
        }
    }
}
