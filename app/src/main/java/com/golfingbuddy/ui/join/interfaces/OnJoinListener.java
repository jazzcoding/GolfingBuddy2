package com.golfingbuddy.ui.join.interfaces;

import com.golfingbuddy.ui.join.classes.JoinUserData;

/**
 * Created by jk on 2/16/16.
 */
public interface OnJoinListener {
    public void onSuccess(JoinUserData data);
    public void onFailed(JoinUserData data);
    public void onError(Exception ex);
}
