package com.hande.goochao.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.hande.goochao.MainActivity;
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
import com.hande.goochao.utils.PriceUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.FurnitureListActivity;
import com.hande.goochao.views.activity.MagazineDetailActivity;
import com.hande.goochao.views.activity.MagazineListActivity;
import com.hande.goochao.views.activity.ProductInformationActivity;
import com.hande.goochao.views.activity.ProductListActivity;
import com.hande.goochao.views.activity.ScenePreviewActivity;
import com.hande.goochao.views.activity.SearchActivity;
import com.hande.goochao.views.activity.SpaceDetailActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.AnimationProgressBar;
import com.hande.goochao.views.components.WebViewActivity;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.ZoomView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ContentView(R.layout.fragment_home)
public class HomeFragment extends BaseFragment implements LoadFailView.OnReloadListener, OnRefreshListener, OnLoadMoreListener ,OnItemClickListener, View.OnClickListener , ScenePreviewActivity.OnPageChangeListener{

    private boolean firstLoad = true;
    private boolean loaded = false;
    private int pageIndex = 1;
    private int pageSize = 20;
    private int currentIndex = 0;
    private boolean noMoreData = false;

    private JSONArray recommends = new JSONArray();
    private JSONObject homes = new JSONObject();
    private JSONArray scenes = new JSONArray();

    @ViewInject(R.id.home_list_view)
    private ListView homeListView;
    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.refresh_layout)
    private RefreshLayout refreshLayout;

    private AnimationProgressBar animationProgressBar;

    private BaseAdapter adapter;
    private SceneAdapter sceneAdapter;

    private ConvenientBanner convenientBanner;
    private List<String> bannerImages = new ArrayList<>();
    private JSONArray sceneDatas = new JSONArray();

    private LinearLayout layCategoryContainer;
    private LinearLayout layFurnitureContainer;
    private LinearLayout layMagazineContainer;
    private GridView laySceneContainer;
    private LinearLayout headerViewMain;
    private TextView categoryLearnMore;
    private TextView furnitureLearnMore;
    private TextView magazineLearnMore;
    private TextView sceneLearnMore;

    /**
     * 标题加粗view
     */
    private TextView headerTitleFirst;
    private TextView headerTitleSecond;
    private TextView headerTitleThird;
    private TextView headerTitleFourth;


    private GlideRequests glide;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle("构巢Goochao");
        ((MainActivity) getActivity()).setSearchType(SearchActivity.SEARCH_ALL);

        if (!loaded) {
            glide = GlideApp.with(getActivity());
            LinearLayout homeHeaderView = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.view_home_header, homeListView, false);
            homeListView.addHeaderView(homeHeaderView);

            /**
             * 标题加粗
             */
            headerTitleFirst = homeHeaderView.findViewById(R.id.header_title_1);
            boldMethod(headerTitleFirst);
            headerTitleSecond = homeHeaderView.findViewById(R.id.header_title_2);
            boldMethod(headerTitleSecond);
            headerTitleThird = homeHeaderView.findViewById(R.id.header_title_3);
            boldMethod(headerTitleThird);
            headerTitleFourth = homeHeaderView.findViewById(R.id.header_title_4);
            boldMethod(headerTitleFourth);

            convenientBanner = homeHeaderView.findViewById(R.id.convenientBanner);
            layCategoryContainer = homeHeaderView.findViewById(R.id.lay_category_container);
            layFurnitureContainer = homeHeaderView.findViewById(R.id.furniture_content);
            layMagazineContainer = homeHeaderView.findViewById(R.id.lay_magazine_container);
            laySceneContainer = homeHeaderView.findViewById(R.id.scene_content);
            headerViewMain = homeHeaderView.findViewById(R.id.header_view_main);
            categoryLearnMore = homeHeaderView.findViewById(R.id.category_learnMore);
            furnitureLearnMore = homeHeaderView.findViewById(R.id.furniture_scene_learnMore);
            magazineLearnMore = homeHeaderView.findViewById(R.id.furniture_magazine_learnMore);
            sceneLearnMore = homeHeaderView.findViewById(R.id.furniture_customize_learnMore);

            animationProgressBar = new AnimationProgressBar(getActivity());
            loadFailView.setOnReloadListener(this);
            refreshLayout.setOnRefreshListener(this);
            refreshLayout.setOnLoadMoreListener(this);

            categoryLearnMore.setOnClickListener(this);
            furnitureLearnMore.setOnClickListener(this);
            magazineLearnMore.setOnClickListener(this);
            sceneLearnMore.setOnClickListener(this);

            sceneAdapter = new SceneAdapter();
            laySceneContainer.setAdapter(sceneAdapter);

            adapter = new RecommendsAdapter();
            homeListView.setAdapter(adapter);

            loadHome();
            loaded = true;
        }

    }

    private void loadHome(){

        if (firstLoad) {
            animationProgressBar.show();
        }

        HttpRequest.get(AppConfig.HOME_TOP, null,null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                animationProgressBar.dismiss();
                closeRefresh();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    homes = JsonUtils.getJsonObject(response, "data", null);
                    initHeader();
                    convenientBannerInit();
                    firstLoad = false;
                    resetView();
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

    private void loadRecommends() {
        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", pageIndex + "");
        params.put("pageSize", pageSize + "");

        HttpRequest.get(AppConfig.HOME_RECOMMENDS, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                closeRefresh();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    if (pageIndex == 1) {
                        recommends = new JSONArray();
                    }
                    JSONArray res = JsonUtils.getJsonArray(response, "data", new JSONArray());
                    JsonUtils.appendArray(recommends, res);
                    adapter.notifyDataSetChanged();
                    if (res.length() < pageSize) {
                        refreshLayout.setNoMoreData(true);
                        refreshLayout.setEnableLoadMore(false);
                        noMoreData = true;
                    }

                    String typeScene = "1";
                    for (int i = 0; i < res.length(); i++) {
                        String type = JsonUtils.getString(JsonUtils.getJsonItem(res, i, null), "type", "") ;
                        if( typeScene.equals( type ) ) {
                            try {
                                JsonUtils.getJsonItem(res, i, null).put("currentIndex",currentIndex);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            scenes.put(JsonUtils.getJsonItem(res, i, null));
                            currentIndex ++;
                        }
                    }
                    AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, scenes);
                    notifyPreview();
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
//
//    private void goBannerDetail(String bannerId) {
//
//        String url = RestfulUrl.build(AppConfig.BANNER_DETAIL, ":bannerId", bannerId);
//
//        HttpRequest.get(url, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
//            @Override
//            public void onComplete(boolean success, JSONObject response) {
//
//            }
//
//            @Override
//            public void onSuccess(JSONObject response) {
//                if (JsonUtils.getCode(response) == 0) {
//                    JSONObject data = JsonUtils.getJsonObject(response, "data", null);
//                    String articleId = JsonUtils.getString(JsonUtils.getJsonObject(data, "article", null),"articleId","");
//                    final String name = "活动详情";
//                    String url = AppConfig.ARTICLE_CONTENT + articleId;
//                    Intent intent = new Intent(getActivity(), WebViewActivity.class);
//                    intent.putExtra("title",name);
//                    intent.putExtra("url",url);
//                    startActivity(intent);
//                } else {
//                    showError();
//                }
//                closeRefresh();
//            }
//
//            @Override
//            public void onError(Throwable ex) {
//                showError();
//            }
//        });
//    }

    private void closeRefresh() {
        animationProgressBar.dismiss();
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
    }

    private void resetView() {
        loadFailView.setVisibility(View.GONE);
    }

    private void showError() {
        closeRefresh();
        if (firstLoad) {
            loadFailView.setVisibility(View.VISIBLE);
        } else {
            AlertManager.showErrorInfo(getActivity());
        }
    }

    @Override
    public void onReload() {
        loadHome();
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        pageIndex = 1;
        refreshLayout.setNoMoreData(false);
        refreshLayout.setEnableLoadMore(true);
        loadHome();
    }

    @Override
    public void onLoadMore(RefreshLayout refreshLayout) {
        pageIndex ++;
        loadRecommends();
    }

    private void initHeader() {
        final JSONArray categories = JsonUtils.getJsonArray(homes, "categories", null);
        JSONArray furniture = JsonUtils.getJsonArray(homes, "spaces", null);
        JSONArray magazine = JsonUtils.getJsonArray(homes, "subjectArticles", null);
        sceneDatas = JsonUtils.getJsonArray(homes ,"scenePictures", null);
        layCategoryContainer.removeAllViews();
        layFurnitureContainer.removeAllViews();
        layMagazineContainer.removeAllViews();

        //设置banner的高度
        int width = WindowUtils.getDeviceWidth(getActivity());
        int height = width * 350 / 1024;
        LinearLayout.LayoutParams bannerParams= (LinearLayout.LayoutParams) convenientBanner.getLayoutParams();
        bannerParams.height = height;
        convenientBanner.setLayoutParams(bannerParams);

        View itemFurnitureView = LayoutInflater.from(getActivity()).inflate(R.layout.view_home_furniture, layFurnitureContainer, false);
        View itemFurnitureView2 = null;
        View itemFurnitureView3 = null;
        layFurnitureContainer.addView(itemFurnitureView);

        if(furniture.length() >= 6 ){
            itemFurnitureView2 = LayoutInflater.from(getActivity()).inflate(R.layout.view_home_furniture_item2, layFurnitureContainer, false);
            layFurnitureContainer.addView(itemFurnitureView2);
        }

        if(furniture.length() >= 9){
            itemFurnitureView3 = LayoutInflater.from(getActivity()).inflate(R.layout.view_home_furniture_item3, layFurnitureContainer, false);
            layFurnitureContainer.addView(itemFurnitureView3);
        }

        for (int i = 0; i < categories.length(); i++) {

            View itemView = LayoutInflater.from(getActivity()).inflate(R.layout.view_home_category, layCategoryContainer, false);
            layCategoryContainer.addView(itemView);
            ImageView categoryImg = (ImageView) itemView.findViewById(R.id.category_img);

            int cateWidth = WindowUtils.getDeviceWidth(getActivity()) / 5;
            int cateHeight = WindowUtils.getDeviceWidth(getActivity()) / 5;
            LinearLayout.LayoutParams cateParams= (LinearLayout.LayoutParams) categoryImg.getLayoutParams();
            cateParams.height = cateHeight;
            cateParams.width = cateWidth;
            categoryImg.setLayoutParams(cateParams);

            ((TextView) itemView.findViewById(R.id.category_name)).setText(JsonUtils.getString(JsonUtils.getJsonItem(categories, i, null), "name", "-"));
            ImageUtils.loadImage(glide, ImageUtils.zoomResize( JsonUtils.getString(JsonUtils.getJsonItem(categories, i, null), "image", ""),500,500), categoryImg, R.mipmap.loadpicture);

            ZoomView zoomView = itemView.findViewById(R.id.category_zoom_view);
            final String categoryId = JsonUtils.getString(JsonUtils.getJsonItem(categories, i, null), "categoryId", "");
            zoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ProductListActivity.class);
                    intent.putExtra("categoryId", categoryId);
                    intent.putExtra("categoryList", categories.toString());
                    startActivity(intent);
                }
            });
        }

        for (int i = 0; i< furniture.length(); i++) {
            final String spaceId = JsonUtils.getString(JsonUtils.getJsonItem(furniture, i, null), "objectId", "");
            if (i == 0){
                String url =ImageUtils.zoomResize( JsonUtils.getString(JsonUtils.getJsonItem(furniture, i, null), "cover", "") ,512,512);
                ImageUtils.loadImage(glide, url, (ImageView) itemFurnitureView.findViewById(R.id.home_furniture_left), R.mipmap.loadpicture);

                LinearLayout homeFurnitureLeft = itemFurnitureView.findViewById(R.id.home_furniture_left_layout);
                int itemWidth = (WindowUtils.getDeviceWidth(getActivity()) - WindowUtils.dpToPixels(getActivity(),42)) / 2;
                LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) homeFurnitureLeft.getLayoutParams();
                params.width = itemWidth;
                params.height = itemWidth;
                homeFurnitureLeft.setLayoutParams(params);

                LinearLayout.LayoutParams imageparams= (LinearLayout.LayoutParams) itemFurnitureView.findViewById(R.id.home_furniture_left).getLayoutParams();
                imageparams.width = itemWidth;
                imageparams.height = itemWidth;
                itemFurnitureView.findViewById(R.id.home_furniture_left).setLayoutParams(imageparams);

                itemFurnitureView.findViewById(R.id.furniture_left_zoom_view).setOnClickListener(new SpaceOnClickListener(spaceId));
            }else if (i == 1){

                LinearLayout homeFurnitureRight = itemFurnitureView.findViewById(R.id.home_furniture_right_layout);
                int itemWidth = (WindowUtils.getDeviceWidth(getActivity()) - WindowUtils.dpToPixels(getActivity(),42)) / 2;
                int itemHeight = (itemWidth - WindowUtils.dpToPixels(getActivity(),10))/2;
                LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) homeFurnitureRight.getLayoutParams();
                params.width = itemWidth;
                params.height = itemWidth;
                homeFurnitureRight.setLayoutParams(params);

                LinearLayout.LayoutParams imageparams= (LinearLayout.LayoutParams) itemFurnitureView.findViewById(R.id.home_furniture_rightTop).getLayoutParams();
                imageparams.height = itemHeight;
                itemFurnitureView.findViewById(R.id.home_furniture_rightTop).setLayoutParams(imageparams);

                String url = JsonUtils.getString(JsonUtils.getJsonItem(furniture, i, null), "cover", "") + "?imageView2/0/w/512/h/256|imageslim";
                ImageUtils.loadImage(glide, url, (ImageView) itemFurnitureView.findViewById(R.id.home_furniture_rightTop), R.mipmap.loadpicture);

                itemFurnitureView.findViewById(R.id.furniture_right_top_zoom_view).setOnClickListener(new SpaceOnClickListener(spaceId));
            }else if (i == 2){
                int itemWidth = (WindowUtils.getDeviceWidth(getActivity()) - WindowUtils.dpToPixels(getActivity(),42)) / 2;
                int itemHeight = (itemWidth - WindowUtils.dpToPixels(getActivity(),10))/2;
                LinearLayout.LayoutParams imageparams= (LinearLayout.LayoutParams) itemFurnitureView.findViewById(R.id.home_furniture_rightBottom).getLayoutParams();
                imageparams.width = itemWidth;
                imageparams.height = itemHeight;
                itemFurnitureView.findViewById(R.id.home_furniture_rightBottom).setLayoutParams(imageparams);

                String url = JsonUtils.getString(JsonUtils.getJsonItem(furniture, i, null), "cover", "") + "?imageView2/0/w/512/h/256|imageslim";
                ImageUtils.loadImage(glide, url, (ImageView) itemFurnitureView.findViewById(R.id.home_furniture_rightBottom), R.mipmap.loadpicture);

                itemFurnitureView.findViewById(R.id.furniture_right_bottom_zoom_view).setOnClickListener(new SpaceOnClickListener(spaceId));
            }else if(i == 3 && furniture.length() >= 6){
                LinearLayout homeFurnitureRight = itemFurnitureView2.findViewById(R.id.home_furniture_right_layout);
                int itemWidth = (WindowUtils.getDeviceWidth(getActivity()) - WindowUtils.dpToPixels(getActivity(),42)) / 2;
                int itemHeight = (itemWidth - WindowUtils.dpToPixels(getActivity(),10))/2;
                LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) homeFurnitureRight.getLayoutParams();
                params.width = itemWidth;
                params.height = itemWidth;
                homeFurnitureRight.setLayoutParams(params);

                LinearLayout.LayoutParams imageparams= (LinearLayout.LayoutParams) itemFurnitureView2.findViewById(R.id.home_furniture_rightTop).getLayoutParams();
                imageparams.width = itemWidth;
                imageparams.height = itemHeight;
                itemFurnitureView2.findViewById(R.id.home_furniture_rightTop).setLayoutParams(imageparams);

                String url = JsonUtils.getString(JsonUtils.getJsonItem(furniture, i, null), "cover", "") + "?imageView2/0/w/512/h/256|imageslim";
                ImageUtils.loadImage(glide, url, (ImageView) itemFurnitureView2.findViewById(R.id.home_furniture_rightTop), R.mipmap.loadpicture);

                itemFurnitureView2.findViewById(R.id.furniture_right_top_zoom_view).setOnClickListener(new SpaceOnClickListener(spaceId));

            }else if(i == 4 && furniture.length() >= 6){

                int itemWidth = (WindowUtils.getDeviceWidth(getActivity()) - WindowUtils.dpToPixels(getActivity(),42)) / 2;
                int itemHeight = (itemWidth - WindowUtils.dpToPixels(getActivity(),10))/2;
                LinearLayout.LayoutParams imageparams= (LinearLayout.LayoutParams) itemFurnitureView2.findViewById(R.id.home_furniture_rightBottom).getLayoutParams();
                imageparams.width = itemWidth;
                imageparams.height = itemHeight;
                itemFurnitureView2.findViewById(R.id.home_furniture_rightBottom).setLayoutParams(imageparams);

                String url = JsonUtils.getString(JsonUtils.getJsonItem(furniture, i, null), "cover", "") + "?imageView2/0/w/512/h/256|imageslim";
                ImageUtils.loadImage(glide, url, (ImageView) itemFurnitureView2.findViewById(R.id.home_furniture_rightBottom), R.mipmap.loadpicture);
                itemFurnitureView2.findViewById(R.id.furniture_right_bottom_zoom_view).setOnClickListener(new SpaceOnClickListener(spaceId));

            }else if(i == 5 && furniture.length() >= 6){
                String url = JsonUtils.getString(JsonUtils.getJsonItem(furniture, i, null), "cover", "") + "?imageView2/0/w/1024/h/1024|imageslim" ;
                ImageUtils.loadImage(glide, url, (ImageView) itemFurnitureView2.findViewById(R.id.home_furniture_left), R.mipmap.loadpicture);

                LinearLayout homeFurnitureLeft = itemFurnitureView2.findViewById(R.id.home_furniture_left_layout);
                int itemWidth = (WindowUtils.getDeviceWidth(getActivity()) - WindowUtils.dpToPixels(getActivity(),42)) / 2;
                LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) homeFurnitureLeft.getLayoutParams();
                params.width = itemWidth;
                params.height = itemWidth;
                homeFurnitureLeft.setLayoutParams(params);

                LinearLayout.LayoutParams imageparams= (LinearLayout.LayoutParams) itemFurnitureView2.findViewById(R.id.home_furniture_left).getLayoutParams();
                imageparams.width = itemWidth;
                imageparams.height = itemWidth;
                itemFurnitureView2.findViewById(R.id.home_furniture_left).setLayoutParams(imageparams);

                itemFurnitureView2.findViewById(R.id.furniture_left_zoom_view).setOnClickListener(new SpaceOnClickListener(spaceId));

            }else if(i == 6 && furniture.length() >= 9){
                String url = JsonUtils.getString(JsonUtils.getJsonItem(furniture, i, null), "cover", "") + "?imageView2/0/w/1024/h/1024|imageslim" ;
                ImageUtils.loadImage(glide, url, (ImageView) itemFurnitureView3.findViewById(R.id.home_furniture_left), R.mipmap.loadpicture);

                LinearLayout homeFurnitureLeft = itemFurnitureView3.findViewById(R.id.home_furniture_left_layout);
                int itemWidth = (WindowUtils.getDeviceWidth(getActivity()) - WindowUtils.dpToPixels(getActivity(),42)) / 2;
                LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) homeFurnitureLeft.getLayoutParams();
                params.width = itemWidth;
                params.height = itemWidth;
                homeFurnitureLeft.setLayoutParams(params);

                LinearLayout.LayoutParams imageparams= (LinearLayout.LayoutParams) itemFurnitureView3.findViewById(R.id.home_furniture_left).getLayoutParams();
                imageparams.width = itemWidth;
                imageparams.height = itemWidth;
                itemFurnitureView3.findViewById(R.id.home_furniture_left).setLayoutParams(imageparams);

                itemFurnitureView3.findViewById(R.id.furniture_left_zoom_view).setOnClickListener(new SpaceOnClickListener(spaceId));

            }else if(i == 7 && furniture.length() >= 9){
                LinearLayout homeFurnitureRight = itemFurnitureView3.findViewById(R.id.home_furniture_right_layout);
                int itemWidth = (WindowUtils.getDeviceWidth(getActivity()) - WindowUtils.dpToPixels(getActivity(),42)) / 2;
                int itemHeight = (itemWidth - WindowUtils.dpToPixels(getActivity(),10))/2;
                LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) homeFurnitureRight.getLayoutParams();
                params.width = itemWidth;
                params.height = itemWidth;
                homeFurnitureRight.setLayoutParams(params);

                LinearLayout.LayoutParams imageparams= (LinearLayout.LayoutParams) itemFurnitureView3.findViewById(R.id.home_furniture_rightTop).getLayoutParams();
                imageparams.width = itemWidth;
                imageparams.height = itemHeight;
                itemFurnitureView3.findViewById(R.id.home_furniture_rightTop).setLayoutParams(imageparams);

                String url = JsonUtils.getString(JsonUtils.getJsonItem(furniture, i, null), "cover", "") + "?imageView2/0/w/512/h/256|imageslim";
                ImageUtils.loadImage(glide, url, (ImageView) itemFurnitureView3.findViewById(R.id.home_furniture_rightTop), R.mipmap.loadpicture);
                itemFurnitureView3.findViewById(R.id.furniture_right_top_zoom_view).setOnClickListener(new SpaceOnClickListener(spaceId));

            }else if(i == 8 && furniture.length() >= 9){

                int itemWidth = (WindowUtils.getDeviceWidth(getActivity()) - WindowUtils.dpToPixels(getActivity(),42)) / 2;
                int itemHeight = (itemWidth - WindowUtils.dpToPixels(getActivity(),10))/2;
                LinearLayout.LayoutParams imageparams= (LinearLayout.LayoutParams) itemFurnitureView3.findViewById(R.id.home_furniture_rightBottom).getLayoutParams();
                imageparams.width = itemWidth;
                imageparams.height = itemHeight;
                itemFurnitureView3.findViewById(R.id.home_furniture_rightBottom).setLayoutParams(imageparams);

                String url = JsonUtils.getString(JsonUtils.getJsonItem(furniture, i, null), "cover", "") + "?imageView2/0/w/512/h/256|imageslim";
                ImageUtils.loadImage(glide, url, (ImageView) itemFurnitureView3.findViewById(R.id.home_furniture_rightBottom), R.mipmap.loadpicture);
                itemFurnitureView3.findViewById(R.id.furniture_right_bottom_zoom_view).setOnClickListener(new SpaceOnClickListener(spaceId));
            }
        }

        int itemWidth = (WindowUtils.getDeviceWidth(getActivity()) - WindowUtils.dpToPixels(getActivity(),46));
        int itemHeight = itemWidth * 284 / 512;

        for (int i = 0; i < magazine.length(); i++) {
            View itemView = LayoutInflater.from(getActivity()).inflate(R.layout.view_home_magazine, layMagazineContainer, false);
            layMagazineContainer.addView(itemView);
            JSONObject object = JsonUtils.getJsonObject(JsonUtils.getJsonItem(magazine, i, null), "data", new JSONObject());
            String cover =  ImageUtils.zoomResize(JsonUtils.getString(JsonUtils.getJsonItem(magazine, i, null), "cover", "" ),1025,568) ;
            String description = JsonUtils.getString(object, "description", "");
            final String subjectArticleId = JsonUtils.getString(object, "subjectArticleId", "" );
            ((TextView) itemView.findViewById(R.id.magazine_description)).setText(description);
            ImageUtils.loadImage(glide, cover, (ImageView) itemView.findViewById(R.id.magazine_img), R.mipmap.loadpicture);

            LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) itemView.findViewById(R.id.home_magazine_zoom_view).getLayoutParams();
            params.width = itemWidth;
            params.height = itemHeight;
            itemView.findViewById(R.id.home_magazine_zoom_view).setLayoutParams(params);
            itemView.findViewById(R.id.home_magazine_zoom_view).setOnClickListener(new MagazineOnClickListener(subjectArticleId));
        }

        for ( int i = 0; i < sceneDatas.length(); i++) {
            try {
                JsonUtils.getJsonItem(sceneDatas, i, null).put("currentIndex",currentIndex);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            scenes.put(JsonUtils.getJsonItem(sceneDatas, i, null));
            currentIndex ++;
        }
        AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, scenes);

        sceneAdapter.notifyDataSetChanged();
        loadRecommends();
        headerViewMain.setVisibility(View.VISIBLE);
        AppSession.getInstance().put(AppConst.MAGAZINE_LIST_SESSION,magazine);
        AppSession.getInstance().put(AppConst.CATEGORY_LIST_SESSION,categories);
    }

    class RecommendsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return recommends.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(recommends, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            JSONObject item = getItem(i);
            String typeGoods = "4";
            String typeMagazine = "3";
            String typeScene = "1";

            RecommendViewHolder viewHolder;

            if( view == null) {
                viewHolder = new RecommendViewHolder();
                view = LayoutInflater.from(getActivity()).inflate(R.layout.view_home_recommend, viewGroup, false);

                viewHolder.layoutGoods = view.findViewById(R.id.recommend_goods);
                viewHolder.layoutMagazine = view.findViewById(R.id.recommend_magazine);
                viewHolder.layoutScene = view.findViewById(R.id.recommend_scene);

                viewHolder.goodsImg = view.findViewById(R.id.home_recommend_goodsImg);
                viewHolder.goodsName = view.findViewById(R.id.home_recommend_goodsName);
                boldMethod(viewHolder.goodsName);
                viewHolder.goodsPrice = view.findViewById(R.id.home_recommend_goodsPrice);
                boldMethod(viewHolder.goodsPrice);
                viewHolder.goodsButton = view.findViewById(R.id.home_recommend_goodsBuy);

                viewHolder.magazineImg = view.findViewById(R.id.home_recommend_magazineImg);
                viewHolder.magazineDescription = view.findViewById(R.id.home_recommend_magazineText);

                viewHolder.sceneImg = view.findViewById(R.id.home_recommend_sceneImg);
                viewHolder.sceneTitle = view.findViewById(R.id.home_recommend_title);
                viewHolder.sceneCount = view.findViewById(R.id.home_recommend_count);

                viewHolder.recommendGoodsZoomView = view.findViewById(R.id.recommend_goods_zoom_view);
                viewHolder.recommendMagazineZoomView = view.findViewById(R.id.recommend_magazine_zoom_view);
                viewHolder.recommendSceneZoomView = view.findViewById(R.id.recommend_scene_zoom_view);

                view.setTag(viewHolder);
            } else {
                viewHolder = (RecommendViewHolder) view.getTag();
            }

            if (typeGoods.equals(JsonUtils.getString(item, "type", ""))){

                String url =  ImageUtils.zoomResize(JsonUtils.getString(item, "cover", "" ),1024,1024);
                JSONObject object = JsonUtils.getJsonObject(item, "object", new JSONObject());
                String goodsTitle = JsonUtils.getString(object, "title", "" );
                String goodsPrice = PriceUtils.beautify("¥" + JsonUtils.getString(object, "minPrice", ""));
                final String goodsId = JsonUtils.getString(object, "goodsId", "" );

                ImageUtils.loadImage(glide, url, viewHolder.goodsImg, R.mipmap.loadpicture);
                viewHolder.goodsName.setText(goodsTitle);
                viewHolder.goodsPrice.setText(goodsPrice);

                viewHolder.recommendGoodsZoomView.setOnClickListener(new GoodsOnClickListener(goodsId));
                viewHolder.goodsButton.setOnClickListener(new GoodsOnClickListener(goodsId));

                viewHolder.layoutGoods.setVisibility(View.VISIBLE);
                viewHolder.layoutMagazine.setVisibility(View.GONE);
                viewHolder.layoutScene.setVisibility(View.GONE);

            } else if (typeMagazine.equals(JsonUtils.getString(item, "type", ""))) {

                JSONObject object = JsonUtils.getJsonObject(item, "object", new JSONObject());
                String cover =  ImageUtils.zoomResize(JsonUtils.getString(object, "cover", "" ),1024,568) ;
                String description = JsonUtils.getString(object, "description", "" );
                final String subjectArticleId = JsonUtils.getString(object, "subjectArticleId", "" );
                ImageUtils.loadImage(glide, cover, viewHolder.magazineImg, R.mipmap.loadpicture);
                viewHolder.magazineDescription.setText(description);
                viewHolder.recommendMagazineZoomView.setOnClickListener(new MagazineOnClickListener(subjectArticleId));

                int itemWidth = (WindowUtils.getDeviceWidth(getActivity()) - WindowUtils.dpToPixels(getActivity(),32));
                int itemHeight = itemWidth * 284 / 512;
                LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) viewHolder.magazineImg.getLayoutParams();
                params.width = itemWidth;
                params.height = itemHeight;

                viewHolder.layoutGoods.setVisibility(View.GONE);
                viewHolder.layoutMagazine.setVisibility(View.VISIBLE);
                viewHolder.layoutScene.setVisibility(View.GONE);

            } else if (typeScene.equals(JsonUtils.getString(item, "type", ""))) {

                JSONObject object = JsonUtils.getJsonObject(item, "detail", new JSONObject());
                String cover =  ImageUtils.zoomResize(JsonUtils.getString(object, "cover", "" ),1024,1024) ;
                String count = JsonUtils.getString(object, "goodsCount", "" );
                String title = JsonUtils.getString(object, "name", "" );
                int index = JsonUtils.getInt(item, "currentIndex",0);

                ImageUtils.loadImage(glide, cover, viewHolder.sceneImg, R.mipmap.loadpicture);
                viewHolder.sceneCount.setText(count);
                viewHolder.sceneTitle.setText(title);
                viewHolder.recommendSceneZoomView.setOnClickListener(new SceneOnClickListener( index ));

                viewHolder.layoutGoods.setVisibility(View.GONE);
                viewHolder.layoutMagazine.setVisibility(View.GONE);
                viewHolder.layoutScene.setVisibility(View.VISIBLE);

            }

            return view;
        }
    }

    public class RecommendViewHolder {
        LinearLayout layoutGoods;
        LinearLayout layoutMagazine;
        LinearLayout layoutScene;

        ImageView goodsImg;
        TextView goodsName;
        TextView goodsPrice;
        Button goodsButton;

        ImageView magazineImg;
        TextView magazineDescription;

        ImageView sceneImg;
        TextView sceneTitle;
        TextView sceneCount;

        ZoomView recommendGoodsZoomView;
        ZoomView recommendMagazineZoomView;
        ZoomView recommendSceneZoomView;
    }

    class SceneAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return sceneDatas == null ? 0 : sceneDatas.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(sceneDatas, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            SceneViewHolder viewHolder;

            if (convertView == null) {
                int itemHeight = (WindowUtils.getDeviceWidth(getActivity()) - WindowUtils.dpToPixels(getActivity(),42)) / 2;

                viewHolder = new SceneViewHolder();

                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.view_home_scene, viewGroup, false);

                viewHolder.sceneImg = convertView.findViewById(R.id.home_recommend_sceneImg);
                viewHolder.sceneName = convertView.findViewById(R.id.home_recommend_title);
                viewHolder.sceneCount = convertView.findViewById(R.id.home_recommend_count);
                viewHolder.sceneZoomView = convertView.findViewById(R.id.home_scene_zoom_view);

                convertView.setTag(viewHolder);

                ViewGroup.LayoutParams params=laySceneContainer.getLayoutParams();

                if( getCount() % 2 == 1){
                    params.height = ( getCount() + 1 ) / 2 * (itemHeight + 16);
                } else {
                    params.height = ( getCount() ) / 2 * (itemHeight + 16);
                }
                laySceneContainer.setLayoutParams(params);

            } else {
                viewHolder = (SceneViewHolder) convertView.getTag();
            }

            JSONObject item = getItem(i);
            String cover = JsonUtils.getString(item, "cover", "");
            String count = JsonUtils.getString(item, "detail.praiseCount", "");
            String name = JsonUtils.getString(item, "detail.name", "");
            int index = JsonUtils.getInt(item, "currentIndex",0);

            viewHolder.sceneName.setText(name);
            viewHolder.sceneCount.setText(count);
            ImageUtils.loadImage(glide, ImageUtils.resize(cover, 500, 500), viewHolder.sceneImg, R.mipmap.loadpicture);
            viewHolder.sceneZoomView.setOnClickListener(new SceneOnClickListener(index));
            return convertView;
        }
    }

    public class SceneViewHolder {
        ImageView sceneImg;
        TextView sceneName;
        TextView sceneCount;
        ZoomView sceneZoomView;
    }

    private void convenientBannerInit(){
        JSONArray banners = JsonUtils.getJsonArray(homes, "banners", null);
        bannerImages.clear();

        for (int i = 0; i < banners.length(); i++) {
          String cover =  ImageUtils.zoomResize( JsonUtils.getString(JsonUtils.getJsonItem(banners, i, null), "cover", ""),1124,383);
          bannerImages.add(cover);
        }

        convenientBanner.setPages(
            new CBViewHolderCreator<NetworkImageHolderView>() {
                @Override
                public NetworkImageHolderView createHolder() {
                    return new NetworkImageHolderView();
                }
            }, bannerImages)
            .setPointViewVisible(true)
            .setPageIndicator(new int[]{R.drawable.ponit_normal, R.drawable.point_select})
            .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
            .startTurning(4000)
            .setManualPageable(true);

        convenientBanner.setOnItemClickListener(this);
    }

    public class NetworkImageHolderView implements Holder<String> {

        private ImageView imageView;

        @Override
        public View createView(Context context) {
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            return imageView;
        }

        @Override
        public void UpdateUI(Context context, int position, String data) {
            ImageUtils.loadImage(glide, data, imageView, R.mipmap.loadpicture);
        }
    }

    public void onItemClick(int position) {
        JSONArray banners = JsonUtils.getJsonArray(homes, "banners", null);
        String detailId = JsonUtils.getString(JsonUtils.getJsonItem(banners, position, null), "detailId", "");
//        goBannerDetail(detailId);
        final String name = "活动详情";
        String url = AppConfig.ARTICLE_CONTENT + detailId;
        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        intent.putExtra("title",name);
        intent.putExtra("url",url);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        if (view == categoryLearnMore) {
            ((MainActivity) getActivity()).setCurrentTab(1);
        }
        if (view == furnitureLearnMore) {
            Intent intent = new Intent(getActivity(), FurnitureListActivity.class);
            getActivity().startActivity(intent);
        }
        if (view == magazineLearnMore) {
            Intent intent = new Intent(getActivity(), MagazineListActivity.class);
            getActivity().startActivity(intent);
        }
        if (view == sceneLearnMore) {
            ((MainActivity) getActivity()).setCurrentTab(2);
        }
    }

    class SpaceOnClickListener implements View.OnClickListener {
        private String spaceId;

        SpaceOnClickListener(String spaceId) {
            this.spaceId = spaceId;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), SpaceDetailActivity.class);
            intent.putExtra("spaceId", spaceId);
            startActivity(intent);
        }
    }

    class GoodsOnClickListener implements View.OnClickListener {
        private String goodsId;

        GoodsOnClickListener(String goodsId) {
            this.goodsId = goodsId;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), ProductInformationActivity.class);
            intent.putExtra("goodsId",goodsId);
            startActivity(intent);
        }
    }

    class MagazineOnClickListener implements View.OnClickListener {
        private String subjectArticleId;

        MagazineOnClickListener(String subjectArticleId) {
            this.subjectArticleId = subjectArticleId;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), MagazineDetailActivity.class);
            intent.putExtra("subjectArticleId",subjectArticleId);
            startActivity(intent);
        }
    }

    class SceneOnClickListener implements View.OnClickListener {
        private int index;

        SceneOnClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View view) {
            ScenePreviewActivity.setOnPageChangeListener(HomeFragment.this);
            AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, scenes);
            Intent intent = new Intent(getActivity(), ScenePreviewActivity.class);
            intent.putExtra("currentIndex", index);
            startActivity(intent);
        }
    }

    public void onPageChange(int currentIndex, int size) {

        if (!noMoreData && size - currentIndex <= 3) {
            pageIndex++;
            loadRecommends();
        }
    }

    private void notifyPreview() {
        ScenePreviewActivity.refreshData();
    }

    private void boldMethod(TextView textView){
        TextPaint paint = textView.getPaint();
        paint.setFakeBoldText(true);
    }
}
