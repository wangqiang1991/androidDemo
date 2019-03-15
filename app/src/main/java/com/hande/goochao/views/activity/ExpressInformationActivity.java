package com.hande.goochao.views.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.ClipboardUtils;
import com.hande.goochao.utils.DoubleUtils;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * Created by Wangenmao on 2018/3/22.
 */

public class ExpressInformationActivity extends ToolBarActivity implements LoadFailView.OnReloadListener {

    private String jsonOrderStr;
    private JSONObject jsonOrder;

    @ViewInject(R.id.express_information_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.mScrollView)
    private ScrollView mScrollView;
    @ViewInject(R.id.goodsCover)
    private ImageView imgCover;
    @ViewInject(R.id.txtExpressName)
    private TextView txtExpressName;
    @ViewInject(R.id.txtExpressNo)
    private TextView txtExpressNo;
    @ViewInject(R.id.layExpressContainer)
    private LinearLayout layExpressContainer;
    @ViewInject(R.id.btnCopy)
    private ImageView btnCopy;
    @ViewInject(R.id.txtGoodsTitle)
    private TextView txtGoodTitle;
    @ViewInject(R.id.txtGoodsStyle)
    private TextView txtStyleName;
    @ViewInject(R.id.txtGoodsSub)
    private TextView txtSubName;
    @ViewInject(R.id.txtGoodsPrice)
    private TextView txtGoodsPrice;
    @ViewInject(R.id.txtGoodsCount)
    private TextView txtGoodsCount;

    private JSONObject resultObject;
    private JSONArray expressArray;

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expressinfomation);
        setTitle("物流信息");
        glide = GlideApp.with(this);
        try {
            jsonOrderStr = getIntent().getStringExtra("jsonOrderStr");
            jsonOrder = new JSONObject(jsonOrderStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyExpressNumber();
            }
        });

        loadFailView.setOnReloadListener(this);
        loadOrderDetails();
    }

    private void copyExpressNumber() {
        JSONObject goodsObject = JsonUtils.getJsonObject(resultObject, "orderGoodsRelationDetail", null);
        String orderNumber = JsonUtils.getString(goodsObject, "expressNumber", "");
        ClipboardUtils.setClipboardText(orderNumber, this);
    }

    private void loadOrderDetails() {
        String orderNumber = JsonUtils.getString(jsonOrder, "orderNumber", "");
        String url = AppConfig.Order_Detail_By_OrderNumber.replace(":orderNumber", orderNumber);
        loadingView.setVisibility(View.VISIBLE);
        loadFailView.setVisibility(View.GONE);
        mScrollView.setVisibility(View.GONE);

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
                    mScrollView.setVisibility(View.VISIBLE);

                    resultObject = JsonUtils.getJsonObject(response, "data", null);
                    bindSource();
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                    mScrollView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Throwable ex) {
                loadFailView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadExpressDetail(String orderNumber) {
        String url = AppConfig.Express_Detail.replace(":orderNumber", orderNumber);
        HttpRequest.get(url, null, null, JSONObject.class, new RequestCallback<JSONObject>() {

            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    JSONObject object = JsonUtils.getJsonObject(response, "data", null);
                    expressArray = JsonUtils.getJsonArray(object, "data", null);
                    try {
                        bindExpressNodeViews();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    AlertManager.showErrorToast(ExpressInformationActivity.this, "加载物流信息失败", false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(ExpressInformationActivity.this, "加载物流信息失败", false);
            }
        });
    }

    private void bindExpressNodeViews() throws JSONException {
        layExpressContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < expressArray.length(); i++) {
            JSONObject nodeObject = expressArray.getJSONObject(i);
            View nodeView = inflater.inflate(R.layout.express_node_item, null);
            layExpressContainer.addView(nodeView);

            View line = nodeView.findViewById(R.id.line);
            if (i == expressArray.length() - 1) {
                line.setVisibility(View.INVISIBLE);
            } else {
                line.setVisibility(View.VISIBLE);
            }

            TextView txtNodeName = nodeView.findViewById(R.id.txtNodeName);
            TextView txtDate = nodeView.findViewById(R.id.txtDate);

            String context = JsonUtils.getString(nodeObject, "context", "");
            txtNodeName.setText(context);
            WindowUtils.boldMethod(txtNodeName);

            String ftime = JsonUtils.getString(nodeObject, "ftime", "");
            txtDate.setText(ftime);

        }
    }

    private void bindSource() {
        JSONObject goodsObject = JsonUtils.getJsonObject(resultObject, "orderGoodsRelationDetail", null);
        String cover = JsonUtils.getString(goodsObject, "cover", "");
        ImageUtils.loadImage(glide, cover, imgCover, R.mipmap.loadpicture);

        String goodsTitle = JsonUtils.getString(goodsObject, "title", "");
        txtGoodTitle.setText(goodsTitle);
        WindowUtils.boldMethod(txtGoodTitle);

        String expressName = JsonUtils.getString(goodsObject, "expressName", "");
        txtExpressName.setText(expressName);
        WindowUtils.boldMethod(txtExpressName);

        String styleName = JsonUtils.getString(goodsObject, "styleName", "");
        txtStyleName.setText(styleName);
        WindowUtils.boldMethod(txtStyleName);

        String subName = JsonUtils.getString(goodsObject, "subName", "");
        txtSubName.setText(subName);
        WindowUtils.boldMethod(txtSubName);

        double money = JsonUtils.getDouble(goodsObject, "money", 0);
        double expressMoney = JsonUtils.getDouble(goodsObject, "expressMoney", 0);
        String moneyStr = DoubleUtils.format(money + expressMoney).replace(".00", "");
        if(expressMoney == 0){
            txtGoodsPrice.setText("¥" + moneyStr + "(免运费)");
        }else {
            txtGoodsPrice.setText("¥" + moneyStr + "(含运费¥" + DoubleUtils.format(expressMoney) + ")");
        }
        WindowUtils.boldMethod(txtGoodsPrice);



        String goodsAmount = JsonUtils.getString(goodsObject, "amount", "");
        txtGoodsCount.setText("x  " + goodsAmount);
        WindowUtils.boldMethod(txtGoodsCount);

        String expressNumber = JsonUtils.getString(goodsObject, "expressNumber", "");
        txtExpressNo.setText("运单编号:  " + expressNumber);
        WindowUtils.boldMethod(txtExpressNo);

        String orderNumber = JsonUtils.getString(goodsObject, "orderNumber", "");
        loadExpressDetail(orderNumber);
    }

    @Override
    public void onReload() {
        loadOrderDetails();
    }
}
