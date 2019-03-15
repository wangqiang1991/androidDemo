package com.hande.goochao.commons.webview_handle;

import android.content.Context;

import com.hande.goochao.commons.EventBusNotification;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LMC
 * @description 无
 * Created by LMC on 2018/9/10.
 */
public class ToPlanListInvokeHandler implements InvokeHandler {
    @Override
    public String invokeHandler(String paramsStr, Context context) throws JSONException {
        JSONObject result = new JSONObject();
        EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_banner_to_plan_list);
        EventBus.getDefault().post(notification);
        result.put("code", 0);
        result.put("data", "success");

        return result.toString();
    }
}
