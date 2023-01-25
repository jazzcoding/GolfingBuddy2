package com.golfingbuddy.ui.mailbox.chat;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.golfingbuddy.ui.mailbox.AuthorizationInfo;
import com.golfingbuddy.ui.mailbox.Constants;
import com.golfingbuddy.ui.mailbox.DeserializerHelper;
import com.golfingbuddy.ui.mailbox.chat.model.ConversationHistory;
import com.golfingbuddy.ui.mailbox.chat.model.Message;
import com.golfingbuddy.ui.mailbox.compose.Recipient;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;
import com.golfingbuddy.utils.SkApi;

/**
 * Created by kairat on 3/20/15.
 */
public class ChatPresenterImpl implements ChatPresenter, ChatModelResponseListener {

    private ChatView mChatView;
    private ChatModel mChatModel;

    public ChatPresenterImpl(ChatView view) {
        mChatView = view;
        mChatModel = new ChatModelImpl(this);
    }

    private JsonObject prepareData(Bundle bundle) {
        JsonObject json = SkApi.processResult(bundle);

        if (json == null || !json.has("result")) {
            return null;
        }

        JsonObject tmpObject = json.getAsJsonObject("result");

        if (tmpObject.has("error") && tmpObject.getAsJsonPrimitive("error").getAsBoolean()) {
            mChatView.feedback(tmpObject.get("message").getAsString());
            mChatView.hideSendingIndicator();

            return null;
        }

        JsonObject result = json.getAsJsonObject("result");
        AuthorizationInfo.getInstance().updateAuthorizationInfo(result);

        return result;
    }

    @Override
    public void getConversationMessages(String conversationId) {
        mChatView.setLoadingMode(true);
        mChatModel.getConversationMessages(conversationId);
    }

    @Override
    public void onConversationMessagesLoad(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        JsonObject json = prepareData(bundle);

        if (json == null) {
            return;
        }

        mChatView.setLoadingMode(false);
        mChatView.onLoadConversationMessages(DeserializerHelper.getInstance().getConversationHistory(json));
        mChatView.setUnreadMessageCount(!json.has("count") ? 0 : json.getAsJsonPrimitive("count").getAsInt());
        mChatView.authorizeAction();
    }

    @Override
    public void getRecipient(String recipientId) {
        mChatView.setLoadingMode(true);
        mChatModel.getRecipientInfo(recipientId);
    }

    @Override
    public void onRecipientReady(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        JsonObject json = prepareData(bundle);

        if (json == null) {
            return;
        }

        mChatView.setLoadingMode(false);
        mChatView.onLoadConversationMessages(DeserializerHelper.getInstance().getConversationHistory(json));

        JsonObject tmpObject = json.getAsJsonObject("info");
        Recipient recipient = new Recipient();
        recipient.setOpponentId(tmpObject.get("opponentId").getAsString());
        recipient.setDisplayName(tmpObject.get("displayName").getAsString());

        if (!tmpObject.get("avatarUrl").isJsonNull()) {
            recipient.setAvatarUrl(tmpObject.get("avatarUrl").getAsString());
        }

        mChatView.setRecipient(recipient);
        mChatView.authorizeAction();
    }

    @Override
    public void unreadConversation(String conversationId) {
        mChatModel.unreadConversation(conversationId);
        mChatView.onUnreadConversation();
    }

    @Override
    public void deleteConversation(String conversationId) {
        mChatModel.deleteConversation(conversationId);
        mChatView.onDeleteConversation();
    }

    @Override
    public void sendMessage(String message) {
        if (message.trim().isEmpty()) {
            mChatView.showChooseDialog();
        } else {
            sendTextMessage(message);
        }
    }

    @Override
    public void sendTextMessage(String message) {
        ConversationList.ConversationItem item = mChatView.getConversationItem();

        Message.ChatMessage chatMessage = new Message.ChatMessage();
        chatMessage.setOpponentId(item.getStringOpponentId());
        chatMessage.setText(message.trim());
        chatMessage.setMode(Constants.MODE_CHAT_STR);
        chatMessage.setConversationId(item.getStrConversationId());
        chatMessage.setLastMessageTimestamp(Integer.toString(item.getLastMsgTimestamp()));

        mChatView.showSendingIndicator();
        mChatModel.sendTextMessage(chatMessage);
    }

    @Override
    public void onSendTextMessage(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        JsonObject json = prepareData(bundle);

        if (json == null) {
            return;
        }

        mChatView.hideSendingIndicator();
        mChatView.onSendTextMessage(DeserializerHelper.getInstance().getConversationHistory(json));

        if (json.has("conversation") && !json.get("conversation").isJsonNull()) {
            mChatView.setConversationItem(new Gson().fromJson(json.get("conversation"), ConversationList.ConversationItem.class));
        }
    }

    @Override
    public void sendAttachmentMessage(String path, String type) {
        mChatView.showSendingIndicator();
        Message.ChatAttachment attachment = new Message.ChatAttachment();
        attachment.setOpponentId(mChatView.getConversationItem().getStringOpponentId());
        attachment.setMode(mChatView.getConversationItem().isChatMode() ? Constants.MODE_CHAT_STR : Constants.MODE_MAIL_STR);
        attachment.setLastMessageTimestamp(Integer.toString(mChatView.getConversationItem().getLastMsgTimestamp()));
        attachment.setFile(path);
        attachment.setType(type);
        mChatModel.sendAttachmentMessage(attachment);
    }

    @Override
    public void onSendAttachmentMessage(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        onSendTextMessage(requestId, requestIntent, resultCode, bundle);
    }

    @Override
    public void loadConversationHistory(String opponentId, String beforeId) {
        mChatModel.loadConversationHistory(opponentId, beforeId);
    }

    @Override
    public void onLoadHistory(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        JsonObject json = prepareData(bundle);

        if (json == null) {
            return;
        }

        mChatView.onLoadConversationHistory(DeserializerHelper.getInstance().getConversationHistory(json));
    }

    @Override
    public void authorizeMessageView(String messageId) {
        mChatModel.authorizeMessageView(messageId, this);
    }

    @Override
    public void winkBack(String messageId) {
        mChatModel.winkBack(messageId, this);
    }

    @Override
    public void onAuthorizeReadMessage(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        JsonObject json = prepareData(bundle);

        if (json == null) {
            return;
        }

        mChatView.setUnreadMessageCount(!json.has("count") ? 0 : json.getAsJsonPrimitive("count").getAsInt());

        ConversationHistory.Messages.Message message = new Gson().fromJson(json.get("message"), ConversationHistory.Messages.Message.class);
        mChatView.onAuthorizeResponse(message);
    }

    @Override
    public void onWinkBack(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        JsonObject json = prepareData(bundle);

        if (json == null) {
            return;
        }

        mChatView.setUnreadMessageCount(!json.has("count") ? 0 : json.getAsJsonPrimitive("count").getAsInt());

        ConversationHistory.Messages.Message message = new Gson().fromJson(json.get("message"), ConversationHistory.Messages.Message.class);
        mChatView.onWinkBackResponse(message);
    }
}
