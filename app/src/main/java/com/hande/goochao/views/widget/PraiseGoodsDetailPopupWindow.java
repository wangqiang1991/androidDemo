package com.hande.goochao.views.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.Params;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.views.gallery.GalleryActivity;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.ImageOptionsUtil;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.activity.LoginActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 点赞商品详情弹出窗口
 * Created by Wangem on 2018/3/13.
 */

public class PraiseGoodsDetailPopupWindow implements OnItemClickListener {

    private Activity activity;
    private JSONObject sceneInfo;
    private JSONObject goodsInfo;
    private Window window;
    private PopupWindow popupWindow;

    private TextView titleText;
    private TextView descriptionText;
    private EditText commentText;
    private ConvenientBanner convenientBanner;
    private ImageView closeView;
    private TagFlowLayout tagFlowLayout;
    private View joinView;
    private View hadView;

    private CustomLoadingDialog loadingDialog;

    private String banners;
    private List<String> bannerImages = new ArrayList<>();
    private String[] bannerImage;

    private GlideRequests glide;

    public PraiseGoodsDetailPopupWindow(Activity activity, JSONObject sceneInfo, JSONObject goodsInfo) {
        this.activity = activity;
        this.sceneInfo = sceneInfo;
        this.goodsInfo = goodsInfo;
        glide = GlideApp.with(activity);
        window = activity.getWindow();
        loadingDialog = new CustomLoadingDialog(activity);
        loadingDialog.setLoadingText("等待提交完成");
    }

    public void show() {
        View contentView = activity.getLayoutInflater().inflate(R.layout.scene_pop_view, null, false);
        int height = WindowUtils.getDeviceHeight(activity) * 570 / 670 ;
        popupWindow = new PopupWindow(contentView, LinearLayout.LayoutParams.MATCH_PARENT ,height , true);
        popupWindow.setBackgroundDrawable(activity.getResources().getDrawable(android.R.color.transparent));
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.popwindow_anim_style);

        WindowManager.LayoutParams params = window.getAttributes();
        params.alpha = 0.5f;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setAttributes(params);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                hideBottomUIMenu(activity);
                WindowManager.LayoutParams params = window.getAttributes();
                params.alpha = 1.0f;
                window.setAttributes(params);
            }
        });
        popupWindow.showAtLocation(window.getDecorView(), Gravity.BOTTOM, 0,  0);

        // get property
        JSONObject comment = getComment(goodsInfo);
        String title = JsonUtils.getString(goodsInfo, "goods.title", "");
        String description = JsonUtils.getString(goodsInfo, "goods.description", "");
        banners = JsonUtils.getString(goodsInfo,"goods.banner",null);

        List<JSONObject> tags = getTags(goodsInfo);
        final String commentTags = JsonUtils.getString(comment, "tags", "");
        String replayContent = JsonUtils.getString(comment, "replayContent", "");

        // get view
        convenientBanner = contentView.findViewById(R.id.scene_pop_banner);
        closeView = contentView.findViewById(R.id.praise_close);
        titleText = contentView.findViewById(R.id.scene_pop_name);
        descriptionText = contentView.findViewById(R.id.scene_pop_desc);
        commentText = contentView.findViewById(R.id.scene_pop_edit);
        tagFlowLayout = contentView.findViewById(R.id.scene_pop_tag_layout);
        joinView = contentView.findViewById(R.id.default_like_view);
        hadView = contentView.findViewById(R.id.had_like_view);
        convenientBannerInit();

        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        // set value
        titleText.setText(title);
        WindowUtils.boldMethod(titleText);
        descriptionText.setText(description);

        if (comment != null) {
            joinView.setVisibility(View.GONE);
            hadView.setVisibility(View.VISIBLE);
            commentText.setText(replayContent);
            // readonly
            commentText.setCursorVisible(false);
            commentText.setFocusable(false);
            commentText.setFocusableInTouchMode(false);
        }

        if (tags != null) {
            // set adapter
            tagFlowLayout.setAdapter(new TagAdapter<JSONObject>(tags) {
                @Override
                public View getView(FlowLayout parent, int position, JSONObject tag) {
                    View view = LayoutInflater.from(activity).inflate(R.layout.layout_praise_tag, parent, false);
                    TextView tv = view.findViewById(R.id.tag_text);
                    view.setTag(tag);
                    tv.setText(JsonUtils.getString(tag, "title", ""));
                    if (commentTags.contains(JsonUtils.getString(tag, "tagsId", ""))) {
                        view.setSelected(true);
                    }
                    return view;
                }
            });

            // set event
            tagFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
                @Override
                public boolean onTagClick(View view, int position, FlowLayout parent) {
                    if (!isComment()) {
                        view.setSelected(!view.isSelected());
                    }
                    return false;
                }
            });
        }

        joinView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveComment();
                InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                imm.hideSoftInputFromWindow(joinView.getWindowToken(), 0); //强制隐藏键盘
            }
        });
    }

    // banner初始化
    private void convenientBannerInit(){
        String[] datas = banners.split(",");
        bannerImages.addAll(Arrays.asList(datas));
        bannerImage = new String[bannerImages.size()];
        for (int i = 0; i < bannerImages.size(); i++) {
            bannerImages.set(i, bannerImages.get(i) + AppConfig.IMAGE_COMPRESS);
            bannerImage[i] = bannerImages.get(i);
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
                .setOnItemClickListener(this)
                .setManualPageable(true);

        int width = WindowUtils.getDeviceWidth(activity) - WindowUtils.dpToPixels(activity,70);
        int height = width ;
        convenientBanner.setLayoutParams(new RelativeLayout.LayoutParams(width , height));
        WindowUtils.setMargins(convenientBanner,WindowUtils.dpToPixels(activity,35),WindowUtils.dpToPixels(activity,35),WindowUtils.dpToPixels(activity,35),0);
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

    @Override
    public void onItemClick(int position) {
        String currentSrc = bannerImages.get(position);
        Intent intent = new Intent();
        intent.setClass(activity, GalleryActivity.class);
        intent.putExtra("isLocal",false );
        intent.putExtra("currentSrc",currentSrc );
        intent.putExtra("images", bannerImage);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private boolean isComment() {
        return getComment(goodsInfo) != null;
    }

    private void saveComment() {
        if (!AppSessionCache.getInstance().isLogin(activity)) {
            LoginActivity.go(activity);
            return;
        }

        List<String> tags = new ArrayList<>();
        for (int i = 0; i < tagFlowLayout.getChildCount(); i++) {
            View tagView = ((ViewGroup) tagFlowLayout.getChildAt(i)).getChildAt(0);
            if (tagView.isSelected()) {
                JSONObject tagInfo = (JSONObject) tagView.getTag();
                String tagsId = JsonUtils.getString(tagInfo, "tagsId", "");
                tags.add(tagsId);
            }
        }

        String sceneId = JsonUtils.getString(sceneInfo, "sceneId", JsonUtils.getString(sceneInfo, "detail.sceneId", ""));
        String goodsId = JsonUtils.getString(goodsInfo, "goodsId", "");
        String tagsId = StringUtils.join(tags, ",");
        String comment = commentText.getText().toString().trim();

        final JSONObject commentItem = new JSONObject();
        JsonUtils.put(commentItem, "replayContent", comment);
        JsonUtils.put(commentItem, "tags", tagsId);

        loadingDialog.show();
        Map<String, Object> params = Params.buildForObj("goodsId", goodsId, "sceneId", sceneId, "tags", tagsId, "content", comment);
        HttpRequest.postJson(AppConfig.PRAISE_GOODS_COMMENT, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    AlertManager.showSuccessToast(activity, "已加入喜欢", false);
                    JSONArray comments = new JSONArray();
                    JsonUtils.appendItem(comments, commentItem);
                    JsonUtils.put(goodsInfo, "comments", comments);
                    refreshWindow();
                } else {
                    if (!AuthUtils.validateAuth(code)) {
                        LoginActivity.go(activity);
                    } else {
                        AlertManager.showErrorToast(activity, JsonUtils.getResponseMessage(response), false);
                    }
                }
            }

            @Override
            public void onError(Throwable ex) {
                AlertManager.showErrorToast(activity, "提交失败", false);
            }
        });
    }

    private void refreshWindow() {
        hadView.setVisibility(View.VISIBLE);
        joinView.setVisibility(View.GONE);
        hadView.setEnabled(false);
        // readonly
        commentText.setCursorVisible(false);
        commentText.setFocusable(false);
        commentText.setFocusableInTouchMode(false);
    }

    private JSONObject getComment(JSONObject goodsInfo) {
        JSONArray comments = JsonUtils.getJsonArray(goodsInfo, "comments", null);
        if (comments == null || comments.length() == 0) {
            return null;
        }

        JSONObject comment = JsonUtils.getJsonItem(comments, 0, null);
        return comment;
    }

    private List<JSONObject> getTags(JSONObject goodsInfo) {
        JSONArray tagArr = JsonUtils.getJsonArray(goodsInfo, "goods.tags", null);
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

    protected void hideBottomUIMenu(Activity activity) {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = activity.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
