package com.golfingbuddy.ui.mailbox.compose;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.golfingbuddy.ui.mailbox.AttachmentHolderInterface;
import com.golfingbuddy.ui.mailbox.AutoCompleteHolderInterface;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;
import com.golfingbuddy.ui.mailbox.mail.image.MailImage;

/**
 * Created by kairat on 3/18/15.
 */
public interface ComposeView extends AttachmentHolderInterface, AutoCompleteHolderInterface {
    void setLoadingMode(Boolean mode);
    void initActionBar();
    void authorizeAction();
    void terminate();
    void startActivity(Intent intent, int requestCode);
    void showProcessDialog();
    void hideProcessDialog();
    void setRecipient(Recipient recipient);

    void prepareSlotForAttachment(int slotId);
    void destroySlotForAttachment(int slotId);
    void updateSlotForAttachment(int slotId, MailImage.Message.Attachment attachment);
    void deleteSlotForAttachment(MailImage.Message.Attachment attachment);

    void failCreateDirectory();
    void failPermissionDirectory();
    void fileNotFound();
    void successSendFeedback();

    Cursor getCursor(Uri uri);
    Context getContext();

    long getUid();

    void onResponseError(String errorMsg);

    void sendMessage();
    void onSendMessage(ConversationList.ConversationItem conversationItem);
}
