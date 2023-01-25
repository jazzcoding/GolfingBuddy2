package com.golfingbuddy.ui.mailbox.conversation_list;

/**
 * Created by kairat on 4/17/15.
 */
interface MailboxModel {
    void getConversationList();

    void getConversationList(int offset);

    void markConversationAsRead(String[] idList);

    void markConversationUnRead(String[] idList);

    void deleteConversation(String[] idList);
}
