package com.hande.goochao.views.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.DateUtils;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.ToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CircleImageView;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.components.LoadFailView;
import com.hande.goochao.views.components.refresh.NoDataTwoLineView;
import com.hande.goochao.views.widget.GoochaoListView;
import com.hande.goochao.views.widget.SharePopupWindow;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.HashMap;
import java.util.Map;

public class MagazineDetailActivity extends ToolBarActivity implements View.OnClickListener {

    private boolean firstLoad = true;
    private Boolean collect = false;
    private int pageIndex = 1;
    private int pageSize = 5;
    private String subjectArticleId;
    private String coverValue;
    private String titleValue;
    private String createValue;
    String articleId;
    WebView webView;

    private JSONArray commentData = new JSONArray();
    private int commentAmount;

    @ViewInject(R.id.loading_article_page)
    private ImageView loadingView;
    @ViewInject(R.id.load_fail_view)
    private LoadFailView loadFailView;
    @ViewInject(R.id.magazine_detail_list_view)
    private GoochaoListView magazineListView;
    @ViewInject(R.id.publish_comment)
    private View publishComment;

    @ViewInject(R.id.middle_view)
    private View middleView;
    @ViewInject(R.id.publish_button)
    private EditText publishView;
    @ViewInject(R.id.push_button)
    private Button pushButton;

    //head控件
    private ImageView magazineCover;
    private TextView magazineTitle;
    private TextView magazineCreateDate;
    private NoDataTwoLineView noDataView;

    private View headerView;
    private CustomLoadingDialog loadingDialog;

    private int headHeight;
    private BaseAdapter adapter;

    private TextView collectNumber;
    private JSONObject detail;

    private long outTime = 20*1000;
    private boolean loadComplete;

    private GlideRequests glide;

    //右上
    private View rightView;
    private LayoutInflater inflater;
    private ImageView collectionBtn;
    private ImageView hadCollectionBtn;
    private ImageView shareBtn;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magazine_detail);
        setTitle("期刊详情");
        glide = GlideApp.with(this);
        Bundle extras = getIntent().getExtras();
        subjectArticleId = extras.getString("subjectArticleId");

        inflater = LayoutInflater.from(this);
        rightView = inflater.inflate(R.layout.layout_new_product_right_view, null);
        rightView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        showRightCustomView(rightView);
        collectionBtn = rightView.findViewById(R.id.new_product_collect_btn);
        hadCollectionBtn = rightView.findViewById(R.id.new_product_collect_had_btn);
        shareBtn = rightView.findViewById(R.id.new_product_share_btn);
        shareBtn.setOnClickListener(MagazineDetailActivity.this);
        collectionBtn.setOnClickListener(MagazineDetailActivity.this);
        hadCollectionBtn.setOnClickListener(MagazineDetailActivity.this);

        headerView = getLayoutInflater().inflate(R.layout.view_magazine_detail_header, null);
        magazineListView.addHeaderView(headerView);
        headHeight = headerView.getHeight();
        View footerView = getLayoutInflater().inflate(R.layout.view_magazine_detail_footer, null);
        magazineListView.addFooterView(footerView);

        collectNumber = headerView.findViewById(R.id.comments_number);
        magazineCover = headerView.findViewById(R.id.magazine_cover);
        magazineTitle = headerView.findViewById(R.id.magazine_title);
        noDataView = headerView.findViewById(R.id.magazine_noDataView);
        magazineCreateDate = headerView.findViewById(R.id.magazine_create_date);

        loadingDialog = new CustomLoadingDialog(this);
        adapter = new MagazineListAdapter();
        magazineListView.setAdapter(adapter);
//        magazineListView.setOnScrollListener(new ScrollListener());

        noDataView.setImageAndText(R.mipmap.magazine_nodata, "还没有评论","快来互动吧");

        loadFailView.setOnReloadListener(new LoadFailView.OnReloadListener() {
            @Override
            public void onReload() {
                loadFailView.setVisibility(View.GONE);
                onReloadView();
            }
        });

        loadMagazineDetail();
    }

    private void loadMagazineDetail() {
        if (firstLoad) {
            loadingView.setVisibility(View.VISIBLE);
        }
        String url = RestfulUrl.build(AppConfig.MAGAZINE_DETAIL, ":subjectArticleId", subjectArticleId);

        HttpRequest.get(url, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    detail = JsonUtils.getJsonObject(response, "data", null);
                    articleId = JsonUtils.getString(JsonUtils.getJsonObject(detail, "article", null), "articleId", "");
                    collect = JsonUtils.getBoolean(detail, "collection", false);
                    commentAmount = JsonUtils.getInt(detail, "commentAmount", 0);
                    coverValue = JsonUtils.getString(detail, "cover", "");
                    titleValue = JsonUtils.getString(detail, "title", "");
                    createValue = JsonUtils.getString(detail, "createdAt", "");
                    collectNumber.setText(("" + commentAmount) + "条评论");
                    WindowUtils.boldMethod(collectNumber);
                    if (collect) {
                        collectionBtn.setVisibility(View.GONE);
                        hadCollectionBtn.setVisibility(View.VISIBLE);
                    } else {
                        collectionBtn.setVisibility(View.VISIBLE);
                        hadCollectionBtn.setVisibility(View.GONE);
                    }
                    initHeader();
                    loadComment();
                    firstLoad = false;
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

    //收藏文章
    private void collectMagazine() {

        if (!AppSessionCache.getInstance().isLogin(MagazineDetailActivity.this)) {
            Intent intent = new Intent(MagazineDetailActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("subjectId", "" + subjectArticleId);

        loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("收藏中");
        loadingDialog.show();

        HttpRequest.post(AppConfig.MAGAZINE_COLLECT, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    AlertManager.showSuccessToast(MagazineDetailActivity.this, "收藏成功", false);
                    collectionBtn.setVisibility(View.GONE);
                    hadCollectionBtn.setVisibility(View.VISIBLE);
                    collect = true;
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

    //发布文章
    private void publishCommentFn(String content) {

        if (!AppSessionCache.getInstance().isLogin(MagazineDetailActivity.this)) {
            Intent intent = new Intent(MagazineDetailActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("subjectArticleId", subjectArticleId);
        params.put("content", content);

        loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("发布中");
        loadingDialog.show();

        HttpRequest.postJson(AppConfig.PUBLISH_COMMENT, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONObject data = JsonUtils.getJsonObject(response, "data", null);
                    JsonUtils.insertItem(commentData, 0, data);
                    collectNumber.setText(("" + (commentAmount + 1)) + "条评论");
                    WindowUtils.boldMethod(collectNumber);
                    publishView.getText().clear();
                    adapter.notifyDataSetChanged();
                    noDataView.setVisibility(View.GONE);
                    AlertManager.showSuccessToast(MagazineDetailActivity.this,"发布成功",false);
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

    private void loadComment() {

        String url = RestfulUrl.build(AppConfig.MAGAZINE_COMMENT, ":subjectArticleId", subjectArticleId);

        Map<String, String> params = new HashMap<>();
        params.put("pageIndex", pageIndex + "");
        params.put("pageSize", pageSize + "");

        HttpRequest.get(url, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
//                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    if (pageIndex == 1) {
                        commentData = new JSONArray();
                    }
                    JSONArray res = JsonUtils.getJsonArray(response, "data", new JSONArray());
                    JsonUtils.appendArray(commentData, res);
                    if (res.length() < pageSize) {
                        magazineListView.setFooterViewVisibility(View.VISIBLE);
                    } else {
                        magazineListView.setFooterViewVisibility(View.GONE);
                    }
                    if (commentData.length() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                    }
                    resetView();
                    adapter.notifyDataSetChanged();
                    pageIndex++;
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                showError();
                AlertManager.showErrorToast(MagazineDetailActivity.this, "服务器繁忙，请稍后重试", false);
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
            loadingView.setVisibility(View.GONE);
            AlertManager.showErrorInfo(MagazineDetailActivity.this);
        }
    }

    public void onReloadView() {
        loadMagazineDetail();
    }

    public void onRefreshView(RefreshLayout refreshLayout) {
        pageIndex = 1;
        refreshLayout.setNoMoreData(false);
        refreshLayout.setEnableLoadMore(true);
        loadComment();
    }

    private void initHeader() {
        int windowX = WindowUtils.getDeviceWidth(this);
        magazineTitle.setText(titleValue);
        WindowUtils.boldMethod(magazineTitle);
        ImageUtils.loadImage(glide, ImageUtils.resize(coverValue,1024,568), magazineCover, R.mipmap.loadpicture);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) magazineCover.getLayoutParams();
        params.height = windowX * 190 / 375;
        params.width = windowX;
        LinearLayout.LayoutParams param1 = (LinearLayout.LayoutParams) publishView.getLayoutParams();
        param1.width = (windowX - WindowUtils.dpToPixels(this, 20)) * 290 / 350;
        LinearLayout.LayoutParams param2 = (LinearLayout.LayoutParams) middleView.getLayoutParams();
        param2.width = (windowX - WindowUtils.dpToPixels(this, 20)) * 10 / 350;
        LinearLayout.LayoutParams param3 = (LinearLayout.LayoutParams) pushButton.getLayoutParams();
        param3.width = (windowX - WindowUtils.dpToPixels(this, 20)) * 50 / 350;
        WindowUtils.setMargins(publishView, WindowUtils.dpToPixels(this, 10), 0, 0, 0);
        WindowUtils.setMargins(pushButton, 0, 0, WindowUtils.dpToPixels(this, 10), 0);

        magazineCreateDate.setText("发布于" + DateUtils.timeStampToStr(Long.parseLong(createValue), "yyyy-MM-dd"));
        webView = findViewById(R.id.detail_webView);
        webView.setFocusable(false);
        webView.setWebViewClient(new WebViewClient() {

            /*
             * onPageStarted中启动一个计时器,到达设置时间后利用handle发送消息给activity执行超时后的动作.
             */
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!loadComplete && !MagazineDetailActivity.this.isDestroyed() && !MagazineDetailActivity.this.isFinishing()) {
                            loadingView.setVisibility(View.GONE);
                            loadFailView.setText("网络不给力~");
                            loadFailView.setVisibility(View.VISIBLE);
                            loadFailView.setText("数据居然走丢了~");
                        }
                    }
                }, outTime);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                loadingView.setVisibility(View.GONE);
                loadFailView.setText("网络不给力~");
                AlertManager.showErrorToast(MagazineDetailActivity.this, "网络不好，请确认网络哟", false);
                loadFailView.setVisibility(View.VISIBLE);
                loadFailView.setText("数据居然走丢了~");
            }

            /**
             * onPageFinished指页面加载完成,完成后取消计时器
             */
            @Override
            public void onPageFinished(WebView view, String url) {
                loadingView.setVisibility(View.GONE);
                loadComplete = true;
                super.onPageFinished(view, url);
            }
        });

        webView.loadUrl(AppConfig.ARTICLE_CONTENT + articleId);
    }

    @Override
    public void onClick(View v) {
        if (v == collectionBtn) {
            collectMagazine();
        } else if (v == hadCollectionBtn){
            deleteCollection();
        }else if (v == shareBtn){
            onShare();
        }
    }

    private void onShare() {
        String title = JsonUtils.getString(detail, "title", null);
        String desc = JsonUtils.getString(detail, "description", null);
        String cover = JsonUtils.getString(detail, "cover", null);
        String url = RestfulUrl.build(AppConfig.SUBJECT_ARTICLE_SHARE_URL, ":articleId", JsonUtils.getString(detail, "subjectArticleId", ""));
        new SharePopupWindow(this).show(url, cover, title, desc);
    }

    class MagazineListAdapter extends BaseAdapter {

        class MagazineListViewHolder {
            CircleImageView userImage;
            TextView userName;
            TextView commentTime;
            TextView commentContent;

            MagazineListViewHolder(View view) {
                userImage = view.findViewById(R.id.user_image);
                userName = view.findViewById(R.id.user_name);
                commentTime = view.findViewById(R.id.comment_time);
                commentContent = view.findViewById(R.id.comment_content);
            }
        }

        @Override
        public int getCount() {
            return commentData == null ? 0 : commentData.length();
        }

        @Override
        public JSONObject getItem(int i) {
            return JsonUtils.getJsonItem(commentData, i, null);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.view_magazine_detail_item, null);
                view.setTag(new MagazineListViewHolder(view));
            }

            MagazineListViewHolder viewHolder = (MagazineListViewHolder) view.getTag();
            JSONObject item = getItem(i);

            String imgUrl = JsonUtils.getString(item, "head", "");
            if (imgUrl.equals("")){
                viewHolder.userImage.setImageResource(R.mipmap.me_profilepic);
            } else {
                ImageUtils.loadImage(glide, imgUrl, viewHolder.userImage, R.mipmap.loadpicture);
            }

            viewHolder.userName.setText(JsonUtils.getString(item, "nickName", ""));
            WindowUtils.boldMethod(viewHolder.userName);
            String time = JsonUtils.getString(item, "createdAt", "");
            viewHolder.commentTime.setText(DateUtils.timeStampToStr(Long.parseLong(time), "yyyy-MM-dd HH:mm"));
            viewHolder.commentContent.setText(JsonUtils.getString(item, "content", ""));

            return view;
        }
    }

    private void deleteCollection() {

        loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("取消中");
        loadingDialog.show();

        HttpRequest.delete(AppConfig.DELETE_COLLECTION_ARTICLE + subjectArticleId, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    AlertManager.showSuccessToast(MagazineDetailActivity.this, "取消成功", false);
                    hadCollectionBtn.setVisibility(View.GONE);
                    collectionBtn.setVisibility(View.VISIBLE);
                    collect = false;
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(MagazineDetailActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(MagazineDetailActivity.this, "取消失败", false);
                AppLog.e("err", ex);
            }
        });
    }


    @Event(value = R.id.push_button, type = View.OnClickListener.class)
    private void surePublish(View view) {
        String content;
        content = publishView.getText().toString();
        String re = "^\\s*$";
        if (content.matches(re)) {
            AlertManager.showErrorToast(MagazineDetailActivity.this, "评论内容不能为空哦~，此评论没有发布",false);
        }else if (content.length() < 5){
            AlertManager.showErrorToast(MagazineDetailActivity.this, "评论内容长度不能小于5哦~，此评论没有发布",false);
        }
        else {
            publishCommentFn(content);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //强制隐藏键盘
        }
    }

}
