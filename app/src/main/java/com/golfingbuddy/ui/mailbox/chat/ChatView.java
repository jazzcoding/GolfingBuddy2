package com.golfingbuddy.ui.mailbox.chat;

import android.view.View;

import com.golfingbuddy.ui.mailbox.ConversationItemHolder;
import com.golfingbuddy.ui.mailbox.chat.model.ConversationHistory;
import com.golfingbuddy.ui.mailbox.compose.Recipient;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;

/**
 * Created by kairat on 3/20/15.
 */
public interface ChatView extends ConversationItemHolder {
    void feedback(String message);

    void setLoadingMode(Boolean mode);

    void onLoadConversationMessages(ConversationHistory history);

    void setUnreadMessageCount(int count);

    void authorizeAction();

    void initActionBar();

    void onUnreadConversation();

    void onDeleteConversation();

    void sendMessage();

    void onSendTextMessage(ConversationHistory history);

    void showChooseDialog();

    void showSendingIndicator();

    void hideSendingIndicator();

    ConversationList.ConversationItem getConversationItem();

    void setConversationItem(ConversationList.ConversationItem item);

    void setRecipient(Recipient recipient);

    void onAttachmentClick(ConversationHistory.Messages.Message.Attachment attachment);

    void onAuthorizeActionClick(ConversationHistory.Messages.Message message, View anchor);

    void onAuthorizeResponse(ConversationHistory.Messages.Message message);

    void onLoadConversationNewMessages(final ConversationHistory history);

    void onLoadConversationHistory(final ConversationHistory history);

    void onWinkBackClick(ConversationHistory.Messages.Message message);

    void onWinkBackResponse(ConversationHistory.Messages.Message message);
}
