package com.golfingbuddy.ui.mailbox.mail.model;

import com.golfingbuddy.ui.mailbox.mail.MailPresenterListener;

/**
 * Created by kairat on 3/14/15.
 */
public interface MailModel {
    void getConversationData(int conversationId, MailPresenterListener listener);

    void unreadConversation(String conversationId, MailPresenterListener listener);

    void deleteConversation(String conversationId, MailPresenterListener listener);

    void authorizeMessageView(String messageId, MailPresenterListener listener);

    void winkBack(String messageId, MailPresenterListener listener);
}
