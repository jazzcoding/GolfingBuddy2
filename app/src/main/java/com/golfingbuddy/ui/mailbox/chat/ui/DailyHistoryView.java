package com.golfingbuddy.ui.mailbox.chat.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.mailbox.AuthorizationInfo;
import com.golfingbuddy.ui.mailbox.chat.ChatView;
import com.golfingbuddy.ui.mailbox.chat.model.ConversationHistory;

/**
 * Created by kairat on 2/25/15.
 */
public class DailyHistoryView extends LinearLayout {
    private ConversationHistory.DailyHistory mDailyHistory;
    private View mView;
    private ChatView mChatView;
    
    public DailyHistoryView(Context context) {
        super(context);
        init();
    }

    public DailyHistoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DailyHistoryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mView = inflate(getContext(), R.layout.mailbox_daily_history, this);
        setWillNotDraw(false);
    }

    public ConversationHistory.DailyHistory getDailyHistory() {
        return mDailyHistory;
    }

    public void setDailyHistory(ConversationHistory.DailyHistory dailyHistory) {
        this.mDailyHistory = dailyHistory;
    }

    public void setChatView(ChatView chatView) {
        mChatView = chatView;
    }

    public void build() {
        if ( mDailyHistory == null || mDailyHistory.getMessages().isEmpty() ) {
            return;
        }

        LinearLayout content = (LinearLayout) mView.findViewById(R.id.mailbox_daily_history);
        content.removeAllViews();
        renderDailyHistory(mDailyHistory, content);
    }

    private void renderDailyHistory(ConversationHistory.DailyHistory dailyHistory, LinearLayout content) {
        for ( int i = 0, j = dailyHistory.getMessages().size(); i < j; i++ ) {
            ConversationHistory.Messages messages = dailyHistory.getMessages().get(i);

            if ( i == 0 ) {
                renderMessages(dailyHistory.getDailyDateTime(), messages, content);
            } else {
                renderMessages(messages.getTime(), messages, content);
            }
        }
    }

    private void renderMessages(String date, ConversationHistory.Messages messages, LinearLayout content) {
        content.addView(createDateView(date));

        for ( ConversationHistory.Messages.UserMessages userMessages : messages.getUserMessages() ) {
            int size = userMessages.getMessages().size();
            HistoryMessageTextView.ORDER order = size == 1 ? HistoryMessageTextView.ORDER.SINGLE : HistoryMessageTextView.ORDER.MIDDLE;

            for ( int i = 0; i < size; i++ ) {
                ConversationHistory.Messages.Message message = userMessages.getMessages().get(i);

                if ( order != HistoryMessageTextView.ORDER.SINGLE ) {
                    if ( i == 0 ) {
                        order = HistoryMessageTextView.ORDER.FIRST;
                    } else if ( i == size - 1 ) {
                        order = HistoryMessageTextView.ORDER.LAST;
                    } else {
                        order = HistoryMessageTextView.ORDER.MIDDLE;
                    }
                }

                if (message.hasAttachments()) {
                    renderAttachments(message, content, order);
                } else if (message.getIsSystem()) {
                    HistoryView view = createMessageSystemView(message, order);
                    view.build();
                    content.addView(view);
                } else {
                    HistoryCustomMessageView view = createMessageView(message, order);
                    view.build();
                    content.addView(view);
                }
            }
        }
    }

    private void renderAttachments(ConversationHistory.Messages.Message message, LinearLayout content, HistoryMessageTextView.ORDER order) {
        int size = message.getAttachments().size();
        HistoryMessageTextView.ORDER _order = message.getAttachments().size() == 1 ? HistoryMessageTextView.ORDER.SINGLE : HistoryMessageTextView.ORDER.MIDDLE;

        for ( int i = 0; i < size; i++ ) {
            if ( order != HistoryMessageTextView.ORDER.SINGLE && _order != HistoryMessageTextView.ORDER.SINGLE ) {
                if ( i == 0 && order == HistoryMessageTextView.ORDER.FIRST ) {
                    _order = HistoryMessageTextView.ORDER.FIRST;
                } else if ( i == size - 1 && order == HistoryMessageTextView.ORDER.LAST) {
                    _order = HistoryMessageTextView.ORDER.LAST;
                } else {
                    order = HistoryMessageTextView.ORDER.MIDDLE;
                }
            }

            ConversationHistory.Messages.Message.Attachment attachment = message.getAttachments().get(i);
            HistoryMessageAttachmentView view = createMessageAttachmentView(attachment, message, order);
            view.setTag(message);
            view.build();
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConversationHistory.Messages.Message tmpMessage = (ConversationHistory.Messages.Message)v.getTag();
                    mChatView.onAttachmentClick(tmpMessage.getAttachments().get(0));
                }
            });

            content.addView(view);
        }
    }

    private HistoryDateView createDateView(String date) {
        HistoryDateView dateView = new HistoryDateView(getContext());
        dateView.setDate(date);
        dateView.build();

        return dateView;
    }

    private HistoryCustomMessageView createMessageView(ConversationHistory.Messages.Message message, HistoryMessageTextView.ORDER order) {
        HistoryCustomMessageView view = new HistoryMessageTextView(getContext());

        view.setMessage(message);
        view.setOrder(order);
        view.setChatView(mChatView);
        view.setTag(message);
        view.setAuthorizationInfo(AuthorizationInfo.getInstance().getActionInfo("read_chat_message"));

        return view;
    }

    private HistoryMessageSystemView createMessageSystemView(ConversationHistory.Messages.Message message, HistoryMessageTextView.ORDER order) {
        HistoryMessageSystemView view = new HistoryMessageSystemView(getContext());
        view.setMessage(message);
        view.setOrder(order);
        view.setChatView(mChatView);
        view.setTag(message);

        return view;
    }

    private HistoryMessageAttachmentView createMessageAttachmentView(ConversationHistory.Messages.Message.Attachment attachment, ConversationHistory.Messages.Message message, HistoryMessageTextView.ORDER order) {
        HistoryMessageAttachmentView attachmentView = new HistoryMessageAttachmentView(getContext());
        attachmentView.setAttachment(attachment);
        attachmentView.setOrder(order);
        attachmentView.setMessage(message);

        return attachmentView;
    }

    public void rebuild() {
        LinearLayout content = (LinearLayout) mView.findViewById(R.id.mailbox_daily_history);

        for (int i = 0; i < content.getChildCount(); i++) {
            View view = content.getChildAt(i);

            if (view instanceof HistoryCustomMessageView) {
                ((HistoryCustomMessageView) view).build();
            }
        }
    }

    public HistoryView getFirstDateView() {
        LinearLayout content = (LinearLayout) mView.findViewById(R.id.mailbox_daily_history);

        for ( int i = 0; i < content.getChildCount(); i++ ) {
            View view = content.getChildAt(i);

            if ( view instanceof HistoryDateView ) {
                return (HistoryView)view;
            }
        }

        return null;
    }

    public HistoryView getFirstMessageView() {
        LinearLayout content = (LinearLayout) mView.findViewById(R.id.mailbox_daily_history);

        for ( int i = 0; i < content.getChildCount(); i++ ) {
            View view = content.getChildAt(i);

            if ( view instanceof HistoryMessageTextView || view instanceof HistoryMessageAttachmentView) {
                return (HistoryView)view;
            }
        }

        return null;
    }
}
