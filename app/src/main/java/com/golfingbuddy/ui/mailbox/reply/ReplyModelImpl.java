package com.golfingbuddy.ui.mailbox.reply;

import android.content.Intent;
import android.os.Bundle;

import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.ui.mailbox.Service;
import com.golfingbuddy.ui.mailbox.mail.image.MailImage;

/**
 * Created by kairat on 3/18/15.
 */
public class ReplyModelImpl implements ReplyModel {

    private Service mService;

    public ReplyModelImpl() {
        mService = new Service(SkApplication.getApplication());
    }

    @Override
    public int attachAttachment(long uid, String type, String path, final ReplyModelListener listener) {
        return mService.attachAttachment(uid, type, path, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                listener.onAttachAttachment(requestId, requestIntent, resultCode, data);
            }
        });
    }

    @Override
    public void deleteAttachment(final MailImage.Message.Attachment attachment, final ReplyModelListener listener) {
        mService.deleteAttachment(attachment.getId(), new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                listener.onDeleteAttachment(attachment, requestId, requestIntent, resultCode, data);
            }
        });
    }

    @Override
    public void sendMessage(ReplyInterface reply, final ReplyModelListener listener) {
        mService.sendReply(reply.getUid(), reply.getOpponentId(), reply.getConversationId(), reply.getText(), new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                listener.onMailSend(requestId, requestIntent, resultCode, data);
            }
        });
    }
}
