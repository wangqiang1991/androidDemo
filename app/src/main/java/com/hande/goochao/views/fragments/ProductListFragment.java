package com.hande.goochao.views.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.NumberUtils;
import com.hande.goochao.utils.PriceUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.NewProductInformationActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;
import com.hande.goochao.views.components.ZoomView;
import com.hande.goochao.views.widget.GoochaoGridView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ContentView(R.layout.fragment_product_list)
public class ProductListFragment extends BaseFragment {
    @ViewInject(R.id.productGridView_fragment)
    private GoochaoGridView productGridView;

    private boolean firstLoad ;
    private boolean loaded = false;

    private int pageIndex = 1;
    private int pageSize = 20;
    private String firstCategoryId;
    private String secondCategoryId;

    private JSONArray productList = new JSONArray();
    private List<JSONObject> dataList = new ArrayList<>();

    @ViewInject(R.id.product_fragment_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.product_list_refresh_layout)
    private RefreshLayout refreshLayout;
    @ViewInject(R.id.load_fail_product)
    private LoadFailView loadFailView;
    @ViewInject(R.id.product_noDataView)
    private NoDataView noDataView;

    private ProductListAdapter productListAdapter;

    private int kWidth;

    private GlideRequests glide;

    //防止多次加载
    private boolean loading = true;

    @SuppressLint("ValidFragment")
    public ProductListFragment(String firstCategoryId, String secondCategoryId) {
        this.firstCategoryId = firstCategoryId;
        this.secondCategoryId = secondCategoryId;
    }

    public ProductListFragment(){}

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(!loaded){
            glide = GlideApp.with(getActivity());
            kWidth = WindowUtils.getDeviceWidth(getActivity());
            productListAdapter = new ProductListAdapter();
            productGridView.setAdapter(productListAdapter);
            firstLoad = true;
            loaded = true;
            noDataView.setImageAndText(R.mipmap.new_product_list, "更多优质商品，敬请期待");
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshLayout) {
                    refreshLayout.setEnableLoadMore(true);
                    if (loading){
                        loading = false;
                        pageIndex = 1;
                        loadProductList();
                    }
                }
            });

            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshLayout) {
                    if (loading){
                        loading = false;
                        loadProductList();
                    }
                }
            });

            loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
                @Override
                public void onReload() {
                    loadFailView.setReloading();
                    if (loading){
                        loading = false;
                        loadProductList();
                    }
                }
            });

        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // TODO Auto-generated method stub
        if (isVisibleToUser) {
            if (loading){
                loading = false;
                loadProductList();
            }
            //fragment可见时加载数据，用一个方法来实现
        } else {
            //不可见时不执行操作
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void showError() {
        loadingView.setVisibility(View.GONE);
        loadFailView.setVisibility(View.VISIBLE);
        AlertManager.showErrorInfo(this.getActivity());
    }

    //查询商品列表
    private void loadProductList() {
        if(firstLoad){
            loadingView.setVisibility(View.VISIBLE);
        }
        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", "" + pageIndex);
        params.put("pageSize",""+ pageSize);
        if (!TextUtils.isEmpty(firstCategoryId)) {
            params.put("firstCategoryId", firstCategoryId);
        }
        if (!TextUtils.isEmpty(secondCategoryId)) {
            params.put("secondCategoryId", secondCategoryId);
        }
        HttpRequest.get(AppConfig.PRODUCT_LIST, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
                loading = true;
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
                loadFailView.setFinishReload();
            }

            @Override
            public void onSuccess(JSONObject response) {
                AppLog.i(response.toString());
                if (pageIndex == 1) {
                    productList = new JSONArray();
                }
                if (JsonUtils.getCode(response) == 0) {
                    loadFailView.setVisibility(View.GONE);
                    JSONArray data = JsonUtils.getJsonArray(response, "data", null);

                    for (int i = 0; i < data.length(); i++) {
                        try {
                            productList.put(data.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    System.out.print(productList);
                    if(data.length() <  pageSize){
                        productGridView.setFooterViewVisibility(View.VISIBLE);
                        refreshLayout.setNoMoreData(true);
                        refreshLayout.setEnableLoadMore(false);
                    }else {
                        productGridView.setFooterViewVisibility(View.GONE);
                        refreshLayout.setNoMoreData(false);
                        refreshLayout.setEnableLoadMore(true);

                    }
                    if( data.length() == 0 && pageIndex == 1 ){
                        refreshLayout.setEnableRefresh(false);
                    }
                    if (productList != null) {
                        for (int i = 0; i < productList.length(); i++) {
                            try {
                                dataList.add(productList.getJSONObject(i));
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
                    pageIndex++;
                    productListAdapter.notifyDataSetChanged();
                    firstLoad = false;
                } else {
                    loadingView.setVisibility(View.GONE);
                    AlertManager.showErrorInfo(getActivity());
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
                AlertManager.showErrorToast(getActivity(), "服务器繁忙，请稍后重试", false);
            }
        });
    }

    class ProductListAdapter extends BaseAdapter {

        class ProductListViewHolder {
            TextView productTitle;
            TextView productPrice;
            TextView productIntroduce;
            TextView productOldPrice;
            ImageView productImage;
            ZoomView categoryGoodsZoomView;
            TextView discountView;
            TextView saleOutView;

            ProductListViewHolder(View view) {
               productTitle = view.findViewById(R.id.produceTitle);
               productPrice = view.findViewById(R.id.producePrice);
               productIntroduce = view.findViewById(R.id.produceIntroduce);
               productOldPrice = view.findViewById(R.id.old_price);
               productImage = view.findViewById(R.id.produceImage);
               categoryGoodsZoomView = view.findViewById(R.id.category_goods_zoom_view);
               discountView = view.findViewById(R.id.discount_view);
               saleOutView = view.findViewById(R.id.sale_out_view);
            }
        }
        @Override
        public int getCount() {
            return productList == null ? 0 : productList.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(productList, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            int heightInt = (int) (kWidth * 0.5 * 243 / 188);
            int value = kWidth *  141 / 375;

            if (convertView == null) {
                // 获得容器
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_shopping_mall_product_item, null);
                convertView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT , heightInt));

                convertView.setTag(new ProductListViewHolder(convertView));

            }
                // 取得converHolder附加的对象
            ProductListViewHolder viewHolder = (ProductListViewHolder)convertView.getTag();

            // 初始化组件
            TextPaint paint = viewHolder.productTitle.getPaint();
            paint.setFakeBoldText(true);

            // 给组件设置资源
            JSONObject item = getItem(i);

            ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(item, "cover", ""),500,500), viewHolder.productImage, -1);
//            x.image().bind(viewHolder.productImage,ImageUtils.resize(JsonUtils.getString(item, "cover", ""),500,500));

            //判断商品类型（满减 折扣 原价）
            String tagValue = JsonUtils.getString(getItem(i),"discountTag","");
            if (tagValue.equals("")){
                viewHolder.discountView.setVisibility(View.GONE);
                viewHolder.saleOutView.setVisibility(View.INVISIBLE);
                viewHolder.productOldPrice.setVisibility(View.GONE);

                double price = JsonUtils.getDouble(item , "minPrice" , 0);
                double priceValue = NumberUtils.decimalDouble(price);
                viewHolder.productPrice.setText("¥" + PriceUtils.beautify(priceValue));
            }                //原价 无活动
            else{
                if (JsonUtils.getInt(getItem(i),"discountType",0) == 2){
                    viewHolder.discountView.setVisibility(View.VISIBLE);
                    viewHolder.saleOutView.setVisibility(View.GONE);
                    viewHolder.discountView.setText(tagValue);

                    viewHolder.productOldPrice.setVisibility(View.VISIBLE);

                    double oldPrice = JsonUtils.getDouble(item , "minPrice" , 0);
                    double oldPriceValue = NumberUtils.decimalDouble(oldPrice);
                    viewHolder.productOldPrice.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG); //原价设置中划线
                    viewHolder.productOldPrice.setText("¥" + PriceUtils.beautify(oldPriceValue));

                    double newPrice = JsonUtils.getDouble(item , "discountPrice" , 0);
                    double newPriceValue = NumberUtils.decimalDouble(newPrice);
                    viewHolder.productPrice.setText("¥" + PriceUtils.beautify(newPriceValue));
                }             //折扣
                else {
                    viewHolder.discountView.setVisibility(View.GONE);
                    viewHolder.saleOutView.setVisibility(View.VISIBLE);
                    viewHolder.productOldPrice.setVisibility(View.GONE);
                    viewHolder.saleOutView.setText(tagValue);
                    double price = JsonUtils.getDouble(item , "minPrice" , 0);
                    double priceValue = NumberUtils.decimalDouble(price);
                    viewHolder.productPrice.setText("¥" + PriceUtils.beautify(priceValue));
                }             //满减
            }
            WindowUtils.boldMethod(viewHolder.productPrice);

            viewHolder.productImage.setLayoutParams(new LinearLayout.LayoutParams(value , value));

            viewHolder.productTitle.setText(JsonUtils.getString(item, "title", ""));

            viewHolder.productIntroduce.setText(JsonUtils.getString(item, "description", "无"));
            final String goodsId = JsonUtils.getString(item, "goodsId", "");

            viewHolder.categoryGoodsZoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), NewProductInformationActivity.class);
                    intent.putExtra("goodsId", goodsId);
                    getActivity().startActivity(intent);
                }
            });

            return convertView;
        }
    }

}
