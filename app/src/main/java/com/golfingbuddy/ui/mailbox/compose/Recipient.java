package com.golfingbuddy.ui.mailbox.compose;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kairat on 3/20/15.
 */
public class Recipient implements RecipientInterface, Parcelable {

    private String mOpponentId;
    private String mDisplayName;
    private String mAvatarUrl;

    @Override
    public String getOpponentId() {
        return mOpponentId;
    }

    @Override
    public String getDisplayName() {
        return mDisplayName;
    }

    @Override
    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    @Override
    public void setOpponentId(String opponentId) {
        mOpponentId = opponentId;
    }

    @Override
    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    @Override
    public void setAvatarUrl(String avatarUrl) {
        mAvatarUrl = avatarUrl;
    }

    public static final Parcelable.Creator<Recipient> CREATOR = new Parcelable.Creator<Recipient>() {
        @Override
        public Recipient createFromParcel(Parcel source) {
            Recipient recipient = new Recipient();
            recipient.mOpponentId = source.readString();
            recipient.mDisplayName = source.readString();
            recipient.mAvatarUrl = source.readString();

            return recipient;
        }

        @Override
        public Recipient[] newArray(int size) {
            return new Recipient[0];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mOpponentId);
        parcel.writeString(mDisplayName);
        parcel.writeString(mAvatarUrl);
    }
}
