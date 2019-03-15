package com.hande.goochao.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.MagazineDetailActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.HeaderGridView;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;
import com.hande.goochao.views.components.refresh.MySmartRefreshLayout;
import com.hande.goochao.views.widget.GoochaoGridView;
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

import in.srain.cube.views.GridViewWithHeaderAndFooter;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/7/12.
 */

@ContentView(R.layout.article_inspiration_layout)
public class ArticleFragment extends BaseFragment {

    @ViewInject(R.id.article_refresh_layout)
    private MySmartRefreshLayout refreshLayout;
    @ViewInject(R.id.inspiration_article_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.article_inspiration_load_fail)
    private LoadFailView loadFailView;
    @ViewInject(R.id.article_inspiration_grid_view)
    private GoochaoGridView articleGridView;
    @ViewInject(R.id.article_noDataView)
    private NoDataView noDataView;

    private boolean loaded = false;
    private int pageIndex = 1;
    private int pageSize = 20;
    private int kWidth;
    private String articleTypeId = "";

    private List<JSONObject> dataList = new ArrayList<>();

    private ArticleAdapter articleAdapter;


    private float mLastY;//用户滑动结束时Y坐标
    private int mTouchSlop;//系统认为的最小滑动距离
    private int mLastHeight;

    private GlideRequests glide;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!loaded) {
            loaded = true;
            kWidth = WindowUtils.getDeviceWidth(getActivity());
            glide = GlideApp.with(getActivity());

            noDataView.setImageAndText(R.mipmap.search_no_result, "亲，暂时木有该类别期刊~");
            articleAdapter = new ArticleAdapter();
            articleGridView.setAdapter(articleAdapter);

            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshLayout) {
                    pageIndex = 1;
                    loadArticle(false, true);
                }
            });
            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshLayout) {
                        loadArticle(false, false);
                }
            });
            loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
                @Override
                public void onReload() {
                    loadFailView.setReloading();
                    loadArticle(true, false);
                }
            });

            loadArticle(true, false);
            EventBus.getDefault().register(this);

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
        }
    }


    private void loadArticle(final boolean showLoading, boolean refresh) {
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
        params.put("articleTypeId", "" + articleTypeId);
        HttpRequest.get(AppConfig.MAGAZINE_LIST, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
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
                    articleAdapter.notifyDataSetChanged();
                    resetView();

                    if (dataList.size() == 0) {    //展示无数据
                        noDataView.setVisibility(View.VISIBLE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                        if (data.length() < pageSize) {
                            refreshLayout.setNoMoreData(true);
                            refreshLayout.setEnableLoadMore(false);
                            articleGridView.setFooterViewVisibility(View.VISIBLE);
                        } else {
                            refreshLayout.setNoMoreData(false);
                            refreshLayout.setEnableLoadMore(true);
                            articleGridView.setFooterViewVisibility(View.GONE);
                        }
                    }
                    pageIndex ++;
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

    class ArticleAdapter extends BaseAdapter {


        class ArticleViewHolder {

            ImageView image;
            TextView desTxt;
            TextView typeBt;

            ArticleViewHolder(View view) {
                image = view.findViewById(R.id.inspiration_article_cover);
                desTxt = view.findViewById(R.id.article_description);
                typeBt = view.findViewById(R.id.type_name);
            }

        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public JSONObject getItem(int i) {
            return dataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {

            if (convertView == null) {
                // 获得容器
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.inspiration_article_item, null);
                convertView.setTag(new ArticleViewHolder(convertView));
            }

            ArticleViewHolder viewHolder = (ArticleViewHolder) convertView.getTag();

            int imgHeight = kWidth * 170 / 335;
            int imgWidth = kWidth;

            final JSONObject item = getItem(i);
            ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(item, "cover", ""), 1024, 568), viewHolder.image, R.mipmap.loadpicture);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.image.getLayoutParams();
            params.width = imgWidth;
            params.height = imgHeight;

            viewHolder.desTxt.setText(JsonUtils.getString(item, "title", ""));

            viewHolder.typeBt.setText(JsonUtils.getString(item,"typeName",""));

            viewHolder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String subjectArticleId = JsonUtils.getString(dataList.get(i), "subjectArticleId", "");
                    Intent intent = new Intent(getActivity(), MagazineDetailActivity.class);
                    intent.putExtra("subjectArticleId", subjectArticleId);
                    getActivity().startActivity(intent);
                }
            });

            return convertView;
        }
    }


    private void resetView() {
        loadFailView.setVisibility(View.GONE);
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

    @Subscribe
    public void onEvent(EventBusNotification notification) {
        if (notification.getKey().equals(EventBusNotification.event_bus_inspiration_article)) {
            articleTypeId = notification.getValue().toString();
            loadArticle(true, false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
