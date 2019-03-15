package com.hande.goochao.commons.http;

import org.json.JSONObject;

/**
 * Created by Wangem on 2018/3/21.
 */

public interface JSONObjectCallback extends RequestCallback<JSONObject> {

    void onSuccess(JSONObject response);

    void onError(Throwable ex);

}
