package com.golfingbuddy.ui.mailbox.chat.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.mailbox.RenderInterface;
import com.golfingbuddy.ui.mailbox.chat.ChatView;
import com.golfingbuddy.ui.mailbox.chat.model.ConversationHistory;
import com.golfingbuddy.ui.mailbox.chat.model.DailyHistory;

/**
 * Created by kairat on 2/20/15.
 */
public class ConversationHistoryAdapter extends ArrayAdapter<ConversationHistory.DailyHistory> {
    private LayoutInflater mInflater;
    private ChatView mChatView;
    private ConversationHistory mConversationHistory;

    public ConversationHistoryAdapter(Context context, ChatView chatView) {
        super(context, R.layout.mailbox_conversation_view);

        mInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mChatView = chatView;
        mConversationHistory = new ConversationHistory();
    }

    public void setConversation(ConversationHistory conversationHistory) throws ConversationException {
        if (conversationHistory == null || conversationHistory.isEmpty()) {
            throw new ConversationException();
        }

        mConversationHistory.merge(conversationHistory);
    }

    public Boolean prependConversation(ConversationHistory conversationHistory) {
        return mConversationHistory.prependMerge(conversationHistory);
    }

    public void appendFakeAttachment(int id) {
//        ConversationHistory.Messages.Message message = new ConversationHistory.Messages.Message();
//        ConversationHistory.Messages.Message.Attachment attachment = new ConversationHistory.Messages.Message.Attachment();
//        attachment.setType("fake");
//        message.addAttachment(attachment);
//        mConversationHistory.findMessage()getLastDailyHistory().addMessage();
    }

    @Override
    public void add(ConversationHistory.DailyHistory object) {
        mConversationHistory.addHistory(object);
    }

    @Override
    public int getCount() {
        return mConversationHistory.size();
    }

    @Override
    public ConversationHistory.DailyHistory getItem(int position) {
        return mConversationHistory.getHistory(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View conversationView = convertView;

        if (conversationView == null) {
            conversationView = mInflater.inflate(R.layout.mailbox_history_item, null);
            HistoryHolder holder = new HistoryHolder(getItem(position));
            holder.setView(conversationView);
            holder.setChatView(mChatView);
            conversationView.setTag(holder);
        } else {
            HistoryHolder holder = (HistoryHolder) conversationView.getTag();
            holder.setHistory(getItem(position));
            holder.setIsBuild(false);
        }

        ((RenderInterface) conversationView.getTag()).render();

        return conversationView;
    }

    public ConversationHistory.Messages.Message findMessage(ConversationHistory.Messages.Message message) {
        return mConversationHistory.findMessage(message);
    }

    public void updateMessage(ConversationHistory.Messages.Message message) {
        ConversationHistory.Messages.Message tmpMessage = findMessage(message);

        if (tmpMessage == null) {
            return;
        }

        tmpMessage.update(message);
    }

    public ConversationHistory getConversationHistory() {
        return mConversationHistory;
    }

    public class ConversationException extends Exception {
        public ConversationException() {
            super("ConversationList is empty!");
        }
    }

    private class HistoryHolder implements DailyHistory {
        private View mView;
        private DailyHistoryView mHistoryView;
        private ChatView mChatView;

        private ConversationHistory.DailyHistory mHistory;

        private Boolean mIsBuild = true;

        public HistoryHolder(ConversationHistory.DailyHistory dailyHistory) {
            mHistory = dailyHistory;
            mIsBuild = false;
        }

        @Override
        public void render() {
            if (mView == null) {
                return;
            }

            if (!mIsBuild) {
                mHistoryView.setChatView(mChatView);
                mHistoryView.build();
                mIsBuild = true;
            }
        }

        private void initView() {
            if (mView == null || mHistory == null) {
                return;
            }

            mHistoryView = (DailyHistoryView) mView.findViewById(R.id.mailbox_history_item);
            mHistoryView.setDailyHistory(mHistory);
        }

        @Override
        public View getView() {
            return mHistoryView;
        }

        @Override
        public void setView(View view) {
            if (view != null) {
                mView = view;
            }

            initView();
        }

        @Override
        public ConversationHistory.DailyHistory getHistory() {
            return mHistory;
        }

        @Override
        public Boolean addHistory(ConversationHistory.DailyHistory dailyHistory) {
            if (!mHistory.isRelatives(dailyHistory)) {
                return false;
            }

            mHistory.merge(dailyHistory);

            return true;
        }

        @Override
        public void rebuild() {
            if (mHistoryView != null) {
                mHistoryView.rebuild();
            }
        }

        public void setHistory(ConversationHistory.DailyHistory history) {
            mHistory = history;
            initView();
        }

        public void setChatView(ChatView chatView) {
            mChatView = chatView;
        }

        public Boolean getIsBuild() {
            return mIsBuild;
        }

        public void setIsBuild(Boolean isBuild) {
            this.mIsBuild = isBuild;
        }
    }
}
