package com.hande.goochao.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.androidkun.xtablayout.XTabLayout;
import com.hande.goochao.MainActivity;
import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.HiddenAnimUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.SearchActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.widget.ChangeLineWidth;
import com.hande.goochao.views.widget.GooChaoPopupWindow;
import com.hande.goochao.views.widget.ViewPagerSlide;
import com.hande.goochao.views.widget.tablayout.TabLayout;

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

import kotlin.TuplesKt;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/7/12.
 */
@ContentView(R.layout.inspiration_fragment)
public class InspirationFragment extends BaseFragment {

    @ViewInject(R.id.inspiration_viewPager)
    private ViewPagerSlide mViewPager;
    @ViewInject(R.id.inspiration_tabs)
    private XTabLayout mTabLayout;
    @ViewInject(R.id.txtCondition)
    private TextView txtCondition;
    @ViewInject(R.id.inspiration_head_view)
    private LinearLayout headView;
    @ViewInject(R.id.choose_key_layout)
    private LinearLayout chooseKeyLayout;
    @ViewInject(R.id.search_view)
    private ImageView searchView;
    @ViewInject(R.id.top_line)
    private View top_line;
    @ViewInject(R.id.layChoose)
    private View layChoose;

    private JSONArray styleArray = new JSONArray();
    private JSONArray regionArray = new JSONArray();
    private JSONArray articleArray = new JSONArray();
    private JSONArray priceArray = new JSONArray();
    private JSONArray houseTypeArray = new JSONArray();

    private GooChaoPopupWindow choosePopView;

    private int spaceStyleSelectIndex = 0;
    private int sceneStyleSelectIndex = 0;
    private int designStyleSelectIndex = 0;
    private int regionSelect = 0;
    private int houseTypeSelect = 0;
    private int priceSelect = 0;
    private int kindSelect = 0;
    private int articleSelect = 0;
    private boolean loaded;

    //记录是否有水平滑动
    private boolean level = false;

    private boolean upAnimation;
    private boolean downAnimation;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("发现");
        ((ToolBarActivity) getActivity()).hiddenToolBar();
        ((MainActivity) getActivity()).setSearchType(SearchActivity.SEARCH_PICTURE);


        if (!loaded) {
            loaded = true;
            mViewPager.setSlide(false);
            layChoose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChoosePop();
                }
            });

            mTabLayout.addOnTabSelectedListener(new XTabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(XTabLayout.Tab tab) {
                    try {
                        renderCondition();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onTabUnselected(XTabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(XTabLayout.Tab tab) {

                }
            });

            searchView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SearchActivity.class);
                    intent.putExtra("searchType", SearchActivity.SEARCH_PICTURE);
                    startActivity(intent);
                }
            });

            headView.setTag(false);
            loadStyle();
            loadRegion();
            loadArticle();
            loadHouseType();
            loadPrice();
            initViewPager();
            try {
                renderCondition();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((ToolBarActivity) getActivity()).hiddenToolBar();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (level) {
            levelMove(true);
        }
    }

    private void initViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new DesignFragment());
        fragments.add(new SpaceFragment());
        fragments.add(new SceneFragment());
        fragments.add(new ArticleFragment());

        InspirationFragmentAdapter mFragmentAdapter =
                new InspirationFragmentAdapter(getChildFragmentManager(), fragments);

        //给ViewPager设置适配器
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(mFragmentAdapter);
        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(((MainActivity) getActivity()).selectPage);
    }

    class InspirationFragmentAdapter extends FragmentPagerAdapter {
        private List<Fragment> mFragments;

        public InspirationFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            mFragments = fragments;
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
                return "软装方案";
            } else if (position == 1) {
                return "空间搭配";
            } else if (position == 2) {
                return "场景图";
            } else {
                return "期刊";
            }
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

    private void loadRegion() {
        HttpRequest.get(AppConfig.QUERY_INFORMATION_REGION, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
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
                        regionArray.put(0, jsonObject);
                    } catch (JSONException ej) {
                    }
                    for (int m = 0; m < data.length(); m++) {
                        try {
                            regionArray.put(data.get(m));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    reloadRegion();
                }

            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                reloadRegion();
            }
        });
    }

    private void loadArticle() {
        HttpRequest.get(AppConfig.ARTICLE_KIND_GET, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONArray data = JsonUtils.getJsonArray(response, "data", null);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("typeName", "全部");
                        articleArray.put(0, jsonObject);
                    } catch (JSONException ej) {
                    }
                    for (int m = 0; m < data.length(); m++) {
                        try {
                            articleArray.put(data.get(m));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    reLoadArticle();
                }

            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                reLoadArticle();
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

    private void reLoadArticle() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadArticle();
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

    private void reloadRegion() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadRegion();
            }
        }, 500);
    }

    /**
     * 重新渲染页面查询条件
     */
    private void renderCondition() throws JSONException {
        if (mTabLayout.getSelectedTabPosition() == 0) {
            txtCondition.setVisibility(View.VISIBLE);
            layChoose.setEnabled(true);
            if (styleArray.length() == 0) {
                txtCondition.setText("全风格");
            }
            JSONObject selectObject = JsonUtils.getJsonItem(styleArray, spaceStyleSelectIndex, null);
            String typeName = spaceStyleSelectIndex == 0 ? "全风格" : JsonUtils.getString(selectObject, "name", "");
            txtCondition.setText(typeName);
        } else if (mTabLayout.getSelectedTabPosition() == 1) {
            txtCondition.setVisibility(View.INVISIBLE);
            layChoose.setEnabled(false);
        } else if (mTabLayout.getSelectedTabPosition() == 2) {
            txtCondition.setVisibility(View.VISIBLE);
            layChoose.setEnabled(true);
            if (styleArray.length() == 0 || regionArray.length() == 0) {
                txtCondition.setText("全风格/全区域/全类型");
            }
            JSONObject selectStyleObject = JsonUtils.getJsonItem(styleArray, sceneStyleSelectIndex, null);
            String typeName = sceneStyleSelectIndex == 0 ? "全风格" : JsonUtils.getString(selectStyleObject, "name", "");

            JSONObject selectRegionObject = JsonUtils.getJsonItem(regionArray, regionSelect, null);
            String regionTypeName = regionSelect == 0 ? "全区域" : JsonUtils.getString(selectRegionObject, "name", "");

            String kindName;
            if (kindSelect == 0) {
                kindName = "全类型";
            } else if (kindSelect == 1) {
                kindName = "场景售卖";
            } else {
                kindName = "人气家居";
            }

            txtCondition.setText(regionTypeName + "/" + typeName + "/" + kindName);

        } else if (mTabLayout.getSelectedTabPosition() == 3) {
            txtCondition.setVisibility(View.VISIBLE);
            layChoose.setEnabled(true);
            if (articleArray.length() == 0) {
                txtCondition.setText("全类型");
            }
            JSONObject selectObject = JsonUtils.getJsonItem(articleArray, articleSelect, null);
            String typeName = articleSelect == 0 ? "全类型" : JsonUtils.getString(selectObject, "typeName", "");
            txtCondition.setText(typeName);
        }
    }

    /**
     * 展示弹出框
     */
    private void showChoosePop() {

        if (mTabLayout.getSelectedTabPosition() == 0 && styleArray.length() == 0) {
            return;
        }

        if (mTabLayout.getSelectedTabPosition() == 1 && (styleArray.length() == 0 || priceArray.length() == 0 || houseTypeArray.length() == 0)) {
            return;
        }

        if (mTabLayout.getSelectedTabPosition() == 2 && (styleArray.length() == 0 || regionArray.length() == 0)) {
            return;
        }

        if (mTabLayout.getSelectedTabPosition() == 3 && articleArray.length() == 0) {
            return;
        }

        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.inspiration_popwindow_layout, null);
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

        //为tabLayout初始化，绑定数据
        LinearLayout styleView = contentView.findViewById(R.id.style_row);
        LinearLayout regionView = contentView.findViewById(R.id.region_row);
        LinearLayout kindView = contentView.findViewById(R.id.kind_row);
        LinearLayout articleView = contentView.findViewById(R.id.article_row);
        LinearLayout houseTypeView = contentView.findViewById(R.id.house_type_row);
        LinearLayout priceView = contentView.findViewById(R.id.price_row);

        final TabLayout styleTab = contentView.findViewById(R.id.pop_style_tabs);
        final TabLayout regionTab = contentView.findViewById(R.id.pop_region_tabs);
        final TabLayout kindTab = contentView.findViewById(R.id.pop_kind_tabs);
        final TabLayout articleTab = contentView.findViewById(R.id.pop_article_tabs);
        final TabLayout houseTypeTab = contentView.findViewById(R.id.pop_house_type_tabs);
        final TabLayout priceTab = contentView.findViewById(R.id.pop_price_tabs);

        for (int i = 0; i < styleArray.length(); i++) {
            String name = JsonUtils.getString(JsonUtils.getJsonItem(styleArray, i, null), "name", "");
            styleTab.addTab(styleTab.newTab().setText(name));
        }
        for (int i = 0; i < regionArray.length(); i++) {
            String name = JsonUtils.getString(JsonUtils.getJsonItem(regionArray, i, null), "name", "");
            regionTab.addTab(regionTab.newTab().setText(name));
        }
        for (int i = 0; i < articleArray.length(); i++) {
            String name = JsonUtils.getString(JsonUtils.getJsonItem(articleArray, i, null), "typeName", "");
            articleTab.addTab(articleTab.newTab().setText(name));
        }
        for (int i = 0; i < houseTypeArray.length(); i++) {
            String name = JsonUtils.getString(JsonUtils.getJsonItem(houseTypeArray, i, null), "name", "");
            houseTypeTab.addTab(houseTypeTab.newTab().setText(name));
        }
        for (int i = 0; i < priceArray.length(); i++) {
            String name = JsonUtils.getString(JsonUtils.getJsonItem(priceArray, i, null), "name", "");
            priceTab.addTab(priceTab.newTab().setText(name));
        }
        kindTab.addTab(kindTab.newTab().setText("全部"));
        kindTab.addTab(kindTab.newTab().setText("场景售卖"));
        kindTab.addTab(kindTab.newTab().setText("人气家居"));
        ChangeLineWidth.reflex(styleTab);
        ChangeLineWidth.reflex(regionTab);
        ChangeLineWidth.reflex(kindTab);
        ChangeLineWidth.reflex(articleTab);
        ChangeLineWidth.reflex(houseTypeTab);
        ChangeLineWidth.reflex(priceTab);

        //判断chooseRow以及初始选中位置
        if (mTabLayout.getSelectedTabPosition() == 0) {
            regionView.setVisibility(View.GONE);
            kindView.setVisibility(View.GONE);
            articleView.setVisibility(View.GONE);
            houseTypeView.setVisibility(View.GONE);
            priceView.setVisibility(View.GONE);
            styleTab.getTabAt(spaceStyleSelectIndex).select();
        } else if (mTabLayout.getSelectedTabPosition() == 1) {
            regionView.setVisibility(View.GONE);
            kindView.setVisibility(View.GONE);
            articleView.setVisibility(View.GONE);
            houseTypeView.setVisibility(View.GONE);
            priceView.setVisibility(View.GONE);
            styleView.setVisibility(View.GONE);
            styleTab.getTabAt(designStyleSelectIndex).select();
        } else if (mTabLayout.getSelectedTabPosition() == 2) {
            regionView.setVisibility(View.VISIBLE);
            kindView.setVisibility(View.VISIBLE);
            articleView.setVisibility(View.GONE);
            houseTypeView.setVisibility(View.GONE);
            priceView.setVisibility(View.GONE);
            styleTab.getTabAt(sceneStyleSelectIndex).select();
        } else if (mTabLayout.getSelectedTabPosition() == 3) {
            styleView.setVisibility(View.GONE);
            regionView.setVisibility(View.GONE);
            kindView.setVisibility(View.GONE);
            houseTypeView.setVisibility(View.GONE);
            priceView.setVisibility(View.GONE);
        }
        regionTab.getTabAt(regionSelect).select();
        kindTab.getTabAt(kindSelect).select();
        articleTab.getTabAt(articleSelect).select();
        houseTypeTab.getTabAt(houseTypeSelect).select();
        priceTab.getTabAt(priceSelect).select();

        //choose后效果
        styleTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String styleId = JsonUtils.getString(JsonUtils.getJsonItem(styleArray, styleTab.getSelectedTabPosition(), null), "styleId", "");

                if (mTabLayout.getSelectedTabPosition() == 0) {
                    spaceStyleSelectIndex = styleTab.getSelectedTabPosition();
                    EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_inspiration_style_space);
                    notification.setValue(styleId);
                    EventBus.getDefault().post(notification);
                } else if (mTabLayout.getSelectedTabPosition() == 2) {
                    sceneStyleSelectIndex = styleTab.getSelectedTabPosition();
                    EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_inspiration_style_scene);
                    notification.setValue(styleId);
                    EventBus.getDefault().post(notification);
                }
                try {
                    renderCondition();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        regionTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String regionId = JsonUtils.getString(JsonUtils.getJsonItem(regionArray, regionTab.getSelectedTabPosition(), null), "areaId", "");
                regionSelect = regionTab.getSelectedTabPosition();
                EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_inspiration_region);
                notification.setValue(regionId);
                EventBus.getDefault().post(notification);
                try {
                    renderCondition();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        kindTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String kindId = "";
                if (kindTab.getSelectedTabPosition() == 0) {
                    kindId = "";
                } else if (kindTab.getSelectedTabPosition() == 1) {
                    kindId = "2";
                } else if (kindTab.getSelectedTabPosition() == 2) {
                    kindId = "1";
                }
                kindSelect = kindTab.getSelectedTabPosition();
                EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_inspiration_kind);
                notification.setValue(kindId);
                EventBus.getDefault().post(notification);
                try {
                    renderCondition();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        articleTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String typeId = JsonUtils.getString(JsonUtils.getJsonItem(articleArray, articleTab.getSelectedTabPosition(), null), "typeId", "");
                articleSelect = articleTab.getSelectedTabPosition();
                EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_inspiration_article);
                notification.setValue(typeId);
                EventBus.getDefault().post(notification);
                try {
                    renderCondition();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (headView.getVisibility() == View.VISIBLE) {
            choosePopView.showAsDropDown(top_line);
        } else {
            choosePopView.showAsDropDown(chooseKeyLayout);
        }
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
            HiddenAnimUtils.getInstance(0, WindowUtils.dpToPixels(getActivity(), 80), headView, new HiddenAnimUtils.AnimateEvent() {
                @Override
                public void animateEnd() {
                    upAnimation = false;
                }

                @Override
                public void animateCancel() {
                    upAnimation = false;
                }
            }).closeWithAnimate();

            levelMove(true);

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
            HiddenAnimUtils.getInstance(0, WindowUtils.dpToPixels(getActivity(), 80), headView, new HiddenAnimUtils.AnimateEvent() {
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

            levelMove(false);

        } else if (notification.getKey().equals(EventBusNotification.event_bus_change_scene)) {
            mViewPager.post(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(2);
                }
            });
        } else if (notification.getKey().equals(EventBusNotification.event_bus_change_space)) {
            mViewPager.post(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(1);
                }
            });
        } else if (notification.getKey().equals(EventBusNotification.event_bus_plan_list) ||
                notification.getKey().equals(EventBusNotification.event_bus_change_plan)) {
            mViewPager.post(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(0);
                }
            });
        }
    }

    //type == true,up; type == false,down;
    private void levelMove(boolean type) {
        if (type) {
            int moveWidth = (WindowUtils.getDeviceWidth(getActivity()) - WindowUtils.dpToPixels(getActivity(), 40) - chooseKeyLayout.getWidth()) / 2;
            HiddenAnimUtils.slideview(0, -moveWidth, chooseKeyLayout);
            top_line.setVisibility(View.GONE);
            level = true;
        } else {
            int moveWidth = (WindowUtils.getDeviceWidth(getActivity()) - WindowUtils.dpToPixels(getActivity(), 40) - chooseKeyLayout.getWidth()) / 2;
            HiddenAnimUtils.slideview(-moveWidth, 0, chooseKeyLayout);
            top_line.setVisibility(View.VISIBLE);
            level = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
