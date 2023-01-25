package com.golfingbuddy.ui.mailbox.chat;

import com.golfingbuddy.ui.mailbox.chat.model.Message;

/**
 * Created by kairat on 3/20/15.
 */
public interface ChatModel {

    void getConversationMessages(String conversationId);

    void getRecipientInfo(String recipientId);

    void unreadConversation(String conversationId);

    void deleteConversation(String conversationId);

    void sendTextMessage(Message.ChatMessage message);

    void sendAttachmentMessage(Message.ChatAttachment attachment);

    void loadConversationHistory(String opponentId, String beforeId);

    void authorizeMessageView(String messageId, ChatModelResponseListener listener);

    void winkBack(String messageId, ChatModelResponseListener listener);

}
