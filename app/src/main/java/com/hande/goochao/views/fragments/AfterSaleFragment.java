package com.hande.goochao.views.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.DoubleUtils;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.AfterSaleActivity;
import com.hande.goochao.views.activity.AfterSaleDetailActivity;
import com.hande.goochao.views.activity.OrderCommentsActivity;
import com.hande.goochao.views.activity.SpaceListActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;
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

/**
 * Created by Wangenmao on 2018/3/16.
 */
@ContentView(R.layout.fragment_order_comments)
public class AfterSaleFragment extends BaseFragment implements LoadFailView.OnReloadListener, OnRefreshListener, OnLoadMoreListener {


    private int type;

    public AfterSaleFragment() {}

    @SuppressLint("ValidFragment")
    public AfterSaleFragment(int type) {
        this.type = type;
    }

    @ViewInject(R.id.after_sale_fragment_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.listView)
    private ListView listView;
    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.noDataView)
    private NoDataView noDataView;
    @ViewInject(R.id.refresh_layout)
    private RefreshLayout refreshLayout;

    private boolean created = false;
    private BaseAdapter adapter;
    private int pageIndex = 1;
    private int pageSize = 20;
    private LayoutInflater inflater;

    private List<JSONObject> dataList = new ArrayList<>();

    private GlideRequests glide;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!created) {
            glide = GlideApp.with(getActivity());
            created = true;
            adapter = new AfterSaleFragment.OrderAdapter();
            listView.setAdapter(adapter);

            noDataView.setImageAndText(R.mipmap.no_order, "此类订单暂无内容");

            loadFailView.setOnReloadListener(this);
            refreshLayout.setOnRefreshListener(this);
            refreshLayout.setOnLoadMoreListener(this);
            inflater = LayoutInflater.from(getActivity());

            loadData(true, 1);
        }
    }


    private void loadData(boolean refresh, final int loadPageIndex) {
        if (refresh && dataList.size() == 0) {
           loadingView.setVisibility(View.VISIBLE);
        }

        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", loadPageIndex + "");
        params.put("pageSize", pageSize + "");

        if (type == AfterSaleActivity.AFTERSALE_PROCESSING) {
            params.put("queryCompletedAfterSale", "false");
        } else {
            params.put("queryCompletedAfterSale", "true");
        }


        HttpRequest.get(AppConfig.After_Sale_List, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadComplete();
            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    pageIndex = loadPageIndex;
                    if (pageIndex == 1) {
                        dataList.clear();
                    }

                    JSONArray array = JsonUtils.getJsonArray(response, "data", null);
                    if (array != null) {
                        for (int i = 0; i < array.length(); i++) {
                            try {
                                dataList.add(array.getJSONObject(i));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (array.length() < pageSize) {
                        refreshLayout.setNoMoreData(true);
                        refreshLayout.setEnableLoadMore(false);
                    }

                    if (dataList.size() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                    }
                    pageIndex++;

                } else {
                    if (dataList.size() == 0) {
                        loadFailView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onError(Throwable ex) {
                if (dataList.size() == 0) {
                    loadFailView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadComplete() {
        loadingView.setVisibility(View.GONE);
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
    }

    @Override
    public void onLoadMore(RefreshLayout refreshLayout) {
        loadData(false, this.pageIndex);
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        loadData(true, 1);
    }

    @Override
    public void onReload() {
        loadFailView.setVisibility(View.GONE);
        loadData(true, 1);
    }

    class OrderAdapter extends BaseAdapter {

        class ViewHolder {

            TextView txtStatus;
            LinearLayout goodsContainer;
            View layAmount;
            TextView txtCount;
            TextView txtAmount;
            View layButtons;
            Button btnPreviewAfterSale;

            public ViewHolder(View convertView) {
                txtStatus = convertView.findViewById(R.id.txtStatus);
                goodsContainer = convertView.findViewById(R.id.goodsContainer);
                layAmount = convertView.findViewById(R.id.layAmount);
                txtCount = convertView.findViewById(R.id.txtCount);
                txtAmount = convertView.findViewById(R.id.txtAmount);
                layButtons = convertView.findViewById(R.id.layButtons);
                btnPreviewAfterSale = convertView.findViewById(R.id.btnPreviewAfterSale);
            }
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final JSONObject dataObject = (JSONObject) getItem(position);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_order_aftersale_item, parent, false);
                convertView.setTag(new AfterSaleFragment.OrderAdapter.ViewHolder(convertView));
            }
            AfterSaleFragment.OrderAdapter.ViewHolder viewHolder = (AfterSaleFragment.OrderAdapter.ViewHolder) convertView.getTag();
            viewHolder.txtStatus.setText("处理中");

            JSONArray orderGoodsRelations = new JSONArray();
            orderGoodsRelations.put(dataObject);
            viewHolder.goodsContainer.removeAllViews();
            for (int i = 0; i < orderGoodsRelations.length(); i++) {
                JSONObject goodsObject = JsonUtils.getJsonItem(orderGoodsRelations, i, null);
                View goodsItemView = inflater.inflate(R.layout.order_goods_item, null);

                ImageView goodsCover = goodsItemView.findViewById(R.id.goodsCover);
                String cover = JsonUtils.getString(goodsObject, "cover", "");
                ImageUtils.loadImage(glide, ImageUtils.resize(cover,500,500), goodsCover, -1);


                TextView txtGoodsTitle = goodsItemView.findViewById(R.id.txtGoodsTitle);
                txtGoodsTitle.setText(JsonUtils.getString(goodsObject, "title", ""));
                WindowUtils.boldMethod(txtGoodsTitle);

                TextView txtGoodsStyle = goodsItemView.findViewById(R.id.txtGoodsStyle);
                txtGoodsStyle.setText(JsonUtils.getString(goodsObject, "styleName", "").trim());
                WindowUtils.boldMethod(txtGoodsStyle);

                TextView txtGoodsSub = goodsItemView.findViewById(R.id.txtGoodsSub);
                txtGoodsSub.setText(JsonUtils.getString(goodsObject, "subName", "").trim());
                WindowUtils.boldMethod(txtGoodsSub);

                TextView txtGoodsPrice = goodsItemView.findViewById(R.id.txtGoodsPrice);
                double stylePrice = JsonUtils.getDouble(goodsObject, "stylePrice", 0);
                txtGoodsPrice.setText("¥" + DoubleUtils.format(stylePrice));
                WindowUtils.boldMethod(txtGoodsPrice);

                TextView txtGoodsCount = goodsItemView.findViewById(R.id.txtGoodsCount);
                int amount = JsonUtils.getInt(goodsObject, "amount", 0);
                txtGoodsCount.setText("×  " + amount);
                WindowUtils.boldMethod(txtGoodsCount);

                viewHolder.goodsContainer.addView(goodsItemView);
            }

            double money = JsonUtils.getDouble(dataObject, "money", 0);
            double expressMoney = JsonUtils.getDouble(dataObject, "expressMoney", 0);
            int amount = JsonUtils.getInt(dataObject, "amount", 0);

            // 订单金额和订单数量
            viewHolder.txtCount.setText("共" + amount + "件商品");
            WindowUtils.boldMethod(viewHolder.txtCount);
            viewHolder.txtAmount.setText("¥" + DoubleUtils.format(money + expressMoney));
            WindowUtils.boldMethod(viewHolder.txtAmount);

            TextView yunFeiView = convertView.findViewById(R.id.no_use_text);
            if(expressMoney == 0){
                yunFeiView.setText("(免运费)");
            }else {
                yunFeiView.setText("(含运费¥" + DoubleUtils.format(expressMoney) + ")");
            }
            WindowUtils.boldMethod(yunFeiView);
            TextView allView = convertView.findViewById(R.id.all_txt_view);
            allView.setText("共计");
            WindowUtils.boldMethod(allView);


            int afterSaleType = JsonUtils.getInt(dataObject, "afterSaleType", 0);
            int applyStatus = JsonUtils.getInt(dataObject, "applyStatus", 0);

            if (afterSaleType == 1) {
                if (applyStatus == 1) {
                    viewHolder.txtStatus.setText("已申请退款");
                } else if (applyStatus == 2) {
                    viewHolder.txtStatus.setText("商家拒绝退款");
                } else if (applyStatus == 3) {
                    viewHolder.txtStatus.setText("退款中");
                } else if (applyStatus == 4) {
                    viewHolder.txtStatus.setText("退款成功");
                } else if (applyStatus == 5) {
                    viewHolder.txtStatus.setText("退款失败");
                }

            } else if (afterSaleType == 2) {
                if (applyStatus == 1) {
                    viewHolder.txtStatus.setText("已申请换货");
                } else if (applyStatus == 2) {
                    viewHolder.txtStatus.setText("商家拒绝换货");
                } else if (applyStatus == 3) {
                    viewHolder.txtStatus.setText("商家待收货");
                } else if (applyStatus == 4) {
                    viewHolder.txtStatus.setText("商家拒绝收货");
                } else if (applyStatus == 5) {
                    viewHolder.txtStatus.setText("商家已确认收货");
                } else if (applyStatus == 6) {
                    viewHolder.txtStatus.setText("客户待收货");
                } else if (applyStatus == 7) {
                    viewHolder.txtStatus.setText("换货完成");
                }
            } else if (afterSaleType == 3) {
                if (applyStatus == 1) {
                    viewHolder.txtStatus.setText("已申请退货");
                } else if (applyStatus == 2) {
                    viewHolder.txtStatus.setText("商家拒绝退货");
                } else if (applyStatus == 3) {
                    viewHolder.txtStatus.setText("商家待收货");
                } else if (applyStatus == 4) {
                    viewHolder.txtStatus.setText("商家拒绝退款");
                } else if (applyStatus == 5) {
                    viewHolder.txtStatus.setText("商家已确认收货");
                } else if (applyStatus == 6) {
                    viewHolder.txtStatus.setText("退款中");
                } else if (applyStatus == 7) {
                    viewHolder.txtStatus.setText("退款成功");
                } else if (applyStatus == 8) {
                    viewHolder.txtStatus.setText("退款失败");
                }
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAfterSaleInfo(dataObject);
                }
            });

            viewHolder.btnPreviewAfterSale.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAfterSaleInfo(dataObject);
                }
            });

            WindowUtils.boldMethod(viewHolder.txtStatus);
            return convertView;
        }
    }

    private void showAfterSaleInfo(JSONObject orderObject) {
        String relationId = JsonUtils.getString(orderObject, "relationId", "");
        Intent intent = new Intent(getActivity(), AfterSaleDetailActivity.class);
        intent.putExtra("relationId", relationId);
        startActivity(intent);
    }


}
