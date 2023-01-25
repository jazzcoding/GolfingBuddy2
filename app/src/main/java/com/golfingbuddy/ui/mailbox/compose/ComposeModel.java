package com.golfingbuddy.ui.mailbox.compose;

import com.golfingbuddy.ui.mailbox.mail.image.MailImage;

import java.util.ArrayList;

/**
 * Created by kairat on 3/18/15.
 */
public interface ComposeModel {
    int attachAttachment(long uid, String type, String path, ComposeModelListener listener);
    void deleteAttachment(MailImage.Message.Attachment attachment, ComposeModelListener listener);
    void suggestionListRequest(String constraint, ArrayList<String> idList, ComposeModelListener listener);
    void sendMessage(ComposeInterface compose, ComposeModelListener listener);
    void getRecipientInfo(String recipientId, ComposeModelListener listener);
}
