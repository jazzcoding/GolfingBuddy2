package com.golfingbuddy.ui.mailbox.mail.adapter;

import com.golfingbuddy.ui.mailbox.AttachmentHolderInterface;
import com.golfingbuddy.ui.mailbox.mail.image.MailImage;

/**
 * Created by kairat on 3/14/15.
 */
public interface MailMessageAttachment {
    MailImage.Message.Attachment getAttachment();
    void setAttachment(MailImage.Message.Attachment attachment);
    void setListener(MailMessageView view);
    void setListener(AttachmentHolderInterface view);
}
