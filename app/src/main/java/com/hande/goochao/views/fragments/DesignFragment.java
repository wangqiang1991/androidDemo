package com.hande.goochao.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.AddressActivity;
import com.hande.goochao.views.activity.DesignPlanDetailActivity;
import com.hande.goochao.views.activity.PlanListActivity;
import com.hande.goochao.views.activity.SnitchActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;
import com.hande.goochao.views.components.refresh.MySmartRefreshLayout;
import com.hande.goochao.views.components.refresh.NoDataTwoLineView;
import com.hande.goochao.views.widget.GoochaoGridView;
import com.hande.goochao.views.widget.GoochaoListView;
import com.hande.goochao.views.widget.RoundImageView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/9/3.
 */
@ContentView(R.layout.design_inspiration_layout)
public class DesignFragment extends BaseFragment {

    @ViewInject(R.id.design_inspiration_load_fail)
    private LoadFailView loadFailView;
    @ViewInject(R.id.inspiration_design_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.design_inspiration_grid_view)
    private GoochaoListView designListView;
    @ViewInject(R.id.design_refresh_layout)
    private MySmartRefreshLayout refreshLayout;
    @ViewInject(R.id.design_noDataView)
    private NoDataView noDataView;

    private boolean loaded = false;
    private boolean isLoading = false;

    private int pageIndex = 1;
    private int pageSize = 20;

    private DesignAdapter designAdapter;

    private List<JSONObject> dataList = new ArrayList<>();

    private float mLastY;//用户滑动结束时Y坐标
    private int mTouchSlop;//系统认为的最小滑动距离
    private int mLastHeight;

    private GlideRequests glide;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!loaded && !isLoading) {
            glide = GlideApp.with(getActivity());
            loaded = true;

            noDataView.setImageAndText(R.mipmap.search_no_result, "软装方案正在完善中...");

            designAdapter = new DesignAdapter();
            designListView.setAdapter(designAdapter);

            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshLayout) {
                    loadDesignData(false, true);
                }
            });
            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout layout) {
                    loadDesignData(false, false);
                }
            });
            loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
                @Override
                public void onReload() {
                    loadFailView.setReloading();
                    loadDesignData(true, false);
                }
            });

            loadDesignData(true, false);

            //获取系统定义的最低滑动距离
            mTouchSlop = ViewConfiguration.get(getActivity()).getScaledTouchSlop();

            refreshLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mLastHeight = v.getMeasuredHeight();
                            mLastY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float currentY = event.getRawY();
                            currentY = currentY + (mLastHeight - v.getMeasuredHeight());
                            if (currentY - mLastY > mTouchSlop) {//手指向下滑动，显示toolbar
                                EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_inspiration_down);
                                notification.setValue("");
                                EventBus.getDefault().post(notification);
                                mLastY = currentY;
                            } else if (currentY - mLastY < -mTouchSlop) {//手指向上滑动，隐藏toolbar
                                EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_inspiration_up);
                                notification.setValue("");
                                EventBus.getDefault().post(notification);
                                mLastY = currentY;
                            }
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            });

            designListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    JSONObject jsonObject = dataList.get(position);
                    String object = jsonObject.toString();
                    Intent intent = new Intent(getActivity(), PlanListActivity.class);
                    intent.putExtra("object" , object);
                    startActivity(intent);
                }
            });
        }
    }

    private void loadDesignData(final boolean showLoading, boolean refresh) {
        if (showLoading) {
            pageIndex = 1;
            loadingView.setVisibility(View.VISIBLE);
        }
        if (refresh) {
            pageIndex = 1;
        }
        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", "" + pageIndex);
        params.put("pageSize", "" + pageSize);

        HttpRequest.get(AppConfig.GET_PREMISES, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadComplete();
                loadFailView.setFinishReload();
            }

            @Override
            public void onSuccess(JSONObject response) {

                if (JsonUtils.getCode(response) == 0) {
                    if (pageIndex == 1) {
                        dataList.clear();
                    }
                    JSONArray data = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < data.length(); i++) {
                        try {
                            dataList.add(data.getJSONObject(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if (data.length() < pageSize) {
                        designListView.setFooterViewVisibility(View.VISIBLE);
                        refreshLayout.setNoMoreData(true);
                        refreshLayout.setEnableLoadMore(false);
                    } else {
                        designListView.setFooterViewVisibility(View.GONE);
                        refreshLayout.setNoMoreData(false);
                        refreshLayout.setEnableLoadMore(true);
                    }
                    designAdapter.notifyDataSetChanged();
                    pageIndex++;

                    loadFailView.setVisibility(View.GONE);

                    if (dataList.size() == 0) {    //展示无数据
                        noDataView.setVisibility(View.VISIBLE);
                        designListView.setVisibility(View.GONE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                        designListView.setVisibility(View.VISIBLE);
                    }

                } else {
                    showError(showLoading || dataList.size() == 0);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError(showLoading || dataList.size() == 0);
                AlertManager.showErrorToast(getActivity(), "服务器繁忙，请稍后重试", false);
            }
        });

    }

    private void loadComplete() {
        loadingView.setVisibility(View.GONE);
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
    }

    class DesignAdapter extends BaseAdapter {

        class DesignViewHolder {

            RoundImageView image;
            TextView nameTxt;
            TextView descTxt;
            TextView planCountTxt;
            TextView roomCountTxt;

            TextView fanganshuliang;
            TextView huxingshuliang;

            DesignViewHolder(View view) {
                image = view.findViewById(R.id.premises_item_cover);
                nameTxt = view.findViewById(R.id.premises_item_name);
                descTxt = view.findViewById(R.id.premises_item_desc);
                planCountTxt = view.findViewById(R.id.plan_count);
                roomCountTxt = view.findViewById(R.id.room_count);

                fanganshuliang = view.findViewById(R.id.txt_fanganshuliang);
                huxingshuliang = view.findViewById(R.id.txt_huxingshuliang);
            }

        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public JSONObject getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {

            if (convertView == null) {
                // 获得容器
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.new_inspiration_design_item, null);
                convertView.setTag(new DesignViewHolder(convertView));
            }

            DesignViewHolder viewHolder = (DesignViewHolder) convertView.getTag();

            final JSONObject item = getItem(i);
            ImageUtils.loadImage(glide, ImageUtils.zoomResize(JsonUtils.getString(item, "cover", ""), 640, 480), viewHolder.image, R.mipmap.loadpicture);

            viewHolder.nameTxt.setText(JsonUtils.getString(item, "houseName", ""));
            WindowUtils.boldMethod(viewHolder.nameTxt);

            String cityStr = JsonUtils.getString(item,"city","");
            String countryStr = JsonUtils.getString(item,"county","");
            String addressStr = JsonUtils.getString(item, "address", "");
            viewHolder.descTxt.setText(cityStr + countryStr + addressStr);
            WindowUtils.boldMethod(viewHolder.descTxt);

            viewHolder.planCountTxt.setText(JsonUtils.getString(item, "planCount", ""));
            WindowUtils.boldMethod(viewHolder.planCountTxt);

            viewHolder.roomCountTxt.setText(JsonUtils.getString(item, "houseTypeCount", ""));
            WindowUtils.boldMethod(viewHolder.roomCountTxt);

            WindowUtils.boldMethod(viewHolder.fanganshuliang);
            WindowUtils.boldMethod(viewHolder.huxingshuliang);

            return convertView;
        }

    }

    private void showError(boolean showReLoad) {
        if (showReLoad) {
            loadFailView.setVisibility(View.VISIBLE);
        } else {
            refreshLayout.finishRefresh();
            refreshLayout.finishLoadMore();
            AlertManager.showErrorInfo(this.getActivity());
        }
    }

}
