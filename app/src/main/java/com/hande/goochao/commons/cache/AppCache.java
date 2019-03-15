package com.hande.goochao.commons.cache;


import android.os.SystemClock;
import android.support.v4.util.LruCache;

/**
 * 缓存处理工具类
 * Created by yanshen on 2018/3/15.
 * @param <K>
 * @param <V>
 */
public class AppCache<K, V> extends LruCache<K, AppCache.Value> {

    private static AppCache<String, Object> instance;

    public static synchronized AppCache<String, Object> getInstance(){
        if(instance == null){
            /*//获取系统分配给每个应用程序的最大内存，每个应用系统分配32M
            int maxMemory = (int) Runtime.getRuntime().maxMemory();
            int cacheSize = maxMemory / 8;
            //给LruCache分配1/8 4M
            instance = new AppCache<String, Object>(cacheSize);*/
            // 只存储20个对象
            instance = new AppCache<String, Object>(100);
        }
        return instance;
    }
    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public AppCache(int maxSize) {
        super(maxSize);
    }

    /**
     * 过期时间
     * @param key
     * @param value
     * @param expire
     * @return
     */
    public final V putCache(K key, V value, long expire) {
        super.put(key, new Value(value, SystemClock.uptimeMillis() + expire));
        return value;
    }

    public final V getCache(K key) {
        Value value = super.get(key);
        if(value == null){
            return null;
        }
        long current = SystemClock.uptimeMillis();
        if(current > value.expire){
            super.remove(key);
            return null;
        }
        return value.v;
    }

    protected class Value{
        private V v;
        private long expire;

        public Value(V v, long expire) {
            this.v = v;
            this.expire = expire;
        }
    }
}
