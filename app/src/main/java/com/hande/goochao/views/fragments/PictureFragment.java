package com.hande.goochao.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hande.goochao.MainActivity;
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
import com.hande.goochao.views.activity.DesignerActivity;
import com.hande.goochao.views.activity.PayCenterActivity;
import com.hande.goochao.views.activity.ScenePreviewActivity;
import com.hande.goochao.views.activity.SearchActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.ZoomView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Lmc on 2018/1/29.
 */
@ContentView(R.layout.fragment_picture)
public class PictureFragment extends BaseFragment implements ScenePreviewActivity.OnPageChangeListener {

    @ViewInject(R.id.refresh_layout_pictureFragment_gird)
    private RefreshLayout refreshLayout;

    @ViewInject(R.id.load_fail_view_pictureFragment_gird)
    private LoadFailView loadFailView;

    @ViewInject(R.id.end_view)
    private View endView;

    private AnimationProgressBar animationProgressBar;
    //private CustomLoadingDialog loadingDialog;

    private boolean below = false;

    private PictureAdapter adapter;
    private RegionAdapter regionAdapter;
    private StyleAdapter styleAdapter;

    private JSONArray queryResource = new JSONArray();
    private JSONArray queryResource1 = new JSONArray();
    private JSONArray pictureResource;
    private JSONArray pictureList = new JSONArray();

    private JSONArray regionList = new JSONArray();
    private JSONArray styleList = new JSONArray();

    private boolean firstLoad = true;

    private PopupWindow popupWindowType;
    private PopupWindow popupWindowArea;
    private PopupWindow popupWindowStyle;
    private View typeSelectView;
    private View areaSelectView;
    private View styleSelectView;
    private TextView allText;
    private ImageView typeSelectIcon;
    private ImageView areSelectIcon;
    private ImageView styleSelectIcon;
    private ImageView closeAll;
    private ImageView closeRegion;
    private ImageView closeStyle;
    private ImageView regionSelect;
    private ImageView styleSelect;
    private TextView region_text;
    private TextView style_text;
    private View mirror_view_all;
    private View mirror_view;
    private View mirror_view_style;
    private GridView gridView;
    private ListView listView;
    private int pageIndex = 1;
    private int pageSize = 20;
    private String regionChoose;
    private String styleChoose;
    private String type = "";
    private boolean isLoading;

    private GlideRequests glide;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("场景图");
        ((MainActivity) getActivity()).setSearchType(SearchActivity.SEARCH_PICTURE);
        if (firstLoad && !isLoading){
            glide = GlideApp.with(getActivity());
            typeSelectView = getActivity().findViewById(R.id.type_view);
            areaSelectView = getActivity().findViewById(R.id.area_view);
            styleSelectView = getActivity().findViewById(R.id.style_view);
            gridView = getActivity().findViewById(R.id.gridview);
            region_text = getActivity().findViewById(R.id.region_text);
            style_text = getActivity().findViewById(R.id.style_text);
            mirror_view = getActivity().findViewById(R.id.mirror_view);
            mirror_view_style = getActivity().findViewById(R.id.mirror_view_style);
            allText = getActivity().findViewById(R.id.all_text);

            typeSelectView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopViewAll();
                    judge(typeSelectView);
                }
            });
            areaSelectView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopViewRegion();
                    judge(areaSelectView);
                }
            });

            styleSelectView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopViewStyle();
                    judge(styleSelectView);
                }
            });
            //loadingDialog = new CustomLoadingDialog(getActivity());
            animationProgressBar = new AnimationProgressBar(getActivity());
            adapter = new PictureAdapter();
            gridView.setAdapter(adapter);

            /**
             * 下拉重新加载，上拉刷新
             */
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshLayout) {
                    pageIndex = 1;
                    loadMorePicture();
                    below = false;
                    endView.setVisibility(View.GONE);
                }
            });
            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshLayout) {
                    if (below){
                        endView.setVisibility(View.VISIBLE);
                        refreshLayout.finishLoadMore();
                    }else {
                        pageIndex++;
                        loadMorePicture();
                    }
                }
            });
            loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
                @Override
                public void onReload() {
                    loadMorePicture();
                }
            });
            loadPicture();
            loadItem();
            loadItem_style();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ScenePreviewActivity.setOnPageChangeListener(PictureFragment.this);
    }

    private void showPopViewAll() {

        if (popupWindowType == null){

            final View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.popwindow_picture_all, null);
            popupWindowType = new PopupWindow(contentView,
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            popupWindowType.setContentView(contentView);
            popupWindowType.setAnimationStyle(R.style.mypopwindow_anim_style);

            mirror_view_all = contentView.findViewById(R.id.mirror_view_all);
            mirror_view_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindowType.dismiss();
                    judge(typeSelectView);
                }
            });

            closeAll=contentView.findViewById(R.id.closeAll);
            closeAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindowType.dismiss();
                    judge(typeSelectView);
                }
            });
            typeSelectIcon = contentView.findViewById(R.id.type_icon);
            areSelectIcon = contentView.findViewById(R.id.area_icon);
            styleSelectIcon = contentView.findViewById(R.id.style_icon);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            popupWindowType.setWidth(Math.round(screenWidth));

            View textView = contentView.findViewById(R.id.text_all1);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int id = v.getId();
                    switch (id) {
                        case R.id.text_all1: {
                            allText.setText("全部");
                            allText.setTextColor(getResources().getColor(R.color.BLACK_GRAY_TRANS));
                            typeSelectIcon.setVisibility(View.VISIBLE);
                            areSelectIcon.setVisibility(View.GONE);
                            styleSelectIcon.setVisibility(View.GONE);
                            pageIndex = 1;
                            type = "";
                            loadMorePicture();
                            popupWindowType.dismiss();
                            judge(typeSelectView);
                        }
                    }
                }
            });

            View textView1 = contentView.findViewById(R.id.text_all2);
            textView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int id = v.getId();
                    switch (id) {
                        case R.id.text_all2: {
                            allText.setText("家具定制");
                            allText.setTextColor(getResources().getColor(R.color.BLACK));
                            areSelectIcon.setVisibility(View.VISIBLE);
                            typeSelectIcon.setVisibility(View.GONE);
                            styleSelectIcon.setVisibility(View.GONE);
                            pageIndex = 1;
                            type = "1";
                            loadMorePicture();
                            popupWindowType.dismiss();
                            judge(typeSelectView);
                        }
                    }
                }
            });

            View textView2 = contentView.findViewById(R.id.text_all3);
            textView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int id = v.getId();
                    switch (id) {
                        case R.id.text_all3: {
                            allText.setText("场景售卖");
                            allText.setTextColor(getResources().getColor(R.color.BLACK));
                            styleSelectIcon.setVisibility(View.VISIBLE);
                            typeSelectIcon.setVisibility(View.GONE);
                            areSelectIcon.setVisibility(View.GONE);
                            pageIndex = 1;
                            type = "2";
                            loadMorePicture();
                            popupWindowType.dismiss();
                            judge(typeSelectView);
                        }
                    }
                }
            });
        }

        View rootViewAll = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_picture, null);
        popupWindowType.showAtLocation(rootViewAll, Gravity.BOTTOM, 0, 0);
    }

    private void showPopViewRegion() {

        if (popupWindowArea == null){
            final View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.popwindow_picture_region, null);
            popupWindowArea = new PopupWindow(contentView,
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            popupWindowArea.setContentView(contentView);
            popupWindowArea.setAnimationStyle(R.style.mypopwindow_anim_style);

            mirror_view = contentView.findViewById(R.id.mirror_view);
            listView = contentView.findViewById(R.id.popview);
            closeRegion=contentView.findViewById(R.id.closeRegion);
            closeRegion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindowArea.dismiss();
                    judge(areaSelectView);
                }
            });
            mirror_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindowArea.dismiss();
                    judge(areaSelectView);
                }
            });

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            popupWindowArea.setWidth(Math.round(screenWidth));

            regionAdapter = new RegionAdapter();
            listView.setAdapter(regionAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {

                    region_text.setText(JsonUtils.getString(JsonUtils.getJsonItem(queryResource, i, null),"name",""));
                    region_text.setTextColor(getResources().getColor(R.color.BLACK));
                    try{
                        for (int i1 = 0; i1 < queryResource.length(); i1++) {
                            queryResource.getJSONObject(i1).put("checked", false);
                        }
                        queryResource.getJSONObject(i).put("checked", true);
                    } catch (Exception ex) {

                    }

                    regionChoose = JsonUtils.getString(JsonUtils.getJsonItem(queryResource, i, null),"areaId","");
                    pageIndex = 1;
                    loadMorePicture();
                    regionAdapter.notifyDataSetChanged();
                    popupWindowArea.dismiss();
                    judge(areaSelectView);
                }
            });
        }
        View rootViewRegion = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_picture, null);
        popupWindowArea.showAtLocation(rootViewRegion, Gravity.BOTTOM, 0, 0);
    }

    private void showPopViewStyle() {

       if (popupWindowStyle == null){
           final View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.popwindow_picture_style, null);
           popupWindowStyle = new PopupWindow(contentView,
                   ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
           popupWindowStyle.setContentView(contentView);
           popupWindowStyle.setAnimationStyle(R.style.mypopwindow_anim_style);

           closeStyle = contentView.findViewById(R.id.closeStyle);
           closeStyle.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   popupWindowStyle.dismiss();
                   judge(styleSelectView);
               }
           });
           mirror_view_style = contentView.findViewById(R.id.mirror_view_style);
           listView = contentView.findViewById(R.id.popview_style);
           mirror_view_style.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   popupWindowStyle.dismiss();
                   judge(styleSelectView);
               }
           });

           int screenWidth = getResources().getDisplayMetrics().widthPixels;
           popupWindowStyle.setWidth(Math.round(screenWidth));

           styleAdapter = new StyleAdapter();
           listView.setAdapter(styleAdapter);

           listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
               @Override
               public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {

                   style_text.setText(JsonUtils.getString(JsonUtils.getJsonItem(queryResource1, i, null),"name",""));
                   style_text.setTextColor(getResources().getColor(R.color.BLACK));
                   try{
                       for (int i1 = 0; i1 < queryResource1.length(); i1++) {
                           queryResource1.getJSONObject(i1).put("checked", false);
                       }
                       queryResource1.getJSONObject(i).put("checked", true);
                   } catch (Exception ex) {

                   }
                   styleChoose = JsonUtils.getString(JsonUtils.getJsonItem(queryResource1, i, null),"styleId","");
                   pageIndex = 1;
                   loadMorePicture();
                   styleAdapter.notifyDataSetChanged();
                   popupWindowStyle.dismiss();
                   judge(styleSelectView);
               }
           });
       }
        View rootViewStyle = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_picture, null);
        popupWindowStyle.showAtLocation(rootViewStyle, Gravity.BOTTOM, 0, 0);
    }

    private void loadItem() {
        HttpRequest.get(AppConfig.QUERY_INFORMATION_REGION, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                refreshLayout.finishRefresh();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    regionList = JsonUtils.getJsonArray(response, "data", null);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("name", "全部");
                        queryResource.put(0, jsonObject);
                    } catch (JSONException ej) { }
                    for (int m = 0; m < regionList.length();m++){
                        try {
                            queryResource.put(regionList.get(m));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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

    private void loadItem_style() {
        HttpRequest.get(AppConfig.QUERY_INFORMATION_STYLE, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                refreshLayout.finishRefresh();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    styleList = JsonUtils.getJsonArray(response, "data", null);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("name", "全部");
                        queryResource1.put(0, jsonObject);
                    } catch (JSONException ej) { }
                    for (int m = 0; m < styleList.length();m++){
                        try {
                            queryResource1.put(styleList.get(m));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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

    @Override
    public void onPageChange(int currentIndex, int size) {
        AppLog.i("currentIndex:" + currentIndex + ", size:" + size);
        gridView.smoothScrollToPositionFromTop (currentIndex, 0);
        int currentPage = currentIndex + 1;
        if (!isLoading && size - currentPage <= 5) {
            AppLog.i("loading next data");
            pageIndex++;
            this.loadPicture();
        }
    }

    class RegionAdapter extends BaseAdapter {

        class ViewHolder {
            TextView txtValue;

            ViewHolder(TextView txtValue) {
                this.txtValue = txtValue;
            }
        }

        @Override
        public int getCount() {
            return queryResource == null ? 0 : queryResource.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(queryResource, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.popwindow_picture_region_item, viewGroup, false);
                view.setTag(new ViewHolder((TextView) view.findViewById(R.id.region_item)));
            }
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.txtValue.setText(JsonUtils.getString(getItem(i),"name",""));
            boolean check = JsonUtils.getBoolean(getItem(i), "checked", false);
            regionSelect=view.findViewById(R.id.regionSelect);
            if (check){
                regionSelect.setVisibility(View.VISIBLE);
            }
            else {
                regionSelect.setVisibility(View.GONE);
            }
            return view;
        }
    }

    class StyleAdapter extends BaseAdapter {

        class ViewHolder {
            TextView txtValue2;

            ViewHolder(TextView txtValue2) {
                this.txtValue2 = txtValue2;
            }
        }

        @Override
        public int getCount() {
            return queryResource1 == null ? 0 : queryResource1.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(queryResource1, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.popwindow_picture_style_item, viewGroup, false);
                view.setTag(new ViewHolder((TextView) view.findViewById(R.id.style_item)));
            }
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.txtValue2.setText(JsonUtils.getString(getItem(i),"name",""));
            boolean check = JsonUtils.getBoolean(getItem(i), "checked", false);
            styleSelect=view.findViewById(R.id.styleSelect);
            if (check){
                styleSelect.setVisibility(View.VISIBLE);
            }
            else {
                styleSelect.setVisibility(View.GONE);
            }
            return view;
        }
    }

    /**
     * 判断view点击状态事件
     * @param v
     */
    public void judge(View v){
        if(v.isSelected() == false){
            v.setSelected(true);
        }
        else{
            v.setSelected(false);
        }
    }

    private void loadPicture() {
        if (firstLoad) {
            animationProgressBar.show();
        }

        loadMorePicture();
    }

    private void loadMorePicture() {
        Map<String, String> params = new HashMap<>();
        if (regionChoose != null || styleChoose != null) {
            if(regionChoose == null && styleChoose != null){
                params.put("styleId", "" + styleChoose);
                params.put("pageIndex", "" + pageIndex);
                params.put("pageSize",""+ pageSize);
                params.put("tagType", "" + type);
                params.put("loadDetail","" + true);
            }
            else if(regionChoose != null && styleChoose == null){
                params.put("areaId", "" + regionChoose);
                params.put("pageIndex", "" + pageIndex);
                params.put("pageSize",""+ pageSize);
                params.put("tagType", "" + type);
                params.put("loadDetail","" + true);
            }
            else if(regionChoose != null && styleChoose != null){
                params.put("areaId", "" + regionChoose);
                params.put("styleId", "" + styleChoose);
                params.put("pageIndex", "" + pageIndex);
                params.put("pageSize",""+ pageSize);
                params.put("tagType", "" + type);
                params.put("loadDetail","" + true);
            }
        }
        else {
            params.put("loadDetail","" + true);
            params.put("pageIndex", "" + pageIndex);
            params.put("pageSize",""+ pageSize);
            params.put("tagType", "" + type);
        }
        isLoading = true;

        //loadingDialog.show();
        HttpRequest.get(AppConfig.PICTURE_GET, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                isLoading = false;
                animationProgressBar.dismiss();
                //loadingDialog.dismiss();
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (pageIndex == 1) {
                    pictureList = new JSONArray();
                }
                if (JsonUtils.getCode(response) == 0) {
                    pictureResource = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < pictureResource.length(); i++) {
                        try {
                            pictureList.put(pictureResource.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if (pictureResource.length() < pageSize) {
                        below = true;
                    } else {
                        below = false;
                    }
                    adapter.notifyDataSetChanged();
                    firstLoad = false;
                    resetView();
                    notifyPreview();
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(getContext(), "服务器繁忙，请稍后重试", false);
                AppLog.e("err", ex);
                showError();
            }
        });
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
        } else {
            refreshLayout.finishRefresh();
            refreshLayout.finishLoadMore();
            //loadingDialog.dismiss();
            AlertManager.showErrorInfo(getActivity());
        }
    }

    class PictureAdapter extends BaseAdapter implements View.OnClickListener {

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
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.gridview_picture_item, viewGroup, false);
                ZoomView zoomView = view.findViewById(R.id.zoom_view);
                zoomView.setOnClickListener(this);
            }

            JSONObject item = getItem(i);
            ZoomView zoomView = view.findViewById(R.id.zoom_view);
            zoomView.setTag(i);
            ImageView imageView = view.findViewById(R.id.picture_view);
            ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(item, "cover", "--"), 500, 500), imageView, R.mipmap.loadpicture);

            return view;
        }

        @Override
        public void onClick(View v) {
            ZoomView zoomView = ((ZoomView) v);
            ScenePreviewActivity.setOnPageChangeListener(PictureFragment.this);
            AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, pictureList);
            Intent intent = new Intent(getActivity(), ScenePreviewActivity.class);
            intent.putExtra("currentIndex", ((Integer) zoomView.getTag()));
            startActivity(intent);
            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }


}
