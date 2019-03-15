package com.hande.goochao.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.config.AppConst;
import com.hande.goochao.session.AppSession;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.WebViewActivity;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.ZoomView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class SpaceDetailActivity extends ToolBarActivity {

    private boolean firstLoad = true;
    String spaceId;
    String articleId;

    private JSONObject spaceData = new JSONObject();
    private JSONArray sceneData = new JSONArray();

    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.refresh_layout)
    private RefreshLayout refreshLayout;
    @ViewInject(R.id.space_detail_view)
    private ListView spaceDetailListView;
    View headerView;

    private AnimationProgressBar animationProgressBar;

    private BaseAdapter adapter;

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_detail);
        setTitle("场景详情");
        glide = GlideApp.with(this);
        Bundle extras = getIntent().getExtras();
        spaceId = extras.getString("spaceId");

        animationProgressBar = new AnimationProgressBar(this);
        adapter = new SpaceDetailListAdapter();
        spaceDetailListView.setAdapter(adapter);

        headerView = getLayoutInflater().inflate(R.layout.view_space_detail_header, null);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                onRefreshView();
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

        String url = RestfulUrl.build(AppConfig.SPACE_DETAIL, ":spaceId", spaceId);

        HttpRequest.get(url, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                animationProgressBar.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    spaceData = JsonUtils.getJsonObject(response, "data", null);
                    JSONArray data = JsonUtils.getJsonArray(spaceData, "items", null);
                    for ( int i = 0; i < data.length(); i++ ){
                        JSONArray item = JsonUtils.getJsonArray(JsonUtils.getJsonItem(data, i, null), "picItems", null);
                        JsonUtils.appendArray(sceneData, item);
                    }
                    adapter.notifyDataSetChanged();
                    firstLoad = false;
                    refreshLayout.setEnableRefresh(false);
                    loadSpaceContent();
                    initHeader();
                    resetView();
                } else {
                    showError();
                }
                closeRefresh();
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
            }
        });
    }

    private void loadSpaceContent() {
        String url = RestfulUrl.build(AppConfig.SPACE_CONTENT, ":spaceId", spaceId);

        HttpRequest.get(url, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                closeRefresh();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONObject data = JsonUtils.getJsonObject(response, "data", null);
                    articleId = JsonUtils.getString(JsonUtils.getJsonObject(data, "article", null),"articleId","");
                    final String name = "场景介绍";
                    final String url = AppConfig.ARTICLE_CONTENT + articleId;
                    headerView.findViewById(R.id.see_more).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        Intent intent=new Intent(SpaceDetailActivity.this, WebViewActivity.class);
                        intent.putExtra("title",name);
                        intent.putExtra("url",url);
                        startActivity(intent);
                        }
                    });
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
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
        } else {
            AlertManager.showErrorInfo(SpaceDetailActivity.this);
        }
    }

    public void onReloadView() {
        resetView();
        loadSpace();
    }

    public void onRefreshView() {
        spaceData = new JSONObject();
        sceneData = new JSONArray();
        spaceDetailListView.removeHeaderView(headerView);
        loadSpace();
    }

    private void initHeader() {
        String cover = JsonUtils.getString(spaceData, "cover", null) ;
        ImageUtils.loadImage(glide, cover, (ImageView) headerView.findViewById(R.id.space_detail_background_image), R.mipmap.loadpicture);
        String name = JsonUtils.getString(spaceData, "name", null);
        ((TextView) headerView.findViewById(R.id.space_name)).setText(name);
        String description = JsonUtils.getString(spaceData, "description", null);
        ((TextView) headerView.findViewById(R.id.space_description)).setText(description);
        spaceDetailListView.addHeaderView(headerView);
    }

    class SpaceDetailListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return sceneData == null ? 0 : sceneData.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(sceneData, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            SceneListViewHolder viewHolder;

            if (view == null) {

                viewHolder = new SceneListViewHolder();
                view = getLayoutInflater().inflate(R.layout.view_space_detail_list_item, null);

                viewHolder.sceneListImg = view.findViewById(R.id.space_detail_image);
                viewHolder.sceneListName = view.findViewById(R.id.space_detail_title);
                viewHolder.sceneListGoodsCount = view.findViewById(R.id.space_detail_count);
                viewHolder.sceneDetailZoomView = view.findViewById(R.id.space_detail_zoom_view);

                view.setTag(viewHolder);

            } else {
                viewHolder = (SceneListViewHolder) view.getTag();
            }

            JSONObject item = getItem(i);
            ImageUtils.loadImage(glide, ImageUtils.zoomResize( JsonUtils.getString(item, "cover", ""),1024,1024), viewHolder.sceneListImg, R.mipmap.loadpicture);


            viewHolder.sceneListName.setText(JsonUtils.getString(item, "name", ""));
            viewHolder.sceneListGoodsCount.setText(JsonUtils.getString(item, "goodsCount", ""));
            viewHolder.sceneDetailZoomView.setOnClickListener(new SceneOnClickListener(i));

            return view;
        }
    }

    public class SceneListViewHolder {
        ImageView sceneListImg;
        TextView sceneListName;
        TextView sceneListGoodsCount;
        ZoomView sceneDetailZoomView;
    }

    class SceneOnClickListener implements View.OnClickListener {
        private int currentIndex;

        SceneOnClickListener(int currentIndex) {
            this.currentIndex = currentIndex;
        }

        @Override
        public void onClick(View view) {
            AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, sceneData);
            Intent intent = new Intent(SpaceDetailActivity.this, ScenePreviewActivity.class);
            intent.putExtra("currentIndex", currentIndex);
            startActivity(intent);
        }
    }
}
