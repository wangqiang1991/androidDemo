package com.hande.goochao.views.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.config.AppConst;
import com.hande.goochao.utils.AESUtil;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.KeyboardUtil;
import com.hande.goochao.utils.MD5Utils;
import com.hande.goochao.utils.PhoneUtils;
import com.hande.goochao.utils.SetTimeout;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CustomLoadingDialog;

import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ForgetPasswordActivity extends ToolBarActivity implements View.OnClickListener {


    @ViewInject(R.id.phone)
    private EditText txtPhone;
    @ViewInject(R.id.verification)
    private EditText txtCode;
    @ViewInject(R.id.password)
    private TextView txtPwd;
    @ViewInject(R.id.btnResetPwd)
    private Button btnResetPwd;
    @ViewInject(R.id.verification_btn)
    private TextView txtGetCode;

    // 倒计时
    private SetTimeout mSetTimeout;
    private final int mMaxTime = AppConst.CODE_MAX_TIME;

    private CustomLoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        setTitle("忘记密码");

        loadingDialog = new CustomLoadingDialog(this);

        txtGetCode.setOnClickListener(this);
        btnResetPwd.setOnClickListener(this);
    }


    private void getCode() {
        KeyboardUtil.hidInput(this);
        String phone = txtPhone.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            AlertManager.toast(this, "手机号码不能为空");
            return;
        }

        if (!PhoneUtils.validateMobile(phone)) {
            AlertManager.toast(this, "号码格式错误");
            return;
        }


        Map<String, String> params = new HashMap<>();
        params.put("phone", phone);

        String nonceStr = UUID.randomUUID().toString();
        String aesStr = phone + nonceStr;
        params.put("nonceStr", nonceStr);
        try {
            params.put("signStr", AESUtil.encryptAES(aesStr, AppConfig.AES_Secret_Key).replace("\n", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadingDialog.setLoadingText("正在获取验证码");
        loadingDialog.show();

        HttpRequest.get(AppConfig.GET_CODE_RestPwd, null, params, JSONObject.class, new RequestCallback<JSONObject>() {

            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    AlertManager.showSuccessToast(ForgetPasswordActivity.this, "短信发送成功", false);

                    startTime();
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(ForgetPasswordActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(ForgetPasswordActivity.this, "获取验证码失败", false);
            }
        });
    }

    private void doResetPwd() {
        String phone = txtPhone.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            AlertManager.toast(this, "请填写手机号码");
            return;
        }

        if (!PhoneUtils.validateMobile(phone)) {
            AlertManager.toast(this, "号码格式错误");
            return;
        }

        String code = txtCode.getText().toString();
        if (TextUtils.isEmpty(code)) {
            AlertManager.toast(this, "请输入验证码");
            return;
        }

        String pwd = txtPwd.getText().toString();
        if (pwd.length() < 6) {
            AlertManager.toast(this, "密码长度至少6位");
            return;
        }

        if (pwd.length() > 20) {
            AlertManager.toast(this, "密码长度最多20位");
            return;
        }

        loadingDialog.setLoadingText("密码重置中");
        loadingDialog.show();

        Map<String, Object> params = new HashMap<>();
        params.put("phone", phone);
        params.put("verificationCode", code);
        params.put("password", MD5Utils.md5Password(pwd));

        HttpRequest.postJson(AppConfig.Reset_Password, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    AlertManager.showSuccessToast(ForgetPasswordActivity.this, "密码重置成功", false);
                    finish();
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(ForgetPasswordActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(ForgetPasswordActivity.this, "密码重置失败", false);
            }
        });

    }

    private void startTime() {
        txtGetCode.setEnabled(false);
        mSetTimeout = new SetTimeout(mMaxTime, TimeUnit.SECONDS, 1);
        mSetTimeout.setHandler(new SetTimeout.SetTimeoutHandler() {
            @Override
            public void handler(final int current) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int remain = mMaxTime - current - 1;
                        if (remain > 0) {
                            txtGetCode.setText(remain + "秒");
                        } else {
                            txtGetCode.setText("获取验证码");
                            txtGetCode.setEnabled(true);
                        }
                    }
                });
            }
        });
        mSetTimeout.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (ForgetPasswordActivity.this.getCurrentFocus() != null) {
                if (ForgetPasswordActivity.this.getCurrentFocus().getWindowToken() != null) {
                    KeyboardUtil.hidInput(ForgetPasswordActivity.this);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        if (v == txtGetCode) {
            getCode();
        } else if (v == btnResetPwd) {
            doResetPwd();
        }
    }
}
