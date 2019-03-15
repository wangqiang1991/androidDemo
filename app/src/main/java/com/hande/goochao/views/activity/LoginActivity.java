package com.hande.goochao.views.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.MD5Utils;
import com.hande.goochao.views.base.BaseActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Wangem on 2018/2/8.
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private CustomLoadingDialog progressDialog;

    @ViewInject(R.id.btnLogin)
    private Button btnLogin;
    @ViewInject(R.id.layLoginWx)
    private View layLoginWx;
    @ViewInject(R.id.forget_password)
    private View txtForgetPwd;
    @ViewInject(R.id.register_btn)
    private View txtRegister;
    @ViewInject(R.id.phone)
    private EditText txtPhone;
    @ViewInject(R.id.password)
    private EditText txtPwd;
    @ViewInject(R.id.close)
    private View close;
    @ViewInject(R.id.login_view)
    private TextView loginView;

    private IWXAPI api;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        api = WXAPIFactory.createWXAPI(this, AppConfig.WX_APPID, true);
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

        TextPaint paint = loginView.getPaint();
        paint.setFakeBoldText(true);

        progressDialog = new CustomLoadingDialog(this);
        progressDialog.setLoadingText("正在登录中");

        btnLogin.setOnClickListener(this);
        layLoginWx.setOnClickListener(this);
        txtForgetPwd.setOnClickListener(this);
        txtRegister.setOnClickListener(this);
        close.setOnClickListener(this);

        EventBus.getDefault().register(this);

    }

    private void doLogin() {
        String phone = txtPhone.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            AlertManager.toast(this, "请输入登录帐号");
            return;
        }

        String pwd = txtPwd.getText().toString();
        if (TextUtils.isEmpty(pwd)) {
            AlertManager.toast(this, "请输入登录密码");
            return;
        }
        if (pwd.length() < 6) {
            AlertManager.toast(this, "密码长度至少6位");
            return;
        }

        if (pwd.length() > 20) {
            AlertManager.toast(this, "密码长度最多20位");
            return;
        }



        Map<String, String> params = new HashMap<>();
        params.put("phone", phone);
        params.put("password", MD5Utils.md5Password(pwd));
        params.put("from", "android");

        progressDialog.show();

        HttpRequest.post(AppConfig.Login, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    JSONObject loginResult = JsonUtils.getJsonObject(response, "data", null);
                    AppSessionCache.getInstance().setLoginResult(loginResult, LoginActivity.this);

                    EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_login_success);
                    EventBus.getDefault().post(notification);

                    finish();
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(LoginActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(LoginActivity.this, "登录失败!", false);
            }
        });

    }

    private void doLoginByWx() {
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wx_oauth2_authorization_state";
        api.sendReq(req);
    }

    private void gotoRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        this.startActivity(intent);
    }

    private void gotoForgetPwd() {
        Intent intent = new Intent(this, ForgetPasswordActivity.class);
        this.startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        go(this);
        if (view == btnLogin) {
            doLogin();
        } else if (view == layLoginWx) {
                layLoginWx.setEnabled(false);
                doLoginByWx();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        layLoginWx.setEnabled(true);
                    }
                }, 1000);
        } else if (view == txtForgetPwd) {
            gotoForgetPwd();
        } else if (view == txtRegister) {
            gotoRegister();
        } else if (view == close) {
            finish();
        }
    }

    @Subscribe
    public void onEvent(EventBusNotification notification) {
        if (notification.getKey().equals(EventBusNotification.event_bus_wx_auth_success)) {
            SendAuth.Resp resp = (SendAuth.Resp) notification.getValue();
            getWxToken(resp);
        } else if (notification.getKey().equals(EventBusNotification.event_bus_register_success)) {
            finish();
        }
    }


    private void getWxToken(final SendAuth.Resp resp) {

        progressDialog.show();

        String url = "https://api.weixin.qq.com/sns/oauth2/access_token";
        Map<String, String> params = new HashMap<>();
        params.put("appid", AppConfig.WX_APPID);
        params.put("secret", AppConfig.WX_APPSECRET);
        params.put("code", resp.code);
        params.put("grant_type", "authorization_code");


        HttpRequest.get(url, null, params, JSONObject.class, new RequestCallback<JSONObject>() {

            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                System.out.println(response);

                String access_token = JsonUtils.getString(response, "access_token", "");
                String unionid = JsonUtils.getString(response, "unionid", "");
                String openid = JsonUtils.getString(response, "openid", "");

                getWxUserInfo(access_token, unionid, openid);
            }

            @Override
            public void onError(Throwable ex) {
                progressDialog.dismiss();
                AlertManager.showErrorToast(LoginActivity.this, "微信登录失败", false);
            }
        });

    }

    private void getWxUserInfo(String accessToken, String openid, String unionid) {
        String url = "https://api.weixin.qq.com/sns/userinfo";
        Map<String, String> params = new HashMap<>();
        params.put("access_token", accessToken);
        params.put("openid", openid);

        progressDialog.show();

        HttpRequest.get(url, null, params, JSONObject.class, new RequestCallback<JSONObject>() {

            @Override
            public void onComplete(boolean success, JSONObject response) {
            }

            @Override
            public void onSuccess(JSONObject response) {
                String headImgUrl = JsonUtils.getString(response, "headimgurl", "");
                String nickName = JsonUtils.getString(response, "nickname", "");
                String openid = JsonUtils.getString(response, "openid", "");
                String unionid = JsonUtils.getString(response, "unionid", "");
                String gender = JsonUtils.getString(response, "sex", "0");

                submitWxUserInfo(openid, unionid, nickName, headImgUrl, gender);
            }

            @Override
            public void onError(Throwable ex) {
                progressDialog.dismiss();
                AlertManager.showErrorToast(LoginActivity.this, "微信登录失败", false);
            }
        });
    }

    private void submitWxUserInfo(String openId, String unionId, String nickName, String headImgUrl, String gender) {
        Map<String, Object> params = new HashMap<>();
        params.put("openId", openId);
        params.put("unionId", unionId);
        params.put("nickName", nickName);
        params.put("head", headImgUrl);
        params.put("gender", gender);

        progressDialog.show();

        HttpRequest.postJson(AppConfig.Wx_Login, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                progressDialog.dismiss();
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    JSONObject loginResult = JsonUtils.getJsonObject(response, "data", null);
                    AppSessionCache.getInstance().setLoginResult(loginResult, LoginActivity.this);

                    EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_login_success);
                    EventBus.getDefault().post(notification);
                    finish();
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(LoginActivity.this,message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                progressDialog.dismiss();
                AlertManager.showErrorToast(LoginActivity.this,"微信登录失败", false);
            }
        });
    }

    @Override
    public void finish() {
        EventBus.getDefault().unregister(this);
        super.finish();
    }

    public static void go(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
