package com.hande.goochao.commons.http;

/**
 * Created by Wangem on 2018/1/30.
 */

public class RestfulUrl {

    public static String build(String url, String... params) {
        if (params == null || params.length == 0) {
            return url;
        }
        if (params.length % 2 != 0) {
            throw new RuntimeException("参数格式不正确, length=" + params.length);
        }
        for (int i = 0; i < params.length; i += 2) {
            url = url.replace(params[i], params[i + 1]);
        }
        return url;
    }

}
