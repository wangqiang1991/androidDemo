package com.hande.goochao.views.activity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
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
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;
import com.hande.goochao.views.components.ZoomView;
import com.hande.goochao.views.widget.NewGridView;
import com.hande.goochao.views.widget.RoundImageView;
import com.hande.goochao.views.widget.tablayout.RadiusFiveImageView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lmc on 2018/2/24.
 */

public class DesignerActivity extends ToolBarActivity implements ScenePreviewActivity.OnPageChangeListener, View.OnClickListener {

    private ImageOptions options = ImageOptionsUtil.getImageOptionsCenter(R.mipmap.loadpicture);

    @ViewInject(R.id.designer_loading_view)
    private LoadingView loadingView;
    @ViewInject(R.id.load_fail_view_designerActivity_gird)
    private LoadFailView loadFailView2;
    @ViewInject(R.id.all_design_grid)
    private GridView allDesignGridView;
    @ViewInject(R.id.recommend_picture_grid)
    private NewGridView recommendPictureGridView;
    @ViewInject(R.id.show_end_view)
    private ImageView showEndView;

    private JSONArray designList = new JSONArray();
    private JSONArray designResource;
    private JSONArray recommendList = new JSONArray();
    private JSONArray recommendResource;
    private JSONObject designer;

    private boolean firstLoad = true;

    private RecommendAdapter recommendAdapter;
    private DesignAdapter designAdapter;

    private int countRecommend = 21;
    private String designerId;
    private String sceneId;
    private boolean loadDetail = true;
    private int pageSize = 9;
    private int pageIndex = 1;

    @ViewInject(R.id.designer_name)
    private TextView designerName;
    @ViewInject(R.id.designer_description)
    private TextView designerDescription;
    @ViewInject(R.id.designer_head)
    private ImageView designerHead;
    @ViewInject(R.id.tag_layout)
    private TagFlowLayout tag;
    @ViewInject(R.id.rating_bar_scene)
    private RatingBar ratingBar;

    @ViewInject(R.id.jianjie)
    private TextView jianjie;
    @ViewInject(R.id.shanchang)
    private TextView shanchang;
    @ViewInject(R.id.zuoping)
    private TextView zuoping;
    @ViewInject(R.id.cainixihuan)
    private TextView cainixihuan;

    private GlideRequests glide;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_designer);
        setTitle("设计师");
        glide = GlideApp.with(this);

        sceneId = getIntent().getStringExtra("sceneId");
        designerId = getIntent().getStringExtra("designerId");

        if (StringUtils.isEmpty(sceneId) || StringUtils.isEmpty(designerId)) {
            AlertManager.showErrorToast(this, "参数无效", false);
            return;
        }

        designerHead.setFocusableInTouchMode(true);
        designerHead.setFocusable(true);
        designerHead.requestFocus();

        designAdapter = new DesignAdapter();
        recommendAdapter = new RecommendAdapter();
        allDesignGridView.setAdapter(designAdapter);
        recommendPictureGridView.setAdapter(recommendAdapter);

        loadFailView2.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadRecommend();
            }
        });

        boldText();
        loadRecommend();
    }

    private void boldText() {
        WindowUtils.boldMethod(zuoping);
        WindowUtils.boldMethod(cainixihuan);
        WindowUtils.boldMethod(jianjie);
        WindowUtils.boldMethod(shanchang);
        WindowUtils.boldMethod(designerName);
    }

    private void loadRecommend() {
        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        }
        Map<String, String> params = new HashMap<>();
        params.put("count", "" + countRecommend);
        params.put("loadDetail", "" + loadDetail);
        params.put("sceneId", "" + sceneId);

        HttpRequest.get(AppConfig.LIKE_GET, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    recommendResource = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < recommendResource.length(); i++) {
                        try {
                            recommendList.put(recommendResource.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    loadDesign();
                    showEndView.setVisibility(View.VISIBLE);
                    recommendAdapter.notifyDataSetChanged();
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(DesignerActivity.this, "服务器繁忙，请稍后重试", false);
                AppLog.e("err", ex);
                showError();
            }
        });
    }

    private void loadDesign() {
        Map<String, String> params = new HashMap<>();
        params.put("pageSize", "" + pageSize);
        params.put("pageIndex", "" + pageIndex);
        params.put("loadDetail", "" + loadDetail);
        params.put("designerId", "" + designerId);
        HttpRequest.get(AppConfig.DESIGN_GET, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    designResource = JsonUtils.getJsonArray(response, "data", null);
                    for (int i = 0; i < designResource.length(); i++) {
                        try {
                            designList.put(designResource.get(i));
                        } catch (JSONException e) {
                            AppLog.e("err", e);
                        }
                    }
                    loadDesigner();
                    designAdapter.notifyDataSetChanged();
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

    private void loadDesigner() {
        Map<String, String> params = new HashMap<>();
        params.put("designId", "" + designerId);
        HttpRequest.get(AppConfig.DESIGNER_GET + designerId, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    designer = JsonUtils.getJsonObject(response, "data", null);
                    designerName.setText(JsonUtils.getString(designer, "nickName", ""));
                    designerDescription.setText(JsonUtils.getString(designer, "description", ""));

                    String head = JsonUtils.getString(designer, "head", "");
                    if (!head.equals("")){
                        designerHead.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        x.image().bind(designerHead, ImageUtils.resize(head, 320, 320), options);
                    }

                    float ratingNum = (float) JsonUtils.getInt(designer, "level", 0);
                    ratingBar.setStepSize(1f);
                    ratingBar.setRating(ratingNum);

                    String tagStr = JsonUtils.getString(designer, "tag", "");
                    List<String> tags = Arrays.asList(tagStr.split(","));
                    tag.setAdapter(new TagAdapter<String>(tags) {
                        @Override
                        public View getView(FlowLayout parent, int position, String s) {
                            s = getItem(position);
                            View view = LayoutInflater.from(DesignerActivity.this).inflate(R.layout.tag_view_item, tag, false);
                            TextView tv = view.findViewById(R.id.tag_text);
                            tv.setText(s);
                            return view;
                        }
                    });

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

    @Override
    public void onPageChange(int currentIndex, int size) {

    }

    @Override
    public void onClick(View v) {

    }

    class DesignAdapter extends BaseAdapter {

        class DesignViewHolder {
            RadiusFiveImageView imageView;
            ZoomView zoomView;

            DesignViewHolder(View view) {
                imageView = view.findViewById(R.id.design_item);
                zoomView = view.findViewById(R.id.zoom_view);
            }
        }

        @Override
        public int getCount() {
            return designList == null ? 0 : designList.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(designList, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(DesignerActivity.this).inflate(R.layout.gridview_design_item, viewGroup, false);
                view.setTag(new DesignViewHolder(view));
            }
            DesignViewHolder viewHolder = (DesignViewHolder) view.getTag();

            viewHolder.zoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ZoomView zoomView = ((ZoomView) v);
                    ScenePreviewActivity.setOnPageChangeListener(DesignerActivity.this);
                    AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, designList);
                    Intent intent = new Intent(DesignerActivity.this, ScenePreviewActivity.class);
                    intent.putExtra("currentIndex", ((Integer) zoomView.getTag()));
                    startActivity(intent);
                    DesignerActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });

            viewHolder.zoomView.setTag(i);
            ImageUtils.loadImage(glide, ImageUtils.resize(JsonUtils.getString(getItem(i), "cover", "--"), 500, 500), viewHolder.imageView, R.mipmap.loadpicture);

            View clickCover = view.findViewById(R.id.click_cover);
            if ((i + 1) == designList.length()) {
                clickCover.setVisibility(View.VISIBLE);
            } else {
                clickCover.setVisibility(View.GONE);
            }

            TextView seeAll = view.findViewById(R.id.see_all_txt);
            WindowUtils.boldMethod(seeAll);

            clickCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), AllDesignActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("designerId", designerId);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
            return view;
        }
    }

    class RecommendAdapter extends BaseAdapter {

        class RecommendViewHolder {

            RadiusFiveImageView imageView;
            ZoomView zoomView;

            RecommendViewHolder(View view) {
                imageView = view.findViewById(R.id.recommend_item);
                zoomView = view.findViewById(R.id.zoom_view);
            }
        }

        @Override
        public int getCount() {
            return recommendList == null ? 0 : recommendList.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(recommendList, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(DesignerActivity.this).inflate(R.layout.gridview_recommend_item, viewGroup, false);
                view.setTag(new RecommendViewHolder(view));
            }

            RecommendViewHolder viewHolder = (RecommendViewHolder) view.getTag();

            viewHolder.zoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ZoomView zoomView = ((ZoomView) v);
                    ScenePreviewActivity.setOnPageChangeListener(DesignerActivity.this);
                    AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, recommendList);
                    Intent intent = new Intent(DesignerActivity.this, ScenePreviewActivity.class);
                    intent.putExtra("currentIndex", ((Integer) zoomView.getTag()));
                    startActivity(intent);
                    DesignerActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });

            viewHolder.zoomView.setTag(i);
            ImageUtils.loadImage(glide, JsonUtils.getString(getItem(i), "cover", "--"), viewHolder.imageView, R.mipmap.loadpicture);

            return view;
        }
    }

    private void resetView() {
        loadFailView2.setVisibility(View.GONE);
    }

    private void showError() {
        if (firstLoad) {
            loadingView.setVisibility(View.GONE);
            loadFailView2.setVisibility(View.VISIBLE);
        } else {
            loadingView.setVisibility(View.GONE);
            AlertManager.showErrorInfo(DesignerActivity.this);
        }
    }
}
