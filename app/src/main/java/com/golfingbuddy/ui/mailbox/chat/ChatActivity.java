package com.golfingbuddy.ui.mailbox.chat;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.golfingbuddy.R;
import com.golfingbuddy.core.SkBaseActivity;
import com.golfingbuddy.ui.mailbox.AuthorizationInfo;
import com.golfingbuddy.ui.mailbox.MailboxPingHandler;
import com.golfingbuddy.ui.mailbox.attachment_viewer.AttachmentHelper;
import com.golfingbuddy.ui.mailbox.compose.Recipient;
import com.golfingbuddy.ui.mailbox.conversation_list.MailboxFragment;
import com.golfingbuddy.ui.mailbox.chat.ui.ConversationHistoryAdapter;
import com.golfingbuddy.ui.mailbox.Constants;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;
import com.golfingbuddy.ui.mailbox.chat.model.ConversationHistory;
import com.golfingbuddy.ui.mailbox.chat.model.DailyHistory;
import com.golfingbuddy.ui.memberships.SubscribeOrBuyActivity;
import com.golfingbuddy.utils.SKFileUtils;
import com.golfingbuddy.utils.SKPopupMenu;
import com.golfingbuddy.utils.SKRoundedImageView;
import com.squareup.picasso.Callback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by kairat on 2/20/15.
 */
public class ChatActivity extends SkBaseActivity implements ChatView, SKPopupMenu.MenuItemClickListener, AbsListView.OnScrollListener, View.OnClickListener {

    private static final int GALLERY_RESULT = 1;
    private static final int CAMERA_RESULT = 2;
    private static final int AUTHORIZE_RESULT = 100;

    private ChatPresenter mChatPresenter;

    private ConversationHistoryAdapter mAdapter;

    private TextView mDateView;
    private ListView mHistoryList;
    private EditText mMessageEditor;
    private ImageView mSendMessage;

    private ConversationList.ConversationItem mConversationItem;
    private int mLoadHistoryRequest;
    private Boolean isScrolled = false;

    private ConversationHistory.Messages.Message mTagMessage;

    private String mCurrentPhotoPath;
    private static final String albumName = "SkadateX";
    private static final String tmpFileName = "mailbox_compose_camera_attachment";
    private static final String tmpFileExt = ".jpg";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mailbox_conversation_view);

        mChatPresenter = new ChatPresenterImpl(this);

        Intent intent = getIntent();

        if (intent.hasExtra(Constants.CONVERSATION_ITEM)) {
            mConversationItem = intent.getParcelableExtra(Constants.CONVERSATION_ITEM);
            mChatPresenter.getConversationMessages(mConversationItem.getStrConversationId());
        } else if (intent.hasExtra("recipientId")) {
            mChatPresenter.getRecipient(Integer.toString(intent.getIntExtra("recipientId", 0)));
        } else {
            throw new IllegalArgumentException("Recipient data required");
        }

        initView();
    }

    private void initView() {
        mDateView = (TextView) findViewById(R.id.mailbox_history_list_date);

        mAdapter = new ConversationHistoryAdapter(this, this);
        mHistoryList = (ListView) findViewById(R.id.mailbox_history_list);
        mHistoryList.setAdapter(mAdapter);

        mMessageEditor = (EditText) findViewById(R.id.mailbox_message_text);
        mSendMessage = (ImageView) findViewById(R.id.mailbox_send_message);

        initEventListener();
    }

    private void initEventListener() {
        mHistoryList.setOnScrollListener(this);
        mMessageEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSendMessage.setImageResource(s.toString().trim().isEmpty() ? R.drawable.ic_sendphoto : R.drawable.ic_send_disable);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mMessageEditor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && checkReplyAction()) {
                    InputMethodManager methodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    methodManager.hideSoftInputFromWindow(mMessageEditor.getWindowToken(), 0);
                }
            }
        });
        mSendMessage.setOnClickListener(this);
    }

    @Override
    public void feedback(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setLoadingMode(Boolean mode) {
        ActionBar actionBar = getActionBar();

        if (mode != null && mode) {
            findViewById(R.id.mailbox_chat_content).setVisibility(View.GONE);
            findViewById(R.id.mailbox_chat_progress).setVisibility(View.VISIBLE);

            if (actionBar != null) {
                actionBar.hide();
            }
        } else {
            findViewById(R.id.mailbox_chat_content).setVisibility(View.VISIBLE);
            findViewById(R.id.mailbox_chat_progress).setVisibility(View.GONE);

            if (actionBar != null) {
                actionBar.show();
            }
        }

        findViewById(R.id.mailbox_authorize).setVisibility(View.GONE);
    }

    @Override
    public void onLoadConversationMessages(ConversationHistory history) {
        try {
            mAdapter.setConversation(history);
            mAdapter.notifyDataSetChanged();
            mHistoryList.post(new Runnable() {
                @Override
                public void run() {
                    if ( mHistoryList != null && mHistoryList.getChildAt(0) != null && mHistoryList.getChildAt(0).getTag() != null ) {
                        mDateView.setText(((DailyHistory) mHistoryList.getChildAt(0).getTag()).getHistory().getDate());
                    }
                }
            });
        } catch (ConversationHistoryAdapter.ConversationException e) {
            // Nothing
        }

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
    public void setRecipient(Recipient recipient) {
        if (recipient == null) {
            return;
        }

        ConversationList.ConversationItem item = new ConversationList.ConversationItem();
        item.setOpponentId(Integer.parseInt(recipient.getOpponentId()));
        item.setAvatarUrl(recipient.getAvatarUrl());
        item.setDisplayName(recipient.getDisplayName());

        mConversationItem = item;

        if (!mAdapter.isEmpty()) {
            ConversationHistory.Messages.Message message = mAdapter.getConversationHistory().getLastDailyHistory().getLastMessages().getLastMessages().getLastMessage();
            mConversationItem.setConversationId(message.getConversationId());
            mConversationItem.setLastMsgTimestamp(message.getTimeStamp());
            MailboxPingHandler.getInstance().setChat(this);
        }
    }

    @Override
    public void authorizeAction() {
        initActionBar();

        if (!mAdapter.isEmpty()) {
            return;
        }

        AuthorizationInfo.ActionInfo startAction = AuthorizationInfo.getInstance().getActionInfo("send_chat_message");

        if (startAction != null && !startAction.isAuthorized()) {
            findViewById(R.id.mailbox_chat_content).setVisibility(View.GONE);
            findViewById(R.id.mailbox_chat_progress).setVisibility(View.GONE);
            findViewById(R.id.mailbox_authorize).setVisibility(View.VISIBLE);

            if (startAction.isBillingEnable() && startAction.getStatus().equalsName(AuthorizationInfo.STATUS.PROMOTED)) {
                ((ImageView) findViewById(R.id.mailbox_authorize_icon)).setImageResource(R.drawable.lock_enable);
                findViewById(R.id.mailbox_authorize_content).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChatActivity.this, SubscribeOrBuyActivity.class);
                        intent.putExtra("pluginKey", "mailbox");
                        intent.putExtra("actionKey", "send_chat_message");

                        startActivityForResult(intent, AUTHORIZE_RESULT);
                    }
                });
            }

            ((TextView) findViewById(R.id.mailbox_authorize_message)).setText(startAction.getMessage());
        }
    }

    @Override
    public void initActionBar() {
        ActionBar actionBar = getActionBar();

        if ( actionBar == null ) {
            return;
        }

        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        final View customActionBar = View.inflate(this, R.layout.mailbox_chat_action_bar, null);
        ((SKRoundedImageView) customActionBar.findViewById(R.id.mailbox_chat_ab_avatar)).setImageUrl(mConversationItem.getAvatarUrl() + "", new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                ((ImageView) customActionBar.findViewById(R.id.mailbox_chat_ab_avatar)).setImageResource(R.drawable.rounded_rectangle_2_copy_2);
            }
        });
        ((TextView)customActionBar.findViewById(R.id.mailbox_chat_ab_display_name)).setText(mConversationItem.getDisplayName());
        customActionBar.findViewById(R.id.mailbox_chat_ab_terminate).setOnClickListener(this);
        customActionBar.findViewById(R.id.mailbox_chat_ab_back_icon).setOnClickListener(this);

        actionBar.setCustomView(customActionBar, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        actionBar.setDisplayShowCustomEnabled(true);

        Resources resources = getResources();

        SKPopupMenu.MenuParams params = new SKPopupMenu.MenuParams(this);
        params.setWidth(530);
        params.setXoff(-470);

        SKPopupMenu.MenuItem unreadMenuItem = new SKPopupMenu.MenuItem("unread");
        unreadMenuItem.setLabel(resources.getString(R.string.mailbox_as_unread));

        SKPopupMenu.MenuItem deleteMenuItem = new SKPopupMenu.MenuItem("delete");
        deleteMenuItem.setLabel(resources.getString(R.string.mailbox_delete_history));

        params.setMenuItems(new ArrayList<>(Arrays.asList(unreadMenuItem, deleteMenuItem)));
        View anchor = customActionBar.findViewById(R.id.mailbox_chat_ab_menu);
        anchor.setVisibility(View.VISIBLE);
        params.setAnchor(anchor);

        final SKPopupMenu popupMenu = new SKPopupMenu(params);
        popupMenu.setMenuItemClickListener(this);

        anchor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }

    @Override
    public Boolean onClick(SKPopupMenu.MenuItem menuItem, int position, long id, ArrayList<SKPopupMenu.MenuItem> list) {
        switch (menuItem.getKey()) {
            case "unread":
                mChatPresenter.unreadConversation(mConversationItem.getStrConversationId());
                break;
            case "delete":
                mChatPresenter.deleteConversation(mConversationItem.getStrConversationId());
                break;
        }

        return true;
    }

    private Boolean checkReplyAction() {
        AuthorizationInfo.ActionInfo replyAction = AuthorizationInfo.getInstance().getActionInfo("reply_to_chat_message");

        if (replyAction == null) {
            return false;
        }

        if (!replyAction.isAuthorized()) {
            findViewById(R.id.mailbox_chat_content).setVisibility(View.GONE);
            findViewById(R.id.mailbox_chat_progress).setVisibility(View.GONE);
            findViewById(R.id.mailbox_authorize).setVisibility(View.VISIBLE);

            if (replyAction.isBillingEnable() && replyAction.getStatus().equalsName(AuthorizationInfo.STATUS.PROMOTED)) {
                ((ImageView) findViewById(R.id.mailbox_authorize_icon)).setImageResource(R.drawable.lock_enable);
                findViewById(R.id.mailbox_authorize_content).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChatActivity.this, SubscribeOrBuyActivity.class);
                        intent.putExtra("pluginKey", "mailbox");
                        intent.putExtra("actionKey", "reply_to_chat_message");

                        startActivityForResult(intent, AUTHORIZE_RESULT);
                    }
                });
            }

            ((TextView) findViewById(R.id.mailbox_authorize_message)).setText(replyAction.getMessage());

            return true;
        }

        return false;
    }

    @Override
    public void onUnreadConversation() {
        MailboxPingHandler.getInstance().setChat(null);
        Intent intent = new Intent();
        intent.putExtra("action", "unread");
        intent.putExtra(Constants.CONVERSATION_ITEM, mConversationItem);
        setResult(MailboxFragment.CHAT_RESULT, intent);
        finish();
    }

    @Override
    public void onDeleteConversation() {
        MailboxPingHandler.getInstance().setChat(null);
        Intent intent = new Intent();
        intent.putExtra("action", "delete");
        intent.putExtra(Constants.CONVERSATION_ITEM, mConversationItem);
        setResult(MailboxFragment.CHAT_RESULT, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        MailboxPingHandler.getInstance().setChat(null);
        Intent intent = new Intent();
        intent.putExtra(Constants.CONVERSATION_ITEM, mConversationItem);
        setResult(MailboxFragment.CHAT_RESULT, intent);

        super.onBackPressed();
    }

    @Override
    public void sendMessage() {
        if (!checkReplyAction()) {
            mChatPresenter.sendMessage(mMessageEditor.getText().toString());
            mMessageEditor.setText("");
        } else {
            InputMethodManager methodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            methodManager.hideSoftInputFromWindow(mMessageEditor.getWindowToken(), 0);
        }
    }

    @Override
    public void onSendTextMessage(ConversationHistory history) {
        onLoadConversationNewMessages(history);

        if (mCurrentPhotoPath != null) {
            File file = new File(mCurrentPhotoPath);

            if (file.exists() && file.canWrite()) {
                file.delete();
            }
        }
    }

    @Override
    public void onLoadConversationNewMessages(ConversationHistory history) {
        if (history == null || history.isEmpty()) {
            return;
        }

        for (int i = 0, j = mHistoryList.getChildCount(); i < j; i++) {
            DailyHistory historyModel = (DailyHistory) mHistoryList.getChildAt(i).getTag();

            for (ConversationHistory.DailyHistory dailyHistory : history.getHistories()) {
                if (historyModel.addHistory(dailyHistory)) {
                    history.getHistories().remove(dailyHistory);

                    break;
                }
            }
        }

        if (!history.isEmpty()) {
            for (ConversationHistory.DailyHistory dailyHistory : history.getHistories()) {
                mAdapter.add(dailyHistory);
            }
        }

        ConversationHistory.DailyHistory dailyHistory = mAdapter.getConversationHistory().getLastDailyHistory();

        mConversationItem.setPreviewText(dailyHistory.getLastMessages().getLastMessages().getLastMessage().getText());
        mConversationItem.setLastMsgTimestamp(dailyHistory.getLastMessageTimestamp());

        mHistoryList.setStackFromBottom(true);
        mHistoryList.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void setConversationItem(ConversationList.ConversationItem item) {
        if (item != null) {
            mConversationItem = item;

            MailboxPingHandler.getInstance().setChat(this);
        }
    }

    @Override
    public void showChooseDialog() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle(R.string.mailbox_attachment_dialog_title);

        myAlertDialog.setPositiveButton(R.string.mailbox_attachment_dialog_gallery, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                galleryIntent.putExtra("return-data", true);
                ChatActivity.this.startActivityForResult(galleryIntent, GALLERY_RESULT);
            }
        });

        myAlertDialog.setNegativeButton(R.string.mailbox_attachment_dialog_camera, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                try {
                    File f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    ChatActivity.this.startActivityForResult(cameraIntent, CAMERA_RESULT);
                } catch (IOException e) {
                    e.printStackTrace();
                    feedback("Failed to create directory");
                }
            }
        });
        myAlertDialog.show();
    }

    @Override
    public void showSendingIndicator() {
        findViewById(R.id.mailbox_compose_send_indicator).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSendingIndicator() {
        findViewById(R.id.mailbox_compose_send_indicator).setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTHORIZE_RESULT) {
            MailboxPingHandler.getInstance().setChat(null);
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            mChatPresenter.getRecipient(mConversationItem.getStringOpponentId());
        } else {
            File file = null;
            String path = null;
            String type = null;

            switch ( requestCode ) {
                case GALLERY_RESULT:
                    if ( data != null && data.getData() != null ) {
                        Uri uri = data.getData();
                        path = SKFileUtils.getPath(this, uri);
                        type = data.getType();
                        file = new File(path);

                        if (file.exists() && (type == null || type.isEmpty())) {
                            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(path));
                        }

                    }
                    break;
                case CAMERA_RESULT:
                    path = mCurrentPhotoPath;
                    type = "image/jpeg";
                    file = new File(path);
                    break;
                default: return;
            }

            if (file == null || !file.exists()) {
                feedback("File not found.");
            } else {
                mChatPresenter.sendAttachmentMessage(path, type);
            }
        }
    }

    @Override
    public void onAttachmentClick(ConversationHistory.Messages.Message.Attachment attachment) {
        AttachmentHelper.Builder builder = new AttachmentHelper.Builder(this);
        builder.setAttachmentUrl(attachment.getDownloadUrl());
        builder.open();
    }

    @Override
    public void onAuthorizeActionClick(ConversationHistory.Messages.Message message, View anchor) {
        AuthorizationInfo.ActionInfo info = AuthorizationInfo.getInstance().getActionInfo("read_chat_message");

        if (info.getStatus().equalsName(AuthorizationInfo.STATUS.PROMOTED)) {
            Intent intent = new Intent(this, SubscribeOrBuyActivity.class);
            intent.putExtra("pluginKey", "mailbox");
            intent.putExtra("actionKey", "read_chat_message");

            startActivityForResult(intent, AUTHORIZE_RESULT);
        } else {
            mChatPresenter.authorizeMessageView(message.getStrId());
        }
    }

    @Override
    public void onAuthorizeResponse(ConversationHistory.Messages.Message message) {
        ConversationHistory.Messages.Message tmpMessage = mAdapter.findMessage(message);

        if (tmpMessage == null) {
            return;
        }

        mAdapter.updateMessage(message);
        mAdapter.notifyDataSetChanged();
        mConversationItem.setPreviewText(message.getText());
    }

    @Override
    public void onLoadConversationHistory(ConversationHistory history) {
        View firstView = mHistoryList.getChildAt(mHistoryList.getFirstVisiblePosition());
        DailyHistory dailyHistory = (DailyHistory)firstView.getTag();
        final ConversationHistory.Messages.Message firstMessage = dailyHistory.getHistory().getFirstMessages().getFirstMessages().getFirstMessage();
        final int top = firstView.findViewWithTag(firstMessage).getTop();

        if (mAdapter.prependConversation(history)) {
            mAdapter.notifyDataSetChanged();
            mHistoryList.post(new Runnable() {
                @Override
                public void run() {
                    final int position = mAdapter.getConversationHistory().getHistoryIndex(firstMessage);
                    mHistoryList.setSelectionFromTop(position, 0);
                    mHistoryList.post(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0, j = mHistoryList.getChildCount(); i < j; i++) {
                                View view = mHistoryList.getChildAt(i).findViewWithTag(firstMessage);

                                if (view != null) {
                                    mHistoryList.setSelectionFromTop(position, -(view.getTop() - top));

                                    break;
                                }
                            }

                            isScrolled = false;
                        }
                    });
                }
            });
        } else {
            isScrolled = false;
        }
    }

    @Override
    public void onWinkBackClick(ConversationHistory.Messages.Message message) {
        mChatPresenter.winkBack(message.getStrId());
    }

    @Override
    public void onWinkBackResponse(ConversationHistory.Messages.Message message) {
        if (message == null) {
            return;
        }

        ConversationHistory.Messages.Message tmpMessage = mAdapter.findMessage(message);

        if (tmpMessage == null) {
            return;
        }

        mHistoryList.setStackFromBottom(true);
        mHistoryList.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mAdapter.updateMessage(message);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mAdapter.isEmpty() || isScrolled) {
            return;
        }

        View view = listView.getChildAt(0);
        DailyHistory dailyHistory = (DailyHistory) view.getTag();
        mDateView.setText(dailyHistory.getHistory().getDate());

        if (view.getTop() != 0) {
            return;
        }

        mHistoryList.setStackFromBottom(false);
        mHistoryList.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        isScrolled = true;
        mChatPresenter.loadConversationHistory(mConversationItem.getStringOpponentId(), Integer.toString(dailyHistory.getHistory().getBeforeMessageId()));
    }

    @Override
    public ConversationList.ConversationItem getConversationItem() {
        return mConversationItem;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mailbox_send_message:
                sendMessage();
                break;
            case R.id.mailbox_chat_ab_terminate:
            case R.id.mailbox_chat_ab_back_icon:
                onBackPressed();
                break;
        }
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);

            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    feedback("Failed to create directory");

                    return null;
                }
            }

        } else {
            feedback("External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private File createImageFile() throws IOException {
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(tmpFileName, tmpFileExt, albumF);
        return imageF;
    }

    private File setUpPhotoFile() throws IOException {
        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }
}
