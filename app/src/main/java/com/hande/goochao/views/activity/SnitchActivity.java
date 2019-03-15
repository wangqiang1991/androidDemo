package com.hande.goochao.views.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.hande.goochao.R;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.PhoneUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CustomLoadingDialog;

import org.json.JSONObject;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Wangem on 2018/2/10.
 */


public class SnitchActivity extends ToolBarActivity {

    @ViewInject(R.id.textarea)
    private EditText editText;
    @ViewInject(R.id.input_name)
    private EditText inputName;
    @ViewInject(R.id.input_phone)
    private EditText inputPhone;
    @ViewInject(R.id.input_bt)
    private Button inputBt;

    private CustomLoadingDialog loadingDialog;

    private String name;
    private String phone;
    private String comment;

    private String titleValue;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snitch);

        titleValue = getIntent().getStringExtra("titleValue");
        if (TextUtils.isEmpty(titleValue)) {
            setTitle("打小报告");
        } else {
            setTitle(titleValue);
        }

        loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("加载中");

        inputBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input();
            }
        });
    }

    @Event(value = R.id.textarea_layout, type = View.OnClickListener.class)
    private void textareaBtnClick(View view) {
        if(editText.isFocused()){
            editText.setFocusable(true);
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }else {
            editText.requestFocus();
            editText.setFocusable(true);
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void input(){
        name = inputName.getText().toString();
        phone = inputPhone.getText().toString();
        comment = editText.getText().toString();

        if (TextUtils.isEmpty(name)) {
            AlertManager.toast(SnitchActivity.this, "姓名不能为空");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            AlertManager.toast(SnitchActivity.this, "电话不能为空");
            return;
        }
        if (!PhoneUtils.validateMobile(phone)) {
            AlertManager.toast(SnitchActivity.this, "号码格式错误");
            return;
        }
        if (comment.length() < 5) {
            AlertManager.toast(SnitchActivity.this, "内容长度不能小于5");
            return;
        }
        saveComment();
    }

    private void saveComment(){

        loadingDialog.show();
        inputBt.setEnabled(false);
        Map<String,Object> params = new HashMap<>();
        params.put("contact","" + name);
        params.put("phone","" + phone);
        params.put("content","" + comment);
        HttpRequest.postJson(AppConfig.FEED_BACK ,null,params,JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(SnitchActivity.this, message, false);
                    inputBt.setEnabled(true);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                inputBt.setEnabled(true);
                AlertManager.showErrorToast(SnitchActivity.this, "服务器繁忙，请稍后重试", false);
            }
        });
    }
}
