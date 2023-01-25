package com.golfingbuddy.ui.mailbox.conversation_list;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created by kairat on 4/21/15.
 */
public interface MailboxModelResponseListener {
    void getConversationListResponse(int requestId, Intent requestIntent, int resultCode, Bundle bundle);
}
