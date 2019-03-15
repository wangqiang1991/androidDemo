package com.hande.goochao.views.base;

import android.os.Bundle;
import android.os.Handler;

import com.hande.goochao.commons.EventBusNotification;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by Wangem on 2018/3/16.
 */

public class PayCloseActivity extends ToolBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    /**
     * 通知关闭
     * @param notification
     */
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
}
