package com.golfingbuddy.ui.mailbox.compose;

import android.content.Intent;
import android.os.Bundle;

import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.ui.mailbox.Service;
import com.golfingbuddy.ui.mailbox.mail.image.MailImage;

import java.util.ArrayList;

/**
 * Created by kairat on 3/18/15.
 */
public class ComposeModelImpl implements ComposeModel {

    private Service mService;

    public ComposeModelImpl() {
        mService = new Service(SkApplication.getApplication());
    }

    @Override
    public int attachAttachment(long uid, String type, String path, final ComposeModelListener listener) {
        return mService.attachAttachment(uid, type, path, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                listener.onAttachAttachment(requestId, requestIntent, resultCode, data);
            }
        });
    }

    @Override
    public void deleteAttachment(final MailImage.Message.Attachment attachment, final ComposeModelListener listener) {
        mService.deleteAttachment(attachment.getId(), new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                listener.onDeleteAttachment(attachment, requestId, requestIntent, resultCode, data);
            }
        });
    }

    @Override
    public void suggestionListRequest(String constraint, ArrayList<String> idList, final ComposeModelListener listener) {
        mService.suggestionListRequest(constraint, idList, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                listener.onSuggestListResponse(requestId, requestIntent, resultCode, data);
            }
        });
    }

    @Override
    public void sendMessage(ComposeInterface compose, final ComposeModelListener listener) {
        mService.sendCompose(compose.getUid(), compose.getOpponentId(), compose.getSubject(), compose.getText(), new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                listener.onMailSend(requestId, requestIntent, resultCode, data);
            }
        });
    }

    @Override
    public void getRecipientInfo(String recipientId, final ComposeModelListener listener) {
        mService.getRecipientInfo(recipientId, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                listener.onRecipientReady(requestId, requestIntent, resultCode, data);
            }
        });
    }
}
