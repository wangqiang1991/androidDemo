package com.hande.goochao.commons.webview_handle;

import android.content.Context;
import android.content.Intent;

import com.hande.goochao.views.activity.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Wangenmao
 * @description /n
 * Created by Wangenmao on 2018/9/7.
 */

public class ToLoginInvokeHandler implements InvokeHandler {
    @Override
    public String invokeHandler(String paramsStr, Context context) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("code", 0);
        result.put("data", "success");
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        return result.toString();
    }
}
