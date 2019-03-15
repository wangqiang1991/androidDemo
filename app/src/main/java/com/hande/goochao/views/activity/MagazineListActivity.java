package com.hande.goochao.views.activity;

import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.config.AppConst;
import com.hande.goochao.session.AppSession;
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

public class MagazineListActivity extends ToolBarActivity {

    private ImageOptions options = ImageOptionsUtil.getBigImageOptions(R.mipmap.loadpicture);

    private boolean firstLoad = true;
    private int pageIndex = 1;
    private int pageSize = 20;

    private JSONArray subjects = new JSONArray();

    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.refresh_layout)
    private RefreshLayout refreshLayout;
    @ViewInject(R.id.magazine_list_view)
    private ListView magazineListView;

    private AnimationProgressBar animationProgressBar;

    private BaseAdapter adapter;

    private LinearLayout layMagazineContainer;

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magazine_list);
        setTitle("家居期刊");
        glide = GlideApp.with(this);
        View headerView = getLayoutInflater().inflate(R.layout.view_magazine_header, null);
        magazineListView.addHeaderView(headerView);
        layMagazineContainer = headerView.findViewById(R.id.magazine_top_container);

        animationProgressBar = new AnimationProgressBar(this);
        adapter = new MagazineAdapter();
        magazineListView.setAdapter(adapter);

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

        loadMoreMagazine();
    }

    private void loadMoreMagazine() {

        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", pageIndex + "");
        params.put("pageSize", pageSize + "");

        if (firstLoad) {
            animationProgressBar.show();
        }
        HttpRequest.get(AppConfig.MAGAZINE_LIST, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                closeRefresh();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONArray res = JsonUtils.getJsonArray(response, "data", null);
                    JsonUtils.appendArray(subjects, res);
                    adapter.notifyDataSetChanged();
                    if (res.length() < pageSize) {
                        refreshLayout.setNoMoreData(true);
                        refreshLayout.setEnableLoadMore(false);
                    }

                    if (firstLoad) {
                        firstLoad = false;
                        initHeader();
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

    private void initHeader() {
        JSONArray magazine = AppSession.getInstance().get(AppConst.MAGAZINE_LIST_SESSION);

        int itemWidth = (WindowUtils.getDeviceWidth(this) - WindowUtils.dpToPixels(this,46));
        int itemHeight = itemWidth * 284 / 512;

        for (int i = 0; i < magazine.length(); i++) {
            View itemView = getLayoutInflater().inflate(R.layout.view_home_magazine, null);
            layMagazineContainer.addView(itemView);
            JSONObject object = JsonUtils.getJsonObject(JsonUtils.getJsonItem(magazine, i, null), "data", new JSONObject());
            String cover = ImageUtils.zoomResize( JsonUtils.getString(JsonUtils.getJsonItem(magazine, i, null), "cover", "" ),1024,568) ;
            String description = JsonUtils.getString(object, "description", "");
            ((TextView) itemView.findViewById(R.id.magazine_description)).setText(description);
            ImageUtils.loadImage(glide, cover, (ImageView) itemView.findViewById(R.id.magazine_img), R.mipmap.loadpicture);

            final String subjectArticleId = JsonUtils.getString(object, "subjectArticleId", "" );

            LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) itemView.findViewById(R.id.home_magazine_zoom_view).getLayoutParams();
            params.width = itemWidth;
            params.height = itemHeight;
            itemView.findViewById(R.id.home_magazine_zoom_view).setLayoutParams(params);
            itemView.findViewById(R.id.home_magazine_zoom_view).setOnClickListener(new MagazineOnClickListener(subjectArticleId));
        }
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
            AlertManager.showErrorInfo(MagazineListActivity.this);
        }
    }

    public void onReloadView() {
        resetView();
        loadMoreMagazine();
    }

    public void onRefreshView(RefreshLayout refreshLayout) {
        pageIndex = 1;
        refreshLayout.setNoMoreData(false);
        refreshLayout.setEnableLoadMore(true);
        subjects = new JSONArray();
        loadMoreMagazine();
    }

    public void onLoadMoreView() {
        pageIndex ++;
        loadMoreMagazine();
    }

    class MagazineAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return subjects == null ? 0 : subjects.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(subjects, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            MagazineViewHolder viewHolder;

            if (view == null) {

                viewHolder = new MagazineViewHolder();
                view = getLayoutInflater().inflate(R.layout.view_magazine_list_item, null);

                viewHolder.magazineImg = view.findViewById(R.id.magazine_img);
                viewHolder.magazineDescription = view.findViewById(R.id.magazine_description);
                viewHolder.magazineListZoomView = view.findViewById(R.id.magazine_list_zoom_view);

                view.setTag(viewHolder);

            } else {
                viewHolder = (MagazineViewHolder) view.getTag();
            }

            JSONObject item = getItem(i);
            ImageUtils.loadImage(glide, ImageUtils.zoomResize( JsonUtils.getString(item, "cover", ""),1024,568), viewHolder.magazineImg, R.mipmap.loadpicture);

            viewHolder.magazineDescription.setText(JsonUtils.getString(item, "description", ""));
            final String subjectArticleId = JsonUtils.getString(item, "subjectArticleId", "");
            viewHolder.magazineListZoomView.setOnClickListener(new MagazineOnClickListener(subjectArticleId));

            int itemWidth = (WindowUtils.getDeviceWidth(MagazineListActivity.this) - WindowUtils.dpToPixels(MagazineListActivity.this,32));
            int itemHeight = itemWidth * 284 / 512;
            LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) viewHolder.magazineImg.getLayoutParams();
            params.width = itemWidth;
            params.height = itemHeight;

            return view;
        }
    }

    public class MagazineViewHolder {
        ImageView magazineImg;
        TextView magazineDescription;
        ZoomView magazineListZoomView;
    }

    class MagazineOnClickListener implements View.OnClickListener {
        private String subjectArticleId;

        MagazineOnClickListener(String subjectArticleId) {
            this.subjectArticleId = subjectArticleId;
        }

        @Override
        public void onClick(View view) {
            Intent intent=new Intent(MagazineListActivity.this, MagazineDetailActivity.class);
            intent.putExtra("subjectArticleId",subjectArticleId);
            startActivity(intent);
        }
    }

}
