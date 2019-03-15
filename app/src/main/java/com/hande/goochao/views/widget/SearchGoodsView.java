package com.hande.goochao.views.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.JSONObjectCallback;
import com.hande.goochao.commons.http.Params;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.NumberUtils;
import com.hande.goochao.utils.PriceUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.NewProductInformationActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;
import com.hande.goochao.views.components.ZoomView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Wangem on 2018/3/21.
 */

public class SearchGoodsView extends LinearLayout implements LoadFailView.OnReloadListener, View.OnClickListener {

    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.no_data_view)
    private NoDataView noDataView;
    @ViewInject(R.id.refresh_layout)
    private SmartRefreshLayout refreshLayout;
    @ViewInject(R.id.goods_grid_view)
    private GoochaoGridView goodsGridView;

    @ViewInject(R.id.search_goods_loading)
    private LoadingView progressBar;

    private String keyword;
    private int pageIndex;
    private int pageSize;
    private List<JSONObject> goodsList = new ArrayList<>();
    private boolean firstLoad;

    private GlideRequests glide;

    private GoodsListAdapter goodsListAdapter;

    public SearchGoodsView(Context context) {
        super(context);
        init();
    }

    public SearchGoodsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SearchGoodsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SearchGoodsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        glide = GlideApp.with(this);
        LayoutInflater.from(getContext()).inflate(R.layout.view_search_goods, this, true);
        x.view().inject(this);

        noDataView.setImageAndText(R.mipmap.search_no_result, "世界上最遥远的距离\n莫过于你知道我而我不知道你");
        loadFailView.setOnReloadListener(this);
        goodsListAdapter = new GoodsListAdapter();
        goodsGridView.setAdapter(goodsListAdapter);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                reload();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                searchGoods();
            }
        });
    }

    public void search(String keyword) {
        this.keyword = keyword;

        noDataView.setVisibility(GONE);
        loadFailView.setVisibility(GONE);
        refreshLayout.setVisibility(GONE);

        onReload();
    }

    private void searchGoods() {
        if (firstLoad) {
            progressBar.setVisibility(VISIBLE);
        }

        Map<String, String> params = Params.buildForStr(
                "keyword", keyword,
                "pageIndex", String.valueOf(pageIndex),
                "pageSize", String.valueOf(pageSize));

        HttpRequest.get(AppConfig.SEARCH_GOODS, null, params, new JSONObjectCallback() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                closeRefresh();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    if (pageIndex == 1) {
                        goodsList.clear();
                    }
                    JSONArray data = JsonUtils.getJsonArray(response, "data", null);
                    handleData(data);
                    pageIndex ++;
                } else {
                    showError(true);
                }
            }

            @Override
            public void onError(Throwable ex) {
                showError(false);
            }
        });
    }

    private void handleData(JSONArray data) {
        if (data != null) {
            for (int i = 0; i < data.length(); i++) {
                JSONObject item = JsonUtils.getJsonItem(data, i, null);
                goodsList.add(item);
            }
        }

        if (data.length() < pageSize) {
            goodsGridView.setFooterViewVisibility(View.VISIBLE);
            refreshLayout.setNoMoreData(true);
            refreshLayout.setEnableLoadMore(false);
        }else {
            goodsGridView.setFooterViewVisibility(View.GONE);
            refreshLayout.setNoMoreData(false);
            refreshLayout.setEnableLoadMore(true);
        }

        if (goodsList.isEmpty()) {
            noDataView.setVisibility(VISIBLE);
            refreshLayout.setVisibility(GONE);
        } else {
            noDataView.setVisibility(GONE);
            refreshLayout.setVisibility(VISIBLE);
        }

        goodsListAdapter.notifyDataSetChanged();
    }

    private void closeRefresh() {
        if (firstLoad) {
            firstLoad = false;
            progressBar.setVisibility(GONE);
        }

        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
    }

    private void showError(boolean showError) {
        closeRefresh();
        if (pageIndex == 1) {
            loadFailView.setVisibility(VISIBLE);
        } else {
            if (showError) {
                AlertManager.showErrorToast(getContext(), "服务器繁忙，请稍后重试", false);
            }
        }
    }

    @Override
    public void onReload() {
        loadFailView.setVisibility(GONE);
        firstLoad = true;
        reload();
    }

    private void reload() {
        pageIndex = 1;
        pageSize = 20;
        refreshLayout.setNoMoreData(false);
        searchGoods();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getContext(), NewProductInformationActivity.class);
        intent.putExtra("goodsId", (String) v.getTag());
        getContext().startActivity(intent);
    }

    class GoodsListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return goodsList.size();
        }

        @Override
        public JSONObject getItem(int position) {
            return goodsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                // 获得容器
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_search_goods_view_item, parent, false);

                viewHolder = new ViewHolder();
                // 初始化组件
                viewHolder.productImage = convertView.findViewById(R.id.produceImage);
                viewHolder.productTitle = convertView.findViewById(R.id.produceTitle);
                viewHolder.productPrice = convertView.findViewById(R.id.producePrice);
                viewHolder.productIntroduce = convertView.findViewById(R.id.produceIntroduce);
                viewHolder.goodsListDataLayout = convertView.findViewById(R.id.goods_list_data_layout);
                viewHolder.discountView = convertView.findViewById(R.id.discount_view);
                viewHolder.saleOutView = convertView.findViewById(R.id.sale_out_view);
                viewHolder.productOldPrice = convertView.findViewById(R.id.old_price);
                viewHolder.goodsListDataLayout.setOnClickListener(SearchGoodsView.this);
                // 给convertHolder附加一个对象
                convertView.setTag(viewHolder);
            } else {
                // 取得convertHolder附加的对象
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // 给组件设置资源
            JSONObject item = getItem(position);

            double itemValue = WindowUtils.getDeviceWidth(getContext()) * 0.35;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.productImage.getLayoutParams();
            params.width = (int) itemValue;
            params.height = (int) itemValue;

            String cover = JsonUtils.getString(item, "detail.cover", "");
            ImageUtils.loadImage(glide, cover + "?imageView2/1/w/320/h/320", viewHolder.productImage, -1);
            viewHolder.productTitle.setText(JsonUtils.getString(item, "title", ""));

            //判断商品类型（满减 折扣 原价）
            String tagValue = JsonUtils.getString(item,"detail.discountTag","");
            if (tagValue.equals("")){
                viewHolder.discountView.setVisibility(View.GONE);
                viewHolder.saleOutView.setVisibility(View.INVISIBLE);
                viewHolder.productOldPrice.setVisibility(View.GONE);

                double price = JsonUtils.getDouble(item , "dynamicId.price" , 0) / 100f;
                double priceValue = NumberUtils.decimalDouble(price);
                viewHolder.productPrice.setText("¥" + PriceUtils.beautify(priceValue));
            }                //原价 无活动
            else{
                if (JsonUtils.getInt(item,"detail.discountType",0) == 2){
                    viewHolder.discountView.setVisibility(View.VISIBLE);
                    viewHolder.saleOutView.setVisibility(View.GONE);
                    viewHolder.discountView.setText(tagValue);

                    viewHolder.productOldPrice.setVisibility(View.VISIBLE);

                    double oldPrice = JsonUtils.getDouble(item , "dynamicId.price" , 0) / 100f;
                    double oldPriceValue = NumberUtils.decimalDouble(oldPrice);
                    viewHolder.productOldPrice.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG); //原价设置中划线
                    viewHolder.productOldPrice.setText("¥" + PriceUtils.beautify(oldPriceValue));

                    double newPrice = JsonUtils.getDouble(item , "dynamicId.discountPrice" , 0) / 100f;
                    double newPriceValue = NumberUtils.decimalDouble(newPrice);
                    viewHolder.productPrice.setText("¥" + PriceUtils.beautify(newPriceValue));
                }             //折扣
                else {
                    viewHolder.discountView.setVisibility(View.GONE);
                    viewHolder.saleOutView.setVisibility(View.VISIBLE);
                    viewHolder.productOldPrice.setVisibility(View.GONE);
                    viewHolder.saleOutView.setText(tagValue);
                    double price = JsonUtils.getDouble(item , "dynamicId.price" , 0) / 100f;
                    double priceValue = NumberUtils.decimalDouble(price);
                    viewHolder.productPrice.setText("¥" + PriceUtils.beautify(priceValue));
                }             //满减
            }

            viewHolder.productIntroduce.setText(JsonUtils.getString(item, "detail.description", ""));
            viewHolder.goodsListDataLayout.setTag(JsonUtils.getString(item, "detail.goodsId", ""));

            return convertView;
        }

        class ViewHolder {
            TextView productTitle;
            TextView productPrice;
            TextView productIntroduce;
            ImageView productImage;
            ZoomView goodsListDataLayout;
            TextView discountView;
            TextView saleOutView;
            TextView productOldPrice;
        }
    }
}
