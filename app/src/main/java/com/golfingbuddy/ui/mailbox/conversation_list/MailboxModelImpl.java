package com.golfingbuddy.ui.mailbox.conversation_list;

import android.content.Intent;
import android.os.Bundle;

import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.ui.mailbox.Service;

/**
 * Created by kairat on 4/17/15.
 */
public class MailboxModelImpl implements MailboxModel {

    protected Service mService;
    protected MailboxModelResponseListener mListener;

    protected int loadRequestId;

    public MailboxModelImpl(MailboxModelResponseListener listener) {
        mService = new Service(SkApplication.getApplication());
        mListener = listener;
    }

    @Override
    public void getConversationList() {
        mService.getConversationList(new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                mListener.getConversationListResponse(requestId, requestIntent, resultCode, data);
            }
        });
    }

    @Override
    public void getConversationList(int offset) {
        if (mService.isPending(loadRequestId)) {
            return;
        }

        loadRequestId = mService.getConversationList(offset, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                mListener.getConversationListResponse(requestId, requestIntent, resultCode, data);
            }
        });
    }

    @Override
    public void markConversationAsRead(String[] idList) {
        mService.markConversationAsRead(idList, null);
    }

    @Override
    public void markConversationUnRead(String[] idList) {
        mService.markConversationUnRead(idList, null);
    }

    @Override
    public void deleteConversation(String[] idList) {
        mService.deleteConversation(idList, null);
    }
}
