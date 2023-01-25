package com.golfingbuddy.ui.mailbox.chat;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkBaseActivity;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.mailbox.Constants;
import com.golfingbuddy.ui.mailbox.conversation_list.MailboxFragment;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;
import com.golfingbuddy.utils.SKRoundedImageView;
import com.golfingbuddy.utils.SkApi;
import com.squareup.picasso.Callback;

import java.util.HashMap;

/**
 * Created by kairat on 2/20/15.
 */
public class WinkActivity extends SkBaseActivity implements View.OnClickListener{

    private ConversationList.ConversationItem mConversationItem;
    private Integer winkId;

    private View acceptButton, ignoreButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wink_conversation_view);

        Intent intent = getIntent();
        mConversationItem = intent.getParcelableExtra(Constants.CONVERSATION_ITEM);
        winkId = mConversationItem.getConversationId();

        TextView mDateView = (TextView) findViewById(R.id.winks_history_list_date), textView = (TextView) findViewById(R.id.winks_history_list_text);
        mDateView.setText(mConversationItem.getDate());
        textView.setText(mConversationItem.getPreviewText());

        acceptButton = findViewById(R.id.accept_button);
        acceptButton.setOnClickListener(this);

        ignoreButton = findViewById(R.id.ignore_button);
        ignoreButton.setOnClickListener(this);
        initActionBar();
    }

    @Override
    public void onClick(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        final HashMap<String, String> params = new HashMap();
        Integer id = mConversationItem.getConversationId(), userId = getApp().getUserInfo().getUserId();
        params.put("winkId", id.toString());
        params.put("userId", userId.toString());
        final Intent intent;

       switch ( view.getId() )
       {
           case R.id.accept_button:
               vibe.vibrate(10);
               BaseServiceHelper.getInstance(getApplication()).runRestRequest(BaseRestCommand.ACTION_TYPE.ACCEPT_WINK, params, new SkServiceCallbackListener() {
                   @Override
                   public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                       JsonObject result = SkApi.processResult(data);
                       if(SkApi.propExistsAndNotNull(result, "cId"))
                       {
                           ConversationList.ConversationItem item = WinkActivity.this.mConversationItem;

                           Intent intent = new Intent(WinkActivity.this, ChatActivity.class);
                           item.setConversationId(result.get("cId").getAsInt());
                           item.setLastMsgTimestamp(item.getLastMsgTimestamp()-3600*240);

                           intent.putExtra(Constants.CONVERSATION_ITEM, item);

                           Intent returnIntent = WinkActivity.this.getIntent();
                           returnIntent.putExtra("winkId", winkId);
                           WinkActivity.this.setResult(MailboxFragment.WINK_RESULT, returnIntent);

                           finish();
                           startActivity(intent);
                       }
                   }
               });

               break;

           case R.id.ignore_button:
               vibe.vibrate(10);
               BaseServiceHelper.getInstance(getApplication()).runRestRequest(BaseRestCommand.ACTION_TYPE.IGNORE_WINK, params);

               Intent returnIntent = WinkActivity.this.getIntent();
               returnIntent.putExtra("winkId", winkId);
               WinkActivity.this.setResult(MailboxFragment.WINK_RESULT, returnIntent);

               finish();
               break;
           default:
               onBackPressed();
       }
    }

    private void initView() {
//
//
//        mAdapter = new ConversationHistoryAdapter(this, this);
//        mHistoryList = (ListView) findViewById(R.id.mailbox_history_list);
//        mHistoryList.setAdapter(mAdapter);
//
//        mMessageEditor = (EditText) findViewById(R.id.mailbox_message_text);
//        mSendMessage = (ImageView) findViewById(R.id.mailbox_send_message);
//
//        initEventListener();
    }

    private void initEventListener() {
//        mHistoryList.setOnScrollListener(this);
//        mMessageEditor.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                mSendMessage.setImageResource(s.toString().trim().isEmpty() ? R.drawable.ic_sendphoto : R.drawable.ic_send_disable);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });
//        mMessageEditor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus && checkReplyAction()) {
//                    InputMethodManager methodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                    methodManager.hideSoftInputFromWindow(mMessageEditor.getWindowToken(), 0);
//                }
//            }
//        });
//        mSendMessage.setOnClickListener(this);
    }



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
    }




}
