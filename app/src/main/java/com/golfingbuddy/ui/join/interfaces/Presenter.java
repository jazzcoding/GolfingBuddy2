package com.golfingbuddy.ui.join.interfaces;

import android.net.Uri;

import java.util.HashMap;

/**
 * Created by jk on 1/21/16.
 */
public interface Presenter {
    public void onResume();
    public void onDestroy();
    public void onNextClicked(HashMap<String, String> data);
    public void validate(HashMap<String, String> data);
    public void validate(HashMap<String, String> data, boolean validateAll);
    public void onBackClicked(HashMap<String, String> data);
    public void uploadTmpAvatar(Uri uri, OnTmpAvatarUploaded listener);
}
