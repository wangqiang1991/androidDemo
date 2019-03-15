package com.hande.goochao.views.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.DoubleUtils;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.BaseActivity;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.ConfirmDialog;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * 功能：待支付订单详情
 * Created by Wangenmao on 2018/3/16.
 */

public class PendingPayOrderDetailActivity extends BaseActivity implements LoadFailView.OnReloadListener, View.OnClickListener {

    private String jsonOrderStr;
    private JSONObject jsonOrder;

    @ViewInject(R.id.pay_activity_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.layContent)
    private View layContent;
    @ViewInject(R.id.layBottom)
    private View layBottom;
    @ViewInject(R.id.txtReallyName)
    private TextView txtReallyName;
    @ViewInject(R.id.txtPhone)
    private TextView txtPhone;
    @ViewInject(R.id.txtAddress)
    private TextView txtAddress;
    @ViewInject(R.id.layOrderGoods)
    private LinearLayout layOrderGoods;
    @ViewInject(R.id.back_btn)
    private View back_btn;
    @ViewInject(R.id.txtExpressMoney)
    private TextView txtExpressMoney;
    @ViewInject(R.id.txtSaleMoney)
    private TextView txtSaleMoney;
    @ViewInject(R.id.txtOrderMoney)
    private TextView txtOrderMoney;
    @ViewInject(R.id.btnPay)
    private Button btnPay;
    @ViewInject(R.id.btnCancelOrder)
    private Button btnCancelOrder;
    @ViewInject(R.id.remark_view)
    private View remarkView;
    @ViewInject(R.id.txtRemark)
    private TextView txtRemark;
    @ViewInject(R.id.middle_line)
    private View line;
    @ViewInject(R.id.goods_amount)
    private TextView txtAmount;
    @ViewInject(R.id.goods_item_price)
    private TextView txtMoney;

    private String goodsId;
    private double allMoney;
    private int allAmount;
    private JSONObject goodsObject;
    private JSONArray orderGoodsArray = new JSONArray();
    private LayoutInflater inflater;

    private String addressCode;

    private GlideRequests glide;

    //加粗
    @ViewInject(R.id.deng_dai_jie_suan)
    private TextView dengDaiJieSuan;
    @ViewInject(R.id.gai_bi_dd)
    private TextView gaiBiDingDan;
    @ViewInject(R.id.daizhifu_yun_fei)
    private TextView yunFei;
    @ViewInject(R.id.daizhifu_you_hui)
    private TextView youHui;
    @ViewInject(R.id.daizhifu_ding_dan_zong_jia)
    private TextView dingDanZongJia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pendingpayorderdetail);
        glide = GlideApp.with(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
//            window.setNavigationBarColor(Color.TRANSPARENT);
        }


        try {
            jsonOrderStr = getIntent().getStringExtra("jsonOrderStr");
            jsonOrder = new JSONObject(jsonOrderStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        loadFailView.setOnReloadListener(this);
        inflater = LayoutInflater.from(this);
        back_btn.setOnClickListener(this);
        btnPay.setOnClickListener(this);
        btnCancelOrder.setOnClickListener(this);

        loadOrderDetail();

        EventBus.getDefault().register(this);

        WindowUtils.boldMethod(dengDaiJieSuan);
        WindowUtils.boldMethod(gaiBiDingDan);
        WindowUtils.boldMethod(yunFei);
        WindowUtils.boldMethod(youHui);
        WindowUtils.boldMethod(dingDanZongJia);
    }

    private void loadOrderDetail() {

        String orderId = JsonUtils.getString(jsonOrder, "orderId", "");
        String url = AppConfig.PAY_ORDER_DETAIL.replace(":orderId", orderId);
        loadingView.setVisibility(View.VISIBLE);
        loadFailView.setVisibility(View.GONE);

        HttpRequest.get(url, null, null, JSONObject.class, new RequestCallback<JSONObject>() {

            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    loadFailView.setVisibility(View.GONE);
                    layContent.setVisibility(View.VISIBLE);
                    layBottom.setVisibility(View.VISIBLE);

                    try {
                        JSONObject dataObject = response.getJSONObject("data");
                        jsonOrder.put("reallyName", JsonUtils.getString(dataObject, "reallyName", ""));
                        jsonOrder.put("phone", JsonUtils.getString(dataObject, "phone", ""));
                        jsonOrder.put("address", JsonUtils.getString(dataObject, "address", ""));
                        jsonOrder.put("orderGoodsRelations", JsonUtils.getJsonArray(dataObject, "orderGoodsRelations", null));
                        jsonOrder.put("goodsMoney", JsonUtils.getDouble(dataObject, "totalMoney", 0));
                        jsonOrder.put("expressMoney", JsonUtils.getDouble(dataObject, "expressMoney", 0));
                        jsonOrder.put("orderNumber", JsonUtils.getString(dataObject, "orderNumber", ""));
                        jsonOrder.put("message", JsonUtils.getString(dataObject, "message", ""));
                        orderGoodsArray = JsonUtils.getJsonArray(dataObject, "orderGoodsRelations", null);

                        addressCode = JsonUtils.getString(dataObject, "addressCityCode", "");
                        double totalMoney = JsonUtils.getDouble(dataObject, "totalMoney", 0);
                        double settlementMoney = JsonUtils.getDouble(dataObject, "settlementMoney", 0);
                        double expressTotalMoney = JsonUtils.getDouble(dataObject, "expressTotalMoney", 0);
                        double expressMoney = JsonUtils.getDouble(dataObject, "expressMoney", 0);
                        double saleMoney = (totalMoney - settlementMoney) + (expressTotalMoney - expressMoney);
                        jsonOrder.put("saleMoney", saleMoney);

                        bindSource();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                    layContent.setVisibility(View.GONE);
                    layBottom.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Throwable ex) {
                loadFailView.setVisibility(View.VISIBLE);
                AlertManager.showErrorToast(PendingPayOrderDetailActivity.this, "服务器繁忙，请稍后重试", false);
            }
        });
    }

    private void bindSource() {
        String remark = JsonUtils.getString(jsonOrder, "message", "");
        if (remark.equals("")) {
            remarkView.setVisibility(View.GONE);
            line.setVisibility(View.GONE);
        } else {
            remarkView.setVisibility(View.VISIBLE);
            line.setVisibility(View.VISIBLE);
            txtRemark.setText(remark);
            WindowUtils.boldMethod(txtRemark);
        }

        txtReallyName.setText("" + JsonUtils.getString(jsonOrder, "reallyName", "--"));
        WindowUtils.boldMethod(txtReallyName);
        txtPhone.setText(JsonUtils.getString(jsonOrder, "phone", "--"));
        WindowUtils.boldMethod(txtPhone);
        txtAddress.setText("" + JsonUtils.getString(jsonOrder, "address", "--"));
        WindowUtils.boldMethod(txtAddress);

        initOrderGoods();

        double expressMoney = JsonUtils.getDouble(jsonOrder, "expressMoney", 0);
        String expressMoneyStr = DoubleUtils.format(expressMoney).replace(".00", "");
        txtExpressMoney.setText("¥  " + expressMoneyStr);
        WindowUtils.boldMethod(txtExpressMoney);

        double saleMoney = JsonUtils.getDouble(jsonOrder, "saleMoney", 0);
        String saleMoneyStr = DoubleUtils.format(saleMoney).replace(".00", "");
        txtSaleMoney.setText("-  " + saleMoneyStr);
        WindowUtils.boldMethod(txtSaleMoney);

        double settlementMoney = JsonUtils.getDouble(jsonOrder, "settlementMoney", 0);
        double orderMoney = settlementMoney + expressMoney;
        String orderMoneyStr = DoubleUtils.format(orderMoney).replace(".00", "");
        txtOrderMoney.setText("¥  " + orderMoneyStr);
        WindowUtils.boldMethod(txtOrderMoney);

        txtAmount.setText("" + allAmount + " 件商品总价");
        WindowUtils.boldMethod(txtAmount);
        String moneyValue = DoubleUtils.format(allMoney).replace(".00", "");
        txtMoney.setText("¥  " + moneyValue);
        WindowUtils.boldMethod(txtMoney);
    }

    private void initOrderGoods() {
        JSONArray orderGoodsRelations = JsonUtils.getJsonArray(jsonOrder, "orderGoodsRelations", null);
        for (int i = 0; i < orderGoodsRelations.length(); i++) {
            goodsObject = JsonUtils.getJsonItem(orderGoodsRelations, i, null);
            final View goodsItemView = inflater.inflate(R.layout.order_detail_goods_item, null);
            goodsItemView.setTag(goodsObject);

            ImageView goodsCover = goodsItemView.findViewById(R.id.goodsCover);
            String cover = JsonUtils.getString(goodsObject, "cover", "");
            ImageUtils.loadImage(glide, ImageUtils.resize(cover, 500, 500), goodsCover, -1);

            TextView txtGoodsTitle = goodsItemView.findViewById(R.id.txtGoodsTitle);
            txtGoodsTitle.setText(JsonUtils.getString(goodsObject, "title", ""));
            WindowUtils.boldMethod(txtGoodsTitle);

            TextView txtGoodsStyle = goodsItemView.findViewById(R.id.txtGoodsStyle);
            txtGoodsStyle.setText(JsonUtils.getString(goodsObject, "styleName", "").trim());
            WindowUtils.boldMethod(txtGoodsStyle);

            TextView txtGoodsSub = goodsItemView.findViewById(R.id.txtGoodsSub);
            txtGoodsSub.setText(JsonUtils.getString(goodsObject, "subName", "").trim());
            WindowUtils.boldMethod(txtGoodsSub);

            TextView txtGoodsPrice = goodsItemView.findViewById(R.id.txtGoodsPrice);
            double stylePrice = JsonUtils.getDouble(goodsObject, "stylePrice", 0);
            txtGoodsPrice.setText("¥" + DoubleUtils.format(stylePrice));
            WindowUtils.boldMethod(txtGoodsPrice);

            TextView txtGoodsCount = goodsItemView.findViewById(R.id.txtGoodsCount);
            int amount = JsonUtils.getInt(goodsObject, "amount", 0);
            txtGoodsCount.setText("×  " + amount);
            WindowUtils.boldMethod(txtGoodsCount);

            allAmount = allAmount + amount;
            allMoney = allMoney + JsonUtils.getDouble(JsonUtils.getJsonItem(orderGoodsArray, i, null), "money", 0);

            goodsItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject newObject = (JSONObject) goodsItemView.getTag();
                    goodsId = JsonUtils.getString(newObject, "goodsId", "");
                    Intent intent = new Intent(PendingPayOrderDetailActivity.this, NewProductInformationActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("goodsId", goodsId);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

            //判断四种状态
            boolean pulled = JsonUtils.getBoolean(goodsObject, "pulled", false); //是否下架
            boolean sellout = JsonUtils.getBoolean(goodsObject, "sellout", false); //是否售罄
            boolean noStorage = JsonUtils.getBoolean(goodsObject, "noStorage", false); //是否库存不足
            ImageView pulledImg = goodsItemView.findViewById(R.id.pulled_img);
            ImageView selloutImg = goodsItemView.findViewById(R.id.sellout_img);
            ImageView noStorageImg = goodsItemView.findViewById(R.id.noStorage_img);
            ImageView cantSent = goodsItemView.findViewById(R.id.cant_sent);

            if (pulled) {
                pulledImg.setVisibility(View.VISIBLE);
                btnPay.setBackgroundColor(getResources().getColor(R.color.gray_add));
                btnPay.setEnabled(false);
            } else {
                if (sellout) {
                    selloutImg.setVisibility(View.VISIBLE);
                    btnPay.setBackgroundColor(getResources().getColor(R.color.gray_add));
                    btnPay.setEnabled(false);
                } else {
                    if (noStorage) {
                        noStorageImg.setVisibility(View.VISIBLE);
                        btnPay.setBackgroundColor(getResources().getColor(R.color.gray_add));
                        btnPay.setEnabled(false);
                    } else {
                        boolean support = getSupportCity(goodsObject);
                        if (support) {
                            cantSent.setVisibility(View.GONE);
                        } else {
                            cantSent.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
            layOrderGoods.addView(goodsItemView);
        }
        WindowUtils.setMargins(layOrderGoods, WindowUtils.dpToPixels(this, 10), 0, WindowUtils.dpToPixels(this, 10), 0);
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

    @Override
    public void onReload() {
        loadOrderDetail();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        x.view().inject(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        x.view().inject(this);
    }

    private void cancelOrder(final JSONObject dataObject) {
        ConfirmDialog alertDialog = new ConfirmDialog(PendingPayOrderDetailActivity.this, ConfirmDialog.ConfirmDialogType.ConfirmDialogType_Warning);
        alertDialog.setMsg("是否确认取消该订单?")
                .setLeftButtonText("关闭")
                .setRightButtonText("确认")
                .setCallBack(new ConfirmDialog.CallBack() {
                    @Override
                    public void buttonClick(Dialog dialog, boolean leftClick) {
                        dialog.dismiss();
                        if (!leftClick) {
                            submitCancelOrder(dataObject);
                        }
                    }
                });
        alertDialog.show();
    }

    private void submitCancelOrder(final JSONObject dataObject) {

        String orderId = JsonUtils.getString(dataObject, "orderId", "");
        String url = AppConfig.Cancel_Order.replace(":orderId", orderId);

        final CustomLoadingDialog loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("订单取消中");
        loadingDialog.show();

        HttpRequest.post(url, null, null, JSONObject.class, new RequestCallback<JSONObject>() {

            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    AlertManager.showSuccessToast(PendingPayOrderDetailActivity.this, "订单取消成功", false);
                    EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_cancel_order);
                    notification.setValue(dataObject);
                    EventBus.getDefault().post(notification);

                    finish();
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(PendingPayOrderDetailActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(PendingPayOrderDetailActivity.this, "订单取消失败", false);

            }
        });
    }

    private void gotoPayCenterActivity() {
        Intent intent = new Intent(this, PayCenterActivity.class);
        intent.putExtra("orderDetail", jsonOrder.toString());
        startActivity(intent);
    }

    private boolean getSupportCity(JSONObject item) {
        if (!(JsonUtils.getJsonArray(item, "supportCityCodes", null) == null)) {
            JSONArray codeDatas = JsonUtils.getJsonArray(item, "supportCityCodes", null);
            for (int i = 0; i < codeDatas.length(); i++) {
                String code = JsonUtils.getStringItem(codeDatas, i, "");
                if (code.equals(addressCode)) {
                    btnPay.setBackgroundColor(getResources().getColor(R.color.Black_Gray));
                    btnPay.setEnabled(true);
                    return true;
                }
            }
        }
        btnPay.setBackgroundColor(getResources().getColor(R.color.gray_add));
        btnPay.setEnabled(false);
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v == back_btn) {
            finish();
        } else if (v == btnCancelOrder) {
            cancelOrder(jsonOrder);
        } else if (v == btnPay) {
            gotoPayCenterActivity();
        }
    }
}
