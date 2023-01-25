package com.golfingbuddy.ui.mailbox.chat.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by kairat on 2/20/15.
 */
public class ConversationHistory {
    private ArrayList<DailyHistory> mHistories;

    public ConversationHistory() {
        mHistories = new ArrayList<>();
    }

    public ConversationHistory(ArrayList<DailyHistory> histories) {
        mHistories = histories;
    }

    public DailyHistory getDailyHistoryByDate(String date) {
        if (isEmpty()) {
            return null;
        }

        for (DailyHistory dailyHistory : mHistories) {
            if (dailyHistory.getDate().equals(date)) {
                return dailyHistory;
            }
        }

        return null;
    }

    public DailyHistory getHistory(int position) {
        if (isEmpty() || position > mHistories.size() - 1) {
            return null;
        }

        return mHistories.get(position);
    }

    public Boolean addHistory(DailyHistory dailyHistory) {
        return addHistory(-1, dailyHistory);
    }

    public boolean addHistory(int index, DailyHistory dailyHistory) {
        if (isEmpty()) {
            return mHistories.add(dailyHistory);
        }

        if (getDailyHistoryByDate(dailyHistory.getDate()) != null) {
            return false;
        }

        if (index < 0) {
            return mHistories.add(dailyHistory);
        }

        mHistories.add(index, dailyHistory);

        return true;
    }

    public ArrayList<DailyHistory> getHistories() {
        return mHistories;
    }

    public void setHistories(ArrayList<DailyHistory> histories) {
        mHistories = histories;
    }

    public Boolean isEmpty() {
        return mHistories == null || mHistories.isEmpty();
    }

    public void merge(ConversationHistory history) {
        if (history == null || history.isEmpty()) {
            return;
        }

        for (int i = 0, j = history.getHistories().size(); i < j; i++) {
            DailyHistory dailyHistory = history.getHistories().get(i);

            if (!addHistory(i, dailyHistory)) {
                DailyHistory parentHistory = getDailyHistoryByDate(dailyHistory.getDate());

                for (Messages messages : dailyHistory.getMessages()) {
                    for (Messages.UserMessages userMessages : messages.getUserMessages()) {
                        for (int k = 0, l = userMessages.getMessages().size(); k < l; k++) {
                            parentHistory.addMessage(k, userMessages.getMessages().get(k));
                        }
                    }
                }
            }
        }
    }

    public Boolean prependMerge(ConversationHistory history) {
        if (history == null || history.isEmpty()) {
            return false;
        }

        for (DailyHistory dailyHistory : history.getHistories()) {
            if (!addHistory(0, dailyHistory)) {
                DailyHistory parentHistory = getDailyHistoryByDate(dailyHistory.getDate());

                for (Messages messages : dailyHistory.getMessages()) {
                    for (Messages.UserMessages userMessages : messages.getUserMessages()) {
                        for (Messages.Message message : userMessages.getMessages()) {
                            parentHistory.prependMessage(message);
                        }
                    }
                }
            }
        }

        return true;
    }

    public int getHistoryIndex(Messages.Message message) {
        if (message == null || isEmpty()) {
            return 0;
        }

        for (int i = 0, j = mHistories.size(); i < j; i++) {
            DailyHistory dailyHistory = mHistories.get(i);

            if (dailyHistory.getDate().equals(message.getDateLabel())) {
                return i;
            }
        }

        return 0;
    }

    public ConversationHistory.Messages.Message findMessage(ConversationHistory.Messages.Message message) {
        if (message == null) {
            return null;
        }

        for (ConversationHistory.DailyHistory history : mHistories) {
            ConversationHistory.Messages.Message tmpMessage = history.findMessage(message.getId());

            if (tmpMessage != null) {
                return tmpMessage;
            }
        }

        return null;
    }

    public DailyHistory getLastDailyHistory() {
        if (isEmpty()) {
            return null;
        }

        return mHistories.get(mHistories.size() - 1);
    }

    public void clear() {
        mHistories.clear();
    }

    public int size() {
        return mHistories.size();
    }

    public static class DailyHistory {
        private String mDate;
        private ArrayList<Messages> mMessages;

        public DailyHistory(String date) {
            mDate = date;
            mMessages = new ArrayList<>();
        }

        public String getDate() {
            return mDate;
        }

        public void setDate(String date) {
            mDate = date;
        }

        public ArrayList<Messages> getMessages() {
            return mMessages;
        }

        public void setMessages(ArrayList<Messages> messages) {
            mMessages = messages;
        }

        public Boolean isEmpty() {
            return mMessages == null || mMessages.isEmpty();
        }

        public Boolean isRelatives(DailyHistory dailyHistory) {
            return dailyHistory != null && mDate.equals(dailyHistory.getDate());
        }

        public String getDailyDateTime() {
            if (isEmpty()) {
                return null;
            }

            return String.format("%1$s %2$s", getDate(), getFirstMessages().getTime());
        }

        public void merge(DailyHistory dailyHistory) {
            if (!isRelatives(dailyHistory)) {
                return;
            }

            addMessages(dailyHistory.getMessages());
        }

        public void addMessages(ArrayList<Messages> messages) {
            if (messages == null || messages.isEmpty()) {
                return;
            }


            for (Messages _messages : messages) {
                addMessage(_messages.getUserMessages());
            }
        }

        public void addMessage(ArrayList<Messages.UserMessages> messages) {
            if (messages == null || messages.isEmpty()) {
                return;
            }

            for (Messages.UserMessages userMessages : messages) {
                addMessage(userMessages);
            }
        }

        public void addMessage(Messages.UserMessages userMessages) {
            if (userMessages == null || userMessages.isEmpty()) {
                return;
            }

            for (Messages.Message message : userMessages.getMessages()) {
                addMessage(message);
            }
        }

        public void addMessage(Messages.Message message) {
            addMessage(-1, message);
        }

        public void addMessage(int index, Messages.Message message) {
            if (message == null) {
                return;
            }

            if (isEmpty()) {
                Messages.UserMessages userMessages = new Messages.UserMessages(message.getSenderId(), message);
                Messages messages = new Messages(message.getTimeLabel(), userMessages);
                add(index, messages);

                return;
            }

            Messages lastMessage = getLastMessages();

            if (lastMessage.isRelatives(message) && lastMessage.addMessage(index, message)) {
                return;
            }

            Messages.UserMessages userMessages = new Messages.UserMessages(message.getSenderId(), message);
            Messages messages = new Messages(message.getTimeLabel(), userMessages);
            add(index, messages);
        }

        public void prependMessage(Messages.Message message) {
            if (message == null) {
                return;
            }

            Messages firstMessage = getFirstMessages();

            if (firstMessage.isRelatives(message) && firstMessage.addMessage(0, message)) {
                return;
            }

            Messages.UserMessages userMessages = new Messages.UserMessages(message.getSenderId(), message);
            Messages messages = new Messages(message.getTimeLabel(), userMessages);
            add(0, messages);
        }

        private void add(int index, Messages messages) {
            if (index < 0) {
                mMessages.add(messages);
            } else {
                mMessages.add(index, messages);
            }
        }

        public int getBeforeMessageId() {
            return getFirstMessages().getFirstMessages().getFirstMessage().getId();
        }

        public Messages getFirstMessages() {
            if (isEmpty()) {
                return null;
            }

            return mMessages.get(0);
        }

        public Messages getLastMessages() {
            if (isEmpty()) {
                return null;
            }

            return mMessages.get(mMessages.size() - 1);
        }

        public int getLastMessageTimestamp() {
            Messages messages = getLastMessages();

            if (messages != null) {
                Messages.UserMessages userMessages = messages.getLastMessages();

                if (userMessages != null) {
                    return userMessages.getLastMessage().getTimeStamp();
                }
            }

            return 0;
        }

        public Messages.Message findMessage(int messageId) {
            for (Messages messages : mMessages) {
                Messages.Message message = messages.findMessage(messageId);

                if (message != null) {
                    return message;
                }
            }

            return null;
        }
    }

    public static class Messages {
        private String mTime;
        private ArrayList<UserMessages> mUserMessages;

        public Messages(String time, UserMessages userMessages) {
            mTime = time;
            mUserMessages = new ArrayList<>();
            mUserMessages.add(userMessages);
        }

        public Boolean isEmpty() {
            return mUserMessages == null || mUserMessages.isEmpty();
        }

        public Boolean isRelatives(Message message) {
            return message != null && message.getTimeLabel().equals(mTime);
        }

        public Boolean addMessage(Message message) {
            return addMessage(-1, message);
        }

        public Boolean addMessage(int index, Message message) {
            if (!isRelatives(message)) {
                return false;
            }

            UserMessages userMessages = getLastMessages();

            if (!userMessages.isRelatives(message)) {
                addUserMessage(index, message);

                return true;
            }

            return userMessages.addMessage(index, message);
        }

        public Boolean addUserMessage(int index, Message message) {
            if (!isRelatives(message)) {
                return false;
            }

            for (UserMessages messages : mUserMessages) {
                if (messages.hasMessage(message)) {
                    return true;
                }
            }

            UserMessages newUserMessages = new UserMessages(message.getSenderId(), message);

            if (index < 0) {
                return mUserMessages.add(newUserMessages);
            } else {
                mUserMessages.add(index, newUserMessages);

                return true;
            }
        }

        public UserMessages getFirstMessages() {
            if ( isEmpty() ) {
                return null;
            }

            return mUserMessages.get(0);
        }

        public UserMessages getLastMessages() {
            if (isEmpty()) {
                return null;
            }

            return mUserMessages.get(mUserMessages.size() - 1);
        }


        public String getTime() {
            return mTime;
        }

        public void setTime(String time) {
            mTime = time;
        }

        public ArrayList<UserMessages> getUserMessages() {
            return mUserMessages;
        }

        public void setUserMessages(ArrayList<UserMessages> userMessages) {
            mUserMessages = userMessages;
        }

        public Message findMessage(int messageId) {
            for (UserMessages messages : mUserMessages) {
                Message message = messages.findMessage(messageId);

                if (message != null) {
                    return message;
                }
            }

            return null;
        }

        public static class UserMessages {
            private int mSenderId;
            private ArrayList<Message> mMessages;

            public UserMessages(int senderId, Message message) {
                mSenderId = senderId;
                mMessages = new ArrayList<>();
                mMessages.add(message);
            }

            public int getSenderId() {
                return mSenderId;
            }

            public void setSenderId(int senderId) {
                mSenderId = senderId;
            }

            public ArrayList<Message> getMessages() {
                return mMessages;
            }

            public void setMessages(ArrayList<Message> messages) {
                mMessages = messages;
            }

            public Boolean isEmpty() {
                return mMessages == null || mMessages.isEmpty();
            }

            public Boolean hasMessage(Message message) {
                if (message == null) {
                    return false;
                }

                for (Message _message : mMessages) {
                    if (_message.getId() == message.getId()) {
                        return true;
                    }
                }

                return false;
            }

            public Boolean isRelatives(Message message) {
                return message != null && mSenderId == message.getSenderId();
            }

            public Boolean addMessage(int index, Message message) {
                if (!isRelatives(message)) {
                    return false;
                }

                if (hasMessage(message)) {
                    return true;
                }

                if (index < 0) {
                    return mMessages.add(message);
                }

                mMessages.add(index, message);

                return true;
            }

            public Boolean addMessage(Message message) {
                return addMessage(-1, message);
            }

            public Message getFirstMessage() {
                if ( isEmpty() ) {
                    return null;
                }

                return mMessages.get(0);
            }

            public Message getLastMessage() {
                if ( isEmpty() ) {
                    return null;
                }

                return mMessages.get(mMessages.size() - 1);
            }

            public Message findMessage(int messageId) {
                for ( Message message : mMessages ) {
                    if (message.getId() == messageId) {
                        return message;
                    }
                }

                return null;
            }
        }

        public static class Message implements Parcelable {
            @SerializedName("id")
            private int mId;

            @SerializedName("convId")
            private int mConversationId;

            @SerializedName("mode")
            private int mMode;

            @SerializedName("date")
            private String mDate;

            @SerializedName("dateLabel")
            private String mDateLabel;

            @SerializedName("timeStamp")
            private int mTimeStamp;

            @SerializedName("timeLabel")
            private String mTimeLabel;

            @SerializedName("isAuthor")
            private Boolean mIsAuthor;

            @SerializedName("senderId")
            private int mSenderId;

            @SerializedName("recipientRead")
            private Boolean mRecipientRead;

            @SerializedName("isSystem")
            private Boolean mIsSystem;

            @SerializedName("systemType")
            private String mSystemType;

            @SerializedName("readMessageAuthorized")
            private Boolean mReadMessageAuthorized;

            @SerializedName("text")
            private String mText;

            @SerializedName("attachments")
            private ArrayList<Attachment> mAttachments;

            public int getId() {
                return mId;
            }

            public void setId(int id) {
                mId = id;
            }

            public String getStrId() {
                return Integer.toString(mId);
            }

            public int getConversationId() {
                return mConversationId;
            }

            public void setConversationId(int conversationId) {
                mConversationId = conversationId;
            }

            public int getMode() {
                return mMode;
            }

            public void setMode(int mode) {
                mMode = mode;
            }

            public String getDate() {
                return mDate;
            }

            public void setDate(String date) {
                mDate = date;
            }

            public String getDateLabel() {
                return mDateLabel;
            }

            public void setDateLabel(String dateLabel) {
                mDateLabel = dateLabel;
            }

            public int getTimeStamp() {
                return mTimeStamp;
            }

            public void setTimeStamp(int timeStamp) {
                mTimeStamp = timeStamp;
            }

            public String getTimeLabel() {
                return mTimeLabel;
            }

            public void setTimeLabel(String timeLabel) {
                mTimeLabel = timeLabel;
            }

            public String getDisplayDate() {
                return String.format("%s %s", mDateLabel, mTimeLabel);
            }

            public Boolean getIsAuthor() {
                return mIsAuthor != null && mIsAuthor;
            }

            public void setIsAuthor(Boolean isAuthor) {
                mIsAuthor = isAuthor;
            }

            public int getSenderId() {
                return mSenderId;
            }

            public void setSenderId(int senderId) {
                mSenderId = senderId;
            }

            public Boolean getRecipientRead() {
                return mRecipientRead;
            }

            public void setRecipientRead(Boolean recipientRead) {
                mRecipientRead = recipientRead;
            }

            public Boolean getIsSystem() {
                return mIsSystem;
            }

            public void setIsSystem(Boolean isSystem) {
                mIsSystem = isSystem;
            }

            public String getSystemType() {
                return mSystemType;
            }

            public void setSystemType(String systemType) {
                mSystemType = systemType;
            }

            public Boolean getReadMessageAuthorized() {
                return mReadMessageAuthorized;
            }

            public void setReadMessageAuthorized(Boolean readMessageAuthorized) {
                mReadMessageAuthorized = readMessageAuthorized;
            }

            public String getText() {
                return mText;
            }

            public void setText(String text) {
                mText = text;
            }

            public ArrayList<Attachment> getAttachments() {
                return mAttachments;
            }

            public void setAttachments(ArrayList<Attachment> attachment) {
                mAttachments = attachment;
            }

            public Boolean hasAttachments() {
                return mAttachments != null && !mAttachments.isEmpty();
            }

            public void addAttachment(Attachment attachment) {
                if (mAttachments == null) {
                    mAttachments = new ArrayList<>();
                }

                mAttachments.add(attachment);
            }

            public Boolean update(Message message) {
                if (message == null || message.getId() != getId()) {
                    return false;
                }

                for (Field field : getClass().getDeclaredFields()) {
                    try {
                        field.set(this, message.getClass().getDeclaredField(field.getName()).get(message));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }

                return true;
            }

            public static final Creator<Message> CREATOR = new Creator<Message>() {
                @Override
                public Message createFromParcel(Parcel source) {
                    Message conversationItem = new Message();
                    conversationItem.mId = source.readInt();
                    conversationItem.mConversationId = source.readInt();
                    conversationItem.mMode = source.readInt();
                    conversationItem.mDate = source.readString();
                    conversationItem.mDateLabel = source.readString();
                    conversationItem.mTimeStamp = source.readInt();
                    conversationItem.mTimeLabel = source.readString();
                    conversationItem.mIsAuthor = source.readByte() != 0;
                    conversationItem.mSenderId = source.readInt();
                    conversationItem.mRecipientRead = source.readByte() != 0;
                    conversationItem.mIsSystem = source.readByte() != 0;
                    conversationItem.mSystemType = source.readString();
                    conversationItem.mReadMessageAuthorized = source.readByte() != 0;
                    conversationItem.mText = source.readString();
                    conversationItem.mAttachments = (ArrayList<Attachment>) source.readArrayList(Attachment.class.getClassLoader());

                    return conversationItem;
                }

                @Override
                public Message[] newArray(int size) {
                    return new Message[0];
                }
            };

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel parcel, int flags) {
                parcel.writeInt(mId);
                parcel.writeInt(mConversationId);
                parcel.writeInt(mMode);
                parcel.writeString(mDate);
                parcel.writeString(mDateLabel);
                parcel.writeInt(mTimeStamp);
                parcel.writeString(mTimeLabel);
                parcel.writeByte((byte) (mIsAuthor ? 1 : 0));
                parcel.writeInt(mSenderId);
                parcel.writeByte((byte) (mRecipientRead ? 1 : 0));
                parcel.writeByte((byte) (mIsSystem ? 1 : 0));
                parcel.writeString(mSystemType);
                parcel.writeByte((byte) (mReadMessageAuthorized ? 1 : 0));
                parcel.writeString(mText);
                Parcelable[] attachments = parcel.readParcelableArray(Attachment.class.getClassLoader());
                parcel.writeParcelableArray(attachments, flags);
            }

            public static class Attachment implements Parcelable {
                @SerializedName("id")
                private int mId;

                @SerializedName("messageId")
                private int mMessageId;

                @SerializedName("downloadUrl")
                private String mDownloadUrl;

                @SerializedName("fileName")
                private String mFileName;

                @SerializedName("fileSize")
                private int mFileSize;

                @SerializedName("type")
                private String mType;

                public int getId() {
                    return mId;
                }

                public void setId(int id) {
                    mId = id;
                }

                public int getMessageId() {
                    return mMessageId;
                }

                public void setMessageId(int messageId) {
                    mMessageId = messageId;
                }

                public String getDownloadUrl() {
                    return mDownloadUrl;
                }

                public void setDownloadUrl(String downloadUrl) {
                    mDownloadUrl = downloadUrl;
                }

                public String getFileName() {
                    return mFileName;
                }

                public void setFileName(String fileName) {
                    mFileName = fileName;
                }

                public int getFileSize() {
                    return mFileSize;
                }

                public void setFileSize(int fileSize) {
                    mFileSize = fileSize;
                }

                public String getType() {
                    return mType;
                }

                public void setType(String type) {
                    mType = type;
                }

                public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
                    @Override
                    public Attachment createFromParcel(Parcel source) {
                        Attachment attachment = new Attachment();
                        attachment.mId = source.readInt();
                        attachment.mMessageId = source.readInt();
                        attachment.mDownloadUrl = source.readString();
                        attachment.mFileName = source.readString();
                        attachment.mFileSize = source.readInt();
                        attachment.mType = source.readString();

                        return attachment;
                    }

                    @Override
                    public Attachment[] newArray(int size) {
                        return new Attachment[0];
                    }
                };

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel parcel, int flags) {
                    parcel.writeInt(mId);
                    parcel.writeInt(mMessageId);
                    parcel.writeString(mDownloadUrl);
                    parcel.writeString(mFileName);
                    parcel.writeInt(mFileSize);
                    parcel.writeString(mType);
                }
            }
        }
    }
}
