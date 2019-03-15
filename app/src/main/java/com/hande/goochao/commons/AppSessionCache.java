package com.hande.goochao.commons;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.hande.goochao.config.AppConst;
import com.hande.goochao.utils.codec.MD5Utils;
import com.hande.goochao.commons.io.SerializableUtils;
import com.hande.goochao.session.AppSession;
import com.hande.goochao.utils.AppLog;
import com.jakewharton.disklrucache.DiskLruCache;
import org.apache.commons.io.IOUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Created by yanshen on 2015/11/24.
 */
public class AppSessionCache {


    private DiskLruCache cache;
    private AppSession session = AppSession.getInstance();

    private volatile static AppSessionCache instance;


    /**
     * 得到实例对象
     * @return
     */
    public static AppSessionCache getInstance(){
        return instance;
    }

    /**
     * 初始化
     * @param cacheDir
     * @param appVersion
     * @throws IOException
     */
    public static void init(File cacheDir, int appVersion) throws IOException {
        instance = new AppSessionCache(cacheDir, appVersion);
    }

    public AppSessionCache(File cacheDir, int appVersion) throws IOException {
        cache = DiskLruCache.open(cacheDir, appVersion, 1, 1024 * 1024 * 64);
    }


    public void put(int key, Object value) {
        if(value == null) return;

        // save to memory
        session.put(key, value);
        // save to local dir
        putLocalCache(key, value);
    }

    /**
     * 保存到本地
     * @param key
     * @param value
     */
    private void putLocalCache(int key, Object value) {
        if(value instanceof Serializable){
            String md5Key = MD5Utils.stringToMD5("" + key);
            try {
                if(cache.get(md5Key) != null){
                    cache.remove(md5Key);
                }
                DiskLruCache.Editor editor = cache.edit(md5Key);
                if (putStream((Serializable) value, editor)){
                    editor.commit();
                } else {
                    editor.abort();
                }
            } catch (Exception e) {
                AppLog.e("save to local dir", e);
            }
        }
    }

    private boolean putStream(Serializable value, DiskLruCache.Editor editor) {
        OutputStream outputStream = null;
        try {
            outputStream = editor.newOutputStream(0);
            outputStream.write(SerializableUtils.serializer(value));
            return true;
        } catch (IOException e) {
            AppLog.e("save to local dir", e);
            return false;
        } catch (Exception e) {
            AppLog.e("save to local dir", e);
            return false;
        } finally {
            if(outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    AppLog.e("save to local dir", e);
                }
            }
        }
    }

    public <T> T get(int key) {
        T value = session.get(key);
        if(value == null){
            AppLog.i("app session get value is null, key = " + key);
            value = (T) getLocalCache(key);
            if(value != null){
                AppLog.i("put cache, key = " + key);
                session.put(key, value);
            }
        }
        return value;
    }

    /**
     * 从本地获取
     * @param key
     * @return
     */
    private Serializable getLocalCache(int key) {
        String md5Key = MD5Utils.stringToMD5(key + "");
        try {
            if (cache.get(md5Key) != null) {
                // 从DiskLruCahce取
                DiskLruCache.Snapshot snapshot = cache.get(md5Key);
                Serializable serializable = null;
                if (snapshot != null) {
                    serializable = SerializableUtils.unSerializer(IOUtils.toByteArray(snapshot.getInputStream(0)));
                }
                return serializable;
            }
        } catch (IOException e) {
            AppLog.e("get to local dir", e);
        } catch (Exception e) {
            AppLog.e("get to local dir", e);
        }
        return null;
    }

    public void remove(int key) {
        session.remove(key);
        try {
            cache.remove(MD5Utils.stringToMD5(key + ""));
        } catch (IOException e) {
            AppLog.e("remove to local dir", e);
        }
    }

    public boolean has(int key) {
        return get(key) != null;
    }

    /**
     * 从缓存中获取登录信息，优先从内存中获取，如果内存中不存在，那么从磁盘中获取
     * @return
     */
    public JSONObject getLoginResult(Context context) {
        JSONObject loginResult = this.getLoginResultFromMemory();
        if (loginResult == null) {
            SharedPreferences sp = context.getSharedPreferences("LoginResult",Context.MODE_PRIVATE);
            String loginResultStr = sp.getString("LoginResult",null);
            if (!TextUtils.isEmpty(loginResultStr)) {
                try {
                    loginResult = new JSONObject(loginResultStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return loginResult;
    }

    public boolean isLogin(Context context) {
        return getLoginResult(context) != null;
    }

    public void setLoginResult(JSONObject loginResult, Context context) {
        if (loginResult == null) {

            // 发送一个EventBus全局通知
            EventBusNotification notification = new EventBusNotification(EventBusNotification.event_bus_logout);
            EventBus.getDefault().post(notification);
            this.session.remove(AppConst.LOGIN_RESULT_KEY);
            SharedPreferences sp = context.getSharedPreferences("LoginResult",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.remove("LoginResult");
            editor.commit();
            boolean success = editor.commit();
            if (success) {
                AppLog.i("注销用户信息成功");
            }
        } else {
            this.session.put(AppConst.LOGIN_RESULT_KEY,loginResult);
            //保存到SharedPreferences中
            SharedPreferences sp = context.getSharedPreferences("LoginResult",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("LoginResult",loginResult.toString());
            boolean success = editor.commit();
            if (success) {
                AppLog.i("保存用户信息成功");
            }
        }

    }

    /**
     * 功能：从内存中获取LoginResult
     * @return
     */
    public JSONObject getLoginResultFromMemory() {
        return this.session.get(AppConst.LOGIN_RESULT_KEY);
    }

}
