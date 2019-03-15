package com.hande.goochao.commons.webview_handle;

import android.content.Context;
import android.content.Intent;


import com.hande.goochao.views.activity.CouponsActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/9/10.
 */
public class ToCouponInvokeHandler implements InvokeHandler {
    @Override
    public String invokeHandler(String paramsStr, Context context) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("code", 0);
        result.put("data", "success");
        Intent intent = new Intent(context, CouponsActivity.class);
        context.startActivity(intent);
        return result.toString();
    }
}
