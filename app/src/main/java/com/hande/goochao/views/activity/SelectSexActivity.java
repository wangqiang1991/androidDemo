package com.hande.goochao.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.views.base.ToolBarActivity;

import org.xutils.view.annotation.ViewInject;

/**
 * Created by Wangenmao on 2018/3/13.
 */

public class SelectSexActivity extends ToolBarActivity implements View.OnClickListener {


    private String titleValue;
    private String leftValue;
    private String rightValue;
    private int genderValue;

    @ViewInject(R.id.layMale)
    private View layMale;
    @ViewInject(R.id.imgMale)
    private ImageView imgMale;
    @ViewInject(R.id.layFemale)
    private View layFemale;
    @ViewInject(R.id.imgFemale)
    private ImageView imgFemale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectgender);

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

        genderValue = getIntent().getIntExtra("genderValue", 1);

        setTitle(titleValue);
        hideBack();
        setCloseText("取消");
        setSubmitText("完成");

        layMale.setOnClickListener(this);
        layFemale.setOnClickListener(this);

        bindSource();
    }

    private void bindSource() {
        if (genderValue == 1) {
            imgMale.setVisibility(View.VISIBLE);
            imgFemale.setVisibility(View.GONE);
        } else if (genderValue == 2) {
            imgMale.setVisibility(View.GONE);
            imgFemale.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onCloseClickLister() {
        super.onCloseClickLister();
        finish();
    }

    @Override
    protected void onSubmitClickLister() {
        super.onSubmitClickLister();

        Intent data = new Intent();
        data.putExtra("genderValue", genderValue);
        setResult(RESULT_OK, data);
        finish();

    }

    @Override
    public void onClick(View v) {
        if (v == layMale) {
            genderValue = 1;
        } else if (v == layFemale) {
            genderValue = 2;
        }
        bindSource();
    }
}
