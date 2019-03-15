package com.hande.goochao.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;


/**
 * Created by yanshen on 2015/11/10.
 */
public class ContextUtils {
    /**
     * 获取缓存目录
     * @param dir
     * @return
     */
    public static String getCacheDir(String dir){
        if(TextUtils.isEmpty(dir)){
            return dir;
        }
        if(!dir.startsWith("/")){
            dir = "/" + dir;
        }
        return Environment.getExternalStorageDirectory() + dir;
    }

    /**
     * 相对sdcard/Android/data/{package}/cache/obj/
     * @param context
     * @param dir
     * @return
     */
    public static String getExternalCacheDir(Context context, String dir){
        if(TextUtils.isEmpty(dir)){
            return dir;
        }
        if(!dir.startsWith("/")){
            dir = "/" + dir;
        }
        return context.getExternalFilesDir(null) + dir;
    }

}

