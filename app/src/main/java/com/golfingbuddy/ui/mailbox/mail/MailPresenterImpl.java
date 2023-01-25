package com.golfingbuddy.ui.mailbox.mail;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.golfingbuddy.ui.mailbox.AuthorizationInfo;
import com.golfingbuddy.ui.mailbox.DeserializerHelper;
import com.golfingbuddy.ui.mailbox.mail.image.MailImage;
import com.golfingbuddy.ui.mailbox.mail.model.MailModel;
import com.golfingbuddy.ui.mailbox.mail.model.MailModelImpl;
import com.golfingbuddy.utils.SkApi;

/**
 * Created by kairat on 3/14/15.
 */
public class MailPresenterImpl implements MailPresenter, MailPresenterListener {

    private MailView mMailView;
    private MailModel mMailModel;

    public MailPresenterImpl(MailView mailView) {
        mMailView = mailView;
        mMailModel = new MailModelImpl();
    }

    @Override
    public void loadConversation(int conversationId) {
        mMailModel.getConversationData(conversationId, this);
    }

    @Override
    public void unreadConversation(String conversationId) {
        mMailModel.unreadConversation(conversationId, this);
    }

    @Override
    public void deleteConversation(String conversationId) {
        mMailModel.deleteConversation(conversationId, this);
    }

    @Override
    public void reloadConversation(int conversationId) {
        mMailView.clearConversationList();
        mMailModel.getConversationData(conversationId, this);
    }

    @Override
    public void authorizeMessageView(String messageId) {
        mMailModel.authorizeMessageView(messageId, this);
    }

    @Override
    public void winkBack(String messageId) {
        mMailModel.winkBack(messageId, this);
    }

    @Override
    public void onLoadConversationData(Bundle bundle) {
        JsonObject json = SkApi.processResult(bundle);

        if (json == null || !json.has("result")) {
            return;
        }

        JsonObject result = json.getAsJsonObject("result");
        AuthorizationInfo.getInstance().updateAuthorizationInfo(result);

        mMailView.loadConversationData(DeserializerHelper.getInstance().getMailImage(result));
        mMailView.setUnreadMessageCount(!result.has("count") ? 0 : result.getAsJsonPrimitive("count").getAsInt());
    }

    @Override
    public void onUnreadConversation(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        mMailView.onUnreadConversation();
    }

    @Override
    public void onDeleteConversation(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        mMailView.onDeleteConversation();
    }

    @Override
    public void onAuthorizeReadMessage(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        JsonObject json = SkApi.processResult(bundle);

        if (json == null || !json.has("result")) {
            return;
        }

        JsonObject result = json.getAsJsonObject("result");
        AuthorizationInfo.getInstance().updateAuthorizationInfo(result);

        mMailView.setUnreadMessageCount(!result.has("count") ? 0 : result.getAsJsonPrimitive("count").getAsInt());

        MailImage.Message message = new Gson().fromJson(result.get("message"), MailImage.Message.class);
        mMailView.onAuthorizeResponse(message);
    }

    @Override
    public void onWinkBack(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        JsonObject json = SkApi.processResult(bundle);

        if (json == null) {
            return;
        }

        JsonObject result = json.getAsJsonObject("result");
        AuthorizationInfo.getInstance().updateAuthorizationInfo(result);

        mMailView.setUnreadMessageCount(!result.has("count") ? 0 : result.getAsJsonPrimitive("count").getAsInt());

        try {
            MailImage.Message message = new Gson().fromJson(result.get("message"), MailImage.Message.class);
            mMailView.onWinkBackResponse(message);
        } catch (JsonSyntaxException ignore) {
            int f = 90;
        }

    }
}
