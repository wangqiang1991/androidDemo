package com.hande.goochao.commons.webview_handle;

import android.content.Context;

import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.utils.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/9/10.
 */
public class GetLoginResultInvokeHandler implements InvokeHandler {
    @Override
    public String invokeHandler(String paramsStr, Context context) throws JSONException {
        JSONObject result = new JSONObject();
        JSONObject loginResult = AppSessionCache.getInstance().getLoginResult(context);
        if (loginResult != null){
           result.put("data" ,loginResult.toString());
           result.put("code" , 0);
        }else {
            result.put("errMsg" , "该版本暂不支持此操作，请升级至最新版本");
            result.put("code" , -1);
        }
        return result.toString();
    }
}
