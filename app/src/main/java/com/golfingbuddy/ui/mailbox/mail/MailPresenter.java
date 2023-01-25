package com.golfingbuddy.ui.mailbox.mail;

/**
 * Created by kairat on 3/14/15.
 */
public interface MailPresenter {
    void loadConversation(final int conversationId);
    void unreadConversation(final String conversationId);
    void deleteConversation(final String conversationId);
    void reloadConversation(final int conversationId);
    void authorizeMessageView(final String messageId);
    void winkBack(String messageId);
}
