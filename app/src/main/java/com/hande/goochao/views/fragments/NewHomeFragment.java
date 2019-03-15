package com.hande.goochao.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.hande.goochao.MainActivity;
import com.hande.goochao.R;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.config.AppConst;
import com.hande.goochao.session.AppSession;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.HiddenAnimUtils;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.NumberUtils;
import com.hande.goochao.utils.PriceUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.DesignPlanDetailActivity;
import com.hande.goochao.views.activity.MagazineDetailActivity;
import com.hande.goochao.views.activity.NewProductInformationActivity;
import com.hande.goochao.views.activity.NewSpaceDetailActivity;
import com.hande.goochao.views.activity.SaleGiftActivity;
import com.hande.goochao.views.activity.ScenePreviewActivity;
import com.hande.goochao.views.activity.SearchActivity;
import com.hande.goochao.views.base.BaseFragment;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.NoScrollListView;
import com.hande.goochao.views.components.WebViewActivity;
import com.hande.goochao.views.components.ZoomView;
import com.hande.goochao.views.widget.GoochaoListView;
import com.hande.goochao.views.widget.GraduallyAnimation;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tencent.mm.opensdk.modelmsg.SendAuth;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
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

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/7/6.
 */
@ContentView(R.layout.new_home_fragment_main)
public class NewHomeFragment extends BaseFragment implements OnItemClickListener, ScenePreviewActivity.OnPageChangeListener, View.OnClickListener {

    private int kWidth;
    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.new_home_fragment_loading_view)
    private View loadingView;
    @ViewInject(R.id.new_home_refresh_layout)
    private RefreshLayout refreshLayout;
    @ViewInject(R.id.red_bag_icon)
    private ImageView redBagIcon;
    @ViewInject(R.id.click_view)
    private View clickView;

    private TextView txtKongJianDaPei;
    private TextView txtRenQiReMai;
    private TextView txtRuanZhuangFangAn;
    private TextView txtJiZanYouXuan;
    private TextView txtHaoHuoTuiJian;
    private View designerPraiseView;

    private ConvenientBanner convenientBanner;
    private NoScrollListView spaceGoodsListView;
    private ViewPager spaceViewPager;
    private ViewPager planViewPager;
    private ViewPager saleHotViewPager;

    private SpaceGoodsAdapter spaceGoodsAdapter;
    private RecommendAdapter recommendAdapter;
    private SpaceAdapter spaceAdapter;
    private PlanAdapter planAdapter;
    private SaleHotAdapter saleHotAdapter;

    private JSONObject homes = new JSONObject();
    private List<String> bannerImages = new ArrayList<>();
    private JSONArray banners;
    private JSONArray planArray = new JSONArray();
    private JSONArray saleHotArray = new JSONArray();
    private JSONArray spaceArray = new JSONArray();
    private JSONArray sceneGoodsArray = new JSONArray();
    private JSONArray praiseArray = new JSONArray();
    private JSONArray articleArray = new JSONArray();
    private JSONArray recommendArray = new JSONArray();
    private JSONArray scenes = new JSONArray();
    private List<Object> praiseList = new ArrayList<>();

    private boolean firstLoad = true;
    private boolean loaded = false;

    //集赞优选、期刊、软装方案
    private ImageView leftImage;
    private ImageView rightUpImage;
    private ImageView rightDownImage;
    private TextView leftText;
    private TextView rightUpText;
    private TextView rightDownText;
    private ImageView recommendImageView;
    private Button recommendTypeBt;
    private ZoomView recommendArticleZoom;
    private View toSpaceView;
    private View toSceneView;
    private View toSpaceDetailView;
    private View praiseLeft;
    private View praiseRightUP;
    private View praiseRightDown;
    private View greatGoodsHead;
    private TextView planTitle;
    private TextView planDesc;
    private View toPlanView;
    private View planTextView;

    //防止二次加载
    private boolean loading = true;

    @ViewInject(R.id.new_home_list_view)
    private GoochaoListView listView;
    private int pageIndex = 1;
    private int pageSize = 20;
    private int currentIndex = 3;
    private int selectPosition = 0; // 记录人气热卖选中位置;

    private LayoutInflater inflater;

    private View sceneLayout;
    private View saleHotLayout;
    private View planLayout;
    private LinearLayout pointView;
    private View praiseTxtLayout;
    private View praiseLayout;
    private View homeArticle;
    private int[] datas = {R.mipmap.tab_service_focus};

    private GlideRequests glide;

    //是否领取过新人礼包
    private boolean getNewPersonGift;
    //新人有礼地址
    private String giftUrl;
    //分享地址
    private String shareUrl;

    //EVENTBUS是否注册过
    private boolean hadRegister = false;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).setSearchType(SearchActivity.SEARCH_ALL);
        ((ToolBarActivity) getActivity()).showToolBar();
        ((ToolBarActivity) getActivity()).showTitle();
        getActivity().setTitle("构巢Goochao");
        if (!loaded) {
            glide = GlideApp.with(getActivity());
            inflater = LayoutInflater.from(getActivity());
            kWidth = WindowUtils.getDeviceWidth(getActivity());

            initHomePage();

            toSpaceView.setOnClickListener(this);
            toSceneView.setOnClickListener(this);
            toPlanView.setOnClickListener(this);
            toSpaceDetailView.setOnClickListener(this);

            boldMethod(txtKongJianDaPei);
            boldMethod(txtRenQiReMai);
            boldMethod(txtJiZanYouXuan);
            boldMethod(txtHaoHuoTuiJian);
            boldMethod(txtRuanZhuangFangAn);

            loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
                @Override
                public void onReload() {
                    loadFailView.setReloading();
                    loadHome();
                }
            });

            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshLayout) {
                    pageIndex = 1;
                    if (loading) {
                        loading = false;
                        loadRecommends(true);
                    }
                }
            });

            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(RefreshLayout refreshLayout) {
                    if (loading) {
                        loading = false;
                        loadRecommends(false);
                    }
                }
            });

            loadHome();
        }

        if (!hadRegister){
            EventBus.getDefault().register(this);
            hadRegister = true;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((ToolBarActivity) getActivity()).showToolBar();
            ((ToolBarActivity) getActivity()).showTitle();
            getActivity().setTitle("构巢Goochao");
        }
    }

    private void initHomePage() {
        FrameLayout homeHeaderView = (FrameLayout) inflater.inflate(R.layout.new_home_fragment, listView, false);
        initRecommendListView(homeHeaderView);
        // 初始化空间ViewPager
        initSpaceViewPager(homeHeaderView);
        // 初始化软装ViewPager
        initPlanViewPager(homeHeaderView);
        // 初始化热卖ViewPager
        initSaleHotViewPager(homeHeaderView);


        designerPraiseView = homeHeaderView.findViewById(R.id.designer_praise_goods_view);

        //集赞优选、期刊
        praiseLeft = homeHeaderView.findViewById(R.id.linearlayout_left_praise);
        praiseRightUP = homeHeaderView.findViewById(R.id.linearlayout_right_up_praise);
        praiseRightDown = homeHeaderView.findViewById(R.id.linearlayout_right_down_praise);
        leftImage = homeHeaderView.findViewById(R.id.praise_picture_left);
        rightUpImage = homeHeaderView.findViewById(R.id.praise_picture_right_up);
        rightDownImage = homeHeaderView.findViewById(R.id.praise_picture_right_down);
        leftText = homeHeaderView.findViewById(R.id.praise_title_left);
        rightUpText = homeHeaderView.findViewById(R.id.praise_title_right_up);
        rightDownText = homeHeaderView.findViewById(R.id.praise_title_right_down);
        recommendImageView = homeHeaderView.findViewById(R.id.recommend_article);
        recommendTypeBt = homeHeaderView.findViewById(R.id.recommend_type_name);
        recommendArticleZoom = homeHeaderView.findViewById(R.id.recommend_article_zoom_view);
        txtKongJianDaPei = homeHeaderView.findViewById(R.id.kong_jian_da_pei);
        txtRenQiReMai = homeHeaderView.findViewById(R.id.txt_renqiremai);
        txtJiZanYouXuan = homeHeaderView.findViewById(R.id.ji_zan_you_xuan);
        txtHaoHuoTuiJian = homeHeaderView.findViewById(R.id.hao_huo_tui_jian);
        txtRuanZhuangFangAn = homeHeaderView.findViewById(R.id.ruan_zhuang_fang_an);
        convenientBanner = homeHeaderView.findViewById(R.id.home_fragment_banner);
        toSpaceView = homeHeaderView.findViewById(R.id.to_space_layout);
        toSceneView = homeHeaderView.findViewById(R.id.to_scene_view);
        toSpaceDetailView = homeHeaderView.findViewById(R.id.go_to_scene);
        greatGoodsHead = homeHeaderView.findViewById(R.id.great_goods_head);
        //软装方案
        planTitle = homeHeaderView.findViewById(R.id.new_home_plan_title);
        planDesc = homeHeaderView.findViewById(R.id.new_home_plan_desc);
        toPlanView = homeHeaderView.findViewById(R.id.to_plan_view);
        planTextView = homeHeaderView.findViewById(R.id.plan_text_view);

    }

    private void initRecommendListView(View homeHeaderView) {
        listView.addHeaderView(homeHeaderView);
        recommendAdapter = new RecommendAdapter();
        listView.setAdapter(recommendAdapter);
    }

    //空间搭配Viewpager
    private void initSpaceViewPager(View homeHeaderView) {

        spaceGoodsListView = homeHeaderView.findViewById(R.id.fragment_scene_list_view);
        spaceGoodsAdapter = new SpaceGoodsAdapter();
        spaceGoodsListView.setAdapter(spaceGoodsAdapter);

        spaceViewPager = homeHeaderView.findViewById(R.id.new_home_fragment_view_pager);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) spaceViewPager.getLayoutParams();
        lp.width = kWidth - WindowUtils.dpToPixels(getActivity(), 27);
        float scale = 200f / 335f;
        lp.height = (int) ((kWidth - WindowUtils.dpToPixels(getActivity(), 27)) * scale);
        spaceViewPager.setLayoutParams(lp);

        sceneLayout = homeHeaderView.findViewById(R.id.home_scene_layout);
        saleHotLayout = homeHeaderView.findViewById(R.id.sale_hot_layout);
        planLayout = homeHeaderView.findViewById(R.id.plan_layout);
        pointView = homeHeaderView.findViewById(R.id.home_point_layout);
        praiseTxtLayout = homeHeaderView.findViewById(R.id.recommend_view);
        praiseLayout = homeHeaderView.findViewById(R.id.praise_layout);
        homeArticle = homeHeaderView.findViewById(R.id.home_article);

        spaceAdapter = new SpaceAdapter();
        spaceViewPager.setAdapter(spaceAdapter);
        spaceAdapter.notifyDataSetChanged();
        spaceViewPager.setOffscreenPageLimit(3);

        spaceViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                GraduallyAnimation.setHideAnimation(spaceGoodsListView, 2000);
                sceneGoodsArray = JsonUtils.getJsonArray(JsonUtils.getJsonItem(spaceArray, position, null), "data", null);
                spaceGoodsAdapter.notifyDataSetChanged();
                if (sceneGoodsArray == null || sceneGoodsArray.length() == 0) {
                    designerPraiseView.setVisibility(View.GONE);
                    toSpaceDetailView.setVisibility(View.GONE);
                } else {
                    designerPraiseView.setVisibility(View.VISIBLE);
                    toSpaceDetailView.setVisibility(View.VISIBLE);
                }
                GraduallyAnimation.setShowAnimation(spaceGoodsListView, 2000);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    //软装方案Viewpager
    private void initPlanViewPager(View homeHeaderView) {

        planViewPager = homeHeaderView.findViewById(R.id.new_home_fragment_plan_view_pager);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) planViewPager.getLayoutParams();
        lp.width = kWidth - WindowUtils.dpToPixels(getActivity(), 27);
        float scale = 315f / 335f;
        lp.height = (int) ((kWidth - WindowUtils.dpToPixels(getActivity(), 27)) * scale);
        planViewPager.setLayoutParams(lp);

        planAdapter = new PlanAdapter();
        planViewPager.setAdapter(planAdapter);
        planAdapter.notifyDataSetChanged();
        planViewPager.setOffscreenPageLimit(3);

        planViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                GraduallyAnimation.setHideAnimation(planTextView, 2000);
                JSONObject planSelect = JsonUtils.getJsonObject(JsonUtils.getJsonItem(planArray, position, null), "detail", null);
                planTitle.setText(JsonUtils.getString(planSelect,"planName",""));
                String planKind = JsonUtils.getString(planSelect,"houseTypeName","");
                String planStyle = JsonUtils.getString(planSelect,"style.name","");
                planDesc.setText(planKind + " | " + planStyle);

                GraduallyAnimation.setShowAnimation(planTextView, 2000);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    //人气热卖ViewPager
    private void initSaleHotViewPager(View homeHeaderView){

        saleHotViewPager = homeHeaderView.findViewById(R.id.new_home_fragment_sale_hot);

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) saleHotViewPager.getLayoutParams();
        lp.width = kWidth - WindowUtils.dpToPixels(getContext(),40);
        float scale = 140f / 375f;
        lp.height = (int) ( kWidth * scale);
        saleHotViewPager.setLayoutParams(lp);

        saleHotAdapter = new SaleHotAdapter();
        saleHotViewPager.setAdapter(saleHotAdapter);
        saleHotAdapter.notifyDataSetChanged();

        saleHotViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pointView.getChildAt(position).setBackgroundResource(R.drawable.black_point_view);
                pointView.getChildAt(selectPosition).setBackgroundResource(R.drawable.white_point_view);
                selectPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == toSpaceView) {
            EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_to_inspiration);
            EventBus.getDefault().post(notification);
        } else if (v == toSceneView) {
            EventBusNotification notification1 = new EventBusNotification(EventBusNotification.event_bus_to_scene);
            EventBus.getDefault().post(notification1);
        } else if (v == toSpaceDetailView) {
            Intent intent = new Intent(getActivity(), NewSpaceDetailActivity.class);
            String spaceId = JsonUtils.getString(JsonUtils.getJsonItem(spaceArray, spaceViewPager.getCurrentItem(), null), "objectId", "");
            intent.putExtra("chenLie", true);
            intent.putExtra("spaceId", spaceId);
            getActivity().startActivity(intent);
        } else if (v == toPlanView){
            EventBusNotification notification1 = new EventBusNotification(EventBusNotification.event_bus_to_plan);
            EventBus.getDefault().post(notification1);
        }
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

    /**
     * banner初始化
     */
    private void renderBannerData() {
        banners = JsonUtils.getJsonArray(homes, "banners", null);
        bannerImages.clear();

        for (int i = 0; i < banners.length(); i++) {
            String cover = ImageUtils.resize(JsonUtils.getString(JsonUtils.getJsonItem(banners, i, null), "cover", ""), 1024, 546);
            bannerImages.add(cover);
        }

        double width = kWidth;
        double height;
        height = width * 200 / 375;
        int widthInt = (int) width;
        int heightInt = (int) height;
        convenientBanner.setLayoutParams(new LinearLayout.LayoutParams(widthInt, heightInt));

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
                .setOnItemClickListener(this)
                .setManualPageable(true);

    }

    /**
     * banner Item点击事件
     *
     * @param position
     */
    @Override
    public void onItemClick(int position) {
        JSONArray banners = JsonUtils.getJsonArray(homes, "banners", null);
        int type = JsonUtils.getInt(JsonUtils.getJsonItem(banners, position, null), "type", 0);
        String bannerId = JsonUtils.getString(JsonUtils.getJsonItem(banners, position, null), "bannerId", "");
        upReadActivity(bannerId);
        if (type == 1) {
            String detailId = JsonUtils.getString(JsonUtils.getJsonItem(banners, position, null), "detailId", "");
            Intent intent = new Intent(getActivity(), MagazineDetailActivity.class);
            intent.putExtra("subjectArticleId", detailId);
            startActivity(intent);
        } else if (type == 2) {
            String detailId = JsonUtils.getString(JsonUtils.getJsonItem(banners, position, null), "detailId", "");
            Intent intent = new Intent(getActivity(), NewProductInformationActivity.class);
            intent.putExtra("goodsId", detailId);
            startActivity(intent);
        } else if (type == 3) {
            String detailId = JsonUtils.getString(JsonUtils.getJsonItem(banners, position, null), "detailId", "");
            Intent intent = new Intent(getActivity(), NewSpaceDetailActivity.class);
            intent.putExtra("spaceId", detailId);
            startActivity(intent);
        } else if (type == 4) {
            String planId = JsonUtils.getString(JsonUtils.getJsonItem(banners, position, null), "detail.planId", "");
            Intent intent = new Intent(getActivity(), DesignPlanDetailActivity.class);
            intent.putExtra("planId", planId);
            startActivity(intent);
        } else if (type == 5) {
            String url = JsonUtils.getString(JsonUtils.getJsonItem(banners, position, null), "detail.url", "");
            String activityId = JsonUtils.getString(JsonUtils.getJsonItem(banners, position, null), "detail.activityId", "");
            Intent intent = new Intent(getActivity(), SaleGiftActivity.class);
            if (url.indexOf("?") == -1){
                intent.putExtra("url", url + "?activityId=" + activityId + "&bannerId=" + bannerId );
            }else {
                intent.putExtra("url", url + "&activityId=" + activityId + "&bannerId=" + bannerId);
            }
            startActivity(intent);
        } else if (type == 6) {
            String url = JsonUtils.getString(JsonUtils.getJsonItem(banners, position, null), "url", "");
            Intent intent = new Intent(getActivity(), SaleGiftActivity.class);
            intent.putExtra("url", url);
            startActivity(intent);
        } else if (type == 7) {
            String name = "活动详情";
            String detailId = JsonUtils.getString(JsonUtils.getJsonItem(banners, position, null), "detailId", "");
            String url = AppConfig.ARTICLE_CONTENT + detailId;
            Intent intent = new Intent(getActivity(), WebViewActivity.class);
            intent.putExtra("title", name);
            intent.putExtra("url", url);
            startActivity(intent);
        }else {
            AlertManager.showErrorToast(getActivity(),"此活动暂不支持，或者请升级最新版本重试！",false);
        }

    }

    /**
     * 首页数据加载
     */
    private void loadHome() {

        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        }

        HttpRequest.get(AppConfig.NEW_HOME_PAGE, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loaded = true;
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
                loadFailView.setFinishReload();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    homes = JsonUtils.getJsonObject(response, "data", null);
                    spaceArray = JsonUtils.getJsonArray(homes, "spaces", null);
                    planArray = JsonUtils.getJsonArray(homes, "plans", null);
                    saleHotArray = JsonUtils.getJsonArray(homes,"goodsArr",null);
                    sceneGoodsArray = JsonUtils.getJsonArray(JsonUtils.getJsonItem(spaceArray, 0, null), "data", null);
                    if (sceneGoodsArray == null || sceneGoodsArray.length() == 0) {
                        designerPraiseView.setVisibility(View.GONE);
                        toSpaceDetailView.setVisibility(View.GONE);
                    } else {
                        designerPraiseView.setVisibility(View.VISIBLE);
                        toSpaceDetailView.setVisibility(View.VISIBLE);
                    }
                    praiseArray = JsonUtils.getJsonArray(homes, "scenePraiseGoods", null);

                    for (int i = 0; i < praiseArray.length(); i++) {
                        try {
                            praiseList.add(praiseArray.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }

                    articleArray = JsonUtils.getJsonArray(homes, "subjectArticles", null);

                    renderBannerData();
                    renderSpaceData();
                    initPraiseAndArticle();
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

    /**
     * 好货推荐数据加载
     */
    private void loadRecommends(boolean refresh) {

        if (refresh) {
            pageIndex = 1;
        }

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
                        recommendArray = new JSONArray();
                    }
                    JSONArray data = JsonUtils.getJsonArray(response, "data", new JSONArray());
                    for (int i = 0; i < data.length(); i++) {
                        try {
                            recommendArray.put(data.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    if (recommendArray == null && recommendArray.length() == 0) {
                        greatGoodsHead.setVisibility(View.GONE);
                    } else {
                        greatGoodsHead.setVisibility(View.VISIBLE);
                        recommendAdapter.notifyDataSetChanged();
                    }
                    if (data.length() < pageSize) {
                        listView.setFooterViewVisibility(View.VISIBLE);
                        refreshLayout.setNoMoreData(true);
                        refreshLayout.setEnableLoadMore(false);
                    } else {
                        listView.setFooterViewVisibility(View.GONE);
                        refreshLayout.setNoMoreData(false);
                        refreshLayout.setEnableLoadMore(true);
                    }
                    String typeScene = "1";
                    for (int i = 0; i < data.length(); i++) {
                        String type = JsonUtils.getString(JsonUtils.getJsonItem(data, i, null), "type", "");
                        if (typeScene.equals(type)) {
                            try {
                                JsonUtils.getJsonItem(data, i, null).put("currentIndex", currentIndex);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            scenes.put(JsonUtils.getJsonItem(data, i, null));
                            currentIndex++;
                        }
                    }
                    AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, scenes);
                    loadActivityUrl();
                    notifyPreview();
                    pageIndex += 1;

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

    /**
     * 初始化Space、plan、saleHot
     */
    private void renderSpaceData() {

        if (spaceArray != null && spaceArray.length() > 0) {

            sceneLayout.setVisibility(View.VISIBLE);

            // 刷新空间
            spaceAdapter.notifyDataSetChanged();
            // 刷新空间商品
//            spaceGoodsAdapter.notifyDataSetChanged();
        } else {
            sceneLayout.setVisibility(View.GONE);
        }

        if (saleHotArray != null && saleHotArray.length() > 0) {

            for (int i = 0 ; i < saleHotArray.length() ; i++){
                View point = new View(getActivity());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(15, 15);
                //设置间隔
                layoutParams.leftMargin = 25;
                //添加到LinearLayout
                if (i == 0) {
                    point.setBackgroundResource(R.drawable.black_point_view);
                }else {
                    point.setBackgroundResource(R.drawable.white_point_view);
                }
                pointView.addView(point, layoutParams);
            }

            saleHotLayout.setVisibility(View.VISIBLE);

            // 刷新人气热卖
            saleHotAdapter.notifyDataSetChanged();
        } else {
            saleHotLayout.setVisibility(View.GONE);
        }

        if (planArray != null && planArray.length() > 0){
            planLayout.setVisibility(View.VISIBLE);

            JSONObject planSelect = JsonUtils.getJsonObject(JsonUtils.getJsonItem(planArray, 0, null), "detail", null);
            planTitle.setText(JsonUtils.getString(planSelect,"planName",""));
            String planKind = JsonUtils.getString(planSelect,"houseTypeName","");
            String planStyle = JsonUtils.getString(planSelect,"style.name","");
            planDesc.setText(planKind + " | " + planStyle);
            planAdapter.notifyDataSetChanged();
        }else {
            planLayout.setVisibility(View.GONE);
        }
        boldMethod(planTitle);
        boldMethod(planDesc);
    }

    /**
     * 集赞优选初始化(以及推荐期刊)
     */
    private void initPraiseAndArticle() {
        if (praiseArray != null && praiseArray.length() > 0 && praiseList.size() >= 3) {
            praiseLayout.setVisibility(View.VISIBLE);
            praiseTxtLayout.setVisibility(View.VISIBLE);

            leftText.setText(JsonUtils.getString(JsonUtils.getJsonItem(praiseArray, 0, null), "detail.title", ""));
            LinearLayout.LayoutParams paramLeftTxT = (LinearLayout.LayoutParams) leftText.getLayoutParams();
            paramLeftTxT.width = kWidth * 100 / 375;
            ImageUtils.loadImage(glide, JsonUtils.getString(JsonUtils.getJsonItem(praiseArray, 0, null), "cover", ""), leftImage, R.mipmap.loadpicture);


            LinearLayout.LayoutParams paramLeft = (LinearLayout.LayoutParams) leftImage.getLayoutParams();
            paramLeft.height = kWidth * 100 / 375;
            paramLeft.width = kWidth * 100 / 375;
            rightUpText.setText(JsonUtils.getString(JsonUtils.getJsonItem(praiseArray, 1, null), "detail.title", ""));
            LinearLayout.LayoutParams paramRightUpTxT = (LinearLayout.LayoutParams) rightUpText.getLayoutParams();
            paramRightUpTxT.width = kWidth * 80 / 375;
            rightDownText.setText(JsonUtils.getString(JsonUtils.getJsonItem(praiseArray, 2, null), "detail.title", ""));
            LinearLayout.LayoutParams paramRightDownTxT = (LinearLayout.LayoutParams) rightDownText.getLayoutParams();
            paramRightDownTxT.width = kWidth * 80 / 375;

            ImageUtils.loadImage(glide, JsonUtils.getString(JsonUtils.getJsonItem(praiseArray, 1, null), "cover", ""), rightUpImage, R.mipmap.loadpicture);

            LinearLayout.LayoutParams paramRightUp = (LinearLayout.LayoutParams) rightUpImage.getLayoutParams();
            paramRightUp.height = kWidth * 80 / 375;
            paramRightUp.width = kWidth * 80 / 375;
            ImageUtils.loadImage(glide, JsonUtils.getString(JsonUtils.getJsonItem(praiseArray, 2, null), "cover", ""), rightDownImage, R.mipmap.loadpicture);


            LinearLayout.LayoutParams paramRightDown = (LinearLayout.LayoutParams) rightDownImage.getLayoutParams();
            paramRightDown.height = kWidth * 80 / 375;
            paramRightDown.width = kWidth * 80 / 375;

            JSONObject leftData = JsonUtils.getJsonObject(JsonUtils.getJsonItem(praiseArray, 0, null), "data", null);
            JSONObject rightUpData = JsonUtils.getJsonObject(JsonUtils.getJsonItem(praiseArray, 1, null), "data", null);
            JSONObject rightDownData = JsonUtils.getJsonObject(JsonUtils.getJsonItem(praiseArray, 2, null), "data", null);
            JSONArray leftDetail = JsonUtils.getJsonArray(JsonUtils.getJsonItem(praiseArray, 0, null), "data.goodsList", null);
            JSONArray rightUpDetail = JsonUtils.getJsonArray(JsonUtils.getJsonItem(praiseArray, 1, null), "data.goodsList", null);
            JSONArray rightDownDetail = JsonUtils.getJsonArray(JsonUtils.getJsonItem(praiseArray, 2, null), "data.goodsList", null);
            JSONObject leftGoods = new JSONObject();
            JSONObject rightUpGoods = new JSONObject();
            JSONObject rightDownGoods = new JSONObject();
            try {
                leftGoods.put("goodsList", leftDetail);
                rightUpGoods.put("goodsList", rightUpDetail);
                rightDownGoods.put("goodsList", rightDownDetail);
                leftData.put("currentIndex", 0);
                rightUpData.put("currentIndex", 1);
                rightDownData.put("currentIndex", 2);
                leftData.put("detail", leftGoods);
                rightUpData.put("detail", rightUpGoods);
                rightDownData.put("detail", rightDownGoods);
                scenes.put(leftData);
                scenes.put(rightUpData);
                scenes.put(rightDownData);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            praiseLeft.setOnClickListener(new SceneOnClickListener(0, scenes));
            praiseRightUP.setOnClickListener(new SceneOnClickListener(1, scenes));
            praiseRightDown.setOnClickListener(new SceneOnClickListener(2, scenes));

            boldMethod(leftText);
            boldMethod(rightUpText);
            boldMethod(rightDownText);

        } else {
            praiseLayout.setVisibility(View.GONE);
            praiseTxtLayout.setVisibility(View.GONE);
        }

        if (articleArray != null && articleArray.length() > 0) {
            homeArticle.setVisibility(View.VISIBLE);

            ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(JsonUtils.getJsonItem(articleArray, 0, null), "cover", ""), 1024, 568), recommendImageView, R.mipmap.loadpicture);
            LinearLayout.LayoutParams paramRecommend = (LinearLayout.LayoutParams) recommendImageView.getLayoutParams();
            paramRecommend.height = (kWidth - dip2px(40)) * 170 / 335;
            paramRecommend.width = kWidth - dip2px(40);

            recommendTypeBt.setText(JsonUtils.getString(JsonUtils.getJsonItem(articleArray, 0, null),"data.typeName",""));

            recommendArticleZoom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String subjectArticleId = JsonUtils.getString(JsonUtils.getJsonItem(articleArray, 0, null), "objectId", "");
                    System.out.print(articleArray);
                    Intent intent = new Intent(getActivity(), MagazineDetailActivity.class);
                    intent.putExtra("subjectArticleId", subjectArticleId);
                    getActivity().startActivity(intent);
                }
            });
        } else {
            homeArticle.setVisibility(View.GONE);
        }
        if (loading) {
            loading = false;
            loadRecommends(true);
        }
    }

    //场景搭配内含商品适配器
    class SpaceGoodsAdapter extends BaseAdapter {

        class SceneViewHolder {

            TextView sceneName;
            TextView sceneIntroduce;
            TextView scenePrice;
            ImageView imageView;
            TextView discountView;
            TextView saleOutView;
            TextView productOldPrice;

            SceneViewHolder(View convertView) {
                sceneName = convertView.findViewById(R.id.link_product_name);
                sceneIntroduce = convertView.findViewById(R.id.link_product_desc);
                scenePrice = convertView.findViewById(R.id.link_product_price);
                imageView = convertView.findViewById(R.id.link_product_cover);
                discountView = convertView.findViewById(R.id.link_discount_view);
                saleOutView = convertView.findViewById(R.id.link_sale_out_view);
                productOldPrice = convertView.findViewById(R.id.link_old_price);
            }

        }

        @Override
        public int getCount() {
            return sceneGoodsArray == null ? 0 : sceneGoodsArray.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(sceneGoodsArray, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.layout_new_product_scene_item, null);
                convertView.setTag(new SceneViewHolder(convertView));
            }
            SceneViewHolder viewHolder = (SceneViewHolder) convertView.getTag();
            JSONObject item = getItem(i);

            //判断商品类型（满减 折扣 原价）
            String tagValue = JsonUtils.getString(item,"discountTag","");
            if (tagValue.equals("")){
                viewHolder.discountView.setVisibility(View.GONE);
                viewHolder.saleOutView.setVisibility(View.INVISIBLE);
                viewHolder.productOldPrice.setVisibility(View.GONE);

                double price = JsonUtils.getDouble(item , "minPrice" , 0);
                double priceValue = NumberUtils.decimalDouble(price);
                viewHolder.scenePrice.setText("¥" + PriceUtils.beautify(priceValue));
            }                //原价 无活动
            else{
                if (JsonUtils.getInt(item,"discountType",0) == 2){
                    viewHolder.discountView.setVisibility(View.VISIBLE);
                    viewHolder.saleOutView.setVisibility(View.GONE);
                    viewHolder.discountView.setText(tagValue);

                    viewHolder.productOldPrice.setVisibility(View.VISIBLE);

                    double oldPrice = JsonUtils.getDouble(item , "minPrice" , 0);
                    double oldPriceValue = NumberUtils.decimalDouble(oldPrice);
                    viewHolder.productOldPrice.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG); //原价设置中划线
                    viewHolder.productOldPrice.setText("¥" + PriceUtils.beautify(oldPriceValue));

                    double newPrice = JsonUtils.getDouble(item , "discountPrice" , 0);
                    double newPriceValue = NumberUtils.decimalDouble(newPrice);
                    viewHolder.scenePrice.setText("¥" + PriceUtils.beautify(newPriceValue));
                }             //折扣
                else {
                    viewHolder.discountView.setVisibility(View.GONE);
                    viewHolder.saleOutView.setVisibility(View.VISIBLE);
                    viewHolder.productOldPrice.setVisibility(View.GONE);
                    viewHolder.saleOutView.setText(tagValue);
                    double price = JsonUtils.getDouble(item , "minPrice" , 0);
                    double priceValue = NumberUtils.decimalDouble(price);
                    viewHolder.scenePrice.setText("¥" + PriceUtils.beautify(priceValue));
                }             //满减
            }

            boldMethod(viewHolder.sceneName);
            boldMethod(viewHolder.scenePrice);

            final String linkGoodsId = JsonUtils.getString(item, "goodsId", "");

            viewHolder.sceneName.setText(JsonUtils.getString(item, "title", ""));

            viewHolder.sceneIntroduce.setText(JsonUtils.getString(item, "description", "无商品描述"));

            ZoomView zoomView = convertView.findViewById(R.id.link_goods_list_zoom_view);
            zoomView.setTag(i);
            String imageUrl = ImageUtils.resize(JsonUtils.getString(item, "cover", ""), 320, 320);
            ImageUtils.loadImage(glide, imageUrl, viewHolder.imageView, -1);

            zoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), NewProductInformationActivity.class);
                    intent.putExtra("goodsId", linkGoodsId);
                    getActivity().startActivity(intent);
                }
            });

            return convertView;
        }
    }

    //好货推荐适配器
    class RecommendAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return recommendArray == null ? 0 : recommendArray.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(recommendArray, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {
            int type = JsonUtils.getInt(JsonUtils.getJsonItem(recommendArray, i, null), "type", 0);
            if (type == 4) {
                convertView = inflater.inflate(R.layout.new_home_fragment_add_good, viewGroup, false);

                JSONObject item = getItem(i);

                TextView priceTxt = convertView.findViewById(R.id.recommend_item_price);
                TextView titleTxt = convertView.findViewById(R.id.recommend_item_title);
                ImageView image = convertView.findViewById(R.id.recommend_item_cover);
                ZoomView zoomView = convertView.findViewById(R.id.recommend_good_zoom_view);
                TextView discountView = convertView.findViewById(R.id.recommend_item_discount_view);
                TextView saleOutView = convertView.findViewById(R.id.recommend_item_sale_out_view);
                TextView productOldPrice = convertView.findViewById(R.id.recommend_item_old_price);

                //判断商品类型（满减 折扣 原价）
                String tagValue = JsonUtils.getString(getItem(i),"object.discountTag","");
                if (tagValue.equals("")){
                    discountView.setVisibility(View.GONE);
                    saleOutView.setVisibility(View.INVISIBLE);
                    productOldPrice.setVisibility(View.GONE);

                    double price = JsonUtils.getDouble(item , "object.minPrice" , 0);
                    double priceValue = NumberUtils.decimalDouble(price);
                    priceTxt.setText("¥" + PriceUtils.beautify(priceValue));
                }                //原价 无活动
                else{
                    if (JsonUtils.getInt(getItem(i),"object.discountType",0) == 2){
                        discountView.setVisibility(View.VISIBLE);
                        saleOutView.setVisibility(View.GONE);
                        discountView.setText(tagValue);

                        productOldPrice.setVisibility(View.VISIBLE);

                        double oldPrice = JsonUtils.getDouble(item , "object.minPrice" , 0);
                        double oldPriceValue = NumberUtils.decimalDouble(oldPrice);
                        productOldPrice.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG); //原价设置中划线
                        productOldPrice.setText("¥" + PriceUtils.beautify(oldPriceValue));

                        double newPrice = JsonUtils.getDouble(item , "object.discountPrice" , 0);
                        double newPriceValue = NumberUtils.decimalDouble(newPrice);
                        priceTxt.setText("¥" + PriceUtils.beautify(newPriceValue));
                    }             //折扣
                    else {
                        discountView.setVisibility(View.GONE);
                        saleOutView.setVisibility(View.VISIBLE);
                        productOldPrice.setVisibility(View.GONE);
                        saleOutView.setText(tagValue);
                        double price = JsonUtils.getDouble(item , "object.minPrice" , 0);
                        double priceValue = NumberUtils.decimalDouble(price);
                        priceTxt.setText("¥" + PriceUtils.beautify(priceValue));
                    }             //满减
                }

                titleTxt.setText(JsonUtils.getString(getItem(i), "object.title", ""));
                ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(getItem(i), "cover", ""), 640, 640), image, R.mipmap.loadpicture);
                double value = kWidth;
                LinearLayout.LayoutParams paramRecommend = (LinearLayout.LayoutParams) image.getLayoutParams();
                paramRecommend.height = (int) value * 335 / 375;
                paramRecommend.width = (int) value * 335 / 375;
                Button buyBt = convertView.findViewById(R.id.recommend_item_button);
                buyBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String goodsId = JsonUtils.getString(JsonUtils.getJsonItem(recommendArray, i, null), "object.goodsId", "");
                        Intent intent = new Intent(getActivity(), NewProductInformationActivity.class);
                        intent.putExtra("goodsId", goodsId);
                        startActivity(intent);
                    }
                });

                zoomView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String goodsId = JsonUtils.getString(JsonUtils.getJsonItem(recommendArray, i, null), "object.goodsId", "");
                        Intent intent = new Intent(getActivity(), NewProductInformationActivity.class);
                        intent.putExtra("goodsId", goodsId);
                        startActivity(intent);
                    }
                });
            } else if (type == 3) {
                convertView = inflater.inflate(R.layout.new_home_fragment_add_article, viewGroup, false);
                ImageView articleView = convertView.findViewById(R.id.article_cover);
                Button typeBt = convertView.findViewById(R.id.type_bt);
                ZoomView zoomView = convertView.findViewById(R.id.recommend_article_zoom_view);
                ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(getItem(i), "object.cover", ""), 1024, 568), articleView, R.mipmap.loadpicture);
                double value = kWidth;
                LinearLayout.LayoutParams paramRecommend = (LinearLayout.LayoutParams) articleView.getLayoutParams();
                paramRecommend.height = (int) ((value - dip2px(40)) * 170 / 335);
                paramRecommend.width = (int) (value - dip2px(40));

                typeBt.setText(JsonUtils.getString(getItem(i),"object.typeName",""));

                zoomView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String subjectArticleId = JsonUtils.getString(getItem(i), "object.subjectArticleId", "");
                        Intent intent = new Intent(getActivity(), MagazineDetailActivity.class);
                        intent.putExtra("subjectArticleId", subjectArticleId);
                        getActivity().startActivity(intent);
                    }
                });
            } else if (type == 1) {
                convertView = inflater.inflate(R.layout.new_home_fragment_add_scene, viewGroup, false);
                ImageView sceneImage = convertView.findViewById(R.id.recommend_scene_cover);
                ZoomView zoomView = convertView.findViewById(R.id.recommend_scene_zoom_view);
                ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(getItem(i), "detail.cover", ""), 640, 640), sceneImage, R.mipmap.loadpicture);

                double value = kWidth;
                FrameLayout.LayoutParams paramRecommend = (FrameLayout.LayoutParams) sceneImage.getLayoutParams();
                paramRecommend.height = (int) value * 325 / 375;
                paramRecommend.width = (int) value * 325 / 375;

                int index;
                if (praiseArray != null && praiseArray.length() > 0) {
                    index = JsonUtils.getInt(getItem(i), "currentIndex", 0);
                } else {
                    index = JsonUtils.getInt(getItem(i), "currentIndex", 0) - 3;
                }
                zoomView.setOnClickListener(new SceneOnClickListener(index, scenes));
            }
            return convertView;
        }
    }

    //空间推荐适配器
    class SpaceAdapter extends PagerAdapter {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        // 这个方法用来实例化页卡
        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View convertView = inflater.inflate(R.layout.home_space_item, null);
            ImageView imageView = convertView.findViewById(R.id.shadow_image_view);
            TextView textView = convertView.findViewById(R.id.tag_number);
            int tag = JsonUtils.getInt(JsonUtils.getJsonItem(spaceArray, position, null), "detail.goodsCount", 0);
            String cover = JsonUtils.getString(JsonUtils.getJsonItem(spaceArray, position, null), "cover", "");
            textView.setText(tag + "");
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(imageView.getLayoutParams());
            params.width = (kWidth - dip2px(35));
            float scale = 200f / 335f;

            params.height = (int) ((kWidth - dip2px(35)) * scale);
            imageView.setLayoutParams(params);

            ImageUtils.loadImage(glide, ImageUtils.resize(cover, 1024, 611), imageView, R.mipmap.loadpicture);


            container.addView(convertView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String spaceId = JsonUtils.getString(JsonUtils.getJsonItem(spaceArray, position, null), "objectId", "");
                    Intent intent = new Intent(getActivity(), NewSpaceDetailActivity.class);
                    intent.putExtra("spaceId", spaceId);
                    startActivity(intent);
                }
            });

            return convertView;

        }

        // 返回页卡的数量
        @Override
        public int getCount() {
            return spaceArray.length();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }

    //软装方案适配器
    class PlanAdapter extends PagerAdapter {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        // 这个方法用来实例化页卡
        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View convertView = inflater.inflate(R.layout.home_plan_viewpager_item, null);
            ImageView imageView = convertView.findViewById(R.id.plan_cover);
            String cover = JsonUtils.getString(JsonUtils.getJsonItem(planArray, position, null), "cover", "");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageView.getLayoutParams());
            params.width = (kWidth - dip2px(35));
            float scale = 315f / 335f;

            params.height = (int) ((kWidth - dip2px(27)) * scale);
            params.gravity = Gravity.CENTER;
            imageView.setLayoutParams(params);

            ImageUtils.loadImage(glide, ImageUtils.resize(cover, 1024, 963), imageView, R.mipmap.loadpicture);
            container.addView(convertView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String planId = JsonUtils.getString(JsonUtils.getJsonItem(planArray, position, null), "detail.planId", "");
                    Intent intent = new Intent(getActivity(), DesignPlanDetailActivity.class);
                    intent.putExtra("planId", planId);
                    startActivity(intent);
                }
            });

            return convertView;

        }

        // 返回页卡的数量
        @Override
        public int getCount() {
            return planArray.length();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }

    //人气热卖适配器
    class SaleHotAdapter extends PagerAdapter {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        // 这个方法用来实例化页卡
        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View convertView = inflater.inflate(R.layout.home_sale_hot_viewpager_item, null);
            ImageView imageView = convertView.findViewById(R.id.hot_goods_cover);
            String cover = JsonUtils.getString(JsonUtils.getJsonItem(saleHotArray, position, null), "cover", "");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageView.getLayoutParams());
            float scale = 140f / 375f;
            params.height = (int) ( kWidth * scale);
            params.width = (int) ( kWidth * scale);
            imageView.setLayoutParams(params);
            ImageUtils.loadImage(glide, ImageUtils.resize(cover, 1024, 963), imageView, R.mipmap.loadpicture);

            JSONObject goodsObject = JsonUtils.getJsonObject(JsonUtils.getJsonItem(saleHotArray,position,null),"detail",null);

            TextView goodsTitle = convertView.findViewById(R.id.hot_goods_title);
            String goodsTitleStr = JsonUtils.getString(goodsObject,"title","");
            goodsTitle.setText(goodsTitleStr);

            TextView goodsDesc = convertView.findViewById(R.id.hot_goods_desc);
            String goodsDescStr = JsonUtils.getString(goodsObject,"description","");
            goodsDesc.setText(goodsDescStr);

            TextView goodsPrice = convertView.findViewById(R.id.hot_goods_price);
            TextView goodsOldPrice = convertView.findViewById(R.id.hot_goods_old_price);

            final String goodsId = JsonUtils.getString(goodsObject,"goodsId","");

            //判断商品类型（满减 折扣 原价）
            String tagValue = JsonUtils.getString(goodsObject, "discountTag", "");
            if (tagValue.equals("")) {
                goodsOldPrice.setVisibility(View.GONE);

                double price = JsonUtils.getDouble(goodsObject, "minPrice", 0);
                double priceValue = NumberUtils.decimalDouble(price);
                goodsPrice.setText("¥" + PriceUtils.beautify(priceValue));
            }                //原价 无活动
            else {
                if (JsonUtils.getInt(goodsObject, "discountType", 0) == 2) {

                    goodsOldPrice.setVisibility(View.VISIBLE);

                    double oldPrice = JsonUtils.getDouble(goodsObject, "minPrice", 0);
                    double oldPriceValue = NumberUtils.decimalDouble(oldPrice);
                    goodsOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //原价设置中划线
                    goodsOldPrice.setText("¥" + PriceUtils.beautify(oldPriceValue));

                    double newPrice = JsonUtils.getDouble(goodsObject, "discountPrice", 0);
                    double newPriceValue = NumberUtils.decimalDouble(newPrice);
                    goodsPrice.setText("¥" + PriceUtils.beautify(newPriceValue));
                }             //折扣
                else {
                    goodsOldPrice.setVisibility(View.GONE);

                    double price = JsonUtils.getDouble(goodsObject, "minPrice", 0);
                    double priceValue = NumberUtils.decimalDouble(price);
                    goodsPrice.setText("¥" + PriceUtils.beautify(priceValue));
                }             //满减
            }

            WindowUtils.boldMethod(goodsTitle);
            WindowUtils.boldMethod(goodsDesc);
            WindowUtils.boldMethod(goodsPrice);
            WindowUtils.boldMethod(goodsOldPrice);

            ZoomView zoomView = convertView.findViewById(R.id.sale_hot_zoom_view);
            zoomView.setTag(position);

            zoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), NewProductInformationActivity.class);
                    intent.putExtra("goodsId", goodsId);
                    getActivity().startActivity(intent);
                }
            });

            container.addView(convertView);

            return convertView;

        }

        // 返回页卡的数量
        @Override
        public int getCount() {
            return saleHotArray.length();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }

    class SceneOnClickListener implements View.OnClickListener {
        private int index;
        private JSONArray scenes;

        SceneOnClickListener(int index, JSONArray scenes) {
            this.index = index;
            this.scenes = scenes;
        }

        @Override
        public void onClick(View view) {
            ScenePreviewActivity.setOnPageChangeListener(NewHomeFragment.this);
            AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, scenes);
            Intent intent = new Intent(getActivity(), ScenePreviewActivity.class);
            intent.putExtra("currentIndex", index);
            startActivity(intent);
        }
    }

    public void onPageChange(int currentIndex, int size) {

        if (praiseArray != null && praiseArray.length() > 0) {
            if (size - currentIndex <= 3) {
                if (loading) {
                    loading = false;
                    loadRecommends(false);
                }
            }
        } else {
            if (size - currentIndex <= 0) {
                if (loading) {
                    loading = false;
                    loadRecommends(false);
                }
            }
        }
    }

    private void notifyPreview() {
        ScenePreviewActivity.refreshData();
    }

    private void closeRefresh() {
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
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
            AlertManager.showErrorInfo(getActivity());
        }
    }

    private int dip2px(int dpValue) {
        return WindowUtils.dpToPixels(getActivity(), dpValue);
    }

    private void boldMethod(TextView textView) {
        TextPaint paint = textView.getPaint();
        paint.setFakeBoldText(true);
    }

    /**
     * 活动浏览+1 (head请求，不执行任何操作)
     */
    private void upReadActivity(String bannerId){
        HttpRequest.head(AppConfig.ACTIVITY_READ + bannerId , null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
            }

            @Override
            public void onSuccess(JSONObject response) {
            }

            @Override
            public void onError(Throwable ex) {
            }
        });
    }

    /**
     * 首页获取活动相关地址
     */
    private void loadActivityUrl() {

        HttpRequest.get(AppConfig.GET_ACTIVITY, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
                loadFailView.setFinishReload();
                levelMove();
                loading = true;
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONObject data = JsonUtils.getJsonObject(response, "data", null);
                    getNewPersonGift = JsonUtils.getBoolean(data,"newMemberPackageReceive",false);
                    giftUrl = JsonUtils.getString(data,"newMemberPackageUrl","");
                    shareUrl = JsonUtils.getString(data,"shareUrl" ,"");

                    clickView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getContext(), SaleGiftActivity.class);
                            intent.putExtra("url", giftUrl);
                            startActivity(intent);
                        }
                    });
                    setRedBag();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(getContext(), "服务器繁忙，请稍后重试", false);
            }
        });
    }

    private void setRedBag(){
        if (getNewPersonGift){
            redBagIcon.setVisibility(View.GONE);
            clickView.setEnabled(false);
        }else {
            redBagIcon.setVisibility(View.VISIBLE);
            clickView.setEnabled(true);
        }

        WindowUtils.setMargins(clickView,(kWidth * 290 / 375),0,0,WindowUtils.dpToPixels(getContext(),80));
    }

    //水平移动，红包出场动画
    private void levelMove(){
        if (getNewPersonGift){
            redBagIcon.setVisibility(View.GONE);
            clickView.setEnabled(false);
        }else {
            redBagIcon.setVisibility(View.VISIBLE);
            clickView.setEnabled(true);
            final int moveWidth = kWidth * 270 / 375; //第一次结束位置
            final int moveWidthSecond = kWidth * 300 / 375; // 第二次结束位置
            final int moveWidthEnd = kWidth * 290 / 375; // 结束位置
            HiddenAnimUtils.slideview(WindowUtils.dpToPixels(getContext(),kWidth + 100),  moveWidth, redBagIcon);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    HiddenAnimUtils.slideview(moveWidth,  moveWidthSecond, redBagIcon);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            HiddenAnimUtils.slideview(moveWidthSecond,  moveWidthEnd, redBagIcon);
                        }
                    }, 300);
                }
            }, 300);
        }
    }

    //页面再次展现时候,水平移动，红包出场动画
    private void levelMoveRe(){
        if (!loaded){
            return;
        }
        if(!getNewPersonGift){
            redBagIcon.setVisibility(View.VISIBLE);
            clickView.setEnabled(true);
            final int moveWidth = kWidth * 290 / 375; // 结束位置
            HiddenAnimUtils.slideview(WindowUtils.dpToPixels(getContext(),kWidth + 100),  moveWidth, redBagIcon);
        }else {
            redBagIcon.clearAnimation();
            redBagIcon.setVisibility(View.GONE);
            clickView.setEnabled(false);
        }
    }

    @Subscribe
    public void onEvent(EventBusNotification notification) {
        if (notification.getKey().equals(EventBusNotification.event_bus_login_success)) {
            loadActivityUrl();
        }else if (notification.getKey().equals(EventBusNotification.event_bus_hide_gift_icon)){
            getNewPersonGift = true;
        }else if (notification.getKey().equals(EventBusNotification.event_bus_logout)){
            getNewPersonGift = false;
            redBagIcon.setVisibility(View.VISIBLE);
            clickView.setEnabled(true);
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        hadRegister = false;
        super.onDestroy();
    }

    @Override
    public void onResume(){
        super.onResume();
        levelMoveRe();
    }
}
