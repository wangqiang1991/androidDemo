package com.hande.goochao.views.components;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.hande.goochao.R;
import com.hande.goochao.views.base.ToolBarActivity;

public class WebViewActivity extends ToolBarActivity {

    WebView webView;
    String url;
    private ProgressBar progressBar;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        showRightBtn(R.drawable.refresh_icon_press);

        Bundle extras = getIntent().getExtras();
        url = extras.getString("url");
        title = extras.getString("title");
        setTitle(title);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progress_bar);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

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
                            String newTitle = view.getTitle();
                            if (!title.equals("构巢客服") && !title.equals("场景介绍")){
                                if (!TextUtils.isEmpty(newTitle)) {
                                    setTitle(newTitle);
//                                titleView.setCenterText(title);
                                }
                            }
                        }
                    });
                    progressBar.setProgress(newProgress);
                }

            }
        });

//        new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                webView.loadUrl(url);
//            }
//        }.sendEmptyMessageDelayed(1, 1000);

        webView.loadUrl(url);
    }

    @Override
    protected void onRightClickListener() {
        webView.reload();
    }
}
