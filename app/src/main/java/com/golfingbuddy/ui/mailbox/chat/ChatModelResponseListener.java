package com.golfingbuddy.ui.mailbox.chat;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created by kairat on 3/20/15.
 */
public interface ChatModelResponseListener {

    void onConversationMessagesLoad(int requestId, Intent requestIntent, int resultCode, Bundle bundle);

    void onSendTextMessage(int requestId, Intent requestIntent, int resultCode, Bundle bundle);

    void onSendAttachmentMessage(int requestId, Intent requestIntent, int resultCode, Bundle bundle);

    void onRecipientReady(int requestId, Intent requestIntent, int resultCode, Bundle bundle);

    void onLoadHistory(int requestId, Intent requestIntent, int resultCode, Bundle bundle);

    void onAuthorizeReadMessage(int requestId, Intent requestIntent, int resultCode, Bundle bundle);

    void onWinkBack(int requestId, Intent requestIntent, int resultCode, Bundle bundle);

}
