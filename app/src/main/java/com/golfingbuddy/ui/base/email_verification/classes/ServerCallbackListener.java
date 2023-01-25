package com.golfingbuddy.ui.base.email_verification.classes;

/**
 * Created by jk on 2/19/16.
 */
public interface ServerCallbackListener {
    public void onSuccess();
    public void onFaild();
    public void onError(Exception ex);
}
