package com.hande.goochao.commons.webview_handle;

import android.content.Context;
import android.text.method.HideReturnsTransformationMethod;
import android.webkit.JavascriptInterface;

import com.hande.goochao.utils.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wangenmao
 * @description /n
 * Created by Wangenmao on 2018/9/7.
 */

public class WebViewApiManager {


    private List<JSONObject> apis = new ArrayList();
    static WebViewApiManager instance = null;

    public  WebViewApiManager(Context context) {
        this.mContext = context;
    }

    private Context mContext = null;

    public static WebViewApiManager getInstance(Context context) {
        /*if (instance == null)
            instance = new WebViewApiManager(context);
        return instance;*/
        return new WebViewApiManager(context);
    }

    public void registerApi(String apiName, InvokeHandler handler) {
        JSONObject api = new JSONObject();
        try {
            api.put("apiName", apiName);
            api.put("handler", handler);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apis.add(api);
    }


    @JavascriptInterface
    public String getPlatform(Object msg) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", 0);
            jsonObject.put("data", "android");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    @JavascriptInterface
    public String hasApi(Object msg) {
        JSONObject result = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject(msg.toString());
            String apiName = JsonUtils.getString(jsonObject, "apiName", "");
            for (int i = 0; i < apis.size(); i++) {
                JSONObject object = apis.get(i);
                String name = JsonUtils.getString(object, "apiName", "");
                if (apiName.equals(name)) {
                    result.put("code", 0);
                    result.put("data", "true");
                    return result.toString();
                }
            }
            result.put("code", -1);
            result.put("data", "该版本暂不支持此操作，请升级至最新版本");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    @JavascriptInterface
    public String invoke(Object msg) {
        try {
            JSONObject apiObject = new JSONObject(msg.toString());
            String apiName = JsonUtils.getString(apiObject, "apiName", "");

            for (JSONObject api : apis) {
                String name = JsonUtils.getString(api, "apiName", "");
                if (apiName.equals(name)) {
                    InvokeHandler handler = (InvokeHandler) api.get("handler");
                    return handler.invokeHandler(msg.toString(), mContext);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject error = new JSONObject();
        try {
            error.put("code", -1);
            error.put("errMsg", "该版本暂不支持此操作，请升级至最新版本");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return error.toString();

    }

}
