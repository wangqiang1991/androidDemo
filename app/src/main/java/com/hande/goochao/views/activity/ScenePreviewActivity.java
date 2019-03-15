package com.hande.goochao.views.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hande.goochao.R;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.commons.GlideApp;
import com.hande.goochao.commons.GlideRequests;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.Params;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.commons.views.gallery.OnPhotoViewChangeListener;
import com.hande.goochao.commons.views.gallery.PhotoView;
import com.hande.goochao.commons.views.gallery.TagImage;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.config.AppConst;
import com.hande.goochao.session.AppSession;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.utils.WindowUtils;
import com.hande.goochao.views.base.PayCloseNoToolBarActivity;
import com.hande.goochao.views.components.AlertManager;
import com.hande.goochao.views.components.CustomLoadingDialog;
import com.hande.goochao.views.widget.PraiseGoodsDetailPopupWindow;
import com.hande.goochao.views.widget.SharePopupWindow;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScenePreviewActivity extends PayCloseNoToolBarActivity implements ViewPager.OnPageChangeListener, OnPhotoViewChangeListener, View.OnClickListener {

    private static ScenePreviewActivity scenePreviewActivity;

    private CustomLoadingDialog loadingDialog;

    @ViewInject(R.id.button_layout)
    private View buttonLayout;
    @ViewInject(R.id.top_layer)
    private View topLayer;
    @ViewInject(R.id.bottom_layer)
    private View bottomLayer;
    @ViewInject(R.id.back_btn)
    private View backBtn;
    @ViewInject(R.id.more_btn)
    private View moreBtn;
    @ViewInject(R.id.collection_btn)
    private ImageView collectionBtn;
    @ViewInject(R.id.share_btn)
    private ImageView shareBtn;
    @ViewInject(R.id.download_btn)
    private View downloadBtn;

    @ViewInject(R.id.hack_view)
    private ViewPager photoViewPager;
    private int currentIndex;
    private JSONArray srcArray;
    private boolean isLocal;
    private boolean isLongClicked;
    private boolean isFullSize;
    private boolean mHideTags;
    private boolean collection; //是否收藏

    private GlideRequests glide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        glide = GlideApp.with(this);

        // 获取参数
        isLocal = getIntent().getBooleanExtra("isLocal", false);
        isFullSize = getIntent().getBooleanExtra("isFullSize", false);
        srcArray = AppSession.getInstance().get(AppConst.SCENE_LIST_SESSION);
        currentIndex = getIntent().getIntExtra("currentIndex", 0);

        // 初始化
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (WindowUtils.hasNavigationBar(this)) {
            WindowUtils.hideBottomUIMenu(this);
        }
        ScenePreviewActivity.scenePreviewActivity = this;
    }

    private void init() {
        loadingDialog = new CustomLoadingDialog(this);

        initViewPager();
        backBtn.setOnClickListener(this);
        collectionBtn.setOnClickListener(this);
        moreBtn.setOnClickListener(this);
        downloadBtn.setOnClickListener(this);
        shareBtn.setOnClickListener(this);

        onPageSelected(currentIndex);
    }

    /**
     * 关闭界面和释放资源
     */
    @Override
    public void finish() {
        super.finish();
        ScenePreviewActivity.onPageChangeListener = null;
        ScenePreviewActivity.scenePreviewActivity = null;
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void initViewPager() {
        setContentView(R.layout.activity_scene_preview);
        photoViewPager.setAdapter(new SamplePagerAdapter());
        photoViewPager.setCurrentItem(currentIndex);
        photoViewPager.addOnPageChangeListener(this);
        photoViewPager.setBackgroundColor(Color.BLACK);
        photoViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        this.currentIndex = position;
        if (ScenePreviewActivity.onPageChangeListener != null) {
            ScenePreviewActivity.onPageChangeListener.onPageChange(position, photoViewPager.getAdapter().getCount());
        }
        refreshButtonLayout();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onChange(ImageView imageView, float scale, RectF displayRect) {
        refreshTags(imageView, scale, displayRect);
    }

    private void refreshTags(ImageView imageView, float scale, RectF displayRect) {
        List<ImageView> tagViews = (List<ImageView>) imageView.getTag();
        if (tagViews == null || tagViews.isEmpty()) {
            return;
        }
        AppLog.i("tag imageView update ");
        for (ImageView tagView : tagViews) {
            float w = displayRect.left * -1 + displayRect.right;
            float h = displayRect.top * -1 + displayRect.bottom;

            float tagOffsetX = tagView.getMeasuredWidth() * (scale - 1);
            float tagOffsetY = tagView.getMeasuredHeight() * (scale - 1);

            if (tagOffsetX < 0) {
                tagOffsetX = 0;
            }

            if (tagOffsetY < 0) {
                tagOffsetY = 0;
            }

            // 获取数据
            JSONObject tagInfo = (JSONObject) tagView.getTag();
            JSONObject location = JsonUtils.newJsonObject(JsonUtils.getString(tagInfo, "location", "{}"), null);
            double x = JsonUtils.getDouble(location, "x", 0) / 100;
            double y = JsonUtils.getDouble(location, "y", 0) / 100;

            tagView.setTranslationX((float) (w * x + displayRect.left + tagOffsetX / 2));
            tagView.setTranslationY((float) (h * y + displayRect.top + tagOffsetY / 2));
        }
    }

    private void hideTags() {
        AppLog.i("tag imageView hide ");
        int childCount = photoViewPager.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View tagLayout = photoViewPager.getChildAt(i).findViewById(R.id.tag_layout);
            startHideAnimation(tagLayout);
        }

        startHideAnimation(topLayer);
        startHideAnimation(bottomLayer);
        startHideAnimation(buttonLayout);
    }

    public void startHideAnimation(final View view) {
        /**
         * @param fromAlpha 开始的透明度，取值是0.0f~1.0f，0.0f表示完全透明， 1.0f表示和原来一样
         * @param toAlpha 结束的透明度，同上
         */
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        //设置动画持续时长
        alphaAnimation.setDuration(300);
        //设置动画结束之后的状态是否是动画的最终状态，true，表示是保持动画结束时的最终状态
        alphaAnimation.setFillAfter(true);
        //设置动画结束之后的状态是否是动画开始时的状态，true，表示是保持动画开始时的状态
        alphaAnimation.setFillBefore(true);
        //设置动画的重复模式：反转REVERSE和重新开始RESTART
        alphaAnimation.setRepeatMode(AlphaAnimation.RESTART);
        //设置动画播放次数
        alphaAnimation.setRepeatCount(0);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
                if (!(view instanceof  ViewGroup)) {
                    return;
                }
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    ((ViewGroup) view).getChildAt(i).setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        //开始动画
        view.startAnimation(alphaAnimation);
    }

    private void showTags() {
        AppLog.i("tag imageView show ");
        int childCount = photoViewPager.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View tagLayout = photoViewPager.getChildAt(i).findViewById(R.id.tag_layout);
            startShowAnimation(tagLayout);
        }

        startShowAnimation(topLayer);
        startShowAnimation(bottomLayer);
        startShowAnimation(buttonLayout);
    }

    public void startShowAnimation(final View view) {
        /**
         * @param fromAlpha 开始的透明度，取值是0.0f~1.0f，0.0f表示完全透明， 1.0f表示和原来一样
         * @param toAlpha 结束的透明度，同上
         */
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        //设置动画持续时长
        alphaAnimation.setDuration(300);
        //设置动画结束之后的状态是否是动画的最终状态，true，表示是保持动画结束时的最终状态
        alphaAnimation.setFillAfter(true);
        //设置动画结束之后的状态是否是动画开始时的状态，true，表示是保持动画开始时的状态
        alphaAnimation.setFillBefore(true);
        //设置动画的重复模式：反转REVERSE和重新开始RESTART
        alphaAnimation.setRepeatMode(AlphaAnimation.RESTART);
        //设置动画播放次数
        alphaAnimation.setRepeatCount(0);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
                if (!(view instanceof  ViewGroup)) {
                    return;
                }
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    ((ViewGroup) view).getChildAt(i).setVisibility(View.VISIBLE);
                }
                refreshButtonLayout();
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        //开始动画
        view.startAnimation(alphaAnimation);
    }

    private void refreshPreview() {
        srcArray = AppSession.getInstance().get(AppConst.SCENE_LIST_SESSION);
        photoViewPager.getAdapter().notifyDataSetChanged();
    }

    private void refreshButtonLayout() {
        JSONObject currentScene = getCurrentScene();
        String designerId = JsonUtils.getString(currentScene, "detail.designerId", null);
        collection = JsonUtils.getBoolean(currentScene, "detail.collection", false);
        if (TextUtils.isEmpty(designerId)) {
            moreBtn.setVisibility(View.GONE);
        } else {
            moreBtn.setVisibility(View.VISIBLE);
        }

        if (collection) {
            collectionBtn.setImageResource(R.mipmap.new_product_collect_select);
        } else {
            collectionBtn.setImageResource(R.mipmap.nav_icon_collect_def);
        }
    }

    private JSONObject getCurrentScene() {
        return JsonUtils.getJsonItem(srcArray, currentIndex, null);
    }

    /**
     * 功能：保存图片到本地
     *
     * @param fileName
     * @param bitmap
     */
    private void saveMyBitmap(String fileName, String filePath, Bitmap bitmap) {
        File f = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            f = new File(dir.getAbsolutePath(), fileName);
            f.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //通知媒体库刷新
        try {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + f.getPath())));
            MediaScannerConnection.scanFile(ScenePreviewActivity.this, new String[]{f.getPath()}, null, null);
            refreshAlbum(f.getPath(), bitmap.getWidth(), bitmap.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingDialog.dismiss();
                AlertManager.showSuccessToast(ScenePreviewActivity.this, "图片已保存到相册", false);
            }
        });
        bitmap.recycle();

    }

    private void refreshAlbum(String filePath, int width, int height) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.ImageColumns.TITLE, "title");
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, "filename.jpg");
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.ImageColumns.ORIENTATION, 0);
        values.put(MediaStore.Images.ImageColumns.DATA, filePath);
        values.put(MediaStore.Images.ImageColumns.WIDTH, width);
        values.put(MediaStore.Images.ImageColumns.HEIGHT, height);
        try {
            Uri uri = ScenePreviewActivity.this.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                Log.e("jileniao.net", "Failed to insert MediaStore");
            } else {
                ScenePreviewActivity.this.sendBroadcast(new Intent(
                        "com.android.camera.NEW_PICTURE", uri));
            }
        } catch (Exception e) {
            Log.e("jileniao.net", "Failed to write MediaStore", e);
        }
    }

    /**
     * 功能：将Drawable对象转为Bitmap对象
     *
     * @param drawable
     * @return
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void onClick(View v) {
        if (v == backBtn) {
            finish();
        } else if (v == collectionBtn) {
            if (collection){
                deletePicture();
            }else {
                doCollection();
            }
        } else if (v == moreBtn) {
            toDesignerPage();
        } else if (v == downloadBtn) {
            doSavePicture();
        } else if (v == shareBtn) {
            showShareView();
        } else {
            JSONObject tagInfo = (JSONObject) v.getTag();
            int tagType = JsonUtils.getInt(tagInfo, "type", 1);
            String goodsId = JsonUtils.getString(tagInfo, "goodsId", null);
            if (TextUtils.isEmpty(goodsId)) {
                return;
            }
            if (tagType == 1) {
                // 购买标签点击
                Intent intent = new Intent(this, NewProductInformationActivity.class);
                intent.putExtra("goodsId", goodsId);
                startActivity(intent);
            } else {
                showPopupPraiseGoods(tagInfo);
            }
        }
    }

    private void showShareView() {
        JSONObject currentScene = getCurrentScene();
        String sceneId = JsonUtils.getString(currentScene, "sceneId", JsonUtils.getString(currentScene, "detail.sceneId", null));
        String image = JsonUtils.getString(currentScene, "detail.image", null);
        String url = RestfulUrl.build(AppConfig.SHARE_SHARE_URL, ":sceneId", sceneId);
        new SharePopupWindow(this).show(url, image, "图片", url);
    }

    private void toDesignerPage() {
        JSONObject currentScene = getCurrentScene();
        String sceneId = JsonUtils.getString(currentScene, "sceneId", JsonUtils.getString(currentScene, "detail.sceneId", null));
        String designerId = JsonUtils.getString(currentScene, "detail.designerId", null);
        Intent intent = new Intent(this, DesignerActivity.class);
        intent.putExtra("sceneId", sceneId);
        intent.putExtra("designerId", designerId);
        startActivity(intent);
    }

    private void doCollection() {
        if (!AppSessionCache.getInstance().isLogin(this)) {
            LoginActivity.go(this);
            return;
        }
        loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("收藏中");
        loadingDialog.show();

        String defaultSceneId = JsonUtils.getString(getCurrentScene(), "detail.sceneId", null);
        String sceneId = JsonUtils.getString(getCurrentScene(), "sceneId", defaultSceneId);
        HttpRequest.post(AppConfig.SCENE_COLLECT, null, Params.buildForStr("sceneId", sceneId), JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    JsonUtils.put(getCurrentScene(), "detail.collection", true);
                    refreshButtonLayout();
                    AlertManager.showSuccessToast(ScenePreviewActivity.this, "图片收藏成功", false);
                } else {
                    if (!AuthUtils.validateAuth(code)) {
                        LoginActivity.go(ScenePreviewActivity.this);
                    } else {
                        AlertManager.showErrorToast(ScenePreviewActivity.this, JsonUtils.getResponseMessage(response), false);
                    }
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(ScenePreviewActivity.this, "图片收藏失败", false);
            }
        });

    }

    private void deletePicture() {

        loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("取消中");
        loadingDialog.show();
        JSONObject currentScene = getCurrentScene();
        String sceneId = JsonUtils.getString(currentScene,"sceneId","");
        HttpRequest.delete(AppConfig.DELETE_COLLECTION_PICTURE + sceneId, null, null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    AlertManager.showSuccessToast(ScenePreviewActivity.this, "取消成功", false);
                    JsonUtils.put(getCurrentScene(), "detail.collection", false);
                    refreshButtonLayout();
                } else {
                    String message = JsonUtils.getResponseMessage(response);
                    AlertManager.showErrorToast(ScenePreviewActivity.this, message, false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorToast(ScenePreviewActivity.this,"取消失败",false);
            }
        });
    }


    private void showPopupPraiseGoods(JSONObject goodsInfo) {
        JSONObject currentScene = getCurrentScene();
        if (currentScene == null) {
            return;
        }
        new PraiseGoodsDetailPopupWindow(this, currentScene, goodsInfo).show();
    }

    class SamplePagerAdapter extends PagerAdapter {

        private PhotoView currentPhoto;

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            currentPhoto = ((View) object).findViewById(R.id.imgView);
        }

        public PhotoView getCurrentPhoto() {
            return currentPhoto;
        }

        @Override
        public int getCount() {
            return srcArray == null ? 0 : srcArray.length();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        public JSONObject getItem(int position) {
            if (srcArray == null) {
                return null;
            }
            return JsonUtils.getJsonItem(srcArray, position, null);
        }

        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            JSONObject item = getItem(position);
            String imagePath = JsonUtils.getString(item, "image", JsonUtils.getString(item, "detail.image", ""));
            final JSONArray goodsList = JsonUtils.getJsonArray(item, "detail.goodsList", null);

            final View view = LayoutInflater.from(ScenePreviewActivity.this).inflate(R.layout.gallery_scene_item, null);
            final PhotoView photoView = view.findViewById(R.id.imgView);
//            final ImageView blurView = view.findViewById(R.id.blur_view);
            // 设置默认图片
            photoView.setImageResource(android.R.color.darker_gray);
            container.addView(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//            final int size = isFullSize ? 2000 : 1000;
            if (isLocal) {
                String filePath = "file://" + imagePath;
                ImageUtils.loadImage(glide, filePath, photoView, -1);
            } else {
                final View loading = view.findViewById(R.id.loading);
                final View loadingBackView = view.findViewById(R.id.loading_back_view);
                loading.setVisibility(View.VISIBLE);
                loadingBackView.setVisibility(View.VISIBLE);
                String imageUrl = ImageUtils.zoomResizeScene(imagePath);

                ImageUtils.loadImage(glide, imageUrl, photoView, R.mipmap.loading_back, new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        loading.setVisibility(View.GONE);
                        loadingBackView.setVisibility(View.GONE);
                        photoView.setImageDrawable(resource);
                        if (goodsList == null) {
                            return;
                        }
                        // 获取图片实际大小
                        BitmapDrawable drawable = (BitmapDrawable) resource;
                        float imageW = drawable.getIntrinsicWidth();
                        float imageH = drawable.getIntrinsicHeight();

                        // 获取屏幕大小
                        float screenW = WindowUtils.getDeviceWidth(ScenePreviewActivity.this);
                        float screenH = WindowUtils.getDeviceHeight(ScenePreviewActivity.this);
                        if (WindowUtils.hasNavigationBar(ScenePreviewActivity.this)) {
                            screenH = screenH + WindowUtils.getBottomBarHeight(ScenePreviewActivity.this);
                        }
                        // 使用屏幕宽度和图片实际宽度计算比例
                        float scale = screenW / imageW;
                        // 使用比例和图片实际高度计算图片在屏幕中的高度
                        float realH = imageH * scale;

                        // 得到偏移，垂直方向
                        float offset = (screenH - realH) / 2;

                        FrameLayout contentView = view.findViewById(R.id.tag_layout);
                        List<ImageView> tagViews = new ArrayList<>();

                        for (int i = 0; i < goodsList.length(); i++) {
                            // 获取数据
                            JSONObject tagInfo = JsonUtils.getJsonItem(goodsList, i, null);
                            int tagType = JsonUtils.getInt(tagInfo, "type", 1);
                            JSONObject location = JsonUtils.newJsonObject(JsonUtils.getString(tagInfo, "location", "{}"), null);
                            double x = JsonUtils.getDouble(location, "x", 0) / 100;
                            double y = JsonUtils.getDouble(location, "y", 0) / 100;

                            // 初始化tag
                            int tagSize = WindowUtils.dpToPixels(getApplicationContext(), 33);
                            TagImage tag = new TagImage(ScenePreviewActivity.this);
                            tag.setLayoutParams(new LayoutParams(tagSize, tagSize));
                            if (tagType == 1) {
                                tag.setGoodsMode();
                            } else {
                                tag.setPraiseMode();
                            }
                            tag.setTranslationX((float) (screenW * x));
                            tag.setTranslationY((float) (offset + realH * y));
                            tag.setTag(tagInfo);
                            contentView.addView(tag);
                            tagViews.add(tag);

                            tag.setOnClickListener(ScenePreviewActivity.this);
                        }
                        if (mHideTags) {
                            contentView.setVisibility(View.GONE);
                        } else {
                            contentView.setVisibility(View.VISIBLE);
                        }
                        photoView.setTag(tagViews);


                        photoView.setFirstZoomDisableDrag(true);
                        photoView.zoomTo(1.0f, screenW / 2, 0);
                    }
                });


                photoView.setOnPhotoViewChangeListener(ScenePreviewActivity.this);
            }
            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isLongClicked) {
                        if (!mHideTags) {
                            // 隐藏tags
                            hideTags();
                            mHideTags = true;
                        } else {
                            showTags();
                            mHideTags = false;
                        }
                    }
                }
            });

            photoView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    isLongClicked = true;
                    AlertDialog.Builder builder = new AlertDialog.Builder(ScenePreviewActivity.this);
                    builder.setItems(new String[]{"保存图片至相册", "取消"},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        doSavePicture();
                                    }
                                    isLongClicked = false;
                                }
                            });
                    builder.setCancelable(true);
                    builder.show();
                    return false;
                }
            });
            return view;
        }
    }

    private void doSavePicture() {
        loadingDialog = new CustomLoadingDialog(this);
        loadingDialog.setLoadingText("下载中");
        loadingDialog.show();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        final String fileName = format.format(new Date()) + ".png";
        new Thread(new Runnable() {
            @Override
            public void run() {
                //系统相册目录
                String galleryPath = Environment.getExternalStorageDirectory()
                        + File.separator + Environment.DIRECTORY_DCIM
                        + File.separator + "Camera" + File.separator;
                PhotoView photoView = ((SamplePagerAdapter) photoViewPager.getAdapter()).getCurrentPhoto();
//                saveMyBitmap(fileName, Environment.getExternalStorageDirectory() + AppConfig.CACHE_DIR + "/images/", drawableToBitmap(photoView.getDrawable()));
                saveMyBitmap(fileName, galleryPath, drawableToBitmap(photoView.getDrawable()));
            }
        }).start();
    }

    private static OnPageChangeListener onPageChangeListener;

    public static void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        ScenePreviewActivity.onPageChangeListener = onPageChangeListener;
    }

    public static void refreshData() {
        if (ScenePreviewActivity.scenePreviewActivity != null) {
            ScenePreviewActivity.scenePreviewActivity.refreshPreview();
        }
    }

    public interface OnPageChangeListener {
        void onPageChange(int currentIndex, int size);
    }

}
