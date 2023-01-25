package com.golfingbuddy.ui.join.interfaces;

import android.net.Uri;

import java.util.HashMap;

/**
 * Created by jk on 1/20/16.
 */
public interface UserModel {
    public int getStep();
    public void setStep(int step);
    public int getGender();
    public HashMap<String, String> getData();
    public HashMap<String, String> getData(int step);
    public void setData(HashMap<String, String> data);
    public void save(OnJoinListener listener);
    public void saveTmpAvatar(Uri uri, OnTmpAvatarUploaded listener);
    public void clean();
}
