package com.hande.goochao.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.JSONObjectCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.commons.views.gallery.GalleryActivity;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.LoadingView;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * 查看我的评价
 */
public class MyCommentsActivity extends ToolBarActivity implements LoadFailView.OnReloadListener, AdapterView.OnItemClickListener {

    @ViewInject(R.id.my_comments_loading)
    private LoadingView loadingView;
    @ViewInject(R.id.rating_bar)
    private RatingBar ratingBar;
    @ViewInject(R.id.comment_text)
    private TextView commentText;
    @ViewInject(R.id.grid_view)
    private GridView gridView;
    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.comment_layout)
    private View commentLayout;
    @ViewInject(R.id.image_layout)
    private View imageLayout;

    private String relationId;
    private JSONObject detail;
    private String[] images;

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_comments);
        setTitle("我的评价");
        glide = GlideApp.with(this);
        relationId = getIntent().getStringExtra("relationId");
        if (StringUtils.isEmpty(relationId)) {
            AlertManager.showErrorToast(this, "参数无效", false);
            return;
        }

        loadFailView.setOnReloadListener(this);
        gridView.setOnItemClickListener(this);
        loadDetail();

    }

    private void loadDetail() {
        loadingView.setVisibility(View.VISIBLE);

        HttpRequest.get(RestfulUrl.build(AppConfig.ORDER_COMMENTS_DETAIL, ":orderGoodsId", relationId), null, null, new JSONObjectCallback() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    detail = JsonUtils.getJsonObject(response, "data", null);
                    if (detail == null) {
                        loadFailView.setVisibility(View.VISIBLE);
                    } else {
                        bindSource();
                    }
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Throwable ex) {
                loadFailView.setVisibility(View.VISIBLE);
                AppLog.i("查询评论详情失败", ex);
                AlertManager.showErrorToast(MyCommentsActivity.this, "服务器繁忙，请稍后重试", false);
            }
        });
    }

    private void bindSource() {
        commentLayout.setVisibility(View.VISIBLE);
        commentText.setVisibility(View.VISIBLE);
        imageLayout.setVisibility(View.VISIBLE);

        ratingBar.setRating(JsonUtils.getInt(detail, "star", 5));
        commentText.setText(JsonUtils.getString(detail, "content", "无"));
        String imagesStr = JsonUtils.getString(detail, "images", "");
        images = StringUtils.split(imagesStr, ",");
        gridView.setAdapter(new ImageAdapter());
        if (images.length == 0) {
            imageLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onReload() {
        loadDetail();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, GalleryActivity.class);
        intent.putExtra("isLocal",false );
        intent.putExtra("currentSrc", images[position]);
        intent.putExtra("images", images);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    class ImageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return images == null ? 0 : images.length;
        }

        @Override
        public String getItem(int position) {
            return images[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.comment_image_item, parent, false);
                int size = (WindowUtils.getDeviceWidth(MyCommentsActivity.this) - WindowUtils.dpToPixels(MyCommentsActivity.this, 39)) / 4;
                GridView.LayoutParams params = new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, size);
                convertView.setLayoutParams(params);
            }

            ImageView imageView = convertView.findViewById(R.id.imageView);
            ImageUtils.loadImage(glide, ImageUtils.resize(getItem(position),500,500), imageView, R.mipmap.loadpicture);
            return convertView;
        }
    }
}
