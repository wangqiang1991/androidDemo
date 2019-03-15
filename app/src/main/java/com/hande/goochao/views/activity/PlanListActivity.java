package com.hande.goochao.views.activity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;
import com.hande.goochao.views.components.refresh.MySmartRefreshLayout;
import com.hande.goochao.views.widget.ChangeLineWidth;
import com.hande.goochao.views.widget.GooChaoPopupWindow;
import com.hande.goochao.views.widget.GoochaoGridView;
import com.hande.goochao.views.widget.tablayout.TabLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hande.goochao.views.base.ToolBarActivity;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/11/8.
 */
public class PlanListActivity extends ToolBarActivity {

    @ViewInject(R.id.design_inspiration_load_fail)
    private LoadFailView loadFailView;
    @ViewInject(R.id.inspiration_design_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.plan_list_grid_view)
    private GoochaoGridView designGridView;
    @ViewInject(R.id.design_refresh_layout)
    private MySmartRefreshLayout refreshLayout;
    @ViewInject(R.id.design_noDataView)
    private NoDataView noDataView;

    private View headView;
    private TextView fanganshuliang;
    private TextView huxingshuliang;
    private TextView planCount;
    private TextView typeCount;
    private TextView premisesTitle;
    private TextView premisesDesc;

    private boolean loaded = false;
    private boolean isLoading = false;

    private int pageIndex = 1;
    private int pageSize = 20;
    private int kWidth;

    private DesignAdapter designAdapter;

    private String styleId = "";
    private String houseTypeId = "";
    private String priceId = "";
    private String planId = "";
    private String houseId = "";

    private JSONArray designArray;
    private List<JSONObject> dataList = new ArrayList<>();
    private JSONArray detailArray;
    private JSONObject object;
    private JSONObject premisesObject;

    private GooChaoPopupWindow choosePopView;
    private JSONArray styleArray = new JSONArray();
    private JSONArray priceArray = new JSONArray();
    private JSONArray houseTypeArray = new JSONArray();

    private View rightView;
    private ImageView chooseView;
    @ViewInject(R.id.plan_list_top_line)
    private View topView;

    private LayoutInflater inflater;
    private GlideRequests glide;

    private int designStyleSelectIndex = 0;
    private int houseTypeSelect = 0;
    private int priceSelect = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);
        hideBackText();
        hideLine();

        if (!loaded && !isLoading) {
            glide = GlideApp.with(this);
            inflater = LayoutInflater.from(this);
            loaded = true;
            kWidth = WindowUtils.getDeviceWidth(this);

            noDataView.setImageAndText(R.mipmap.search_no_result, "软装方案正在完善中...");

            String premisesObjectStr = getIntent().getStringExtra("object");
            try {
                 premisesObject = new JSONObject(premisesObjectStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            designAdapter = new DesignAdapter();
            designGridView.setAdapter(designAdapter);

            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshLayout) {
                    loadDesignData(false, true);
                }
            });
            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout layout) {
                    loadDesignData(false, false);
                }
            });
            loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
                @Override
                public void onReload() {
                    loadFailView.setReloading();
                    loadDesignData(true, false);
                }
            });

            initRightView();
            initPage();
            loadDesignData(true, false);

            designGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    object = dataList.get(position);
                    planId = JsonUtils.getString(object, "planId", "");
                    Intent intent = new Intent(PlanListActivity.this, DesignPlanDetailActivity.class);
                    intent.putExtra("planId", planId);
                    startActivity(intent);
                }
            });
        }
    }

    private void loadStyle() {
        HttpRequest.get(AppConfig.QUERY_INFORMATION_STYLE, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONArray data = JsonUtils.getJsonArray(response, "data", null);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("name", "全部");
                        styleArray.put(0, jsonObject);
                    } catch (JSONException ej) {
                    }
                    for (int m = 0; m < data.length(); m++) {
                        try {
                            styleArray.put(data.get(m));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    reloadStyle();
                }

            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                reloadStyle();
            }
        });
    }

    private void loadPrice() {
        HttpRequest.get(AppConfig.SEARCH_KEY_WORDS + "2", null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONArray data = JsonUtils.getJsonArray(response, "data", null);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("name", "全部");
                        priceArray.put(0, jsonObject);
                    } catch (JSONException ej) {
                    }
                    for (int m = 0; m < data.length(); m++) {
                        try {
                            priceArray.put(data.get(m));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    reloadPrice();
                }

            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                reloadPrice();
            }
        });
    }

    private void loadHouseType() {
        HttpRequest.get(AppConfig.SEARCH_KEY_WORDS + "1", null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONArray data = JsonUtils.getJsonArray(response, "data", null);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("name", "全部");
                        houseTypeArray.put(0, jsonObject);
                    } catch (JSONException ej) {
                    }
                    for (int m = 0; m < data.length(); m++) {
                        try {
                            houseTypeArray.put(data.get(m));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    reloadHouseType();
                }

            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                reloadHouseType();
            }
        });
    }

    private void reloadHouseType() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadHouseType();
            }
        }, 500);
    }

    private void reloadPrice() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadPrice();
            }
        }, 500);
    }

    private void reloadStyle() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadStyle();
            }
        }, 500);
    }

    private void loadDesignData(final boolean showLoading, boolean refresh) {
        if (showLoading) {
            pageIndex = 1;
            loadingView.setVisibility(View.VISIBLE);
        }
        if (refresh) {
            pageIndex = 1;
        }
        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", "" + pageIndex);
        params.put("pageSize", "" + pageSize);
        params.put("styleId", "" + styleId);
        params.put("houseType", "" + houseTypeId);
        params.put("priceRange", "" + priceId);
        params.put("houseId", "" + houseId);

        HttpRequest.get(AppConfig.DESIGN_PLAN_LIST, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadComplete();
                loadFailView.setFinishReload();
            }

            @Override
            public void onSuccess(JSONObject response) {

                if (JsonUtils.getCode(response) == 0) {
                    if (pageIndex == 1) {
                        dataList.clear();
                    }
                    JSONArray data = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < data.length(); i++) {
                        try {
                            dataList.add(data.getJSONObject(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if (data.length() < pageSize) {
                        designGridView.setFooterViewVisibility(View.VISIBLE);
                        refreshLayout.setNoMoreData(true);
                        refreshLayout.setEnableLoadMore(false);
                    } else {
                        designGridView.setFooterViewVisibility(View.GONE);
                        refreshLayout.setNoMoreData(false);
                        refreshLayout.setEnableLoadMore(true);
                    }
                    designAdapter.notifyDataSetChanged();
                    pageIndex++;
                    loadStyle();
                    loadPrice();
                    loadHouseType();
                    loadFailView.setVisibility(View.GONE);

                    if (dataList.size() == 0) {    //展示无数据
                        noDataView.setVisibility(View.VISIBLE);
                        designGridView.setVisibility(View.GONE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                        designGridView.setVisibility(View.VISIBLE);
                    }

                } else {
                    showError(showLoading || dataList.size() == 0);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError(showLoading || dataList.size() == 0);
                AlertManager.showErrorToast(PlanListActivity.this, "服务器繁忙，请稍后重试", false);
            }
        });

    }

    private void initPage(){

        headView = LayoutInflater.from(this).inflate(R.layout.plan_list_head_view, designGridView, false);
        designGridView.addHeaderView(headView);
        fanganshuliang = headView.findViewById(R.id.plan_list_fanganshuliang);
        huxingshuliang = headView.findViewById(R.id.plan_list_huxingshuliang);
        planCount = headView.findViewById(R.id.plan_list_planCount);
        typeCount = headView.findViewById(R.id.plan_list_typeCount);
        premisesTitle = headView.findViewById(R.id.premises_title_view);
        premisesDesc = headView.findViewById(R.id.premises_desc_view);

        houseId = JsonUtils.getString(premisesObject , "houseId", "");

        String planCountStr = JsonUtils.getString(premisesObject,"planCount","");
        planCount.setText(planCountStr);

        String typeCountStr = JsonUtils.getString(premisesObject,"houseTypeCount","");
        typeCount.setText(typeCountStr);

        String premisesTitleStr = JsonUtils.getString(premisesObject,"houseName","");
        premisesTitle.setText(premisesTitleStr);

        String premisesDescStr = JsonUtils.getString(premisesObject,"address","");
        String cityStr = JsonUtils.getString(premisesObject,"city","");
        String countryStr = JsonUtils.getString(premisesObject,"county","");
        premisesDesc.setText(cityStr + countryStr + premisesDescStr);

        WindowUtils.boldMethod(fanganshuliang);
        WindowUtils.boldMethod(huxingshuliang);
        WindowUtils.boldMethod(planCount);
        WindowUtils.boldMethod(typeCount);
        WindowUtils.boldMethod(premisesTitle);
        WindowUtils.boldMethod(premisesDesc);
    }

    private void loadComplete() {
        loadingView.setVisibility(View.GONE);
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
    }

    private void initRightView(){

        rightView = inflater.inflate(R.layout.plan_list_right_view, null);
        rightView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        showRightCustomView(rightView);

        chooseView = rightView.findViewById(R.id.plan_list_choose_icon);
        chooseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPop();
            }
        });
    }

    private void showPop(){

        if (styleArray.length() == 0 || priceArray.length() == 0 || houseTypeArray.length() == 0) {
            return;
        }

        View contentView = LayoutInflater.from(this).inflate(R.layout.inspiration_popwindow_layout, null);
        choosePopView = new GooChaoPopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        // 解决popupWindow显示后不消失问题
        choosePopView.setBackgroundDrawable(new BitmapDrawable());
        choosePopView.setOutsideTouchable(true);
        choosePopView.setContentView(contentView);

        View mirrorView = contentView.findViewById(R.id.mirror_inspiration_view);
        mirrorView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                choosePopView.dismiss();
                return false;
            }
        });

        LinearLayout styleView = contentView.findViewById(R.id.style_row);
        LinearLayout houseTypeView = contentView.findViewById(R.id.house_type_row);
        LinearLayout priceView = contentView.findViewById(R.id.price_row);
        LinearLayout regionView = contentView.findViewById(R.id.region_row);
        LinearLayout kindView = contentView.findViewById(R.id.kind_row);
        LinearLayout articleView = contentView.findViewById(R.id.article_row);

        regionView.setVisibility(View.GONE);
        kindView.setVisibility(View.GONE);
        articleView.setVisibility(View.GONE);

        final TabLayout styleTab = contentView.findViewById(R.id.pop_style_tabs);
        final TabLayout houseTypeTab = contentView.findViewById(R.id.pop_house_type_tabs);
        final TabLayout priceTab = contentView.findViewById(R.id.pop_price_tabs);

        for (int i = 0; i < styleArray.length(); i++) {
            String name = JsonUtils.getString(JsonUtils.getJsonItem(styleArray, i, null), "name", "");
            styleTab.addTab(styleTab.newTab().setText(name));
        }
        for (int i = 0; i < houseTypeArray.length(); i++) {
            String name = JsonUtils.getString(JsonUtils.getJsonItem(houseTypeArray, i, null), "name", "");
            houseTypeTab.addTab(houseTypeTab.newTab().setText(name));
        }
        for (int i = 0; i < priceArray.length(); i++) {
            String name = JsonUtils.getString(JsonUtils.getJsonItem(priceArray, i, null), "name", "");
            priceTab.addTab(priceTab.newTab().setText(name));
        }
        ChangeLineWidth.reflex(styleTab);
        ChangeLineWidth.reflex(houseTypeTab);
        ChangeLineWidth.reflex(priceTab);

        styleTab.getTabAt(designStyleSelectIndex).select();
        houseTypeTab.getTabAt(houseTypeSelect).select();
        priceTab.getTabAt(priceSelect).select();

        //choose后效果
        styleTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                styleId = JsonUtils.getString(JsonUtils.getJsonItem(styleArray, styleTab.getSelectedTabPosition(), null), "styleId", "");
                designStyleSelectIndex = styleTab.getSelectedTabPosition();
                loadDesignData(true, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        houseTypeTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                houseTypeId = JsonUtils.getString(JsonUtils.getJsonItem(houseTypeArray, houseTypeTab.getSelectedTabPosition(), null), "value", "");
                houseTypeSelect = houseTypeTab.getSelectedTabPosition();
                loadDesignData(true, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        priceTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                priceId = JsonUtils.getString(JsonUtils.getJsonItem(priceArray, priceTab.getSelectedTabPosition(), null), "value", "");
                priceSelect = priceTab.getSelectedTabPosition();
                loadDesignData(true, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        choosePopView.showAsDropDown(topView);
    }

    class DesignAdapter extends BaseAdapter {

        class DesignViewHolder {

            ImageView image;
            TextView titleTxt;
            TextView houseTypeTxt;
            TextView areaTxt;
            TextView priceRangeTxt;
            TextView styleTxt;
            View garyView;

            TextView title1;
            TextView title2;
            TextView title3;
            TextView title4;

            DesignViewHolder(View view) {
                image = view.findViewById(R.id.design_plan_cover);
                titleTxt = view.findViewById(R.id.design_plan_title);
                houseTypeTxt = view.findViewById(R.id.house_type_txt);
                areaTxt = view.findViewById(R.id.house_area_txt);
                priceRangeTxt = view.findViewById(R.id.house_price_range_txt);
                styleTxt = view.findViewById(R.id.house_style_txt);
                garyView = view.findViewById(R.id.design_plan_gary_view);

                title1 = view.findViewById(R.id.title_1);
                title2 = view.findViewById(R.id.title_2);
                title3 = view.findViewById(R.id.title_3);
                title4 = view.findViewById(R.id.title_4);
            }

        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public JSONObject getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {

            if (convertView == null) {
                // 获得容器
                convertView = LayoutInflater.from(PlanListActivity.this).inflate(R.layout.inspiration_design_item, null);
                convertView.setTag(new DesignViewHolder(convertView));
            }

            DesignViewHolder viewHolder = (DesignViewHolder) convertView.getTag();

            int imgHeight = kWidth * 224 / 375;
            int imgWidth = kWidth;

            final JSONObject item = getItem(i);
            ImageUtils.loadImage(glide, ImageUtils.zoomResize(JsonUtils.getString(item, "cover", ""), 1024, 611), viewHolder.image, R.mipmap.loadpicture);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.image.getLayoutParams();
            params.width = imgWidth;
            params.height = imgHeight;
            JSONArray houseTypePlanRelations = JsonUtils.getJsonArray(item, "houseTypePlanRelations", null);
            JSONObject houseTypeObject = new JSONObject();
            JSONObject styleObject = JsonUtils.getJsonObject(item, "style", null);
            try {
                houseTypeObject = houseTypePlanRelations.getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            viewHolder.titleTxt.setText(JsonUtils.getString(item, "planName", ""));
            WindowUtils.boldMethod(viewHolder.titleTxt);

            viewHolder.houseTypeTxt.setText(JsonUtils.getString(item, "houseTypeName", ""));
            WindowUtils.boldMethod(viewHolder.houseTypeTxt);

            viewHolder.areaTxt.setText(JsonUtils.getString(houseTypeObject, "houseType.buildArea", "") + "㎡");
            WindowUtils.boldMethod(viewHolder.areaTxt);

            viewHolder.priceRangeTxt.setText(JsonUtils.getString(item, "priceRangeName", ""));
            WindowUtils.boldMethod(viewHolder.priceRangeTxt);

            viewHolder.styleTxt.setText(JsonUtils.getString(styleObject, "name", ""));
            WindowUtils.boldMethod(viewHolder.styleTxt);

            WindowUtils.boldMethod(viewHolder.title1);
            WindowUtils.boldMethod(viewHolder.title2);
            WindowUtils.boldMethod(viewHolder.title3);
            WindowUtils.boldMethod(viewHolder.title4);
            LinearLayout.LayoutParams paramsGaryView = (LinearLayout.LayoutParams) viewHolder.garyView.getLayoutParams();
            paramsGaryView.width = imgWidth;
            paramsGaryView.height = kWidth * 15 / 375;

            return convertView;
        }

    }

    private void showError(boolean showReLoad) {
        if (showReLoad) {
            loadFailView.setVisibility(View.VISIBLE);
        } else {
            refreshLayout.finishRefresh();
            refreshLayout.finishLoadMore();
            AlertManager.showErrorInfo(PlanListActivity.this);
        }
    }

}