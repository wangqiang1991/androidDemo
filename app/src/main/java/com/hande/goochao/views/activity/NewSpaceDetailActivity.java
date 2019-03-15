package com.hande.goochao.views.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hande.goochao.MainActivity;
import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.config.AppConst;
import com.hande.goochao.session.AppSession;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.NumberUtils;
import com.hande.goochao.utils.PriceUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoScrollGridView;
import com.hande.goochao.views.components.NoScrollListView;
import com.hande.goochao.views.components.WebViewActivity;
import com.hande.goochao.views.components.ZoomView;
import com.hande.goochao.views.widget.OnScrollChangeView;
import com.hande.goochao.views.widget.SharePopupWindow;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;


/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/7/16.
 */
public class NewSpaceDetailActivity extends ToolBarActivity implements ScenePreviewActivity.OnPageChangeListener {

    @ViewInject(R.id.loading_space_page)
    private ImageView loadingView;
    @ViewInject(R.id.new_space_detail_load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.space_detail_image_grid_view)
    private NoScrollGridView spaceDetailView;
    @ViewInject(R.id.new_space_detail_goods)
    private NoScrollListView spaceGoodsView;

    @ViewInject(R.id.space_detail_name)
    private TextView spaceNameView;
    @ViewInject(R.id.space_detail_desc)
    private TextView spaceDescView;
    @ViewInject(R.id.space_detail_cover)
    private ImageView spaceCoverView;
    @ViewInject(R.id.no_use_title_space)
    private TextView changJing;
    @ViewInject(R.id.txt_no_use_space)
    private TextView chaKan;
    @ViewInject(R.id.icon_back)
    private ImageView backIcon;
    @ViewInject(R.id.share_icon)
    private ImageView shareIcon;
    @ViewInject(R.id.layTopBar)
    private RelativeLayout layTopBar;
    @ViewInject(R.id.to_space_h5_view)
    private View toH5View;
    @ViewInject(R.id.scroll_view_space)
    private OnScrollChangeView scrollView;

    private boolean isLoading = false;
    private int kWidth;

    private String spaceId;
    private String articleId;
    private String spaceName;
    private String spaceDesc;
    private String spaceCover;
    private JSONArray sceneListArray = new JSONArray();
    private JSONArray goodsDetailArray = new JSONArray();

    private SpaceDetailAdapter spaceDetailAdapter;
    private SpaceGoodsAdapter spaceGoodsAdapter;

    private LayoutInflater inflater;
    private boolean chenLie;
    private Handler handler;

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_space_detail_head);
        glide = GlideApp.with(this);
        inflater = LayoutInflater.from(this);
        hiddenToolBar();
        initTopBar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        kWidth = WindowUtils.getDeviceWidth(this);
        spaceId = getIntent().getStringExtra("spaceId");

        spaceDetailAdapter = new SpaceDetailAdapter();
        spaceDetailView.setAdapter(spaceDetailAdapter);
        spaceGoodsAdapter = new SpaceGoodsAdapter();
        spaceGoodsView.setAdapter(spaceGoodsAdapter);

        initListener();

        shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = RestfulUrl.build(AppConfig.SPACE_SHARE_URL, ":spaceId", spaceId);
                new SharePopupWindow(NewSpaceDetailActivity.this).show(url, spaceCover, spaceName, spaceDesc);
            }
        });

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        toH5View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = "场景介绍";
                String url = AppConfig.ARTICLE_CONTENT + articleId;
                Intent intent = new Intent(NewSpaceDetailActivity.this, WebViewActivity.class);
                intent.putExtra("title", name);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });

        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                chenLie = false;
                loadDetail(true);
            }
        });

        loadDetail(true);

        chenLie = getIntent().getBooleanExtra("chenLie", false);
        spaceCoverView.setFocusableInTouchMode(true);
        spaceCoverView.setFocusable(true);
        spaceCoverView.requestFocus();
        if (chenLie) {
            loadingView.setVisibility(View.VISIBLE);
            handler = new Handler();
            handler.postDelayed(runnable, 3000);
        }
    }

    /**
     * 功能：初始化顶部按钮间距
     */
    private void initTopBar() {
        int result;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        } else {
            result = WindowUtils.dpToPixels(this, 24);
        }

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) layTopBar.getLayoutParams();
        lp.topMargin = result;

    }

    private void loadDetail(boolean showLoading) {
        if (showLoading) {
            loadingView.setVisibility(View.VISIBLE);
        }
        String url = RestfulUrl.build(AppConfig.SPACE_DETAIL, ":spaceId", spaceId);
        isLoading = true;
        HttpRequest.get(url, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                if (!chenLie){
                    loadingView.setVisibility(View.GONE);
                }
                isLoading = true;
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONArray data = JsonUtils.getJsonArray(response, "data.items", null);
                    articleId = JsonUtils.getString(response, "data.detailId", "");
                    spaceName = JsonUtils.getString(response, "data.name", "");
                    spaceDesc = JsonUtils.getString(response, "data.description", "");
                    spaceCover = JsonUtils.getString(response, "data.cover", "");
                    JSONArray picItems = JsonUtils.getJsonArray(JsonUtils.getJsonItem(data, 0, null), "picItems", null);
                    for (int i = 0; i < picItems.length(); i++) {
                        sceneListArray.put(JsonUtils.getJsonItem(picItems, i, null));
                    }
                    for (int i = 0; i < sceneListArray.length(); i++) {
                        JSONArray goods = JsonUtils.getJsonArray(JsonUtils.getJsonItem(sceneListArray, i, null),
                                "detail.goodsList", null);
                        addToGoodsArray(goods);
                    }
                    initPage();
                    spaceDetailAdapter.notifyDataSetChanged();
                    spaceGoodsAdapter.notifyDataSetChanged();
                    notifyPreview();
                    resetView();
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
                AlertManager.showErrorToast(NewSpaceDetailActivity.this, "服务器繁忙，请稍后重试", false);
            }
        });

    }

    private void addToGoodsArray(JSONArray goods) {
        for (int i = 0; i < goods.length(); i++) {
            JSONObject goodsObject = JsonUtils.getJsonItem(goods, i, null);
            if (JsonUtils.getInt(goodsObject, "type", 0) == 2) {
                continue;
            }
            String goodsId = JsonUtils.getString(goodsObject, "goodsId", null);
            if (!goodsExists(goodsId)) {
                goodsDetailArray.put(goodsObject);
            }
        }
    }

    /**
     * 功能：判断商品是否存在
     * @return
     */
    private boolean goodsExists(String goodsId) {
        for (int i = 0; i < goodsDetailArray.length(); i++) {
            String objectId = JsonUtils.getString(JsonUtils.getJsonItem(goodsDetailArray, i, null), "goodsId", "");
            if (objectId.equals(goodsId)) {
                return true;
            }
        }
        return false;
    }

    private void initPage() {

        spaceNameView.setText(spaceName);
        spaceDescView.setText(spaceDesc);
        ImageUtils.loadImage(glide, spaceCover, spaceCoverView, R.mipmap.loadpicture);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) spaceCoverView.getLayoutParams();
        params.width = kWidth;
        params.height = (int) (kWidth * (224f / 375f));

        changJing.setText("场景组合单品");
        chaKan.setText("查看完整介绍");
        WindowUtils.boldMethod(changJing);
        WindowUtils.boldMethod(chaKan);
        WindowUtils.boldMethod(spaceNameView);
    }

    class SpaceDetailAdapter extends BaseAdapter implements View.OnClickListener {


        class SpaceDetailViewHolder {

            ImageView image;
            TextView txtName;
            TextView tagNumber;
            View signView;
            ZoomView zoomView;

            SpaceDetailViewHolder(View view) {
                image = view.findViewById(R.id.recommend_scene_cover);
                txtName = view.findViewById(R.id.recommend_scene_title);
                tagNumber = view.findViewById(R.id.scene_tag_number);
                signView = view.findViewById(R.id.sign_view);
                zoomView = view.findViewById(R.id.space_detail_zoom_view);
            }

        }


        @Override
        public int getCount() {
            return sceneListArray == null ? 0 : sceneListArray.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(sceneListArray, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                // 获得容器
                convertView = inflater.inflate(R.layout.new_space_cover_item, null);
                convertView.setTag(new SpaceDetailViewHolder(convertView));
            }
            SpaceDetailViewHolder viewHolder = (SpaceDetailViewHolder) convertView.getTag();
            viewHolder.zoomView.setTag(i);
            viewHolder.zoomView.setOnClickListener(this);

            int imgWidth =(kWidth - WindowUtils.dpToPixels(NewSpaceDetailActivity.this, 45)) / 2;
            int imgHeight = (kWidth - WindowUtils.dpToPixels(NewSpaceDetailActivity.this, 45)) / 2;

            // 给组件设置资源
            final JSONObject item = getItem(i);
            ImageUtils.loadImage(glide, ImageUtils.zoomResize(JsonUtils.getString(item, "detail.cover", ""), 500, 500), viewHolder.image, R.mipmap.loadpicture);

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) viewHolder.image.getLayoutParams();
            params.width = imgWidth;
            params.height = imgHeight;

            int tagNum = JsonUtils.getInt(item, "goodsCount", 0);
            viewHolder.tagNumber.setText("" + tagNum);
            String name = JsonUtils.getString(item, "detail.name", "");
            viewHolder.txtName.setText(name);

            int img1Width = (kWidth - WindowUtils.dpToPixels(NewSpaceDetailActivity.this, 45)) / 2;
            FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) viewHolder.signView.getLayoutParams();
            param.width = img1Width;

            return convertView;
        }

        @Override
        public void onClick(View v) {
            ZoomView zoomView = ((ZoomView) v);
            ScenePreviewActivity.setOnPageChangeListener(NewSpaceDetailActivity.this);
            AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, sceneListArray);
            Intent intent = new Intent(NewSpaceDetailActivity.this, ScenePreviewActivity.class);
            intent.putExtra("currentIndex", ((Integer) zoomView.getTag()));
            startActivity(intent);
            NewSpaceDetailActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    class SpaceGoodsAdapter extends BaseAdapter {


        class SpaceGoodsViewHolder {

            ImageView goodImage;
            TextView goodName;
            TextView goodDesc;
            TextView goodPrice;
            ZoomView zoomView;
            TextView discountView;
            TextView saleOutView;
            TextView productOldPrice;

            SpaceGoodsViewHolder(View view) {
                goodImage = view.findViewById(R.id.link_product_cover);
                goodName = view.findViewById(R.id.link_product_name);
                goodDesc = view.findViewById(R.id.link_product_desc);
                goodPrice = view.findViewById(R.id.link_product_price);
                zoomView = view.findViewById(R.id.link_goods_list_zoom_view);
                discountView = view.findViewById(R.id.space_discount_view);
                saleOutView = view.findViewById(R.id.space_sale_out_view);
                productOldPrice = view.findViewById(R.id.space_old_price);
            }

        }


        @Override
        public int getCount() {
            return goodsDetailArray == null ? 0 : goodsDetailArray.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(goodsDetailArray, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {

            if (convertView == null) {
                // 获得容器
                convertView = inflater.inflate(R.layout.new_space_detail_goods_item, null);
                convertView.setTag(new SpaceGoodsViewHolder(convertView));
            }

            SpaceGoodsViewHolder viewHolder = (SpaceGoodsViewHolder) convertView.getTag();

            int imgWidth = (int) (kWidth * (100f / 375f));

            // 给组件设置资源
            final JSONObject item = getItem(i);
            ImageUtils.loadImage(glide, ImageUtils.zoomResize(JsonUtils.getString(item, "goods.cover", ""), 400, 400), viewHolder.goodImage, -1);


            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.goodImage.getLayoutParams();
            params.width = imgWidth;
            params.height = imgWidth;

            String goodName = JsonUtils.getString(item, "goods.title", "");
            viewHolder.goodName.setText(goodName);

            String goodDesc = JsonUtils.getString(item, "goods.description", "");
            viewHolder.goodDesc.setText(goodDesc);

            //判断商品类型（满减 折扣 原价）
            String tagValue = JsonUtils.getString(getItem(i),"goods.discountTag","");
            if (tagValue.equals("")){
                viewHolder.discountView.setVisibility(View.GONE);
                viewHolder.saleOutView.setVisibility(View.INVISIBLE);
                viewHolder.productOldPrice.setVisibility(View.GONE);

                double price = JsonUtils.getDouble(item , "goods.minPrice" , 0);
                double priceValue = NumberUtils.decimalDouble(price);
                viewHolder.goodPrice.setText("¥" + PriceUtils.beautify(priceValue));
            }                //原价 无活动
            else{
                if (JsonUtils.getInt(getItem(i),"goods.discountType",0) == 2){
                    viewHolder.discountView.setVisibility(View.VISIBLE);
                    viewHolder.saleOutView.setVisibility(View.GONE);
                    viewHolder.discountView.setText(tagValue);

                    viewHolder.productOldPrice.setVisibility(View.VISIBLE);

                    double oldPrice = JsonUtils.getDouble(item , "goods.minPrice" , 0);
                    double oldPriceValue = NumberUtils.decimalDouble(oldPrice);
                    viewHolder.productOldPrice.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG); //原价设置中划线
                    viewHolder.productOldPrice.setText("¥" + PriceUtils.beautify(oldPriceValue));

                    double newPrice = JsonUtils.getDouble(item , "goods.discountPrice" , 0);
                    double newPriceValue = NumberUtils.decimalDouble(newPrice);
                    viewHolder.goodPrice.setText("¥" + PriceUtils.beautify(newPriceValue));
                }             //折扣
                else {
                    viewHolder.discountView.setVisibility(View.GONE);
                    viewHolder.saleOutView.setVisibility(View.VISIBLE);
                    viewHolder.productOldPrice.setVisibility(View.GONE);
                    viewHolder.saleOutView.setText(tagValue);
                    double price = JsonUtils.getDouble(item , "goods.minPrice" , 0);
                    double priceValue = NumberUtils.decimalDouble(price);
                    viewHolder.goodPrice.setText("¥" + PriceUtils.beautify(priceValue));
                }             //满减
            }

            WindowUtils.boldMethod(viewHolder.goodName);
            WindowUtils.boldMethod(viewHolder.goodPrice);
            WindowUtils.boldMethod(viewHolder.goodDesc);

            viewHolder.zoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String goodsId = JsonUtils.getString(JsonUtils.getJsonItem(goodsDetailArray, i, null), "goodsId", "");
                    Intent intent = new Intent(NewSpaceDetailActivity.this, NewProductInformationActivity.class);
                    intent.putExtra("goodsId", goodsId);
                    startActivity(intent);
                }
            });
            return convertView;
        }
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void showError() {
        loadFailView.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);
    }

    @Override
    public void onPageChange(int currentIndex, int size) {
        AppLog.i("currentIndex:" + currentIndex + ", size:" + size);
        spaceDetailView.smoothScrollToPositionFromTop(currentIndex, 0);
        int currentPage = currentIndex + 1;
        if (!isLoading && size - currentPage <= 5) {
            AppLog.i("loading next data");
            loadDetail(false);
        }
    }

    private void notifyPreview() {
        AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, sceneListArray);
        ScenePreviewActivity.refreshData();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int y = spaceDetailView.getBottom() - WindowUtils.dpToPixels(NewSpaceDetailActivity.this,50);
            scrollView.scrollTo(0, y);// 改变滚动条的位置
            loadingView.setVisibility(View.GONE);
        }
    };

    private void initListener() {

        scrollView.setScrollViewListener(new OnScrollChangeView.ScrollViewListener() {
            @Override
            public void onScrollChanged(OnScrollChangeView scrollView, int x, int y, int oldx, int oldy) {


                //获取下滑总高度height
                int height = spaceCoverView.getBottom() - WindowUtils.dpToPixels(NewSpaceDetailActivity.this,70);
                if (y >= height){
                    if (Build.VERSION.SDK_INT >Build.VERSION_CODES.M) {
                        Window window = NewSpaceDetailActivity.this.getWindow();
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(Color.WHITE);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = NewSpaceDetailActivity.this.getWindow();
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(Color.BLACK);
                    }
                    layTopBar.setBackgroundColor(getResources().getColor(R.color.WHITE));
                    backIcon.setBackground(getResources().getDrawable(R.drawable.back_icon_press));
                    shareIcon.setBackground(getResources().getDrawable(R.drawable.new_share_style));
                }else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = NewSpaceDetailActivity.this.getWindow();
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(Color.TRANSPARENT);
                    }
                    shareIcon.setBackground(getResources().getDrawable(R.drawable.back_icon_white_press));
                    backIcon.setBackground(getResources().getDrawable(R.drawable.new_space_back));
                    layTopBar.setBackgroundColor(getResources().getColor(R.color.transparent));
                }
            }
        });
    }

}
