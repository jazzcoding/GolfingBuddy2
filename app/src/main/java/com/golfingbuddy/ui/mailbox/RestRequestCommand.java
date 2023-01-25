package com.golfingbuddy.ui.mailbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;

import com.google.gson.JsonObject;

import java.io.File;

import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by kairat on 2/13/15.
 */
public class RestRequestCommand extends com.golfingbuddy.core.RestRequestCommand {
    public enum COMMAND {
        GET_MODES,

        LOAD_CONVERSATION, MARK_CONVERSATION_AS_READ, MARK_CONVERSATION_UN_READ, DELETE_CONVERSATION,

        LOAD_MESSAGES, LOAD_MESSAGES_HISTORY, LOAD_MESSAGES_NEW,
        SEND_MESSAGE, SEND_ATTACHMENT,

        ATTACH_ATTACHMENT, DELETE_ATTACHMENT,

        COMPOSE_SUGGESTION_LIST, COMPOSE_SEND, REPLY_SEND,

        GET_RECIPIENT_INFO, GET_CHAT_RECIPIENT_INFO,

        AUTHORIZE_ACTION_INFO, AUTHORIZE_READ_MESSAGE,

        WINK_BACK
    }

    private COMMAND mCommand;
    private Attachment mAttachment;

    public RestRequestCommand(COMMAND command) {
        mCommand = command;
    }

    public void setParams(String key, String value) {
        params.put(key, value);
    }

    public void setFile(String type, String file) {
        mAttachment = new Attachment(type, file);
    }

    @Override
    protected void doExecute(Intent intent, Context context, ResultReceiver callback) {
        JsonObject result = new JsonObject();

        try {
            switch ( mCommand ) {
                case GET_MODES:
                    result = getRest().getMailboxModes();
                    break;
                case LOAD_CONVERSATION:
                    result = getRest().getConversationList(Integer.parseInt(params.get("offset")));
                    break;
                case MARK_CONVERSATION_AS_READ:
                    result = getRest().conversationAsRead(params);
                    break;
                case MARK_CONVERSATION_UN_READ:
                    result = getRest().conversationUnRead(params);
                    break;
                case DELETE_CONVERSATION:
                    result = getRest().deleteConversation(params);
                    break;
                case LOAD_MESSAGES:
                    result = getRest().getConversationMessages(params);
                    break;
                case LOAD_MESSAGES_HISTORY:
                    result = getRest().getConversationHistory(params);
                    break;
                case LOAD_MESSAGES_NEW:
                    result = getRest().getConversationMessages(params);
                    break;
                case SEND_MESSAGE:
                    result = getRest().sendMessage(params);
                    break;
                case SEND_ATTACHMENT:
                    result = getRest().sendAttachment(
                            new TypedFile(mAttachment.mType, new File(mAttachment.mPath)),
                            new TypedString(params.get("opponentId")),
                            new TypedString(params.get("lastMessageTimestamp"))
                    );
                    break;
                case ATTACH_ATTACHMENT:
                    result = getRest().attachAttachment(
                            new TypedFile(mAttachment.mType, new File(mAttachment.mPath)),
                            new TypedString(params.get("uid"))
                    );
                    break;
                case DELETE_ATTACHMENT:
                    result = getRest().deleteAttachment(params);
                    break;
                case COMPOSE_SUGGESTION_LIST:
                    result = getRest().findUser(params);
                    break;
                case COMPOSE_SEND:
                    result = getRest().sendCompose(params);
                    break;
                case REPLY_SEND:
                    result = getRest().sendReply(params);
                    break;
                case GET_RECIPIENT_INFO:
                    result = getRest().getRecipientInfo(params);
                    break;
                case GET_CHAT_RECIPIENT_INFO:
                    result = getRest().getChatRecipientInfo(params);
                    break;
                case AUTHORIZE_READ_MESSAGE:
                    result = getRest().authorizeReadMessage(params);
                    break;
                case AUTHORIZE_ACTION_INFO:
                    result = getRest().getActionInfo(params);
                    break;
                case WINK_BACK:
                    result = getRest().winkBack(params);
                    break;
            }
        } catch (Exception e) {
            e.toString();
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable("command", mCommand);
        bundle.putString("data", result.toString());
        callback.send(RESPONSE_SUCCESS, bundle);
    }

    public RestRequestCommand(Parcel in) {
        super(in);

        mCommand = (COMMAND)in.readSerializable();
        mAttachment = in.readParcelable(Attachment.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);

        parcel.writeSerializable(mCommand);
        parcel.writeParcelable(mAttachment, i);
    }

    public static final Creator<RestRequestCommand> CREATOR = new Creator<RestRequestCommand>() {
        public RestRequestCommand createFromParcel(Parcel in) {
            return new RestRequestCommand(in);
        }

        public RestRequestCommand[] newArray(int size) {
            return new RestRequestCommand[size];
        }
    };

    private static class Attachment implements Parcelable {
        private String mType;
        private String mPath;

        public Attachment(String type, String path) {
            mType = type;
            mPath = path;
        }

        public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
            @Override
            public Attachment createFromParcel(Parcel source) {
                return new Attachment(source.readString(), source.readString());
            }

            @Override
            public Attachment[] newArray(int size) {
                return new Attachment[0];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(mType);
            parcel.writeString(mPath);
        }
    }
}
