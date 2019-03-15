package com.hande.goochao.views.activity;

import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.ZoomView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.HashMap;
import java.util.Map;

public class FurnitureListActivity extends ToolBarActivity {

    private boolean firstLoad = true;
    private int pageIndex = 1;
    private int pageSize = 20;

    private JSONArray styleData = new JSONArray();
    private JSONArray spaceData = new JSONArray();

    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.refresh_layout)
    private RefreshLayout refreshLayout;
    @ViewInject(R.id.furniture_list_view)
    private ListView furnitureListView;

    private AnimationProgressBar animationProgressBar;

    private BaseAdapter adapter;

    private LinearLayout layFurnitureHeaderContainer;

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_furniture_list);
        setTitle("家居场景");
        glide = GlideApp.with(this);
        View headerView = getLayoutInflater().inflate(R.layout.view_furniture_list_header, null);
        furnitureListView.addHeaderView(headerView);
        layFurnitureHeaderContainer = headerView.findViewById(R.id.magazine_top_container);

        animationProgressBar = new AnimationProgressBar(this);
        adapter = new FurnitureListAdapter();
        furnitureListView.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                onRefreshView(refreshLayout);
            }
        });

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                onLoadMoreView();
            }
        });

        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                onReloadView();
            }
        });

        loadStyle();
    }

    private void loadStyle() {

        if (firstLoad) {
            animationProgressBar.show();
        }

        HttpRequest.get(AppConfig.QUERY_INFORMATION_STYLE, null, null, JSONObject.class, new RequestCallback<JSONObject>() {

            @Override
            public void onComplete(boolean success, JSONObject response) {
                closeRefresh();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONArray res = JsonUtils.getJsonArray(response, "data", null);
                    JsonUtils.appendArray(styleData, res);
                    firstLoad = false;
                    loadMoreSpace();
                    initHeader();
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

    private void loadMoreSpace() {

        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", pageIndex + "");
        params.put("pageSize", pageSize + "");

        HttpRequest.get(AppConfig.FURNITURE_LIST, null, params, JSONObject.class, new RequestCallback<JSONObject>() {

            @Override
            public void onComplete(boolean success, JSONObject response) {
                closeRefresh();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONArray res = JsonUtils.getJsonArray(response, "data", null);
                    JsonUtils.appendArray(spaceData, res);
                    adapter.notifyDataSetChanged();
                    if (res.length() < pageSize) {
                        refreshLayout.setNoMoreData(true);
                        refreshLayout.setEnableLoadMore(false);
                    }
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

    private void closeRefresh() {
        animationProgressBar.dismiss();
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
        } else {
            AlertManager.showErrorInfo(FurnitureListActivity.this);
        }
    }

    public void onReloadView() {
        resetView();
        loadStyle();
    }

    public void onRefreshView(RefreshLayout refreshLayout) {
        pageIndex = 1;
        refreshLayout.setNoMoreData(false);
        styleData = new JSONArray();
        spaceData = new JSONArray();
        loadStyle();
    }

    public void onLoadMoreView() {
        pageIndex ++;
        refreshLayout.setEnableLoadMore(true);
        loadMoreSpace();
    }

    private void initHeader() {
        layFurnitureHeaderContainer.removeAllViews();
        JSONArray style = styleData;
        for (int i = 0; i < style.length(); i++) {
            View itemView = getLayoutInflater().inflate(R.layout.view_furniture_list_style, null);
            layFurnitureHeaderContainer.addView(itemView);
            ImageView styleImage = itemView.findViewById(R.id.furniture_top_img);

            int width = WindowUtils.getDeviceWidth(this) / 3;
            int height = width * 568 / 1024;
            LinearLayout.LayoutParams bannerParams= (LinearLayout.LayoutParams) styleImage.getLayoutParams();
            bannerParams.height = height;
            bannerParams.width = width;
            styleImage.setLayoutParams(bannerParams);

            String cover = ImageUtils.zoomResize( JsonUtils.getString(JsonUtils.getJsonItem(style, i, null), "cover", ""),512,286) ;
            ImageUtils.loadImage(glide, cover, styleImage, R.mipmap.loadpicture);

            final String name = JsonUtils.getString(JsonUtils.getJsonItem(style, i , null), "name", "");
            ((TextView) itemView.findViewById(R.id.furniture_top_name)).setText(name);
            final String styleId = JsonUtils.getString(JsonUtils.getJsonItem(style, i , null), "styleId", "");
            itemView.findViewById(R.id.furniture_list_style_zoom_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(FurnitureListActivity.this, SpaceListActivity.class);
                    intent.putExtra("name",name);
                    intent.putExtra("styleId",styleId);
                    startActivity(intent);
                }
            });
        }
    }

    class FurnitureListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return spaceData == null ? 0 : spaceData.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(spaceData, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            FurnitureListViewHolder viewHolder;

            if (view == null) {

                viewHolder = new FurnitureListViewHolder();
                view = getLayoutInflater().inflate(R.layout.view_furniture_list_item, null);

                viewHolder.furnitureListImg = view.findViewById(R.id.furniture_list_img);
                viewHolder.furnitureListName = view.findViewById(R.id.furniture_list_name);
                viewHolder.furnitureListStyleName = view.findViewById(R.id.furniture_list_style_name);
                viewHolder.furnitureListZoomView = view.findViewById(R.id.furniture_list_zoom_view);

                view.setTag(viewHolder);

            } else {
                viewHolder = (FurnitureListViewHolder) view.getTag();
            }

            JSONObject item = getItem(i);
            final String spaceId = JsonUtils.getString(item, "spaceId", "");
            ImageUtils.loadImage(glide, ImageUtils.zoomResize( JsonUtils.getString(item, "cover", ""),1024,1024), viewHolder.furnitureListImg, R.mipmap.loadpicture);
            viewHolder.furnitureListName.setText(JsonUtils.getString(item, "name", ""));
            viewHolder.furnitureListStyleName.setText(JsonUtils.getString(item, "styleNames", ""));
            viewHolder.furnitureListZoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(FurnitureListActivity.this, SpaceDetailActivity.class);
                    intent.putExtra("spaceId",spaceId);
                    startActivity(intent);
                }
            });

            return view;
        }
    }

    public class FurnitureListViewHolder {
        ImageView furnitureListImg;
        TextView furnitureListName;
        TextView furnitureListStyleName;
        ZoomView furnitureListZoomView;
    }

}
