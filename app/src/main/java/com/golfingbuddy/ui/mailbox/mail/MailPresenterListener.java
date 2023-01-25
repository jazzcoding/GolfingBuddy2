package com.golfingbuddy.ui.mailbox.mail;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created by kairat on 3/14/15.
 */
public interface MailPresenterListener {
    void onLoadConversationData(Bundle bundle);
    void onUnreadConversation(int requestId, Intent requestIntent, int resultCode, Bundle bundle);
    void onDeleteConversation(int requestId, Intent requestIntent, int resultCode, Bundle bundle);
    void onAuthorizeReadMessage(int requestId, Intent requestIntent, int resultCode, Bundle bundle);
    void onWinkBack(int requestId, Intent requestIntent, int resultCode, Bundle bundle);
}
