package com.hande.goochao.commons.webview_handle;

import android.content.Context;

import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.ZhichiUtils;
import com.hande.goochao.utils.CustomerServiceUtils;
import com.hande.goochao.utils.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/10/10.
 */
public class ToCustomerServiceInvokeHandler implements InvokeHandler {
    @Override
    public String invokeHandler(String paramsStr, Context context) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("code", 0);
        result.put("data", "success");
        ZhichiUtils.startZhichi(context , null);
        return result.toString();
    }
}
