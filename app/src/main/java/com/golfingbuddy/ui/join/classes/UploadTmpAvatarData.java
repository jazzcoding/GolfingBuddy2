package com.golfingbuddy.ui.join.classes;

/**
 * Created by jk on 2/10/16.
 */
public class UploadTmpAvatarData {
    String result;
    String error;
    String fileKey;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getMessage() {
        return error;
    }

    public void setMessage(String message) {
        this.error = message;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
