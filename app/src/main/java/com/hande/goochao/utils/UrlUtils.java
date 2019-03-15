package com.hande.goochao.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by yanshen on 2015/10/30.
 */
public class UrlUtils {

    /**
     * 将Map转换为Url参数字符串
     * @param params
     * @param paramsEncoding
     * @return
     */
    public static String parseParams(Map<String, String> params, String paramsEncoding){
        return encodeParameters(params, paramsEncoding);
    }

    private static String encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }
}
