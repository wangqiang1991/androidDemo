package com.hande.goochao.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
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
import com.hande.goochao.views.activity.AfterSaleDetailActivity;
import com.hande.goochao.views.activity.ApplyAfterSaleActivity;
import com.hande.goochao.views.activity.FinishedOrderDetailActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;
import com.hande.goochao.views.widget.GoochaoListView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

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
 * Created by Wangenmao on 2018/3/14.
 */
@ContentView(R.layout.fragment_pending_finished)
public class FinishedOrderListFragment extends BaseFragment implements LoadFailView.OnReloadListener, OnRefreshListener, OnLoadMoreListener {


    @ViewInject(R.id.listView)
    private GoochaoListView listView;
    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.finish_order_loading)
    private LoadingView loadingView;
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
            adapter = new FinishedOrderListFragment.OrderAdapter();
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    JSONObject dataObject = dataList.get(position);
                    Intent intent = new Intent(getActivity(), FinishedOrderDetailActivity.class);
                    intent.putExtra("jsonOrderStr", dataObject.toString());
                    startActivity(intent);
                }
            });

            noDataView.setImageAndText(R.mipmap.no_order, "此类订单暂无内容");

            loadFailView.setOnReloadListener(this);
            refreshLayout.setOnRefreshListener(this);
            refreshLayout.setOnLoadMoreListener(this);
            inflater = LayoutInflater.from(getActivity());

            loadData(true, 1);

        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (!EventBus.getDefault().isRegistered(this)){
                EventBus.getDefault().register(this);
            }
            if (created){
                loadData(true, 1);
            }
            //相当于Fragment的onResume
        } else {
            //相当于Fragment的onPause
        }
    }

    private void loadData(boolean refresh, final int loadPageIndex) {
        if (refresh && dataList.size() == 0) {
            loadingView.setVisibility(View.VISIBLE);
        }

        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", loadPageIndex + "");
        params.put("pageSize", pageSize + "");
        params.put("orderStatus", "5");

        HttpRequest.get(AppConfig.Order_List, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
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
                        listView.setFooterViewVisibility(View.VISIBLE);
                        refreshLayout.setNoMoreData(true);
                        refreshLayout.setEnableLoadMore(false);
                    }else {
                        listView.setFooterViewVisibility(View.GONE);
                        refreshLayout.setNoMoreData(false);
                        refreshLayout.setEnableLoadMore(true);
                    }

                    if (dataList.size() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
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
                AlertManager.showErrorToast(getActivity(), "服务器繁忙，请稍后重试", false);
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
            Button btnRefunds;
            Button btnPreviewAfterSale;
            Button btnExpress;
            Button btnReceive;
            Button btnApplyAfterService;

            public ViewHolder(View convertView) {
                txtStatus = convertView.findViewById(R.id.txtStatus);
                goodsContainer = convertView.findViewById(R.id.goodsContainer);
                layAmount = convertView.findViewById(R.id.layAmount);
                txtCount = convertView.findViewById(R.id.txtCount);
                txtAmount = convertView.findViewById(R.id.txtAmount);
                layButtons = convertView.findViewById(R.id.layButtons);
                btnRefunds = convertView.findViewById(R.id.btnRefunds);
                btnPreviewAfterSale = convertView.findViewById(R.id.btnPreviewAfterSale);
                btnApplyAfterService = convertView.findViewById(R.id.btnApplyAfterService);
                btnExpress = convertView.findViewById(R.id.btnExpress);
                btnReceive = convertView.findViewById(R.id.btnReceive);
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
                convertView = inflater.inflate(R.layout.list_order_item, parent, false);
                convertView.setTag(new FinishedOrderListFragment.OrderAdapter.ViewHolder(convertView));
            }
            FinishedOrderListFragment.OrderAdapter.ViewHolder viewHolder = (FinishedOrderListFragment.OrderAdapter.ViewHolder) convertView.getTag();
            viewHolder.txtStatus.setText("订单已完成");

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
                TextView txtGoodsSub = goodsItemView.findViewById(R.id.txtGoodsSub);
                String styleName = JsonUtils.getString(goodsObject, "styleName", "").trim();
                String subName = JsonUtils.getString(goodsObject, "subName", "").trim();
                txtGoodsStyle.setText(styleName);
                txtGoodsSub.setText(subName);
                WindowUtils.boldMethod(txtGoodsStyle);
                WindowUtils.boldMethod(txtGoodsSub);

                TextView txtGoodsPrice = goodsItemView.findViewById(R.id.txtGoodsPrice);
                double stylePrice = JsonUtils.getDouble(goodsObject, "stylePrice", 0);
                txtGoodsPrice.setText("¥" + DoubleUtils.format(stylePrice));
                WindowUtils.boldMethod(txtGoodsPrice);

                TextView txtGoodsCount = goodsItemView.findViewById(R.id.txtGoodsCount);
                int amount = JsonUtils.getInt(goodsObject, "amount", 0);
                txtGoodsCount.setText("× " + amount);
                WindowUtils.boldMethod(txtGoodsCount);

                viewHolder.goodsContainer.addView(goodsItemView);
            }

            double money = JsonUtils.getDouble(dataObject, "money", 0);
            double expressMoney = JsonUtils.getDouble(dataObject, "expressMoney", 0);
            int amount = JsonUtils.getInt(dataObject, "amount", 0);
            int afterApplyCount = JsonUtils.getInt(dataObject,"afterApplyCount",0);

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

            viewHolder.btnRefunds.setVisibility(View.GONE);
            viewHolder.btnPreviewAfterSale.setVisibility(View.GONE);
            viewHolder.btnExpress.setVisibility(View.GONE);
            viewHolder.btnReceive.setVisibility(View.GONE);


            int afterSaleType = JsonUtils.getInt(dataObject, "afterSaleType", 0);
            int applyStatus = JsonUtils.getInt(dataObject, "applyStatus", 0);

            if ((amount - afterApplyCount) > 0) {
                viewHolder.btnApplyAfterService.setVisibility(View.VISIBLE);
            } else {
                viewHolder.btnApplyAfterService.setVisibility(View.GONE);

                if (afterSaleType == 2) {
//                    if (applyStatus == 1) {
//                        viewHolder.txtStatus.setText("已申请换货");
//                    } else if (applyStatus == 2) {
//                        viewHolder.txtStatus.setText("商家拒绝换货");
//                    } else if (applyStatus == 3) {
//                        viewHolder.txtStatus.setText("商家待收货");
//                    } else if (applyStatus == 4) {
//                        viewHolder.txtStatus.setText("商家拒绝收货");
//                    } else if (applyStatus == 5) {
//                        viewHolder.txtStatus.setText("商家已确认收货");
//                    } else if (applyStatus == 6) {
//                        viewHolder.txtStatus.setText("客户待收货");
//                    } else if (applyStatus == 7) {
//                        viewHolder.txtStatus.setText("换货完成");
//                    }
                    viewHolder.txtStatus.setText("订单已完成");
                }
//                else if (afterSaleType == 3) {
//                    if (applyStatus == 1) {
//                        viewHolder.txtStatus.setText("已申请退货");
//                    } else if (applyStatus == 2) {
//                        viewHolder.txtStatus.setText("商家拒绝退货");
//                    } else if (applyStatus == 3) {
//                        viewHolder.txtStatus.setText("商家待收货");
//                    } else if (applyStatus == 4) {
//                        viewHolder.txtStatus.setText("商家拒绝退款");
//                    } else if (applyStatus == 5) {
//                        viewHolder.txtStatus.setText("商家已确认收货");
//                    } else if (applyStatus == 6) {
//                        viewHolder.txtStatus.setText("退款中");
//                    } else if (applyStatus == 7) {
//                        viewHolder.txtStatus.setText("退款成功");
//                    } else if (applyStatus == 8) {
//                        viewHolder.txtStatus.setText("退款失败");
//                    }
//                }
            }

            int orderType = JsonUtils.getInt(dataObject, "orderType", 0);
            if (orderType == 4) {
                viewHolder.txtStatus.setText(viewHolder.txtStatus.getText().toString() + "(" + "换货" + ")");
            }

            viewHolder.btnApplyAfterService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnApplyAfterSaleClick(dataObject);
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

    private void btnApplyAfterSaleClick(JSONObject dataObject) {
        String orderNumber = JsonUtils.getString(dataObject, "orderNumber", "");
        Intent intent = new Intent(getActivity(), ApplyAfterSaleActivity.class);
        intent.putExtra("title", "申请售后");
        intent.putExtra("orderNumber", orderNumber);
        startActivity(intent);
    }

    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        super.onDetach();
    }

    @Subscribe
    public void onEvent(EventBusNotification notification) {
        if (notification.getKey().equals(EventBusNotification.event_bus_refresh_order_list)) {
            loadData(true, 1);
        }
    }

}
