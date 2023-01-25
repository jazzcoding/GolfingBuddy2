package com.golfingbuddy.ui.mailbox.reply;

import android.content.Intent;
import android.os.Bundle;

import com.golfingbuddy.ui.mailbox.mail.image.MailImage;

/**
 * Created by kairat on 3/18/15.
 */
public interface ReplyModelListener {
    void onAttachAttachment(int requestId, Intent requestIntent, int resultCode, Bundle bundle);
    void onDeleteAttachment(MailImage.Message.Attachment attachment, int requestId, Intent requestIntent, int resultCode, Bundle bundle);
    void onMailSend(int requestId, Intent requestIntent, int resultCode, Bundle bundle);
}
