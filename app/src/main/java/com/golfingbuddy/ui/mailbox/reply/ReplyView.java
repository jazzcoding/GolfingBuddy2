package com.golfingbuddy.ui.mailbox.reply;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.golfingbuddy.ui.mailbox.AttachmentHolderInterface;
import com.golfingbuddy.ui.mailbox.chat.model.ConversationHistory;
import com.golfingbuddy.ui.mailbox.mail.image.MailImage;

/**
 * Created by kairat on 3/18/15.
 */
public interface ReplyView extends AttachmentHolderInterface {
    void terminate();
    void startActivity(Intent intent, int requestCode);
    void initActionBar();
    void authorizeAction();
    void showPleaseWaite();
    void hidePleaseWaite();

    void prepareSlotForAttachment(int slotId);
    void destroySlotForAttachment(int slotId);
    void updateSlotForAttachment(int slotId, MailImage.Message.Attachment attachment);
    void deleteSlotForAttachment(MailImage.Message.Attachment attachment);

    void failCreateDirectory();
    void failPermissionDirectory();
    void fileNotFound();

    Cursor getCursor(Uri uri);
    Context getContext();

    long getUid();

    void onResponseError(String errorMsg);

    void sendMessage();
    void onSendMessage(ConversationHistory.Messages.Message message);
}
