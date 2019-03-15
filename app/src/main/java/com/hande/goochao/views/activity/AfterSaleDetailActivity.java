package com.hande.goochao.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.ZhichiUtils;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.views.gallery.GalleryActivity;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.ClipboardUtils;
import com.hande.goochao.utils.CustomerServiceUtils;
import com.hande.goochao.utils.DoubleUtils;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;

import org.json.JSONObject;
import org.w3c.dom.Text;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Wangenmao on 2018/3/21.
 */

public class AfterSaleDetailActivity extends ToolBarActivity implements LoadFailView.OnReloadListener,View.OnClickListener {

    private String relationId;

    @ViewInject(R.id.after_sale_detail_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.mScrollView)
    private ScrollView mScrollView;
    @ViewInject(R.id.txtOrderNumber)
    private TextView txtOrderNumber;
    @ViewInject(R.id.btnContactService)
    private Button btnContactService;
    @ViewInject(R.id.nodeContainer)
    private LinearLayout nodeContainer;
    @ViewInject(R.id.goodsCover)
    private ImageView goodsCover;
    @ViewInject(R.id.txtGoodsTitle)
    private TextView txtGoodsTitle;
    @ViewInject(R.id.txtGoodsStyle)
    private TextView txtGoodsStyle;
    @ViewInject(R.id.txtGoodsSub)
    private TextView txtGoodsSub;
    @ViewInject(R.id.txtReason)
    private TextView txtReason;
    @ViewInject(R.id.txtReasonValue)
    private TextView txtReasonValue;
    @ViewInject(R.id.moneyContainer)
    private View moneyContainer;
    @ViewInject(R.id.txtMoneyValue)
    private TextView txtMoneyValue;
    @ViewInject(R.id.txtProblemValue)
    private TextView txtProblemValue;
    @ViewInject(R.id.rejectContainer)
    private View rejectContainer;
    @ViewInject(R.id.txtRejectValue)
    private TextView txtRejectValue;
    @ViewInject(R.id.txtGoodsPrice)
    private TextView txtGoodsPrice;
    @ViewInject(R.id.txtModeValue)
    private TextView txtModeValue;
    @ViewInject(R.id.txtApplyCountValue)
    private TextView txtApplyCountValue;
    @ViewInject(R.id.image_layout)
    private LinearLayout imageLayout;
    @ViewInject(R.id.returnCover1)
    private ImageView returnCover1;
    @ViewInject(R.id.returnCover2)
    private ImageView returnCover2;
    @ViewInject(R.id.returnCover3)
    private ImageView returnCover3;
    @ViewInject(R.id.btnCopy)
    private ImageView btnCopy;
    @ViewInject(R.id.txtGoodsAmount)
    private TextView txtGoodsAmount;

    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;


    private JSONObject resultObject;
    private List<NodeEntity> nodeArray = new ArrayList<>();

    //为加粗设置无关变量
    @ViewInject(R.id.txtMode)
    private TextView shouHouLeiXing;
    @ViewInject(R.id.txtMoney)
    private TextView tuiKuanJinE;
    @ViewInject(R.id.txtProblem)
    private TextView wenTiMiaoShu;
    @ViewInject(R.id.txtReject)
    private TextView juJueYuanYin;
    @ViewInject(R.id.txtImage)
    private TextView wenTiTuPian;
    @ViewInject(R.id.txtApplyCount)
    private TextView shenQingShuLiang;


    private GlideRequests glide;
    private String goodTitle;
    private String goodNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aftersale_detail);
        glide = GlideApp.with(this);

        setTitle("售后详情");
        relationId = getIntent().getStringExtra("relationId");

        loadFailView.setOnReloadListener(this);
        btnContactService.setOnClickListener(this);
        btnCopy.setOnClickListener(this);

        loadAfterSaleDetail();
    }

    private void loadAfterSaleDetail() {
        String url = AppConfig.AfterSale_Info.replace(":relationId", relationId);
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
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void bindSource() {
        String orderNumber = JsonUtils.getString(resultObject, "orderNumber", "");
        txtOrderNumber.setText("订单编号:  " + orderNumber);
        WindowUtils.boldMethod(txtOrderNumber);
        initNodes();
        bindNodeViews();

        JSONObject goodsObject = JsonUtils.getJsonObject(resultObject, "detail", null);
        String cover = JsonUtils.getString(goodsObject, "cover", "");
        ImageUtils.loadImage(glide, ImageUtils.resize(cover,500,500), goodsCover, R.mipmap.loadpicture);

        goodNumber = JsonUtils.getString(goodsObject, "goodsNumber", "");

        goodTitle = JsonUtils.getString(goodsObject, "title", "");
        txtGoodsTitle.setText(goodTitle);
        WindowUtils.boldMethod(txtGoodsTitle);

        String styleName = JsonUtils.getString(goodsObject, "styleName", "");
        txtGoodsStyle.setText(styleName);
        WindowUtils.boldMethod(txtGoodsStyle);

        String subName = JsonUtils.getString(goodsObject, "subName", "");
        txtGoodsSub.setText(subName);
        WindowUtils.boldMethod(txtGoodsSub);

        String applyCount = JsonUtils.getString(resultObject,"applyCount","0");
        txtApplyCountValue.setText(applyCount);
        WindowUtils.boldMethod(txtApplyCountValue);

        double money = JsonUtils.getDouble(goodsObject, "money", 0);
        double expressMoney = JsonUtils.getDouble(goodsObject, "expressMoney", 0);
        String moneyStr = DoubleUtils.format(money).replace(".00", "");
        if(expressMoney == 0){
            txtGoodsPrice.setText("¥" + moneyStr + "(免运费)");
        }else {
            txtGoodsPrice.setText("¥" + moneyStr + "(含运费¥" + DoubleUtils.format(expressMoney) + ")");
        }
        WindowUtils.boldMethod(txtGoodsPrice);

        String amount = JsonUtils.getString(goodsObject, "amount", "");
        txtGoodsAmount.setText("x  " + amount);
        WindowUtils.boldMethod(txtGoodsAmount);

        int afterSaleType = JsonUtils.getInt(goodsObject, "afterSaleType", 0);
        if (afterSaleType == 1) {
            txtModeValue.setText("退款");
            txtReason.setText("退款原因");
        } else if (afterSaleType == 2) {
            txtModeValue.setText("换货");
            txtReason.setText("换货原因");
        } else if (afterSaleType == 3) {
            txtModeValue.setText("退货退款");
            txtReason.setText("退货原因");
        }
        WindowUtils.boldMethod(txtModeValue);

        String refundReason = JsonUtils.getString(resultObject, "refundReason", "--");
        txtReasonValue.setText(refundReason);
        WindowUtils.boldMethod(txtReasonValue);

        if (afterSaleType == 1 || afterSaleType == 3) {
            moneyContainer.setVisibility(View.VISIBLE);

            double refundMoney = JsonUtils.getDouble(resultObject, "refundMoney", 0);
            String refundMoneyStr = DoubleUtils.format(refundMoney).replace(".00", "");
            txtMoneyValue.setText("¥" + refundMoneyStr);
            WindowUtils.boldMethod(txtMoneyValue);
        } else {
            moneyContainer.setVisibility(View.GONE);
        }

        String remark = JsonUtils.getString(resultObject, "remark", "无");
        txtProblemValue.setText(remark);
        WindowUtils.boldMethod(txtProblemValue);

        String rejectReason = JsonUtils.getString(resultObject, "rejectReason", "");
        if (!TextUtils.isEmpty(rejectReason)) {
            rejectContainer.setVisibility(View.VISIBLE);
            txtRejectValue.setText(rejectReason);
            WindowUtils.boldMethod(txtRejectValue);
        } else {
            rejectContainer.setVisibility(View.GONE);
        }

        String images = JsonUtils.getString(resultObject, "images", "");
        if (!TextUtils.isEmpty(images)) {
            imageLayout.setVisibility(View.VISIBLE);
            String[] imageArry =  images.split(",");
            if (imageArry.length == 1 ){
                returnCover2.setVisibility(View.GONE);
                returnCover3.setVisibility(View.GONE);
            }else if (imageArry.length == 2) {
                returnCover3.setVisibility(View.GONE);
            }

            for (int i = 0; i < imageArry.length; i++) {
                if ( i == 0){
                    ImageUtils.loadImage(glide, imageArry[i], returnCover1, -1);
                    returnCover1.setOnClickListener(new ImageCoverOnClickListener(imageArry[i],imageArry));
                }
                else if ( i == 1){
                    ImageUtils.loadImage(glide, imageArry[i], returnCover2, -1);
                    returnCover2.setOnClickListener(new ImageCoverOnClickListener(imageArry[i],imageArry));
                }
                else if ( i == 2){
                    ImageUtils.loadImage(glide, imageArry[i], returnCover3, -1);
                    returnCover3.setOnClickListener(new ImageCoverOnClickListener(imageArry[i],imageArry));
                }
            }

        } else {
            imageLayout.setVisibility(View.GONE);
        }

        WindowUtils.boldMethod(shouHouLeiXing);
        WindowUtils.boldMethod(txtReason);
        WindowUtils.boldMethod(tuiKuanJinE);
        WindowUtils.boldMethod(wenTiMiaoShu);
        WindowUtils.boldMethod(juJueYuanYin);
        WindowUtils.boldMethod(wenTiTuPian);
        WindowUtils.boldMethod(shenQingShuLiang);
    }

    class ImageCoverOnClickListener implements View.OnClickListener {
        private String currentSrc;
        private String[] imageArry;

        ImageCoverOnClickListener(String currentSrc, String[] imageArry ) {
            this.currentSrc = currentSrc;
            this.imageArry = imageArry;
        }

        @Override
        public void onClick(View view) {

            Intent intent = new Intent();
            intent.setClass(AfterSaleDetailActivity.this, GalleryActivity.class);
            intent.putExtra("isLocal",false );
            intent.putExtra("currentSrc",currentSrc );
            intent.putExtra("images", imageArry);
            AfterSaleDetailActivity.this.startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void bindNodeViews() {
        nodeContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < nodeArray.size(); i++) {
            NodeEntity nodeEntity = nodeArray.get(i);
            View nodeView = inflater.inflate(R.layout.after_sale_node_item, null);
            nodeContainer.addView(nodeView);

            View line = nodeView.findViewById(R.id.line);
            if (i == nodeArray.size() - 1) {
                line.setVisibility(View.INVISIBLE);
            } else {
                line.setVisibility(View.VISIBLE);
            }

            TextView txtNodeName = nodeView.findViewById(R.id.txtNodeName);
            TextView txtDate = nodeView.findViewById(R.id.txtDate);

            txtNodeName.setText(nodeEntity.getTitle());
            WindowUtils.boldMethod(txtNodeName);
            txtDate.setText(nodeEntity.getDate());
            WindowUtils.boldMethod(txtDate);
        }
    }


    private void initNodes() {
        nodeArray.clear();

        int workType = JsonUtils.getInt(resultObject, "workType", 0);
        int applyStatus = JsonUtils.getInt(resultObject, "applyStatus", 0);

        String format = "yyyy-MM-dd HH:mm:ss";

        if (workType == 1) {           //退款
            nodeArray.add(new NodeEntity("已申请退款", JsonUtils.getDateStr(resultObject, "createdAt", "--", format)));

            if (applyStatus == 2) {
                nodeArray.add(new NodeEntity("商家拒绝退款", JsonUtils.getDateStr(resultObject, "rejectDate", "--", format)));
                return;
            }

            if (applyStatus >= 3) {
                nodeArray.add(new NodeEntity("退款中", JsonUtils.getDateStr(resultObject, "refundingDate", "--", format)));
            }

            if (applyStatus == 4) {
                nodeArray.add(new NodeEntity("退款成功", JsonUtils.getDateStr(resultObject, "refundDate", "--", format)));
                return;
            }

            if (applyStatus == 5) {
                nodeArray.add(new NodeEntity("退款失败", JsonUtils.getDateStr(resultObject, "refundFailureDate", "--", format)));
            }

        } else if (workType == 2) {    //换货
            nodeArray.add(new NodeEntity("已申请换货", JsonUtils.getDateStr(resultObject, "createdAt", "--", format)));

            if (applyStatus == 2) {
                nodeArray.add(new NodeEntity("商家拒绝换货", JsonUtils.getDateStr(resultObject, "rejectDate", "--", format)));
                return;
            }
            if (applyStatus >= 3) {
                nodeArray.add(new NodeEntity("商家待收货", JsonUtils.getDateStr(resultObject, "deliveredDate", "--", format)));
            }
            if (applyStatus == 4) {
                nodeArray.add(new NodeEntity("商家拒绝换货", JsonUtils.getDateStr(resultObject, "deliveryRejectDate", "--", format)));
                return;
            }

            if (applyStatus >= 5) {
                nodeArray.add(new NodeEntity("商家已确认收货", JsonUtils.getDateStr(resultObject, "deliveryCompleteDate", "--", format)));
            }

            if (applyStatus >= 6) {
                nodeArray.add(new NodeEntity("客户待收货", JsonUtils.getDateStr(resultObject, "clientDeliveredDate", "--", format)));
            }

            if (applyStatus == 7) {
                nodeArray.add(new NodeEntity("客户已确认收货", JsonUtils.getDateStr(resultObject, "clientDeliveryCompleteDate", "--", format)));
            }

        } else if (workType == 3) {    //退货退款
            nodeArray.add(new NodeEntity("已申请退货", JsonUtils.getDateStr(resultObject, "createdAt", "--", format)));
            if (applyStatus == 2) {
                nodeArray.add(new NodeEntity("商家拒绝退货", JsonUtils.getDateStr(resultObject, "rejectDate", "--", format)));
                return;
            }
            if (applyStatus >= 3) {
                nodeArray.add(new NodeEntity("商家待收货", JsonUtils.getDateStr(resultObject, "deliveredDate", "--", format)));
            }
            if (applyStatus == 4) {
                nodeArray.add(new NodeEntity("商家拒绝退款", JsonUtils.getDateStr(resultObject, "refundRejectDate", "--", format)));
                return;
            }
            if (applyStatus >= 5) {
                nodeArray.add(new NodeEntity("商家已确认收货", JsonUtils.getDateStr(resultObject, "deliveryCompleteDate", "--", format)));
            }

            if (applyStatus >= 6) {
                nodeArray.add(new NodeEntity("退款中", JsonUtils.getDateStr(resultObject, "refundingDate", "--", format)));
            }

            if (applyStatus == 7) {
                nodeArray.add(new NodeEntity("退款成功", JsonUtils.getDateStr(resultObject, "refundDate", "--", format)));
                return;
            }

            if (applyStatus == 8) {
                nodeArray.add(new NodeEntity("退款失败", JsonUtils.getDateStr(resultObject, "refundFailureDate", "--", format)));
            }
        }


    }

    @Override
    public void onReload() {
        loadAfterSaleDetail();
    }

    private void gotoCustomService() {
//        JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(this);
//        CustomerServiceUtils.CustomerServiceModel model = new CustomerServiceUtils.CustomerServiceModel();
//        model.setUname(JsonUtils.getString(loginResult, "nickName", ""));
//        model.setRealname(JsonUtils.getString(loginResult, "nickName", ""));
//        model.setFace(JsonUtils.getString(loginResult, "head", ""));
//        model.setOrderNumber(JsonUtils.getString(resultObject, "orderNumber", ""));
//        model.setAfterWorkNumber(JsonUtils.getString(resultObject, "workNumber", ""));
//        CustomerServiceUtils.goService(model, this);

        Map<String,String> customerFields = new HashMap<>();
        customerFields.put("customField2",goodTitle); // 商品名称
        customerFields.put("customField4",goodNumber); // 商品编号
        customerFields.put("customField1",JsonUtils.getString(resultObject, "orderNumber", null)); // 订单号
        customerFields.put("customField3",JsonUtils.getString(resultObject, "workNumber", null)); // 售后工单编号
        ZhichiUtils.startZhichi(this , customerFields);
    }

    @Override
    public void onClick(View v) {
        if (v == btnContactService) {
            gotoCustomService();
        } else if (v == btnCopy) {
            copyOrderNumber();
        }
    }

    private void copyOrderNumber() {
        String orderNumber = JsonUtils.getString(resultObject, "orderNumber", "");
        ClipboardUtils.setClipboardText(orderNumber, this);
    }

    class NodeEntity {
        String title;
        String date;

        public NodeEntity(String title, String date) {
            this.title = title;
            this.date = date;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }
}
