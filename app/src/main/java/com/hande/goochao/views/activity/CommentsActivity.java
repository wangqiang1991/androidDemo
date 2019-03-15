package com.hande.goochao.views.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.androidkun.xtablayout.XTabLayout;
import com.hande.goochao.R;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.fragments.CommentsFragment;
import com.hande.goochao.views.widget.tablayout.TabLayout;

import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

public class CommentsActivity extends ToolBarActivity {
    private String currentGoodsId = "";

    @ViewInject(R.id.view_pager)
    private ViewPager mViewPager;
    @ViewInject(R.id.tabs)
    private TabLayout mTabLayout;

    private List<String> mTitles = null;
    private CommentsListAdapter mFragmentAdapter = null;


    private int totalCount;
    private int imageCount;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_comments);
        setTitle("商品评价");
        hideLine();
        currentGoodsId = getIntent().getStringExtra("goodsId");
        initViewPager();
        loadCount();

    }

    class CommentsListAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> mFragments;


        public CommentsListAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
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
            return mTitles.get(position);
        }
    }

    private void initViewPager() {
        List<String> titles = new ArrayList<>();
        titles.add("全部评价");
        titles.add("带图评价");
        Boolean images[] = new Boolean[2];
        images[0] = false;
        images[1] = true;
        for(int i = 0;i < titles.size();i++){
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(i)));
        }
        List<Fragment> fragments = new ArrayList<>();
        for(int i = 0;i < titles.size();i++){
            fragments.add(new CommentsFragment(currentGoodsId,images[i]));
        }
        mFragmentAdapter =
                new CommentsListAdapter(getSupportFragmentManager(),fragments,titles);

        //给ViewPager设置适配器
        mViewPager.setAdapter(mFragmentAdapter);
        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
        //设置选项卡默认选中
        mViewPager.setCurrentItem(0);
    }

    /**
     * 功能：加载评论条数
     */
    private void loadCount() {

        String url = RestfulUrl.build(AppConfig.GET_GOODS_COMMENT_COUNT, ":goodsId", currentGoodsId);
        HttpRequest.get(url, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    JSONObject resultObject = JsonUtils.getJsonObject(response, "data", new JSONObject());
                    totalCount = JsonUtils.getInt(resultObject, "totalCount", 0);
                    imageCount = JsonUtils.getInt(resultObject, "imageCount", 0);
                    refreshTitles();
                }
            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onComplete(boolean success, JSONObject response) {

            }
        });
    }

    private void refreshTitles() {
        mTitles.clear();
        mTitles.add(String.format("全部评价(%s)",totalCount + ""));
        mTitles.add(String.format("带图评价(%s)",imageCount + ""));
        mFragmentAdapter.notifyDataSetChanged();
    }


}
