package com.hande.goochao.views.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hande.goochao.BuildConfig;
import com.hande.goochao.MainActivity;
import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.JsonUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * Created by LMC on 2018/3/26.
 */

public class WelcomeActivity extends Activity {

    private static final int GO_MAIN=100;
    private static final int GO_GUIDE=101;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GO_MAIN:
                    goMain();
                    break;
                case GO_GUIDE:
                    goGuide();
                    break;
            }
        }
    };

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

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

        init();
    }

    private void init() {
        SharedPreferences sf = getSharedPreferences("data", MODE_PRIVATE);//判断是否是第一次进入
        boolean isFirstIn = sf.getBoolean("isFirstIn" + BuildConfig.VERSION_CODE, true);
        SharedPreferences.Editor editor = sf.edit();
        if (isFirstIn) {     //若为true，则是第一次进入
            editor.putBoolean("isFirstIn" + BuildConfig.VERSION_CODE, false);
            handler.sendEmptyMessageDelayed(GO_GUIDE, 1000 * 2);
        }//将欢迎页停留2秒，并且将message设置为跳转到引导页SplashActivity，跳转在goGuide中实现
        else {
            preGoMain();
        }
        editor.commit();
    }

    private void preGoMain() {
        if (!AppSessionCache.getInstance().isLogin(this)) {
            postGoMain();
            return;
        }

        JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(this);
        String accessToken = JsonUtils.getString(loginResult, "accessToken", null);
        if (StringUtils.isEmpty(accessToken)) {
            postGoMain();
            return;
        }
        RequestParams requestParams = new RequestParams(AppConfig.VALIDATE_TOKEN);
        requestParams.addHeader("access_token", accessToken);
        x.http().post(requestParams, new Callback.CacheCallback<String>() {
            @Override
            public boolean onCache(String result) {
                return false;
            }

            @Override
            public void onSuccess(String result) {
                JSONObject response = JsonUtils.newJsonObject(result, null);
                if (response == null || JsonUtils.getCode(response) != 0) {
                    AppSessionCache.getInstance().setLoginResult(null, WelcomeActivity.this);
                }
                handler.sendEmptyMessageDelayed(GO_MAIN, 1000 * 1);//将欢迎页停留2秒，并且将message设置文跳转到MainActivity，跳转功能在goMain中实现
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
//                AppSessionCache.getInstance().setLoginResult(null, WelcomeActivity.this);
                handler.sendEmptyMessageDelayed(GO_MAIN, 1000 * 1);//将欢迎页停留2秒，并且将message设置文跳转到MainActivity，跳转功能在goMain中实现
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void postGoMain() {
        handler.sendEmptyMessageDelayed(GO_MAIN, 1000 * 2);//将欢迎页停留2秒，并且将message设置文跳转到MainActivity，跳转功能在goMain中实现
    }

    private void goMain() {
        Intent it = new Intent(WelcomeActivity.this, MainActivity.class); //下一步转向MainActivity
        startActivity(it); //执行意图
        WelcomeActivity.this.finish();
    }

    private void goGuide() {
        Intent intent=new Intent(
                WelcomeActivity.this,GuideActivity.class);
        startActivity(intent);
        finish();
    }
}

