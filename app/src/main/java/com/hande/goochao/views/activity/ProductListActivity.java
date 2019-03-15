package com.hande.goochao.views.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.controller.BadgeView;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.fragments.ProductListFragment;
import com.hande.goochao.views.widget.ChangeLineWidth;
import com.hande.goochao.views.widget.tablayout.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends ToolBarActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    @ViewInject(R.id.viewpager)
    private ViewPager mViewPager;
    @ViewInject(R.id.tabs)
    private TabLayout mTabLayout;

    private BadgeView cartCountView;
    private ImageView searchBtn;
    private ImageView cartBtn;

    private String currentCategoryId = "";
    private List<JSONObject> categoryList = new ArrayList<>();
    private String all = "";
    private String firstId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        hideLine();
        hideBackText();
        setTitle("商品列表");

        showBack();
        showRightCustomView(R.layout.layout_home_right);
        cartCountView = findViewById(R.id.cart_count_view);
        searchBtn = findViewById(R.id.search_btn);
        cartBtn = findViewById(R.id.cart_btn);
        searchBtn.setOnClickListener(this);
        cartBtn.setOnClickListener(this);
        cartCountView.hide();

        currentCategoryId = getIntent().getStringExtra("categoryId");
        all = getIntent().getStringExtra("all");
        firstId = getIntent().getStringExtra("firstId");
        String categoryListStr = getIntent().getStringExtra("categoryList");
        if (!TextUtils.isEmpty(categoryListStr)) {
            try {
                JSONArray jsonArray = new JSONArray(categoryListStr);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    categoryList.add(object);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        initViewPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getShoppingCartCount();
    }

    //跳转到商品详情
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(this, NewProductInformationActivity.class);
        try {
            String goodsId = categoryList.get(i).getString("goodsId");
            intent.putExtra("goodsId", goodsId);
            this.startActivity(intent);
        } catch (JSONException e) {
            AppLog.e("error", e);
        }
    }

    //横屏时不再执行onCreate方法，打印当前屏幕状态
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            Log.i("LANDSCAPE = ", String.valueOf(Configuration.ORIENTATION_LANDSCAPE));
        }
        else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            Log.i("LANDSCAPE = ", String.valueOf(Configuration.ORIENTATION_PORTRAIT));

        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View v) {
        if (v == searchBtn) {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("searchType", SearchActivity.SEARCH_GOODS);
            startActivity(intent);
        } else if (v == cartBtn) {
            Intent intent = new Intent(this, ShopCartActivity.class);
            startActivity(intent);
        }
    }

    class CategoryListAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> mFragments;
        private List<JSONObject> mTitles;

        public CategoryListAdapter(FragmentManager fm, List<Fragment> fragments, List<JSONObject> titles) {
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
            return JsonUtils.getString(mTitles.get(position), "name", "-");
        }

    }

    private void initViewPager() {
        ChangeLineWidth.reflex(mTabLayout);

        int selectedIndex = 0;
        if (all.equals("1")){
            selectedIndex = 0;
        }else {
            for (int i = 0; i < categoryList.size(); i++) {
                String categoryId = JsonUtils.getString(categoryList.get(i), "categoryId", "");
                if (currentCategoryId.equals(categoryId)) {
                    selectedIndex = i;
                }
            }
        }

        List<Fragment> fragments = new ArrayList<>();
        for (int i = 0; i < categoryList.size();i++) {
            fragments.add(new ProductListFragment(firstId, JsonUtils.getString(categoryList.get(i), "categoryId", null)));
        }
        CategoryListAdapter mFragmentAdapter =
                new CategoryListAdapter(getSupportFragmentManager(), fragments, categoryList);

        //给ViewPager设置适配器
        mViewPager.setAdapter(mFragmentAdapter);
        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
        //设置选项卡默认选中
        mViewPager.setCurrentItem(selectedIndex);
    }

    private void getShoppingCartCount() {
        if (!AppSessionCache.getInstance().isLogin(this)) {
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
                    cartCountView.setText(count + "");
                    if (count == 0) {
                        cartCountView.setVisibility(View.GONE);
                    } else {
                        cartCountView.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
            }
        });
    }
}