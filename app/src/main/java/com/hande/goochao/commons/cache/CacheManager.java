package com.hande.goochao.commons.cache;

import java.io.File;

/**
 * Created by yanshen on 2015/11/10.
 * 缓存管理对象
 */
public class CacheManager {

    private static File cacheDir;

    private static int appVersion;

    /**
     * 使用缓存之前必须先初始化
     * @param cacheDir
     * @param appVersion
     */
    public static void init(File cacheDir, int appVersion){
        CacheManager.cacheDir = cacheDir;
        CacheManager.appVersion = appVersion;
    }

    /**
     * 获取缓存类
     * @return
     */
    public static AppCache<String, Object> getAppCache(){
        return AppCache.getInstance();
    }

}
