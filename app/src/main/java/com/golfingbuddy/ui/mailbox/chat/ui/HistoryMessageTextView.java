package com.golfingbuddy.ui.mailbox.chat.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.mailbox.chat.model.ConversationHistory;

/**
 * Created by kairat on 2/26/15.
 */
public class HistoryMessageTextView extends HistoryCustomMessageView {
    public HistoryMessageTextView(Context context) {
        super(context);
    }

    public HistoryMessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HistoryMessageTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void init() {
        if (mView != null) {
            replaceView();
        } else {
            mView = inflate(getContext(), R.layout.mailbox_history_message, this);
            setWillNotDraw(false);

            super.init();
        }
    }

    public ConversationHistory.Messages.Message getMessage() {
        return mMessage;
    }

    public void setMessage(ConversationHistory.Messages.Message message) {
        mMessage = message;
    }

    public Boolean build() {
        if (!super.build()) {
            return false;
        }

        TextView messageView = (TextView) mView.findViewById(R.id.mailbox_history_message);
        messageView.setText(mMessage.getText().replaceAll("(?i)<br />", ""));
        messageView.setTextColor(mMessage.getIsAuthor() ? Color.WHITE : Color.BLACK);

        return true;
    }
}
