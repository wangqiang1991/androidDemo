package com.hande.goochao.commons.webview_handle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.JsonUtils;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.x;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/9/10.
 */
public class ShareToCircleInvokeHandler implements InvokeHandler {

    private JSONObject resultObject;

    @Override
    public String invokeHandler(String paramsStr,final Context context) throws JSONException {

        resultObject = new JSONObject();
        JSONObject paramsObject = new JSONObject(paramsStr);
        JSONObject value = JsonUtils.getJsonObject(paramsObject,"params",null);
        final  String shareTitle = JsonUtils.getString(value,"title","");
        final String shareDesc = JsonUtils.getString(value,"desc","");
        final String shareUrl = JsonUtils.getString(value,"link","");
        final String imgUrl = JsonUtils.getString(value,"imgUrl","") + "?imageView2/1/w/100/h/100";

        ImageOptions options = new ImageOptions.Builder().setSize(100, 100).build();
        x.image().loadDrawable(imgUrl , options, new Callback.CommonCallback<Drawable>() {
            @Override
            public void onSuccess(Drawable result) {
                if (result instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) result).getBitmap();
                    WXMediaMessage message = new WXMediaMessage();
                    message.title = shareTitle;
                    message.description = shareDesc;
                    message.setThumbImage(bitmap);

                    WXWebpageObject object = new WXWebpageObject();
                    object.webpageUrl = shareUrl;
                    message.mediaObject = object;

                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.message = message;
                    req.scene = SendMessageToWX.Req.WXSceneTimeline;

                    IWXAPI api = WXAPIFactory.createWXAPI(context , AppConfig.WX_APPID);
                    boolean res = api.sendReq(req);
                    if (res) {
                        try {
                            resultObject.put("data","success");
                            resultObject.put("code" , 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            resultObject.put("code",-1);
                            resultObject.put("errMsg","分享失败");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        resultObject.put("code",-1);
                        resultObject.put("errMsg","图片类型异常");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                try {
                    resultObject.put("code",-1);
                    resultObject.put("errMsg","加载图片封面失败");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {
                try {
                    resultObject.put("code",-1);
                    resultObject.put("errMsg","取消分享");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinished() {

            }
        });
        return resultObject.toString();
    }
}
