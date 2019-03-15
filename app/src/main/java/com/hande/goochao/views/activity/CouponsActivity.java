package com.hande.goochao.views.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.NumberUtils;
import com.hande.goochao.utils.PriceUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
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

public class CouponsActivity extends ToolBarActivity {

    @ViewInject(R.id.refresh_layout_my_coupons)
    private RefreshLayout refreshLayout;
    @ViewInject(R.id.noDataView)
    private NoDataView noDataView;
    @ViewInject(R.id.my_coupon_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.load_fail_view_coupons_list)
    private LoadFailView loadFailView;
    private boolean firstLoad = true;

    @ViewInject(R.id.coupons_list)
    private ListView couponsListView;
    private CouponsAdapter couponsAdapter;

    private JSONArray couponsResource;
    private List<JSONObject> dataList = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupons);
        setTitle("我的优惠券");

        couponsAdapter = new CouponsAdapter();
        couponsListView.setAdapter(couponsAdapter);

        noDataView.setImageAndText(R.mipmap.no_coupon, "暂无可用优惠券");

        /**
         * 下拉重新加载
         */
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                dataList.clear();
                loadCoupons();
            }
        });

        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadCoupons();
            }
        });

        refreshLayout.setEnableLoadMore(false);
        loadCoupons();
    }

    private void loadCoupons() {
        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        }
        Map<String, String> params = new HashMap<>();
//        params.put("status", "" + 1 );
        HttpRequest.get(AppConfig.COUPONS_LIST, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
                refreshLayout.finishRefresh();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    couponsResource = JsonUtils.getJsonArray(response, "data", null);
                    if (couponsResource != null) {
                        for (int i = 0; i < couponsResource.length(); i++) {
                            try {
                                dataList.add(couponsResource.getJSONObject(i));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (dataList.size() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                    }
                    resetView();
                    firstLoad = false;
                    couponsAdapter.notifyDataSetChanged();
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(CouponsActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
                AlertManager.showErrorToast(CouponsActivity.this, "服务器繁忙", false);
            }
        });
    }

    class CouponsAdapter extends BaseAdapter {

        class ViewHolder {
            TextView detailTxt;
            TextView descTxt;
            TextView dateTxt;
            View couponView;
            TextView typeString;

            ViewHolder(View view) {
                detailTxt = view.findViewById(R.id.coupon_detail);
                descTxt = view.findViewById(R.id.coupon_desc);
                dateTxt = view.findViewById(R.id.coupon_date);
                couponView = view.findViewById(R.id.person_coupon_background);
                typeString = view.findViewById(R.id.coupon_type_string);
            }
        }

        @Override
        public int getCount() {
            return couponsResource == null ? 0 : couponsResource.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(couponsResource, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(CouponsActivity.this).inflate(R.layout.activity_coupons_item, viewGroup, false);
                view.setTag(new ViewHolder(view));
            }
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            JSONObject item = getItem(i);

            String deadDate = JsonUtils.getString(item, "expireDescription", "");
            viewHolder.dateTxt.setText(deadDate);

            String couponTypeStr = JsonUtils.getString(item, "typeStr", "");
            viewHolder.typeString.setText(couponTypeStr);
            WindowUtils.boldMethod(viewHolder.typeString);

            viewHolder.descTxt.setText(JsonUtils.getString(item, "description",""));

            String couponName = JsonUtils.getString(item, "name", "");
            viewHolder.detailTxt.setText(couponName);
            WindowUtils.boldMethod(viewHolder.detailTxt);

            int cmrStatus = JsonUtils.getInt(item, "cmrStatus", 0);
            if (cmrStatus != 1) {
                //过期
                viewHolder.couponView.setBackgroundResource(R.mipmap.coupon_disabled_guoqi);
                viewHolder.detailTxt.setTextColor(getResources().getColor(R.color.gray_add));
                viewHolder.descTxt.setTextColor(getResources().getColor(R.color.gray_add));
                viewHolder.typeString.setTextColor(getResources().getColor(R.color.gray_add));
            } else {
                //未过期
                viewHolder.couponView.setBackgroundResource(R.mipmap.coupon_bg);
                viewHolder.detailTxt.setTextColor(getResources().getColor(R.color.TAB_GRAY));
                viewHolder.descTxt.setTextColor(getResources().getColor(R.color.new_product_detail));
                viewHolder.typeString.setTextColor(getResources().getColor(R.color.nice_red));
            }

            return view;
        }
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
        } else {
            AlertManager.showErrorInfo(getApplicationContext());
            loadingView.setVisibility(View.GONE);
        }
    }
}
