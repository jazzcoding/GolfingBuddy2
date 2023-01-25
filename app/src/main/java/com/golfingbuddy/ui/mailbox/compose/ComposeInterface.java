package com.golfingbuddy.ui.mailbox.compose;

/**
 * Created by kairat on 3/20/15.
 */
public interface ComposeInterface {
    String getUid();
    String getOpponentId();
    String getSubject();
    String getText();

    void setUid(String uid);
    void setOpponentId(String opponentId);
    void setSubject(String subject);
    void setText(String text);
}
