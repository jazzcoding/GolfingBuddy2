package com.golfingbuddy.ui.mailbox.conversation_list.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.golfingbuddy.ui.mailbox.Constants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by kairat on 2/13/15.
 */
public class ConversationList {
    private ArrayList<ConversationItem> mList;

    public ConversationList() {
        mList = new ArrayList<>();
    }

    public Boolean addAll(ArrayList<ConversationItem> list) {
        return !(list == null || list.isEmpty()) && mList.addAll(list);
    }

    public ArrayList<ConversationItem> getList() {
        return mList;
    }

    public void setList(ArrayList<ConversationItem> list) {
        if ( list != null ) {
            mList = list;
        }
    }

    public void addOrUpdateConversationItem(ConversationItem item) {
        if (item == null) {
            return;
        }

        for (ConversationItem tmpItem : mList) {
            if (tmpItem.getConversationId() == item.getConversationId()) {
                tmpItem.update(item);

                return;
            }
        }

        mList.add(item);
    }

    public void merge(ConversationList conversationList) {
        Collections.sort(conversationList.getList());
    }

    public ConversationItem findConversation(ConversationItem conversationItem) {
        if ( conversationItem == null || isEmpty() ) {
            return null;
        }

        for ( ConversationItem item : mList ) {
            if ( item.getConversationId() == conversationItem.getConversationId() ) {
                return item;
            }
        }

        return null;
    }

    public Boolean isEmpty() {
        return mList == null || mList.isEmpty();
    }

    public int size() {
        return mList.size();
    }

    public static class ConversationItem implements Parcelable, Comparable {
        @SerializedName("conversationId")
        private int mId;

        @SerializedName("conversationViewed")
        private Boolean mIsViewed;

        @SerializedName("conversationRead")
        private Boolean mIsRead;

        @SerializedName("lastMessageTimestamp")
        private int mLastMsgTimestamp;

        @SerializedName("date")
        private String mDate;

        @SerializedName("hasAttachment")
        private Boolean mHasAttachment;

        @SerializedName("mode")
        private int mMode;

        @SerializedName("newMessageCount")
        private int mNewMsgCount;

        @SerializedName("opponentId")
        private int mOpponentId;

        @SerializedName("reply")
        private Boolean mIsReply;

        @SerializedName("displayName")
        private String mDisplayName;

        @SerializedName("avatarUrl")
        private String mAvatarUrl;

        @SerializedName("isOnline")
        private Boolean mIsOnline;

        @SerializedName("isDeleted")
        private Boolean mIsDeleted;

        @SerializedName("subject")
        private String mSubject;

        @SerializedName("previewText")
        private String mPreviewText;

        @SerializedName("wink")
        private Boolean mIsWink;

        public Boolean isMailMode() {
            return (mMode & Constants.MODE_MAIL) > 0;
        }

        public Boolean isChatMode() {
            return (mMode & Constants.MODE_CHAT) > 0;
        }

        public Boolean update(ConversationItem conversationItem) {
            if ( conversationItem == null || conversationItem.getConversationId() != getConversationId() ) {
                return false;
            }

            for ( Field field : getClass().getDeclaredFields() ) {
                try {
                    field.set(this, conversationItem.getClass().getDeclaredField(field.getName()).get(conversationItem));
                } catch ( IllegalAccessException e ) {
                    e.printStackTrace();
                } catch ( NoSuchFieldException e ) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        public void markAsRead() {
            mIsRead = true;
        }

        public void markUnRead() {
            mIsRead = false;
        }

        public int getConversationId() {
            return mId;
        }

        public void setConversationId(int conversationId) {
            mId = conversationId;
        }

        public String getStrConversationId() {
            return Integer.toString(mId);
        }

        public Boolean getIsViewed() {
            return mIsViewed != null && mIsViewed;
        }

        public void setIsViewed(Boolean isViewed) {
            mIsViewed = isViewed;
        }

        public Boolean getIsRead() {
            return mIsRead != null && mIsRead;
        }

        public void setIsRead(Boolean isRead) {
            mIsRead = isRead;
        }

        public int getLastMsgTimestamp() {
            return mLastMsgTimestamp;
        }

        public void setLastMsgTimestamp(int lastMsgTimestamp) {
            mLastMsgTimestamp = lastMsgTimestamp;
        }

        public String getDate() {
            return mDate;
        }

        public void setDate(String date) {
            mDate = date;
        }

        public Boolean getHasAttachment() {
            return mHasAttachment != null && mHasAttachment;
        }

        public void setHasAttachment(Boolean hasAttachment) {
            mHasAttachment = hasAttachment;
        }

        public int getMode() {
            return mMode;
        }

        public void setMode(int mode) {
            mMode = mode;
        }

        public int getNewMsgCount() {
            return mNewMsgCount;
        }

        public void setNewMsgCount(int newMsgCount) {
            mNewMsgCount = newMsgCount;
        }

        public int getOpponentId() {
            return mOpponentId;
        }

        public String getStringOpponentId() {
            return Integer.toString(mOpponentId);
        }

        public void setOpponentId(int opponentId) {
            mOpponentId = opponentId;
        }

        public Boolean getIsReply() {
            return mIsReply != null && mIsReply;
        }

        public void setIsReply(Boolean isReply) {
            mIsReply = isReply;
        }

        public String getDisplayName() {
            return mDisplayName;
        }

        public void setDisplayName(String displayName) {
            mDisplayName = displayName;
        }

        public String getAvatarUrl() {
            return mAvatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            mAvatarUrl = avatarUrl;
        }

        public Boolean getIsOnline() {
            return mIsOnline != null && mIsOnline;
        }

        public void setIsOnline(Boolean isOnline) {
            mIsOnline = isOnline;
        }

        public Boolean getIsDeleted() {
            return mIsDeleted != null && mIsDeleted;
        }

        public void setIsDeleted(Boolean isDeleted) {
            mIsDeleted = isDeleted;
        }

        public String getSubject() {
            return mSubject;
        }

        public void setSubject(String subject) {
            mSubject = subject;
        }

        public String getPreviewText() {
            return mPreviewText;
        }

        public void setPreviewText(String text) {
            mPreviewText = text;
        }

        public Boolean getIsWink() {
            return mIsWink != null && mIsWink;
        }

        public void setIsWink(Boolean isWink) {
            mIsWink = isWink;
        }

        public static final Creator<ConversationItem> CREATOR = new Creator<ConversationItem>() {
            @Override
            public ConversationItem createFromParcel(Parcel source) {
                ConversationItem conversationItem = new ConversationItem();
                conversationItem.mId = source.readInt();
                conversationItem.mIsViewed = source.readByte() != 0;
                conversationItem.mIsRead = source.readByte() != 0;
                conversationItem.mLastMsgTimestamp = source.readInt();
                conversationItem.mDate = source.readString();
                conversationItem.mHasAttachment = source.readByte() != 0;
                conversationItem.mMode = source.readInt();
                conversationItem.mNewMsgCount = source.readInt();
                conversationItem.mOpponentId = source.readInt();
                conversationItem.mIsReply = source.readByte() != 0;
                conversationItem.mDisplayName = source.readString();
                conversationItem.mAvatarUrl = source.readString();
                conversationItem.mIsOnline = source.readByte() != 0;
                conversationItem.mIsDeleted = source.readByte() != 0;
                conversationItem.mSubject = source.readString();
                conversationItem.mPreviewText = source.readString();
                conversationItem.mIsWink = source.readByte() != 0;

                return conversationItem;
            }

            @Override
            public ConversationItem[] newArray(int size) {
                return new ConversationItem[0];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(mId);
            parcel.writeByte((byte) (getIsViewed() ? 1 : 0));
            parcel.writeByte((byte)(getIsRead() ? 1 : 0));
            parcel.writeInt(mLastMsgTimestamp);
            parcel.writeString(mDate);
            parcel.writeByte((byte) (getHasAttachment() ? 1 : 0));
            parcel.writeInt(mMode);
            parcel.writeInt(mNewMsgCount);
            parcel.writeInt(mOpponentId);
            parcel.writeByte((byte)(getIsReply() ? 1 : 0));
            parcel.writeString(mDisplayName);
            parcel.writeString(mAvatarUrl);
            parcel.writeByte((byte)(getIsOnline() ? 1 : 0));
            parcel.writeByte((byte)(getIsDeleted() ? 1 : 0));
            parcel.writeString(mSubject);
            parcel.writeString(mPreviewText);
            parcel.writeByte((byte)(getIsWink() ? 1 : 0));
        }

        @Override
        public int compareTo(Object conversationItem) {
            ConversationItem item = (ConversationItem)conversationItem;

            return getLastMsgTimestamp() < item.getLastMsgTimestamp() ? 1 : -1;
        }
    }
}
