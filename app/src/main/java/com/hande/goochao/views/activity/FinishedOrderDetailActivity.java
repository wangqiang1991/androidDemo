package com.hande.goochao.views.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.ZhichiUtils;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.ClipboardUtils;
import com.hande.goochao.utils.CustomerServiceUtils;
import com.hande.goochao.utils.DateUtils;
import com.hande.goochao.utils.DoubleUtils;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.components.AnimationProgressBar;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Wangenmao on 2018/3/20.
 */

public class FinishedOrderDetailActivity extends Activity implements LoadFailView.OnReloadListener,View.OnClickListener  {

    private ImageOptions options = ImageOptionsUtil.getImageOptions(R.mipmap.loadpicture);

    private String jsonOrderStr;
    private JSONObject jsonOrder;

    @ViewInject(R.id.finish_order_detail_loading)
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
    @ViewInject(R.id.txtOrderNumber)
    private TextView txtOrderNumber;
    @ViewInject(R.id.btnCopy)
    private ImageView btnCopy;
    @ViewInject(R.id.txtPayDate)
    private TextView txtPayDate;
    @ViewInject(R.id.txtDeliverDate)
    private TextView txtDeliverDate;
    @ViewInject(R.id.txtExpressName)
    private TextView txtExpressName;
    @ViewInject(R.id.txtExpressNo)
    private TextView txtExpressNo;
    @ViewInject(R.id.txtReceiveDate)
    private TextView txtReceiveDate;
    @ViewInject(R.id.btnContactService)
    private Button btnContactService;
    @ViewInject(R.id.btnApplyAfterService)
    private Button btnApplyAfterService;
    @ViewInject(R.id.btnPreviewAfterSale)
    private Button btnPreviewAfterSale;
    @ViewInject(R.id.txtStatus)
    private TextView txtStatus;
    @ViewInject(R.id.remark_view)
    private View remarkView;
    @ViewInject(R.id.middle_line)
    private View line;
    @ViewInject(R.id.txtRemark)
    private TextView txtRemark;
    @ViewInject(R.id.goods_amount)
    private TextView txtAmount;
    @ViewInject(R.id.goods_item_price)
    private TextView txtMoney;
    @ViewInject(R.id.btnCopySecond)
    private ImageView btnCopy1;
    @ViewInject(R.id.gai_bi_ding_dan)
    private TextView gaiBi;

    private LayoutInflater inflater;

    private String goodsId;
    private double allMoney;
    private int allAmount;
    private JSONObject goodsObject;

    private GlideRequests glide;
    private String orderNumber;
    private String goodsTitle;
    private String goodsNumber;

    //字体加粗
    @ViewInject(R.id.ding_dan_bian_hao)
    private TextView dingDanBianHao;
    @ViewInject(R.id.xia_dan_shi_jian)
    private TextView xiaDanShiJian;
    @ViewInject(R.id.fa_huo_shi_jian)
    private TextView faHuoShiJian;
    @ViewInject(R.id.pei_song_fang_shi)
    private TextView peiSongFangShi;
    @ViewInject(R.id.yun_dan_bian_hao)
    private TextView yunDanBianHao;
    @ViewInject(R.id.shou_huo_shi_jian)
    private TextView shouHuoShiJian;
    @ViewInject(R.id.yun_fei)
    private TextView yunFei;
    @ViewInject(R.id.you_hui_quan)
    private TextView youHuiQuan;
    @ViewInject(R.id.ding_dan_zong_jia)
    private TextView dingDanZongJia;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finishedorderdetail);

        glide = GlideApp.with(this);
        WindowUtils.boldMethod(dingDanBianHao);
        WindowUtils.boldMethod(xiaDanShiJian);
        WindowUtils.boldMethod(faHuoShiJian);
        WindowUtils.boldMethod(peiSongFangShi);
        WindowUtils.boldMethod(yunDanBianHao);
        WindowUtils.boldMethod(shouHuoShiJian);
        WindowUtils.boldMethod(yunFei);
        WindowUtils.boldMethod(youHuiQuan);
        WindowUtils.boldMethod(dingDanZongJia);

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
        btnCopy.setOnClickListener(this);
        btnCopy1.setOnClickListener(this);
        btnContactService.setOnClickListener(this);
        btnApplyAfterService.setOnClickListener(this);
        btnPreviewAfterSale.setOnClickListener(this);

        int orderType = JsonUtils.getInt(jsonOrder, "orderType", 0);
        if (orderType == 4) {
            txtStatus.setText(txtStatus.getText().toString() + "(" + "换货" +  ")");
        }

        loadOrderDetail();

        WindowUtils.boldMethod(gaiBi);
        WindowUtils.boldMethod(txtStatus);
        EventBus.getDefault().register(this);
    }

    private void loadOrderDetail() {

        String orderNumber = JsonUtils.getString(jsonOrder, "orderNumber", "");
        String url = AppConfig.Order_Detail_By_OrderNumber.replace(":orderNumber", orderNumber);
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
                        jsonOrder.put("orderNumber", JsonUtils.getString(dataObject, "orderNumber", ""));
                        jsonOrder.put("goods", JsonUtils.getJsonObject(dataObject, "orderGoodsRelationDetail", null));
                        jsonOrder.put("payDate" ,JsonUtils.getLong(dataObject, "payDate", 0));
                        jsonOrder.put("message", JsonUtils.getString(dataObject, "message", ""));


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
                }
            }

            @Override
            public void onError(Throwable ex) {
                loadFailView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void bindSource() {

        String remark = JsonUtils.getString(jsonOrder, "message", "");
        if (remark.equals("")){
            remarkView.setVisibility(View.GONE);
            line.setVisibility(View.GONE);
        }else {
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

        JSONObject goodsObject = JsonUtils.getJsonObject(jsonOrder, "goods", null);

        double money = JsonUtils.getDouble(goodsObject, "money", 0);
        double expressMoney = JsonUtils.getDouble(goodsObject, "expressMoney", 0);
        double deductionMoney = JsonUtils.getDouble(goodsObject, "deductionMoney", 0);

        String expressMoneyStr = DoubleUtils.format(expressMoney).replace(".00","");
        txtExpressMoney.setText("¥  " + expressMoneyStr);
        WindowUtils.boldMethod(txtExpressMoney);

        String saleMoneyStr = DoubleUtils.format(deductionMoney).replace(".00", "");
        txtSaleMoney.setText("-  " + saleMoneyStr);
        WindowUtils.boldMethod(txtSaleMoney);

        double orderMoney = money + expressMoney;
        String orderMoneyStr = DoubleUtils.format(orderMoney).replace(".00", "");
        txtOrderMoney.setText("¥  " + orderMoneyStr);
        WindowUtils.boldMethod(txtOrderMoney);

        orderNumber = JsonUtils.getString(goodsObject, "orderNumber", "");
        txtOrderNumber.setText("" + orderNumber);
        WindowUtils.boldMethod(txtOrderNumber);

        long payDate = JsonUtils.getLong(jsonOrder, "payDate", 0);
        String payDateStr = DateUtils.timeStampToStr(payDate, "yyyy-MM-dd HH:mm");
        txtPayDate.setText("" + payDateStr);
        WindowUtils.boldMethod(txtPayDate);

        long deliverDate = JsonUtils.getLong(goodsObject, "deliveredDate", 0);
        String deliverDateStr = DateUtils.timeStampToStr(deliverDate, "yyyy-MM-dd HH:mm");
        txtDeliverDate.setText("" + deliverDateStr);
        WindowUtils.boldMethod(txtDeliverDate);

        String expressName = JsonUtils.getString(goodsObject, "expressName", "");
        txtExpressName.setText("" + expressName);
        WindowUtils.boldMethod(txtExpressName);

        String expressNumber = JsonUtils.getString(goodsObject, "expressNumber", "");
        txtExpressNo.setText("" + expressNumber);
        WindowUtils.boldMethod(txtExpressNo);

        long receiveDate = JsonUtils.getLong(goodsObject, "receiveDate", 0);
        String receiveDateStr = DateUtils.timeStampToStr(receiveDate, "yyyy-MM-dd HH:mm");
        txtReceiveDate.setText("" + receiveDateStr);
        WindowUtils.boldMethod(txtReceiveDate);

        txtAmount.setText("" + allAmount + " 件商品总价");
        WindowUtils.boldMethod(txtAmount);
        String moneyValue = DoubleUtils.format(allMoney).replace(".00", "");
        txtMoney.setText("¥  " + moneyValue);
        WindowUtils.boldMethod(txtMoney);

        int afterSaleStatus = JsonUtils.getInt(jsonOrder, "afterSaleStatus", 0);
        int afterSaleType = JsonUtils.getInt(jsonOrder, "afterSaleType", 0);
        int applyStatus = JsonUtils.getInt(jsonOrder, "applyStatus", 0);

        if (afterSaleStatus != 0) {
            btnApplyAfterService.setVisibility(View.GONE);
            btnPreviewAfterSale.setVisibility(View.VISIBLE);
            if (afterSaleType == 2) {
                if (applyStatus == 1) {
                    txtStatus.setText("已申请换货");
                } else if (applyStatus == 2) {
                    txtStatus.setText("商家拒绝换货");
                } else if (applyStatus == 3) {
                    txtStatus.setText("商家待收货");
                } else if (applyStatus == 4) {
                    txtStatus.setText("商家拒绝收货");
                } else if (applyStatus == 5) {
                    txtStatus.setText("商家已确认收货");
                } else if (applyStatus == 6) {
                    txtStatus.setText("客户待收货");
                } else if (applyStatus == 7) {
                    txtStatus.setText("换货完成");
                }
            } else if (afterSaleType == 3) {
                if (applyStatus == 1) {
                    txtStatus.setText("已申请退货");
                } else if (applyStatus == 2) {
                    txtStatus.setText("商家拒绝退货");
                } else if (applyStatus == 3) {
                    txtStatus.setText("商家待收货");
                } else if (applyStatus == 4) {
                    txtStatus.setText("商家拒绝退款");
                } else if (applyStatus == 5) {
                    txtStatus.setText("商家已确认收货");
                } else if (applyStatus == 6) {
                    txtStatus.setText("退款中");
                } else if (applyStatus == 7) {
                    txtStatus.setText("退款成功");
                } else if (applyStatus == 8) {
                    txtStatus.setText("退款失败");
                }
            }
        } else {
            txtStatus.setText("如有疑问请联系客服");
            btnApplyAfterService.setVisibility(View.VISIBLE);
            btnPreviewAfterSale.setVisibility(View.GONE);
        }

        int orderType = JsonUtils.getInt(jsonOrder, "orderType", 0);
        if (orderType == 4) {
            txtStatus.setText(txtStatus.getText().toString() + "(" + "换货" +  ")");
        }
    }

    private void initOrderGoods() {

        JSONObject goods = JsonUtils.getJsonObject(jsonOrder, "goods", null);
        JSONArray orderGoodsRelations = new JSONArray();
        orderGoodsRelations.put(goods);

        for (int i = 0; i < orderGoodsRelations.length(); i++) {
            goodsObject = JsonUtils.getJsonItem(orderGoodsRelations, i, null);
            final View goodsItemView = inflater.inflate(R.layout.order_detail_goods_item, null);
            goodsItemView.setTag(goodsObject);

            ImageView goodsCover = goodsItemView.findViewById(R.id.goodsCover);
            String cover = JsonUtils.getString(goodsObject, "cover", "");
            ImageUtils.loadImage(glide, ImageUtils.resize(cover,500,500), goodsCover, -1);

            TextView txtGoodsTitle = goodsItemView.findViewById(R.id.txtGoodsTitle);
            goodsTitle = JsonUtils.getString(goodsObject, "title", "");
            txtGoodsTitle.setText(JsonUtils.getString(goodsObject, "title", ""));
            WindowUtils.boldMethod(txtGoodsTitle);

            goodsNumber = JsonUtils.getString(goodsObject, "goodsNumber", "");

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

            allAmount = amount;
            double single = JsonUtils.getDouble(goods,"stylePrice",0);
            allMoney = allAmount * single;

            goodsItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject newObject = (JSONObject) goodsItemView.getTag();
                    goodsId = JsonUtils.getString(newObject ,"goodsId","");
                    Intent intent = new Intent(FinishedOrderDetailActivity.this, NewProductInformationActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("goodsId",goodsId);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
            layOrderGoods.addView(goodsItemView);
        }
    }


    private void copyOrderNumber() {
        JSONObject goodsObject = JsonUtils.getJsonObject(jsonOrder, "goods", null);
        String orderNumber = JsonUtils.getString(goodsObject, "orderNumber", "");
        ClipboardUtils.setClipboardText(orderNumber, this);
    }

    private void copyExpressNumber() {
        JSONObject goodsObject = JsonUtils.getJsonObject(jsonOrder, "goods", null);
        String expressNumber = JsonUtils.getString(goodsObject, "expressNumber", "");
        ClipboardUtils.setClipboardText(expressNumber, this);
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

    private void showAfterSaleInfo(JSONObject orderObject) {
        String relationId = JsonUtils.getString(orderObject, "relationId", "");
        Intent intent = new Intent(this, AfterSaleDetailActivity.class);
        intent.putExtra("relationId", relationId);
        startActivity(intent);
    }

    private void btnApplyAfterSaleClick() {
        JSONObject goodsObject = JsonUtils.getJsonObject(jsonOrder, "goods", null);
        String orderNumber = JsonUtils.getString(goodsObject, "orderNumber", "");
        Intent intent = new Intent(this, ApplyAfterSaleActivity.class);
        intent.putExtra("title", "申请售后");
        intent.putExtra("orderNumber", orderNumber);
        startActivity(intent);
    }

    @Override
    public void finish() {
        EventBus.getDefault().unregister(this);
        super.finish();
    }

    @Subscribe
    public void onEvent(EventBusNotification notification) {
        if (notification.getKey().equals(EventBusNotification.event_bus_refresh_order_list)) {
            finish();
        }
    }

    private void gotoCustomService() {
//        JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(this);
//        CustomerServiceUtils.CustomerServiceModel model = new CustomerServiceUtils.CustomerServiceModel();
//        model.setUname(JsonUtils.getString(loginResult, "nickName", ""));
//        model.setRealname(JsonUtils.getString(loginResult, "nickName", ""));
//        model.setFace(JsonUtils.getString(loginResult, "head", ""));
//        JSONObject goodsObject = JsonUtils.getJsonObject(jsonOrder, "goods", null);
//        String orderNumber = JsonUtils.getString(goodsObject, "orderNumber", "");
//        model.setOrderNumber(orderNumber);
//        CustomerServiceUtils.goService(model, this);

        Map<String,String> customerFields = new HashMap<>();
        customerFields.put("customField2",goodsTitle); // 商品名称
        customerFields.put("customField4",goodsNumber); // 商品编号
        customerFields.put("customField1",orderNumber); // 订单号
        customerFields.put("customField3",""); // 售后工单编号
        ZhichiUtils.startZhichi(this , customerFields);
    }

    @Override
    public void onClick(View v) {
        if (v == back_btn) {
            finish();
        } else if (v == btnCopy) {
            copyOrderNumber();
        } else if (v == btnCopy1) {
            copyExpressNumber();
        } else if (v == btnPreviewAfterSale) {
            showAfterSaleInfo(jsonOrder);
        } else if (v == btnContactService) {
            gotoCustomService();
        } else if (v == btnApplyAfterService){
            btnApplyAfterSaleClick();
        }
    }
}
