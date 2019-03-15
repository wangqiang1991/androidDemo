package com.hande.goochao.commons.qiniu;

/**
 * Created by limengyi on 16/5/23.
 */
public interface UploadListener {

    void success(int index, String path);

    void startUpload(int index);

    void complete();

    void error(int index);
}
