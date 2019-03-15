package com.hande.goochao.utils;

import com.hande.goochao.BuildConfig;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.JSONObjectCallback;
import com.hande.goochao.config.AppConfig;

import org.json.JSONObject;

/**
 * Created by Wangem on 2018/3/28.
 */
public class UpgradeCheckUtils {

    /**
     * 检查更新
     * @param listener 检查后回调
     */
    public static void check(final UpgradeCheckListener listener) {
        HttpRequest.get(AppConfig.UPGRADE_CHECK + "?v=" + System.currentTimeMillis(), null, null, new JSONObjectCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                int currentVersionCode = JsonUtils.getInt(response, "data.currentVersionCode", 1);
                // 最新版本比当前版本新
                if (currentVersionCode > BuildConfig.VERSION_CODE) {
                    String currentVersionName = JsonUtils.getString(response, "data.currentVersion", "");
                    String downloadUrl = JsonUtils.getString(response, "data.downloadUrl", "");
                    int minVersionCode = JsonUtils.getInt(response, "data.minVersionCode", 1);
                    String desc = JsonUtils.getString(response, "data.desc", "新版本上线啦~");
                    listener.onUpgrade(
                            BuildConfig.VERSION_CODE < minVersionCode,
                            currentVersionName,
                            currentVersionCode,
                            desc,
                            downloadUrl);
                } else {
                    listener.onLastVersion();
                }
            }

            @Override
            public void onError(Throwable ex) {
                listener.onCheckFail(ex);
            }

            @Override
            public void onComplete(boolean success, JSONObject response) {
                listener.onComplete();
            }
        });
    }

    public interface UpgradeCheckListener {
        void onUpgrade(boolean force, String versionName, int versionCode, String upgradeContent, String downloadUrl);
        void onCheckFail(Throwable ex);
        void onLastVersion();
        void onComplete();
    }

}
