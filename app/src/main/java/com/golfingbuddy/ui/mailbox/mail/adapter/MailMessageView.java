package com.golfingbuddy.ui.mailbox.mail.adapter;

import com.golfingbuddy.ui.mailbox.AttachmentHolderInterface;
import com.golfingbuddy.ui.mailbox.RenderInterface;
import com.golfingbuddy.ui.mailbox.mail.image.MailImage;

/**
 * Created by kairat on 3/14/15.
 */
public interface MailMessageView extends RenderInterface, AttachmentHolderInterface {
    MailImage.Message getMessage();
    void setMessage(MailImage.Message message);
    void expandMessage();
}
