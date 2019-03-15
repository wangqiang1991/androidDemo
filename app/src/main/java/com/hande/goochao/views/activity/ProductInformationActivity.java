package com.hande.goochao.views.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.controller.BadgeView;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.commons.views.gallery.GalleryActivity;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.CustomerServiceUtils;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.PriceUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.PayCloseActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.widget.SharePopupWindow;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.adapter.recyclerview.wrapper.HeaderAndFooterWrapper;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ProductInformationActivity extends PayCloseActivity implements OnItemClickListener {


    private TextView productName;
    private TextView productContent;
    private TextView productPrice;
    private TextView commentsCount;
    private TextView showSimilarity;
    private ImageView collectionBtn;
    private ImageView haveCollect;
    private ImageView couponBtn;
    private TextView joinShoppingCat;
    private TextView goodsOff;
    private TextView buyNow;
    private TextView goodsStyleName;
    private TextView goodsStylePrice;
    private TextView goodsCount;
    private ImageView goodsStyleImage;

    private String currentGoodsId;
    private String detailUrl;
    private boolean firstLoad = true;
    private boolean loaded = false;
    private boolean buyNowShow;
    private boolean skuPull = true;
    private int maxCount;
    private int tagPosition = 0;
    private int amount;
    private String styleId;

    private CustomLoadingDialog loadingDialog;
    private ConvenientBanner convenientBanner;
    private PopupWindow couponPopUpWindow;
    private PopupWindow showGoodsDetail;
    private Window window;
    private WebView mWebView;

    private HeaderAndFooterWrapper mHeaderAndFooterWrapper;

    @ViewInject(R.id.recommend_refresh_view)
    private RefreshLayout refreshLayout;
    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.shopping_goods_count)
    private BadgeView shoppingGoodsCount;
    @ViewInject(R.id.outside_buy_now)
    private LinearLayout outsideBuyNow;
    @ViewInject(R.id.outside_goods_off)
    private LinearLayout outsideGoodsOff;
    @ViewInject(R.id.custom_layout)
    private LinearLayout customSerVice;
    @ViewInject(R.id.shopping_cat)
    private RelativeLayout shoppingCatLayout;
    @ViewInject(R.id.my_recycler_view)
    private RecyclerView mRecyclerView;

    private JSONObject productArray = new JSONObject();
    private JSONArray couponArray = new JSONArray();
    private JSONArray styles = new JSONArray();
    private JSONArray similarityData = new JSONArray();
    private List<String> bannerImages = new ArrayList<>();
    private List<String> tagArrayName = new ArrayList<>();
    private List<JSONObject> similarityDataGoods = new ArrayList<>();
    private String[] bannerImage;
    private int windowX;

    private long outTime = 20000;
    private Handler timer;
    private Handler mHandler = new Handler();

    private int count;

    private GlideRequests glide;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_information);
        setTitle("商品详情");
        showBack();
        showRightBtn(R.mipmap.goods_share_def);
        glide = GlideApp.with(this);
        currentGoodsId = getIntent().getStringExtra("goodsId");

        if(!loaded){

            windowX = WindowUtils.getDeviceWidth(ProductInformationActivity.this);

            LinearLayout productHeaderView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.fragment_product_information_header_view, null);

            GoodsSimilarityAdapter goodsSimilarityAdapter = new GoodsSimilarityAdapter(this, R.layout.view_product_detail_item, similarityDataGoods);

            mHeaderAndFooterWrapper = new HeaderAndFooterWrapper(goodsSimilarityAdapter);
            mHeaderAndFooterWrapper.addHeaderView(productHeaderView);

            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2, OrientationHelper.VERTICAL, false));
            mRecyclerView.setAdapter(mHeaderAndFooterWrapper);

            convenientBanner = productHeaderView.findViewById(R.id.informationConvenientBanner);

            productName = productHeaderView.findViewById(R.id.productName);
            productContent = productHeaderView.findViewById(R.id.productContent);
            productPrice = productHeaderView.findViewById(R.id.productPrice);
            commentsCount = productHeaderView.findViewById(R.id.commentsCount);
            collectionBtn = productHeaderView.findViewById(R.id.collectionBtn);
            haveCollect = productHeaderView.findViewById(R.id.have_collect);
            showSimilarity = productHeaderView.findViewById(R.id.show_similarity);
            couponBtn = productHeaderView.findViewById(R.id.coupon);
            RelativeLayout choiceSize = productHeaderView.findViewById(R.id.choice_size);

            loaded = true;

            commentsCount.setOnClickListener(new CommentsCountListener());
            collectionBtn.setOnClickListener(new CollectGoods());
            couponBtn.setOnClickListener(new Coupon());
            choiceSize.setOnClickListener(new showGoodsClick("0"));
            outsideBuyNow.setOnClickListener(new showGoodsClick("1"));

            loadingDialog = new CustomLoadingDialog(this);

            refreshLayout.setNoMoreData(false);
            refreshLayout.setEnableRefresh(false);

            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshLayout) {
                            AlertManager.toast(ProductInformationActivity.this, " 已经是最底部了哦~ ");
                            refreshLayout.finishLoadMore();
                }
            });
            loadFailView.setOnReloadListener(new  LoadFailView.OnReloadListener() {
                @Override
                public void onReload() {
                    loadProductInformation();
                }
            });

            loadShoppingCatCount();
            loadProductInformation();
        }

    }

    //分享点击事件
    @Override
    protected void onRightClickListener() {
        String title = JsonUtils.getString(productArray,"title",null);
        String desc = JsonUtils.getString(productArray,"description",null);
        String cover = JsonUtils.getString(productArray,"cover",null);
        String url = RestfulUrl.build(AppConfig.GOODS_SHARE_URL, ":goodsId", currentGoodsId);

        new SharePopupWindow(this).show(url, cover, title, desc);
    }

    //查询商品详情
    private void loadProductInformation() {

        loadingDialog.show();

        String url = RestfulUrl.build(AppConfig.PRODUCT_INFORMATION, ":goodsId", currentGoodsId);
        HttpRequest.get(url, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
            }

            @Override
            public void onSuccess(JSONObject response) {
                if(JsonUtils.getCode(response) == 0){
                    productArray = JsonUtils.getJsonObject(response, "data", null);
                    convenientBannerInit();
                    productInit();
                    loadSimilarity();
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

    // 查询推荐商品
    private void loadSimilarity() {

        Map<String, String> params = new HashMap<>();
        params.put("goodsId", "" + currentGoodsId);
        int pageSize = 8;
        params.put("size",""+ pageSize);


        HttpRequest.get(AppConfig.SIMILARITY_PRODUCT, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {

                    similarityData = JsonUtils.getJsonArray(response, "data", null);

                    for (int i = 0; i < similarityData.length(); i++) {
                        similarityDataGoods.add(JsonUtils.getJsonItem(similarityData, i, null));
                    }

                    mHeaderAndFooterWrapper.notifyDataSetChanged();

                    if ( similarityData.length() == 0 ){
                        showSimilarity.setVisibility(View.GONE);
                    }

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

    //查询购物车数量
    private void loadShoppingCatCount() {

        if (!AppSessionCache.getInstance().isLogin(ProductInformationActivity.this))  {
            shoppingGoodsCount.setVisibility(View.GONE);
            return;
        }
        HttpRequest.get(AppConfig.SHOPPING_CAT_COUNT, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    int count = JsonUtils.getInt(response, "data", 0);
                    if (count == 0) {
                        shoppingGoodsCount.setVisibility(View.GONE);
                    } else {
                        shoppingGoodsCount.setVisibility(View.VISIBLE);
                        shoppingGoodsCount.setText(count + "");
                    }
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(ProductInformationActivity.this, "服务器繁忙，请稍后重试", false);
                showError();
            }
        });
    }

    //收藏商品
    private void collectGoods() {
        if (!AppSessionCache.getInstance().isLogin(ProductInformationActivity.this))  {
            Intent intent=new Intent(ProductInformationActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("goodsId", "" + currentGoodsId);

        loadingDialog.show();

        HttpRequest.post(AppConfig.COLLECT_GOODS, null, params, JSONObject.class, new RequestCallback<JSONObject>() {

            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    AlertManager.showSuccessToast(ProductInformationActivity.this, "收藏成功", false);
                    collectionBtn.setVisibility(View.GONE);
                    haveCollect.setVisibility(View.VISIBLE);
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

    //领取全部优惠券
    private void getCoupon() {
        if (!AppSessionCache.getInstance().isLogin(ProductInformationActivity.this))  {
            Intent intent=new Intent(ProductInformationActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        }
        for (int i = 0 ; i < couponArray.length() ; i++){
            count = 0;
            if (JsonUtils.getBoolean(JsonUtils.getJsonItem(couponArray,i,null),"owned",false)){
                count = count + 1;
            }
        }
        if (count == couponArray.length()){
            AlertManager.showErrorToast(ProductInformationActivity.this, "已经领取或者使用过啦~", false);
            return;
        }

        loadingDialog.show();
        String url = RestfulUrl.build(AppConfig.GET_COUPONS, ":goodsId", currentGoodsId);
        HttpRequest.post(url, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    String res = JsonUtils.getString(response, "data", "");
                    if( res.equals("success") ) {
                        AlertManager.showSuccessToast(ProductInformationActivity.this, "领取成功", false);
                        couponPopUpWindow.dismiss();
                    } else {
                        AlertManager.showErrorToast(ProductInformationActivity.this, JsonUtils.getString(response, "message", ""), false);
                    }
                } else {
                    AlertManager.showErrorToast(ProductInformationActivity.this, "领取失败", false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
            }
        });
    }

    //领取单个优惠券
    private void getSingleCoupon(String couponId) {
        if (!AppSessionCache.getInstance().isLogin(ProductInformationActivity.this))  {
            Intent intent=new Intent(ProductInformationActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        loadingDialog.show();
        HttpRequest.post(AppConfig.GET_SINGLE_COUPONS + couponId, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    String res = JsonUtils.getString(response, "data", "");
                    if( res.equals("success") ) {
                        AlertManager.showSuccessToast(ProductInformationActivity.this, "领取成功", false);
                        couponPopUpWindow.dismiss();
                    } else {
                        AlertManager.showErrorToast(ProductInformationActivity.this, JsonUtils.getString(response, "message", ""), false);
                    }
                } else {
                    AlertManager.showErrorToast(ProductInformationActivity.this, "已经领取或者使用过啦~", false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
            }
        });
    }

    //加入购物车
    private void addShoppingCat() {

        if (!AppSessionCache.getInstance().isLogin(ProductInformationActivity.this))  {
            Intent intent=new Intent(ProductInformationActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        loadingDialog.show();

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount+ "");
        params.put("goodsId", currentGoodsId + "");
        params.put("styleId", styleId + "");

        HttpRequest.postJson(AppConfig.ADD_SHOPPING_CAT, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    String res = JsonUtils.getString(response, "data", "");
                    if( res.equals("success") ) {
                        AlertManager.showSuccessToast(ProductInformationActivity.this, "加入成功", false);
                        loadShoppingCatCount();
                        showGoodsDetail.dismiss();
                    } else {
                        AlertManager.showErrorToast(ProductInformationActivity.this, JsonUtils.getString(response, "message", ""), false);
                    }
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

    //立即购买
    private void buyNow() {

        if (!AppSessionCache.getInstance().isLogin(ProductInformationActivity.this))  {
            Intent intent=new Intent(ProductInformationActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        loadingDialog.show();
        Map<String, Object> item = new HashMap<>();
        item.put("amount", amount);
        item.put("goodsId",currentGoodsId);
        item.put("styleId",styleId);
        item.put("outright",true);


        Map<String, Object> params = new HashMap<>();
        params.put("items", new Map[] {item});

        HttpRequest.postJson(AppConfig.BUY_NOW, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    String orderId = JsonUtils.getString(response, "data", "");
                    showGoodsDetail.dismiss();
                    Intent intent = new Intent();
                    intent.setClass(ProductInformationActivity.this, ConfirmOrderActivity.class);
                    intent.putExtra("orderId",orderId);
                    ProductInformationActivity.this.startActivity(intent);

                } else {
                    AlertManager.showErrorToast(ProductInformationActivity.this, JsonUtils.getString(response, "message", ""),false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
            }
        });
    }

    private void showError() {
        loadingDialog.dismiss();
        loadFailView.setVisibility(View.VISIBLE);
        AlertManager.showErrorInfo(this);

    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    //header头部的初始化
    private void productInit (){
        final String title = JsonUtils.getString(productArray,"title",null);
        final String goodsNumber = JsonUtils.getString(productArray,"goodsNumber",null);
        styles = JsonUtils.getJsonArray(productArray,"styles",new JSONArray());
        detailUrl = JsonUtils.getString(productArray,"article.articleId",null);
        productName.setText(title);
        productContent.setText(JsonUtils.getString(productArray,"description",null));
        couponArray = JsonUtils.getJsonArray(productArray,"coupons",new JSONArray());
        float price = (float) JsonUtils.getDouble(productArray,"minPrice",0);

        JSONArray tagArray = JsonUtils.getJsonArray(productArray, "styles", new JSONArray());
        int itemWidth = WindowUtils.getDeviceWidth(ProductInformationActivity.this) - WindowUtils.dpToPixels(ProductInformationActivity.this,107);
        LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) productContent.getLayoutParams();
        params.width = itemWidth;
        productContent.setLayoutParams(params);

        int width = WindowUtils.getDeviceWidth(this);
        int height = width * 887 / 1024;
        RelativeLayout.LayoutParams bannerParams= (RelativeLayout.LayoutParams) convenientBanner.getLayoutParams();
        bannerParams.height = height;
        convenientBanner.setLayoutParams(bannerParams);

        //商品规格名的数组生成
        for (int i = 0; i < tagArray.length(); i++){
            String name = JsonUtils.getString(JsonUtils.getJsonItem(tagArray, i, null), "name", "");
            tagArrayName.add(name);
        }

        productPrice.setText(PriceUtils.beautify("¥" + price));

        commentsCount.setText("评价 (" + JsonUtils.getString(productArray,"commentsCount",null) + ")");

        //判断是否收藏
        boolean collect = JsonUtils.getBoolean(productArray,"collection",false);
        if (collect) {
            collectionBtn.setVisibility(View.GONE);
            haveCollect.setVisibility(View.VISIBLE);
        }

        //判断是否有优惠券
        if ( couponArray.length() == 0){
            couponBtn.setVisibility(View.GONE);
        } else {
            couponBtn.setVisibility(View.VISIBLE);
        }

        //判断是否下架
        if ( skuPull ){
            outsideGoodsOff.setVisibility(View.VISIBLE);
            outsideBuyNow.setVisibility(View.GONE);
        }

        //客服
        customSerVice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(ProductInformationActivity.this);
                CustomerServiceUtils.CustomerServiceModel model = new CustomerServiceUtils.CustomerServiceModel();
                model.setUname(JsonUtils.getString(loginResult, "nickName", null));
                model.setRealname(model.getUname());
                model.setFace(JsonUtils.getString(loginResult, "head", null));
                model.setGoodsNumber(goodsNumber);
                model.setGoodsTitle(title);
                CustomerServiceUtils.goService(model, ProductInformationActivity.this);
            }
        });

        //购物车
        shoppingCatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!AppSessionCache.getInstance().isLogin(ProductInformationActivity.this))  {
                    Intent intent=new Intent(ProductInformationActivity.this, LoginActivity.class);
                    startActivity(intent);
                    return;
                }

                Intent intent = new Intent();
                intent.setClass(ProductInformationActivity.this, ShopCartActivity.class);
                ProductInformationActivity.this.startActivity(intent);
            }
        });

        //初始化webview
        webViewInit();
        mRecyclerView.scrollToPosition (0);
    }

    //选择规格函数
    private void choiceStyle(int position) {

        amount = 1;

        String goodsName = JsonUtils.getString(JsonUtils.getJsonItem(styles, position, null), "name", "");
        float goodsPrice = (float) JsonUtils.getDouble(JsonUtils.getJsonItem(styles, position, null), "price", 0);
        maxCount = JsonUtils.getInt(JsonUtils.getJsonItem(styles, position, null), "count", 1);
        String goodsCover = JsonUtils.getString(JsonUtils.getJsonItem(styles, position, null), "cover", "");
        styleId = JsonUtils.getString(JsonUtils.getJsonItem(styles, position, null),"styleId","");

        goodsStyleName.setText(goodsName);
        goodsStylePrice.setText( PriceUtils.beautify("¥" + goodsPrice) );

        if ( maxCount == 0 && !skuPull) {
            goodsOff.setVisibility(View.VISIBLE);
            goodsOff.setText("无库存");
            joinShoppingCat.setVisibility(View.GONE);
            buyNow.setVisibility(View.GONE);
        } else if ( maxCount != 0 && !skuPull) {
            goodsOff.setVisibility(View.GONE);
            if (buyNowShow) {
                buyNow.setVisibility(View.VISIBLE);
            } else {
                joinShoppingCat.setVisibility(View.VISIBLE);
            }
        }

        goodsCount.setText("库存" + maxCount + "件");
        ImageUtils.loadImage(glide, goodsCover, goodsStyleImage, R.mipmap.loadpicture);
    }

    //webView初始化
    private void webViewInit() {

        mWebView = findViewById(R.id.goods_webView);
        mWebView.setFocusable(false);

        mWebView.setWebViewClient(new WebViewClient() {

            @SuppressLint("HandlerLeak")
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                Log.d("testTimeout", "onPageStarted...........");
                // TODO Auto-generated method stub
                super.onPageStarted(view, url, favicon);

                timer = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                         /*
                         * 超时后,首先判断页面加载进度,超时并且进度小于10,就执行超时后的动作
                         */
                        if (ProductInformationActivity.this.mWebView.getProgress() < 10) {
                            Message msg1 = new Message();
                            msg.what = 1;
                            mHandler.sendMessage(msg1);
                            loadingDialog.dismiss();
                            loadFailView.setText("网络不给力~");
                            AlertManager.showErrorToast(ProductInformationActivity.this ,"网络不好，请确认网络哟", false);
                            loadFailView.setVisibility(View.VISIBLE);
                            loadFailView.setText("数据居然走丢了~");
                        }
                    }
                };
                timer.sendEmptyMessageAtTime(1, outTime);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                loadingDialog.dismiss();
                loadFailView.setText("网络不给力~");
                AlertManager.showErrorToast(ProductInformationActivity.this ,"网络不好，请确认网络哟", false);
                loadFailView.setVisibility(View.VISIBLE);
                loadFailView.setText("数据居然走丢了~");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loadingDialog.dismiss();
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
                timer.removeMessages(1);
            }
        });

        mWebView.loadUrl(AppConfig.ARTICLE_CONTENT + detailUrl);
        //mWebView.loadUrl("https://wx.goochao.com/editor/show.html?articleId=NbP42j0D");

    }

    //banner初始化
    private void convenientBannerInit() {

        String banners = JsonUtils.getString(productArray, "banner", null);
        bannerImages = Arrays.asList(banners.split(","));
        bannerImage = new String[bannerImages.size()];
        for (int i = 0; i < bannerImages.size(); i++) {
            bannerImages.set(i, bannerImages.get(i)+ AppConfig.IMAGE_COMPRESS);
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

        double width;
        double height;
        width = WindowUtils.getDeviceWidth(ProductInformationActivity.this);
        height = width * 0.8 ;
        int widthInt = (int) width;
        int heightInt = (int) height;
        convenientBanner.setLayoutParams(new RelativeLayout.LayoutParams(widthInt, heightInt));
    }
    @Override
    public void onItemClick(int position) {
        String currentSrc = bannerImages.get(position);
        Intent intent = new Intent();
        intent.setClass(ProductInformationActivity.this, GalleryActivity.class);
        intent.putExtra("isLocal",false );
        intent.putExtra("currentSrc",currentSrc );
        intent.putExtra("images", bannerImage);
        ProductInformationActivity.this.startActivity(intent);
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

    //推荐相似商品适配器
    public class GoodsSimilarityAdapter extends CommonAdapter<JSONObject> {
        GoodsSimilarityAdapter(Context context, int layoutId, List<JSONObject> datas) {
            super(context, layoutId, datas);
        }

        @Override
        protected void convert(ViewHolder holder, JSONObject item, int position) {

            String title = JsonUtils.getString(item,"detail.title","");
            String cover = ImageUtils.zoomResize(JsonUtils.getString(item,"detail.cover",""),512,512);
            String description = JsonUtils.getString(item,"detail.description","");
            final String goodsId = JsonUtils.getString(item,"detail.goodsId","");

            float price = (float) (JsonUtils.getDouble(item,"dynamicId.price", 0) / 100f);

            holder.setText(R.id.produceTitle_left,title);
            holder.setText(R.id.produceIntroduce_left,description);
            holder.setText(R.id.producePrice_left, PriceUtils.beautify("¥" + price));
            ImageView image = holder.getView(R.id.produceImage_left);

            double itemWidth = windowX * 0.35;
            double itemHeight = itemWidth * 11 / 15;
            LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) image.getLayoutParams();
            params.width = (int) itemWidth;
            params.height = (int) itemHeight;
            image.setLayoutParams(params);

            ImageUtils.loadImage(glide, cover, image, R.mipmap.loadpicture);
            holder.getView(R.id.goods_detail_zoom_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(ProductInformationActivity.this, NewProductInformationActivity.class);
                    intent.putExtra("goodsId",goodsId);
                    ProductInformationActivity.this.startActivity(intent);
                }
            });

        }
    }

    //优惠券的适配器
    class CouponAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return couponArray == null ? 0 : couponArray.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(couponArray, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            CouponViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new CouponViewHolder();
                convertView = LayoutInflater.from(ProductInformationActivity.this).inflate(R.layout.view_coupon_item, null);

                viewHolder.couponName = convertView.findViewById(R.id.coupon_title);
                viewHolder.couponTime = convertView.findViewById(R.id.coupon_time);
                viewHolder.couponDes = convertView.findViewById(R.id.coupon_des);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (CouponViewHolder) convertView.getTag();
            }

            JSONObject item = getItem(i);
            viewHolder.couponName.setText(JsonUtils.getString(item, "name", "" ));
            viewHolder.couponTime.setText(JsonUtils.getString(item, "expireDescription", "" ));
            viewHolder.couponDes.setText(JsonUtils.getString(item, "description", "" ));

            return convertView;
        }
    }

    class CouponViewHolder {
        TextView couponName;
        TextView couponDes;
        TextView couponTime;
    }

    //到商品评价页面
    class CommentsCountListener implements View.OnClickListener {

        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(ProductInformationActivity.this, CommentsActivity.class);
            intent.putExtra("goodsId",currentGoodsId);
            ProductInformationActivity.this.startActivity(intent);
        }
    }

    //收藏商品点击事件
    class CollectGoods implements View.OnClickListener {

        public void onClick(View v) {
            collectGoods();
        }
    }

    //点击优惠卷按钮
    class Coupon implements View.OnClickListener {

        public void onClick(View view){

            showCoupons(ProductInformationActivity.this);
        }
    }

    //显示优惠券的popwindow
    private void showCoupons(Activity activity) {

        window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.alpha = 0.5f;
        window.setAttributes(params);

        if (couponPopUpWindow == null){
            final View contentView = LayoutInflater.from(ProductInformationActivity.this).inflate(R.layout.view_goods_detail_coupon_layout, null);

            ListView couponListView = contentView.findViewById(R.id.coupon_list_view);
            TextView getCoupons = contentView.findViewById(R.id.coupon_btn);

            couponPopUpWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            couponPopUpWindow.setContentView(contentView);
            couponPopUpWindow.setAnimationStyle(R.style.mypopwindow_anim_style);

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            couponPopUpWindow.setWidth(Math.round(screenWidth));

            RelativeLayout cancel_btn;
            cancel_btn = contentView.findViewById(R.id.cancel_coupon);
            cancel_btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    couponPopUpWindow.dismiss();
                }
            });

            CouponAdapter couponAdapter = new CouponAdapter();
            couponListView.setAdapter(couponAdapter);
            getCoupons.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getCoupon();
                }
            });

            couponListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selectId = JsonUtils.getString(JsonUtils.getJsonItem(couponArray,position,null),"couponId","");
                    getSingleCoupon(selectId);
                }
            });

        }
        couponPopUpWindow.showAtLocation(findViewById(R.id.coupon_layout), Gravity.BOTTOM, 0, 0);
        couponPopUpWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = window.getAttributes();
                params.alpha = 1.0f;
                window.setAttributes(params);
            }
        });
    }

    //关闭展示商品 选择规格的区域
    class closeGoodsClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            showGoodsDetail.dismiss();
        }
    }

    // 点击展示商品信息 选择规格
    class showGoodsClick implements View.OnClickListener {

        private String clickType;

        showGoodsClick(String clickType) {
            this.clickType = clickType;
        }

        @Override
        public void onClick(View view) {
            showGoods(clickType,ProductInformationActivity.this);
        }

    }

    //显示商品规格的popwindow
    private void showGoods(final String clickType , Activity activity) {

        window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.alpha = 0.5f;
        window.setAttributes(params);

        if (showGoodsDetail == null){

            final View contentView = LayoutInflater.from(ProductInformationActivity.this).inflate(R.layout.view_goods_detail_layout, null);

            joinShoppingCat = contentView.findViewById(R.id.join_to_shopping_cat);
            goodsOff = contentView.findViewById(R.id.goods_off);
            buyNow = contentView.findViewById(R.id.buy_now);
            final TagFlowLayout tag = contentView.findViewById(R.id.tag_layout);
            goodsStyleName = contentView.findViewById(R.id.goods_style_name);
            goodsStylePrice = contentView.findViewById(R.id.goods_style_price);
            goodsStyleImage = contentView.findViewById(R.id.goods_style_image);
            goodsCount = contentView.findViewById(R.id.goods_count);
            final TextView goodsNumber = contentView.findViewById(R.id.goods_number);

            //点开默认选中第一个规格
            choiceStyle(tagPosition);
            goodsNumber.setText("1");

            //初始化popwindow
            showGoodsDetail = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            showGoodsDetail.setContentView(contentView);
            showGoodsDetail.setAnimationStyle(R.style.mypopwindow_anim_style);

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            showGoodsDetail.setWidth(Math.round(screenWidth));

            //关闭商品规格选择
            RelativeLayout cancel_btn = contentView.findViewById(R.id.close_goods_detail);
            cancel_btn.setOnClickListener(new closeGoodsClick());
            ImageView closeBtn = contentView.findViewById(R.id.close_goods);
            closeBtn.setOnClickListener(new closeGoodsClick());

            //阻止事件冒泡
            contentView.findViewById(R.id.show_goods_Layout).setClickable(true);

            final TextView addView = contentView.findViewById(R.id.add_goods);
            final TextView miuView = contentView.findViewById(R.id.minus_goods);
            if (amount == 1){
                miuView.setTextColor(getResources().getColor(R.color.gray_add));
            }else {
                miuView.setTextColor(getResources().getColor(R.color.BLACK));
            }
            //减少商品数量
            miuView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if( amount >= 2 ) {
                        amount--;
                        goodsNumber.setText(amount+"");
                    }
                    if (amount == maxCount){
                        addView.setTextColor(getResources().getColor(R.color.gray_add));
                    }else {
                        addView.setTextColor(getResources().getColor(R.color.BLACK));
                    }
                    if (amount == 1){
                        miuView.setTextColor(getResources().getColor(R.color.gray_add));
                    }else {
                        miuView.setTextColor(getResources().getColor(R.color.BLACK));
                    }
                }
            });

            //增加商品数量
            addView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if( amount < maxCount ) {
                        amount++;
                        goodsNumber.setText(amount+"");
                    }
                    if (amount == maxCount){
                        addView.setTextColor(getResources().getColor(R.color.gray_add));
                    }else {
                        addView.setTextColor(getResources().getColor(R.color.BLACK));
                    }
                    if (amount == 1){
                        miuView.setTextColor(getResources().getColor(R.color.gray_add));
                    }else {
                        miuView.setTextColor(getResources().getColor(R.color.BLACK));
                    }
                }
            });

            //加入购物车
            joinShoppingCat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addShoppingCat();
                }
            });

            //立即购买
            buyNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buyNow();
                }
            });

            //商品规格
            tag.setAdapter(new TagAdapter<String>(tagArrayName) {
                @Override
                public View getView(FlowLayout parent, int position, String styleName) {
                    styleName = getItem(position);
                    View view = LayoutInflater.from(ProductInformationActivity.this).inflate(R.layout.goods_tag_view_item, null);
                    TextView tagName = view.findViewById(R.id.tag_text);
                    if( position == tagPosition){
                        tagName.setBackgroundResource(R.drawable.goods_tag_select);
                        tagName.setTextColor(getResources().getColor(R.color.WHITE));
                    } else {
                        tagName.setBackgroundResource(R.drawable.goods_tag_normal);
                        tagName.setTextColor(getResources().getColor(R.color.Default_Gray));
                    }
                    tagName.setText(styleName);

                    return view;
                }
            });

            //设置规格的点击事件
            tag.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
                @Override
                public boolean onTagClick(View view, int position, FlowLayout parent) {
                    if ( position != tagPosition){
                        tagPosition = position;
                        tag.onChanged();
                        goodsNumber.setText("1");

                        choiceStyle(tagPosition);
                    }
                    return false;
                }
            });
        }

        showGoodsDetail.showAtLocation(findViewById(R.id.show_goods_detail), Gravity.BOTTOM, 0, 0);
        showGoodsDetail.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = window.getAttributes();
                params.alpha = 1.0f;
                window.setAttributes(params);
            }
        });

        //判断是否下架 点击立即购买按钮或者加入购物车
        if (skuPull){
            goodsOff.setVisibility(View.VISIBLE);
            goodsOff.setText("已下架");
            joinShoppingCat.setVisibility(View.GONE);
            buyNow.setVisibility(View.GONE);
        } else if ( clickType.equals("0") ) {
            joinShoppingCat.setVisibility(View.VISIBLE);
            buyNow.setVisibility(View.GONE);
            buyNowShow = false;
            goodsOff.setVisibility(View.GONE);
        } else {
            joinShoppingCat.setVisibility(View.GONE);
            buyNow.setVisibility(View.VISIBLE);
            buyNowShow = true;
            goodsOff.setVisibility(View.GONE);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    protected void onResume() {
        super.onResume();
        loadShoppingCatCount();
    }
}
