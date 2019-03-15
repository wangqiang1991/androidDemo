package com.hande.goochao.views.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.PhoneUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.widget.AddressPickerView;

import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LMC on 2018/3/5.
 */

public class EditAddressActivity extends ToolBarActivity{

    @ViewInject(R.id.edit_address_loading_view)
    private LoadingView loadingView;
    @ViewInject(R.id.edit_address_load_fail)
    private LoadFailView loadFailView;
    @ViewInject(R.id.new_receiver_name)
    private EditText editName;
    @ViewInject(R.id.new_receiver_phone)
    private EditText editPhone;
    @ViewInject(R.id.address)
    private TextView editRegion;
    @ViewInject(R.id.new_receiver_all_address)
    private EditText editAddress;
    @ViewInject(R.id.set_default_bt)
    private Switch editDefault;
    @ViewInject(R.id.save_bt)
    private Button saveBt;

    private JSONObject data;

    private String name;
    private String phone;
    private String region;
    private String address;

    private String newSetName;
    private String newSetPhone;
    private String newSetAddress;
    private String addressId;

    /**
     * 为弹窗初始化选中项参数
     */
    private String provinceCode;
    private String cityCode;
    private String countyCode;
    private String province;
    private String city;
    private String county;

    /**
     * 选中时传递值
     */
    private String saveProvince;
    private String saveCity;
    private String saveCounty;
    private String saveProvinceCode;
    private String saveCityCode;
    private String saveCountyCode;

    /**
     * 弹窗相关
     */
    @ViewInject(R.id.open_window)
    private View openRegion;
    private PopupWindow popupWindowRegion;
    private View mirror_view;
    private AddressPickerView addressView;
    private Window window;

    private boolean firstLoad = true;
    private boolean defaultUse;

    //加粗
    @ViewInject(R.id.shou_huo_ren)
    private TextView shouHuo;
    @ViewInject(R.id.lian_xi_dian_hua)
    private TextView dianHua;
    @ViewInject(R.id.sheng_shi_qv)
    private TextView shengShi;
    @ViewInject(R.id.xiang_xi_di_zhi)
    private TextView xiangXi;
    @ViewInject(R.id.she_zhi_mo_ren)
    private TextView sheZhi;

    private CustomLoadingDialog loadingDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);
        setTitle("编辑地址");

        loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("保存中");

        //加粗字体
        WindowUtils.boldMethod(shouHuo);
        WindowUtils.boldMethod(dianHua);
        WindowUtils.boldMethod(shengShi);
        WindowUtils.boldMethod(xiangXi);
        WindowUtils.boldMethod(sheZhi);

        /**
         * 获取收货地址界面传入的参数
         */
        Intent intent = getIntent();
        addressId = intent.getStringExtra("addressId");

            editDefault.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        editDefault.setBackgroundResource(R.mipmap.address_btn_setting_open);
                        defaultUse = true;
                    } else {
                        editDefault.setBackgroundResource(R.mipmap.address_btn_setting_close);
                        defaultUse = false;
                    }
                }
            });

            openRegion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopViewRegion(EditAddressActivity.this);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    imm.hideSoftInputFromWindow(openRegion.getWindowToken(), 0); //强制隐藏键盘
                }
            });

            saveBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    newSetName = editName.getText().toString();
                    newSetPhone = editPhone.getText().toString();
                    newSetAddress = editAddress.getText().toString();
                    String areaAddress = editAddress.getText().toString();

                    if (TextUtils.isEmpty(newSetName)) {
                        AlertManager.toast(EditAddressActivity.this, "收货人不能为空");
                        return;
                    }

                    if (TextUtils.isEmpty(newSetPhone)) {
                        AlertManager.toast(EditAddressActivity.this, "手机号码不能为空");
                        return;
                    }

                    if (!PhoneUtils.validateMobile(newSetPhone)) {
                        AlertManager.toast(EditAddressActivity.this, "手机号码格式错误");
                        return;
                    }

                    if (TextUtils.isEmpty(areaAddress)) {
                        AlertManager.toast(EditAddressActivity.this, "请选择省、市、区县");
                        return;
                    }

                    if (TextUtils.isEmpty(newSetAddress)) {
                        AlertManager.toast(EditAddressActivity.this, "请设置收货详细地址");
                        return;
                    }
                    saveAddress();
                }
            });

            loadAddressDetail();
    }

    /**
     * 为页面初始化数据
     */
    private void pageInit(){
        name = JsonUtils.getString(data, "reallyName", "");
        phone = JsonUtils.getString(data, "phone", "");
        address = JsonUtils.getString(data, "address", "");
        provinceCode = JsonUtils.getString(data, "provinceCode", "");
        cityCode = JsonUtils.getString(data, "cityCode", "");
        countyCode = JsonUtils.getString(data, "countyCode", "");
        province = JsonUtils.getString(data, "province", "");
        city = JsonUtils.getString(data, "city", "");
        county = JsonUtils.getString(data, "county", "");
        defaultUse = JsonUtils.getBoolean(data, "defaultUse", false);
        region = province + " " + city + " " + county;

        editName.setText(name);
        editPhone.setText(phone);
        editRegion.setText(region);
        editAddress.setText(address);
        editDefault.setChecked(defaultUse);
        editDefault.setBackgroundResource(editDefault.isChecked() ? R.mipmap.address_btn_setting_open : R.mipmap.address_btn_setting_close);

    }

    private void loadAddressDetail(){

        loadingView.setVisibility(View.VISIBLE);

        HttpRequest.get(AppConfig.ADDRESS_DETAIL + addressId, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    data = JsonUtils.getJsonObject(response,"data",null);
                    pageInit();
                    firstLoad = false;
                    loadFailView.setVisibility(View.GONE);
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(EditAddressActivity.this, "服务器繁忙，请稍后重试", false);
                AppLog.e("err", ex);
                showError();
            }

            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
            }

        });
    }

    private void showPopViewRegion(Activity activity){

        window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.alpha = 0.5f;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setAttributes(params);

        if (popupWindowRegion == null){
            final View contentView = LayoutInflater.from(EditAddressActivity.this).inflate(R.layout.popwindow_address_region, null);
            popupWindowRegion = new PopupWindow(contentView,
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            popupWindowRegion.setContentView(contentView);
            popupWindowRegion.setAnimationStyle(R.style.mypopwindow_anim_style);
            popupWindowRegion.setOutsideTouchable(true);

            mirror_view = contentView.findViewById(R.id.mirror_view);
            mirror_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindowRegion.dismiss();
                }
            });
            addressView = contentView.findViewById(R.id.apvAddress);
            addressView.setOnAddressPickerSure(new AddressPickerView.OnAddressPickerSureListener() {
                @Override
                public void onSureClick(String address, String provinceCode, String cityCode, String districtCode) {
                    editRegion.setText(address);
                    saveProvince = addressView.returnSelectProvince();
                    saveCity = addressView.returnSelectCity();
                    saveCounty = addressView.returnSelectCounty();
                    saveProvinceCode = addressView.returnSelectProvinceCode();
                    saveCityCode = addressView.returnSelectCityCode();
                    saveCountyCode = addressView.returnSelectCountyCode();
                    popupWindowRegion.dismiss();
                }
            });

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            popupWindowRegion.setWidth(Math.round(screenWidth));
        }
        View rootViewRegion = LayoutInflater.from(EditAddressActivity.this).inflate(R.layout.activity_edit_address, null);
        popupWindowRegion.showAtLocation(rootViewRegion, Gravity.BOTTOM, 0, 0);

        popupWindowRegion.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = window.getAttributes();
                params.alpha = 1.0f;
                window.setAttributes(params);
            }
        });
    }

    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
        } else {
            loadingView.setVisibility(View.GONE);
            AlertManager.showErrorInfo(this);
        }
    }

    private void saveAddress (){

        loadingDialog.show();

        Map<String, Object> params = new HashMap<>();
        params.put("reallyName", "" + newSetName);
        params.put("phone",""+ newSetPhone);
        params.put("address", "" + newSetAddress);
        params.put("defaultUse", "" + defaultUse);
        params.put("addressId", ""+ addressId);
        if (addressView == null || saveCounty == null ){
            params.put("province",""+ province);
            params.put("city", "" + city);
            params.put("county", "" + county);
            params.put("provinceCode", ""+ provinceCode);
            params.put("cityCode", ""+ cityCode);
            params.put("countyCode", ""+ countyCode);
        }
        else {
            params.put("province", "" + saveProvince);
            params.put("city", "" + saveCity);
            params.put("county", "" + saveCounty);
            params.put("provinceCode", "" + saveProvinceCode);
            params.put("cityCode", "" + saveCityCode);
            params.put("countyCode", "" + saveCountyCode);
        }

        HttpRequest.postJson(AppConfig.ADDRESS_LIST, null, params, JSONObject.class, new RequestCallback<JSONObject>(){
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    setResult(3);
                    AlertManager.showSuccessToast(EditAddressActivity.this,"保存成功",false);
                    finish();
                }else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(EditAddressActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
            }
        });
    }
}
