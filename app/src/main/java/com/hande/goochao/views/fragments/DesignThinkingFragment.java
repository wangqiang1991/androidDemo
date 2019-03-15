package com.hande.goochao.views.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bigkoo.convenientbanner.listener.OnItemClickListener;
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
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.DesignPlanDetailActivity;
import com.hande.goochao.views.activity.LoginActivity;
import com.hande.goochao.views.activity.NewProductInformationActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.widget.SharePopupWindow;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hande.goochao.utils.ImageUtils.resize;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/9/3.
 */
@ContentView(R.layout.fragment_design_thinking)
public class DesignThinkingFragment extends BaseFragment implements View.OnClickListener {

    @ViewInject(R.id.thinking_houseTypeImage)
    private ImageView houseTypeImg;
    @ViewInject(R.id.design_think_txt)
    private TextView designThinkView;
    @ViewInject(R.id.color_relation)
    private ImageView colorRelationImg;
    @ViewInject(R.id.material_mix)
    private ImageView materialMixImg;
    @ViewInject(R.id.design_plan_title)
    private TextView houseTitle;
    @ViewInject(R.id.house_type_txt)
    private TextView houseTypeTxt;
    @ViewInject(R.id.house_area_txt)
    private TextView houseAreaTxt;
    @ViewInject(R.id.house_price_range_txt)
    private TextView housePriceRangeTxt;
    @ViewInject(R.id.house_style_txt)
    private TextView houseStyleTxt;
    @ViewInject(R.id.title_1)
    private TextView title1;
    @ViewInject(R.id.title_2)
    private TextView title2;
    @ViewInject(R.id.title_3)
    private TextView title3;
    @ViewInject(R.id.title_4)
    private TextView title4;
    @ViewInject(R.id.quan_mian_hu_xing)
    private TextView quanMianHuXingTxt;
    @ViewInject(R.id.she_ji_shuo_ming)
    private TextView sheJIShuoMingTxt;
    @ViewInject(R.id.se_cai_guan_xi)
    private TextView seCaiGuanXiTxt;
    @ViewInject(R.id.cai_liao_da_pei)
    private TextView caiLiaoDaPeiTxt;

    @ViewInject(R.id.thinking_share)
    private ImageView shareIcon;
    @ViewInject(R.id.thinking_collect)
    private ImageView collectIcon;
    @ViewInject(R.id.had_collect)
    private ImageView hadCollectIcon;

    private CustomLoadingDialog loadingDialog;

    private JSONObject jsonObject;

    private String houseTypeData;
    private int houseTypeImageWidth;
    private int houseTypeImageHeight;

    private String colorRelationData;
    private int colorRelationImageWidth;
    private int colorRelationImageHeight;

    private String materialMixData;
    private int materialImageWidth;
    private int materialImageHeight;
    private int kWidth;

    private String designThinkingData;
    private String style;
    private String planName;
    private String cover;
    private String houseType;
    private String priceRange;
    private String planId;
    private List<JSONObject> houseTypeValue = new ArrayList<>();

    private boolean loaded = false;

    private GlideRequests glide;

    //显示大图预览属性
    private List<String> newImages = new ArrayList<>();

    //滑动属性
    @ViewInject(R.id.plan_thinking_scroll)
    private ScrollView scrollView;
    private int mTouchSlop;//系统认为的最小滑动距离

    private boolean hadRegister;

    private GestureDetector gestureDetector;

    @SuppressLint("ValidFragment")
    public DesignThinkingFragment(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public DesignThinkingFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!loaded) {
            glide = GlideApp.with(getActivity());
            loaded = true;
            kWidth = WindowUtils.getDeviceWidth(getActivity());
            loadingDialog = new CustomLoadingDialog(getActivity());

            houseTypeData = JsonUtils.getString(jsonObject, "houseTypeImage", "");
            colorRelationData = JsonUtils.getString(jsonObject, "colorRelationImage", "");
            materialMixData = JsonUtils.getString(jsonObject, "materialImage", "");
            designThinkingData = JsonUtils.getString(jsonObject, "designExplain", "");
            style = JsonUtils.getString(jsonObject, "style", "");
            planName = JsonUtils.getString(jsonObject, "planName", "");
            cover = JsonUtils.getString(jsonObject, "cover", "");
            houseType = JsonUtils.getString(jsonObject, "houseType", "");
            priceRange = JsonUtils.getString(jsonObject, "priceRange", "");
            planId = JsonUtils.getString(jsonObject, "planId", "");
            houseTypeImageWidth = JsonUtils.getInt(jsonObject, "houseTypeImageWidth", 0);
            houseTypeImageHeight = JsonUtils.getInt(jsonObject, "houseTypeImageHeight", 0);
            colorRelationImageWidth = JsonUtils.getInt(jsonObject, "colorRelationImageWidth", 0);
            colorRelationImageHeight = JsonUtils.getInt(jsonObject, "colorRelationImageHeight", 0);
            materialImageWidth = JsonUtils.getInt(jsonObject, "materialImageWidth", 0);
            materialImageHeight = JsonUtils.getInt(jsonObject, "materialImageHeight", 0);

            newImages.add(houseTypeData);
            newImages.add(colorRelationData);
            newImages.add(materialMixData);

            String houseTypeStr = JsonUtils.getString(jsonObject, "houseTypeDataStr", "");
            if (!TextUtils.isEmpty(houseTypeStr)) {
                try {
                    JSONArray jsonArray = new JSONArray(houseTypeStr);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        houseTypeValue.add(object);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            shareIcon.setOnClickListener(this);
            collectIcon.setOnClickListener(this);
            hadCollectIcon.setOnClickListener(this);

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
                    if (distanceY > mTouchSlop) {
                        EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_inspiration_up);
                        notification.setValue("");
                        EventBus.getDefault().post(notification);
                    }
                    if (distanceY < -mTouchSlop) {
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

            pageInit();
            boldTxt();

            if (!hadRegister){
                hadRegister = true;
                EventBus.getDefault().register(this);
            }
        }
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

    //字体加粗
    private void boldTxt() {
        WindowUtils.boldMethod(title1);
        WindowUtils.boldMethod(title2);
        WindowUtils.boldMethod(title3);
        WindowUtils.boldMethod(title4);
        WindowUtils.boldMethod(quanMianHuXingTxt);
        WindowUtils.boldMethod(sheJIShuoMingTxt);
        WindowUtils.boldMethod(seCaiGuanXiTxt);
        WindowUtils.boldMethod(caiLiaoDaPeiTxt);

        WindowUtils.boldMethod(houseTitle);
        WindowUtils.boldMethod(houseTypeTxt);
        WindowUtils.boldMethod(houseAreaTxt);
        WindowUtils.boldMethod(housePriceRangeTxt);
        WindowUtils.boldMethod(houseStyleTxt);
        WindowUtils.boldMethod(designThinkView);
    }


    //绑定控件数据
    private void pageInit() {

        designThinkView.setText(designThinkingData);
        ImageUtils.loadImage(glide, resize(houseTypeData, houseTypeImageWidth, houseTypeImageHeight), houseTypeImg, R.mipmap.loadpicture);
        houseTypeImg.setLayoutParams(new LinearLayout.LayoutParams(kWidth, kWidth * houseTypeImageHeight / houseTypeImageWidth));
        houseTypeImg.setTag(R.id.tag_image, houseTypeData);

        ImageUtils.loadImage(glide, resize(colorRelationData, colorRelationImageWidth, colorRelationImageHeight), colorRelationImg, R.mipmap.loadpicture);
        colorRelationImg.setLayoutParams(new LinearLayout.LayoutParams(kWidth, kWidth * colorRelationImageHeight / colorRelationImageWidth));
        colorRelationImg.setTag(R.id.tag_image, colorRelationData);

        ImageUtils.loadImage(glide, resize(materialMixData, materialImageWidth, materialImageHeight), materialMixImg, R.mipmap.loadpicture);
        materialMixImg.setLayoutParams(new LinearLayout.LayoutParams(kWidth, kWidth * materialImageHeight / materialImageWidth));
        materialMixImg.setTag(R.id.tag_image, materialMixData);

        JSONObject firstType = houseTypeValue.get(0);
        String houseAreaValue = JsonUtils.getString(firstType, "houseType.buildArea", "");

        houseTitle.setText(planName);
        houseTypeTxt.setText(houseType);
        houseAreaTxt.setText(houseAreaValue + "㎡");
        housePriceRangeTxt.setText(priceRange);
        houseStyleTxt.setText(style);
    }

    @Override
    public void onClick(View v) {
        if (v == shareIcon) {
            String url = RestfulUrl.build(AppConfig.PLAN_SHARE_URL, ":planId", planId);  //待修改真实接口地址
            new SharePopupWindow(getActivity()).show(url, cover, planName, designThinkingData);
        } else if (v == collectIcon) {
            collectPlan();
        } else if (v == hadCollectIcon) {
            deleteCollection();
        }
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

    @Subscribe
    public void onEvent(EventBusNotification notification) {
        if (notification.getKey().equals(EventBusNotification.event_bus_plan_delete)) {
            hadCollectIcon.setVisibility(View.GONE);
            hadCollectIcon.setEnabled(false);
            collectIcon.setVisibility(View.VISIBLE);
            collectIcon.setEnabled(true);
        }else if (notification.getKey().equals(EventBusNotification.event_bus_plan_get)){
            hadCollectIcon.setVisibility(View.VISIBLE);
            hadCollectIcon.setEnabled(true);
            collectIcon.setVisibility(View.GONE);
            collectIcon.setEnabled(false);
        }
    }

}
