package com.hande.goochao.views.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
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
import com.hande.goochao.views.activity.ScenePreviewActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.HeaderGridView;
import com.hande.goochao.views.components.ConfirmDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.ZoomView;
import com.hande.goochao.views.components.refresh.NoDataTwoLineView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LMC on 2018/3/15.
 */

@ContentView(R.layout.fragment_collection_picture)
public class CollectionPictureFragment extends BaseFragment {

    private JSONArray pictureResource;
    private JSONArray pictureList = new JSONArray();

    @ViewInject(R.id.collection_picture_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.collection_picture_load_fail)
    private LoadFailView loadFailView;
    @ViewInject(R.id.collection_pic_noDataView)
    private NoDataTwoLineView noDataView;

    private boolean firstLoad = true;
    private boolean loaded = false;

    private GlideRequests glide;


    private PictureListAdapter pictureListAdapter;
    @ViewInject(R.id.collection_picture_grid_view)
    private HeaderGridView pictureGridView;

    private CustomLoadingDialog loadingDialog;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!loaded) {
            glide = GlideApp.with(getActivity());
            pictureListAdapter = new PictureListAdapter();
            pictureGridView.setAdapter(pictureListAdapter);

            loadingDialog = new CustomLoadingDialog(getActivity());

            loaded = true;

            noDataView.setImageAndText(R.mipmap.new_start_nodata, "收藏空空如也" , "快去逛逛图片吧");

            loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
                @Override
                public void onReload() {
                    loadPicture();
                }
            });
            loadPicture();
        }
    }

    private void loadPicture() {
        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        }
        Map<String, String> params = new HashMap<>();
        params.put("loadDetail", "" + true);
        HttpRequest.get(AppConfig.COLLECTION_PICTURE, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    pictureResource = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < pictureResource.length(); i++) {
                        try {
                            pictureList.put(pictureResource.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if (pictureResource.length() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                    }
                    pictureListAdapter.notifyDataSetChanged();
                    firstLoad = false;
                    notifyPreview();
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

    class PictureListAdapter extends BaseAdapter implements View.OnClickListener {

        class PictureViewHolder {
            ImageView image;
            ZoomView zoomView;

            PictureViewHolder(View view){
                image = view.findViewById(R.id.picture_view);
                zoomView = view.findViewById(R.id.picture_zoom_view);
            }

        }

        @Override
        public int getCount() {
            return pictureList == null ? 0 : pictureList.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(pictureList, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                // 获得容器
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_collection_picture_item, null);
                convertView.setTag(new PictureViewHolder(convertView));
            }
            PictureViewHolder viewHolder = (PictureViewHolder)convertView.getTag();

                // 初始化组件
                viewHolder.zoomView.setOnClickListener(this);

            // 给组件设置资源
            JSONObject item = getItem(i);
            viewHolder.zoomView.setTag(i);
            ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(item, "scenePictureVo.cover", ""), 500, 500), viewHolder.image, R.mipmap.loadpicture);

            return convertView;
        }

        @Override
        public void onClick(View v) {
            ZoomView zoomView = ((ZoomView) v);
            AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, pictureList);
            ScenePreviewActivity.setOnPageChangeListener(null);
            Intent intent = new Intent(getActivity(), ScenePreviewActivity.class);
            intent.putExtra("currentIndex", ((Integer) zoomView.getTag()));
            startActivity(intent);
            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void notifyPreview() {
        AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, pictureList);
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
            AlertManager.showErrorInfo(this.getActivity());
            loadingView.setVisibility(View.GONE);
        }
    }
}


