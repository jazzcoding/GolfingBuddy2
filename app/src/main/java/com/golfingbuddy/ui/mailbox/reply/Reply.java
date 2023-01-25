package com.golfingbuddy.ui.mailbox.reply;

/**
 * Created by kairat on 3/20/15.
 */
public class Reply implements ReplyInterface {
    private String mUid;
    private String mOpponentId;
    private String mConversationId;
    private String mText;

    @Override
    public String getUid() {
        return mUid;
    }

    @Override
    public String getOpponentId() {
        return mOpponentId;
    }

    @Override
    public String getConversationId() {
        return mConversationId;
    }

    @Override
    public String getText() {
        return mText;
    }

    @Override
    public void setUid(String uid) {
        mUid = uid;
    }

    @Override
    public void setOpponentId(String opponentId) {
        mOpponentId = opponentId;
    }

    @Override
    public void setConversationId(String conversationId) {
        mConversationId = conversationId;
    }

    @Override
    public void setText(String text) {
        mText = text;
    }
}
