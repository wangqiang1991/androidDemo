package com.hande.goochao.commons.http;

/**
 * Created by Wangem on 2018/1/30.
 */

public interface RequestCallback<T> {

    void onSuccess(T response);

    void onError(Throwable ex);

    void onComplete(boolean success, T response);

}
