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
import com.hande.goochao.views.widget.AddressPickerView;

import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LMC on 2018/3/6.
 */

public class CreateAddressActivity extends ToolBarActivity implements View.OnClickListener{

    @ViewInject(R.id.new_receiver_name)
    private EditText writeName;
    @ViewInject(R.id.new_receiver_phone)
    private EditText writePhone;
    @ViewInject(R.id.new_receiver_all_address)
    private EditText writeAddress;
    @ViewInject(R.id.address)
    private TextView mTvAddress;
    @ViewInject(R.id.open_window)
    private View openWindow;
    @ViewInject(R.id.set_default_bt)
    private Switch defaultSetBt;
    @ViewInject(R.id.save_bt)
    private Button saveBt;
    private View mirror_view;

    private Window window;
    private PopupWindow popupWindowRegion;
    private AddressPickerView addressView;

    private CustomLoadingDialog loadingDialog;
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);
        setTitle("新增地址");

        loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("保存中");

        //加粗字体
        WindowUtils.boldMethod(shouHuo);
        WindowUtils.boldMethod(dianHua);
        WindowUtils.boldMethod(shengShi);
        WindowUtils.boldMethod(xiangXi);
        WindowUtils.boldMethod(sheZhi);

        openWindow.setOnClickListener(this);
        saveBt.setOnClickListener(this);
        defaultSetBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    defaultSetBt.setBackgroundResource(R.mipmap.address_btn_setting_open);
                    defaultUse = true;
                    }
                    else {
                    defaultSetBt.setBackgroundResource(R.mipmap.address_btn_setting_close);
                    defaultUse = false;
                    }

            }
        });

    }

    private void doSave(){

        String name = writeName.getText().toString();
        String phone = writePhone.getText().toString();
        String areaAddress = mTvAddress.getText().toString();
        String detailAddress = writeAddress.getText().toString();

        if (TextUtils.isEmpty(name)) {
            AlertManager.toast(CreateAddressActivity.this, "收货人不能为空");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            AlertManager.toast(CreateAddressActivity.this, "手机号码不能为空");
            return;
        }

        if (!PhoneUtils.validateMobile(phone)) {
            AlertManager.toast(CreateAddressActivity.this, "手机号码格式错误");
            return;
        }

        if (TextUtils.isEmpty(areaAddress)) {
            AlertManager.toast(CreateAddressActivity.this, "请选择省、市、区县");
            return;
        }

        if (TextUtils.isEmpty(detailAddress)) {
            AlertManager.toast(CreateAddressActivity.this, "请设置收货详细地址");
            return;
        }
            saveAddress(name, phone, detailAddress);

    }

    private void showPopViewRegion(Activity activity){

        window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.alpha = 0.5f;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setAttributes(params);

        if (popupWindowRegion == null){
            final View contentView = LayoutInflater.from(CreateAddressActivity.this).inflate(R.layout.popwindow_address_region, null);
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
                    mTvAddress.setText(address);
                    popupWindowRegion.dismiss();
                }
            });

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            popupWindowRegion.setWidth(Math.round(screenWidth));
        }
        View rootViewRegion = LayoutInflater.from(CreateAddressActivity.this).inflate(R.layout.activity_edit_address, null);
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

    private void saveAddress (String name, String phone, String detailAddress){

        loadingDialog.show();

        Map<String, Object> params = new HashMap<>();
        params.put("reallyName", "" + name);
        params.put("phone",""+ phone);
        params.put("province",""+ addressView.returnSelectProvince());
        params.put("city", "" + addressView.returnSelectCity());
        params.put("county", "" + addressView.returnSelectCounty());
        params.put("address", "" + detailAddress);
        params.put("defaultUse", "" + defaultUse);
        params.put("provinceCode", ""+ addressView.returnSelectProvinceCode());
        params.put("cityCode", ""+ addressView.returnSelectCityCode());
        params.put("countyCode", ""+ addressView.returnSelectCountyCode());
        HttpRequest.postJson(AppConfig.ADDRESS_LIST, null, params, JSONObject.class, new RequestCallback<JSONObject>(){
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    setResult(2);
                    AlertManager.showSuccessToast(CreateAddressActivity.this,"保存成功",false);
                    finish();
                }else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(CreateAddressActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(CreateAddressActivity.this,"服务器繁忙，请稍后重试",false);
                AppLog.e("err", ex);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == openWindow){
            showPopViewRegion(CreateAddressActivity.this);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            imm.hideSoftInputFromWindow(openWindow.getWindowToken(), 0); //强制隐藏键盘
        }else if (v == saveBt){
            doSave();
        }
    }
}
