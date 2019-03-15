package com.hande.goochao.commons.webview_handle;

import android.content.Context;

import org.json.JSONException;

/**
 * @author Wangenmao
 * @description /n
 * Created by Wangenmao on 2018/9/7.
 */

public interface InvokeHandler {

    public String invokeHandler(String paramsStr, Context context) throws JSONException;
}
