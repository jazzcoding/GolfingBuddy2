package com.golfingbuddy.ui.mailbox.conversation_list.model;

import com.golfingbuddy.ui.mailbox.RenderInterface;

/**
 * Created by kairat on 2/25/15.
 */
public interface ConversationItemInterface extends RenderInterface {
    ConversationList.ConversationItem getItem();
    void setItem(ConversationList.ConversationItem item);
    void setSelected(Boolean selected);
}
