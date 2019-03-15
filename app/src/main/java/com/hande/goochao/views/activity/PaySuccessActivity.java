package com.hande.goochao.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.views.base.ToolBarActivity;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ViewInject;

public class PaySuccessActivity extends ToolBarActivity implements View.OnClickListener {

    @ViewInject(R.id.go_order_btn)
    private View goOrderBtn;
    @ViewInject(R.id.go_home_btn)
    private View goHomeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_success);

        setTitle("支付成功");

        // 通知关闭
        EventBusNotification notification = new EventBusNotification(EventBusNotification.event_pay_close);
        EventBus.getDefault().post(notification);

        goOrderBtn.setOnClickListener(this);
        goHomeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == goOrderBtn) {
            Intent intent = new Intent(this, MyOrderActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("selectedIndex",2);
            startActivity(intent);
            finish();
        } else if (v == goHomeBtn) {
            finish();
        }
    }
}
