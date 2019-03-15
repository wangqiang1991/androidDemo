package com.hande.goochao.utils;

import android.app.Activity;
import android.content.Context;

import com.hande.goochao.GoochaoApplication;
import com.hande.goochao.commons.AppSessionCache;
import com.hande.goochao.commons.EventBusNotification;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.config.AppConst;
import com.hande.goochao.views.activity.LoginActivity;
import com.hande.goochao.views.components.AlertManager;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

/**
 * Created by Wangem on 2018/3/13.
 */

public class AuthUtils {
    public static boolean validateAuth(int code) {
        if (code == 403 || code == 402 || code == 401) {
            return false;
        }
        return true;
    }

    public static boolean validateAuth(JSONObject response) {
        int code = JsonUtils.getCode(response);
        if (!validateAuth(code)) {
            AppSessionCache.getInstance().setLoginResult(null, GoochaoApplication.getApplication());
            EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_re_login);
            EventBus.getDefault().post(notification);
            return false;
        }

        // 服务器拒绝服务
        return !isServerReject(response);
    }

    public static boolean isServerReject(JSONObject response) {
        int code = JsonUtils.getCode(response);
        if (code == 505) {
            EventBusNotification notification = new EventBusNotification(
                    EventBusNotification.event_bus_server_reject,
                    JsonUtils.getString(response, "message", "当前版本过低，请升级最新版本"));
            EventBus.getDefault().post(notification);
            return true;
        }
        return false;
    }
}
