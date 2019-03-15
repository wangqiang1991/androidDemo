package com.hande.goochao.views.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.hande.goochao.R;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.Params;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.pay.PayResult;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.NumberUtils;
import com.hande.goochao.utils.PriceUtils;
import com.hande.goochao.views.base.PayCloseActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.ConfirmDialog;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.util.HashMap;
import java.util.Map;

public class PayCenterActivity extends PayCloseActivity implements View.OnClickListener {

    @ViewInject(R.id.order_text)
    private TextView orderText;
    @ViewInject(R.id.receiver_name_text)
    private TextView receiverNameText;
    @ViewInject(R.id.pay_text)
    private TextView payText;
    @ViewInject(R.id.pay_btn)
    private Button payBtn;
    @ViewInject(R.id.wechat_layout)
    private View wechatLayout;
    @ViewInject(R.id.alipay_layout)
    private View alipayLayout;

    private CustomLoadingDialog loadingDialog;

    private String orderDetailStr;
    private JSONObject orderDetail;

    private static final int SDK_PAY_FLAG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_center);

        setTitle("支付中心");
        hideLine();

        orderDetailStr = getIntent().getStringExtra("orderDetail");
        if (StringUtils.isEmpty(orderDetailStr)) {
            AlertManager.showErrorToast(this, "参数无效", false);
            return;
        }

        orderDetail = JsonUtils.newJsonObject(orderDetailStr, new JSONObject());

        String orderNumber = JsonUtils.getString(orderDetail, "orderNumber", "");
        String reallyName = JsonUtils.getString(orderDetail, "reallyName", "");

        orderText.setText("订单号：" + orderNumber);
        receiverNameText.setText("收货人：" + reallyName);

        double settlementMoney = JsonUtils.getDouble(orderDetail, "settlementMoney", 0);
        double expressMoney = JsonUtils.getDouble(orderDetail, "expressMoney", 0);
        double add = settlementMoney + expressMoney;
        payText.setText(Html.fromHtml("实付款：<b>¥" + PriceUtils.beautify(NumberUtils.decimal(add)) + "</b>"));

        wechatLayout.setSelected(true);
        wechatLayout.setOnClickListener(this);
        alipayLayout.setOnClickListener(this);
        payBtn.setOnClickListener(this);

        loadingDialog = new CustomLoadingDialog(this);
    }

    @Override
    protected void onBackListener() {

        ConfirmDialog alertDialog = new ConfirmDialog(PayCenterActivity.this, ConfirmDialog.ConfirmDialogType.ConfirmDialogType_Warning);
        alertDialog.setMsg("取消后订单将保留一段时间    确认放弃该订单吗?")
                .setLeftButtonText("继续支付")
                .setRightButtonText("确认离开")
                .setCallBack(new ConfirmDialog.CallBack() {
                    @Override
                    public void buttonClick(Dialog dialog, boolean leftClick) {
                        dialog.dismiss();
                        if (!leftClick) {
                            finish();
                        }
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
        if (v == wechatLayout) {
            selectWechat();
        } else if (v == alipayLayout) {
            selectAlipay();
        } else if (v == payBtn) {
            doPay();
        }
    }

    private void doPay() {
        if (wechatLayout.isSelected()) {
            // 微信支付
            wechatPay();
        } else {
            // 支付宝支付
            alipayPay();
        }
    }

    private void selectAlipay() {
        wechatLayout.setSelected(false);
        alipayLayout.setSelected(true);
    }

    private void selectWechat() {
        wechatLayout.setSelected(true);
        alipayLayout.setSelected(false);
    }

    /**
     * 调用微信支付
     */
    private void wechatPay() {
        loadingDialog.show();
        String orderNumber = JsonUtils.getString(orderDetail, "orderNumber", null);
        String orderId = JsonUtils.getString(orderDetail, "orderId" , null);
        String url = RestfulUrl.build(AppConfig.WECHAT_PREPAY, ":orderNumber", orderNumber);
        HttpRequest.post(url, null, Params.buildForStr("platform", "APP", "orderId", orderId), JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    // 唤起微信支付
                    callWechatPay(JsonUtils.getJsonObject(response, "data", null));
                } else {
                    AlertManager.showErrorToast(PayCenterActivity.this, JsonUtils.getResponseMessage(response), false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(PayCenterActivity.this, "服务器繁忙，请稍后重试", false);
                AppLog.e("err", ex);
            }
        });
    }

    private void alipayPay() {
        loadingDialog.show();
        String orderNumber = JsonUtils.getString(orderDetail, "orderNumber", null);
        String orderId = JsonUtils.getString(orderDetail, "orderId" , null);
        String url = RestfulUrl.build(AppConfig.ALIPAY_PREPAY_PARAMS, ":orderNumber", orderNumber);
        Map<String,String> params = new HashMap<>();
        params.put("orderId",orderId);
        HttpRequest.get(url, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    // 唤起支付宝支付
                    callAlipayPay(JsonUtils.getString(response, "data", null));
                } else {
                    AlertManager.showErrorToast(PayCenterActivity.this, JsonUtils.getResponseMessage(response), false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(PayCenterActivity.this, "服务器繁忙，请稍后重试", false);
                AppLog.e("err", ex);
            }
        });
    }

    private void callWechatPay(JSONObject data) {
        if (data == null) {
            AlertManager.showErrorToast(this, "支付参数错误", false);
            return;
        }

        String partnerId = JsonUtils.getString(data, "pay.partnerid", null);
        String prepayId = JsonUtils.getString(data, "pay.prepayid", null);
        String packageValue = JsonUtils.getString(data, "pay.package", null);
        String nonceStr = JsonUtils.getString(data, "pay.noncestr", null);
        long timeStamp = JsonUtils.getLong(data, "pay.timestamp", 0l);
        String sign = JsonUtils.getString(data, "pay.sign", null);

        PayReq request = new PayReq();
        request.appId = AppConfig.WX_APPID;
        request.partnerId = partnerId;
        request.prepayId = prepayId;
        request.packageValue = packageValue;
        request.nonceStr = nonceStr;
        request.timeStamp = timeStamp + "";
        request.sign = sign;

        boolean res = WXAPIFactory.createWXAPI(this, AppConfig.WX_APPID).sendReq(request);
        if (!res) {
            AlertManager.showErrorToast(this, "发起微信支付失败，请稍后重试", false);
        }
    }

    private void callAlipayPay(final String orderString) {
        if (StringUtils.isEmpty(orderString)) {
            AlertManager.showErrorToast(this, "支付参数错误", false);
            return;
        }
        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                PayTask alipay = new PayTask(PayCenterActivity.this);
                Map<String, String> result = alipay.payV2(orderString, true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                    if (TextUtils.equals(resultStatus, "9000")) {
                        AlertManager.showSuccessToast(PayCenterActivity.this, "支付成功", false);
                        Intent intent = new Intent(PayCenterActivity.this, PaySuccessActivity.class);
                        startActivity(intent);
                    } else if (TextUtils.equals(resultStatus, "8000")) {
                        AlertManager.showErrorToast(PayCenterActivity.this, "正在处理中，支付结果未知", false);
                    } else if (TextUtils.equals(resultStatus, "5000")) {
                        AlertManager.showErrorToast(PayCenterActivity.this, "重复发起支付请求", false);
                    } else if (TextUtils.equals(resultStatus, "6001")) {
                        AlertManager.showErrorToast(PayCenterActivity.this, "支付被取消", false);
                    } else if (TextUtils.equals(resultStatus, "6002")) {
                        AlertManager.showErrorToast(PayCenterActivity.this, "网络连接出错", false);
                    } else if (TextUtils.equals(resultStatus, "6004")) {
                        AlertManager.showErrorToast(PayCenterActivity.this, "支付结果未知", false);
                    } else {
                        AlertManager.showErrorToast(PayCenterActivity.this, "支付失败", false);
                    }
                    break;
                }
            }
        }
    };
}
