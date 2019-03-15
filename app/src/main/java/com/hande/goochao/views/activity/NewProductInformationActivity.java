package com.hande.goochao.views.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
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
import com.hande.goochao.commons.ZhichiReceiver;
import com.hande.goochao.commons.ZhichiUtils;
import com.hande.goochao.commons.controller.BadgeView;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.commons.views.gallery.GalleryActivity;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.CustomerServiceUtils;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.NumberUtils;
import com.hande.goochao.utils.PriceUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.NoScrollGridView;
import com.hande.goochao.views.components.NoScrollListView;
import com.hande.goochao.views.components.ZoomView;
import com.hande.goochao.views.widget.SharePopupWindow;
import com.hande.goochao.views.widget.SkuView;
import com.sobot.chat.SobotApi;
import com.sobot.chat.api.enumtype.SobotChatTitleDisplayMode;
import com.sobot.chat.api.model.ConsultingContent;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.kpswitch.util.ViewUtil;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;

import java.security.interfaces.ECKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/6/19.
 */
public class NewProductInformationActivity extends ToolBarActivity implements OnItemClickListener, View.OnClickListener, ZhichiReceiver.OnMessageReceive {

    private ImageOptions options = new ImageOptions.Builder()
            .setLoadingDrawableId(R.mipmap.loadpicture)
            .setImageScaleType(ImageView.ScaleType.FIT_XY).build();

    private CustomLoadingDialog loadingDialog;

    @ViewInject(R.id.load_fail_new_product_introduce_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.loading_goods_page)
    private ImageView loadingView;

    private String currentGoodsId;

    private JSONObject productArray = new JSONObject();
    private JSONArray contentArray = new JSONArray();
    private List<String> bannerImages = new ArrayList<>();
    private String[] bannerImage;
    private JSONArray similarityData = new JSONArray();
    private JSONArray dimArray = new JSONArray();
    private JSONArray paraArray = new JSONArray();
    private JSONArray detailsArray = new JSONArray();
    private JSONArray colorArray = new JSONArray();
    private JSONArray sceneArray = new JSONArray();
    private JSONObject sceneObject;
    private String banners;
    private List<String> styleIds = new ArrayList<>();
    private int bannerIndex;// banner 当前页码
    private int bannerAmount = 0;// banner 总量

    private PopupWindow popupWindow;

    private boolean goodsOffBoolean;
    private boolean buyNowShow;
    //判断弹窗是否弹出
    private boolean popShowBoolean = false;

    /**
     * 固定xml文件控件绑定
     */
    @ViewInject(R.id.product_banner)
    private ConvenientBanner convenientBanner;
    @ViewInject(R.id.similar_title_layout)
    private View similarTitleLayout;
    @ViewInject(R.id.similar_text_view)
    private TextView similarTextView;
    @ViewInject(R.id.similar_product_grid_view)
    private NoScrollGridView similarGridView;
    @ViewInject(R.id.product_name)
    private TextView productNameView;
    @ViewInject(R.id.product_introduce)
    private TextView productDescView;
    @ViewInject(R.id.product_price)
    private TextView productPriceView;
    @ViewInject(R.id.no_use_title_1)
    private TextView noUseTitle;
    @ViewInject(R.id.no_use_title_2)
    private TextView noUseTitleSecond;
    @ViewInject(R.id.banner_layout)
    private View bannerLayout;
    @ViewInject(R.id.old_price)
    private TextView productOldPrice;
    @ViewInject(R.id.discount_view)
    private TextView discountView;
    @ViewInject(R.id.sale_out_view)
    private TextView saleOutView;

    @ViewInject(R.id.scene_layout)
    private View sceneView;
    @ViewInject(R.id.product_link_image)
    private ImageView linkImage;
    @ViewInject(R.id.link_product_list_view)
    private NoScrollListView linkProductView;
    @ViewInject(R.id.comments_count)
    private TextView commentsCountView;
    @ViewInject(R.id.to_comments)
    private View toCommentsView;

    //浮动购物条
    @ViewInject(R.id.custom_layout)
    private View customView; //客服
    @ViewInject(R.id.shopping_cat)
    private View shoppingCartView; //购物车
    @ViewInject(R.id.outside_join_to_shopping_cat)
    private View addShoppingCartView; //加入购物车
    @ViewInject(R.id.outside_buy_now)
    private View buyNowView; //立即购买
    @ViewInject(R.id.outside_goods_off)
    private View goodsOffView; //已下架
    @ViewInject(R.id.shopping_goods_count)
    private BadgeView shoppingGoodsCount; //购物车红点中数量
    @ViewInject(R.id.unread_count)
    private BadgeView unreadCount; //未读客服信息数量
    @ViewInject(R.id.go_to_scene)
    private View toSpaceDetailView;

    @ViewInject(R.id.new_product_information_tag_layout)
    private TagFlowLayout tagLayout;

    private SimilarAdapter similarAdapter;
    private ParamAdapter paramAdapter;
    private SpaceGoodsAdapter spaceGoodsAdapter;
    private CouponAdapter couponAdapter;
    @ViewInject(R.id.product_des_add_view)
    private LinearLayout addLinearLayout;

    @ViewInject(R.id.skuView)
    private SkuView skuView;
    @ViewInject(R.id.select_page_number)
    private TextView selectPageNumber;
    @ViewInject(R.id.total_page_number)
    private TextView totalPageNumber;

    private View rightTopView;
    private ImageView collectBtn;
    private ImageView haveCollectBtn;
    private ImageView shareBtn;

    private String title;
    private String goodsNumber;

    private Window window;
    private Window windowCoupon;
    private PopupWindow showGoodsDetail;
    private TextView joinShoppingCat;
    private TextView goodsOff;
    private TextView buyNow;
    private TextView goodsStyleName;
    private TextView goodsSubName;
    private TextView goodsStylePrice;
    private ImageView goodsStyleImage;
    private TextView goodsCountNumber;
    private View disMissView;
    private TextView addView;
    private TextView miuView;
    private TextView popOldPrice;
    private ImageView closeView;
    private int amount;
    private String styleId;
    private int maxCount;

    private LinearLayout dimLinearLayout;
    private ImageView styleBigView;
    private TextView txtStyleName;

    private SkuView skuPopView;

    private String tagValue;
    private int typeValue;

    //style中Name以及subName组合成的数组
    private List<String> styles = new ArrayList<>();

    private int kWidth;
    private LayoutInflater inflater;

    /**
     * 优惠劵弹窗
     */
    private ListView couponList;

    private JSONArray couponData = new JSONArray();

    //无库存是否可售卖
    private boolean noStorageSale = false;
    //滑动的初始位置
    private int oldPosition = 0;
    //是否为倒序滑动
    private boolean opposite;

    private GlideRequests glide;

    private ZhichiReceiver zhichiReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product_information);
        setTitle("商品详情");
        glide = GlideApp.with(this);
        hideLine();
        inflater = LayoutInflater.from(this);
        kWidth = WindowUtils.getDeviceWidth(this);
        currentGoodsId = getIntent().getStringExtra("goodsId");

        loadingDialog = new CustomLoadingDialog(this);

        boldMethod(noUseTitle);
        boldMethod(noUseTitleSecond);
        boldMethod(similarTextView);
        couponAdapter = new CouponAdapter();

        toCommentsView.setOnClickListener(this);
        customView.setOnClickListener(this);
        shoppingCartView.setOnClickListener(this);
        addShoppingCartView.setOnClickListener(new showGoodsClick("0"));
        buyNowView.setOnClickListener(new showGoodsClick("1"));

        rightTopView = inflater.inflate(R.layout.layout_new_product_right_view, null);
        rightTopView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        collectBtn = rightTopView.findViewById(R.id.new_product_collect_btn);
        collectBtn.setOnClickListener(this);
        haveCollectBtn = rightTopView.findViewById(R.id.new_product_collect_had_btn);
        haveCollectBtn.setOnClickListener(this);
        shareBtn = rightTopView.findViewById(R.id.new_product_share_btn);
        shareBtn.setOnClickListener(this);
        showRightCustomView(rightTopView);
        toSpaceDetailView.setOnClickListener(this);

        similarAdapter = new SimilarAdapter();
        similarGridView.setAdapter(similarAdapter);

        convenientBanner.setFocusable(true);
        convenientBanner.setFocusableInTouchMode(true);
        convenientBanner.requestFocus();

        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadProductInformation();
            }
        });

        /**
         * action:ZhiChiConstants.sobot_unreadCountBrocast
         */
        IntentFilter filter = new IntentFilter();
        zhichiReceiver = new ZhichiReceiver(this);
        filter.addAction(ZhiChiConstant.sobot_unreadCountBrocast);
        registerReceiver(zhichiReceiver, filter);

        getUnReadCount();
        loadShoppingCatCount();
        loadProductInformation();
    }

    /**
     * 添加右上navbar点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v == collectBtn) {
            collectGoods();
        } else if (v == haveCollectBtn) {
            deleteCollection();
        } else if (v == shareBtn) {
            String title = JsonUtils.getString(productArray, "title", null);
            String desc = JsonUtils.getString(productArray, "description", null);
            String cover = JsonUtils.getString(productArray, "cover", null);
            String url = RestfulUrl.build(AppConfig.GOODS_SHARE_URL, ":goodsId", currentGoodsId);
            new SharePopupWindow(this).show(url, cover, title, desc);
        } else if (v == customView) {
//            JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(NewProductInformationActivity.this);
//            CustomerServiceUtils.CustomerServiceModel model = new CustomerServiceUtils.CustomerServiceModel();
//            model.setUname(JsonUtils.getString(loginResult, "nickName", null));
//            model.setRealname(model.getUname());
//            model.setFace(JsonUtils.getString(loginResult, "head", null));
//            model.setGoodsNumber(goodsNumber);
//            model.setGoodsTitle(title);
//            CustomerServiceUtils.goService(model, NewProductInformationActivity.this);

            //设置用户自定义字段
            Map<String, String> customerFields = new HashMap<>();
            customerFields.put("customField2", JsonUtils.getString(productArray, "title", null)); // 商品名称
            customerFields.put("customField4", JsonUtils.getString(productArray, "goodsNumber", null)); // 商品编号
            customerFields.put("customField1", ""); // 订单号
            customerFields.put("customField3", ""); // 售后工单编号
            ZhichiUtils.startZhichi(this, customerFields);
        } else if (v == shoppingCartView) {
            if (!AppSessionCache.getInstance().isLogin(NewProductInformationActivity.this)) {
                Intent intent = new Intent(NewProductInformationActivity.this, LoginActivity.class);
                startActivity(intent);
                return;
            }

            Intent intent = new Intent();
            intent.setClass(NewProductInformationActivity.this, ShopCartActivity.class);
            NewProductInformationActivity.this.startActivity(intent);
        } else if (v == toCommentsView) {
            Intent intent = new Intent();
            intent.setClass(NewProductInformationActivity.this, CommentsActivity.class);
            intent.putExtra("goodsId", currentGoodsId);
            NewProductInformationActivity.this.startActivity(intent);
        } else if (v == toSpaceDetailView) {
            Intent intent = new Intent(NewProductInformationActivity.this, NewSpaceDetailActivity.class);
            String spaceId = JsonUtils.getString(sceneObject, "spaceId", "");
            intent.putExtra("chenLie", true);
            intent.putExtra("spaceId", spaceId);
            startActivity(intent);
        }
    }

    @Override
    public void onReceive(String content, int noReadNum) {
        getUnReadCount();
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

    // 获取商品详情
    private void loadProductInformation() {
        loadingView.setVisibility(View.VISIBLE);

        String url = RestfulUrl.build(AppConfig.NEW_PRODUCT_INFORMATION, ":goodsId", currentGoodsId);
        HttpRequest.get(url, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (JsonUtils.getCode(response) == 0) {
                        productArray = JsonUtils.getJsonObject(response, "data", null);
                        couponData = JsonUtils.getJsonArray(productArray, "coupons", null);

                        String contentObject = JsonUtils.getString(productArray, "content", null);
                        if (contentObject != null) {
                            JSONObject contentJs = JsonUtils.newJsonObject(contentObject, null);
                            contentArray = JsonUtils.getJsonArray(contentJs, "content", null);
                        } else {
                            loadFailView.setVisibility(View.VISIBLE);
                            loadingView.setVisibility(View.GONE);
                            shareBtn.setVisibility(View.GONE);
                            collectBtn.setVisibility(View.GONE);
                            loadFailView.setText("商品已经out了，找不到了。");
                            return;
                        }
                        colorArray = JsonUtils.getJsonArray(productArray, "styles", null);
                        for (int i = 0; i < colorArray.length(); i++) {
                            styles.add(i, JsonUtils.getString(JsonUtils.getJsonItem(colorArray, i, null), "name", "") +
                                    JsonUtils.getString(JsonUtils.getJsonItem(colorArray, i, null), "subName", ""));
                            String banner = JsonUtils.getString(JsonUtils.getJsonItem(colorArray, i, null), "banner", null);
                            List<String> bannerSize = Arrays.asList(banner.split(","));
                            int size = bannerSize.size();
                            if (i == 0) {
                                banners = banner;
                            } else {
                                banners = banners + "," + banner;
                            }
                            String id = JsonUtils.getString(JsonUtils.getJsonItem(colorArray, i, null), "styleId", "");
                            for (int n = 0; n < size; n++) {
                                styleIds.add(id);
                            }
                        }
                        bannerAmount = Arrays.asList(banners.split(",")).size();

                        sceneObject = JsonUtils.getJsonObject(productArray, "hotSpace", null);

                        commentsCountView.setText("(" + JsonUtils.getString(productArray, "commentsCount", "") + ")");
                        title = JsonUtils.getString(productArray, "title", null);

                        couponAdapter.notifyDataSetChanged();

                        goodsNumber = JsonUtils.getString(productArray, "goodsNumber", null);

                        //判断无库存是否可售卖
                        noStorageSale = JsonUtils.getBoolean(productArray, "noStorageSale", false);

                        //判断是否下架
                        goodsOffBoolean = JsonUtils.getBoolean(productArray, "skuPull", false);
                        if (goodsOffBoolean) {
                            goodsOffView.setVisibility(View.VISIBLE);
                            buyNowView.setVisibility(View.GONE);
                        }

                        boolean collect = JsonUtils.getBoolean(productArray, "collection", false);
                        if (collect) {
                            collectBtn.setVisibility(View.GONE);
                            haveCollectBtn.setVisibility(View.VISIBLE);
                        }

                        initSkuView();
                        initBanner();
                        initPageView();
                        initSpaceGoods();
                        addPageValue();
                        checkActivity();
                        loadSimilarity();

                        JSONArray styles = JsonUtils.getJsonArray(productArray, "styles", new JSONArray());
                        if (styles.length() > 0) {
                            skuView.setSelectedSku(JsonUtils.getJsonItem(styles, 0, null));
                        }
                    } else {
                        showError();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError();
                }

            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
                AlertManager.showErrorToast(NewProductInformationActivity.this, "服务器繁忙，请稍后重试", false);
            }
        });
    }

    // 查询推荐商品
    private void loadSimilarity() {
        Map<String, String> params = new HashMap<>();
        params.put("goodsId", "" + currentGoodsId);
        params.put("size", 6 + ""); // 默认只加载6条数据

        HttpRequest.get(AppConfig.SIMILARITY_PRODUCT, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    similarityData = JsonUtils.getJsonArray(response, "data", null);
                    if (similarityData.length() == 0) {
                        similarTitleLayout.setVisibility(View.GONE);
                    }
                    resetView();
                    similarAdapter.notifyDataSetChanged();
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
                AlertManager.showErrorToast(NewProductInformationActivity.this, "服务器繁忙，请稍后重试", false);
            }
        });
    }

    //判断商品是否有折扣，满减活动
    private void checkActivity() {
        tagValue = JsonUtils.getString(productArray, "discountTag", "");
        typeValue = JsonUtils.getInt(productArray, "discountType", 0);
        if (tagValue.equals("")) {
            discountView.setVisibility(View.GONE);
            saleOutView.setVisibility(View.GONE);
            productOldPrice.setVisibility(View.GONE);
        }                //原价 无活动
        else {
            if (typeValue == 2) {
                discountView.setVisibility(View.VISIBLE);
                saleOutView.setVisibility(View.GONE);
                discountView.setText(tagValue);
                productOldPrice.setVisibility(View.VISIBLE);
            }             //折扣
            else {
                discountView.setVisibility(View.GONE);
                saleOutView.setVisibility(View.VISIBLE);
                productOldPrice.setVisibility(View.GONE);
                saleOutView.setText(tagValue);
            }             //满减
        }
    }

    //初始化SkuView 并设置点击事件
    private void initSkuView() {
        skuView.setData(productArray);

        skuView.setListener(new SkuView.OnSkuChangeListener() {
            @Override
            public void onChange(JSONObject sku) {
                setBannerAndPrice(sku);
                if (skuPopView != null) {
                    skuPopView.setSelectedSku(sku);
                }
            }
        });
    }

    /**
     * banner初始化
     */
    private void initBanner() {
        WindowUtils.boldMethod(selectPageNumber);
        selectPageNumber.setText("1");
        if (banners != null) {
            bannerLayout.setVisibility(View.VISIBLE);
            bannerImages = Arrays.asList(banners.split(","));
            bannerImage = new String[bannerImages.size()];
            totalPageNumber.setText("/" + bannerImages.size());
            for (int i = 0; i < bannerImages.size(); i++) {
                bannerImages.set(i, bannerImages.get(i) + AppConfig.IMAGE_COMPRESS);
                bannerImage[i] = bannerImages.get(i);
            }

            int width = WindowUtils.getDeviceWidth(NewProductInformationActivity.this);
            int height = (int) (width * 320f / 375f);
            convenientBanner.setLayoutParams(new FrameLayout.LayoutParams(width, height));

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
                    .setOnItemClickListener(this)
                    .setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                        }

                        @Override
                        public void onPageSelected(int position) {
                            if (position > oldPosition) {
                                opposite = false;
                            } else {
                                opposite = true;
                            }
                            oldPosition = position;
                            selectPageNumber.setText("" + (position + 1));
                            String nowStyleId = styleIds.get(position);
                            for (int i = 0; i < colorArray.length(); i++) {
                                String id = JsonUtils.getString(JsonUtils.getJsonItem(colorArray, i, null), "styleId", "");
                                if (id.equals(nowStyleId)) {
                                    JSONObject sku = JsonUtils.getJsonItem(colorArray, i, null);
                                    skuView.setSelectedSku(sku);
                                    if (skuPopView != null) {
                                        skuPopView.setSelectedSku(sku);
                                        setPopWindow(sku);
                                    }
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });
            convenientBanner.setCanLoop(true);
            convenientBanner.setCanLoop(false);
            convenientBanner.setPointViewVisible(false);
        } else {
            bannerLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(int position) {
        String currentSrc = bannerImages.get(position);
        Intent intent = new Intent();
        intent.setClass(NewProductInformationActivity.this, GalleryActivity.class);
        intent.putExtra("isLocal", false);
        intent.putExtra("currentSrc", currentSrc);
        intent.putExtra("images", bannerImage);
        NewProductInformationActivity.this.startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showError() {
        loadingView.setVisibility(View.GONE);
        loadFailView.setVisibility(View.VISIBLE);
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    //推荐相似商品适配器
    class SimilarAdapter extends BaseAdapter {

        class SimilarViewHolder {
            ZoomView zoomView;
            TextView similarName;
            TextView similarIntroduce;
            TextView similarPrice;
            ImageView imageView;
            TextView discountView;
            TextView saleOutView;
            TextView productOldPrice;

            SimilarViewHolder(View convertView) {
                zoomView = convertView.findViewById(R.id.similar_product_zoom_view);
                similarName = convertView.findViewById(R.id.similar_product_name);
                similarIntroduce = convertView.findViewById(R.id.similar_product_introduce);
                similarPrice = convertView.findViewById(R.id.similar_product_price);
                imageView = convertView.findViewById(R.id.item_cover);
                discountView = convertView.findViewById(R.id.discount_view);
                saleOutView = convertView.findViewById(R.id.sale_out_view);
                productOldPrice = convertView.findViewById(R.id.old_price);
            }
        }

        @Override
        public int getCount() {
            return similarityData == null ? 0 : similarityData.length();
        }

        @Override
        public JSONObject getItem(int position) {
            return JsonUtils.getJsonItem(similarityData, position, null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.layout_new_product_similay_item, null);
                convertView.setTag(new SimilarViewHolder(convertView));
            }
            SimilarViewHolder viewHolder = (SimilarViewHolder) convertView.getTag();
            boldMethod(viewHolder.similarName);
            boldMethod(viewHolder.similarPrice);

            JSONObject item = getItem(i);

            //判断商品类型（满减 折扣 原价）
            String tagValue = JsonUtils.getString(getItem(i), "detail.discountTag", "");
            if (tagValue.equals("")) {
                viewHolder.discountView.setVisibility(View.GONE);
                viewHolder.saleOutView.setVisibility(View.INVISIBLE);
                viewHolder.productOldPrice.setVisibility(View.GONE);

                double price = JsonUtils.getDouble(item, "dynamicId.price", 0) / 100f;
                double priceValue = NumberUtils.decimalDouble(price);
                viewHolder.similarPrice.setText("¥" + PriceUtils.beautify(priceValue));
            }                //原价 无活动
            else {
                if (JsonUtils.getInt(getItem(i), "detail.discountType", 0) == 2) {
                    viewHolder.discountView.setVisibility(View.VISIBLE);
                    viewHolder.saleOutView.setVisibility(View.GONE);
                    viewHolder.discountView.setText(tagValue);

                    viewHolder.productOldPrice.setVisibility(View.VISIBLE);

                    double oldPrice = JsonUtils.getDouble(item, "dynamicId.price", 0) / 100f;
                    double oldPriceValue = NumberUtils.decimalDouble(oldPrice);
                    viewHolder.productOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //原价设置中划线
                    viewHolder.productOldPrice.setText("¥" + PriceUtils.beautify(oldPriceValue));

                    double newPrice = JsonUtils.getDouble(item, "dynamicId.discountPrice", 0) / 100f;
                    double newPriceValue = NumberUtils.decimalDouble(newPrice);
                    viewHolder.similarPrice.setText("¥" + PriceUtils.beautify(newPriceValue));
                }             //折扣
                else {
                    viewHolder.discountView.setVisibility(View.GONE);
                    viewHolder.saleOutView.setVisibility(View.VISIBLE);
                    viewHolder.productOldPrice.setVisibility(View.GONE);
                    viewHolder.saleOutView.setText(tagValue);
                    double price = JsonUtils.getDouble(item, "dynamicId.price", 0) / 100f;
                    double priceValue = NumberUtils.decimalDouble(price);
                    viewHolder.similarPrice.setText("¥" + PriceUtils.beautify(priceValue));
                }             //满减
            }

            final String goodsId = JsonUtils.getString(item, "detail.goodsId", "");

            double itemWidth = kWidth * 0.35;
            double itemHeight = itemWidth;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.imageView.getLayoutParams();
            params.width = (int) itemWidth;
            params.height = (int) itemHeight;
            viewHolder.imageView.setLayoutParams(params);

            ImageUtils.loadImage(glide, JsonUtils.getString(item, "detail.cover", ""),
                    viewHolder.imageView, -1);
            viewHolder.zoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(NewProductInformationActivity.this, NewProductInformationActivity.class);
                    intent.putExtra("goodsId", goodsId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    NewProductInformationActivity.this.startActivity(intent);
                }
            });
            viewHolder.similarName.setText(JsonUtils.getString(item, "detail.title", ""));

            viewHolder.similarIntroduce.setText(JsonUtils.getString(item, "detail.description", ""));

            return convertView;
        }
    }

    /**
     * 动态添加控件方法
     */
    private void initPageView() {
        // 20dp对应的px值
        int pxValue20 = dip2px(20);
        // 35dp对应px值
        int pxValue35 = dip2px(35);

        TextView mainTitle = new TextView(NewProductInformationActivity.this);
        mainTitle.setPadding(pxValue20, pxValue35, pxValue20, WindowUtils.dpToPixels(this, 25));
        mainTitle.setText(JsonUtils.getString(JsonUtils.getJsonItem(contentArray, 0, null), "title", ""));
        mainTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mainTitle.setTextColor(getResources().getColor(R.color.Black_Gray));
        LinearLayout.LayoutParams paramTitle = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramTitle.gravity = Gravity.CENTER_HORIZONTAL;
        mainTitle.setLayoutParams(paramTitle);
        mainTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        boldMethod(mainTitle);
        addLinearLayout.addView(mainTitle);

        for (int i = 1; i < contentArray.length(); i++) {
            String type = JsonUtils.getString(JsonUtils.getJsonItem(contentArray, i, null), "type", "");
            if (type.equals("text")) {      // 详情页_文字描述
                LinearLayout textItem = (LinearLayout) inflater.inflate(R.layout.goods_detail_text_item, null);
                TextView titleView = textItem.findViewById(R.id.txtTitle);
                TextView descView = textItem.findViewById(R.id.txtDesc);

                String title = JsonUtils.getString(JsonUtils.getJsonItem(contentArray, i, null), "title", "");
                String desc = JsonUtils.getString(JsonUtils.getJsonItem(contentArray, i, null), "desc", "");
                titleView.setText(title);
                boldMethod(titleView);
                descView.setText(desc);
                if (desc.isEmpty()) {
                    descView.setVisibility(View.GONE);
                } else {
                    descView.setVisibility(View.VISIBLE);
                }
                addLinearLayout.addView(textItem);
            } else if (type.equals("image")) {  // 详情页_图片
                JSONObject item = JsonUtils.getJsonItem(contentArray, i, null);
                ImageView imageView = new ImageView(NewProductInformationActivity.this);
                String cover = JsonUtils.getString(item, "url", "");

                int cwidth = WindowUtils.getDeviceWidth(NewProductInformationActivity.this);
                int width = JsonUtils.getInt(item, "width", 0);
                int height = JsonUtils.getInt(item, "height", 0);
                if (width == 0 || height == 0) {
                    height = (int) (cwidth * (374.0 / 375));
                } else {
                    height = (int) (cwidth * (height * 1f / width));
                }
                LinearLayout.LayoutParams imageParam = new LinearLayout.LayoutParams(cwidth, height);
                imageView.setLayoutParams(imageParam);
                ImageUtils.loadImage(glide, ImageUtils.imageslim(cover), imageView, R.mipmap.loadpicture);
//                imageView.setAdjustViewBounds(true);
                addLinearLayout.addView(imageView);
            } else if (type.equals("dim")) {    // 详情页_尺寸
                View dimView = inflater.inflate(R.layout.goods_detail_dimension, null);
                addLinearLayout.addView(dimView);

                //尺寸标题加粗
                TextView txtEngDim = dimView.findViewById(R.id.txtDimEnglish);
                TextView txtChiDim = dimView.findViewById(R.id.txtDimChinese);
                WindowUtils.boldMethod(txtEngDim);
                WindowUtils.boldMethod(txtChiDim);

                dimArray = JsonUtils.getJsonArray(JsonUtils.getJsonItem(contentArray, i, null), "content", null);

                // 尺寸名称
                txtStyleName = dimView.findViewById(R.id.txtStyleName);
                final String defaultName = JsonUtils.getString(JsonUtils.getJsonItem(dimArray, 0, null), "name", "");
                txtStyleName.setText(defaultName);

                // 尺寸大图片
//                dimArray = JsonUtils.getJsonArray(JsonUtils.getJsonItem(contentArray, i, null), "content", null);
                styleBigView = dimView.findViewById(R.id.styleImage);
                String defaultCover = JsonUtils.getString(JsonUtils.getJsonItem(dimArray, 0, null), "image", "");
                try {
                    dimArray.getJSONObject(0).put("checked", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ImageUtils.loadImage(glide, defaultCover, styleBigView, R.mipmap.loadpicture);
                int height = (int) (kWidth * 200f / 375f);
                styleBigView.setLayoutParams(new LinearLayout.LayoutParams(kWidth, height));

                // 尺寸小图片
                dimLinearLayout = dimView.findViewById(R.id.add_dimension_small_img);
                if (dimArray.length() == 1) {
                    dimLinearLayout.setVisibility(View.GONE);
                } else {
                    addDim();
                }

            } else if (type.equals("parameter")) {  // 详情页_参数
                View paramsView = inflater.inflate(R.layout.goods_detail_parameter, null);
                addLinearLayout.addView(paramsView);

                TextView paraEnglishView = paramsView.findViewById(R.id.txtParaEng);
                boldMethod(paraEnglishView);

                TextView paraChineseView = paramsView.findViewById(R.id.txtParaZh);
                boldMethod(paraChineseView);

                paraArray = JsonUtils.getJsonArray(JsonUtils.getJsonItem(contentArray, i, null), "content", null);
                NoScrollListView noScrollListView = paramsView.findViewById(R.id.paramsListView);
                paramAdapter = new ParamAdapter();
                noScrollListView.setAdapter(paramAdapter);
                paramAdapter.notifyDataSetChanged();

                TextView visionEnglishView = paramsView.findViewById(R.id.txtVisionEng);
                boldMethod(visionEnglishView);

                TextView visionChineseView = paramsView.findViewById(R.id.txtVisonChi);
                boldMethod(visionChineseView);
            } else if (type.equals("detail")) { // 详情页_细节
                View detailsView = inflater.inflate(R.layout.goods_detail_detail, null);
                addLinearLayout.addView(detailsView);

                detailsArray = JsonUtils.getJsonArray(JsonUtils.getJsonItem(contentArray, i, null), "content", null);

                TextView paraEnglishView = detailsView.findViewById(R.id.txtDetailEng);
                boldMethod(paraEnglishView);

                TextView paraChineseView = detailsView.findViewById(R.id.txtDetailZh);
                boldMethod(paraChineseView);

                JSONObject leftObject = JsonUtils.getJsonItem(detailsArray, 0, new JSONObject());
                JSONObject rightObject = JsonUtils.getJsonItem(detailsArray, 1, new JSONObject());
                JSONObject bottomObject = JsonUtils.getJsonItem(detailsArray, 2, new JSONObject());

                // 左细节
                ImageView leftImage = detailsView.findViewById(R.id.leftCover);
                String leftCover = JsonUtils.getString(leftObject, "image", "");
                ImageUtils.loadImage(glide,leftCover, leftImage, R.mipmap.loadpicture);
                int imageWidth = (kWidth - dip2px(35)) / 2;
                int imageHeight = (int) (imageWidth * 200f / 170f);
                leftImage.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageHeight));
//                leftImage.setAdjustViewBounds(true);

                TextView txtLeftTitle = detailsView.findViewById(R.id.txtLeftTitle);
                boldMethod(txtLeftTitle);
                String leftTitle = JsonUtils.getString(leftObject, "title", "");
                leftTitle = removeFirstAndLastLineMark(leftTitle);
                txtLeftTitle.setText(leftTitle);
                String leftDesc = JsonUtils.getString(leftObject, "desc", "");
                leftDesc = removeFirstAndLastLineMark(leftDesc);
                TextView txtLeftDesc = detailsView.findViewById(R.id.txtLeftDesc);
                txtLeftDesc.setText(leftDesc);

                //右细节
                ImageView rightImage = detailsView.findViewById(R.id.rightCover);
                String rightCover = JsonUtils.getString(rightObject, "image", "");
                ImageUtils.loadImage(glide, rightCover, rightImage, R.mipmap.loadpicture);
                rightImage.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageHeight));
//                rightImage.setAdjustViewBounds(true);

                TextView txtRightTitle = detailsView.findViewById(R.id.txtRightTitle);
                boldMethod(txtRightTitle);
                String rightTitle = JsonUtils.getString(rightObject, "title", "");
                rightTitle = removeFirstAndLastLineMark(rightTitle);
                txtRightTitle.setText(rightTitle);
                String rightDesc = JsonUtils.getString(rightObject, "desc", "");
                rightDesc = removeFirstAndLastLineMark(rightDesc);
                TextView txtRightDesc = detailsView.findViewById(R.id.txtRightDesc);
                txtRightDesc.setText(rightDesc);

                // 下边细节
                ImageView bottomImage = detailsView.findViewById(R.id.bottomCover);
                String bottomCover = JsonUtils.getString(bottomObject, "image", "");

                int height = (int) (kWidth * 250f / 345f);
                bottomImage.setLayoutParams(new LinearLayout.LayoutParams(kWidth, height));
                ImageUtils.loadImage(glide, bottomCover, bottomImage, R.mipmap.loadpicture);
//                bottomImage.setAdjustViewBounds(true);

                TextView txtBottomTitle = detailsView.findViewById(R.id.txtBottomTitle);
                boldMethod(txtBottomTitle);
                String bottomTitle = JsonUtils.getString(bottomObject, "title", "");
                bottomTitle = removeFirstAndLastLineMark(bottomTitle);
                txtBottomTitle.setText(bottomTitle);
                TextView txtBottomDesc = detailsView.findViewById(R.id.txtBottomDesc);
                String bottomDesc = JsonUtils.getString(bottomObject, "desc", "");
                bottomDesc = removeFirstAndLastLineMark(bottomDesc);
                txtBottomDesc.setText(bottomDesc);
            }
        }
    }

    /**
     * 尺寸小图动态添加方法
     */
    private void addDim() {
        dimLinearLayout.removeAllViews();
        for (int dim = 0; dim < dimArray.length(); dim++) {

            View dimItem = inflater.inflate(R.layout.layout_new_product_style_item, null);
            ImageView dimItemImg = dimItem.findViewById(R.id.style_item_cover);

            double value = kWidth / 6;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dimItemImg.getLayoutParams();
            params.width = (int) value;
            params.height = (int) value;
            dimItemImg.setLayoutParams(params);

            ImageUtils.loadImage(glide, JsonUtils.getString(
                    JsonUtils.getJsonItem(dimArray, dim, null), "cover", ""), dimItemImg, R.mipmap.loadpicture);
            boolean check = JsonUtils.getBoolean(JsonUtils.getJsonItem(dimArray, dim, null), "checked", false);
            if (check) {
                dimItemImg.setBackground(getDrawable(R.drawable.view_new_product_select));
            } else {
                dimItemImg.setBackground(getDrawable(R.drawable.view_border));
            }
            final int position = dim;
            dimItemImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String styleCover = JsonUtils.getString(JsonUtils.getJsonItem(dimArray, position, null), "image", "");
                    ImageUtils.loadImage(glide,styleCover, styleBigView, -1);
                    txtStyleName.setText(JsonUtils.getString(JsonUtils.getJsonItem(dimArray, position, null), "name", ""));

                    try {
                        for (int i = 0; i < dimArray.length(); i++) {
                            dimArray.getJSONObject(i).put("checked", false);
                        }
                        dimArray.getJSONObject(position).put("checked", true);
                        addDim();
                    } catch (Exception ex) {

                    }
                }
            });

            dimLinearLayout.addView(dimItem);
        }
    }

    /**
     * 功能：去掉文本开始和结束换行符
     *
     * @param value
     * @return
     */
    private String removeFirstAndLastLineMark(String value) {
        if (value.startsWith("\n")) {
            value = value.substring(1, value.length());
        }
        if (value.endsWith("\n")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    /**
     * 设置页面固定控件数据
     */
    private void addPageValue() {
        productNameView.setText(JsonUtils.getString(productArray, "title", ""));
        boldMethod(productNameView);
        productDescView.setText(JsonUtils.getString(productArray, "description", "无"));
        if (colorArray != null && colorArray.length() != 0) {
            float productPrice = (float) JsonUtils.getDouble(JsonUtils.getJsonItem(colorArray, 0, null), "price", 0);
            productPriceView.setText(PriceUtils.beautify("¥" + productPrice));
            boldMethod(productPriceView);
        }

        String tagStr = JsonUtils.getString(productArray, "serviceTags", "");
        List<String> tags = Arrays.asList(tagStr.split(","));
        if (tags.size() > 1) {
            // set adapter]
            tagLayout.setVisibility(View.VISIBLE);
            tagLayout.setAdapter(new TagAdapter<String>(tags) {
                @Override
                public View getView(FlowLayout parent, int position, String tag) {
                    View view = LayoutInflater.from(NewProductInformationActivity.this).inflate(R.layout.new_product_tag, parent, false);
                    TextView tv = view.findViewById(R.id.tag_txt);
                    tag = getItem(position);
                    tv.setText(tag);
                    return view;
                }
            });
        } else {
            tagLayout.setVisibility(View.GONE);
        }

    }

    /**
     * 设置场景搭配模块
     */
    private void initSpaceGoods() {
        if (sceneObject == null) {
            sceneView.setVisibility(View.GONE);
        } else {
            sceneArray = JsonUtils.getJsonArray(sceneObject, "hotGoods", null);
            String sceneImage = JsonUtils.getString(sceneObject, "cover", "");
            int width;
            int height;
            width = kWidth - WindowUtils.dpToPixels(this, 35);
            height = (int) (width * (200f / 335f));
            LinearLayout.LayoutParams paramLink = new LinearLayout.LayoutParams(width, height);
            linkImage.setLayoutParams(paramLink);
            linkImage.setAdjustViewBounds(true);
            linkImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String spaceId = JsonUtils.getString(sceneObject, "spaceId", "");
                    Intent intent = new Intent(NewProductInformationActivity.this, NewSpaceDetailActivity.class);
                    intent.putExtra("spaceId", spaceId);
                    startActivity(intent);
                }
            });

            sceneView.setVisibility(View.VISIBLE);
            ImageUtils.loadImage(glide, sceneImage, linkImage, R.mipmap.loadpicture);

            spaceGoodsAdapter = new SpaceGoodsAdapter();
            linkProductView.setAdapter(spaceGoodsAdapter);

        }
    }

    class ParamAdapter extends BaseAdapter {
        class ParamViewHolder {
            TextView paramKey;
            TextView paramValue;

            ParamViewHolder(View convertView) {
                paramKey = convertView.findViewById(R.id.param_key);
                paramValue = convertView.findViewById(R.id.param_value);
            }
        }

        @Override
        public int getCount() {
            return paraArray == null ? 0 : paraArray.length();
        }

        @Override
        public JSONObject getItem(int position) {
            return JsonUtils.getJsonItem(paraArray, position, null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.layout_new_product_param_item, null);
                convertView.setTag(new ParamViewHolder(convertView));
            }
            ParamViewHolder viewHolder = (ParamViewHolder) convertView.getTag();
            JSONObject item = getItem(position);
            boldMethod(viewHolder.paramKey);
            viewHolder.paramKey.setText(JsonUtils.getString(item, "name", ""));
            viewHolder.paramValue.setText(JsonUtils.getString(item, "value", ""));
            return convertView;
        }
    }

    //场景搭配内含商品适配器
    class SpaceGoodsAdapter extends BaseAdapter {
        class SceneViewHolder {
            TextView sceneName;
            TextView sceneIntroduce;
            TextView scenePrice;
            ImageView imageView;
            ZoomView zoomView;
            TextView discountView;
            TextView saleOutView;
            TextView productOldPrice;

            SceneViewHolder(View convertView) {
                sceneName = convertView.findViewById(R.id.link_product_name);
                sceneIntroduce = convertView.findViewById(R.id.link_product_desc);
                scenePrice = convertView.findViewById(R.id.link_product_price);
                imageView = convertView.findViewById(R.id.link_product_cover);
                zoomView = convertView.findViewById(R.id.link_goods_list_zoom_view);
                discountView = convertView.findViewById(R.id.link_discount_view);
                saleOutView = convertView.findViewById(R.id.link_sale_out_view);
                productOldPrice = convertView.findViewById(R.id.link_old_price);
            }
        }

        @Override
        public int getCount() {
            return sceneArray == null ? 0 : sceneArray.length();
        }

        @Override
        public JSONObject getItem(int position) {
            return JsonUtils.getJsonItem(sceneArray, position, null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.layout_new_product_scene_item, null);
                convertView.setTag(new SceneViewHolder(convertView));
            }
            SceneViewHolder viewHolder = (SceneViewHolder) convertView.getTag();
            JSONObject item = getItem(i);

            //判断商品类型（满减 折扣 原价）
            String tagValue = JsonUtils.getString(item, "discountTag", "");
            if (tagValue.equals("")) {
                viewHolder.discountView.setVisibility(View.GONE);
                viewHolder.saleOutView.setVisibility(View.INVISIBLE);
                viewHolder.productOldPrice.setVisibility(View.GONE);

                double price = JsonUtils.getDouble(item, "minPrice", 0);
                double priceValue = NumberUtils.decimalDouble(price);
                viewHolder.scenePrice.setText("¥" + PriceUtils.beautify(priceValue));
            }                //原价 无活动
            else {
                if (JsonUtils.getInt(item, "discountType", 0) == 2) {
                    viewHolder.discountView.setVisibility(View.VISIBLE);
                    viewHolder.saleOutView.setVisibility(View.GONE);
                    viewHolder.discountView.setText(tagValue);

                    viewHolder.productOldPrice.setVisibility(View.VISIBLE);

                    double oldPrice = JsonUtils.getDouble(item, "minPrice", 0);
                    double oldPriceValue = NumberUtils.decimalDouble(oldPrice);
                    viewHolder.productOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //原价设置中划线
                    viewHolder.productOldPrice.setText("¥" + PriceUtils.beautify(oldPriceValue));

                    double newPrice = JsonUtils.getDouble(item, "discountPrice", 0);
                    double newPriceValue = NumberUtils.decimalDouble(newPrice);
                    viewHolder.scenePrice.setText("¥" + PriceUtils.beautify(newPriceValue));
                }             //折扣
                else {
                    viewHolder.discountView.setVisibility(View.GONE);
                    viewHolder.saleOutView.setVisibility(View.VISIBLE);
                    viewHolder.productOldPrice.setVisibility(View.GONE);
                    viewHolder.saleOutView.setText(tagValue);
                    double price = JsonUtils.getDouble(item, "minPrice", 0);
                    double priceValue = NumberUtils.decimalDouble(price);
                    viewHolder.scenePrice.setText("¥" + PriceUtils.beautify(priceValue));
                }             //满减
            }
            boldMethod(viewHolder.sceneName);
            boldMethod(viewHolder.scenePrice);

            final String linkGoodsId = JsonUtils.getString(item, "goodsId", "");

            viewHolder.sceneName.setText(JsonUtils.getString(item, "title", ""));

            viewHolder.sceneIntroduce.setText(JsonUtils.getString(item, "description", ""));

            viewHolder.zoomView.setTag(i);
            ImageUtils.loadImage(glide, JsonUtils.getString(item, "cover", ""), viewHolder.imageView, -1);

            viewHolder.zoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(NewProductInformationActivity.this, NewProductInformationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("goodsId", linkGoodsId);
                    NewProductInformationActivity.this.startActivity(intent);
                }
            });
            return convertView;
        }
    }

    //查询购物车数量
    private void loadShoppingCatCount() {

        if (!AppSessionCache.getInstance().isLogin(NewProductInformationActivity.this)) {
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
                AlertManager.showErrorToast(NewProductInformationActivity.this, "服务器繁忙，请稍后重试", false);
                showError();
            }
        });
    }

    // 点击展示商品信息 选择规格
    class showGoodsClick implements View.OnClickListener {

        private String clickType;

        showGoodsClick(String clickType) {
            this.clickType = clickType;
        }

        @Override
        public void onClick(View view) {
            if (popShowBoolean) {
                return;
            } else {
                popShowBoolean = true;
                showGoods(clickType, NewProductInformationActivity.this);
            }
        }
    }

    //选择规格函数
    private void choiceStyle() {

        if (maxCount <= 0 && !goodsOffBoolean) {
            amount = 0;
            goodsCountNumber.setText("" + amount);
            goodsOff.setVisibility(View.VISIBLE);
            goodsOff.setText("无库存");
            joinShoppingCat.setVisibility(View.GONE);
            buyNow.setVisibility(View.GONE);
            addView.setTextColor(getResources().getColor(R.color.gray_add));
            miuView.setTextColor(getResources().getColor(R.color.gray_add));
        } else if (maxCount > 0 && !goodsOffBoolean) {
            amount = 1;
            miuView.setTextColor(getResources().getColor(R.color.gray_add));
            addView.setTextColor(getResources().getColor(R.color.BLACK));
            goodsCountNumber.setText("" + amount);
            goodsOff.setVisibility(View.GONE);
            if (buyNowShow) {
                buyNow.setVisibility(View.VISIBLE);
            } else {
                joinShoppingCat.setVisibility(View.VISIBLE);
            }
        }
    }

    //关闭展示商品 选择规格的区域
    class closeGoodsClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            showGoodsDetail.dismiss();
            popShowBoolean = false;
        }
    }

    //加入购物车
    private void addShoppingCat() {

        if (!AppSessionCache.getInstance().isLogin(NewProductInformationActivity.this)) {
            Intent intent = new Intent(NewProductInformationActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        }
        loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("加载中");
        loadingDialog.show();

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount + "");
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
                    if (res.equals("success")) {
                        AlertManager.showSuccessToast(NewProductInformationActivity.this, "加入成功", false);
                        loadShoppingCatCount();
                        showGoodsDetail.dismiss();
                        popShowBoolean = false;
                    } else {
                        AlertManager.showErrorToast(NewProductInformationActivity.this, JsonUtils.getString(response, "message", ""), false);
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

    //显示商品规格的popwindow
    private void showGoods(final String clickType, Activity activity) {

        window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.alpha = 0.5f;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setAttributes(params);

        final View contentView = inflater.inflate(R.layout.view_new_product_information_pop, null);

        closeView = contentView.findViewById(R.id.product_information_close);
        joinShoppingCat = contentView.findViewById(R.id.new_join_to_shopping_cat);
        goodsOff = contentView.findViewById(R.id.new_goods_off);
        buyNow = contentView.findViewById(R.id.new_buy_now);
        goodsStyleName = contentView.findViewById(R.id.new_goods_style_name);
        goodsSubName = contentView.findViewById(R.id.new_goods_style_subName);
        goodsStylePrice = contentView.findViewById(R.id.new_goods_style_price);
        goodsStyleImage = contentView.findViewById(R.id.new_goods_style_image);
        goodsCountNumber = contentView.findViewById(R.id.new_goods_number);
        disMissView = contentView.findViewById(R.id.dissmiss_pop_view);
        popOldPrice = contentView.findViewById(R.id.pop_old_price);

        skuPopView = contentView.findViewById(R.id.skuView_pop);
        skuPopView.post(new Runnable() {
            @Override
            public void run() {
                skuPopView.setData(productArray);
                skuPopView.setListener(new SkuView.OnSkuChangeListener() {
                    @Override
                    public void onChange(JSONObject sku) {
                        setBannerAndPrice(sku);
                        setPopWindow(sku);
                        choiceStyle();
                        skuView.setSelectedSku(sku);
                    }
                });
                skuPopView.setSelectedSku(skuView.getSelectedSku());
            }
        });
        disMissView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGoodsDetail.dismiss();
                popShowBoolean = false;
            }
        });
        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGoodsDetail.dismiss();
                popShowBoolean = false;
            }
        });

        TextView shuliangView = contentView.findViewById(R.id.shuliang_view);
        boldMethod(shuliangView);

        //初始化popwindow
        showGoodsDetail = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        showGoodsDetail.setContentView(contentView);
        showGoodsDetail.setAnimationStyle(R.style.mypopwindow_anim_style);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        showGoodsDetail.setWidth(Math.round(screenWidth));

        //关闭商品规格选择
        RelativeLayout cancel_btn = contentView.findViewById(R.id.new_close_goods_detail);
        cancel_btn.setOnClickListener(new closeGoodsClick());

        //阻止事件冒泡
        View popLayout = contentView.findViewById(R.id.new_show_goods_Layout);
        popLayout.setClickable(true);
        View popBodyView = contentView.findViewById(R.id.pop_body_view);

        int height = WindowUtils.getDeviceHeight(this) * 3 / 5;
        FrameLayout.LayoutParams paramPop = (FrameLayout.LayoutParams) popBodyView.getLayoutParams();
        paramPop.height = height;

        addView = contentView.findViewById(R.id.new_add_goods);
        miuView = contentView.findViewById(R.id.new_minus_goods);

        if (amount == 1) {
            miuView.setTextColor(getResources().getColor(R.color.gray_add));
        } else {
            miuView.setTextColor(getResources().getColor(R.color.BLACK));
        }
        //减少商品数量
        miuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (maxCount == 0) {
                    AlertManager.showErrorToast(NewProductInformationActivity.this, "没有库存啦", false);
                } else {
                    if (amount >= 2) {
                        amount--;
                        goodsCountNumber.setText(amount + "");
                    }
                    if (amount == maxCount) {
                        addView.setTextColor(getResources().getColor(R.color.gray_add));
                    } else {
                        addView.setTextColor(getResources().getColor(R.color.BLACK));
                    }
                    if (amount == 1) {
                        miuView.setTextColor(getResources().getColor(R.color.gray_add));
                    } else {
                        miuView.setTextColor(getResources().getColor(R.color.BLACK));
                    }
                }
            }

        });

        //增加商品数量
        addView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (maxCount == 0) {
                    AlertManager.showErrorToast(NewProductInformationActivity.this, "没有库存啦", false);
                } else {
                    if (amount < maxCount) {
                        amount++;
                        goodsCountNumber.setText(amount + "");
                    }
                    if (amount == maxCount) {
                        addView.setTextColor(getResources().getColor(R.color.gray_add));
                    } else {
                        addView.setTextColor(getResources().getColor(R.color.BLACK));
                    }
                    if (amount == 1) {
                        miuView.setTextColor(getResources().getColor(R.color.gray_add));
                    } else {
                        miuView.setTextColor(getResources().getColor(R.color.BLACK));
                    }
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

        showGoodsDetail.showAtLocation(findViewById(R.id.new_show_goods_detail), Gravity.BOTTOM, 0, 0);
        showGoodsDetail.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = window.getAttributes();
                params.alpha = 1.0f;
                popShowBoolean = false;
                window.setAttributes(params);
            }
        });

        //判断是否下架 点击立即购买按钮或者加入购物车
        if (goodsOffBoolean) {
            goodsOff.setVisibility(View.VISIBLE);
            goodsOff.setText("已下架");
            joinShoppingCat.setVisibility(View.GONE);
            buyNow.setVisibility(View.GONE);
        } else if (clickType.equals("0")) {
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
        choiceStyle();
    }

    //立即购买
    private void buyNow() {
        if (!AppSessionCache.getInstance().isLogin(NewProductInformationActivity.this)) {
            Intent intent = new Intent(NewProductInformationActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        }
        loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("加载中");
        loadingDialog.show();

        Map<String, Object> item = new HashMap<>();
        item.put("amount", amount);
        item.put("goodsId", currentGoodsId);
        item.put("styleId", styleId);
        item.put("outright", true);

        Map<String, Object> params = new HashMap<>();
        params.put("items", new Map[]{item});

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
                    popShowBoolean = false;
                    Intent intent = new Intent();
                    intent.setClass(NewProductInformationActivity.this, ConfirmOrderActivity.class);
                    intent.putExtra("orderId", orderId);
                    NewProductInformationActivity.this.startActivity(intent);

                } else {
                    AlertManager.showErrorToast(NewProductInformationActivity.this, JsonUtils.getString(response, "message", ""), false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
            }
        });
    }

    //收藏商品
    private void collectGoods() {
        if (!AppSessionCache.getInstance().isLogin(NewProductInformationActivity.this)) {
            Intent intent = new Intent(NewProductInformationActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("goodsId", "" + currentGoodsId);

        loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("收藏中");
        loadingDialog.show();

        HttpRequest.post(AppConfig.COLLECT_GOODS, null, params, JSONObject.class, new RequestCallback<JSONObject>() {

            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    AlertManager.showSuccessToast(NewProductInformationActivity.this, "收藏成功", false);
                    collectBtn.setVisibility(View.GONE);
                    haveCollectBtn.setVisibility(View.VISIBLE);
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

    private int dip2px(float dpValue) {
        return WindowUtils.dpToPixels(this, (int) dpValue);
    }

    private void boldMethod(TextView textView) {
        TextPaint paint = textView.getPaint();
        paint.setFakeBoldText(true);
    }

    /**
     * 设置banner price
     */
    private void setBannerAndPrice(JSONObject sku) {
        String whichId = JsonUtils.getString(sku, "styleId", "");
        for (int i = 0; i < styleIds.size(); i++) {
            if (whichId.equals(styleIds.get(i))) {
                if (!opposite) {
                    convenientBanner.setcurrentitem(i);
                    break;
                } //顺序
                else {
                    convenientBanner.setcurrentitem(i);
                } //倒序
            }
        }

        double price = JsonUtils.getDouble(sku, "price", 0);
        double priceValue = NumberUtils.decimalDouble(price);
        double newPrice = JsonUtils.getDouble(sku, "discountPrice", 0);
        double newPriceValue = NumberUtils.decimalDouble(newPrice);

        if (tagValue.equals("")) {
            productPriceView.setText("¥" + PriceUtils.beautify(priceValue));
        }                //原价 无活动
        else {
            if (typeValue == 2) {
                productOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //原价设置中划线
                productOldPrice.setText("¥" + PriceUtils.beautify(priceValue));
                productPriceView.setText("¥" + PriceUtils.beautify(newPriceValue));
            }             //折扣
            else {
                productPriceView.setText("¥" + PriceUtils.beautify(priceValue));
            }             //满减
        }
    }

    /**
     * 设置弹窗中cover 选字 价格
     */
    private void setPopWindow(JSONObject sku) {
        String styleName = JsonUtils.getString(sku, "name", "");
        String styleSubName = JsonUtils.getString(sku, "subName", "");

        double price = JsonUtils.getDouble(sku, "price", 0);
        double priceValue = NumberUtils.decimalDouble(price);
        double newPrice = JsonUtils.getDouble(sku, "discountPrice", 0);
        double newPriceValue = NumberUtils.decimalDouble(newPrice);
        if (tagValue.equals("")) {
            goodsStylePrice.setText("¥" + PriceUtils.beautify(priceValue));
        }                //原价 无活动
        else {
            if (typeValue == 2) {
                popOldPrice.setVisibility(View.VISIBLE);
                popOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //原价设置中划线
                popOldPrice.setText("¥" + PriceUtils.beautify(priceValue));
                goodsStylePrice.setText("¥" + PriceUtils.beautify(newPriceValue));
            }             //折扣
            else {
                goodsStylePrice.setText("¥" + PriceUtils.beautify(priceValue));
            }             //满减
        }

        String goodsCover = JsonUtils.getString(sku, "cover", "");
        styleId = JsonUtils.getString(sku, "styleId", "");
        goodsStyleName.setText(styleName);
        goodsSubName.setText(styleSubName);

        WindowUtils.boldMethod(goodsStylePrice);
        ImageUtils.loadImage(glide, goodsCover, goodsStyleImage, -1);
        maxCount = JsonUtils.getInt(sku, "count", 0);
        if (noStorageSale) {
            maxCount = 99;
        }
    }

    /**
     * 删除收藏
     */
    private void deleteCollection() {

        loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("取消中");
        loadingDialog.show();
        HttpRequest.delete(AppConfig.DELETE_COLLECTION_GOODS + currentGoodsId, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    AlertManager.showSuccessToast(NewProductInformationActivity.this, "取消成功", false);
                    haveCollectBtn.setVisibility(View.GONE);
                    collectBtn.setVisibility(View.VISIBLE);
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(NewProductInformationActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(NewProductInformationActivity.this, "取消失败", false);
                AppLog.e("err", ex);
            }
        });
    }

    class CouponAdapter extends BaseAdapter {


        class CouponViewHolder {

            TextView detailTxt;
            TextView descTxt;
            TextView dateTxt;
            TextView typeString;

            CouponViewHolder(View view) {
                detailTxt = view.findViewById(R.id.coupon_detail);
                descTxt = view.findViewById(R.id.coupon_desc);
                dateTxt = view.findViewById(R.id.coupon_date);
                typeString = view.findViewById(R.id.coupon_type_string);
            }

        }

        @Override
        public int getCount() {
            return couponData == null ? 0 : couponData.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(couponData, i, null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                // 获得容器
                convertView = inflater.inflate(R.layout.activity_coupon_item, null);
                convertView.setTag(new CouponViewHolder(convertView));
            }
            CouponViewHolder viewHolder = (CouponViewHolder) convertView.getTag();
            final JSONObject item = getItem(position);

            String couponTypeStr = JsonUtils.getString(item, "typeStr", "");
            viewHolder.typeString.setText(couponTypeStr);
            WindowUtils.boldMethod(viewHolder.typeString);

            viewHolder.descTxt.setText(JsonUtils.getString(item, "description", ""));

            String couponName = JsonUtils.getString(item, "name", "");
            viewHolder.detailTxt.setText(couponName);

            String deadDate = JsonUtils.getString(item, "expireDescription", "");
            viewHolder.dateTxt.setText(deadDate);

            return convertView;
        }
    }

    private void getUnReadCount() {
        JSONObject result = ZhichiUtils.getUserUnReadCount(this);
        if (JsonUtils.getBoolean(result, "haveLog", false)) {
            int count = JsonUtils.getInt(result, "count", 0);
            unreadCount.setText(count + "");
            if (count == 0) {
                unreadCount.setVisibility(View.GONE);
            } else {
                unreadCount.setVisibility(View.VISIBLE);
            }
        } else {
            unreadCount.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        getUnReadCount();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        try {
            if (zhichiReceiver != null) {
                unregisterReceiver(zhichiReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
