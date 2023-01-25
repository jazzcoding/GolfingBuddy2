package com.golfingbuddy.ui.mailbox.send;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.ui.mailbox.Constants;
import com.golfingbuddy.ui.mailbox.Service;
import com.golfingbuddy.ui.mailbox.chat.ChatActivity;
import com.golfingbuddy.ui.mailbox.compose.ComposeActivity;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;
import com.golfingbuddy.ui.mailbox.mail.MailActivity;
import com.golfingbuddy.utils.SkApi;

/**
 * Created by kairat on 3/30/15.
 */
public class Send {

    private Service mService;
    private Context mContext;
    private int mRecipient;
    private ProgressDialog mProcessDialog;

    public Send(Context context, int recipient) {
        this(context, recipient, "");
    }

    public Send(Context context, JsonObject item) {
        if (item == null) {
            return;
        }

        Intent intent = new Intent(context, MailActivity.class);
        intent.putExtra(Constants.CONVERSATION_ITEM, new Gson().fromJson(item, ConversationList.ConversationItem.class));
        context.startActivity(intent);
    }

    public Send(Context context, int recipient, final String mode) {
        if (context == null || recipient <= 0) {
            throw new IllegalArgumentException("Illegal arguments");
        }

        mService = new Service(SkApplication.getApplication());
        mContext = context;
        mRecipient = recipient;

        mProcessDialog = new ProgressDialog(mContext);
        mProcessDialog.setMessage(mContext.getResources().getString(R.string.mailbox_send_message));
        mProcessDialog.setCancelable(false);
        mProcessDialog.show();

        mService.getMailboxModes(new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                mProcessDialog.dismiss();
                JsonObject json = SkApi.processResult(data);

                if (json == null || !json.has("modes") || !json.get("modes").isJsonArray()) {
                    return;
                }

                JsonArray modes = json.getAsJsonArray("modes");

                if (modes.size() == 2) {
                    if (TextUtils.isEmpty(mode)) {
                        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(mContext);
                        myAlertDialog.setTitle(R.string.mailbox_send_dialog_title);

                        myAlertDialog.setPositiveButton(R.string.mailbox_send_dialog_chat, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                startChat();
                            }
                        });

                        myAlertDialog.setNegativeButton(R.string.mailbox_send_dialog_mail, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                startMail();
                            }
                        });
                        myAlertDialog.show();
                    } else {
                        switch (mode) {
                            case Constants.MODE_CHAT_STR:
                                startChat();
                                break;
                            case Constants.MODE_MAIL_STR:
                                startMail();
                                break;
                        }
                    }
                } else {
                    for (JsonElement element : modes) {
                        String _mode = element.getAsJsonPrimitive().getAsString();

                        if (!TextUtils.isEmpty(mode) && !_mode.equals(mode)) {
                            Toast.makeText(mContext, "Mailbox: " + mode + " mode is not enable", Toast.LENGTH_LONG).show();

                            return;
                        }

                        if (_mode.equals(Constants.MODE_MAIL_STR)) {
                            startMail();
                        } else {
                            startChat();
                        }

                        break;
                    }
                }
            }
        });
    }

    private void startChat() {
        Intent intent = new Intent(mContext, ChatActivity.class);
        intent.putExtra("recipientId", mRecipient);
        mContext.startActivity(intent);
    }

    private void startMail() {
        Intent intent = new Intent(mContext, ComposeActivity.class);
        intent.putExtra("recipientId", mRecipient);
        mContext.startActivity(intent);
    }
}
