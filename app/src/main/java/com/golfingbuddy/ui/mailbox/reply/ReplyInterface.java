package com.golfingbuddy.ui.mailbox.reply;

/**
 * Created by kairat on 3/20/15.
 */
public interface ReplyInterface {
    String getUid();
    String getOpponentId();
    String getConversationId();
    String getText();

    void setUid(String uid);
    void setOpponentId(String opponentId);
    void setConversationId(String conversationId);
    void setText(String text);
}
