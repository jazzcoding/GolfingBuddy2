package com.golfingbuddy.ui.mailbox.chat.model;

/**
 * Created by kairat on 3/12/15.
 */
public class Message {
    protected String mOpponentId;
    protected String mMode;

    public void setOpponentId(String opponentId) {
        mOpponentId = opponentId;
    }

    public String getOpponentId() {
        return mOpponentId;
    }

    public void setMode(String mode) {
        mMode = mode;
    }

    public String getMode() {
        return mMode;
    }

    public static class ChatMessage extends Message {
        protected String mConversationId;
        protected String mText;
        protected String mLastMessageTimestamp;

        public String getConversationId() {
            return mConversationId;
        }

        public void setConversationId(String conversationId) {
            mConversationId = conversationId;
        }

        public String getText() {
            return mText;
        }

        public void setText(String text) {
            mText = text;
        }

        public String getLastMessageTimestamp() {
            return mLastMessageTimestamp;
        }

        public void setLastMessageTimestamp(String lastMessageTimestamp) {
            mLastMessageTimestamp = lastMessageTimestamp;
        }
    }

    public static class ChatAttachment extends Message {
        protected String mLastMessageTimestamp;
        protected String mFile;
        protected String mType;

        public String getLastMessageTimestamp() {
            return mLastMessageTimestamp;
        }

        public void setLastMessageTimestamp(String lastMessageTimestamp) {
            mLastMessageTimestamp = lastMessageTimestamp;
        }

        public String getFile() {
            return mFile;
        }

        public void setFile(String file) {
            mFile = file;
        }

        public String getType() {
            return mType;
        }

        public void setType(String type) {
            mType = type;
        }
    }
}
