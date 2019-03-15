package com.hande.goochao.commons.http;

import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.hande.goochao.BuildConfig;
import com.hande.goochao.GoochaoApplication;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.config.AppConst;
import com.hande.goochao.session.AppSession;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.AuthUtils;
import com.hande.goochao.utils.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;

/**
 * Created by Wangem on 2018/1/30.
 */
public class HttpRequest {

    private static void fillHeader(RequestParams requestParams, Map<String, String> header) {
        if (header != null) {
            for (String key : header.keySet()) {
                requestParams.addHeader(key, header.get(key));
            }
        }
        GoochaoApplication application = GoochaoApplication.getApplication();
        if (AppSessionCache.getInstance().isLogin(application)) {
            String token = JsonUtils.getString(AppSessionCache.getInstance().getLoginResult(application), "accessToken", "");
            if (!TextUtils.isEmpty(token)) {
                requestParams.addHeader("access_token", token);
            }
        }

        requestParams.addHeader("from","android");
        String channel = AppSession.getInstance().get(AppConst.CHANNEL);
        requestParams.addHeader("channel", channel);

        // 添加App信息
        requestParams.addHeader("version_name", BuildConfig.VERSION_NAME);
        requestParams.addHeader("version_code", BuildConfig.VERSION_CODE + "");
    }

    private static void fillQueryParams(RequestParams requestParams, Map<String, String> params) {
        if (params != null) {
            for (String key : params.keySet()) {
                requestParams.addQueryStringParameter(key, params.get(key));
            }
        }
    }

    private static void fillParams(RequestParams requestParams, Map<String, String> params) {
        if (params != null) {
            for (String key : params.keySet()) {
                requestParams.addParameter(key, params.get(key));
            }
        }
    }

    private static void fillBodyParams(RequestParams requestParams, Map<String, String> params) {
        if (params != null) {
            for (String key : params.keySet()) {
                requestParams.addBodyParameter(key, params.get(key));
            }
        }
    }

    public static void head(String url, Map<String, String> header, Map<String, String> params, Type type, RequestCallback callback) {
        RequestParams requestParams = new RequestParams(url);
        requestParams.setConnectTimeout(15000);
        requestParams.setReadTimeout(15000);
        fillHeader(requestParams, header);
        fillQueryParams(requestParams, params);
        x.http().request(HttpMethod.HEAD ,requestParams, new InnerCallback(type, callback));
    }

    public static void get(String url, Map<String, String> header, Map<String, String> params, Type type, RequestCallback callback) {
        RequestParams requestParams = new RequestParams(url);
        requestParams.setConnectTimeout(15000);
        requestParams.setReadTimeout(15000);
        fillHeader(requestParams, header);
        fillQueryParams(requestParams, params);
        x.http().get(requestParams, new InnerCallback(type, callback));
    }

    public static void get(String url, Map<String, String> header, Map<String, String> params, JSONObjectCallback callback) {
        get(url, header, params, JSONObject.class, callback);
    }

    public static void post(String url, Map<String, String> header, Map<String, String> params, Type type, RequestCallback callback) {
        RequestParams requestParams = new RequestParams(url);
        fillHeader(requestParams, header);
        fillParams(requestParams, params);
        x.http().post(requestParams, new InnerCallback(type, callback));
    }

    public static void post(String url, Map<String, String> header, Map<String, String> params, JSONObjectCallback callback) {
        post(url, header, params, JSONObject.class, callback);
    }

    public static void postJson(String url, Map<String, String> header, Map<String, Object> params, Type type, RequestCallback callback) {
        RequestParams requestParams = new RequestParams(url);
        requestParams.addHeader("Content-Type", "application/json");
        fillHeader(requestParams, header);
        if (params != null) {
            requestParams.setBodyContent(new Gson().toJson(params));
        }
        x.http().post(requestParams, new InnerCallback(type, callback));
    }

    public static void postJson(String url, Map<String, String> header, Map<String, Object> params, JSONObjectCallback callback) {
        postJson(url, header, params, JSONObject.class, callback);
    }

    public static void postJson(String url, Map<String, String> header, String content, Type type, RequestCallback callback) {
        RequestParams requestParams = new RequestParams(url);
        requestParams.addHeader("Content-Type", "application/json");
        fillHeader(requestParams, header);
        if (!TextUtils.isEmpty(content)) {
            requestParams.setBodyContent(content);
        }
        x.http().post(requestParams, new InnerCallback(type, callback));
    }

    public static void postJson(String url, Map<String, String> header, String content, JSONObjectCallback callback) {
        postJson(url, header, content, JSONObject.class, callback);
    }

    public static void put(String url, Map<String, String> header, Map<String, String> params, Type type, RequestCallback callback) {
        RequestParams requestParams = new RequestParams(url);
        fillHeader(requestParams, header);
        fillParams(requestParams, params);
        x.http().request(HttpMethod.PUT ,requestParams, new InnerCallback(type, callback));
    }

    public static void put(String url, Map<String, String> header, Map<String, String> params, JSONObjectCallback callback) {
        put(url, header, params, JSONObject.class, callback);
    }

    public static void delete(String url, Map<String, String> header, Map<String, String> params, Type type, RequestCallback callback) {
        RequestParams requestParams = new RequestParams(url);
        fillHeader(requestParams, header);
        fillQueryParams(requestParams, params);
        x.http().request(HttpMethod.DELETE ,requestParams, new InnerCallback(type, callback));
    }

    public static void delete(String url, Map<String, String> header, Map<String, String> params, JSONObjectCallback callback) {
        delete(url, header, params, JSONObject.class, callback);
    }


    static class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Date(json.getAsLong());
        }

        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return null;
        }
    }

    static class InnerCallback implements Callback.CacheCallback<String> {

        private Type type;
        private RequestCallback callback;

        public InnerCallback(Type type, RequestCallback callback) {
            this.type = type;
            this.callback = callback;
        }

        @Override
        public boolean onCache(String result) {
            return false;
        }

        @Override
        public void onSuccess(final String result) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (type == null) {
                        callback.onSuccess(result);
                        callback.onComplete(true, result);
                        return;
                    }

                    if (type == JSONObject.class) {
                        try {
                            JSONObject response = new JSONObject(result);
                            if (AuthUtils.validateAuth(response)) {
                                callback.onSuccess(response);
                            }
                            callback.onComplete(true, response);
                        } catch (JSONException e) {
                            callback.onError(e);
                            callback.onComplete(false, null);
                        }
                        return;
                    }

                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(Date.class, new DateTypeAdapter());
                    Gson g = gsonBuilder.create();
                    Object res = g.fromJson(result, type);
                    callback.onSuccess(res);
                    callback.onComplete(true, res);
                }
            }, 1000);
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            callback.onError(ex);
            callback.onComplete(true, new JSONObject());
        }

        @Override
        public void onCancelled(Callback.CancelledException cex) {

        }

        @Override
        public void onFinished() {

        }
    }
}
