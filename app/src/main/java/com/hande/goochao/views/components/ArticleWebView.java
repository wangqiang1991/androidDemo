package com.hande.goochao.views.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.commons.http.RestfulUrl;
import com.hande.goochao.commons.views.gallery.GalleryActivity;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.config.AppConst;
import com.hande.goochao.session.AppSession;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.activity.LoginActivity;
import com.hande.goochao.views.activity.NewProductInformationActivity;
import com.hande.goochao.views.activity.ScenePreviewActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ArticleWebView extends WebView {

    private CustomLoadingDialog loadingDialog;

    public ArticleWebView(Context context) {
        super(context);
    }

    public ArticleWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArticleWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ArticleWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ArticleWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        loadingDialog = new CustomLoadingDialog(getContext());

        getSettings().setJavaScriptEnabled(true);

        addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void preview(String datas) {
                try {
                    JSONObject data = new JSONObject(datas);
                    String currentSrc = JsonUtils.getString(data,"currentImage","");
                    JSONArray srcArray = JsonUtils.getJsonArray(data, "images", new JSONArray());
                    String[] images = new String[srcArray.length()];
                    for (int i = 0; i < srcArray.length(); i++) {
                        String image = JsonUtils.getStringItem(srcArray, i, "");
                        images[i] = image;
                    }
                    Intent intent = new Intent(getContext(), GalleryActivity.class);
                    intent.putExtra("isLocal",false);
                    intent.putExtra("isFullSize",true );
                    intent.putExtra("currentSrc",currentSrc );
                    intent.putExtra("images", images);
                    getContext().startActivity(intent);
                    ((Activity) getContext()).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @JavascriptInterface
            public void showScene(String sceneInfo) {
                try {
                    JSONObject scene = new JSONObject(sceneInfo);
                    String sceneId = JsonUtils.getString(scene,"sceneId","");
                    loadScene(sceneId);
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
            @JavascriptInterface
            public void receiveGoodsMessage(String goodsInfo){
                try {
                    JSONObject goods = new JSONObject(goodsInfo);
                    String goodsId = JsonUtils.getString(goods,"goodsId","");
                    Intent intent = new Intent(getContext(), NewProductInformationActivity.class);
                    intent.putExtra("goodsId",goodsId );
                    getContext().startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @JavascriptInterface
            public void receiveCouponMessage(String couponInfo) {
                try {
                    JSONObject coupon = new JSONObject(couponInfo);
                    final String couponId = JsonUtils.getString(coupon,"couponId","");
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getCoupon(couponId);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, "gcJsBridge");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

    }

    private void loadScene(String sceneId ){

        String url = RestfulUrl.build(AppConfig.SCENE_DETAIL, ":sceneId", sceneId);

        HttpRequest.get(url, null,null, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    JSONObject data = JsonUtils.getJsonObject(response, "data", null);
                    JSONObject detailData = new JSONObject();
                    try {
                        detailData.put("detail",data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JSONArray sceneDatas = new JSONArray();
                    sceneDatas.put(detailData);
                    AppSession.getInstance().put(AppConst.SCENE_LIST_SESSION, sceneDatas);
                    Intent intent = new Intent(getContext(), ScenePreviewActivity.class);
                    intent.putExtra("currentIndex", 0);
                    getContext().startActivity(intent);
                } else {
                    AlertManager.showErrorToast(getContext(), JsonUtils.getString(response, "message", ""), false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorInfo(getContext());
            }
        });
    }

    private void getCoupon(String couponId) {
        if (!AppSessionCache.getInstance().isLogin(getContext())) {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            getContext().startActivity(intent);
            return;
        }

        loadingDialog.show();
        String url = RestfulUrl.build(AppConfig.GET_COUPON, ":couponId", couponId);
        Map<String, Object> params = new HashMap<>();
        HttpRequest.postJson(url, null, params, JSONObject.class, new RequestCallback<JSONObject>() {
            @Override
            public void onComplete(boolean success, JSONObject response) {
                loadingDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (JsonUtils.getCode(response) == 0) {
                    AlertManager.showSuccessToast(getContext(), "领取成功", false);
                } else {
                    AlertManager.showErrorToast(getContext(), JsonUtils.getString(response, "message", ""), false);
                }
            }

            @Override
            public void onError(Throwable ex) {
                AppLog.e("err", ex);
                AlertManager.showErrorInfo(getContext());
            }
        });
    }
}
