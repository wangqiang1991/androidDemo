package com.hande.goochao.views.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.hande.goochao.R;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.fragments.OrderCommentsFragment;
import com.hande.goochao.views.widget.tablayout.TabLayout;

import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangenmao on 2018/3/16.
 */

public class OrderCommentsActivity extends ToolBarActivity {

    public static final int PENDING_COMMENTS = 1;
    public static final int FINISHED_COMMENTS = 2;


    @ViewInject(R.id.viewPager)
    private ViewPager mViewPager;
    @ViewInject(R.id.tabs)
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordercomments);
        setTitle("商品评价");
        hideLine();
        initViewPager();
    }

    private void initViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new OrderCommentsFragment(PENDING_COMMENTS));
        fragments.add(new OrderCommentsFragment(FINISHED_COMMENTS));

        OrderCommentsActivity.OrderFragmentAdapter mFragmentAdapter =
                new OrderCommentsActivity.OrderFragmentAdapter(getSupportFragmentManager(), fragments);

        //给ViewPager设置适配器
        mViewPager.setAdapter(mFragmentAdapter);
        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
        //设置选项卡默认选中
        mViewPager.setCurrentItem(0);
    }


    class OrderFragmentAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> mFragments;

        public OrderFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
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
                return "待评价";
            } else {
                return "已评价";
            }
        }
    }
}
