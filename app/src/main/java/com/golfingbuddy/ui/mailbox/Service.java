package com.golfingbuddy.ui.mailbox;

import android.app.Application;
import android.content.Intent;

import com.google.gson.Gson;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.core.SkServiceHelper;

import java.util.ArrayList;

/**
 * Created by kairat on 2/13/15.
 */
public class Service extends SkServiceHelper {
    public Service( Application app ) {
        super(app);
    }

    public int getActionInfo(String[] actionNames, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.AUTHORIZE_ACTION_INFO);
        command.setParams("actionNames", new Gson().toJson(actionNames));

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int getMailboxModes(SkServiceCallbackListener listener) {
        int requestId = createId();
        Intent i = createIntent(application, new RestRequestCommand(RestRequestCommand.COMMAND.GET_MODES), requestId);

        return runRequest(requestId, i, listener);
    }

    public int getConversationList(SkServiceCallbackListener listener) {
        return getConversationList(0, listener);
    }

    public int getConversationList(int offset, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.LOAD_CONVERSATION);
        command.setParams("offset", Integer.toString(offset));

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int markConversationAsRead(String conversationId, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.MARK_CONVERSATION_AS_READ);
        command.setParams(Constants.CONVERSATION_ID, conversationId);

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int markConversationAsRead(String[] conversationIdList, SkServiceCallbackListener listener) {
        return markConversationAsRead(new Gson().toJson(conversationIdList), listener);
    }

    public int markConversationUnRead(String conversationId, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.MARK_CONVERSATION_UN_READ);
        command.setParams(Constants.CONVERSATION_ID, conversationId);

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int markConversationUnRead(String[] conversationIdList, SkServiceCallbackListener listener) {
        return markConversationUnRead(new Gson().toJson(conversationIdList), listener);
    }

    public int deleteConversation(String conversationId, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.DELETE_CONVERSATION);
        command.setParams(Constants.CONVERSATION_ID, conversationId);

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int deleteConversation(String[] conversationIdList, SkServiceCallbackListener listener) {
        return deleteConversation(new Gson().toJson(conversationIdList), listener);
    }

    public int getConversationMessages(String conversationId, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.LOAD_MESSAGES);
        command.setParams(Constants.CONVERSATION_ID, conversationId);

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int getConversationHistory(String opponentId, String beforeMessageId, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.LOAD_MESSAGES_HISTORY);
        command.setParams(Constants.OPPONENT_ID, opponentId);
        command.setParams(Constants.BEFORE_MESSAGE_ID, beforeMessageId);

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int sendMessage(String opponentId, String text, String mode, String lastMessageTimestamp, String conversationId, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.SEND_MESSAGE);
        command.setParams(Constants.OPPONENT_ID, opponentId);
        command.setParams(Constants.TEXT, text);
        command.setParams(Constants.MODE, mode);
        command.setParams(Constants.LAST_MESSAGE_TIMESTAMP, lastMessageTimestamp);

        if (conversationId != null) {
            command.setParams(Constants.CONVERSATION_ID, conversationId);
        }

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int sendAttachment(String opponentId, String mode, String lastMessageTimestamp, String type, String path, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.SEND_ATTACHMENT);
        command.setParams(Constants.OPPONENT_ID, opponentId);
        command.setParams(Constants.MODE, mode);
        command.setParams(Constants.LAST_MESSAGE_TIMESTAMP, lastMessageTimestamp);
        command.setFile(type, path);

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int attachAttachment(long uid, String type, String path, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.ATTACH_ATTACHMENT);
        command.setParams("uid", Long.toString(uid));
        command.setFile(type, path);

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int deleteAttachment(int id, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.DELETE_ATTACHMENT);
        command.setParams("id", Integer.toString(id));

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int suggestionListRequest(String constraint, ArrayList<String> idList, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.COMPOSE_SUGGESTION_LIST);
        command.setParams("term", constraint);

        if (idList != null && !idList.isEmpty()) {
            command.setParams("idList", new Gson().toJson(idList));
        }

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int sendCompose(String uid, String opponentId, String subject, String text, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.COMPOSE_SEND);
        command.setParams("uid", uid);
        command.setParams("opponentId", opponentId);
        command.setParams("subject", subject);
        command.setParams("text", text);

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int sendReply(String uid, String opponentId, String conversationId, String text, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.REPLY_SEND);
        command.setParams("uid", uid);
        command.setParams("opponentId", opponentId);
        command.setParams("conversationId", conversationId);
        command.setParams("text", text);

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int getRecipientInfo(String recipientId, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.GET_RECIPIENT_INFO);
        command.setParams("recipientId", recipientId);

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int getChatRecipientInfo(String recipientId, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.GET_CHAT_RECIPIENT_INFO);
        command.setParams("recipientId", recipientId);

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int authorizeReadMessage(String messageId, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.AUTHORIZE_READ_MESSAGE);
        command.setParams("messageId", messageId);

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }

    public int winkBack(String messageId, SkServiceCallbackListener listener) {
        int requestId = createId();

        RestRequestCommand command = new RestRequestCommand(RestRequestCommand.COMMAND.WINK_BACK);
        command.setParams("messageId", messageId);

        Intent i = createIntent(application, command, requestId);

        return runRequest(requestId, i, listener);
    }
}
