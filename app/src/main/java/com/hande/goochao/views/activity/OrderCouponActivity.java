package com.hande.goochao.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.Params;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.NumberUtils;
import com.hande.goochao.utils.PriceUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

public class OrderCouponActivity extends ToolBarActivity implements LoadFailView.OnReloadListener, AdapterView.OnItemClickListener {

    @ViewInject(R.id.coupon_list_view)
    private ListView couponListView;
    @ViewInject(R.id.order_coupon_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.order_coupon_nodata)
    private NoDataView  noDataView;

    private CouponListAdapter adapter;

    // intent params
    private String orderId;
    private String couponId;
    // load return
    private JSONArray couponArr;

    private boolean haveCheckOne;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_coupon);
        setTitle("优惠券");

        orderId = getIntent().getStringExtra("orderId");
        couponId = getIntent().getStringExtra("couponId");
        if (TextUtils.isEmpty(orderId)) {
            AlertManager.showErrorToast(this, "参数无效", false);
            return;
        }

        noDataView.setImageAndText(R.mipmap.no_coupon,"暂无可用优惠券");

        initViews();

        loadCoupon();
    }

    private void initViews() {
        loadFailView.setOnReloadListener(this);
        adapter = new CouponListAdapter();
        couponListView.setAdapter(adapter);
        couponListView.setOnItemClickListener(this);
    }

    private void showLoadFail() {
        loadFailView.setVisibility(View.VISIBLE);
    }

    private void hideLoadFail() {
        loadFailView.setVisibility(View.GONE);
    }

    private void loadCoupon() {
        loadingView.setVisibility(View.VISIBLE);
        HttpRequest.get(AppConfig.ORDER_COUPON_LIST, null, Params.buildForStr("orderId", orderId), JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    couponArr = JsonUtils.getJsonArray(response, "data", new JSONArray());
                    for (int i = 0; i < couponArr.length(); i++) {
                        JSONObject item = JsonUtils.getJsonItem(couponArr, i, new JSONObject());
                        String itemCouponId = JsonUtils.getString(item, "couponId", null);
                        if (!TextUtils.isEmpty(couponId) && couponId.equals(itemCouponId) && !haveCheckOne) {
                            haveCheckOne = true;
                            JsonUtils.put(item, "checked", true);
                        } else {
                            JsonUtils.put(item, "checked", false);
                        }
                    }
                    if (couponArr.length() == 0 ){
                        noDataView.setVisibility(View.VISIBLE);
                    }else {
                        noDataView.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    showLoadFail();
                }
            }

            @Override
            public void onError(Throwable ex) {
                showLoadFail();
            }
        });
    }

    @Override
    public void onReload() {
        hideLoadFail();
        loadCoupon();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        JSONObject item = JsonUtils.getJsonItem(couponArr, position, null);
        boolean use = JsonUtils.getBoolean(item, "use", false);
        // 不可用，不能选择
        if (!use) {
            return;
        }
        boolean checked = getChecked(item);

        // 重置选择
        resetChecked();
        if (checked){
            adapter.notifyDataSetChanged();
            // 选择后关闭
            doSelectedFinish(null);
        }else {
            // 设置选择
            setChecked(item, !checked);
            // 通知更新页面
            adapter.notifyDataSetChanged();
            // 选择后关闭
            doSelectedFinish(item);
        }
    }

    private void doSelectedFinish(JSONObject item) {
        if (item != null){
            Intent intent = new Intent();
            intent.putExtra("selectedItem", item.toString());
            setResult(RESULT_OK, intent);
            finish();
        }else {
            Intent intent = new Intent();
            intent.putExtra("selectedItem", "");
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void resetChecked() {
        for (int i = 0; i < couponArr.length(); i++) {
            JSONObject item = JsonUtils.getJsonItem(couponArr, i, null);
            setChecked(item, false);
        }
    }

    private void setChecked(JSONObject item, boolean checked) {
        JsonUtils.put(item, "checked", checked);
    }

    private boolean getChecked(JSONObject item) {
        return JsonUtils.getBoolean(item, "checked", false);
    }

    class CouponListAdapter extends BaseAdapter {

        class ViewHolder {
            TextView detailTxt;
            TextView descTxt;
            TextView dateTxt;
            View couponView;
            ImageView checkboxView;
            TextView typeString;

            ViewHolder(View view) {
                detailTxt = view.findViewById(R.id.coupon_detail);
                descTxt = view.findViewById(R.id.coupon_desc);
                dateTxt = view.findViewById(R.id.coupon_date);
                couponView = view.findViewById(R.id.coupon_background);
                checkboxView = view.findViewById(R.id.checkbox);
                typeString = view.findViewById(R.id.coupon_type_string);
            }
        }

        @Override
        public int getCount() {
            return couponArr == null ? 0 : couponArr.length();
        }

        @Override
        public JSONObject getItem(int position) {
            return JsonUtils.getJsonItem(couponArr, position, null);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.layout_order_coupon_item, parent, false);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            final JSONObject item = getItem(position);
            String deadDate = JsonUtils.getString(item, "expireDescription", "");
            viewHolder.dateTxt.setText(deadDate);
            viewHolder.checkboxView.setSelected(getChecked(item));

            String couponTypeStr = JsonUtils.getString(item, "typeStr", "");
            viewHolder.typeString.setText(couponTypeStr);
            WindowUtils.boldMethod(viewHolder.typeString);

            viewHolder.descTxt.setText(JsonUtils.getString(item, "description",""));

            String couponName = JsonUtils.getString(item, "name", "");
            viewHolder.detailTxt.setText(couponName);

            boolean use = JsonUtils.getBoolean(item, "use", false);
            if (use) {
                // 可以使用
                viewHolder.checkboxView.setVisibility(View.VISIBLE);
                viewHolder.checkboxView.setEnabled(true);
                viewHolder.couponView.setBackgroundResource(R.mipmap.coupon_bg);
                viewHolder.detailTxt.setTextColor(getResources().getColor(R.color.TAB_GRAY));
                viewHolder.typeString.setTextColor(getResources().getColor(R.color.nice_red));
                viewHolder.descTxt.setTextColor(getResources().getColor(R.color.new_product_detail));
            } else {
                // 不可以使用
                viewHolder.checkboxView.setVisibility(View.GONE);
                viewHolder.checkboxView.setEnabled(false);
                viewHolder.couponView.setBackgroundResource(R.mipmap.coupon_disabled);
                viewHolder.detailTxt.setTextColor(getResources().getColor(R.color.gray_add));
                viewHolder.typeString.setTextColor(getResources().getColor(R.color.gray_add));
                viewHolder.descTxt.setTextColor(getResources().getColor(R.color.gray_add));
            }

            return convertView;
        }
    }
}
