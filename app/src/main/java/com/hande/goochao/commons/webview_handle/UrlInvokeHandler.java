package com.hande.goochao.commons.webview_handle;

import android.content.Context;
import android.content.Intent;

import com.hande.goochao.utils.JsonUtils;
import com.hande.goochao.views.activity.SaleGiftActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/10/10.
 */
public class UrlInvokeHandler implements InvokeHandler {
    @Override
    public String invokeHandler(String paramsStr, Context context) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("code", 0);
        result.put("data", "success");
        JSONObject paramsObject = new JSONObject(paramsStr);
        JSONObject value = JsonUtils.getJsonObject(paramsObject,"params",null);
        String url = JsonUtils.getString(value,"url","");
        Intent intent = new Intent(context, SaleGiftActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
        return result.toString();
    }
}
