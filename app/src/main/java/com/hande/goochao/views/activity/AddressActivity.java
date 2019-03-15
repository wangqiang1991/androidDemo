package com.hande.goochao.views.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.ConfirmDialog;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Wangem on 2018/2/10.
 */

public class AddressActivity extends ToolBarActivity implements AdapterView.OnItemClickListener {

    @ViewInject(R.id.refresh_layout_address_list)
    private RefreshLayout refreshLayout;
    @ViewInject(R.id.load_fail_view_address_list)
    private LoadFailView loadFailView;
    @ViewInject(R.id.noDataView)
    private NoDataView noDataView;
    @ViewInject(R.id.address_loading_view)
    private LoadingView loadingView;

    @ViewInject(R.id.address_list)
    private ListView addressListView;
    @ViewInject(R.id.create_address_bt)
    private Button createAddressButton;

    private AddressListAdapter addressListAdapter;
    private JSONArray addressResources;
    private List<JSONObject> dataList = new ArrayList<>();

    private boolean firstLoad = true;
    private CustomLoadingDialog loadingDialog;
    private boolean selectMode;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_address_listview);
        setTitle("收货地址");

        selectMode = getIntent().getBooleanExtra("selectMode", false);

        noDataView.setImageAndText(R.mipmap.new_address, "这位巢客 您还没有添加收货地址~");
        loadingDialog = new CustomLoadingDialog(this);

        createAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() ,CreateAddressActivity.class);
                startActivityForResult(intent,1);
            }
        });
        addressListAdapter = new AddressListAdapter();
        addressListView.setAdapter(addressListAdapter);

        /**
         * 下拉重新加载
         */
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                dataList.clear();
                loadAddressItem();
            }
        });

        loadAddressItem();

        if (selectMode) {
            addressListView.setOnItemClickListener(this);
        }
        refreshLayout.setEnableLoadMore(false);
    }

    private void loadAddressItem() {
        if (firstLoad){
            loadingView.setVisibility(View.VISIBLE);
        }
        HttpRequest.get(AppConfig.ADDRESS_LIST, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
                refreshLayout.finishRefresh();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    addressResources = JsonUtils.getJsonArray(response, "data", null);
                    if (addressResources != null) {
                        for (int i = 0; i < addressResources.length(); i++) {
                            try {
                                dataList.add(addressResources.getJSONObject(i));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (dataList.size() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                        addressListView.setVisibility(View.GONE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                        addressListView.setVisibility(View.VISIBLE);
                    }
                    firstLoad = false;
                    addressListAdapter.notifyDataSetChanged();
                    resetView();
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(AddressActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(AddressActivity.this, "服务器繁忙", false);
                showError();
            }
        });
    }

    private void setAddressDefault(int m) {
        loadingDialog.setLoadingText("加载中");
        loadingDialog.show();

        String addressId = JsonUtils.getString(JsonUtils.getJsonItem(addressResources,m,null),"addressId","");
        Map<String,Object> params = new HashMap<>();
        params.put("addressId","" + addressId);
        HttpRequest.postJson(AppConfig.ADDRESS_USE_POST + addressId, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    AlertManager.showSuccessToast(AddressActivity.this, "设置成功", false);
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(AddressActivity.this, message, false);
                }

            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                loadingDialog.dismiss();
                showError();
            }
        });

    }

    private void cancelAddressDefault(int m) {
        loadingDialog.setLoadingText("加载中");
        loadingDialog.show();

        String addressId = JsonUtils.getString(JsonUtils.getJsonItem(addressResources,m,null),"addressId","");
        Map<String,Object> params = new HashMap<>();
        params.put("addressId","" + addressId);
        HttpRequest.postJson(AppConfig.ADDRESS_USE_CANCEL + addressId, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    AlertManager.showSuccessToast(AddressActivity.this, "设置成功", false);
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(AddressActivity.this, message, false);
                }

            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                loadingDialog.dismiss();
                showError();
            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        JSONObject item = JsonUtils.getJsonItem(addressResources, position, null);
        Intent intent = new Intent();
        intent.putExtra("selectedItem", item.toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    class AddressListAdapter extends BaseAdapter {

        class ViewHolder {
            TextView txtNameValue;
            TextView txtPhoneValue;
            TextView txtAddressValue;
            View editAddress;
            View deleteAddress;
            View defaultBt;
            ImageView defaultView;

            ViewHolder(View view) {
                txtNameValue = view.findViewById(R.id.receiver_name);
                txtPhoneValue = view.findViewById(R.id.receiver_phone);
                txtAddressValue = view.findViewById(R.id.receiver_address);
                editAddress = view.findViewById(R.id.edit_address_bt);
                deleteAddress =  view.findViewById(R.id.delete_address_bt);
                defaultBt = view.findViewById(R.id.default_view);
                defaultView =  view.findViewById(R.id.default_image_view);
            }
        }

        @Override
        public int getCount() {
            return addressResources == null ? 0 : addressResources.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(addressResources, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(AddressActivity.this).inflate(R.layout.address_listview_item, viewGroup, false);
                view.setTag(new ViewHolder(view));
            }

            ViewHolder holder = (ViewHolder) view.getTag();

            holder.editAddress.setOnClickListener(new View.OnClickListener() {
                @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() ,EditAddressActivity.class);
                Bundle bundle = new Bundle();
                    bundle.putString("addressId",JsonUtils.getString(getItem(i),"addressId",""));
                    intent.putExtras(bundle);
                    startActivityForResult(intent,2);
            }
            });

            holder.deleteAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConfirmDialog alertDialog = new ConfirmDialog(AddressActivity.this, ConfirmDialog.ConfirmDialogType.ConfirmDialogType_Danger);
                    alertDialog.setMsg("您是否确认删除该收货地址？")
                            .setLeftButtonText("取消")
                            .setRightButtonText("删除")
                            .setCallBack(new ConfirmDialog.CallBack() {
                                @Override
                                public void buttonClick(Dialog dialog, boolean leftClick) {
                                    dialog.dismiss();
                                    if (!leftClick) {
                                        deleteAddressItem(i);
                                    }
                                }
                            });
                    alertDialog.show();
                }
            });

            holder.defaultBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!JsonUtils.getBoolean(JsonUtils.getJsonItem(addressResources, i ,null),"defaultUse" ,false)){
                        setAddressDefault(i);
                        try{
                            for (int i1 = 0; i1 < addressResources.length(); i1++) {
                                addressResources.getJSONObject(i1).put("defaultUse", false);
                            }
                            addressResources.getJSONObject(i).put("defaultUse", true);
                        } catch (Exception ex) {

                        }
                    }else {
                        cancelAddressDefault(i);
                        try{
                            for (int i1 = 0; i1 < addressResources.length(); i1++) {
                                addressResources.getJSONObject(i1).put("defaultUse", false);
                            }
                        } catch (Exception ex) {

                        }
                    }
                    notifyDataSetChanged();
                }
            });

            boolean check = JsonUtils.getBoolean(getItem(i), "defaultUse", false);
            if (check){
                holder.defaultView.setImageResource(R.mipmap.me_address_icon_selected);
            }else {
                holder.defaultView.setImageResource(R.mipmap.me_address_icon_default);
            }

            holder.txtNameValue.setText(JsonUtils.getString(getItem(i),"reallyName",""));
            holder.txtPhoneValue.setText(JsonUtils.getString(getItem(i),"phone",""));
            String allTheAddress = JsonUtils.getString(getItem(i),"province","")+JsonUtils.getString(getItem(i),"city","")
                    +JsonUtils.getString(getItem(i),"county","")+JsonUtils.getString(getItem(i),"address","");
            holder.txtAddressValue.setText(allTheAddress);
            return view;
        }
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
        } else {
            AlertManager.showErrorInfo(getApplicationContext());
        }
    }

    private void deleteAddressItem(int n){
        String addressId = JsonUtils.getString(JsonUtils.getJsonItem(addressResources,n,null),"addressId","");
        Map<String, String> params = new HashMap<>();
        params.put("addressId", ""+ addressId);
        loadingDialog.setLoadingText("删除中");
        loadingDialog.show();
        HttpRequest.post(AppConfig.DELETE_ADDRESS + addressId, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
                AlertManager.showSuccessToast(AddressActivity.this, "删除成功", false);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if(JsonUtils.getCode(response) == 0) {
                    loadingDialog.dismiss();
                    dataList = new ArrayList<>();
                    loadAddressItem();
                    addressListAdapter.notifyDataSetChanged();
                }else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(AddressActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                loadingDialog.dismiss();
                AppLog.e("err", ex);
                showError();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loadAddressItem();
        addressListAdapter.notifyDataSetChanged();

    }
}
