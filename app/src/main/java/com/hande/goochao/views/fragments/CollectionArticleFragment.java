package com.hande.goochao.views.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.hande.goochao.views.activity.MagazineDetailActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.ConfirmDialog;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.refresh.NoDataTwoLineView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LMC on 2018/3/15.
 */

@ContentView(R.layout.fragment_collection_article)
public class CollectionArticleFragment extends BaseFragment {


    @ViewInject(R.id.collection_article_list_view)
    private ListView articleListView;
    private ArticleListAdapter articleListAdapter;

    @ViewInject(R.id.collection_article_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.collection_article_load_fail)
    private LoadFailView loadFailView;
    @ViewInject(R.id.collection_article_noDataView)
    private NoDataTwoLineView noDataView;

    private JSONArray articleResource;
    private JSONArray articleList = new JSONArray();

    private boolean firstLoad = true;
    private boolean loaded = false;

    private GlideRequests glide;
    private int kWidth;

    private CustomLoadingDialog loadingDialog;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!loaded) {
            glide = GlideApp.with(getActivity());
            loadingDialog = new CustomLoadingDialog(getActivity());

            kWidth = WindowUtils.getDeviceWidth(getActivity());

            articleListAdapter = new ArticleListAdapter();
            articleListView.setAdapter(articleListAdapter);
            articleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), MagazineDetailActivity.class);
                    String subjectId = JsonUtils.getString(JsonUtils.getJsonItem(articleList, position, null), "subjectArticleDetailVo.subjectArticleId", null);
                    intent.putExtra("subjectArticleId", subjectId);
                    getActivity().startActivity(intent);
                }
            });

            loaded = true;

            noDataView.setImageAndText(R.mipmap.new_start_nodata, "收藏空空如也", "快去逛逛期刊吧");

            loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
                @Override
                public void onReload() {
                    loadArticle();
                }
            });
            loadArticle();
        }
    }

    private void loadArticle() {
        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        }
        HttpRequest.get(AppConfig.COLLECTION_ARTICLE, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    articleResource = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < articleResource.length(); i++) {
                        try {
                            articleList.put(articleResource.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if (articleResource.length() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                    }
                    articleListAdapter.notifyDataSetChanged();
                    firstLoad = false;
                    resetView();
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(getActivity(), "服务器繁忙，请稍后重试", false);
                showError();
            }
        });
    }

    class ArticleListAdapter extends BaseAdapter {

        class ViewHolder {
            TextView txtTitleValue;
            ImageView imageView;
            TextView typeName;

            ViewHolder(View view) {
                txtTitleValue = view.findViewById(R.id.collection_article_desc);
                imageView = view.findViewById(R.id.collection_article_cover);
                typeName = view.findViewById(R.id.collection_type_name);
            }
        }

        @Override
        public int getCount() {
            return articleList == null ? 0 : articleList.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(articleList, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_collection_article_item, viewGroup, false);
                view.setTag(new ViewHolder(view));
            }

            ViewHolder holder = (ViewHolder) view.getTag();
            holder.txtTitleValue.setText(JsonUtils.getString(getItem(i), "subjectArticleDetailVo.title", ""));
            WindowUtils.boldMethod( holder.txtTitleValue);

            int imgWidth = kWidth - WindowUtils.dpToPixels(getActivity(),40);
            int imgHeight = imgWidth * 568 / 1024;

            ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(getItem(i), "subjectArticleDetailVo.cover", ""), 1024, 568), holder.imageView, R.mipmap.loadpicture);

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.imageView.getLayoutParams();
            params.width = imgWidth;
            params.height = imgHeight;

            holder.typeName.setText(JsonUtils.getString(getItem(i), "subjectArticleDetailVo.typeName", ""));
            return view;
        }
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
        } else {
            AlertManager.showErrorInfo(this.getActivity());
            loadingView.setVisibility(View.GONE);
        }
    }

}
