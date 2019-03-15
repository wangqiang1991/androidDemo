package com.hande.goochao.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.hande.goochao.MainActivity;
import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.config.AppConst;
import com.hande.goochao.session.AppSession;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.ProductInformationActivity;
import com.hande.goochao.views.activity.ProductListActivity;
import com.hande.goochao.views.activity.SearchActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.HeaderGridView;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.ZoomView;
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

import java.util.HashMap;
import java.util.Map;

@ContentView(R.layout.fragment_shopping_mall)
public class ShoppingMallFragment extends BaseFragment {
    private GridView gridView;
    private HeaderGridView productGridView;
    private int pageIndex = 1;
    private int pageSize = 20;

    private boolean below = false;

    @ViewInject(R.id.recommend_refresh_layout)
    private RefreshLayout refreshLayout;
    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    private TextView recommendText;
    private CategoryAdapter categoryAdapter;
    private RecommendAdapter productAdapter;

    private AnimationProgressBar animationProgressBar;

    private JSONArray categoryList;
    private JSONArray recommendList = new JSONArray();
    private boolean firstLoad;
    private boolean loaded = false;

    private int windowX;

    private GlideRequests glide;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("商城");
        ((MainActivity) getActivity()).setSearchType(SearchActivity.SEARCH_GOODS);
        if(!loaded){
            glide = GlideApp.with(getActivity());
            windowX = WindowUtils.getDeviceWidth(getActivity());

            productGridView = getActivity().findViewById(R.id.productGridView);

            productAdapter = new RecommendAdapter();
            categoryAdapter = new CategoryAdapter();

            View hv = LayoutInflater.from(this.getActivity()).inflate(R.layout.fragment_shopping_mall_header_view, productGridView, false);
            productGridView.addHeaderView(hv);
            gridView = hv.findViewById(R.id.categroyMainview);
            recommendText = hv.findViewById(R.id.recommendTextview);
            gridView.setAdapter(categoryAdapter);

            productGridView.setAdapter(productAdapter);
            firstLoad = true;

            animationProgressBar = new AnimationProgressBar(this.getActivity());
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                public void onRefresh(RefreshLayout refreshLayout) {
                    pageIndex = 1;
                    loadCategory();
                    below = false;
                }
            });
            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshLayout) {
                    if (below){
                        AlertManager.toast(getActivity(), " 已经是最底部了哦~ ");
                        refreshLayout.finishLoadMore();
                    }else {
                        pageIndex++;
                        loadRecommend();
                    }
                }
            });

            loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
                @Override
                public void onReload() {
                    loadCategory();
                    //loadRecommend();
                }
            });
            loadCategory();
            loaded = true;
        }
    }

    // 查询类目列表
    private void loadCategory() {
        if(firstLoad){
            animationProgressBar.show();
        }
        HttpRequest.get(AppConfig.CATEGORY_LIST, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                animationProgressBar.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    categoryList = JsonUtils.getJsonArray(response, "data", null);
                    AppSession.getInstance().put(AppConst.CATEGORY_LIST_SESSION,categoryList);
                    resetView();
                    loadRecommend();
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(getContext(), "服务器繁忙，请稍后重试",false);
                AppLog.e("err", ex);
                showError();
            }
        });
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    // 查询推荐商品
    private void loadRecommend() {

        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", "" + pageIndex);
        params.put("pageSize",""+ pageSize);
        HttpRequest.get(AppConfig.RECOMMEND_PRODUCT, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                animationProgressBar.dismiss();
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
            }

            @Override
            public void onSuccess(JSONObject response) {
                AppLog.i(response.toString());
                if (pageIndex == 1) {
                    recommendList = new JSONArray();
                }
                if (JsonUtils.getCode(response) == 0) {
                    JSONArray datas = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < datas.length(); i++) {
                        try {
                            recommendList.put(datas.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if(datas.length() <  pageSize){
                        below = true;
                    }else {
                        below = false;
                    }
                    categoryAdapter.notifyDataSetChanged();
                    productAdapter.notifyDataSetChanged();
                    recommendText.setVisibility(View.VISIBLE);
                    firstLoad = false;
                    resetView();
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
            }
        });
    }


    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
        } else {
            refreshLayout.finishRefresh();
            refreshLayout.finishLoadMore();
            AlertManager.showErrorInfo(this.getActivity());
        }
    }

    // 商品类目列表
    class CategoryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return categoryList == null ? 0 : categoryList.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(categoryList, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            CategoryViewHolder viewHolder = null;

            if (convertView == null) {

                viewHolder = new CategoryViewHolder();
                // 获得容器
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_shopping_mall_category_item, null);
                double width;
                double height;
                width = windowX / 3;
                height = width ;
                int widthInt = (int) width;
                int heightInt = (int) height * 11 / 8;
                convertView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , heightInt));

                // 初始化组件
                viewHolder.image = convertView.findViewById(R.id.image);
                viewHolder.title = convertView.findViewById(R.id.title);
                viewHolder.categoryZoomView =convertView.findViewById(R.id.category_zoom_view);
                // 给converHolder附加一个对象
                convertView.setTag(viewHolder);
            } else {
                // 取得converHolder附加的对象
                viewHolder = (CategoryViewHolder) convertView.getTag();
            }

            double imgWidth;
            double imgHeight;
            imgWidth = windowX / 4;
            imgHeight = imgWidth;
            int imgWidthInt = (int) imgWidth;
            int imgHeightInt = (int) imgHeight * 11 / 8;

            // 给组件设置资源
            JSONObject item = getItem(i);
            ImageUtils.loadImage(glide, ImageUtils.zoomResize( JsonUtils.getString(item, "image", ""),imgWidthInt,imgHeightInt), viewHolder.image, R.mipmap.loadpicture);


            viewHolder.title.setText(JsonUtils.getString(item, "name", ""));
            final String categoryId =  JsonUtils.getString(item, "categoryId", "");

            viewHolder.categoryZoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ProductListActivity.class);
                    intent.putExtra("categoryId", categoryId);
                    intent.putExtra("categoryList", categoryList.toString());
                    getActivity().startActivity(intent);
                }
            });

            return convertView;
        }
    }

    // 推荐商品
    class RecommendAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return recommendList == null ? 0 : recommendList.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(recommendList, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            RecommendViewHolder viewHolder = null;

            if (convertView == null) {

                viewHolder = new RecommendViewHolder();
                // 获得容器
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_shopping_mall_product_item, null);
                double width;
                double height;
                width = windowX  * 0.5;
                height = width * 11 / 9;
                int widthInt = (int) width;
                int heightInt = (int) height;
                convertView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , heightInt));

                // 初始化组件
                viewHolder.productImage = convertView.findViewById(R.id.produceImage);
                viewHolder.productTitle = convertView.findViewById(R.id.produceTitle);
                viewHolder.productPrice = convertView.findViewById(R.id.producePrice);
                viewHolder.productIntroduce = convertView.findViewById(R.id.produceIntroduce);
                viewHolder.categoryGoodsZoomView = convertView.findViewById(R.id.category_goods_zoom_view);
                // 给converHolder附加一个对象
                convertView.setTag(viewHolder);
            } else {
                // 取得converHolder附加的对象
                viewHolder = (RecommendViewHolder) convertView.getTag();
            }

            // 给组件设置资源
            JSONObject item = getItem(i);

            double width;
            double height;
            width = windowX * 0.4 ;
            height = width * 11 / 15;
            int widthInt = (int) width;
            int heightInt = (int) height;

            ImageUtils.loadImage(glide, ImageUtils.zoomResize(JsonUtils.getString(item, "cover", ""),widthInt,heightInt), viewHolder.productImage, R.mipmap.loadpicture);

            viewHolder.productTitle.setText(JsonUtils.getString(item, "title", ""));
            String price = "¥"+JsonUtils.getString(item, "minPrice", "");
            if (price.endsWith(".0")) {
                price = price.replace(".0", "");
            }
            viewHolder.productPrice.setText(price);
            viewHolder.productIntroduce.setText(JsonUtils.getString(item, "description", ""));

            final String goodsId = JsonUtils.getString(item, "goodsId", "");
            viewHolder.categoryGoodsZoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ProductInformationActivity.class);
                    intent.putExtra("goodsId", goodsId);
                    getActivity().startActivity(intent);
                }
            });

            return convertView;
        }
    }

    public class CategoryViewHolder {
        public TextView title;
        public ImageView image;
        ZoomView categoryZoomView;
    }

    class RecommendViewHolder {
        public TextView productTitle;
        public TextView productPrice;
        public TextView productIntroduce;
        public ImageView productImage;
        ZoomView categoryGoodsZoomView;
    }
}