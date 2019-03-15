package com.hande.goochao.views.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.fragments.AllOrderListFragment;
import com.hande.goochao.views.fragments.FinishedOrderListFragment;
import com.hande.goochao.views.fragments.PendingDeliverOrderListFragment;
import com.hande.goochao.views.fragments.PendingPayOrderListFragment;
import com.hande.goochao.views.fragments.PendingReceiveOrderListFragment;
import com.hande.goochao.views.widget.ChangeLineWidth;
import com.hande.goochao.views.widget.tablayout.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangenmao on 2018/3/14.
 */

public class MyOrderActivity extends ToolBarActivity {


    @ViewInject(R.id.viewPager)
    private ViewPager mViewPager;
    @ViewInject(R.id.tabs)
    private TabLayout mTabLayout;

    private int selectedIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myorder);
        setTitle("我的订单");
        selectedIndex = getIntent().getIntExtra("selectedIndex", 0);
        hideLine();
        initViewPager();

        EventBus.getDefault().register(this);
    }

    private void initViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new AllOrderListFragment());
        fragments.add(new PendingPayOrderListFragment());
        fragments.add(new PendingDeliverOrderListFragment());
        fragments.add(new PendingReceiveOrderListFragment());
        fragments.add(new FinishedOrderListFragment());

        OrderFragmentAdapter mFragmentAdapter =
                new OrderFragmentAdapter(getSupportFragmentManager(), fragments);

        //给ViewPager设置适配器
        mViewPager.setAdapter(mFragmentAdapter);
        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
        //设置选项卡默认选中
        mViewPager.setCurrentItem(selectedIndex);

        ChangeLineWidth.reflex(mTabLayout);
    }

    @Subscribe
    public void onEvent(EventBusNotification notification) {
        if (notification.getKey().equals(EventBusNotification.event_pay_close)) {
            finish();
        }
    }


    @Override
    public void finish() {
        EventBus.getDefault().unregister(this);
        super.finish();
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
                return "全部";
            } else if (position == 1) {
                return "待付款";
            } else if (position == 2) {
                return "待发货";
            } else if (position == 3) {
                return "待收货";
            } else {
                return "已完成";
            }
        }
    }

}
