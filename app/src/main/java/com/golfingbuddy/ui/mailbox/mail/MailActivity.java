package com.golfingbuddy.ui.mailbox.mail;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.core.SkBaseActivity;
import com.golfingbuddy.ui.mailbox.AuthorizationInfo;
import com.golfingbuddy.ui.mailbox.Constants;
import com.golfingbuddy.ui.mailbox.MailboxPingHandler;
import com.golfingbuddy.ui.mailbox.attachment_viewer.AttachmentHelper;
import com.golfingbuddy.ui.mailbox.conversation_list.MailboxFragment;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;
import com.golfingbuddy.ui.mailbox.mail.adapter.MailListAdapter;
import com.golfingbuddy.ui.mailbox.mail.adapter.MailMessageAttachment;
import com.golfingbuddy.ui.mailbox.mail.adapter.MailMessageView;
import com.golfingbuddy.ui.mailbox.mail.image.MailImage;
import com.golfingbuddy.ui.mailbox.reply.ReplyActivity;
import com.golfingbuddy.ui.memberships.SubscribeOrBuyActivity;
import com.golfingbuddy.utils.SKPopupMenu;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by kairat on 3/14/15.
 */
public class MailActivity extends SkBaseActivity implements MailView {

    public static final int REPLY_RESULT = 1;
    public static final int COMPOSE_RESULT = 3;

    private static final int AUTHORIZE_RESULT = 100;

    private ListView mListView;
    private MailListAdapter mAdapter;

    private ConversationList.ConversationItem mConversationItem;
    private MailPresenter mMailPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (!intent.hasExtra(Constants.CONVERSATION_ITEM)) {
            throw new IllegalArgumentException("ConversationList item required");
        }

        setContentView(R.layout.mailbox_mail);

        mConversationItem = intent.getParcelableExtra(Constants.CONVERSATION_ITEM);
        mListView = (ListView) findViewById(R.id.mailbox_mail_list);
        mAdapter = new MailListAdapter(this, this);
        mListView.setAdapter(mAdapter);

        mMailPresenter = new MailPresenterImpl(this);

        ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);

            View customActionBar = View.inflate(this, R.layout.mailbox_mail_action_bar, null);
            customActionBar.findViewById(R.id.mailbox_chat_ab_terminate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            customActionBar.findViewById(R.id.mailbox_message_reply).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mAdapter.isEmpty()) {
                        reply(mConversationItem);
                    }
                }
            });

            Resources resources = getResources();

            SKPopupMenu.MenuParams params = new SKPopupMenu.MenuParams(this);
            params.setWidth(530);
            params.setXoff(-470);

            SKPopupMenu.MenuItem unreadMenuItem = new SKPopupMenu.MenuItem("unread");
            unreadMenuItem.setLabel(resources.getString(R.string.mailbox_as_unread));

            SKPopupMenu.MenuItem deleteMenuItem = new SKPopupMenu.MenuItem("delete");
            deleteMenuItem.setLabel(resources.getString(R.string.mailbox_delete_history));

            params.setMenuItems(new ArrayList<>(Arrays.asList(unreadMenuItem, deleteMenuItem)));
            View anchor = customActionBar.findViewById(R.id.mailbox_mail_ab_menu);
            params.setAnchor(anchor);

            final SKPopupMenu popupMenu = new SKPopupMenu(params);
            popupMenu.setMenuItemClickListener(new SKPopupMenu.MenuItemClickListener() {
                @Override
                public Boolean onClick(SKPopupMenu.MenuItem menuItem, int position, long id, ArrayList<SKPopupMenu.MenuItem> list) {
                    switch (menuItem.getKey()) {
                        case "unread":
                            mMailPresenter.unreadConversation(mConversationItem.getStrConversationId());
                            break;
                        case "delete":
                            mMailPresenter.deleteConversation(mConversationItem.getStrConversationId());
                            break;
                    }

                    return true;
                }
            });

            anchor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMenu.show();
                }
            });

            actionBar.setCustomView(customActionBar, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayShowCustomEnabled(true);
        }

        ((TextView) findViewById(R.id.mailbox_mail_subject)).setText(mConversationItem.getSubject());
        mMailPresenter.loadConversation(mConversationItem.getConversationId());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTHORIZE_RESULT) {
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            mListView.setVisibility(View.GONE);
            mMailPresenter.loadConversation(mConversationItem.getConversationId());
        } else {
            switch (resultCode) {
                case REPLY_RESULT:
                    if (data != null && data.hasExtra("reply")) {
                        mAdapter.clear();
                        mAdapter.notifyDataSetChanged();
                        mListView.setVisibility(View.GONE);
                        MailboxPingHandler.getInstance().setChat(null);
                        mMailPresenter.loadConversation(mConversationItem.getConversationId());
                    }

                    overridePendingTransition(R.anim.speedmatches_slide_in_left, R.anim.speedmatches_slide_out_right);
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constants.CONVERSATION_ITEM, mConversationItem);
        setResult(MailboxFragment.MAIL_RESULT, intent);

        super.onBackPressed();
    }

    @Override
    public void loadConversationData(MailImage image) {
        if (image == null || image.isEmpty()) {
            return;
        }

        mConversationItem.setLastMsgTimestamp(image.getLastMessage().getTimeStamp());
        mAdapter.addAll(image.getMessages());
        mAdapter.notifyDataSetChanged();
        mListView.post(new Runnable() {
            @Override
            public void run() {
                mListView.setSelection(mListView.getCount() - 1);
            }
        });
        mListView.setVisibility(View.VISIBLE);

        if (MailboxPingHandler.getInstance().getChat() == null) {
            MailboxPingHandler.getInstance().setChat(this);
        }
    }

    @Override
    public void setUnreadMessageCount(int count) {
        if (mConversationItem != null) {
            mConversationItem.setNewMsgCount(count);
        }
    }

    @Override
    public void onUnreadConversation() {
        MailboxPingHandler.getInstance().setChat(null);
        Intent intent = new Intent();
        intent.putExtra("action", "unread");
        intent.putExtra(Constants.CONVERSATION_ITEM, mConversationItem);
        setResult(MailboxFragment.MAIL_RESULT, intent);
        finish();
    }

    @Override
    public void onDeleteConversation() {
        MailboxPingHandler.getInstance().setChat(null);
        Intent intent = new Intent();
        intent.putExtra("action", "delete");
        intent.putExtra(Constants.CONVERSATION_ITEM, mConversationItem);
        setResult(MailboxFragment.MAIL_RESULT, intent);
        finish();
    }

    @Override
    public void clearConversationList() {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public ConversationList.ConversationItem getConversationItem() {
        return mConversationItem;
    }

    @Override
    public void onMailMessageClick(MailMessageView mailMessageView) {
        MailImage.Message message = mailMessageView.getMessage();

        if (message.getReadMessageAuthorized()) {
            if (message.getIsSystem() && message.getSystemType() != null) {
                switch (message.getSystemType()) {
                    case "renderWink":
                        message.setSystemType("winkIsSent");
                        mMailPresenter.winkBack(message.getStrId());
                        break;
                }

                mailMessageView.getView().setClickable(false);
            } else {
                mailMessageView.expandMessage();
            }
        } else {
            AuthorizationInfo.ActionInfo info = AuthorizationInfo.getInstance().getActionInfo("read_message");

            if (info == null) {
                return;
            }

            switch (info.getStatus()) {
                case PROMOTED:
                    Intent intent = new Intent(this, SubscribeOrBuyActivity.class);
                    intent.putExtra("pluginKey", "mailbox");
                    intent.putExtra("actionKey", "read_message");

                    startActivityForResult(intent, AUTHORIZE_RESULT);
                    break;
                case AVAILABLE:
                    mMailPresenter.authorizeMessageView(mailMessageView.getMessage().getStrId());
                    break;
            }
        }
    }

    @Override
    public void onMailAttachmentClick(MailMessageAttachment attachment) {
        AttachmentHelper.Builder builder = new AttachmentHelper.Builder(this);
        builder.setAttachmentUrl(attachment.getAttachment().getDownloadUrl());
        builder.open();
    }

    @Override
    public void reply(ConversationList.ConversationItem conversationItem) {
        Intent intent = new Intent(this, ReplyActivity.class);
        intent.putExtra(Constants.CONVERSATION_ITEM, mConversationItem);
        startActivityForResult(intent, REPLY_RESULT);
        overridePendingTransition(R.anim.speedmatches_slide_in_right, R.anim.speedmatches_slide_out_left);
    }

    @Override
    public void onAuthorizeResponse(MailImage.Message message) {
        if (message == null) {
            return;
        }

        if (mAdapter.updateItem(message)) {
            mAdapter.notifyDataSetChanged();
        }

        mConversationItem.setPreviewText(message.getText());
    }

    @Override
    public void onWinkBackResponse(MailImage.Message message) {
        if (message == null) {
            return;
        }

//        mConversationItem.setLastMsgTimestamp(message.getTimeStamp());
//        mAdapter.add(message);
//        mAdapter.notifyDataSetChanged();
//        mListView.post(new Runnable() {
//            @Override
//            public void run() {
//                mListView.setSelection(mListView.getCount() - 1);
//            }
//        });
    }
}
