package com.hande.goochao.commons.qiniu;

/**
 * Created by limengyi on 16/5/23.
 */
public class UploadFile {

    private String file;
    private UploadStatus status;
    private String path;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public UploadStatus getStatus() {
        return status;
    }

    public void setStatus(UploadStatus status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
