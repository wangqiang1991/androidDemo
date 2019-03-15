package com.hande.goochao.views.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.hande.goochao.commons.views.gallery.GalleryActivity;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;

import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by LMC on 2018/3/15.
 */

public class LikesItemActivity extends ToolBarActivity implements OnItemClickListener {


    private List<String> bannerImages = new ArrayList<>();
    private String[] bannerArray = new String[0];
    private String[] bannerImage;
    private String goodsId;

    private JSONObject likeItemData;
    private String title;
    private String introduce;
    private String banner;

    @ViewInject(R.id.like_item_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.like_item_load_fail)
    private LoadFailView loadFailView;
    @ViewInject(R.id.like_goods_name)
    private TextView goodsTitle;
    @ViewInject(R.id.like_goods_description)
    private TextView goodsDescription;
    @ViewInject(R.id.like_banner)
    private ConvenientBanner convenientBanner;

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes_item);
        glide = GlideApp.with(this);
        Intent intent = getIntent();
        goodsId = intent.getStringExtra("goodsId");
        title = intent.getStringExtra("title");
        setTitle(title);

        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadDetail();
            }
        });

        loadDetail();
    }

    private void loadDetail() {

        loadingView.setVisibility(View.VISIBLE);

        HttpRequest.get(AppConfig.EXCELLENT_GOODS_DETAIL + goodsId, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    likeItemData = JsonUtils.getJsonObject(response, "data", null);

                    initPage();
                    loadFailView.setVisibility(View.GONE);
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(LikesItemActivity.this, "服务器繁忙，请稍后重试", false);
                loadFailView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initPage(){
        introduce = JsonUtils.getString(likeItemData,"description","");
        banner = JsonUtils.getString(likeItemData,"banner","");

        bannerArray = banner.split(",");
        goodsTitle.setText(title);
        goodsDescription.setText(introduce);
        convenientBannerInit();
    }

    private void convenientBannerInit() {

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
        intent.setClass(LikesItemActivity.this, GalleryActivity.class);
        intent.putExtra("isLocal", false);
        intent.putExtra("currentSrc", currentSrc);
        intent.putExtra("images", bannerImage);
        LikesItemActivity.this.startActivity(intent);
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
