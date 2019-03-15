package com.hande.goochao.views.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.HiddenAnimUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.fragments.DesignPlanDetailFragment;
import com.hande.goochao.views.fragments.DesignThinkingFragment;
import com.hande.goochao.views.widget.ChangeLineWidth;
import com.hande.goochao.views.widget.ViewPagerSlide;
import com.hande.goochao.views.widget.tablayout.TabLayout;

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

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/9/3.
 */
public class DesignPlanDetailActivity extends ToolBarActivity {

    @ViewInject(R.id.design_detail_load_fail)
    private LoadFailView loadFailView;
    @ViewInject(R.id.detail_design_loading_page)
    private ImageView loadingView;

    @ViewInject(R.id.design_plan_viewpager)
    private ViewPager mViewPager;
    @ViewInject(R.id.design_plan_tabs)
    private TabLayout mTabLayout;

    @ViewInject(R.id.icon_back)
    private ImageView backIcon;
    @ViewInject(R.id.page_icon)
    private TextView pageIcon;
    @ViewInject(R.id.all_page_icon)
    private TextView allPageIcon;

    private String houseTypeImage;
    private String designExplain;
    private String colorRelationImage;
    private String materialImage;
    private String houseTypeDataStr;
    private String style;
    private String planName;
    private String cover;
    private String planId;
    private String priceRange;
    private String houseType;

    private int houseTypeImageWidth;
    private int houseTypeImageHeight;
    private int colorRelationImageWidth;
    private int colorRelationImageHeight;
    private int materialImageWidth;
    private int materialImageHeight;

    private JSONObject firstObject = new JSONObject();
    private JSONArray jsonArray = new JSONArray();

    private int maxCharCount = 0;
    private int kWidth;
    private int m;

    private List<JSONObject> designPlanList = new ArrayList<>();

    //滑动属性
    @ViewInject(R.id.loop_head)
    private LinearLayout headView;
    private boolean level = false;

    //tablayout高度
    private int height;

    private boolean upAnimation;
    private boolean downAnimation;

    private JSONObject datas;
    private JSONArray detailArray;
    public boolean collection = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_design_plan_detail);
        hiddenToolBar();
//        initTopBar();

        kWidth = WindowUtils.getDeviceWidth(this);

        showBack();

        planId = getIntent().getStringExtra("planId");

        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadFailView.setReloading();
                loadDetail();
            }
        });

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pageCount = mTabLayout.getSelectedTabPosition() + 1;
                pageIcon.setText("0" + pageCount);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        loadDetail();
        EventBus.getDefault().register(this);
    }

    private void loadDetail() {
        Map<String, String> params = new HashMap<>();
        HttpRequest.get(AppConfig.DESIGN_PLAN_DETAIL + planId, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    datas = JsonUtils.getJsonObject(response, "data", null);
                    detailArray = JsonUtils.getJsonArray(datas, "details", null);
                    collection = JsonUtils.getBoolean(datas, "collection", false);
                    getData();
                    setTool();
                    initViewPager();
                    resetView();
                } else {
                    showError(true);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError(true);
                AlertManager.showErrorToast(DesignPlanDetailActivity.this, "服务器繁忙，请稍后重试", false);
            }
        });
    }

    private void getData() {
        houseTypeImage = JsonUtils.getString(datas, "houseTypeImage", ""); //户型图,及宽高
        houseTypeImageWidth = JsonUtils.getInt(datas, "houseTypeImageWidth", kWidth);
        houseTypeImageHeight = JsonUtils.getInt(datas, "houseTypeImageHeight", kWidth);

        designExplain = JsonUtils.getString(datas, "designExplain", ""); //设计说明

        colorRelationImage = JsonUtils.getString(datas, "colorRelationImage", ""); //色彩关系图,及宽高
        colorRelationImageWidth = JsonUtils.getInt(datas, "colorRelationImageWidth", kWidth);
        colorRelationImageHeight = JsonUtils.getInt(datas, "colorRelationImageHeight", kWidth);

        materialImage = JsonUtils.getString(datas, "materialImage", ""); //材料搭配图,及宽高
        materialImageWidth = JsonUtils.getInt(datas, "materialImageWidth", kWidth);
        materialImageHeight = JsonUtils.getInt(datas, "materialImageHeight", kWidth);

        JSONArray houseTypePlanRelations = JsonUtils.getJsonArray(datas, "houseTypePlanRelations", null);
        houseTypeDataStr = houseTypePlanRelations.toString();

        style = JsonUtils.getString(datas, "style.name", "");//风格
        planName = JsonUtils.getString(datas, "planName", "");//设计名称
        cover = JsonUtils.getString(datas, "cover", "");//封面
        houseType = JsonUtils.getString(datas, "houseTypeName", "");//户型名称（与查询条件对应）
        priceRange = JsonUtils.getString(datas, "priceRangeName", "");//价格区间


        JsonUtils.put(firstObject, "houseTypeImage", houseTypeImage);
        JsonUtils.put(firstObject, "houseTypeImageWidth", houseTypeImageWidth);
        JsonUtils.put(firstObject, "houseTypeImageHeight", houseTypeImageHeight);
        JsonUtils.put(firstObject, "designExplain", designExplain);
        JsonUtils.put(firstObject, "colorRelationImage", colorRelationImage);
        JsonUtils.put(firstObject, "colorRelationImageWidth", colorRelationImageWidth);
        JsonUtils.put(firstObject, "colorRelationImageHeight", colorRelationImageHeight);
        JsonUtils.put(firstObject, "materialImage", materialImage);
        JsonUtils.put(firstObject, "materialImageWidth", materialImageWidth);
        JsonUtils.put(firstObject, "materialImageHeight", materialImageHeight);
        JsonUtils.put(firstObject, "houseTypeDataStr", houseTypeDataStr);
        JsonUtils.put(firstObject, "style", style);
        JsonUtils.put(firstObject, "planName", planName);
        JsonUtils.put(firstObject, "cover", cover);
        JsonUtils.put(firstObject, "houseType", houseType);
        JsonUtils.put(firstObject, "priceRange", priceRange);
        JsonUtils.put(firstObject, "planId", planId);
        JsonUtils.put(firstObject, "collection", collection);

        String designPlanDetailListStr = detailArray.toString();
        if (!TextUtils.isEmpty(designPlanDetailListStr)) {
            try {
                jsonArray = new JSONArray(designPlanDetailListStr);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    designPlanList.add(object);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class DesignPlanDetailAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> mFragments;
        private List<JSONObject> mTitles;

        public DesignPlanDetailAdapter(FragmentManager fm, List<Fragment> fragments, List<JSONObject> titles) {
            super(fm);
            mFragments = fragments;
            mTitles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "设\n计\n概\n念";
            } else {
                String newName = "";
                String name = JsonUtils.getString(mTitles.get(position - 1), "detailName", "-");
                for (int i = 0; i < name.length(); i++) {
                    char ch = name.charAt(i);
                    if ((i + 1) == name.length()) {
                        newName = newName + ch;
                    } else {
                        newName = newName + ch + "\n";
                    }
                }
                return newName;
            }
        }

    }

    private void initViewPager() {
        ChangeLineWidth.reflex(mTabLayout);

        int selectedIndex = 0;
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new DesignThinkingFragment(firstObject));
        for (int i = 0; i < designPlanList.size(); i++) {
            fragments.add(new DesignPlanDetailFragment(designPlanList.get(i), firstObject));
        }
        DesignPlanDetailAdapter mFragmentAdapter = new DesignPlanDetailAdapter(getSupportFragmentManager(), fragments, designPlanList);

        //给ViewPager设置适配器
        mViewPager.setAdapter(mFragmentAdapter);
        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
        //设置选项卡默认选中
        mViewPager.setCurrentItem(selectedIndex);

        for (int i = 0; i < mFragmentAdapter.getCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);//获得每一个tab
            tab.setCustomView(R.layout.design_tab_item);//给每一个tab设置view
            if (i == 0) {
                // 设置第一个tab的TextView是被选择的样式
                tab.getCustomView().findViewById(R.id.tab_item_view).setSelected(true);//第一个tab被选中
            }
            String newName = "";
            if (i == 0) {
                newName = "设\n计\n概\n念";
            } else {
                String name = JsonUtils.getString(designPlanList.get(i - 1), "detailName", "-");
                for (m = 0; m < name.length(); m++) {
                    char ch = name.charAt(m);
                    if ((m + 1) == name.length()) {
                        newName = newName + ch;
                    } else {
                        newName = newName + ch + "\n";
                    }
                }
                if (m > maxCharCount) {
                    maxCharCount = m;
                }
            }

            TextView textView = (TextView) tab.getCustomView().findViewById(R.id.tab_item_view);
            textView.setText(newName);//设置tab上的文字
            WindowUtils.boldMethod(textView);
        }

        height = 100 + (maxCharCount + 1) * 30;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mTabLayout.getLayoutParams();
        params.width = kWidth;
        params.height = height;

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getCustomView().findViewById(R.id.tab_item_view).setSelected(true);
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getCustomView().findViewById(R.id.tab_item_view).setSelected(false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    private void setTool() {

        int allPageCount = jsonArray.length() + 1;
        allPageIcon.setText("0" + allPageCount);
        int pageCount = mTabLayout.getSelectedTabPosition() + 1;
        pageIcon.setText("0" + pageCount);

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Subscribe
    public void onEvent(EventBusNotification notification) {
        if (notification.getKey().equals(EventBusNotification.event_bus_inspiration_up)) {

            if (upAnimation) {
                return;
            }

            if (headView.getVisibility() != View.VISIBLE) {
                return;
            }

            if (downAnimation) {
                headView.clearAnimation();
            }

            upAnimation = true;
            HiddenAnimUtils.getInstance(0, height, headView, new HiddenAnimUtils.AnimateEvent() {
                @Override
                public void animateEnd() {
                    upAnimation = false;
                }

                @Override
                public void animateCancel() {
                    upAnimation = false;
                }
            }).closeWithAnimate();

        } else if (notification.getKey().equals(EventBusNotification.event_bus_inspiration_down)) {

            if (downAnimation) {
                return;
            }

            if (headView.getVisibility() != View.GONE) {
                return;
            }

            if (upAnimation) {
                headView.clearAnimation();
            }

            downAnimation = true;
            HiddenAnimUtils.getInstance(0, height, headView, new HiddenAnimUtils.AnimateEvent() {
                @Override
                public void animateEnd() {
                    downAnimation = false;
                    headView.setVisibility(View.VISIBLE);
                }

                @Override
                public void animateCancel() {
                    downAnimation = false;
                }
            }).openWithAnim();

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void showError(boolean showReLoad) {
        if (showReLoad) {
            loadingView.setVisibility(View.GONE);
            loadFailView.setVisibility(View.VISIBLE);
        } else {
            loadingView.setVisibility(View.GONE);
            AlertManager.showErrorInfo(this);
        }
    }
}
