package com.golfingbuddy.ui.hot_list.classes;

import com.google.gson.annotations.SerializedName;

/**
 * Created by kairat on 2/5/15.
 */
public class HotListItem {
    @SerializedName("userId")
    private int mUserId;

    @SerializedName("avatar")
    private String mAvatar;

    public HotListItem(int userId, String avatar) {
        mUserId = userId;
        mAvatar = avatar;
    }

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int mUserId) {
        if ( mUserId != 0 ) {
            this.mUserId = mUserId;
        }
    }

    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(String avatar) {
        if ( avatar != null ) {
            this.mAvatar = avatar;
        }
    }
}
