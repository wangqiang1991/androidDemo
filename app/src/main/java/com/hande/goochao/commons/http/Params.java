package com.hande.goochao.commons.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Wangem on 2018/1/30.
 */

public class Params {

    public static Map<String, Object> buildForObj(Object... params) {
        if (params.length % 2 != 0) {
            throw new RuntimeException("参数格式不正确, length=" + params.length);
        }
        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i < params.length; i += 2) {
            paramMap.put((String) params[i], params[i + 1]);
        }
        return paramMap;
    }

    public static Map<String, String> buildForStr(String... params) {
        if (params.length % 2 != 0) {
            throw new RuntimeException("参数格式不正确, length=" + params.length);
        }
        Map<String, String> paramMap = new HashMap<>();
        for (int i = 0; i < params.length; i += 2) {
            paramMap.put(params[i], params[i + 1]);
        }
        return paramMap;
    }
}
