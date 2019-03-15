package com.hande.goochao.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import com.hande.goochao.MainActivity;
import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.webview_handle.CallPhoneInvokeHandler;
import com.hande.goochao.commons.webview_handle.GetLoginResultInvokeHandler;
import com.hande.goochao.commons.webview_handle.HasLoginInvokeHandler;
import com.hande.goochao.commons.webview_handle.JoinSuccessInvokeHandler;
import com.hande.goochao.commons.webview_handle.ShareToCircleInvokeHandler;
import com.hande.goochao.commons.webview_handle.ShareToFriendInvokeHandler;
import com.hande.goochao.commons.webview_handle.ToCouponInvokeHandler;
import com.hande.goochao.commons.webview_handle.ToCustomerServiceInvokeHandler;
import com.hande.goochao.commons.webview_handle.ToLoginInvokeHandler;
import com.hande.goochao.commons.webview_handle.ToPlanListInvokeHandler;
import com.hande.goochao.commons.webview_handle.ToProductListInvokeHandler;
import com.hande.goochao.commons.webview_handle.UrlInvokeHandler;
import com.hande.goochao.commons.webview_handle.WebViewApiManager;
import com.hande.goochao.views.base.ToolBarActivity;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.xutils.view.annotation.ViewInject;

import wendu.dsbridge.DWebView;

/**
 * @author Wangenmao
 * @description /n
 * Created by Wangenmao on 2018/9/7.
 */

public class SaleGiftActivity extends ToolBarActivity {

    private String url;
    private WebViewApiManager webApi;
    private ProgressBar progressBar;

    @ViewInject(R.id.webView)
    private DWebView webView;

    //是否需要跳转至软装方案列表
//    private boolean toPlanList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salegift);

        progressBar = findViewById(R.id.progress_bar);
        setTitle("努力加载中...");
        initWebView();
        loadUrl();

        webView.setInitialScale(100);
        webView.getSettings().setUseWideViewPort(true); //将图片调整到适合webview的大小
        webView.getSettings().setTextZoom(100);

        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webView.getSettings().setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        webView.getSettings().setUseWideViewPort(true);

        webView.getSettings().setSupportZoom(false); //禁止缩放

        WebView.setWebContentsDebuggingEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            String title = view.getTitle();
                            if (!TextUtils.isEmpty(title)) {
                                setTitle(title);
                            }
                        }
                    });
                    progressBar.setProgress(newProgress);
                }

            }
        });

        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onEvent(EventBusNotification notification) {
        if (notification.getKey().equals(EventBusNotification.event_bus_login_success)) {
            loadUrl();
        }else if (notification.getKey().equals(EventBusNotification.event_bus_banner_to_plan_list)){
            Intent intent = new Intent(SaleGiftActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("toPlanList" , true);
            startActivity(intent);
        }
    }

    private void initWebView() {
        webApi = WebViewApiManager.getInstance(this);
        webApi.registerApi("isLogin", new HasLoginInvokeHandler());
        webApi.registerApi("toLogin", new ToLoginInvokeHandler());
        webApi.registerApi("getLoginResult", new GetLoginResultInvokeHandler());
        webApi.registerApi("joinSuccess",new JoinSuccessInvokeHandler());
        webApi.registerApi("toCouponList",new ToCouponInvokeHandler());
        webApi.registerApi("toPlanList",new ToPlanListInvokeHandler());
        webApi.registerApi("shareToCircle",new ShareToCircleInvokeHandler());
        webApi.registerApi("shareToFriend",new ShareToFriendInvokeHandler());
        webApi.registerApi("toCustomerService",new ToCustomerServiceInvokeHandler());
        webApi.registerApi("callPhone",new CallPhoneInvokeHandler());
        webApi.registerApi("openUrl",new UrlInvokeHandler());
        webApi.registerApi("toGoodsList",new ToProductListInvokeHandler());
        webView.addJavascriptObject(webApi, null);
    }

    private void loadUrl() {
        url = getIntent().getStringExtra("url");
        webView.loadUrl(url);
    }


    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
//        if (toPlanList){
//            EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_banner_finish);
//            EventBus.getDefault().post(notification);
//        }
        super.onDestroy();
    }
}
