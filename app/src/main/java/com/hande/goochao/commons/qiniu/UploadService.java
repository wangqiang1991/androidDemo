package com.hande.goochao.commons.qiniu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.hande.goochao.GoochaoApplication;
import com.hande.goochao.commons.http.HttpRequest;
import com.hande.goochao.commons.http.RequestCallback;
import com.hande.goochao.config.AppConfig;
import com.hande.goochao.utils.AppLog;
import com.hande.goochao.utils.ImageUtils;
import com.hande.goochao.utils.JsonUtils;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by limengyi on 16/5/23.
 */
public class UploadService implements IUploadService {

    private static final Map<String, UploadService> services;

    //上传图片的最大宽高
    private static final int MAX_WIDTH = 1000;
    private static final int MAX_HEIGHT = 1000;

    static {
        services = new HashMap<>();
    }

    private String tokenValue;
    private String domain;
    private List<UploadFile> files;
    private List<UploadListener> callbacks;
    private int uploadIndex;
    private boolean runing;
    private String target;
    // 是否上传原图
    private boolean uploadSourceImage = false;

    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * 获取实例
     * @param tag
     * @return
     */
    public static UploadService getService(String tag) {
        if (services.containsKey(tag)) {
            return services.get(tag);
        } else {
            UploadService service = new UploadService();
            services.put(tag, service);
            return service;
        }

    }

    /**
     * 获取实例
     * @param tag
     * @param target 七牛回调
     * @return
     */
    public static UploadService getService(String tag, String target) {
        if (services.containsKey(tag)) {
            return services.get(tag);
        } else {
            UploadService service = new UploadService();
            service.setTarget(target);
            services.put(tag, service);
            return service;
        }

    }

    public UploadService() {
        files = new ArrayList<>(5);
        callbacks = new ArrayList<>(5);
    }

    @Override
    public void startUpload() {
        tokenValue = null; //每次上传  清空一次token
        if (!this.runing) {
            this.runing = true;
            this.upload(uploadIndex);
        } else {
            AppLog.w("已经启动上传,不能重复启动");
        }
    }

    @Override
    public boolean stopUpload() {
        if (this.runing) {
            this.runing = false;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void addTask(String file) {
        UploadFile uploadFile = new UploadFile();
        uploadFile.setFile(file);
        uploadFile.setStatus(UploadStatus.UploadStatus_Waiting);
        files.add(uploadFile);
    }

    @Override
    public void addCallback(UploadListener listener) {
        callbacks.add(listener);
    }

    @Override
    public void removeCallback(UploadListener listener) {
        callbacks.remove(listener);
    }

    @Override
    public void clearTask() {
        files.clear();
    }

    private void upload(int index) {
        // 已经取消上传
        if (!this.runing) {
            return;
        }
        if (index >= files.size()) {
            this.runing = false;
            uploadIndex = 0;
            callUploadComplete();
        } else {
            uploadQN(index);
        }
    }

    private void uploadQN(int index) {
        if (tokenValue != null) {
            handleUpload(index);
        } else {
            getUpLoadToken(index);
        }
    }

    private void handleUpload(final int index) {
        callStartUpload(index);
        final UploadFile file = files.get(index);
        // 已上传成功不需要继续上传
        if (file.getStatus() == UploadStatus.UploadStatus_Success) {
            uploadNext();
            return;
        }
        file.setStatus(UploadStatus.UploadStatus_Uploading);

        UploadManager uploadManager = GoochaoApplication.uploadManager;

        final Bitmap bitmap = getBitmap(file);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        uploadManager.put(baos.toByteArray(), null, tokenValue,
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info, JSONObject res) {
                        bitmap.recycle();
                        try {
                            baos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //  res 包含hash、key等信息，具体字段取决于上传策略的设置。
                        int statusCode = info.statusCode;
                        if (statusCode == 200) {
                            file.setStatus(UploadStatus.UploadStatus_Success);
                            try {
                                file.setPath(res.getString("key"));
                                callUploadSuccess(index);
                                uploadNext();
                            } catch (JSONException e) {
                                file.setStatus(UploadStatus.UploadStatus_Failure);
                                callUploadError(index);
                                uploadNext();
                            }

                        } else {
                            file.setStatus(UploadStatus.UploadStatus_Failure);
                            callUploadError(index);
                            uploadNext();
                        }
                    }
                }, null);
    }

    private Bitmap getBitmap(UploadFile file) {
        if (uploadSourceImage) {
            try {
                return getSourceBitmap(file.getFile());
            } catch (IOException e) {
                AppLog.e("err", e);
                return ImageUtils.getScaledBitmap(file.getFile(), MAX_WIDTH, MAX_HEIGHT);
            }
        }
        return ImageUtils.getScaledBitmap(file.getFile(), MAX_WIDTH, MAX_HEIGHT);
    }

    private Bitmap getSourceBitmap(String file) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }


    }

    public boolean isRuning() {
        return runing;
    }

    private void uploadNext() {
        uploadIndex++;
        upload(uploadIndex);
    }

    private void getUpLoadToken(final int index) {
        HttpRequest.get(AppConfig.GET_QINIU_TOKEN, null, null, JSONObject.class, new RequestCallback<JSONObject>() {

            @Override
            public void onComplete(boolean success, JSONObject response) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                int code = JsonUtils.getCode(response);
                if (code == 0) {
                    JSONObject tokenObject = JsonUtils.getJsonObject(response, "data", null);
                    if (tokenObject != null) {
                        tokenValue = JsonUtils.getString(tokenObject, "token", "");
                        domain = JsonUtils.getString(tokenObject, "domain", "");
                        handleUpload(index);
                    } else {
                        callUploadError(index);
                    }

                } else {
                    callUploadError(index);
                }
            }

            @Override
            public void onError(Throwable ex) {
                callUploadError(index);
            }
        });
    }

    private void callUploadSuccess(int index) {
        for (UploadListener callback : callbacks) {
            UploadFile file = files.get(index);
            callback.success(index, domain + file.getPath());
        }
    }

    private void callStartUpload(int index) {
        for (UploadListener callback : callbacks) {
            callback.startUpload(index);
        }
    }

    private void callUploadComplete() {
        for (UploadListener callback : callbacks) {
            callback.complete();
            tokenValue = null;
        }
    }

    private void callUploadError(int index) {
        for (UploadListener callback : callbacks) {
            callback.error(index);
        }
    }

    public void setUploadSourceImage(boolean uploadSourceImage) {
        this.uploadSourceImage = uploadSourceImage;
    }

}
