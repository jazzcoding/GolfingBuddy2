package com.golfingbuddy.ui.matches.classes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kairat on 12/29/14.
 */
public class SlidePhoto implements Parcelable {
    private Boolean mIsAuthorized;
    private Boolean mIsSubscribe;
    private String mAuthorizeMsg;
    private String mPhoto;

    public SlidePhoto() {

    }

    public SlidePhoto(Boolean isAuthorized, Boolean isSubscribe, String photo) {
        mIsAuthorized = isAuthorized;
        mIsSubscribe = isSubscribe;
        mPhoto = photo;
    }

    public Boolean getIsAuthorized() {
        return mIsAuthorized != null && mIsAuthorized;
    }

    public void setIsAuthorized(Boolean mIsAuthorized) {
        this.mIsAuthorized = mIsAuthorized;
    }

    public Boolean getIsSubscribe() {
        return mIsSubscribe != null && mIsSubscribe;
    }

    public void setIsSubscribe(Boolean mIsSubscribe) {
        this.mIsSubscribe = mIsSubscribe;
    }

    public String getAuthorizeMsg() {
        return mAuthorizeMsg;
    }

    public void setAuthorizeMsg(String authorizeMsg) {
        mAuthorizeMsg = authorizeMsg;
    }

    public String getPhoto() {
        return mPhoto;
    }

    public void setPhoto(String mPhoto) {
        this.mPhoto = mPhoto;
    }

    public static final Creator<SlidePhoto> CREATOR = new Creator<SlidePhoto>() {
        @Override
        public SlidePhoto createFromParcel(Parcel parcel) {
            SlidePhoto slidePhoto = new SlidePhoto();
            slidePhoto.mIsAuthorized = parcel.readByte() != 0;
            slidePhoto.mIsSubscribe = parcel.readByte() != 0;
            slidePhoto.mAuthorizeMsg = parcel.readString();
            slidePhoto.mPhoto = parcel.readString();

            return slidePhoto;
        }

        @Override
        public SlidePhoto[] newArray(int i) {
            return new SlidePhoto[0];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte)(getIsAuthorized() ? 1 : 0));
        parcel.writeByte((byte)(getIsSubscribe() ? 1 : 0));
        parcel.writeString(getAuthorizeMsg());
        parcel.writeString(mPhoto);
    }
}
