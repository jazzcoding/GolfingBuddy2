package com.golfingbuddy.ui.mailbox.reply;

import android.content.Intent;

import com.golfingbuddy.ui.mailbox.mail.image.MailImage;

/**
 * Created by kairat on 3/18/15.
 */
public interface ReplyPresenter {
    void attachmentClick();
    void onActivityResult(int requestCode, int resultCode, Intent data);
    void deleteAttachment(MailImage.Message.Attachment attachment);

    void sendMessage(ReplyInterface compose);
}
