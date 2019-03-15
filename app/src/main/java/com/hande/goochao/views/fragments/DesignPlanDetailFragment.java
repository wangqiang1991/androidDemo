package com.hande.goochao.views.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.job.JobInfo;
import android.content.Intent;
import android.graphics.Paint;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.commons.views.gallery.GalleryActivity;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.NumberUtils;
import com.hande.goochao.utils.PriceUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.DesignPlanDetailActivity;
import com.hande.goochao.views.activity.LoginActivity;
import com.hande.goochao.views.activity.NewProductInformationActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoScrollGridView;
import com.hande.goochao.views.components.ZoomView;
import com.hande.goochao.views.widget.SharePopupWindow;
import com.hande.goochao.views.widget.SkuView;

import org.apache.log4j.chainsaw.Main;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hande.goochao.utils.ImageUtils.resize;
import static com.hande.goochao.utils.ImageUtils.zoomResize;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/9/3.
 */
@ContentView(R.layout.fragment_design_plan_detail)
public class DesignPlanDetailFragment extends BaseFragment implements View.OnClickListener {

    @ViewInject(R.id.ping_mian_bu_zhi)
    private TextView pingMianBuZhiView;
    @ViewInject(R.id.chang_jing_shi_yi)
    private TextView changJingShiYiView;
    @ViewInject(R.id.se_cai_guan_xi)
    private TextView seCaiGuanXiView;
    @ViewInject(R.id.ruan_zhuang_da_pei)
    private TextView ruanZhuangDaPeiView;
    @ViewInject(R.id.xiang_guan_dan_pin)
    private TextView xiangGuanDanPin;

    @ViewInject(R.id.thinking_collect)
    private ImageView collectIcon;
    @ViewInject(R.id.had_collect)
    private ImageView hadCollectIcon;
    @ViewInject(R.id.thinking_share)
    private ImageView shareIcon;

    @ViewInject(R.id.detail_planImage)
    private ImageView planImageView;
    @ViewInject(R.id.detail_effectImage)
    private ImageView effectImageView;
    @ViewInject(R.id.detail_colorRelationImage)
    private ImageView colorRelationImageView;
    @ViewInject(R.id.detail_matchImage)
    private ImageView matchImageView;
    @ViewInject(R.id.put_in_cart)
    private View putInCart;
    @ViewInject(R.id.put_cart_txt)
    private TextView putInCartText;
    @ViewInject(R.id.goods_line)
    private View goodsLineView;

    @ViewInject(R.id.similar_product_grid_view)
    private NoScrollGridView similarGridView;
    private SimilarAdapter similarAdapter;

    private JSONObject data;
    private JSONObject firstObject;
    private JSONArray similarityData;
    private boolean noData = false;

    private String detailId;
    private String planName;
    private String cover;
    private String planId;
    private String designThinkingData;

    private ImageOptions options = ImageOptionsUtil.getImageOptionsCenter(R.mipmap.loadpicture);

    private CustomLoadingDialog loadingDialog;
    private boolean loaded = false;
    private GlideRequests glide;
    private int kWidth;
    private LayoutInflater inflater;

    private Window window;

    //显示大图预览属性
    private List<String> newImages = new ArrayList<>();

    //弹窗控件.属性
    private TextView allCount;
    private TextView allMoney;
    private TextView preferentialView;
    private TextView addToCart;
    private LinearLayout addGoodsView;

    private JSONArray shoppingCartVos = new JSONArray();

    private PopupWindow designPopWindow;

    private String styleId;
    private JSONArray skuArray = new JSONArray();

    //判断弹窗是否弹出
    private boolean popShowBoolean = false;
    //判断是否有无库存
    private boolean saleOut = false;

    private int maxCount;
    private int amount;
    private double allPrice;
    private double allDiscountPrice;

    //滑动属性
    @ViewInject(R.id.plan_detail_scroll)
    private ScrollView scrollView;

    private int mTouchSlop;//系统认为的最小滑动距离

    private boolean hadRegister;

    private GestureDetector gestureDetector;

    @SuppressLint("ValidFragment")
    public DesignPlanDetailFragment(JSONObject data, JSONObject firstObject) {
        this.data = data;
        this.firstObject = firstObject;
    }

    public DesignPlanDetailFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (((DesignPlanDetailActivity) getActivity()).collection) {
            hadCollectIcon.setVisibility(View.VISIBLE);
            hadCollectIcon.setEnabled(true);
            collectIcon.setVisibility(View.GONE);
            collectIcon.setEnabled(false);
        } else {
            hadCollectIcon.setVisibility(View.GONE);
            hadCollectIcon.setEnabled(false);
            collectIcon.setVisibility(View.VISIBLE);
            collectIcon.setEnabled(true);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!loaded) {

            glide = GlideApp.with(getActivity());
            loaded = true;
            kWidth = WindowUtils.getDeviceWidth(getActivity());
            inflater = LayoutInflater.from(getActivity());
            loadingDialog = new CustomLoadingDialog(getActivity());

            similarAdapter = new SimilarAdapter();
            similarGridView.setAdapter(similarAdapter);

            shareIcon.setOnClickListener(this);
            collectIcon.setOnClickListener(this);
            putInCart.setOnClickListener(this);
            hadCollectIcon.setOnClickListener(this);

            planId = JsonUtils.getString(firstObject, "planId", "");

            //获取系统定义的最低滑动距离
            mTouchSlop = ViewConfiguration.get(getActivity()).getScaledTouchSlop();

            gestureDetector = new GestureDetector(getActivity(), new GestureDetector.OnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return false;
                }

                @Override
                public void onShowPress(MotionEvent e) {

                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    AppLog.i("ASD click");
                    View view = findViewByXY(scrollView, (int) e.getRawX(), (int) e.getRawY());
                    if (view instanceof ImageView && view.getTag(R.id.tag_image) != null) {
                        String imageUrl = (String) view.getTag(R.id.tag_image);
                        if (!TextUtils.isEmpty(imageUrl)) {
                            Intent intent = new Intent(getActivity(), GalleryActivity.class);
                            intent.putExtra("isLocal", false);
                            intent.putExtra("currentSrc", imageUrl);
                            intent.putExtra("images", newImages.toArray(new String[newImages.size()]));
                            startActivity(intent);
                            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    }
                    AppLog.i("ASD view = " + view);
                    return false;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    AppLog.i("ASD move distanceX=" + distanceX + " distanceY=" + distanceY);
                    distanceY = e1.getRawY() - e2.getRawY();
                    if (distanceY > mTouchSlop) {
                        EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_inspiration_up);
                        notification.setValue("");
                        EventBus.getDefault().post(notification);
                    }
                    if (distanceY < -mTouchSlop) {
                        View contentView = scrollView.getChildAt(0);
                        if (contentView.getMeasuredHeight() <= scrollView.getScrollY() + scrollView.getHeight()) {
                            return false;
                        }
                        EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_inspiration_down);
                        notification.setValue("");
                        EventBus.getDefault().post(notification);
                    }
                    return false;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    return false;
                }

                private View findViewByXY(View view, int x, int y) {
                    View targetView = null;
                    if (view instanceof ViewGroup) {
                        // 父容器,遍历子控件
                        ViewGroup v = (ViewGroup) view;
                        for (int i = 0; i < v.getChildCount(); i++) {
                            targetView = findViewByXY(v.getChildAt(i), x, y);
                            if (targetView != null) {
                                break;
                            }
                        }
                    } else {
                        targetView = getTouchTarget(view, x, y);
                    }
                    return targetView;

                }

                private View getTouchTarget(View view, int x, int y) {
                    if (!(view instanceof ImageView)) {
                        return null;
                    }
                    if (isTouchPointInView(view, x, y)) {
                        return view;
                    }
                    return null;
                }

                private boolean isTouchPointInView(View view, int x, int y) {
                    int[] location = new int[2];
                    view.getLocationOnScreen(location);
                    int left = location[0];
                    int top = location[1];
                    int right = left + view.getMeasuredWidth();
                    int bottom = top + view.getMeasuredHeight();
                    if (y >= top && y <= bottom && x >= left
                            && x <= right) {
                        return true;
                    }
                    return false;
                }
            });

            scrollView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return false;
                }
            });
            boldText();
            pageInit();

            if (!hadRegister) {
                hadRegister = true;
                EventBus.getDefault().register(this);
            }
        }
    }

    private void pageInit() {

        similarityData = JsonUtils.getJsonArray(data, "planDetailGoodsRelations", null);

        if (similarityData.length() == 0) {
            putInCart.setEnabled(false);
            putInCart.setVisibility(View.GONE);
            xiangGuanDanPin.setVisibility(View.GONE);
            goodsLineView.setVisibility(View.GONE);
        }
        String planImage = JsonUtils.getString(data, "planImage", "");
        int planImageWidth = JsonUtils.getInt(data, "planImageWidth", kWidth);
        int planImageHeight = JsonUtils.getInt(data, "planImageHeight", kWidth);
        ImageUtils.loadImage(glide, resize(planImage, planImageWidth, planImageHeight), planImageView, R.mipmap.loadpicture);
        planImageView.setLayoutParams(new LinearLayout.LayoutParams(kWidth, kWidth * planImageHeight / planImageWidth));
        planImageView.setTag(R.id.tag_image, planImage);

        detailId = JsonUtils.getString(data, "detailId", "");

        String effectImage = JsonUtils.getString(data, "effectImage", "");
        int effectImageWidth = JsonUtils.getInt(data, "effectImageWidth", kWidth);
        int effectImageHeight = JsonUtils.getInt(data, "effectImageHeight", kWidth);
        if (effectImage.equals("")) {
            changJingShiYiView.setVisibility(View.GONE);
            effectImageView.setVisibility(View.GONE);
        } else {
            changJingShiYiView.setVisibility(View.VISIBLE);
            effectImageView.setVisibility(View.VISIBLE);
            ImageUtils.loadImage(glide, resize(effectImage, effectImageWidth, effectImageHeight), effectImageView, R.mipmap.loadpicture);
            effectImageView.setLayoutParams(new LinearLayout.LayoutParams(kWidth, kWidth * effectImageHeight / effectImageWidth));
            effectImageView.setTag(R.id.tag_image, effectImage);
        }

        String colorRelationImage = JsonUtils.getString(data, "colorRelationImage", "");
        int colorRelationImageWidth = JsonUtils.getInt(data, "colorRelationImageWidth", kWidth);
        int colorRelationImageHeight = JsonUtils.getInt(data, "colorRelationImageHeight", kWidth);
        ImageUtils.loadImage(glide, resize(colorRelationImage, colorRelationImageWidth, colorRelationImageHeight), colorRelationImageView, R.mipmap.loadpicture);
        colorRelationImageView.setLayoutParams(new LinearLayout.LayoutParams(kWidth, kWidth * colorRelationImageHeight / colorRelationImageWidth));
        colorRelationImageView.setTag(R.id.tag_image, colorRelationImage);

        String matchImage = JsonUtils.getString(data, "matchImage", "");
        int matchImageWidth = JsonUtils.getInt(data, "matchImageWidth", kWidth);
        int matchImageHeight = JsonUtils.getInt(data, "matchImageHeight", kWidth);
        ImageUtils.loadImage(glide, resize(matchImage, matchImageWidth, matchImageHeight), matchImageView, R.mipmap.loadpicture);
        matchImageView.setLayoutParams(new LinearLayout.LayoutParams(kWidth, kWidth * matchImageHeight / matchImageWidth));
        matchImageView.setTag(R.id.tag_image, matchImage);

        if (effectImage.equals("")) {
            newImages.add(planImage);
            newImages.add(colorRelationImage);
            newImages.add(matchImage);

        } else {
            newImages.add(planImage);
            newImages.add(effectImage);
            newImages.add(colorRelationImage);
            newImages.add(matchImage);
        }
    }

    private void boldText() {
        WindowUtils.boldMethod(pingMianBuZhiView);
        WindowUtils.boldMethod(changJingShiYiView);
        WindowUtils.boldMethod(seCaiGuanXiView);
        WindowUtils.boldMethod(ruanZhuangDaPeiView);
        WindowUtils.boldMethod(xiangGuanDanPin);
    }

    @Override
    public void onClick(View v) {
        if (v == shareIcon) {
            designThinkingData = JsonUtils.getString(firstObject, "designExplain", "");
            planName = JsonUtils.getString(firstObject, "planName", "");
            cover = JsonUtils.getString(firstObject, "cover", "");
            String url = RestfulUrl.build(AppConfig.PLAN_SHARE_URL, ":planId", planId);
            new SharePopupWindow(getActivity()).show(url, cover, planName, designThinkingData);
        } else if (v == collectIcon) {
            collectPlan();
        } else if (v == hadCollectIcon) {
            deleteCollection();
        } else if (v == putInCart) {
            if (noData) {
                return;
            } else {
                if (!popShowBoolean) {
                    popShowBoolean = true;
                    checkArray();
                    showGoods();
                }
            }
        }
    }

    private void deleteCollection() {
        loadingDialog.setLoadingText("取关中");
        loadingDialog.show();
        HttpRequest.delete(AppConfig.DELETE_COLLECTION_PLAN + planId, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    AlertManager.showSuccessToast(getActivity(), "取消关注成功", false);
                    EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_plan_delete);
                    EventBus.getDefault().post(notification);
                    hadCollectIcon.setVisibility(View.GONE);
                    hadCollectIcon.setEnabled(false);
                    collectIcon.setVisibility(View.VISIBLE);
                    collectIcon.setEnabled(true);
                    ((DesignPlanDetailActivity) getActivity()).collection = false;
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(getActivity(), message, false);

                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(getActivity(), "删除失败", false);
                AppLog.e("err", ex);
            }
        });
    }

    private void checkArray() {
        skuArray = new JSONArray();
        for (int i = 0; i < similarityData.length(); i++) {
            try {
                if (JsonUtils.getBoolean(similarityData.getJSONObject(i), "check", false)) {
                    JSONObject object = JsonUtils.getJsonObject(similarityData.getJSONObject(i), "goodsDetail", null);
                    String styleId = JsonUtils.getString(similarityData.getJSONObject(i), "styleId", null);
                    object.put("styleId", styleId);
                    skuArray.put(object);
                }
            } catch (JSONException e) {
                AppLog.e("err", e);
            }
        }
    }

    //推荐相关商品适配器
    class SimilarAdapter extends BaseAdapter {

        class SimilarViewHolder {
            ZoomView zoomView;
            TextView similarName;
            TextView similarPrice;
            ImageView imageView;
            ImageView selectView;
            TextView zhekouView;
            TextView manjianView;
            TextView rainbowTag;
            TextView productOldPrice;

            SimilarViewHolder(View convertView) {
                zoomView = convertView.findViewById(R.id.similar_product_zoom_view);
                similarName = convertView.findViewById(R.id.similar_product_name);
                similarPrice = convertView.findViewById(R.id.similar_product_price);
                imageView = convertView.findViewById(R.id.item_cover);
                selectView = convertView.findViewById(R.id.design_select_view);
                zhekouView = convertView.findViewById(R.id.zhe_kou_coupon);
                manjianView = convertView.findViewById(R.id.man_jian_coupon);
                rainbowTag = convertView.findViewById(R.id.rainbow_tag);
                productOldPrice = convertView.findViewById(R.id.old_price_plan);
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
                convertView = inflater.inflate(R.layout.layout_design_detail_goods_item, null);
                convertView.setTag(new SimilarViewHolder(convertView));
                int heightInt = (int) (kWidth * 0.5 * 245 / 185);
                convertView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, heightInt));
            }
            final SimilarViewHolder viewHolder = (SimilarViewHolder) convertView.getTag();
            WindowUtils.boldMethod(viewHolder.similarName);
            WindowUtils.boldMethod(viewHolder.similarPrice);

            final JSONObject item = getItem(i);
            final String goodsId = JsonUtils.getString(item, "goodsId", "");

            //判断商品类型（满减 折扣 原价）
            String tagValue = JsonUtils.getString(getItem(i), "goodsDetail.discountTag", "");
            if (tagValue.equals("")) {
//                viewHolder.rainbowTag.setVisibility(View.GONE);
                viewHolder.zhekouView.setVisibility(View.GONE);
                viewHolder.manjianView.setVisibility(View.INVISIBLE);
                viewHolder.productOldPrice.setVisibility(View.GONE);

                double price = JsonUtils.getDouble(item, "minPrice", 0);
                double priceValue = NumberUtils.decimalDouble(price);
                viewHolder.similarPrice.setText("¥" + PriceUtils.beautify(priceValue));
            }                //原价 无活动
            else {
                if (JsonUtils.getInt(getItem(i), "goodsDetail.discountType", 0) == 2) {
//                    viewHolder.rainbowTag.setVisibility(View.VISIBLE);
                    viewHolder.zhekouView.setVisibility(View.VISIBLE);
                    viewHolder.manjianView.setVisibility(View.GONE);
                    viewHolder.zhekouView.setText(tagValue);

                    viewHolder.productOldPrice.setVisibility(View.VISIBLE);

                    double oldPrice = JsonUtils.getDouble(item, "minPrice", 0);
                    double oldPriceValue = NumberUtils.decimalDouble(oldPrice);
                    viewHolder.productOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //原价设置中划线
                    viewHolder.productOldPrice.setText("¥" + PriceUtils.beautify(oldPriceValue));

                    double newPrice = JsonUtils.getDouble(item, "goodsDetail.discountPrice", 0);
                    double newPriceValue = NumberUtils.decimalDouble(newPrice);
                    viewHolder.similarPrice.setText("¥" + PriceUtils.beautify(newPriceValue));
                }             //折扣
                else {
//                    viewHolder.rainbowTag.setVisibility(View.VISIBLE);
                    viewHolder.zhekouView.setVisibility(View.GONE);
                    viewHolder.manjianView.setVisibility(View.VISIBLE);
                    viewHolder.productOldPrice.setVisibility(View.GONE);
                    viewHolder.manjianView.setText(tagValue);
                    double price = JsonUtils.getDouble(item, "minPrice", 0);
                    double priceValue = NumberUtils.decimalDouble(price);
                    viewHolder.similarPrice.setText("¥" + PriceUtils.beautify(priceValue));
                }             //满减
            }
            WindowUtils.boldMethod(viewHolder.similarPrice);

            double itemWidth = kWidth * 141 / 375;
            double itemHeight = itemWidth;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.imageView.getLayoutParams();
            params.width = (int) itemWidth;
            params.height = (int) itemHeight;
            viewHolder.imageView.setLayoutParams(params);

            ImageUtils.loadImage(glide, resize(JsonUtils.getString(item, "cover", ""), 500, 500),
                    viewHolder.imageView, -1);
            viewHolder.zoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), NewProductInformationActivity.class);
                    intent.putExtra("goodsId", goodsId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().startActivity(intent);
                }
            });
            viewHolder.similarName.setText(JsonUtils.getString(item, "title", ""));
            boolean check = true;
            JsonUtils.put(item, "check", check);

            viewHolder.selectView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (JsonUtils.getBoolean(item, "check", false)) {
                        skuArray = new JSONArray();
                        viewHolder.selectView.setImageResource(R.mipmap.new_default_select);
                        JsonUtils.put(item, "check", false);
                    } else {
                        skuArray = new JSONArray();
                        viewHolder.selectView.setImageResource(R.mipmap.new_select);
                        JsonUtils.put(item, "check", true);
                    }
                    checkArray();
                    if (skuArray.length() == 0) {
                        noData = true;
                        putInCartText.setText("请选择商品");
                        putInCartText.setTextColor(getResources().getColor(R.color.WHITE));
                        putInCart.setBackgroundColor(getResources().getColor(R.color.No_Data_Color));
                    } else {
                        noData = false;
                        putInCartText.setText("一起加入购物车");
                        putInCartText.setTextColor(getResources().getColor(R.color.WHITE));
                        putInCart.setBackgroundColor(getResources().getColor(R.color.Black_Gray));
                    }
                    WindowUtils.boldMethod(putInCartText);
                }
            });
            return convertView;
        }
    }

    //显示商品规格的popwindow
    private void showGoods() {

        window = getActivity().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.alpha = 0.5f;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setAttributes(params);

        final View contentView = inflater.inflate(R.layout.view_design_plan_pop, null);

        allCount = contentView.findViewById(R.id.amount_count);
        allMoney = contentView.findViewById(R.id.all_money);
        preferentialView = contentView.findViewById(R.id.preferential_view);
        addToCart = contentView.findViewById(R.id.add_to_cart);
        WindowUtils.boldMethod(addToCart);
        addGoodsView = contentView.findViewById(R.id.add_sku_view);

        //初始化popwindow
        designPopWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        designPopWindow.setContentView(contentView);
        designPopWindow.setAnimationStyle(R.style.mypopwindow_anim_style);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        designPopWindow.setWidth(Math.round(screenWidth));

        allCount.setText(skuArray.length() + "件搭配价");

        for (int i = 0; i < skuArray.length(); i++) {

            FrameLayout goodView = (FrameLayout) inflater.inflate(R.layout.design_pop_item, null);

            final TextView goodName = goodView.findViewById(R.id.good_name);
            final TextView goodsStyleName = goodView.findViewById(R.id.new_goods_style_name);
            final TextView goodsSubName = goodView.findViewById(R.id.new_goods_style_subName);
            final TextView goodsStylePrice = goodView.findViewById(R.id.new_goods_style_price);
            final ImageView goodsStyleImage = goodView.findViewById(R.id.new_goods_style_image);
            final TextView goodsCountNumber = goodView.findViewById(R.id.new_goods_number);
            final TextView shuLiangView = goodView.findViewById(R.id.shuliang_view);
            final SkuView skuPopView = goodView.findViewById(R.id.skuView_pop);
            final TextView addView = goodView.findViewById(R.id.new_add_goods);
            final TextView miuView = goodView.findViewById(R.id.new_minus_goods);
            final TextView saleOutTxt = goodView.findViewById(R.id.sale_out_text);
            final ImageView saleOutImg = goodView.findViewById(R.id.sale_out_image);
            final TextView popOldPrice = goodView.findViewById(R.id.pop_plan_old_price);
            final TextView popSaleOutView = goodView.findViewById(R.id.sale_out_plan_item);
            popOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //原价设置中划线

            WindowUtils.boldMethod(shuLiangView);

            final JSONObject item = JsonUtils.getJsonItem(skuArray, i, null);

            final String tagType = JsonUtils.getString(item, "discountType", "");
            String tagValue = JsonUtils.getString(item, "discountTag", "");
            popSaleOutView.setText(tagValue);

            if (tagType.equals("2")) {
                popOldPrice.setVisibility(View.VISIBLE);
                popSaleOutView.setVisibility(View.GONE);
            } else {
                popOldPrice.setVisibility(View.GONE);
                popSaleOutView.setVisibility(View.VISIBLE);
            }

            JSONArray styles = JsonUtils.getJsonArray(item, "styles", new JSONArray());

            String goodNameStr = JsonUtils.getString(item, "title", null);
            goodName.setText(goodNameStr);

            //判断无库存是否可售卖
            final boolean noStorageSale = JsonUtils.getBoolean(item, "noStorageSale", false);

            //skuView数据绑定
            skuPopView.setData(item);

            skuPopView.setListener(new SkuView.OnSkuChangeListener() {
                @Override
                public void onChange(JSONObject sku) {
                    int itemAmount = 0;
                    String styleName = JsonUtils.getString(sku, "name", "");
                    String styleSubName = JsonUtils.getString(sku, "subName", "");

                    double price = JsonUtils.getDouble(sku, "price", 0);
                    double priceValue = NumberUtils.decimalDouble(price);
                    double newPrice = JsonUtils.getDouble(sku, "discountPrice", 0);
                    double newPriceValue = NumberUtils.decimalDouble(newPrice);

                    goodsStylePrice.setText(PriceUtils.beautify("¥" + newPriceValue));
                    popOldPrice.setText(PriceUtils.beautify("¥" + priceValue));

                    String goodsCover = JsonUtils.getString(sku, "cover", "");
                    styleId = JsonUtils.getString(sku, "styleId", "");
                    goodsStyleName.setText(styleName);
                    goodsSubName.setText(styleSubName);
                    WindowUtils.boldMethod(goodsStylePrice);
                    ImageUtils.loadImage(glide, resize(goodsCover, 640, 640), goodsStyleImage, -1);
                    int maxCountValue = JsonUtils.getInt(sku, "count", 0);
                    if (noStorageSale) {
                        maxCountValue = 99;
                    }

                    if (maxCountValue <= 0) {
                        itemAmount = 0;
                        goodsCountNumber.setText("" + itemAmount);
                        addToCart.setText("部分商品已售罄,请重新选择");
                        saleOut = true;
                        saleOutImg.setVisibility(View.VISIBLE);
                        saleOutTxt.setVisibility(View.VISIBLE);
                        addToCart.setTextColor(getResources().getColor(R.color.WHITE));
                        addToCart.setBackgroundColor(getResources().getColor(R.color.gray_add));
                        miuView.setTextColor(getResources().getColor(R.color.gray_add));
                        addView.setTextColor(getResources().getColor(R.color.gray_add));
                    } else if (maxCountValue > 0) {
                        addToCart.setText("加入购物车");
                        saleOut = false;
                        saleOutImg.setVisibility(View.GONE);
                        saleOutTxt.setVisibility(View.GONE);
                        addToCart.setBackgroundColor(getResources().getColor(R.color.Black_Gray));
                        itemAmount = 1;
                        miuView.setTextColor(getResources().getColor(R.color.gray_add));
                        addView.setTextColor(getResources().getColor(R.color.BLACK));
                        goodsCountNumber.setText("" + itemAmount);
                    }
                    double discountMoney = itemAmount * JsonUtils.getDouble(skuPopView.getSelectedSku(), "discountPrice", 0);
                    double itemMoney = itemAmount * JsonUtils.getDouble(skuPopView.getSelectedSku(), "price", 0);
                    if (tagType.equals("3")) {
                        double reduceValue = JsonUtils.getDouble(item, "coupon.amount", 0);
                        double getReduceValue = JsonUtils.getDouble(item, "coupon.toAmount", 0);
                        if (discountMoney >= getReduceValue) {
                            discountMoney = discountMoney - reduceValue;
                        }
                    }
                    try {
                        item.put("selectAmount", itemAmount);
                        item.put("selectMoney", itemMoney);
                        item.put("selectStyleId", styleId);
                        item.put("discountMoney", discountMoney);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    getMoney();
                }
            });

            String styleId = JsonUtils.getString(item, "styleId", "");
            boolean haveStyle = false;
            if (!(styleId.equals("") || styleId == null)) {
                for (int x = 0; x < styles.length(); x++) {
                    String getStyle = JsonUtils.getString(JsonUtils.getJsonItem(styles, x, null), "styleId", "");
                    if (styleId.equals(getStyle)) {
                        skuPopView.setSelectedSku(JsonUtils.getJsonItem(styles, x, null));
                        haveStyle = true;
                        break;
                    }
                }
            }
            if (!haveStyle){
                skuPopView.setSelectedSku(JsonUtils.getJsonItem(styles, 0, null));
            }


            //减少商品数量
            miuView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    amount = JsonUtils.getInt(item, "selectAmount", 0);
                    maxCount = JsonUtils.getInt(skuPopView.getSelectedSku(), "count", 0);
                    boolean noStorageSale = JsonUtils.getBoolean(item, "noStorageSale", false);
                    if (noStorageSale) {
                        maxCount = 99;
                    }
                    if (maxCount == 0) {
                        AlertManager.showErrorToast(getActivity(), "没有库存啦", false);
                    } else {
                        if (amount >= 2) {
                            amount--;
                            goodsCountNumber.setText(amount + "");

                            double discountMoney = amount * JsonUtils.getDouble(skuPopView.getSelectedSku(), "discountPrice", 0);
                            double itemMoney = amount * JsonUtils.getDouble(skuPopView.getSelectedSku(), "price", 0);
                            if (tagType.equals("3")) {
                                double reduceValue = JsonUtils.getDouble(item, "coupon.amount", 0);
                                double getReduceValue = JsonUtils.getDouble(item, "coupon.toAmount", 0);
                                if (discountMoney >= getReduceValue) {
                                    discountMoney = discountMoney - reduceValue;
                                }
                            }
                            try {
                                item.put("selectAmount", amount);
                                item.put("selectMoney", itemMoney);
                                item.put("discountMoney", discountMoney);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            getMoney();
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
                    amount = JsonUtils.getInt(item, "selectAmount", 0);
                    maxCount = JsonUtils.getInt(skuPopView.getSelectedSku(), "count", 0);
                    boolean noStorageSale = JsonUtils.getBoolean(item, "noStorageSale", false);
                    if (noStorageSale) {
                        maxCount = 99;
                    }
                    if (maxCount == 0) {
                        AlertManager.showErrorToast(getActivity(), "没有库存啦", false);
                    } else {
                        if (amount < maxCount) {
                            amount++;
                            goodsCountNumber.setText(amount + "");
                            double discountMoney = amount * JsonUtils.getDouble(skuPopView.getSelectedSku(), "discountPrice", 0);
                            double itemMoney = amount * JsonUtils.getDouble(skuPopView.getSelectedSku(), "price", 0);
                            if (tagType.equals("3")) {
                                double reduceValue = JsonUtils.getDouble(item, "coupon.amount", 0);
                                double getReduceValue = JsonUtils.getDouble(item, "coupon.toAmount", 0);
                                if (discountMoney >= getReduceValue) {
                                    discountMoney = discountMoney - reduceValue;
                                }
                            }
                            try {
                                item.put("selectAmount", amount);
                                item.put("selectMoney", itemMoney);
                                item.put("discountMoney", discountMoney);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            getMoney();
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

            addGoodsView.addView(goodView);
        }

        //关闭商品规格选择
        LinearLayout cancel_btn = contentView.findViewById(R.id.dismiss_pop);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                designPopWindow.dismiss();
            }
        });
        ImageView closeView = contentView.findViewById(R.id.close_icon);
        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                designPopWindow.dismiss();
            }
        });


        View popBodyView = contentView.findViewById(R.id.design_pop_body);
        int height = WindowUtils.getDeviceHeight(getActivity()) * 2 / 3;
        FrameLayout.LayoutParams paramPop = (FrameLayout.LayoutParams) popBodyView.getLayoutParams();
        paramPop.height = height;

        //加入购物车
        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saleOut) {
                    return;
                }
                addShoppingCat();
            }
        });

        designPopWindow.showAtLocation(getActivity().findViewById(R.id.design_show_pop), Gravity.BOTTOM, 0, 0);
        designPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popShowBoolean = false;
                WindowManager.LayoutParams params = window.getAttributes();
                params.alpha = 1.0f;
                saleOut = false;
                window.setAttributes(params);
            }
        });

    }

    private void getMoney() {
        allPrice = 0;
        allDiscountPrice = 0;
        for (int m = 0; m < skuArray.length(); m++) {
            JSONObject item = JsonUtils.getJsonItem(skuArray, m, null);
            double money = NumberUtils.decimalDouble(JsonUtils.getDouble(item, "selectMoney", 0));
            double discountMoney = NumberUtils.decimalDouble(JsonUtils.getDouble(item, "discountMoney", 0));
            allPrice = allPrice + discountMoney;
            allDiscountPrice = allDiscountPrice + money - discountMoney;
        }
        if (allDiscountPrice == 0) {
            preferentialView.setVisibility(View.INVISIBLE);
        } else {
            preferentialView.setVisibility(View.VISIBLE);
        }
        allMoney.setText("¥" + NumberUtils.decimalDouble(allPrice));
        preferentialView.setText("已优惠:¥" + NumberUtils.decimalDouble(allDiscountPrice));
    }

    private void getParams() {
        for (int m = 0; m < skuArray.length(); m++) {
            JSONObject item = JsonUtils.getJsonItem(skuArray, m, null);
            String goodsId = JsonUtils.getString(item, "goodsId", "");
            String styleId = JsonUtils.getString(item, "selectStyleId", "");
            int count = JsonUtils.getInt(item, "selectAmount", 0);

            JSONObject cartItem = new JSONObject();
            try {
                cartItem.put("goodsId", goodsId);
                cartItem.put("styleId", styleId);
                cartItem.put("amount", count);

                shoppingCartVos.put(cartItem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //加入购物车
    private void addShoppingCat() {

        if (!AppSessionCache.getInstance().isLogin(getActivity())) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            return;
        }
        loadingDialog = new CustomLoadingDialog(getActivity());
        loadingDialog.setLoadingText("加入中");
        loadingDialog.show();

        getParams();
        JSONObject params = new JSONObject();
        try {
            params.put("planId", planId);
            params.put("planDetailId", detailId);
            params.put("shoppingCartVos", shoppingCartVos);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpRequest.postJson(AppConfig.ADD_TO_CART_LIST, null, params.toString(), JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    String res = JsonUtils.getString(response, "data", "");
                    if (res.equals("success")) {
                        AlertManager.showSuccessToast(getActivity(), "加入成功", false);
                        designPopWindow.dismiss();
                    } else {
                        AlertManager.showErrorToast(getActivity(), JsonUtils.getString(response, "message", ""), false);
                    }
                } else {
                    AlertManager.showErrorToast(getContext(), "服务器繁忙，请稍后重试", false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(getContext(), "服务器繁忙，请稍后重试", false);
            }
        });
    }

    //领取方案
    private void collectPlan() {
        if (!AppSessionCache.getInstance().isLogin(getActivity())) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            return;
        }

        loadingDialog = new CustomLoadingDialog(getActivity());
        loadingDialog.setLoadingText("关注中");
        loadingDialog.show();

        Map<String, String> params = new HashMap<>();
        params.put("planId", planId);

        HttpRequest.post(AppConfig.COLLECT_PLAN, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    String res = JsonUtils.getString(response, "data", "");
                    if (res.equals("success")) {
                        AlertManager.showSuccessToast(getActivity(), "关注成功", false);
                        EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_plan_get);
                        EventBus.getDefault().post(notification);
                        hadCollectIcon.setVisibility(View.VISIBLE);
                        hadCollectIcon.setEnabled(true);
                        collectIcon.setVisibility(View.GONE);
                        collectIcon.setEnabled(false);
                        ((DesignPlanDetailActivity) getActivity()).collection = true;
                    } else {
                        AlertManager.showErrorToast(getActivity(), JsonUtils.getString(response, "message", ""), false);
                    }
                } else {
                    AlertManager.showErrorToast(getContext(), "服务器繁忙，请稍后重试", false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(getContext(), "服务器繁忙，请稍后重试", false);
            }
        });
    }

    @Subscribe
    public void onEvent(EventBusNotification notification) {
        if (notification.getKey().equals(EventBusNotification.event_bus_plan_delete)) {
            hadCollectIcon.setVisibility(View.GONE);
            hadCollectIcon.setEnabled(false);
            collectIcon.setVisibility(View.VISIBLE);
            collectIcon.setEnabled(true);
        } else if (notification.getKey().equals(EventBusNotification.event_bus_plan_get)) {
            hadCollectIcon.setVisibility(View.VISIBLE);
            hadCollectIcon.setEnabled(true);
            collectIcon.setVisibility(View.GONE);
            collectIcon.setEnabled(false);
        }
    }

}
