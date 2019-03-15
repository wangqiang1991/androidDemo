package com.hande.goochao.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.ZoomView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.HashMap;
import java.util.Map;

public class SpaceListActivity extends ToolBarActivity {


    private int pageIndex = 1;
    private int pageSize = 20;
    Boolean firstLoad = true;
    String styleId;

    private JSONArray spaceData = new JSONArray();

    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.refresh_layout)
    private RefreshLayout refreshLayout;
    @ViewInject(R.id.space_list_view)
    private ListView spaceListView;
    @ViewInject(R.id.default_image)
    private LinearLayout defaultImage;

    private AnimationProgressBar animationProgressBar;

    private BaseAdapter adapter;

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_list);
        glide = GlideApp.with(this);
        Bundle extras = getIntent().getExtras();
        String name = extras.getString("name");
        styleId = extras.getString("styleId");
        setTitle(name);

        animationProgressBar = new AnimationProgressBar(this);
        adapter = new SpaceListAdapter();
        spaceListView.setAdapter(adapter);

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

        loadSpace();
    }

    private void loadSpace() {

        if (firstLoad) {
            animationProgressBar.show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", pageIndex + "");
        params.put("pageSize", pageSize + "");
        params.put("styleId", styleId + "");

        HttpRequest.get(AppConfig.FURNITURE_LIST, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                closeRefresh();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONArray res = JsonUtils.getJsonArray(response, "data", null);
                    if ( res.length() == 0 && pageIndex == 1 ){
                        defaultImage.setVisibility(View.VISIBLE);
                        refreshLayout.setEnableLoadMore(false);
                        refreshLayout.setEnableRefresh(false);
                    }else {
                        defaultImage.setVisibility(View.GONE);
                        refreshLayout.setEnableLoadMore(true);
                    }
                    JsonUtils.appendArray(spaceData, res);
                    adapter.notifyDataSetChanged();
                    firstLoad = false;
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
            AlertManager.showErrorInfo(SpaceListActivity.this);
        }
    }

    public void onReloadView() {
        resetView();
        loadSpace();
    }

    public void onRefreshView(RefreshLayout refreshLayout) {
        pageIndex = 1;
        refreshLayout.setNoMoreData(false);
        refreshLayout.setEnableLoadMore(true);
        spaceData = new JSONArray();
        loadSpace();
    }

    public void onLoadMoreView() {
        pageIndex ++;
        loadSpace();
    }

    class SpaceListAdapter extends BaseAdapter {

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
            SpaceListViewHolder viewHolder;

            if (view == null) {

                viewHolder = new SpaceListViewHolder();
                view = getLayoutInflater().inflate(R.layout.view_space_list_item, null);

                viewHolder.spaceListImg = view.findViewById(R.id.space_list_img);
                viewHolder.spaceListName = view.findViewById(R.id.space_list_name);
                viewHolder.spaceListStyleName = view.findViewById(R.id.space_list_style_name);
                viewHolder.spaceListZoomView = view.findViewById(R.id.space_list_zoom_view);

                view.setTag(viewHolder);

            } else {
                viewHolder = (SpaceListViewHolder) view.getTag();
            }

            JSONObject item = getItem(i);
            final String spaceId = JsonUtils.getString(item, "spaceId", "");
            ImageUtils.loadImage(glide, ImageUtils.zoomResize( JsonUtils.getString(item, "cover", ""),1024,1024), viewHolder.spaceListImg, R.mipmap.loadpicture);

            viewHolder.spaceListName.setText(JsonUtils.getString(item, "name", ""));
            viewHolder.spaceListStyleName.setText(JsonUtils.getString(item, "styleNames", ""));
            viewHolder.spaceListZoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SpaceListActivity.this, SpaceDetailActivity.class);
                    intent.putExtra("spaceId", spaceId);
                    startActivity(intent);
                }
            });

            return view;
        }
    }

    public class SpaceListViewHolder {
        ImageView spaceListImg;
        TextView spaceListName;
        TextView spaceListStyleName;
        ZoomView spaceListZoomView;
    }
}
