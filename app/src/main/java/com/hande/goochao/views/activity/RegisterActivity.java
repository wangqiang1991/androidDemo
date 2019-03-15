package com.hande.goochao.views.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.EventBusNotification;
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
import com.hande.goochao.views.base.BaseActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.WebViewActivity;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * Created by Wangem on 2018/2/8.
 */

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    @ViewInject(R.id.back_btn)
    private ImageView back_btn;
    @ViewInject(R.id.txtGetCode)
    private TextView txtGetCode;
    @ViewInject(R.id.phone)
    private EditText txtPhone;
    @ViewInject(R.id.verification)
    private EditText txtCode;
    @ViewInject(R.id.password)
    private EditText txtPassword;
    @ViewInject(R.id.btnRegister)
    private Button btnRegister;
    @ViewInject(R.id.user_agreement)
    private View userAgreementView;
    @ViewInject(R.id.agreement)
    private TextView agreementText;

    private CustomLoadingDialog loadingDialog;

    // 倒计时
    private SetTimeout mSetTimeout;
    private final int mMaxTime = AppConst.CODE_MAX_TIME;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        back_btn.setOnClickListener(this);
        txtGetCode.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        userAgreementView.setOnClickListener(this);

        agreementText.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG ); //下划线
        agreementText.getPaint().setAntiAlias(true);//抗锯齿

        loadingDialog = new CustomLoadingDialog(this);
    }

    @Override
    public void onClick(View v) {
        if (v == back_btn) {
            finish();
        } else if (v == txtGetCode) {
            getCode();
        } else if (v == btnRegister) {
            doRegister();
        } else if (v == userAgreementView){
            gotoUserProtocol();
        }
    }

    private void gotoUserProtocol() {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("title", "用户协议");
        intent.putExtra("url", AppConfig.User_Protocol_Url);
        startActivity(intent);
    }

    private void getCode() {
        KeyboardUtil.hidInput(RegisterActivity.this);
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

        HttpRequest.get(AppConfig.GET_CODE, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    AlertManager.showSuccessToast(RegisterActivity.this, "短信发送成功",false);

                    startTime();
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(RegisterActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(RegisterActivity.this, "获取验证码失败", false);
            }
        });
    }

    private void doRegister() {
        String phone = txtPhone.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            AlertManager.toast(this, "手机号码不能为空");
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

        String pwd = txtPassword.getText().toString().trim();
        if (pwd.length() < 6) {
            AlertManager.toast(this, "密码至少6位");
            return;
        }
        if (pwd.length() > 20) {
            AlertManager.toast(this, "密码最多20位");
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("phone", phone);
        params.put("password", MD5Utils.md5Password(pwd));
        params.put("verificationCode", code);
        params.put("from", "android");


        loadingDialog.setLoadingText("正在注册中");
        loadingDialog.show();

        HttpRequest.postJson(AppConfig.Register, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    AlertManager.showSuccessToast(RegisterActivity.this, "注册成功，已为你自动登录", false);

                    JSONObject loginResult = JsonUtils.getJsonObject(response, "data", null);
                    AppSessionCache.getInstance().setLoginResult(loginResult, RegisterActivity.this);

                    EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_register_success);
                    EventBus.getDefault().post(notification);
                    finish();
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(RegisterActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(RegisterActivity.this, "注册失败", false);
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
            if (RegisterActivity.this.getCurrentFocus() != null) {
                if (RegisterActivity.this.getCurrentFocus().getWindowToken() != null) {
                    KeyboardUtil.hidInput(RegisterActivity.this);
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
