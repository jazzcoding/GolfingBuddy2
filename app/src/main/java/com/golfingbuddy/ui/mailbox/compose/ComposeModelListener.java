package com.golfingbuddy.ui.mailbox.compose;

import android.content.Intent;
import android.os.Bundle;

import com.golfingbuddy.ui.mailbox.mail.image.MailImage;

/**
 * Created by kairat on 3/18/15.
 */
public interface ComposeModelListener {
    void onAttachAttachment(int requestId, Intent requestIntent, int resultCode, Bundle bundle);
    void onDeleteAttachment(MailImage.Message.Attachment attachment, int requestId, Intent requestIntent, int resultCode, Bundle bundle);
    void onSuggestListResponse(int requestId, Intent requestIntent, int resultCode, Bundle bundle);
    void onMailSend(int requestId, Intent requestIntent, int resultCode, Bundle bundle);
    void onRecipientReady(int requestId, Intent requestIntent, int resultCode, Bundle bundle);
}
