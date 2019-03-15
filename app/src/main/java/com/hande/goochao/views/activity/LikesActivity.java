package com.hande.goochao.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.hande.goochao.utils.DateUtils;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;
import com.hande.goochao.views.widget.GoochaoListView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Wangem on 2018/2/10.
 */

public class LikesActivity extends ToolBarActivity {

    @ViewInject(R.id.like_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.refresh_layout_view_like_list)
    private RefreshLayout refreshLayout;
    @ViewInject(R.id.load_fail_view_like_list)
    private LoadFailView loadFailView;
    @ViewInject(R.id.noDataView)
    private NoDataView noDataView;
    private boolean firstLoad = true;

    @ViewInject(R.id.like_list)
    private GoochaoListView likeListView;
    private LikeListAdapter likeListAdapter;
    private JSONArray likeResource;
    private JSONArray likeList;

    private int pageIndex = 1;
    private int pageSize = 20;

    private GlideRequests glide;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like);
        setTitle("我喜欢的");
        glide = GlideApp.with(this);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                pageIndex = 1;
                loadLikes();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                loadLikes();
            }
        });
        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadLikes();
            }
        });

        noDataView.setImageAndText(R.mipmap.no_likes, "没有喜欢何来的爱");

        likeListAdapter = new LikeListAdapter();
        likeListView.setAdapter(likeListAdapter);
        loadLikes();

        likeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String goodsId = JsonUtils.getString(JsonUtils.getJsonItem(likeList,position,null),"goodsId","");
                String title = JsonUtils.getString(JsonUtils.getJsonItem(likeList,position,null),"title","");
                Intent intent = new Intent(getApplication(),LikesItemActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("goodsId" , goodsId);
                bundle.putString("title" , title);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void loadLikes(){
        if (firstLoad){
            loadingView.setVisibility(View.VISIBLE);
        }
        Map<String,String> params = new HashMap<>();
        params.put("pageIndex", "" + pageIndex );
        params.put("pageSize", "" + pageSize );
        HttpRequest.get(AppConfig.LIKES_LIST ,null , params , JSONObject.class , new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (pageIndex == 1) {
                    likeList = new JSONArray();
                }
                if (JsonUtils.getCode(response) == 0){
                    likeResource = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < likeResource.length(); i++) {
                        try {
                            likeList.put(likeResource.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if (likeList.length() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                        if (likeResource.length() < pageSize) {
                            likeListView.setFooterViewVisibility(View.VISIBLE);
                            refreshLayout.setNoMoreData(true);
                            refreshLayout.setEnableLoadMore(false);
                        } else {
                            likeListView.setFooterViewVisibility(View.GONE);
                            refreshLayout.setNoMoreData(false);
                            refreshLayout.setEnableLoadMore(true);
                        }
                    }
                    likeListAdapter.notifyDataSetChanged();
                    firstLoad = false;
                    resetView();
                    notifyPreview();
                    pageIndex++;
                }else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(LikesActivity.this, message, false);
                }
            }
            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
                AlertManager.showErrorToast(LikesActivity.this, "服务器繁忙，请稍后重试", false);
            }
        });
    }

    class LikeListAdapter extends BaseAdapter {

        class ViewHolder {

            TextView txtLikeName;
            TextView txtLikeDate;
            TextView txtLikeIntroduce;
            TextView txtLikeContent;
            TextView txtLikeCount;
            LinearLayout likeContentLayout;
            TagFlowLayout tagFlowLayout;
//            ImageView deleteImg;

            ViewHolder(View view) {
                txtLikeName = view.findViewById(R.id.like_name);
                txtLikeDate = view.findViewById(R.id.like_date);
                txtLikeIntroduce = view.findViewById(R.id.like_introduce);
                txtLikeContent = view.findViewById(R.id.like_content);
                txtLikeCount = view.findViewById(R.id.like_count);
                likeContentLayout = view.findViewById(R.id.like_content_layout);
                tagFlowLayout = view.findViewById(R.id.like_tag_layout);
//                deleteImg = view.findViewById(R.id.delete_like_item);
            }
        }

        @Override
        public int getCount() {
            return likeList == null ? 0 : likeList.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(likeList, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(LikesActivity.this).inflate(R.layout.activity_likes_listview_item, viewGroup, false);
                view.setTag(new ViewHolder(view));
            }
            ViewHolder holder = (ViewHolder) view.getTag();

            ImageView imageView = view.findViewById(R.id.like_image);
            ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(getItem(i), "cover", ""),500,500), imageView, R.mipmap.loadpicture);

            holder.txtLikeName.setText(JsonUtils.getString(getItem(i),"title",""));
            holder.txtLikeDate.setText(DateUtils.timeStampToStr(JsonUtils.getLong(getItem(i),"createdAt",0),"yyyy-MM-dd"));
            holder.txtLikeIntroduce.setText(JsonUtils.getString(getItem(i),"description",""));
            JSONArray likeContent = JsonUtils.getJsonArray(getItem(i),"praiseCommentsVos",new JSONArray());
            String replayContent = JsonUtils.getString(JsonUtils.getJsonItem(likeContent, 0, null),"replayContent","");
            if ( Objects.equals(replayContent, "")){
                holder.likeContentLayout.setVisibility(View.GONE);
            }else {
                holder.likeContentLayout.setVisibility(View.VISIBLE);
                holder.txtLikeContent.setText(replayContent);
            }
            holder.txtLikeCount.setText("已有"+JsonUtils.getString(getItem(i),"praiseCount","")+"人喜欢");

            JSONArray praiseCommentsVos = JsonUtils.getJsonArray(getItem(i),"praiseCommentsVos",null);
            List<JSONObject> tags = getTags(JsonUtils.getJsonItem(praiseCommentsVos,0,null));
            if (tags != null) {
                // set adapter
                holder.tagFlowLayout.setAdapter(new TagAdapter<JSONObject>(tags) {
                    @Override
                    public View getView(FlowLayout parent, int position, JSONObject tag) {
                        View view = LayoutInflater.from(LikesActivity.this).inflate(R.layout.like_list_tag_item, parent, false);
                        TextView tv = view.findViewById(R.id.tag_text);
                        view.setTag(tag);
                        tv.setText(JsonUtils.getString(tag, "title", ""));
                        view.setSelected(true);
                        return view;
                    }
                });
            }
//            holder.deleteImg.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });

            return view;
        }
    }

    private void notifyPreview() {
        AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, likeList);
        ScenePreviewActivity.refreshData();
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void showError() {
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
        } else {
            loadingView.setVisibility(View.GONE);
            AlertManager.showErrorInfo(getApplicationContext());
        }
    }

    private List<JSONObject> getTags(JSONObject goodsInfo) {
        JSONArray tagArr = JsonUtils.getJsonArray(goodsInfo, "tagVos", null);
        if (tagArr == null) {
            return null;
        }
        List<JSONObject> tags = new ArrayList<>();
        for (int i = 0; i < tagArr.length(); i++) {
            JSONObject item = JsonUtils.getJsonItem(tagArr, i, null);
            if (item == null) {
                continue;
            }
            tags.add(item);
        }
        return tags;
    }
}
