package com.hande.goochao.commons.qiniu;

/**
 * Created by limengyi on 16/5/23.
 */
public interface IUploadService {

    /***
     * **
     * 开始上传
     */
    void startUpload();

    /**
     * 停止上传
     */
    boolean stopUpload();

    /**
     * 添加任务
     *
     * @param file
     */
    void addTask(String file);

    /**
     * 添加回调
     * @param listener
     */
    void addCallback(UploadListener listener);

    /**
     * 删除回调
     * @param listener
     */
    void removeCallback(UploadListener listener);


    void clearTask();


}
