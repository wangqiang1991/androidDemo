package com.hande.goochao.views.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.Params;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.NumberUtils;
import com.hande.goochao.utils.PriceUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.PayCloseActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;

import org.apache.commons.lang.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.HashMap;
import java.util.Map;


public class ConfirmOrderActivity extends PayCloseActivity implements LoadFailView.OnReloadListener, View.OnClickListener {

    /**
     * 地址选择
     */
    public static final int ADDRESS_REQUEST_CODE = 1;

    /**
     * 优惠券选择
     */
    public static final int COUPON_REQUEST_CODE = 2;

    @ViewInject(R.id.pay_text)
    private TextView payText;
    @ViewInject(R.id.goods_list_view)
    private ListView goodsListView;
    @ViewInject(R.id.order_confirm_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.receiver_name)
    private TextView receiverNameText;
    @ViewInject(R.id.receiver_phone)
    private TextView receiverPhoneText;
    @ViewInject(R.id.receiver_address)
    private TextView receiverAddressText;
    @ViewInject(R.id.express_price_text)
    private TextView expressPriceText;
    @ViewInject(R.id.message_edit)
    private EditText messageEdit;
    @ViewInject(R.id.coupon_text)
    private TextView couponText;
    @ViewInject(R.id.address_layout)
    private View addressLayout;
    @ViewInject(R.id.coupon_layout)
    private View couponLayout;
    @ViewInject(R.id.confirm_btn)
    private Button confirmBtn;
    @ViewInject(R.id.have_address_show)
    private View haveAddressView;
    @ViewInject(R.id.no_address_show)
    private View noAddressView;
//    @ViewInject(R.id.reduce_money)
//    private TextView reduceMoneyView;
    @ViewInject(R.id.discount_text)
    private TextView discountText;

    private CustomLoadingDialog loadingDialog;

    private GoodsListViewAdapter adapter;

    // intent params
    private String orderId;
    // load return
    private JSONObject orderDetail;
    private JSONArray orderGoodsRelations;

    private double oldMoney;
    private double oldGoodsMoney;
    private double newMoney;
    private double middleMoney;
    private double freightMoney;
    private double trueMoney; // 最初始的订单总价格

    private String couponId;
    private String addressCode;

    //第一次进入时，判断是否有默认地址 给出对应界面
    private boolean firstHaveDefault;

    //为true表示有至少一件商品不支持该地区配送
    private boolean oneCant;

    //是否选择了优惠券
    private boolean haveChooseCoupon;

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);
        setTitle("订单确认");
        glide = GlideApp.with(this);
        // 获取参数
        orderId = getIntent().getStringExtra("orderId");
        if (TextUtils.isEmpty(orderId)) {
            AlertManager.showErrorToast(this, "参数无效", false);
            return;
        }

        initViews();
        loadDetail();
    }

    /**
     * 初始化view
     */
    private void initViews() {
        loadingDialog = new CustomLoadingDialog(this);
        adapter = new GoodsListViewAdapter();
        goodsListView.setAdapter(adapter);

        loadFailView.setOnReloadListener(this);
        addressLayout.setOnClickListener(this);
        couponLayout.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
    }

    private void showOrderInfo() {
        refreshAddressView();
        refreshGoodsListView();
        refreshExpressView();
        refreshCouponView();
        refreshPayView();
    }

    private void refreshPayView() {
        double settlementMoney = JsonUtils.getDouble(orderDetail, "settlementMoney", 0);
        double trueMoney = JsonUtils.getDouble(orderDetail, "totalMoney", 0);
        if ((settlementMoney + freightMoney) < 0) {
            payText.setText(Html.fromHtml("¥" + 0 + ""));
        } else {
            double addValue = settlementMoney + freightMoney;
            double lookValue = NumberUtils.decimalDouble(addValue);
            payText.setText(Html.fromHtml("¥" + PriceUtils.beautify(lookValue) + ""));
        }
        double discount = trueMoney - settlementMoney;
        double discountValue = NumberUtils.decimalDouble(discount);
        discountText.setText(Html.fromHtml("已优惠:¥" + PriceUtils.beautify(discountValue) + ""));
        WindowUtils.boldMethod(payText);
        WindowUtils.boldMethod(discountText);
    }

    private void refreshCouponView() {
        if (!JsonUtils.getBoolean(orderDetail, "haveCoupon", false) && !haveChooseCoupon) {
            couponText.setText("暂无可用");
            couponText.setTextColor(getResources().getColor(R.color.new_product_detail));
            adapter.notifyDataSetChanged();
            return;
        }
        if (JsonUtils.isEmpty(orderDetail, "couponId")) {
            return;
        }

        couponText.setText(JsonUtils.getString(orderDetail, "couponVo.name", ""));
        couponText.setTextColor(getResources().getColor(R.color.black_red));
        WindowUtils.boldMethod(couponText);
        adapter.notifyDataSetChanged();
    }

    private void refreshExpressView() {
        float expressMoney = (float) JsonUtils.getDouble(orderDetail, "expressMoney", 0);
        String message = JsonUtils.getString(orderDetail, "message", "");
        expressPriceText.setText(PriceUtils.beautify("¥" + expressMoney));
        messageEdit.setText(message);
    }

    private void refreshGoodsListView() {
        orderGoodsRelations = JsonUtils.getJsonArray(orderDetail, "orderGoodsRelations", null);
        adapter.notifyDataSetChanged();
    }

    private void refreshAddressView() {
        if (JsonUtils.getString(orderDetail, "addressId", "").equals("")) {
            noAddressView.setVisibility(View.VISIBLE);
            haveAddressView.setVisibility(View.GONE);
        } else {
            noAddressView.setVisibility(View.GONE);
            haveAddressView.setVisibility(View.VISIBLE);
            String reallyName = JsonUtils.getString(orderDetail, "reallyName", "");
            String phone = JsonUtils.getString(orderDetail, "phone", "");
            String address = JsonUtils.getString(orderDetail, "address", "");
            addressCode = JsonUtils.getString(orderDetail, "addressCityCode", "");
            receiverNameText.setText(reallyName);
            receiverPhoneText.setText(phone);
            receiverAddressText.setText(address);
            WindowUtils.boldMethod(receiverNameText);
            WindowUtils.boldMethod(receiverPhoneText);
            WindowUtils.boldMethod(receiverAddressText);
        }
    }

    private void showLoadFail() {
        loadFailView.setVisibility(View.VISIBLE);
    }

    private void hideLoadFail() {
        loadFailView.setVisibility(View.GONE);
    }

    @Override
    public void onReload() {
        hideLoadFail();
        loadDetail();
    }

    @Override
    public void onClick(View v) {
        if (v == addressLayout) {
            Intent intent = new Intent(this, AddressActivity.class);
            intent.putExtra("selectMode", true);
            startActivityForResult(intent, ADDRESS_REQUEST_CODE);
        } else if (v == couponLayout) {
            Intent intent = new Intent(this, OrderCouponActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("couponId", couponId);
            startActivityForResult(intent, COUPON_REQUEST_CODE);
        } else if (v == confirmBtn) {
            if (firstHaveDefault) {
                AlertManager.showErrorToast(this, "请选择收货人", false);
            } else {
                confirmOrder();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADDRESS_REQUEST_CODE && resultCode == RESULT_OK) {
            // 选择了地址
            firstHaveDefault = false;
            oneCant = false;
            String selectedItemJson = data.getStringExtra("selectedItem");
            JSONObject selectedItem = JsonUtils.newJsonObject(selectedItemJson, null);
            String addressId = JsonUtils.getString(selectedItem, "addressId", null);
            String province = JsonUtils.getString(selectedItem, "province", "");
            String city = JsonUtils.getString(selectedItem, "city", "");
            String county = JsonUtils.getString(selectedItem, "county", "");
            String address = province + city + county + JsonUtils.getString(selectedItem, "address", "");
            String phone = JsonUtils.getString(selectedItem, "phone", null);
            String reallyName = JsonUtils.getString(selectedItem, "reallyName", null);
            String addressNewCode = JsonUtils.getString(selectedItem, "cityCode", "");
            JsonUtils.put(orderDetail, "addressId", addressId);
            JsonUtils.put(orderDetail, "address", address);
            JsonUtils.put(orderDetail, "phone", phone);
            JsonUtils.put(orderDetail, "reallyName", reallyName);
            JsonUtils.put(orderDetail, "addressCityCode", addressNewCode);
            loadFreight(addressId);
            refreshAddressView();
            adapter.notifyDataSetChanged();
        } else if (requestCode == COUPON_REQUEST_CODE && resultCode == RESULT_OK) {
            String selectedItemJson = data.getStringExtra("selectedItem");
            if (!selectedItemJson.equals("")){
                haveChooseCoupon = true;
                JSONObject selectedItem = JsonUtils.newJsonObject(selectedItemJson, null);
                couponId = JsonUtils.getString(selectedItem, "couponId", null);
                JsonUtils.put(orderDetail, "couponId", couponId);
                JsonUtils.put(orderDetail, "couponVo", selectedItem);
                loadMoney(couponId);
            }else {
                couponId = null;
                haveChooseCoupon = false;
                JsonUtils.put(orderDetail, "couponId", couponId);
                loadMoney(couponId);
            }
            refreshCouponView();
        }
    }

    private void goPayCenterPage() {
        Intent intent = new Intent(this, PayCenterActivity.class);
        intent.putExtra("orderDetail", orderDetail.toString());
        startActivity(intent);
    }

    private void loadFreight(String addressId) {
        loadingDialog.setLoadingText("加载中");
        loadingDialog.show();
        Map<String, String> params = new HashMap<>();
        params.put("addressId", addressId);

        HttpRequest.get(AppConfig.FREIGHT_GET + orderId, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONObject freightObject = JsonUtils.getJsonObject(response, "data", null);
                    freightMoney = JsonUtils.getDouble(freightObject, "expressMoney", 0);
                    JsonUtils.put(orderDetail, "expressMoney", freightMoney);
                    double lookExpress = NumberUtils.decimalDouble(freightMoney);
                    expressPriceText.setText(PriceUtils.beautify("¥" + lookExpress));
                    oldMoney = oldGoodsMoney + freightMoney;
                    refreshPayView();
//                    refreshOldPay();
                } else {
                    showLoadFail();
                }
            }

            @Override
            public void onError(Throwable ex) {
                showLoadFail();
                AppLog.e("err", ex);
                AlertManager.showErrorToast(ConfirmOrderActivity.this, "服务器繁忙，请稍后重试", false);
            }
        });
    }

    /**
     * 加载订单详情
     */
    private void loadDetail() {
        loadingView.setVisibility(View.VISIBLE);
        HttpRequest.get(RestfulUrl.build(AppConfig.ORDER_DETAIL, ":orderId", orderId), null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    orderDetail = JsonUtils.getJsonObject(response, "data", null);
                    firstHaveDefault = JsonUtils.getString(orderDetail, "addressId", "").equals(""); // true没有默认地址，false有
                    freightMoney = JsonUtils.getDouble(orderDetail, "expressMoney", 0);
                    oldMoney = JsonUtils.getDouble(orderDetail, "settlementMoney", 0) +
                            JsonUtils.getDouble(orderDetail, "expressMoney", 0);
                    oldGoodsMoney = JsonUtils.getDouble(orderDetail, "settlementMoney", 0);
                    showOrderInfo();
                } else {
                    showLoadFail();
                }
            }

            @Override
            public void onError(Throwable ex) {
                showLoadFail();
                AppLog.e("err", ex);
                AlertManager.showErrorToast(ConfirmOrderActivity.this, "服务器繁忙，请稍后重试", false);
            }
        });
    }

    private void loadMoney(String couponId) {
        loadingDialog.setLoadingText("加载中");
        loadingDialog.show();
        String url = RestfulUrl.build(AppConfig.COUPON_MONEY, ":orderId", orderId);
        Map<String, String> params = Params.buildForStr("couponId", couponId);
        HttpRequest.get(url, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    double settlementMoney = JsonUtils.getDouble(response, "data.settlementMoney", 0);
                    JsonUtils.put(orderDetail, "settlementMoney", settlementMoney);
                    newMoney = settlementMoney;
                    refreshPayView();
                } else {
                    AlertManager.showErrorToast(ConfirmOrderActivity.this, JsonUtils.getResponseMessage(response), false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(ConfirmOrderActivity.this, "服务器繁忙，请稍后重试", false);
                AppLog.e("err", ex);
            }
        });
    }

    private void confirmOrder() {
        String addressId = JsonUtils.getString(orderDetail, "addressId", null);
        String couponId = JsonUtils.getString(orderDetail, "couponId", null);
        String message = messageEdit.getText().toString().trim();
        if (StringUtils.isEmpty(addressId)) {
            AlertManager.showErrorToast(this, "请选择收货人", false);
            return;
        }

        Map<String, Object> params = Params.buildForObj(
                "orderId", orderId,
                "addressId", addressId,
                "couponId", couponId,
                "message", message);

        loadingDialog.setLoadingText("提交订单");
        loadingDialog.show();

        HttpRequest.postJson(AppConfig.CONFIRM_ORDER, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_create_waitpay_order);
                    EventBus.getDefault().post(notification);
                    // 跳转到支付中心去
                    goPayCenterPage();
                } else {
                    AlertManager.showErrorToast(ConfirmOrderActivity.this, JsonUtils.getResponseMessage(response), false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(ConfirmOrderActivity.this, "服务器繁忙，请稍后重试", false);
                AppLog.e("err", ex);
            }
        });
    }

    class GoodsListViewAdapter extends BaseAdapter {

        class ViewHolder {
            private ImageView coverView;
            private TextView titleText;
            private TextView styleText;
            private TextView subText;
            private TextView priceText;
            private TextView amountText;
            private ImageView cantSent;
            private TextView productOldPrice;
            private TextView saleOutPrice;

            ViewHolder(View view) {
                coverView = view.findViewById(R.id.cover_view);
                titleText = view.findViewById(R.id.title_text);
                styleText = view.findViewById(R.id.style_text);
                subText = view.findViewById(R.id.sub_text);
                priceText = view.findViewById(R.id.price_text);
                amountText = view.findViewById(R.id.amount_text);
                cantSent = view.findViewById(R.id.cant_sent);
                productOldPrice = view.findViewById(R.id.old_price);
                saleOutPrice = view.findViewById(R.id.sale_out_price);
            }
        }

        @Override
        public int getCount() {
            return orderGoodsRelations == null ? 0 : orderGoodsRelations.length();
        }

        @Override
        public JSONObject getItem(int position) {
            return JsonUtils.getJsonItem(orderGoodsRelations, position, null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.layout_goods_list_view_item, parent, false);
                convertView.setTag(new ViewHolder(convertView));
            }

            JSONObject item = getItem(position);

            String cover = JsonUtils.getString(item, "cover", "");
            String title = JsonUtils.getString(item, "title", "");
            String style = JsonUtils.getString(item, "styleName", "");
            String sub = JsonUtils.getString(item, "subName", "");

            int amount = JsonUtils.getInt(item, "amount", 0);

            ViewHolder holder = (ViewHolder) convertView.getTag();

            if (haveChooseCoupon){
                holder.productOldPrice.setVisibility(View.GONE);
                holder.saleOutPrice.setVisibility(View.GONE);
                double price = JsonUtils.getDouble(item , "stylePrice" , 0);
                double priceValue = NumberUtils.decimalDouble(price);
                holder.priceText.setText("¥" + PriceUtils.beautify(priceValue));
            }else {
                //判断商品类型（满减 折扣 原价）
                String tagValue = JsonUtils.getString(item,"discountTag","");
                if (tagValue.equals("")){
                    holder.productOldPrice.setVisibility(View.GONE);
                    holder.saleOutPrice.setVisibility(View.GONE);

                    double price = JsonUtils.getDouble(item , "stylePrice" , 0);
                    double priceValue = NumberUtils.decimalDouble(price);
                    holder.priceText.setText("¥" + PriceUtils.beautify(priceValue));
                }                //原价 无活动
                else{
                    if (JsonUtils.getInt(item,"discountType",0) == 2){

                        holder.productOldPrice.setVisibility(View.VISIBLE);
                        holder.saleOutPrice.setVisibility(View.GONE);

                        double oldPrice = JsonUtils.getDouble(item , "stylePrice" , 0);
                        double oldPriceValue = NumberUtils.decimalDouble(oldPrice);
                        holder.productOldPrice.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG); //原价设置中划线
                        holder.productOldPrice.setText("¥" + PriceUtils.beautify(oldPriceValue));

                        double newPrice = JsonUtils.getDouble(item , "discountPrice" , 0);
                        double newPriceValue = NumberUtils.decimalDouble(newPrice);
                        holder.priceText.setText("¥" + PriceUtils.beautify(newPriceValue));
                    }             //折扣
                    else {
                        double price = JsonUtils.getDouble(item , "stylePrice" , 0);
                        double priceValue = NumberUtils.decimalDouble(price);
                        double newPrice = JsonUtils.getDouble(item , "discountPrice" , 0);
                        double newPriceValue = NumberUtils.decimalDouble(newPrice);
                        if (price == newPrice) {
                            holder.productOldPrice.setVisibility(View.GONE);
                            holder.saleOutPrice.setVisibility(View.VISIBLE);

                            holder.priceText.setText("¥" + PriceUtils.beautify(priceValue));

                            holder.saleOutPrice.setText(tagValue);
                        }else {
                            holder.productOldPrice.setVisibility(View.VISIBLE);
                            holder.saleOutPrice.setVisibility(View.GONE);

                            holder.productOldPrice.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG); //原价设置中划线
                            holder.productOldPrice.setText("¥" + PriceUtils.beautify(priceValue));

                            holder.priceText.setText("¥" + PriceUtils.beautify(newPriceValue));
                        }
                    }             //满减
                }
            }

            ImageUtils.loadImage(glide, cover, holder.coverView, -1);

            holder.titleText.setText(title);
            WindowUtils.boldMethod(holder.titleText);
            holder.styleText.setText(style);
            holder.subText.setText(sub);
            WindowUtils.boldMethod(holder.styleText);
            WindowUtils.boldMethod(holder.subText);

            WindowUtils.boldMethod(holder.priceText);
            holder.amountText.setText("x " + amount);

            if (!firstHaveDefault) {
                boolean support = getSupportCity(item);
                if (support) {
                    holder.cantSent.setVisibility(View.GONE);
                } else {
                    holder.cantSent.setVisibility(View.VISIBLE);
                }
                if (oneCant){
                    confirmBtn.setBackgroundColor(getResources().getColor(R.color.gray_add));
                    confirmBtn.setEnabled(false);
                }else {
                    confirmBtn.setBackgroundColor(getResources().getColor(R.color.Black_Gray));
                    confirmBtn.setEnabled(true);
                }
            }

            return convertView;
        }
    }

    private boolean getSupportCity(JSONObject item) {
        if (!(JsonUtils.getJsonArray(item, "supportCityCodes", null) == null)) {
            JSONArray codeDatas = JsonUtils.getJsonArray(item, "supportCityCodes", null);
            for (int i = 0; i < codeDatas.length(); i++) {
                String code = JsonUtils.getStringItem(codeDatas, i, "");
                if (code.equals(addressCode)) {
//                    confirmBtn.setBackgroundColor(getResources().getColor(R.color.Black_Gray));
//                    confirmBtn.setEnabled(true);
                    return true;
                }
            }
        }
//        confirmBtn.setBackgroundColor(getResources().getColor(R.color.gray_add));
//        confirmBtn.setEnabled(false);
        oneCant = true;
        return false;
    }
}
