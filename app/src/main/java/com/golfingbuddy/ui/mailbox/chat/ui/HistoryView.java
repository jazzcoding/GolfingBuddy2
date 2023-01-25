package com.golfingbuddy.ui.mailbox.chat.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.mailbox.Constants;
import com.golfingbuddy.ui.mailbox.chat.ChatView;
import com.golfingbuddy.ui.mailbox.chat.model.ConversationHistory;

/**
 * Created by kairat on 2/27/15.
 */
public abstract class HistoryView extends LinearLayout {
    public enum ORDER {
        SINGLE, FIRST, MIDDLE, LAST
    }

    protected ChatView mChatView;
    protected View mView;
    protected ORDER mOrder;
    protected ConversationHistory.Messages.Message mMessage;

    protected HistoryView(Context context) {
        super(context);
    }

    protected HistoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected HistoryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void init() {
        if ( mMessage.getIsAuthor() ) {
            ((LinearLayout) mView.findViewById(R.id.mailbox_history_message_root)).setGravity(Gravity.RIGHT);
        }

        mView.findViewById(R.id.mailbox_history_message_content).setBackgroundResource(getBackgroundShape());
    }

    public ChatView getChatView() {
        return mChatView;
    }

    public void setChatView(ChatView chatView) {
        mChatView = chatView;
    }

    public View getView() {
        return mView;
    }

    public void setView(View view) {
        mView = view;
    }

    public ORDER getOrder() {
        return mOrder;
    }

    public void setOrder(ORDER order) {
        mOrder = order;
    }

    public ConversationHistory.Messages.Message getMessage() {
        return mMessage;
    }

    public void setMessage(ConversationHistory.Messages.Message message) {
        mMessage = message;
    }


    protected Boolean build() {
        return mMessage != null;
    }

    public int getBackgroundShape() {
        String background;

        switch ( mOrder ) {
            case FIRST:
                background = Constants.MAILBOX_MESSAGE_FIRST;
                break;
            case MIDDLE:
                background = Constants.MAILBOX_MESSAGE_MIDDLE;
                break;
            case LAST:
                background = Constants.MAILBOX_MESSAGE_LAST;
                break;
            case SINGLE:
            default:
                background = Constants.MAILBOX_MESSAGE_SINGLE;
                break;
        }

        if ( mMessage.getIsAuthor() ) {
            background += Constants.MAILBOX_AUTHOR_POSTFIX;
        }

        return getResources().getIdentifier(background, "drawable", getContext().getPackageName());
    }
}
