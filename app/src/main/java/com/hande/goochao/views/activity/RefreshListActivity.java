package com.hande.goochao.views.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.LoadFailView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

public class RefreshListActivity extends ToolBarActivity {

    @ViewInject(R.id.refresh_layout)
    private RefreshLayout refreshLayout;

    @ViewInject(R.id.category_list_view)
    private ListView categoryListView;

    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;

    private AnimationProgressBar animationProgressBar;

    private CategoryAdapter adapter;
    private JSONArray categoryList;

    private boolean firstLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh_list);
        setTitle("下拉和上拉刷新Demo");

        firstLoad = true;

        animationProgressBar = new AnimationProgressBar(this);
        adapter = new CategoryAdapter();
        categoryListView.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                loadCategory();
            }
        });

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                refreshLayout.finishLoadmore(2000);
            }
        });

        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadFailView.setVisibility(View.GONE);
                loadCategory();
            }
        });
        loadCategory();
    }

    private void loadCategory() {
        if (firstLoad) {
            animationProgressBar.show();
        }
        HttpRequest.get(AppConfig.CATEGORY_LIST, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                animationProgressBar.dismiss();
                refreshLayout.finishRefresh();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    categoryList = JsonUtils.getJsonArray(response, "data", null);
                    adapter.notifyDataSetChanged();
                    firstLoad = false;
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

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
        } else {
            AlertManager.showErrorInfo(RefreshListActivity.this);
        }
    }

    class CategoryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return categoryList == null ? 0 : categoryList.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(categoryList, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = new TextView(RefreshListActivity.this);
                view.setLayoutParams(new ViewGroup.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, 100));
            }
            ((TextView) view).setText(JsonUtils.getString(getItem(i), "name", "--"));
            return view;
        }
    }
}
