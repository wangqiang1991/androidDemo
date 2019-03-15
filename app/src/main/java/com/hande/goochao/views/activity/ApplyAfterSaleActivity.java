package com.hande.goochao.views.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hande.goochao.BuildConfig;
import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.qiniu.UploadListener;
import com.hande.goochao.commons.qiniu.UploadService;
import com.hande.goochao.commons.views.gallery.GalleryActivity;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.config.AppConst;
import com.hande.goochao.session.AppSession;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.ClipboardUtils;
import com.hande.goochao.utils.DoubleUtils;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.PhoneUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * Created by Wangenmao on 2018/3/23.
 */

public class ApplyAfterSaleActivity extends ToolBarActivity implements LoadFailView.OnReloadListener, View.OnClickListener, UploadListener {

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.CAMERA"};
    private static final int REQUEST_CAMERA_CODE = 1;

    private String orderNumber;
    private String title;
    private String afterSaleType;
    private JSONObject resultObject;

    @ViewInject(R.id.apply_after_sale_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.mScrollView)
    private ScrollView mScrollView;
    @ViewInject(R.id.goodsCover)
    private ImageView goodsCover;
    @ViewInject(R.id.txtGoodsTitle)
    private TextView txtGoodsTitle;
    @ViewInject(R.id.txtGoodsStyle)
    private TextView txtGoodsStyle;
    @ViewInject(R.id.txtGoodsSub)
    private TextView txtGoodsSub;
    @ViewInject(R.id.txtMoney)
    private TextView txtMoney;
    @ViewInject(R.id.modeContainer)
    private View modeContainer;
    @ViewInject(R.id.grid_view)
    private GridView grid_view;
    @ViewInject(R.id.btnSubmit)
    private Button btnSubmit;
    @ViewInject(R.id.txtOrderNumberValue)
    private TextView txtOrderNumberValue;
    @ViewInject(R.id.btnCopy)
    private ImageView btnCopy;
    @ViewInject(R.id.txtModeValue)
    private TextView txtModeValue;
    @ViewInject(R.id.txtReasonValue)
    private TextView txtReasonValue;
    @ViewInject(R.id.txtPhoneValue)
    private TextView txtPhoneValue;
    @ViewInject(R.id.txtProblemValue)
    private TextView txtProblemValue;
    @ViewInject(R.id.reasonContainer)
    private View reasonContainer;

    @ViewInject(R.id.adjust_view)
    private View adjustView;
    @ViewInject(R.id.txt_count)
    private TextView countView;

    @ViewInject(R.id.after_minus_goods)
    private TextView miuView;
    @ViewInject(R.id.after_goods_number)
    private TextView askCount;
    @ViewInject(R.id.after_add_goods)
    private TextView addView;
    private int maxCount;
    private int applyCount = 1;
    private boolean addCanClick = true;
    private boolean miuCanClick = true;

    private LayoutInflater inflater;

    private CustomLoadingDialog loadingDialog;
    private ImageAdapter adapter = new ImageAdapter();
    private ArrayList<String> mSelectPath = new ArrayList<>();
    private List<String> mArray;
    private String[] mArrayImage;
    //上传返回的服务器图片地址
    private ArrayList<String> uploadBackUrls = new ArrayList<String>();

    private UploadService uploadService = UploadService.getService("ApplyAfterSaleActivity");
    private boolean isUploading = false;

    private static final int SELECT_IMAGE_CODE = 1;

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_aftersale);
        hideLine();
        glide = GlideApp.with(this);
        orderNumber = getIntent().getStringExtra("orderNumber");

        title = getIntent().getStringExtra("title");
        afterSaleType = getIntent().getStringExtra("afterSaleType");
        if (!TextUtils.isEmpty(afterSaleType)) {
            txtModeValue.setText(afterSaleType);
        }
        setTitle(title);

        inflater = LayoutInflater.from(this);
        loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("数据上传中");

        uploadService.addCallback(this);

        loadFailView.setOnReloadListener(this);

        loadOrderDetail();
    }

    private void loadOrderDetail() {
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
                }
            }

            @Override
            public void onError(Throwable ex) {
                loadFailView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void bindSource() {
        JSONObject goodsObject = JsonUtils.getJsonObject(resultObject, "orderGoodsRelationDetail", null);

        int applyCount = JsonUtils.getInt(goodsObject, "afterApplyCount", 0);
        int buyCount = JsonUtils.getInt(goodsObject, "amount", 0);
        maxCount = buyCount - applyCount;
        countView.setText(maxCount + "");
        miuView.setEnabled(false);
        miuView.setTextColor(getResources().getColor(R.color.gray_add));
        miuCanClick = false;
        if (maxCount == 1) {
            addView.setTextColor(getResources().getColor(R.color.gray_add));
            addView.setEnabled(false);
            miuCanClick = false;
        }

        String cover = JsonUtils.getString(goodsObject, "cover", "");
        ImageUtils.loadImage(glide, cover, goodsCover, R.mipmap.loadpicture);

        String title = JsonUtils.getString(goodsObject, "title", "");
        txtGoodsTitle.setText(title);
        WindowUtils.boldMethod(txtGoodsTitle);

        String style = JsonUtils.getString(goodsObject, "styleName", "");
        txtGoodsStyle.setText(style);
        WindowUtils.boldMethod(txtGoodsStyle);

        String sub = JsonUtils.getString(goodsObject, "subName", "");
        txtGoodsSub.setText(sub);
        WindowUtils.boldMethod(txtGoodsSub);

        String phone = JsonUtils.getString(resultObject, "phone", "");
        if (!TextUtils.isEmpty(phone)) {
            txtPhoneValue.setText(phone);
            txtPhoneValue.setTextColor(getResources().getColor(R.color.TAB_GRAY));
        }

        double money = JsonUtils.getDouble(goodsObject, "money", 0);
        double expressMoney = JsonUtils.getDouble(goodsObject, "expressMoney", 0);
        String moneyStr = DoubleUtils.format(money + expressMoney).replace(".00", "");
        if (expressMoney == 0) {
            txtMoney.setText("¥" + moneyStr + "(免运费)");
        } else {
            txtMoney.setText("¥" + moneyStr + "(含运费¥" + DoubleUtils.format(expressMoney) + ")");
        }
        WindowUtils.boldMethod(txtMoney);

        if (!TextUtils.isEmpty(afterSaleType) && afterSaleType.equals("申请退款")) {
            countView.setVisibility(View.VISIBLE);
            adjustView.setVisibility(View.GONE);
            modeContainer.setVisibility(View.GONE);
            btnSubmit.setText("申请退款");
        } else {
            countView.setVisibility(View.GONE);
            adjustView.setVisibility(View.VISIBLE);
            modeContainer.setVisibility(View.VISIBLE);
            btnSubmit.setText("申请售后");
        }
        addView.setOnClickListener(this);
        miuView.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        modeContainer.setOnClickListener(this);
        reasonContainer.setOnClickListener(this);

        final String orderNumber = JsonUtils.getString(goodsObject, "orderNumber", "");
        txtOrderNumberValue.setText(orderNumber);

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCopyClick(orderNumber);
            }
        });

        grid_view.setAdapter(adapter);
    }

    @Override
    public void onReload() {
        loadOrderDetail();
    }

    private void btnCopyClick(String orderNumber) {
        ClipboardUtils.setClipboardText(orderNumber, this);
    }

    private void btnSubmitClick() {
        if (txtModeValue.getText().toString().equals("请选择")) {
            AlertManager.toast(this, "请选择处理方式");
            return;
        }

        if (txtReasonValue.getText().toString().equals("请选择")) {
            AlertManager.toast(this, "请选择申请原因");
            return;
        }

        String phone = txtPhoneValue.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            AlertManager.toast(this, "请填写手机号码");
            return;
        }

        if (!PhoneUtils.validateMobile(phone)) {
            AlertManager.toast(this, "手机号码格式错误");
            return;
        }

        String problem = txtProblemValue.getText().toString();
        if (problem.length() < 5) {
            AlertManager.toast(this, "问题描述内容长度不能小于5");
            return;
        }

        if (uploadService.isRuning()) {
            AlertManager.toast(this, "操作过于频繁，请稍后再试");
            return;
        }

        if (isUploading) {
            return;
        }

        loadingDialog.show();
        isUploading = true;

        if (mSelectPath.size() == 0) {
            submitData();
        } else {
            uploadService.clearTask();
            uploadBackUrls.clear();
            for (String path : mSelectPath) {
                uploadService.addTask(path);
            }
            uploadService.startUpload();
        }

    }

    private void submitData() {
        JSONObject goodsObject = JsonUtils.getJsonObject(resultObject, "orderGoodsRelationDetail", null);
        String relationId = JsonUtils.getString(goodsObject, "relationId", "");
        String phone = txtPhoneValue.getText().toString();
        String remark = txtProblemValue.getText().toString();
        String reason = txtReasonValue.getText().toString();
        StringBuilder images = new StringBuilder();
        for (int i = 0; i < uploadBackUrls.size(); i++) {
            if (i < uploadBackUrls.size() - 1) {
                images.append(uploadBackUrls.get(i) + ",");
            } else {
                images.append(uploadBackUrls.get(i));
            }
        }

        String workType = null;
        String type = txtModeValue.getText().toString();
        if (type.equals("申请退款")) {
            workType = "1";
        } else if (type.equals("换货")) {
            workType = "2";
        } else if (type.equals("退货退款")) {
            workType = "3";
        }

        Map<String, Object> params = new HashMap<>();
        params.put("relationId", relationId);
        params.put("phone", phone);
        params.put("remark", remark);
        params.put("reason", reason);
        params.put("images", images.toString());
        params.put("workType", workType);
        params.put("applyCount", applyCount);

        loadingDialog.show();

        HttpRequest.postJson(AppConfig.Apply_AfterSale, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                isUploading = false;
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {

                    AlertManager.showSuccessToast(ApplyAfterSaleActivity.this, "申请成功", false);

                    EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_refresh_order_list);
                    EventBus.getDefault().post(notification);

                    finish();

                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(ApplyAfterSaleActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(ApplyAfterSaleActivity.this, "申请失败，请稍后再试", false);
            }
        });


    }

    private void showReasons() {
        if (!TextUtils.isEmpty(afterSaleType) && afterSaleType.equals("申请退款")) {
            final String[] items = new String[]{"不想要了", "发货太慢，不想等了", "忘记使用优惠券", "商家无货", "其他"};
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            txtReasonValue.setText(items[which]);
                            txtReasonValue.setTextColor(getResources().getColor(R.color.TAB_GRAY));
                        }
                    })
                    .setCancelable(true)
                    .setNegativeButton("关闭", null)
                    .create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } else {
            final String[] itemNew = new String[]{"不想要了", "商家发错货", "质量问题", "商品损坏", "商品与页面描述不符", "其他"};
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setItems(itemNew, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            txtReasonValue.setText(itemNew[which]);
                            txtReasonValue.setTextColor(getResources().getColor(R.color.TAB_GRAY));
                        }
                    })
                    .setCancelable(true)
                    .setNegativeButton("关闭", null)
                    .create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        }
    }

    private void showModes() {
        final String[] items = new String[]{"换货", "退货退款"};
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        txtModeValue.setText(items[which]);
                        txtModeValue.setTextColor(getResources().getColor(R.color.TAB_GRAY));
                    }
                })
                .setCancelable(true)
                .setNegativeButton("关闭", null)
                .create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }


    @Override
    public void onClick(View v) {
        if (v == btnSubmit) {
            btnSubmitClick();
        } else if (v == modeContainer) {
            showModes();
        } else if (v == reasonContainer) {
            showReasons();
        } else if (v == addView) {
            addCount();
        } else if (v == miuView) {
            miuCount();
        } else {
            if ("add".equals(v.getTag())) {
                if (mSelectPath != null && mSelectPath.size() == 3) {
                    return;
                }
                doSelectImages();
            } else {

            }
        }
    }

    private void doSelectImages() {
        int permission = ActivityCompat.checkSelfPermission(this,
                "android.permission.CAMERA");
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 没有写的权限，去申请写的权限，会弹出对话框
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_CAMERA_CODE);
            return;
        }

        Intent intent = new Intent(this, MultiImageSelectorActivity.class);
        // 是否显示拍摄图片
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        // 最大可选择图片数量
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 3);
        // 选择模式
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
        // 默认选择
        if (mSelectPath != null && mSelectPath.size() > 0) {
            intent.putExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSelectPath);
//            if (mSelectPath.size() == 3){
//                intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, false);
//                AlertManager.show(ApplyAfterSaleActivity.this,"已达到最大可选图片数目，不可再拍摄添加图片");
//            }else {
//                intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
//            }
        }
        startActivityForResult(intent, SELECT_IMAGE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_CODE) {
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.CAMERA");
            if (permission == PackageManager.PERMISSION_GRANTED) {
                doSelectImages();
            } else {
                new android.support.v7.app.AlertDialog.Builder(this).setTitle("权限申请")//设置对话框标题
                        .setMessage("在设置-应用-构巢-权限中启用摄像头权限，以正常使用构巢功能")//设置显示的内容
                        .setPositiveButton("去设置", new DialogInterface.OnClickListener() {//添加确定按钮
                            @Override
                            public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                Uri packageURI = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();//在按键响应事件中显示此对话框
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE_CODE && resultCode == RESULT_OK) {
            mSelectPath = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void success(int index, String path) {
        uploadBackUrls.add(path);
    }

    @Override
    public void startUpload(int index) {
    }

    @Override
    public void complete() {
        submitData();
    }

    @Override
    public void error(int index) {
        loadingDialog.dismiss();
        uploadService.clearTask();
        uploadBackUrls.clear();
        isUploading = false;
        AlertManager.showErrorToast(this, "上传失败，请稍后再试", false);
    }

    @Override
    public void finish() {
        uploadService.removeCallback(this);
        super.finish();
    }

    class ImageAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return (mSelectPath == null ? 0 : mSelectPath.size()) + 1;
        }

        @Override
        public String getItem(int position) {
            return mSelectPath.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.add_image_item, parent, false);
            int size = (WindowUtils.getDeviceWidth(ApplyAfterSaleActivity.this) - WindowUtils.dpToPixels(ApplyAfterSaleActivity.this, 39)) / 4;
            GridView.LayoutParams params = new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, size);
            convertView.setLayoutParams(params);

            final ImageView imageView = convertView.findViewById(R.id.imageView);

            View layDelete = convertView.findViewById(R.id.layDelete);
            if (position == getCount() - 1) {
                imageView.setImageResource(R.mipmap.add);
                convertView.setTag("add");
                layDelete.setVisibility(View.GONE);
            } else {
                String filePath = "file://" + getItem(position);

                ImageUtils.loadImage(glide, filePath, imageView, -1);
                convertView.setTag(position);
                layDelete.setVisibility(View.VISIBLE);

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mArray = new ArrayList<>();
                        for (int i = 0; i < mSelectPath.size(); i++) {
                            mArray.add(mSelectPath.get(i));
                        }
                        mArrayImage = new String[mArray.size()];
                        for (int i = 0; i < mArray.size(); i++) {
                            mArray.set(i, mArray.get(i) + AppConfig.IMAGE_COMPRESS);
                            mArrayImage[i] = mArray.get(i);
                        }

                        String currentSrc = mArray.get(position);
                        Intent intent = new Intent();
                        intent.setClass(ApplyAfterSaleActivity.this, GalleryActivity.class);
                        intent.putExtra("isLocal", true);
                        intent.putExtra("currentSrc", currentSrc);
                        intent.putExtra("images", mArrayImage);
                        ApplyAfterSaleActivity.this.startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                });
            }
            if (getCount() == 4) {
                if (position == getCount() - 1) {
                    imageView.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            }
            convertView.setOnClickListener(ApplyAfterSaleActivity.this);

            layDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectPath.remove(position);
                    adapter.notifyDataSetChanged();
                }
            });


            return convertView;
        }
    }

    private void addCount() {
        if (applyCount == (maxCount - 1)) {
            addView.setEnabled(false);
            addView.setTextColor(getResources().getColor(R.color.gray_add));
            addCanClick = false;
        }
        if (!miuCanClick) {
            miuView.setEnabled(true);
            miuView.setTextColor(getResources().getColor(R.color.BLACK));
            miuCanClick = true;
        }
        applyCount = applyCount + 1;
        askCount.setText(applyCount + "");
    }

    private void miuCount() {
        if (applyCount == 2) {
            miuView.setEnabled(false);
            miuView.setTextColor(getResources().getColor(R.color.gray_add));
            miuCanClick = false;
        }
        if (!addCanClick) {
            addView.setEnabled(true);
            addView.setTextColor(getResources().getColor(R.color.BLACK));
            addCanClick = true;
        }
        applyCount = applyCount - 1;
        askCount.setText(applyCount + "");
    }
}
