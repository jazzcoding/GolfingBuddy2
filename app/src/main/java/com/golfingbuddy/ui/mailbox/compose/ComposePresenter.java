package com.golfingbuddy.ui.mailbox.compose;

import android.content.Intent;

import com.golfingbuddy.ui.mailbox.mail.image.MailImage;

import java.util.ArrayList;

/**
 * Created by kairat on 3/18/15.
 */
public interface ComposePresenter {
    void attachmentClick();
    void onActivityResult(int requestCode, int resultCode, Intent data);
    void deleteAttachment(MailImage.Message.Attachment attachment);

    void getRecipient(String recipientId);
    void suggestionListRequest(String constraint, ArrayList<String> idList);
    void sendMessage(ComposeInterface compose);
}
