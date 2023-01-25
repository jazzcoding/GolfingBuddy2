package com.golfingbuddy.ui.mailbox.conversation_list;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.golfingbuddy.ui.mailbox.AuthorizationInfo;
import com.golfingbuddy.ui.mailbox.Constants;
import com.golfingbuddy.ui.mailbox.DeserializerHelper;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;
import com.golfingbuddy.utils.SkApi;

/**
 * Created by kairat on 4/17/15.
 */
public class MailboxPresenterImpl implements MailboxPresenter, MailboxModelResponseListener {

    private static final int CONVERSATION_LIMIT = 10;

    protected MailboxView mView;
    protected MailboxModel mModel;

    public MailboxPresenterImpl(MailboxView view) {
        mView = view;
        mModel = new MailboxModelImpl(this);
    }

    protected JsonObject prepareResponse(Bundle bundle) {
        JsonObject jsonObject = SkApi.processResult(bundle);

        if (jsonObject == null) {
            return null;
        }

        if (jsonObject.has("error") && jsonObject.getAsJsonPrimitive("error").getAsBoolean()) {
            mView.feedback(jsonObject.get("message").getAsString());

            return null;
        }

        return jsonObject.getAsJsonObject("result");
    }

    @Override
    public void renderConversationList() {
        mView.setLoadingMode(true);
        mModel.getConversationList();
    }

    @Override
    public void getConversationListResponse(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        JsonObject result = prepareResponse(bundle);

        if (result == null) {
            return;
        }

        AuthorizationInfo.getInstance().updateAuthorizationInfo(result);

        mView.setLoadingMode(false);

        JsonArray modes = result.getAsJsonArray("modes");

        if (modes.size() == 2) {
            mView.setModes(Constants.MODE_BOTH);
        } else {
            mView.setModes(modes.get(0).getAsString().equals(Constants.MODE_CHAT_STR) ? Constants.MODE_CHAT : Constants.MODE_MAIL);
        }

        ConversationList list = DeserializerHelper.getInstance().getConversationList(result);
        mView.setConversationList(list);

        if (list == null || list.size() % CONVERSATION_LIMIT != 0) {
            mView.disableScrollListener();
        } else {
            mView.initListView();
        }
    }

    @Override
    public void loadOffsetConversationList(int offset) {
        mModel.getConversationList(offset);
    }

    @Override
    public void markConversationAsRead(String[] idList) {
        mModel.markConversationAsRead(idList);
    }

    @Override
    public void markConversationUnRead(String[] idList) {
        mModel.markConversationUnRead(idList);
    }

    @Override
    public void deleteConversation(String[] idList) {
        mModel.deleteConversation(idList);
    }
}
