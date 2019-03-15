package com.hande.goochao.views.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.commons.views.gallery.GalleryActivity;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.LoadFailView;

import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExcellentGoodsActivity extends ToolBarActivity implements OnItemClickListener {

    private boolean firstLoad = true;
    private String goodsId;
    String articleId;
    WebView webView;

    private List<String> bannerImages = new ArrayList<>();
    private String[] bannerArray = new String[0];
    private String[] bannerImage;

    private AnimationProgressBar animationProgressBar;

    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.goods_name)
    private TextView goodsTitle;
    @ViewInject(R.id.goods_description)
    private TextView goodsDescription;
    @ViewInject(R.id.produce)
    private TextView goodsProduce;
    @ViewInject(R.id.background_line)
    LinearLayout backgroundLine;
    @ViewInject(R.id.convenientBanner)
    private ConvenientBanner convenientBanner;

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excellent_goods);
        setTitle("点赞详情");
        glide = GlideApp.with(this);
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        goodsId = extras.getString("goodsId");

        animationProgressBar = new AnimationProgressBar(this);

        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                onReloadView();
            }
        });

        loadExcellentGoods();
    }

    private void  loadExcellentGoods() {

        if (firstLoad) {
            animationProgressBar.show();
        }

        String url = RestfulUrl.build(AppConfig.EXCELLENT_GOODS_DETAIL, ":goodsId", goodsId);

        HttpRequest.get(url, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                animationProgressBar.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONObject data = JsonUtils.getJsonObject(response, "data", null);
                    JSONObject article = JsonUtils.getJsonObject(data, "article", null);
                    if(article != null){
                        articleId = JsonUtils.getString(article,"articleId","");
                        goodsProduce.setVisibility(View.VISIBLE);
                        backgroundLine.setVisibility(View.VISIBLE);
                        initWebView();
                    }else {
                        goodsProduce.setVisibility(View.GONE);
                        backgroundLine.setVisibility(View.GONE);
                    }

                    String title = JsonUtils.getString(data,"title","");
                    goodsTitle.setText(title);
                    String description = JsonUtils.getString(data,"description","");
                    goodsDescription.setText(description);
                    String banner = JsonUtils.getString(data,"banner","");
                    bannerArray = banner.split(",");
                    convenientBannerInit();

                    firstLoad = false;
                    resetView();
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
            }
        });
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
        } else {
            AlertManager.showErrorInfo(ExcellentGoodsActivity.this);
        }
    }

    public void onReloadView() {
        resetView();
        loadExcellentGoods();
    }

    private void initWebView() {
        webView = findViewById(R.id.web_view);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                animationProgressBar.dismiss();
            }

        });

        webView.loadUrl(AppConfig.ARTICLE_CONTENT + articleId);
    }

    private void convenientBannerInit(){

        bannerImages.addAll(Arrays.asList(bannerArray));
        bannerImage = new String[bannerImages.size()];
        for (int i = 0; i < bannerImages.size(); i++) {
            bannerImages.set(i, bannerImages.get(i) + AppConfig.IMAGE_COMPRESS);
            bannerImage[i] = bannerImages.get(i);
        }

        convenientBanner.setPages(
                new CBViewHolderCreator<NetworkImageHolderView>() {
                    @Override
                    public NetworkImageHolderView createHolder() {
                        return new NetworkImageHolderView();
                    }
                }, bannerImages)
                .setPointViewVisible(true)
                .setPageIndicator(new int[]{R.drawable.ponit_normal, R.drawable.point_select})
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
                .startTurning(4000)
                .setOnItemClickListener(this)
                .setManualPageable(true);
    }

    @Override
    public void onItemClick(int position) {
        String currentSrc = bannerImages.get(position);
        Intent intent = new Intent();
        intent.setClass(ExcellentGoodsActivity.this, GalleryActivity.class);
        intent.putExtra("isLocal",false );
        intent.putExtra("currentSrc",currentSrc );
        intent.putExtra("images", bannerImage);
        ExcellentGoodsActivity.this.startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public class NetworkImageHolderView implements Holder<String> {

        private ImageView imageView;

        @Override
        public View createView(Context context) {
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            return imageView;
        }

        @Override
        public void UpdateUI(Context context, int position, String data) {
            ImageUtils.loadImage(glide, data, imageView, R.mipmap.loadpicture);
        }
    }
}
