package com.hande.goochao.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hande.goochao.R;
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
import com.hande.goochao.views.base.BaseActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.ZoomView;
import com.hande.goochao.views.components.refresh.MySmartRefreshLayout;
import com.hande.goochao.views.widget.GoochaoGridView;
import com.hande.goochao.views.widget.tablayout.RadiusFiveImageView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LMC on 2018/2/27.
 */

public class AllDesignActivity extends BaseActivity implements ScenePreviewActivity.OnPageChangeListener {

    private ImageOptions options = ImageOptionsUtil.getImageOptionsCenter(R.mipmap.loadpicture);

    @ViewInject(R.id.all_design)
    private GoochaoGridView headerGridView;
    @ViewInject(R.id.design_loading_view)
    private LoadingView loadingView;
    @ViewInject(R.id.all_design_refresh_layout)
    private MySmartRefreshLayout refreshLayout;
    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;

    private boolean loadDetail = true;
    private int pageSize = 21;
    private int pageIndex = 1;

    private ImageView designerHead;
    private TextView quanbuzuoping;

    private JSONArray designAllList = new JSONArray();
    private JSONArray designAll;
    private boolean firstLoad = true;
    private AllDesignAdapter allDesignAdapter;
    private String designerId;

    private View headView;

    private GlideRequests glide;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_design);

        Intent intent = getIntent();
        designerId = intent.getStringExtra("designerId");
        glide = GlideApp.with(this);

        headView = LayoutInflater.from(this).inflate(R.layout.activity_all_design_head_view, headerGridView, false);
        headerGridView.addHeaderView(headView);
        designerHead = headView.findViewById(R.id.designer_head_view);
        quanbuzuoping = headView.findViewById(R.id.quanbuzuoping);
        WindowUtils.boldMethod(quanbuzuoping);

        allDesignAdapter = new AllDesignAdapter();
        headerGridView.setAdapter(allDesignAdapter);

        refreshLayout.setEnableRefresh(false);

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                loadAllDesign();
            }
        });

        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadDesigner();
            }
        });

        loadAllDesign();

    }

    private void loadAllDesign() {
        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        }
        Map<String, String> params = new HashMap<>();
        params.put("pageSize", "" + pageSize);
        params.put("pageIndex", "" + pageIndex);
        params.put("loadDetail", "" + loadDetail);
        params.put("designerId", "" + designerId);

        HttpRequest.get(AppConfig.DESIGN_GET, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                refreshLayout.finishLoadMore();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    designAll = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < designAll.length(); i++) {
                        try {
                            designAllList.put(designAll.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if (designAll.length() < pageSize) {
                        headerGridView.setFooterViewVisibility(View.VISIBLE);
                        refreshLayout.setNoMoreData(true);
                        refreshLayout.setEnableLoadMore(false);
                    } else {
                        headerGridView.setFooterViewVisibility(View.GONE);
                        refreshLayout.setNoMoreData(false);
                    }
                    if (firstLoad) {
                        loadDesigner();
                    }
                    allDesignAdapter.notifyDataSetChanged();
                    pageIndex++;
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(AllDesignActivity.this, "服务器繁忙，请稍后重试", false);
                AppLog.e("err", ex);
                showError();
            }
        });
    }

    private void loadDesigner() {
        Intent intent = getIntent();
        String designerId = intent.getStringExtra("designerId");
        Map<String, String> params = new HashMap<>();
        params.put("designerId", "" + designerId);

        HttpRequest.get(AppConfig.DESIGNER_GET + designerId, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadComplete();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONObject designer = JsonUtils.getJsonObject(response, "data", null);
                    x.image().bind(designerHead, ImageUtils.resize(JsonUtils.getString(designer, "head", "--"), 320, 320), options);
                    firstLoad = false;
                    resetView();
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(AllDesignActivity.this, "服务器繁忙，请稍后重试", false);
                AppLog.e("err", ex);
                showError();
            }
        });
    }

    @Override
    public void onPageChange(int currentIndex, int size) {

    }

    class AllDesignAdapter extends BaseAdapter {

        class AllDesignViewHolder {

            RadiusFiveImageView imageView;
            ZoomView zoomView;

            AllDesignViewHolder(View view) {

                imageView = view.findViewById(R.id.design_item);
                zoomView = view.findViewById(R.id.zoom_view);
            }
        }

        @Override
        public int getCount() {
            return designAllList == null ? 0 : designAllList.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(designAllList, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(AllDesignActivity.this).inflate(R.layout.gridview_design_item, viewGroup, false);
                view.setTag(new AllDesignViewHolder(view));
            }

            AllDesignViewHolder viewHolder = (AllDesignViewHolder) view.getTag();

            viewHolder.zoomView.setTag(i);
            viewHolder.zoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ZoomView zoomView = ((ZoomView) v);
                    ScenePreviewActivity.setOnPageChangeListener(AllDesignActivity.this);
                    AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, designAllList);
                    Intent intent = new Intent(AllDesignActivity.this, ScenePreviewActivity.class);
                    intent.putExtra("currentIndex", ((Integer) zoomView.getTag()));
                    startActivity(intent);
                    AllDesignActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
            ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(getItem(i), "cover", "--"), 320, 320), viewHolder.imageView, R.mipmap.loadpicture);
            x.image().bind(viewHolder.imageView, ImageUtils.resize(JsonUtils.getString(getItem(i), "cover", "--"), 320, 320), options);
            return view;
        }
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void loadComplete() {
        loadingView.setVisibility(View.GONE);
        refreshLayout.finishLoadMore();
    }

    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
        } else {
            loadingView.setVisibility(View.GONE);
            AlertManager.showErrorInfo(AllDesignActivity.this);
        }
    }
}