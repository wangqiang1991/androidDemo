package com.hande.goochao.views.widget;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.hande.goochao.R;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.views.components.AlertManager;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.x;

/**
 * 分享弹出窗口
 * Created by Wangem on 2018/3/13.
 */

public class SharePopupWindow {

    // context property
    private Activity activity;
    private Window window;
    private PopupWindow popupWindow;

    public SharePopupWindow(Activity activity) {
        this.activity = activity;
        this.window = activity.getWindow();
    }

    public void show(String url, String imageUrl, String title, String desc) {
        View contentView = activity.getLayoutInflater().inflate(R.layout.layout_share_pop, null, false);
        popupWindow = new PopupWindow(contentView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(activity.getResources().getDrawable(android.R.color.transparent));
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.popwindow_anim_style);

        WindowManager.LayoutParams params = window.getAttributes();
        params.alpha = 0.5f;
        window.setAttributes(params);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = window.getAttributes();
                params.alpha = 1.0f;
                window.setAttributes(params);
            }
        });

        popupWindow.showAtLocation(window.getDecorView(), Gravity.BOTTOM, 0, 0);

        View shareFriendsBtn = contentView.findViewById(R.id.share_friends_btn);
        View shareCommunityBtn = contentView.findViewById(R.id.share_community_btn);
        View shareCollectBtn = contentView.findViewById(R.id.share_collect_btn);

        OnShareClickListener clickListener = new OnShareClickListener(url, imageUrl, title, desc);
        shareFriendsBtn.setOnClickListener(clickListener);
        shareCommunityBtn.setOnClickListener(clickListener);
        shareCollectBtn.setOnClickListener(clickListener);
    }

    private void shareToScene(int scene, String url, Bitmap thumbData, String title, String desc) {
        WXMediaMessage message = new WXMediaMessage();
        message.title = title;
        message.description = desc;
        message.setThumbImage(thumbData);

        WXWebpageObject object = new WXWebpageObject();
        object.webpageUrl = url;
        message.mediaObject = object;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.message = message;
        req.scene = scene;

        IWXAPI api = WXAPIFactory.createWXAPI(activity, AppConfig.WX_APPID);
        boolean res = api.sendReq(req);
        if (res) {
            close();
        } else {
            AlertManager.showErrorToast(activity, "分享失败", false);
        }
    }

    public void close() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    class OnShareClickListener implements View.OnClickListener {

        private String url;
        private String title;
        private String imageUrl;
        private String desc;

        public OnShareClickListener(String url, String imageUrl, String title, String desc) {
            this.url = url;
            this.title = title;
            this.imageUrl = imageUrl;
            this.desc = desc;
        }

        @Override
        public void onClick(final View v) {
            ImageOptions options = new ImageOptions.Builder().setSize(100, 100).build();
            x.image().loadDrawable(imageUrl + "?imageView2/1/w/100/h/100", options, new Callback.CommonCallback<Drawable>() {
                @Override
                public void onSuccess(Drawable result) {
                    if (result instanceof BitmapDrawable) {
                        Bitmap bitmap = ((BitmapDrawable) result).getBitmap();
                        switch (v.getId()) {
                            case R.id.share_friends_btn:
                                shareToScene(SendMessageToWX.Req.WXSceneSession, url, bitmap, title, desc);
                                break;
                            case R.id.share_community_btn:
                                shareToScene(SendMessageToWX.Req.WXSceneTimeline, url, bitmap, title, desc);
                                break;
                            case R.id.share_collect_btn:
                                shareToScene(SendMessageToWX.Req.WXSceneFavorite, url, bitmap, title, desc);
                                break;
                        }
                    } else {
                        AlertManager.showErrorToast(activity, "图片类型异常", false);
                    }
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    AlertManager.showErrorToast(activity, "加载图片封面失败", false);
                }

                @Override
                public void onCancelled(CancelledException cex) {
                    AlertManager.toast(activity, "取消分享");
                }

                @Override
                public void onFinished() {

                }
            });
        }
    }

}
