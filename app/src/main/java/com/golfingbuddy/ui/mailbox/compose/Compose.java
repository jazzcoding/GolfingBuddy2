package com.golfingbuddy.ui.mailbox.compose;

/**
 * Created by kairat on 3/20/15.
 */
public class Compose implements ComposeInterface {
    private String mUid;
    private String mOpponentId;
    private String mSubject;
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
    public String getSubject() {
        return mSubject;
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
    public void setSubject(String subject) {
        mSubject = subject;
    }

    @Override
    public void setText(String text) {
        mText = text;
    }
}
