package com.hande.goochao.views.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.commons.views.gallery.GalleryActivity;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.DateUtils;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CircleImageView;
import com.hande.goochao.views.components.ImageBox;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.NoDataView;
import com.hande.goochao.views.widget.GoochaoGridView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("ValidFragment")
@ContentView(R.layout.fragment_product_comments_list)
public class CommentsFragment extends BaseFragment {
    private String currentGoodsId = "";
    private Boolean image;
    private int pageIndex = 1;
    private int pageSize = 20;

    private boolean firstLoad = true;
    private boolean loaded = false;

    @ViewInject(R.id.comment_loading_view)
    private LoadingView loadingView;
    @ViewInject(R.id.comment_no_data_view)
    private NoDataView noDataView;
    @ViewInject(R.id.comment_fragment_load_fail)
    private LoadFailView loadFailView;
    @ViewInject(R.id.product_comments_list_refresh_layout)
    private RefreshLayout refreshLayout;

    private JSONArray commentsList = new JSONArray();
    private String[] images;

    private CommentsListAdapter commentsListAdapter;
    private GoochaoGridView productCommentsListGridView;

    private GlideRequests glide;

    public CommentsFragment(String currentGoodsId, Boolean image) {
        this.currentGoodsId = currentGoodsId;
        this.image = image;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (!loaded) {
            glide = GlideApp.with(getActivity());
            productCommentsListGridView = view.findViewById(R.id.productCommentListGridView);
            commentsListAdapter = new CommentsListAdapter();
            productCommentsListGridView.setAdapter(commentsListAdapter);
            if (!image) {
                noDataView.setImageAndText(R.mipmap.no_comment,"此商品还没有巢客晒单哦");
            } else {
                noDataView.setImageAndText(R.mipmap.no_comment,"此商品还没有巢客晒图哦");
            }

            loaded = true;

            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                public void onRefresh(RefreshLayout refreshLayout) {
                    pageIndex = 1;
                    loadComments();
                }
            });
            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshLayout) {
                    loadComments();
                }
            });
            loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
                @Override
                public void onReload() {
                    loadComments();
                }
            });
            loadComments();
        }
        return view;
    }

    //商品评论查询
    private void loadComments() {
        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        }
        String url = RestfulUrl.build(AppConfig.PRODUCT_COMMENTS, ":goodsId", currentGoodsId);
        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", "" + pageIndex);
        params.put("pageSize", "" + pageSize);
        params.put("image", "" + image);
        HttpRequest.get(url, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
            }

            @Override
            public void onSuccess(JSONObject response) {
                AppLog.i(response.toString());
                if (pageIndex == 1) {
                    commentsList = new JSONArray();
                }
                if (JsonUtils.getCode(response) == 0) {
                    JSONArray data = JsonUtils.getJsonArray(response, "data.data", null);
                    for (int i = 0; i < data.length(); i++) {
                        try {
                            commentsList.put(data.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if (data.length() == 0) {    //展示无数据
                        noDataView.setVisibility(View.VISIBLE);
                        productCommentsListGridView.setVisibility(View.GONE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                        productCommentsListGridView.setVisibility(View.VISIBLE);
                    }
                    if (data.length() < pageSize) {
                        productCommentsListGridView.setFooterViewVisibility(View.VISIBLE);
                        refreshLayout.setNoMoreData(true);
                        refreshLayout.setEnableLoadMore(false);
                    } else {
                        productCommentsListGridView.setFooterViewVisibility(View.GONE);
                        refreshLayout.setNoMoreData(false);
                        refreshLayout.setEnableLoadMore(true);
                    }
                    pageIndex++;
                    firstLoad = false;
                    AppLog.i(commentsList.toString());
                    commentsListAdapter.notifyDataSetChanged();
                    resetView();
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(getActivity(),"服务器繁忙，请稍后重试",false);
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
            loadingView.setVisibility(View.GONE);
        } else {
            AlertManager.showErrorInfo(getActivity());
            loadingView.setVisibility(View.GONE);
        }
    }

    class CommentsListAdapter extends BaseAdapter {

        class CommentsViewHolder {
            RatingBar ratingBar;
            CircleImageView userHeader;
            TextView userName;
            RatingBar commentsStar;
            TextView createdTime;
            TextView commentsContent;
            ImageBox commentPicture;

            CommentsViewHolder(View view){
                 ratingBar = view.findViewById(R.id.rating_bar);
                 userHeader = view.findViewById(R.id.userHeader);
                 userName = view.findViewById(R.id.userName);
                 commentsStar = view.findViewById(R.id.rating_bar);
                 createdTime = view.findViewById(R.id.creatTime);
                 commentsContent = view.findViewById(R.id.commentsContent);
                 commentPicture = view.findViewById(R.id.imageBox);
            }
        }

        @Override
        public int getCount() {
            return commentsList == null ? 0 : commentsList.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(commentsList, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                // 获得容器
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_product_comment_item, null);
                convertView.setTag(new CommentsViewHolder(convertView));
            }

            CommentsViewHolder viewHolder =(CommentsViewHolder) convertView.getTag();

            // 给组件设置资源
            JSONObject item = getItem(i);

            String imgUrl = JsonUtils.getString(item, "memberVo.head", "");
            if (imgUrl.equals("")){
                viewHolder.userHeader.setImageResource(R.mipmap.me_profilepic);
            }else {
                ImageUtils.loadImage(glide, ImageUtils.resize(imgUrl,500,500), viewHolder.userHeader, R.mipmap.loadpicture);
            }

            String userNameStr = JsonUtils.getString(item, "memberVo.nickName", "");
            viewHolder.userName.setText(userNameStr);

            int ratingStar = JsonUtils.getInt(item, "star", 0);
            float ratingStarF = (float) ratingStar;
            viewHolder.ratingBar.setRating(ratingStarF);

            String time = JsonUtils.getString(item, "createdAt", "");
            viewHolder.createdTime.setText(DateUtils.timeStampToStr(Long.parseLong(time), "yyyy-MM-dd HH:mm"));
            viewHolder.commentsContent.setText(JsonUtils.getString(item, "content", ""));

            String imageStrings = JsonUtils.getString(item, "images", "");
            images = imageStrings.split(",");
            if (imageStrings.equals("") || imageStrings == null) {

                viewHolder.commentPicture.setVisibility(View.GONE);

            } else {
                final List<String> imageList = new ArrayList<String>();
                for (String imageUrl : images) {
                    imageList.add(imageUrl);
                }
                viewHolder.commentPicture.setVisibility(View.VISIBLE);

                ImageAdapter imageAdapter = new ImageAdapter(imageList);
                viewHolder.commentPicture.setAdapter(imageAdapter);
                imageAdapter.notifyDataSetChanged();
            }

            return convertView;
        }
    }

    class ImageAdapter extends BaseAdapter {

        List<String> imageList = new ArrayList<>();

        ImageAdapter(List<String> imageList) {
            this.imageList = imageList;
        }

        class ImageViewHolder{

            ImageView imageView;
            ImageViewHolder(View view){
                imageView = view.findViewById(R.id.image_view);
            }
        }

        @Override
        public int getCount() {
            return imageList.size();
        }

        @Override
        public Object getItem(int position) {
            return imageList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            int itemWidth = (WindowUtils.getDeviceWidth(getActivity()) - WindowUtils.dpToPixels(getActivity(), 50)) / 4;

            final String currentSrc = getItem(position).toString();
            final String[] bannerImage = new String[imageList.size()];

            if (convertView == null){
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.image_layout, null);
                convertView.setTag(new ImageViewHolder(convertView));
            }

            ImageViewHolder viewHolder =(ImageViewHolder) convertView.getTag();

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(GridView.LayoutParams.MATCH_PARENT, itemWidth);
            viewHolder.imageView.setLayoutParams(params);

            for (int i = 0; i < imageList.size(); i++) {
                bannerImage[i] = imageList.get(i);
            }

            ImageUtils.loadImage(glide, ImageUtils.resize(getItem(position).toString(),500,500), viewHolder.imageView, R.mipmap.loadpicture);

            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), GalleryActivity.class);
                    intent.putExtra("isLocal", false);
                    intent.putExtra("currentSrc", currentSrc);
                    intent.putExtra("images", bannerImage);
                    startActivity(intent);
                    getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
            return convertView;
        }
    }

}
