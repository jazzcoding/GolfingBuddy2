package com.golfingbuddy.ui.mailbox.reply;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.golfingbuddy.R;
import com.golfingbuddy.core.SkBaseActivity;
import com.golfingbuddy.ui.mailbox.AuthorizationInfo;
import com.golfingbuddy.ui.mailbox.Constants;
import com.golfingbuddy.ui.mailbox.chat.model.ConversationHistory;
import com.golfingbuddy.ui.mailbox.compose.ui.ComposeAttachmentAdapter;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;
import com.golfingbuddy.ui.mailbox.mail.MailActivity;
import com.golfingbuddy.ui.mailbox.mail.adapter.MailMessageAttachment;
import com.golfingbuddy.ui.mailbox.mail.image.MailImage;
import com.golfingbuddy.ui.memberships.SubscribeOrBuyActivity;

/**
 * Created by kairat on 3/18/15.
 */
public class ReplyActivity extends SkBaseActivity implements ReplyView, View.OnClickListener {

    private static final int AUTHORIZE_RESULT = 100;

    private ReplyPresenter mPresenter;
    private long mUid;
    private ConversationList.ConversationItem mConversationItem;

    private GridView mAttachmentGrid;
    private ComposeAttachmentAdapter mAttachmentAdapter;
    private ProgressDialog mProcessDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mailbox_reply);

        mPresenter = new ReplyPresenterImpl(this);
        mUid = System.currentTimeMillis() / 1000l;

        mAttachmentGrid = (GridView) findViewById(R.id.mailbox_reply_attachment_grid);
        mAttachmentAdapter = new ComposeAttachmentAdapter(this, this);
        mAttachmentGrid.setAdapter(mAttachmentAdapter);

        Intent intent = getIntent();

        if (intent.hasExtra(Constants.CONVERSATION_ITEM)) {
            mConversationItem = intent.getParcelableExtra(Constants.CONVERSATION_ITEM);
        } else {
            throw new IllegalArgumentException("Conversation Item required");
        }

        mProcessDialog = new ProgressDialog(this);
        mProcessDialog.setMessage(getString(R.string.mailbox_send_message));
        mProcessDialog.setCancelable(false);

        AuthorizationInfo.getInstance().getActionInfo(new String[]{"reply_to_message"}, new AuthorizationInfo.ActionInfoReady() {
            @Override
            public void onReady() {
                authorizeAction();
            }
        });
    }

    @Override
    public void terminate() {
        Intent intent = new Intent();
        intent.putExtra(Constants.CONVERSATION_ITEM, mConversationItem);
        setResult(MailActivity.REPLY_RESULT, intent);
        finish();
    }

    private void feedback(int resourceId) {
        feedback(getResources().getString(resourceId));
    }

    private void feedback(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void failCreateDirectory() {
        feedback("Failed to create directory");
    }

    @Override
    public void failPermissionDirectory() {
        feedback("External storage is not mounted READ/WRITE.");
    }

    @Override
    public void fileNotFound() {
        feedback("File not found.");
    }

    @Override
    public Cursor getCursor(Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        return getContentResolver().query(
                uri, filePathColumn, null, null, null);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public long getUid() {
        return mUid;
    }

    @Override
    public void onResponseError(String errorMsg) {
        feedback((errorMsg == null || errorMsg.isEmpty()) ? "Server error" : errorMsg);
    }

    @Override
    public void startActivity(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void initActionBar() {
        ActionBar actionBar = getActionBar();

        if ( actionBar == null ) {
            return;
        }

        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        View customActionBar = View.inflate(this, R.layout.mailbox_compose_action_bar, null);
        customActionBar.findViewById(R.id.mailbox_compose_ab_terminate).setOnClickListener(this);
        customActionBar.findViewById(R.id.mailbox_compose_ab_send).setOnClickListener(this);

        Resources resources = getResources();
        ((TextView) customActionBar.findViewById(R.id.mailbox_compose_ab_caption)).setText(String.format(resources.getString(R.string.mailbox_reply_title), mConversationItem.getDisplayName()));
        actionBar.setCustomView(customActionBar, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        actionBar.setDisplayShowCustomEnabled(true);

        AuthorizationInfo.ActionInfo info = AuthorizationInfo.getInstance().getActionInfo("reply_to_message");

        if (info == null || info.isAuthorized()) {
            findViewById(R.id.mailbox_compose_ab_attachment).setOnClickListener(this);
            findViewById(R.id.mailbox_compose_ab_send).setOnClickListener(this);
            findViewById(R.id.mailbox_compose_ab_back_icon).setOnClickListener(this);
        } else {
            findViewById(R.id.mailbox_compose_ab_attachment).setVisibility(View.GONE);
            findViewById(R.id.mailbox_compose_ab_send).setVisibility(View.GONE);
        }
    }

    @Override
    public void authorizeAction() {
        AuthorizationInfo.ActionInfo info = AuthorizationInfo.getInstance().getActionInfo("reply_to_message");

        if (info != null && !info.isAuthorized()) {
            findViewById(R.id.mailbox_reply_authorize).setVisibility(View.VISIBLE);

            if (info.isBillingEnable() && info.getStatus().equalsName(AuthorizationInfo.STATUS.PROMOTED)) {
                ((ImageView) findViewById(R.id.mailbox_reply_authorize_icon)).setImageResource(R.drawable.lock_enable);
                findViewById(R.id.mailbox_reply_authorize_content).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ReplyActivity.this, SubscribeOrBuyActivity.class);
                        intent.putExtra("pluginKey", "mailbox");
                        intent.putExtra("actionKey", "reply_to_message");

                        startActivityForResult(intent, AUTHORIZE_RESULT);
                    }
                });
            }

            ((TextView) findViewById(R.id.mailbox_reply_authorize_message)).setText(info.getMessage());
        } else {
            findViewById(R.id.mailbox_reply_content).setVisibility(View.VISIBLE);
            findViewById(R.id.mailbox_reply_progress).setVisibility(View.GONE);
            setFocus((EditText) findViewById(R.id.mailbox_reply_text_input));
        }

        initActionBar();
    }

    @Override
    public void showPleaseWaite() {
        mProcessDialog.show();
    }

    @Override
    public void hidePleaseWaite() {
        mProcessDialog.dismiss();
    }

    @Override
    public void prepareSlotForAttachment(int slotId) {
        mAttachmentAdapter.addSlot(slotId);
        mAttachmentAdapter.notifyDataSetChanged();
    }

    @Override
    public void destroySlotForAttachment(int slotId) {
        mAttachmentAdapter.removeSlot(slotId);
        mAttachmentAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateSlotForAttachment(int slotId, MailImage.Message.Attachment attachment) {
        mAttachmentAdapter.updateSlot(slotId, attachment);
        mAttachmentAdapter.notifyDataSetChanged();
    }

    @Override
    public void deleteSlotForAttachment(MailImage.Message.Attachment attachment) {
        mPresenter.deleteAttachment(attachment);
        mAttachmentAdapter.removeSlot(attachment);
        mAttachmentAdapter.notifyDataSetChanged();
    }

    @Override
    public void sendMessage() {
        EditText textInput = (EditText) findViewById(R.id.mailbox_reply_text_input);
        String text = textInput.getText().toString().trim();

        if (text.isEmpty()) {
            textInput.requestFocus();
            feedback(R.string.mailbox_mail_send_no_text);

            return;
        }

        ReplyInterface reply = new Reply();
        reply.setUid(Long.toString(mUid));
        reply.setOpponentId(Integer.toString(mConversationItem.getOpponentId()));
        reply.setConversationId(Integer.toString(mConversationItem.getConversationId()));
        reply.setText(text);
        mPresenter.sendMessage(reply);
    }

    @Override
    public void onSendMessage(ConversationHistory.Messages.Message message) {
        Intent intent = new Intent();
        intent.putExtra(Constants.CONVERSATION_ITEM, mConversationItem);
        intent.putExtra("reply", message);
        setResult(MailActivity.REPLY_RESULT, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTHORIZE_RESULT) {
            AuthorizationInfo.getInstance().getActionInfo(new String[]{"reply_to_message"}, new AuthorizationInfo.ActionInfoReady() {
                @Override
                public void onReady() {
                    authorizeAction();
                }
            });
        } else {
            mPresenter.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        terminate();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mailbox_compose_ab_terminate:
            case R.id.mailbox_compose_ab_back_icon:
                terminate();
                break;
            case R.id.mailbox_compose_ab_attachment:
                mPresenter.attachmentClick();
                break;
            case R.id.mailbox_compose_ab_send:
                sendMessage();
                break;
        }
    }

    @Override
    public void onAttachmentClick(MailMessageAttachment attachment) {
        //feedback("attach click:TODO");
    }

    private void setFocus(EditText editText) {
        if (editText == null) {
            return;
        }

        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }
}
