package com.hande.goochao.views.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.hande.goochao.R;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.fragments.CollectionArticleFragment;
import com.hande.goochao.views.fragments.CollectionGoodsFragment;
import com.hande.goochao.views.fragments.CollectionPictureFragment;
import com.hande.goochao.views.widget.tablayout.TabLayout;

import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LMC on 2018/3/15.
 */

public class CollectionActivity extends ToolBarActivity {

    @ViewInject(R.id.collection_viewPager)
    private ViewPager mViewPager;
    @ViewInject(R.id.collection_tabs)
    private TabLayout mTabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        setTitle("我的收藏");

        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        hideLine();
        initViewPager();
    }

    private void initViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new CollectionGoodsFragment());
        fragments.add(new CollectionPictureFragment());
        fragments.add(new CollectionArticleFragment());

        CollectionFragmentAdapter mFragmentAdapter =
                new CollectionFragmentAdapter(getSupportFragmentManager(), fragments);

        //给ViewPager设置适配器
        mViewPager.setAdapter(mFragmentAdapter);
        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);

    }

    class CollectionFragmentAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> mFragments;

        public CollectionFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
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
                return "商品";
            } else if (position == 1) {
                return "图片";
            } else {
                return "期刊";
            }
        }
    }

}


