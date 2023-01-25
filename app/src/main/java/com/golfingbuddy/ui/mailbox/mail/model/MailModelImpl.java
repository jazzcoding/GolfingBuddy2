package com.golfingbuddy.ui.mailbox.mail.model;

import android.content.Intent;
import android.os.Bundle;

import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.ui.mailbox.Service;
import com.golfingbuddy.ui.mailbox.mail.MailPresenterListener;

/**
 * Created by kairat on 3/14/15.
 */
public class MailModelImpl implements MailModel {

    private Service mService;

    public MailModelImpl() {
        mService = new Service(SkApplication.getApplication());
    }

    @Override
    public void getConversationData(final int conversationId, final MailPresenterListener listener) {
        mService.getConversationMessages(Integer.toString(conversationId), new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                listener.onLoadConversationData(data);
            }
        });
    }

    @Override
    public void unreadConversation(String conversationId, final MailPresenterListener listener) {
        mService.markConversationUnRead(conversationId, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                listener.onUnreadConversation(requestId, requestIntent, resultCode, data);
            }
        });
    }

    @Override
    public void deleteConversation(String conversationId, final MailPresenterListener listener) {
        mService.deleteConversation(conversationId, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                listener.onDeleteConversation(requestId, requestIntent, resultCode, data);
            }
        });
    }

    @Override
    public void authorizeMessageView(String messageId, final MailPresenterListener listener) {
        mService.authorizeReadMessage(messageId, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                listener.onAuthorizeReadMessage(requestId, requestIntent, resultCode, data);
            }
        });
    }

    protected int winkRequest;

    @Override
    public void winkBack(String messageId, final MailPresenterListener listener) {
        if (mService.isPending(winkRequest)) {
            return;
        }

        winkRequest = mService.winkBack(messageId, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                listener.onWinkBack(requestId, requestIntent, resultCode, data);
            }
        });
    }
}
