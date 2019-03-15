package com.hande.goochao.views.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.NumberUtils;
import com.hande.goochao.utils.PriceUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.NewProductInformationActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.ConfirmDialog;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.HeaderGridView;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;
import com.hande.goochao.views.components.ZoomView;
import com.hande.goochao.views.components.refresh.NoDataTwoLineView;
import com.hande.goochao.views.widget.GoochaoGridView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
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

/**
 * Created by LMC on 2018/3/15.
 */

@ContentView(R.layout.fragment_collection_goods)
public class CollectionGoodsFragment extends BaseFragment {

    @ViewInject(R.id.collection_goods_grid_view)
    private GoochaoGridView goodsGridView;
    private CollectionGoodsAdapter goodsListAdapter;

    private JSONArray goodsResource;
    private JSONArray goodsList = new JSONArray();

    @ViewInject(R.id.collection_goods_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.collection_goods_load_fail)
    private LoadFailView loadFailView;
    @ViewInject(R.id.collection_goods_noDataView)
    private NoDataTwoLineView noDataView;

    private boolean firstLoad = true;
    private boolean loaded = false;

    private int kWidth;

    private GlideRequests glide;

    private CustomLoadingDialog loadingDialog;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!loaded) {
            glide = GlideApp.with(getActivity());
            goodsListAdapter = new CollectionGoodsAdapter();
            goodsGridView.setAdapter(goodsListAdapter);

            kWidth = WindowUtils.getDeviceWidth(getActivity());

            loadingDialog = new CustomLoadingDialog(getActivity());

            noDataView.setImageAndText(R.mipmap.new_start_nodata, "收藏空空如也", "快去逛逛商品吧");

            loaded = true;

            loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
                @Override
                public void onReload() {
                    loadGoods();
                }
            });
            loadGoods();

        }
    }

    private void loadGoods() {
        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        }
        HttpRequest.get(AppConfig.COLLECTION_GOODS, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    goodsResource = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < goodsResource.length(); i++) {
                        try {
                            goodsList.put(goodsResource.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if (goodsResource.length() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                        goodsGridView.setVisibility(View.GONE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                        goodsGridView.setVisibility(View.VISIBLE);
                    }
                    goodsListAdapter.notifyDataSetChanged();
                    firstLoad = false;
                    resetView();
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(getActivity(), "服务器繁忙，请稍后重试", false);
                showError();
            }
        });
    }

    class CollectionGoodsAdapter extends BaseAdapter {

        class CollectionGoodsViewHolder {
            TextView productTitle;
            TextView productPrice;
            TextView productIntroduce;
            TextView productOldPrice;
            ImageView productImage;
            ZoomView categoryGoodsZoomView;
            TextView discountView;
            TextView saleOutView;

            CollectionGoodsViewHolder(View view) {
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
            return goodsList == null ? 0 : goodsList.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(goodsList, i, null);
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

                convertView.setTag(new CollectionGoodsViewHolder(convertView));

            }
            // 取得converHolder附加的对象
            CollectionGoodsViewHolder viewHolder = (CollectionGoodsViewHolder)convertView.getTag();

            // 初始化组件
            TextPaint paint = viewHolder.productTitle.getPaint();
            paint.setFakeBoldText(true);

            // 给组件设置资源
            JSONObject object = getItem(i);
            JSONObject item = JsonUtils.getJsonObject(object,"goodsSimpleDetailVo",null);

            ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(item, "cover", ""),500,500), viewHolder.productImage, -1);

            //判断商品类型（满减 折扣 原价）
            String tagValue = JsonUtils.getString(item,"discountTag","");
            if (tagValue.equals("")){
                viewHolder.discountView.setVisibility(View.GONE);
                viewHolder.saleOutView.setVisibility(View.INVISIBLE);
                viewHolder.productOldPrice.setVisibility(View.GONE);

                double price = JsonUtils.getDouble(item , "minPrice" , 0);
                double priceValue = NumberUtils.decimalDouble(price);
                viewHolder.productPrice.setText("¥" + PriceUtils.beautify(priceValue));
            }                //原价 无活动
            else{
                if (JsonUtils.getInt(item,"discountType",0) == 2){
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

            viewHolder.productIntroduce.setText(JsonUtils.getString(item, "description", ""));
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

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
        } else {
            AlertManager.showErrorInfo(this.getActivity());
            loadingView.setVisibility(View.GONE);
        }
    }
}
