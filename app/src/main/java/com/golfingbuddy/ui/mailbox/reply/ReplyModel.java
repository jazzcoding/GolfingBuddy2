package com.golfingbuddy.ui.mailbox.reply;

import com.golfingbuddy.ui.mailbox.mail.image.MailImage;

/**
 * Created by kairat on 3/18/15.
 */
public interface ReplyModel {
    int attachAttachment(long uid, String type, String path, ReplyModelListener listener);
    void deleteAttachment(MailImage.Message.Attachment attachment, ReplyModelListener listener);
    void sendMessage(ReplyInterface compose, ReplyModelListener listener);
}
