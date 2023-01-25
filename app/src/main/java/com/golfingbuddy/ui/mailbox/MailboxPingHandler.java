package com.golfingbuddy.ui.mailbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.ui.mailbox.chat.ChatView;
import com.golfingbuddy.ui.mailbox.conversation_list.MailboxView;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;
import com.golfingbuddy.ui.mailbox.mail.MailView;
import com.golfingbuddy.utils.SKDate;
import com.golfingbuddy.utils.SKPing;
import com.golfingbuddy.utils.SkApi;

import java.util.HashMap;

/**
 * Created by kairat on 3/5/15.
 */
public class MailboxPingHandler {

    private static MailboxPingHandler mInstance;

    private ConversationItemHolder mChat;
    private MailboxView mMailboxView;
    private PingServiceReceiver mReceiver;

    public static MailboxPingHandler getInstance() {
        if ( mInstance == null ) {
            synchronized ( MailboxPingHandler.class ) {
                mInstance = new MailboxPingHandler();
            }
        }

        return mInstance;
    }

    private MailboxPingHandler() {
        mReceiver = new PingServiceReceiver();
    }

    public void register(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SKPing.ACTION_COLLECT);
        filter.addAction(SKPing.ACTION_RESULT);
        LocalBroadcastManager.getInstance(context).registerReceiver(mReceiver, new IntentFilter(filter));
    }

    public void unregister(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mReceiver);
    }

    public ConversationItemHolder getChat() {
        return mChat;
    }

    public void setChat(ConversationItemHolder chat) {
        mChat = chat;
    }

    public MailboxView getConversationListHolder() {
        return mMailboxView;
    }

    public void setConversationListHolder(MailboxView conversationListHolder) {
        mMailboxView = conversationListHolder;
    }

    private class PingServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch ( intent.getAction() ) {
                case SKPing.ACTION_COLLECT:
                    HashMap<String, String> pingData = (HashMap<String, String>)intent.getSerializableExtra("commands");

                    if (mChat != null) {
                        ConversationList.ConversationItem item = mChat.getConversationItem();
                        HashMap<String, String> dialogData = new HashMap<>();

                        if (mChat instanceof ChatView) {
                            dialogData.put("mode", "chat");
                            dialogData.put(Constants.OPPONENT_ID, item.getStringOpponentId());
                            dialogData.put(Constants.LAST_MESSAGE_TIMESTAMP, Integer.toString(item.getLastMsgTimestamp()));
                        } else if (mChat instanceof MailView) {
                            dialogData.put("mode", "mail");
                            dialogData.put(Constants.CONVERSATION_ID, item.getStrConversationId());
                            dialogData.put(Constants.LAST_MESSAGE_TIMESTAMP, Integer.toString(item.getLastMsgTimestamp()));
                        }

                        pingData.put(Constants.DIALOG, new Gson().toJson(dialogData));
                    }

                    if (mMailboxView != null) {
                        HashMap<String, String> data = new HashMap<>();
                        data.put("lastRequestTimestamp", Long.toString(SkApplication.getConfig().getLongValue("mailbox", "lastRequestTimestamp", Long.valueOf("0"))));
                        pingData.put(Constants.NEW_MESSAGES, new Gson().toJson(data));
                    }
                    break;
                case SKPing.ACTION_RESULT:
                    JsonObject dataJson = SkApi.processResult(intent.getBundleExtra(SKPing.RESULT));

                    if (dataJson == null || !dataJson.has("ping") || dataJson.get("ping").isJsonNull()) {
                        return;
                    }

                    JsonObject pingJson = dataJson.getAsJsonObject("ping");

                    if (mChat != null && pingJson.has(Constants.DIALOG) && !pingJson.get(Constants.DIALOG).isJsonNull()) {
                        JsonObject dialogData = pingJson.getAsJsonObject(Constants.DIALOG);

                        if (mChat instanceof ChatView) {
                            ((ChatView) mChat).onLoadConversationNewMessages(DeserializerHelper.getInstance().getConversationHistory(dialogData));
                            ((ChatView) mChat).setUnreadMessageCount(!dialogData.has("countUnread") ? 0 : dialogData.getAsJsonPrimitive("countUnread").getAsInt());
                        } else if (mChat instanceof MailView) {
                            ((MailView) mChat).loadConversationData(DeserializerHelper.getInstance().getMailImage(dialogData));
                            ((MailView) mChat).setUnreadMessageCount(!dialogData.has("countUnread") ? 0 : dialogData.getAsJsonPrimitive("countUnread").getAsInt());
                        }

                        SkApplication.getConfig().saveConfig("mailbox", "count_new_messages", dialogData.getAsJsonPrimitive("count").getAsInt());
                    }

                    if (mMailboxView != null && pingJson.has(Constants.NEW_MESSAGES) && !pingJson.get(Constants.NEW_MESSAGES).isJsonNull()) {
                        mMailboxView.setConversationList(DeserializerHelper.getInstance().getConversationList(pingJson.getAsJsonObject(Constants.NEW_MESSAGES)));
                    }

                    SkApplication.getConfig().saveConfig("mailbox", "lastRequestTimestamp", SKDate.getInstance().getServerTimestamp());
                    break;
            }
        }
    }
}
