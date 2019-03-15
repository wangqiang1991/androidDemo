package com.hande.goochao.commons.webview_handle;

import android.content.Context;

import com.hande.goochao.commons.AppSessionCache;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Wangenmao
 * @description /n
 * Created by Wangenmao on 2018/9/7.
 */

public class HasLoginInvokeHandler implements InvokeHandler {

    @Override
    public String invokeHandler(String paramsStr, Context context) throws JSONException {
        JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(context);
        JSONObject result = new JSONObject();
        result.put("code", 0);
        if (loginResult != null) {
            result.put("data", "true");
        } else {
            result.put("data", "false");
        }
        return result.toString();
    }
}
