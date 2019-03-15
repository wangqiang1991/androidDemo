package com.hande.goochao.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.config.AppConst;
import com.hande.goochao.session.AppSession;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.ScenePreviewActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.HeaderGridView;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;
import com.hande.goochao.views.components.ZoomView;
import com.hande.goochao.views.components.refresh.MySmartRefreshLayout;
import com.hande.goochao.views.components.refresh.NoDataTwoLineView;
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

import java.util.HashMap;
import java.util.Map;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/7/12.
 */

@ContentView(R.layout.scene_inspiration_layout)
public class SceneFragment extends BaseFragment implements ScenePreviewActivity.OnPageChangeListener {

    @ViewInject(R.id.scene_refresh_layout)
    private MySmartRefreshLayout refreshLayout;
    @ViewInject(R.id.inspiration_scene_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.scene_inspiration_load_fail)
    private LoadFailView loadFailView;
    private ImageOptions options = ImageOptionsUtil.getImageOptions(R.mipmap.loadpicture);
    @ViewInject(R.id.scene_inspiration_grid_view)
    private GoochaoGridView sceneGridView;
    @ViewInject(R.id.scene_noDataView)
    private NoDataTwoLineView noDataView;

    private boolean loaded = false;

    private int pageIndex = 1;
    private int pageSize = 20;
    private int kWidth;
    private String styleId = "";
    private String areaId = "";
    private String tagType = "";

    private JSONArray sceneArray = new JSONArray();

    private SceneAdapter sceneAdapter;

    private boolean isLoading = false;

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
            kWidth = WindowUtils.getDeviceWidth(getActivity());
            noDataView.setImageAndText(R.mipmap.search_no_result, "能不能给我一首歌的时间","这个空间还在搭建中哦");

            sceneAdapter = new SceneAdapter();
            sceneGridView.setAdapter(sceneAdapter);

            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshLayout) {
                    loadScene(false, true);
                }
            });
            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshLayout) {
                    loadScene(false, false);
                }
            });
            loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
                @Override
                public void onReload() {
                    loadFailView.setReloading();
                    loadScene(true, false);
                }
            });

            loadScene(true, false);
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

    private void loadScene(final boolean showLoading, boolean refresh) {
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
        params.put("styleId", "" + styleId);
        params.put("areaId", "" + areaId);
        params.put("loadDetail", "" + true);
        params.put("tagType", "" + tagType);

        isLoading = true;

        HttpRequest.get(AppConfig.PICTURE_GET, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadComplete();
                loadFailView.setFinishReload();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    if (pageIndex == 1) {
                        sceneArray = new JSONArray();
                    }

                    JSONArray data = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < data.length(); i++) {
                        try {
                            sceneArray.put(data.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if (data.length() < pageSize) {
                        sceneGridView.setFooterViewVisibility(View.VISIBLE);
                        refreshLayout.setNoMoreData(true);
                        refreshLayout.setEnableLoadMore(false);
                    } else {
                        sceneGridView.setFooterViewVisibility(View.GONE);
                        refreshLayout.setNoMoreData(false);
                        refreshLayout.setEnableLoadMore(true);
                    }
                    sceneAdapter.notifyDataSetChanged();
                    pageIndex++;

                    if (sceneArray.length() == 0) {    //展示无数据
                        noDataView.setVisibility(View.VISIBLE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                    }
                    resetView();
                    notifyPreview();
                } else {
                    showError(showLoading || sceneArray.length() == 0);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError(showLoading || sceneArray.length() == 0);
                AlertManager.showErrorToast(getActivity(), "服务器繁忙，请稍后重试", false);
            }
        });

    }

    private void loadComplete() {
        loadingView.setVisibility(View.GONE);
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
        isLoading = false;
    }


    @Override
    public void onPageChange(int currentIndex, int size) {
        AppLog.i("currentIndex:" + currentIndex + ", size:" + size);
        sceneGridView.smoothScrollToPositionFromTop(currentIndex, 0);
        int currentPage = currentIndex + 1;
        if (!isLoading && size - currentPage <= 5) {
            AppLog.i("loading next data");
            this.loadScene(false, false);
        }
    }

    private void notifyPreview() {
        AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, sceneArray);
        ScenePreviewActivity.refreshData();
    }

    class SceneAdapter extends BaseAdapter implements View.OnClickListener {


        class SceneViewHolder {

            ImageView image;
            View shadow_background;
            ZoomView zoomView;

            SceneViewHolder(View view) {
                image = view.findViewById(R.id.inspiration_scene_item_cover);
                shadow_background = view.findViewById(R.id.shadow_background);
                zoomView = view.findViewById(R.id.inspiration_scene_zoom_view);
            }

        }

        @Override
        public int getCount() {
            return sceneArray == null ? 0 : sceneArray.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(sceneArray, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {

            if (convertView == null) {
                // 获得容器
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.inspiration_scene_item, null);
                convertView.setTag(new SceneViewHolder(convertView));
            }
            SceneViewHolder viewHolder = (SceneViewHolder) convertView.getTag();
            viewHolder.zoomView.setTag(i);
            viewHolder.zoomView.setOnClickListener(this);

            int imgWidth = (kWidth - WindowUtils.dpToPixels(getActivity(), 30)) / 2;
            int imgHeight = imgWidth;
            final JSONObject item = getItem(i);

            ImageUtils.loadImage(glide,JsonUtils.getString(item, "cover", ""), viewHolder.image, R.mipmap.loadpicture);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.image.getLayoutParams();
            params.width = imgWidth;
            params.height = imgHeight;

            return convertView;
        }

        @Override
        public void onClick(View v) {
            ZoomView zoomView = ((ZoomView) v);
            ScenePreviewActivity.setOnPageChangeListener(SceneFragment.this);
            AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, sceneArray);
            Intent intent = new Intent(getActivity(), ScenePreviewActivity.class);
            intent.putExtra("currentIndex", ((Integer) zoomView.getTag()));
            startActivity(intent);
            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
        if (notification.getKey().equals(EventBusNotification.event_bus_inspiration_region)) {
            areaId = notification.getValue().toString();
            loadScene(true, false);
        } else if (notification.getKey().equals(EventBusNotification.event_bus_inspiration_style_scene)) {
            styleId = notification.getValue().toString();
            loadScene(true, false);
        } else if (notification.getKey().equals(EventBusNotification.event_bus_inspiration_kind)) {
            tagType = notification.getValue().toString();
            loadScene(true, false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
