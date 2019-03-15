package com.hande.goochao.utils;


import android.text.TextUtils;
import android.widget.TextView;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by Wangem on 16/5/23.
 */
public class JsonUtils {


    /**
     * 功能:获取时间字符串
     *
     * @param jsonObject
     * @param key
     * @param defaultValue
     * @param format
     * @return
     */
    public static String getDateStr(JSONObject jsonObject, String key, String defaultValue, String format) {
        try {
            if (key.indexOf(".") != -1) {
                String keyAfter = key.substring(0, key.lastIndexOf("."));
                jsonObject = getJsonObject(jsonObject, keyAfter, null);
                key = key.substring(key.lastIndexOf(".") + 1);
            }

            String value = jsonObject.getString(key);
            if (value != null && !value.equals("")) {
                return DateUtils.timeStampToStr(Long.parseLong(value), format);
            } else {
                return defaultValue;
            }

        } catch (JSONException e) {
            AppLog.e("err", e);
        }
        return defaultValue;
    }


    /**
     * 功能:获取JsonObject key-value
     *
     * @param jsonObject
     * @param key
     * @return
     */
    public static String getString(JSONObject jsonObject, String key, String defaultValue) {
        try {
            if (key.indexOf(".") != -1) {
                String keyAfter = key.substring(0, key.lastIndexOf("."));
                jsonObject = getJsonObject(jsonObject, keyAfter, null);
                key = key.substring(key.lastIndexOf(".") + 1);
            }

            if (jsonObject == null) {
                return defaultValue;
            }

            if (!jsonObject.has(key)) {
                return defaultValue;
            }
            String value = jsonObject.getString(key);
            if (value == null || value.trim().equals("") || value.trim().equals("null") || value.trim().equals("<null>")) {
                return defaultValue;
            }
            return value;
        } catch (JSONException e) {
            AppLog.e("err", e);
        }
        return defaultValue;
    }

    /**
     * 功能:从JsonObject中获取int类型的值
     *
     * @param jsonObject
     * @param key
     * @param defaultValue
     * @return
     */
    public static int getInt(JSONObject jsonObject, String key, int defaultValue) {
        try {
            if (key.indexOf(".") != -1) {
                String keyAfter = key.substring(0, key.lastIndexOf("."));
                jsonObject = getJsonObject(jsonObject, keyAfter, null);
                key = key.substring(key.lastIndexOf(".") + 1);
            }

            if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                int value = jsonObject.getInt(key);
                return value;
            }
            return defaultValue;
        } catch (JSONException e) {
            AppLog.e("err", e);
        }
        return defaultValue;
    }


    public static long getLong(JSONObject jsonObject, String key, long defaultValue) {
        try {
            if (key.indexOf(".") != -1) {
                String keyAfter = key.substring(0, key.lastIndexOf("."));
                jsonObject = getJsonObject(jsonObject, keyAfter, null);
                key = key.substring(key.lastIndexOf(".") + 1);
            }

            if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                long value = jsonObject.getLong(key);
                return value;
            }
            return defaultValue;
        } catch (JSONException e) {
            AppLog.e("err", e);
        }
        return defaultValue;
    }


    /**
     * 功能:从JsonObject中获取int类型的值
     *
     * @param jsonObject
     * @param key
     * @param defaultValue
     * @return
     */
    public static double getDouble(JSONObject jsonObject, String key, int defaultValue) {
        try {
            if (key.indexOf(".") != -1) {
                String keyAfter = key.substring(0, key.lastIndexOf("."));
                jsonObject = getJsonObject(jsonObject, keyAfter, null);
                key = key.substring(key.lastIndexOf(".") + 1);
            }

            if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                double value = jsonObject.getDouble(key);
                return value;
            }
            return defaultValue;
        } catch (JSONException e) {
            AppLog.e("err", e);
        }
        return defaultValue;
    }


    /**
     * 功能:从JSONObject中获取boolean类型的值
     *
     * @param jsonObject
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean getBoolean(JSONObject jsonObject, String key, boolean defaultValue) {
        try {
            if (key.indexOf(".") != -1) {
                String keyAfter = key.substring(0, key.lastIndexOf("."));
                jsonObject = getJsonObject(jsonObject, keyAfter, null);
                key = key.substring(key.lastIndexOf(".") + 1);
            }

            if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                boolean value = jsonObject.getBoolean(key);
                return value;
            } else {
                return defaultValue;
            }
        } catch (JSONException e) {
            AppLog.e("err", e);
        }
        return false;
    }

    public static JSONArray getJsonArray(JSONObject jsonObject, String key, JSONArray defaultValue) {
        try {
            if (key.indexOf(".") != -1) {
                String keyAfter = key.substring(0, key.lastIndexOf("."));
                jsonObject = getJsonObject(jsonObject, keyAfter, null);
                key = key.substring(key.lastIndexOf(".") + 1);
            }

            if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                return jsonObject.getJSONArray(key);
            }
            return defaultValue;
        } catch (JSONException e) {
            AppLog.e("err", e);
        }
        return defaultValue;
    }

    public static JSONObject getJsonItem(JSONArray jsonArray, int index, JSONObject defaultValue) {
        if (index >= jsonArray.length()) {
            return defaultValue;
        }
        try {
            return jsonArray.getJSONObject(index);
        } catch (JSONException e) {
            AppLog.e("err", e);
        }
        return defaultValue;
    }

    public static void put(JSONObject jsonObject, String key, Object value) {
        if (jsonObject == null || TextUtils.isEmpty(key)) {
            return;
        }
        try {
            if (key.indexOf(".") != -1) {
                String keyAfter = key.substring(0, key.lastIndexOf("."));
                jsonObject = getJsonObject(jsonObject, keyAfter, null);
                key = key.substring(key.lastIndexOf(".") + 1);
            }

            jsonObject.put(key, value);
        } catch (JSONException e) {
            AppLog.e("err", e);
        }
    }

    public static int getCode(JSONObject jsonObject) {
        return getInt(jsonObject, "code", -1);
    }

    public static String getResponseMessage(JSONObject resultObject) {
        return getString(resultObject,"message","服务器繁忙");
    }

    public static void appendArray(JSONArray source, JSONArray array) {
        if (array == null || source == null) {
            return;
        }

        for (int i = 0; i < array.length(); i++) {
            try {
                source.put(array.get(i));
            } catch (JSONException e) {
                AppLog.e("err", e);
            }
        }
    }

    public static void appendItem(JSONArray source, Object value) {
        if (value == null || source == null) {
            return;
        }
        source.put(value);
    }

    public static JSONObject getJsonObject(JSONObject jsonObject, String key, JSONObject defaultValue) {
        try {
            if (jsonObject == null || key == null) {
                return defaultValue;
            }

            if (key.indexOf(".") != -1) {
                String[] keyArr = key.split(".");
                for (int i = 0; i < keyArr.length - 1; i++) {
                    jsonObject = getJsonObject(jsonObject, keyArr[i], null);
                    if (jsonObject == null) {
                        return defaultValue;
                    }
                }
            }

            if (!jsonObject.has(key)) {
                return defaultValue;
            }

            if (!jsonObject.has(key) || jsonObject.isNull(key)) {
                return defaultValue;
            }

            JSONObject value = jsonObject.getJSONObject(key);
            if (value == null) {
                return defaultValue;
            }
            return value;
        } catch (JSONException e) {
            AppLog.e("err", e);
            return defaultValue;
        }
    }

    public static JSONObject newJsonObject(String json, JSONObject defaultValue) {
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            AppLog.e("err", e);
            return defaultValue;
        }
    }

    public static boolean isEmpty(JSONObject jsonObject, String key) {

        if (jsonObject == null || key == null) {
            return true;
        }

        if (key.indexOf(".") != -1) {
            String[] keyArr = key.split(".");
            for (int i = 0; i < keyArr.length - 1; i++) {
                jsonObject = getJsonObject(jsonObject, keyArr[i], null);
                if (jsonObject == null) {
                    return true;
                }
            }
        }

        if (jsonObject.isNull(key)) {
            return true;
        }
        try {
            Object value = jsonObject.get(key);
            if (value instanceof String) {
                return StringUtils.isEmpty((String) value);
            }
            return value == null;
        } catch (JSONException e) {
            AppLog.e("err", e);
            return true;
        }
    }

    public static void insertItem(JSONArray source, int index, Object value) {
        if (value == null || source == null) {
            return;
        }
        try {
            for (int i = source.length() - 1; i >= 0; i--) {
                source.put(i + 1, source.get(i));
            }
            source.put(index, value);
        } catch (JSONException e) {
            AppLog.e("err", e);
        }
    }

    public static String getStringItem(JSONArray jsonArray, int index, String defaultValue) {
        if (index >= jsonArray.length()) {
            return defaultValue;
        }
        try {
            return jsonArray.getString(index);
        } catch (JSONException e) {
            AppLog.e("err", e);
        }
        return defaultValue;
    }

    public static JSONArray newJsonArray(List<JSONObject> pictureList) {
        JSONArray array = new JSONArray();
        if (pictureList != null) {
            for (JSONObject object : pictureList) {
                JsonUtils.appendItem(array, object);
            }
        }
        return array;
    }
}
