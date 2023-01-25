package com.golfingbuddy.ui.mailbox.chat;

import android.content.Intent;
import android.os.Bundle;

import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.ui.mailbox.Service;
import com.golfingbuddy.ui.mailbox.chat.model.Message;

/**
 * Created by kairat on 3/20/15.
 */
public class ChatModelImpl implements ChatModel {

    protected Service mService;
    protected ChatModelResponseListener mListener;
    protected int requestLoadHistory;

    public ChatModelImpl(ChatModelResponseListener listener) {
        mService = new Service(SkApplication.getApplication());
        mListener = listener;
    }

    @Override
    public void getConversationMessages(String conversationId) {
        mService.getConversationMessages(conversationId, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
                mListener.onConversationMessagesLoad(requestId, requestIntent, resultCode, bundle);
            }
        });
    }

    @Override
    public void getRecipientInfo(String recipientId) {
        mService.getChatRecipientInfo(recipientId, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                mListener.onRecipientReady(requestId, requestIntent, resultCode, data);
            }
        });
    }

    @Override
    public void unreadConversation(String conversationId) {
        mService.markConversationUnRead(conversationId, null);
    }

    @Override
    public void deleteConversation(String conversationId) {
        mService.deleteConversation(conversationId, null);
    }

    @Override
    public void sendTextMessage(Message.ChatMessage message) {
        mService.sendMessage(message.getOpponentId(), message.getText(), message.getMode(), message.getLastMessageTimestamp(), message.getConversationId(), new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                mListener.onSendTextMessage(requestId, requestIntent, resultCode, data);
            }
        });
    }

    @Override
    public void sendAttachmentMessage(Message.ChatAttachment attachment) {
        mService.sendAttachment(attachment.getOpponentId(), attachment.getMode(), attachment.getLastMessageTimestamp(), attachment.getType(), attachment.getFile(), new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                mListener.onSendAttachmentMessage(requestId, requestIntent, resultCode, data);
            }
        });
    }

    @Override
    public void loadConversationHistory(String opponentId, String beforeId) {
        if (mService.isPending(requestLoadHistory)) {
            return;
        }

        requestLoadHistory = mService.getConversationHistory(opponentId, beforeId, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                mListener.onLoadHistory(requestId, requestIntent, resultCode, data);
            }
        });
    }

    @Override
    public void authorizeMessageView(String messageId, final ChatModelResponseListener listener) {
        mService.authorizeReadMessage(messageId, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                listener.onAuthorizeReadMessage(requestId, requestIntent, resultCode, data);
            }
        });
    }

    @Override
    public void winkBack(String messageId, final ChatModelResponseListener listener) {
        mService.winkBack(messageId, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                listener.onWinkBack(requestId, requestIntent, resultCode, data);
            }
        });
    }
}
