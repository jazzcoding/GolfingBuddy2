package com.golfingbuddy.ui.mailbox.compose;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkBaseActivity;
import com.golfingbuddy.ui.mailbox.AuthorizationInfo;
import com.golfingbuddy.ui.mailbox.Constants;
import com.golfingbuddy.ui.mailbox.compose.ui.ComposeAttachmentAdapter;
import com.golfingbuddy.ui.mailbox.compose.ui.ComposeAutoCompleteView;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;
import com.golfingbuddy.ui.mailbox.mail.MailActivity;
import com.golfingbuddy.ui.mailbox.mail.adapter.MailMessageAttachment;
import com.golfingbuddy.ui.mailbox.mail.image.MailImage;
import com.golfingbuddy.ui.memberships.SubscribeOrBuyActivity;
import com.golfingbuddy.utils.SKRoundedImageView;

import java.util.ArrayList;

/**
 * Created by kairat on 3/18/15.
 */
public class ComposeActivity extends SkBaseActivity implements ComposeView, View.OnClickListener {

    private static final int AUTHORIZE_RESULT = 100;

    private ComposePresenter mPresenter;
    private long mUid;
    private ComposeAutoCompleteView.AutocompleteAdapter mAutoCompleteAdapter;
    private ComposeAutoCompleteView mAutoCompleteView;
    private RecipientInterface mRecipient;
    private ProgressDialog mProcessDialog;

    private GridView mAttachmentGrid;
    private ComposeAttachmentAdapter mAttachmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mailbox_compose);

        mPresenter = new ComposePresenterImpl(this);
        mUid = System.currentTimeMillis() / 1000l;

        setLoadingMode(true);

        AuthorizationInfo.getInstance().getActionInfo(new String[]{"send_message"}, new AuthorizationInfo.ActionInfoReady() {
            @Override
            public void onReady() {
                authorizeAction();
            }
        });

        findViewById(R.id.mailbox_compose_suggest_cancel).setOnClickListener(this);
        View clear = findViewById(R.id.mailbox_compose_suggest_clear);
        clear.setOnClickListener(this);

        mProcessDialog = new ProgressDialog(this);
        mProcessDialog.setMessage(getResources().getString(R.string.mailbox_compose_send_message));
        mProcessDialog.setCancelable(false);

        mAutoCompleteView = (ComposeAutoCompleteView) findViewById(R.id.mailbox_compose_suggest);
        mAutoCompleteAdapter = new ComposeAutoCompleteView.AutocompleteAdapter(this, R.layout.mailbox_compose_auto_complete_item);
        mAutoCompleteView.setThreshold(1);
        mAutoCompleteView.setAdapter(mAutoCompleteAdapter);
        mAutoCompleteView.setHolder(this);
        mAutoCompleteView.setLoadingIndicator((ProgressBar) findViewById(R.id.mailbox_compose_progress_bar));
        mAutoCompleteView.setClear(clear);
        mAutoCompleteView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onSuggestionClick((RecipientInterface) ((ItemHolderInterface) view.getTag()).getData());
            }
        });

        mAttachmentGrid = (GridView) findViewById(R.id.mailbox_compose_attachment_grid);
        mAttachmentAdapter = new ComposeAttachmentAdapter(this, this);
        mAttachmentGrid.setAdapter(mAttachmentAdapter);
    }

    @Override
    public void setLoadingMode(Boolean mode) {
        ActionBar actionBar = getActionBar();

        if (mode != null && mode) {
            findViewById(R.id.mailbox_compose_content).setVisibility(View.GONE);
            findViewById(R.id.mailbox_compose_progress).setVisibility(View.VISIBLE);

            if (actionBar != null) {
                actionBar.hide();
            }
        } else {
            findViewById(R.id.mailbox_compose_content).setVisibility(View.VISIBLE);
            findViewById(R.id.mailbox_compose_progress).setVisibility(View.GONE);

            if (actionBar != null) {
                actionBar.show();
            }
        }

        findViewById(R.id.mailbox_compose_authorize).setVisibility(View.GONE);
    }

    @Override
    public void initActionBar() {
        ActionBar actionBar = getActionBar();

        if ( actionBar != null ) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);

            View customActionBar = View.inflate(this, R.layout.mailbox_compose_action_bar, null);
            customActionBar.findViewById(R.id.mailbox_compose_ab_terminate).setOnClickListener(this);
            customActionBar.findViewById(R.id.mailbox_compose_ab_back_icon).setOnClickListener(this);
            customActionBar.findViewById(R.id.mailbox_compose_ab_send).setOnClickListener(this);

            actionBar.setCustomView(customActionBar, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayShowCustomEnabled(true);

            AuthorizationInfo.ActionInfo info = AuthorizationInfo.getInstance().getActionInfo("send_message");

            if (info == null || info.isAuthorized()) {
                findViewById(R.id.mailbox_compose_ab_attachment).setOnClickListener(this);
                findViewById(R.id.mailbox_compose_ab_send).setOnClickListener(this);
            } else {
                findViewById(R.id.mailbox_compose_ab_attachment).setVisibility(View.GONE);
                findViewById(R.id.mailbox_compose_ab_send).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void authorizeAction() {
        AuthorizationInfo.ActionInfo startAction = AuthorizationInfo.getInstance().getActionInfo("send_message");

        initActionBar();
        setLoadingMode(false);

        if (startAction.isAuthorized()) {
            Intent intent = getIntent();

            if ( intent.hasExtra("recipient") ) {
                onSuggestionClick((RecipientInterface) intent.getParcelableExtra("recipient"));
            } else if (intent.hasExtra("recipientId")) {
                String recipientId = Integer.toString(intent.getIntExtra("recipientId", 0));
                mPresenter.getRecipient(recipientId);
            } else {
                setFocus(mAutoCompleteView);
            }

            return;
        }

        findViewById(R.id.mailbox_compose_content).setVisibility(View.GONE);
        findViewById(R.id.mailbox_compose_authorize).setVisibility(View.VISIBLE);

        if (startAction.isBillingEnable() && startAction.getStatus().equalsName(AuthorizationInfo.STATUS.PROMOTED)) {
            ((ImageView) findViewById(R.id.mailbox_compose_authorize_icon)).setImageResource(R.drawable.lock_enable);
            findViewById(R.id.mailbox_compose_authorize_content).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ComposeActivity.this, SubscribeOrBuyActivity.class);
                    intent.putExtra("pluginKey", "mailbox");
                    intent.putExtra("actionKey", "send_message");

                    startActivityForResult(intent, AUTHORIZE_RESULT);
                }
            });
        }

        ((TextView) findViewById(R.id.mailbox_compose_authorize_message)).setText(startAction.getMessage());
    }

    @Override
    public void terminate() {
        Intent intent = new Intent();
        setResult(MailActivity.COMPOSE_RESULT, intent);
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
    public void successSendFeedback() {
        feedback(R.string.mailbox_mail_send_success);
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
    public void showProcessDialog() {
        mProcessDialog.show();
    }

    @Override
    public void hideProcessDialog() {
        mProcessDialog.hide();
    }

    @Override
    public void setRecipient(Recipient recipient) {
        if (recipient != null) {
            setLoadingMode(false);
            onSuggestionClick(recipient);
        }
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
        if (mRecipient == null) {
            feedback(R.string.mailbox_mail_send_no_user);

            return;
        }

        EditText subjectInput = (EditText) findViewById(R.id.mailbox_compose_subject_input);
        String subject = subjectInput.getText().toString().trim();

        if (subject.isEmpty()) {
            subjectInput.requestFocus();
            feedback(R.string.mailbox_mail_send_no_subject);

            return;
        }

        EditText textInput = (EditText) findViewById(R.id.mailbox_compose_text_input);
        String text = textInput.getText().toString().trim();

        if (text.isEmpty()) {
            textInput.requestFocus();
            feedback(R.string.mailbox_mail_send_no_text);

            return;
        }

        ComposeInterface compose = new Compose();
        compose.setUid(Long.toString(mUid));
        compose.setOpponentId(mRecipient.getOpponentId());
        compose.setSubject(subject);
        compose.setText(text);
        mPresenter.sendMessage(compose);
    }

    @Override
    public void onSendMessage(ConversationList.ConversationItem conversationItem) {
        Intent intent = new Intent();
        intent.putExtra(Constants.CONVERSATION_ITEM, conversationItem);
        setResult(MailActivity.COMPOSE_RESULT, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTHORIZE_RESULT) {
            setLoadingMode(true);
            AuthorizationInfo.getInstance().getActionInfo(new String[]{"send_message"}, new AuthorizationInfo.ActionInfoReady() {
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
            case R.id.mailbox_compose_suggest_cancel:
                onSuggestionCancel();
                break;
            case R.id.mailbox_compose_ab_send:
                sendMessage();
                break;
            case R.id.mailbox_compose_suggest_clear:
                onSuggestionClear();
                break;
        }
    }

    @Override
    public void onAttachmentClick(MailMessageAttachment attachment) {
        //feedback("attach click:TODO");
    }

    @Override
    public void suggestionListRequest(String constraint, ArrayList<String> idList) {
        mPresenter.suggestionListRequest(constraint, idList);
    }

    @Override
    public void onSuggestionListResponse(JsonArray list) {
        mAutoCompleteAdapter.setFilterResult(list);
        mAutoCompleteView.onComplete();
    }

    @Override
    public void onSuggestionClick(RecipientInterface recipient) {
        mRecipient = recipient;
        ViewGroup suggestSelected = (ViewGroup) findViewById(R.id.mailbox_compose_suggest_selected);

        ((TextView) suggestSelected.findViewById(R.id.mailbox_compose_suggest_display_name)).setText(mRecipient.getDisplayName());

        if (mRecipient.getAvatarUrl() != null) {
            ((SKRoundedImageView) suggestSelected.findViewById(R.id.mailbox_compose_suggest_avatar)).setImageUrl(mRecipient.getAvatarUrl());
        }

        findViewById(R.id.mailbox_compose_suggest_input).setVisibility(View.GONE);
        mAutoCompleteView.setText("");
        suggestSelected.setVisibility(View.VISIBLE);

        setFocus((EditText) findViewById(R.id.mailbox_compose_subject_input));
    }

    @Override
    public void onSuggestionCancel() {
        if (mRecipient == null) {
            return;
        }

        findViewById(R.id.mailbox_compose_suggest_selected).setVisibility(View.GONE);
        findViewById(R.id.mailbox_compose_suggest_input).setVisibility(View.VISIBLE);
        mAutoCompleteView.hideClear();
        mAutoCompleteView.requestFocus();
    }

    @Override
    public void onSuggestionClear() {
        mAutoCompleteView.setText("");
        mAutoCompleteView.hideClear();
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
