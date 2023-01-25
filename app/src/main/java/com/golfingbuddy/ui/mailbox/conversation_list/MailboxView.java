package com.golfingbuddy.ui.mailbox.conversation_list;

import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationItemInterface;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;

/**
 * Created by kairat on 4/17/15.
 */
public interface MailboxView {
    void feedback(String msg);

    void setLoadingMode(Boolean mode);

    void setEmptyMode(Boolean mode);

    void setModes(int mode);

    Boolean isSingleMode();

    void setConversationList(ConversationList conversationList);

    void initListView();

    void disableScrollListener();

    void onConversationItemClick(ConversationItemInterface conversationItem);

    void onConversationContextClick(ConversationItemInterface conversationItem);

    void onConversationAvatarClick(ConversationItemInterface conversationItem);

    void updateConversationItem(ConversationList.ConversationItem conversationItem);
}
