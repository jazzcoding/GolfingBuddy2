package com.golfingbuddy.ui.mailbox.chat;

/**
 * Created by kairat on 3/20/15.
 */
public interface ChatPresenter {

    void getConversationMessages(String conversationId);

    void getRecipient(String recipientId);

    void unreadConversation(String conversationId);

    void deleteConversation(String conversationId);

    void sendMessage(String message);

    void sendTextMessage(String message);

    void sendAttachmentMessage(String path, String type);

    void loadConversationHistory(String opponentId, String beforeId);

    void authorizeMessageView(String messageId);

    void winkBack(String messageId);

}
