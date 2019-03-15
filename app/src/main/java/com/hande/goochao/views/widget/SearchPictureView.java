package com.hande.goochao.views.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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
import com.hande.goochao.config.AppConst;
import com.hande.goochao.session.AppSession;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.activity.ScenePreviewActivity;
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

public class SearchPictureView extends LinearLayout implements LoadFailView.OnReloadListener, View.OnClickListener, ScenePreviewActivity.OnPageChangeListener {

    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.no_data_view)
    private NoDataView noDataView;
    @ViewInject(R.id.refresh_layout)
    private SmartRefreshLayout refreshLayout;
    @ViewInject(R.id.picture_grid_view)
    private GoochaoGridView pictureGridView;

    @ViewInject(R.id.search_picture_loading)
    private LoadingView progressBar;

    private String keyword;
    private int pageIndex;
    private int pageSize;
    private List<JSONObject> pictureList = new ArrayList<>();
    private boolean firstLoad;
    private boolean isLoading;

    private GlideRequests glide;

    private PictureListAdapter pictureListAdapter;

    public SearchPictureView(Context context) {
        super(context);
        init();
    }

    public SearchPictureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SearchPictureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SearchPictureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        glide = GlideApp.with(this);
        LayoutInflater.from(getContext()).inflate(R.layout.view_search_picture, this, true);
        x.view().inject(this);

        noDataView.setImageAndText(R.mipmap.search_no_result, "世界上最遥远的距离\n莫过于你知道我而我不知道你");
        loadFailView.setOnReloadListener(this);
        pictureListAdapter = new PictureListAdapter();
        pictureGridView.setAdapter(pictureListAdapter);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                reload();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                searchPicture();
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

    private void searchPicture() {
        if (firstLoad) {
            progressBar.setVisibility(VISIBLE);
        }

        Map<String, String> params = Params.buildForStr(
                "keyword", keyword,
                "pageIndex", String.valueOf(pageIndex),
                "pageSize", String.valueOf(pageSize),
                "loadDetail", String.valueOf(true));

        isLoading = true;
        HttpRequest.get(AppConfig.SEARCH_PICTURE, null, params, new JSONObjectCallback() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                isLoading = false;
                closeRefresh();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    if (pageIndex == 1) {
                        pictureList.clear();
                    }
                    JSONArray data = JsonUtils.getJsonArray(response, "data", null);
                    handleData(data);
                    pageIndex ++;
                    notifyPreview();
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
                pictureList.add(item);
            }
        }

        if (data.length() < pageSize) {
            pictureGridView.setFooterViewVisibility(View.VISIBLE);
            refreshLayout.setNoMoreData(true);
            refreshLayout.setEnableLoadMore(false);
        }else {
            pictureGridView.setFooterViewVisibility(View.GONE);
            refreshLayout.setNoMoreData(false);
            refreshLayout.setEnableLoadMore(true);
        }

        if (pictureList.isEmpty()) {
            noDataView.setVisibility(VISIBLE);
            refreshLayout.setVisibility(GONE);
        } else {
            noDataView.setVisibility(GONE);
            refreshLayout.setVisibility(VISIBLE);
        }

        pictureListAdapter.notifyDataSetChanged();
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
        searchPicture();
    }

    public void onResume() {
        ScenePreviewActivity.setOnPageChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        ZoomView zoomView = ((ZoomView) v);
        ScenePreviewActivity.setOnPageChangeListener(this);
        JSONArray pictureArr = JsonUtils.newJsonArray(pictureList);
        AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, pictureArr);
        Intent intent = new Intent(getContext(), ScenePreviewActivity.class);
        intent.putExtra("currentIndex", ((Integer) zoomView.getTag()));
        getContext().startActivity(intent);
        ((Activity) getContext()).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onPageChange(int currentIndex, int size) {
        AppLog.i("currentIndex:" + currentIndex + ", size:" + size);
        pictureGridView.smoothScrollToPositionFromTop (currentIndex, 0);
        int currentPage = currentIndex + 1;
        if (!isLoading && size - currentPage <= 5) {
            AppLog.i("loading next data");
            this.searchPicture();
        }
    }

    private void notifyPreview() {
        AppLog.i("notify scene preview refresh");
        JSONArray pictureArr = JsonUtils.newJsonArray(pictureList);
        AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, pictureArr);
        ScenePreviewActivity.refreshData();
    }

    class PictureListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return pictureList.size();
        }

        @Override
        public JSONObject getItem(int position) {
            return pictureList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.gridview_picture_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.zoomView = convertView.findViewById(R.id.zoom_view);
                viewHolder.zoomView .setOnClickListener(SearchPictureView.this);
                viewHolder.pictureVie = convertView.findViewById(R.id.picture_view);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            JSONObject item = getItem(position);
            viewHolder.zoomView.setTag(position);
            ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(item, "detail.cover", ""), 500, 500), viewHolder.pictureVie, R.mipmap.loadpicture);


            return convertView;
        }

        class ViewHolder {
            private ZoomView zoomView;
            private ImageView pictureVie;
        }
    }
}
