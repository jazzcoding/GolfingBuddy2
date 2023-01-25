package com.golfingbuddy.ui.mailbox.conversation_list.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.mailbox.conversation_list.MailboxView;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationItemInterface;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;
import com.golfingbuddy.ui.mailbox.RenderInterface;
import com.golfingbuddy.utils.SKImageView;
import com.squareup.picasso.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by kairat on 2/13/15.
 */
public class ConversationListAdapter extends ArrayAdapter<ConversationList.ConversationItem> {

    private ConversationList mConversationList;
    private LayoutInflater mInflater;
    private MailboxView mMailboxView;
    private SelectedConversationItems mSelectedItem;

    public ConversationListAdapter(Context context, MailboxView mailboxView) {
        super(context, R.layout.mailbox_conversation_list_item);
        mMailboxView = mailboxView;

        mConversationList = new ConversationList();
        mInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSelectedItem = new SelectedConversationItems();
    }

    public void setConversation(ConversationList conversationList) throws ConversationListException {
        if ( conversationList == null || conversationList.isEmpty() ) {
            throw new ConversationListException();
        }

        for (ConversationList.ConversationItem item : conversationList.getList()) {
            mConversationList.addOrUpdateConversationItem(item);
        }

        Collections.sort(mConversationList.getList());
    }

    public void insert(ConversationList.ConversationItem item) {
        mConversationList.addOrUpdateConversationItem(item);
        Collections.sort(mConversationList.getList());
    }

    public void delete(ConversationList.ConversationItem item, Boolean notify) {
        if (item == null) {
            return;
        }

        mConversationList.getList().remove(item);

        if (notify != null && notify) {
            notifyDataSetChanged();
        }
    }

    public void delete(int id, Boolean notify) {
        for( ConversationList.ConversationItem cItem : mConversationList.getList())
        {
            if( cItem.getConversationId() == id)
            {
                delete(cItem, notify);
                return;
            }
        }
    }

    public void delete(ConversationList.ConversationItem item) {
        delete(item, true);
    }

    public void deleteById(Integer id) {
        delete(id, true);
    }

    public ConversationList.ConversationItem findConversationItem(ConversationList.ConversationItem item) {
        return mConversationList.findConversation(item);
    }

    @Override
    public int getCount() {
        return mConversationList.size();
    }

    @Override
    public ConversationList.ConversationItem getItem(int position) {
        return mConversationList.getList().get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View conversationView = convertView;

        if (conversationView == null) {
            conversationView = mInflater.inflate(R.layout.mailbox_conversation_list_item, null);

            ConversationHolder holder = new ConversationHolder(getItem(position));
            holder.setView(conversationView);

            conversationView.setTag(holder);
        } else {
            ConversationHolder holder = (ConversationHolder) conversationView.getTag();
            holder.setItem(getItem(position));
        }

        ((RenderInterface) conversationView.getTag()).render();

        return conversationView;
    }

    @Override
    public boolean isEmpty() {
        return mConversationList.isEmpty();
    }

    public ArrayList<ConversationList.ConversationItem> getList() {
        return mConversationList.getList();
    }

    public void setList(ArrayList<ConversationList.ConversationItem> list) {
        mConversationList.setList(list);
    }

    public Boolean isSelected(ConversationList.ConversationItem item) {
        return mSelectedItem.isSelected(item);
    }

    public Boolean hasSelected() {
        return !mSelectedItem.getSelectedItems().isEmpty();
    }

    public SelectedConversationItems getSelectedItems() {
        return mSelectedItem;
    }

    public void clearSelectedList() {
        mSelectedItem.clear();
    }

    public void setAsReadSelectedItems() {
        if (!hasSelected()) {
            return;
        }

        for (ConversationList.ConversationItem item : mSelectedItem.getSelectedItems().values()) {
            item.markAsRead();
            item.setNewMsgCount(0);
        }
    }

    public void setUnReadSelectedItems() {
        if (!hasSelected()) {
            return;
        }

        for (ConversationList.ConversationItem item : mSelectedItem.getSelectedItems().values()) {
            item.markUnRead();
        }
    }

    public void deleteSelectedItems() {
        if (!hasSelected()) {
            return;
        }

        for (ConversationList.ConversationItem item : mSelectedItem.getSelectedItems().values()) {
            delete(item, false);
        }
    }

    public class ConversationHolder implements ConversationItemInterface {
        private View mView;
        private SKImageView mAvatar;
        private TextView mDisplayName;
        private TextView mSubject;
        private TextView mPreviewText;
        private TextView mData;
        private ImageView mIndicator;
        private View mHasAttach, panel;

        private ConversationList.ConversationItem mConversationItem;
        private View.OnClickListener mClickHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch ( v.getId() ) {
                    case R.id.mailbox_conversation_action:
                        ConversationListAdapter.this.mMailboxView.onConversationContextClick(ConversationHolder.this);
                        break;
                    case R.id.mailbox_conversation_info:
                        ConversationListAdapter.this.mMailboxView.onConversationItemClick(ConversationHolder.this);
                        break;
                    case R.id.mailbox_conversation_avatar:
                        ConversationListAdapter.this.mMailboxView.onConversationAvatarClick(ConversationHolder.this);
                        break;
                }
            }
        };
        private Callback mCallback = new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                ConversationHolder.this.mAvatar.setImageResource(R.drawable.rounded_rectangle_2_copy_2);
            }
        };

        public ConversationHolder(ConversationList.ConversationItem item) {
            mConversationItem = item;
        }

        public ConversationList.ConversationItem getConversation() {
            return mConversationItem;
        }

        public void setConversation(ConversationList.ConversationItem conversationItem) {
            mConversationItem = conversationItem;
        }

        @Override
        public View getView() {
            return mView;
        }

        @Override
        public void setView(View view) {
            this.mView = view;
            initView();
        }

        private void initView() {
            if ( mView == null ) {
                return;
            }

            mAvatar = (SKImageView) mView.findViewById(R.id.mailbox_conversation_avatar);
            mDisplayName = (TextView) mView.findViewById(R.id.mailbox_conversation_displayname);
            mSubject = (TextView) mView.findViewById(R.id.mailbox_conversation_subject);
            mPreviewText = (TextView) mView.findViewById(R.id.mailbox_conversation_preview);
            mData = (TextView) mView.findViewById(R.id.mailbox_conversation_date);
            mIndicator = (ImageView) mView.findViewById(R.id.mailbox_conversation_new_indicator);
            mHasAttach = mView.findViewById(R.id.mailbox_conversation_has_attach);
            panel = mView.findViewById(R.id.mailbox_conversation_action_panel);
        }

        @Override
        public void render() {
            if ( mConversationItem == null || mView == null ) {
                return;
            }

            mAvatar.setImageUrl(mConversationItem.getAvatarUrl(), mCallback);
            mDisplayName.setText(mConversationItem.getDisplayName());

            mSubject.setText(mConversationItem.getSubject());
            mPreviewText.setText(mConversationItem.getPreviewText());
            mData.setText(mConversationItem.getDate());

            setSelected(isSelected(mConversationItem));

            resetViewByMode();
            buildConversation();
            initEventListener();
        }

        private void resetViewByMode() {
            if (mMailboxView.isSingleMode()) {
                FrameLayout.LayoutParams indicatorParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                indicatorParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                mIndicator.setLayoutParams(indicatorParams);
                mIndicator.setVisibility(View.GONE);

                FrameLayout.LayoutParams dateParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dateParams.gravity = Gravity.TOP | Gravity.RIGHT;
                mData.setLayoutParams(dateParams);
            } else {
                FrameLayout.LayoutParams indicatorParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                indicatorParams.gravity = Gravity.TOP | Gravity.RIGHT;
                mIndicator.setLayoutParams(indicatorParams);
                mIndicator.setVisibility(View.VISIBLE);

                FrameLayout.LayoutParams dateParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dateParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                mData.setLayoutParams(dateParams);
            }
        }

        private void buildConversation() {
            mView.findViewById(R.id.mailbox_conversation_online).setVisibility(mConversationItem.getIsOnline() ? View.VISIBLE : View.GONE);

            if( mConversationItem.getConversationId() < 0 )
            {
                mDisplayName.setTextAppearance(getContext(), R.style.mailbox_new_subject);
                mPreviewText.setTextAppearance(getContext(), R.style.mailbox_new);
                mPreviewText.setText(getContext().getResources().getString(R.string.mailbox_conversation_list_wink));
                mSubject.setVisibility(View.GONE);
                mIndicator.setVisibility(View.VISIBLE);
                mIndicator.setImageResource(mConversationItem.getIsRead() ? R.drawable.ic_wink : R.drawable.wink_new);
                mData.setVisibility(View.GONE);
                panel.setVisibility(View.GONE);
            }
            else if (mConversationItem.getIsWink()) {
                mDisplayName.setTextAppearance(getContext(), R.style.mailbox_new_subject);
                mPreviewText.setTextAppearance(getContext(), R.style.mailbox_new);
                mSubject.setVisibility(View.GONE);
                mPreviewText.setText(getContext().getResources().getString(R.string.mailbox_conversation_list_wink));
                mIndicator.setVisibility(View.VISIBLE);
                mIndicator.setImageResource(mConversationItem.getIsRead() ? R.drawable.ic_wink : R.drawable.wink_new);
                panel.setVisibility(View.VISIBLE);
            } else {
                if (!mConversationItem.getIsRead() || mConversationItem.getNewMsgCount() > 0) {
                    mDisplayName.setTextAppearance(getContext(), R.style.mailbox_new_subject);
                    mSubject.setTextAppearance(getContext(), R.style.mailbox_new);
                    mPreviewText.setTextAppearance(getContext(), R.style.mailbox_new);

                    mIndicator.setImageResource(mConversationItem.isMailMode() ? R.drawable.ic_mail_new : R.drawable.ic_msg_new);

                    if (mConversationItem.getNewMsgCount() > 0) {
                        mDisplayName.setText(getContext().getResources().getString(
                                R.string.mailbox_conversation_list_display_name,
                                mConversationItem.getDisplayName(),
                                mConversationItem.getNewMsgCount()
                        ));
                    }
                } else {
                    mDisplayName.setTextAppearance(getContext(), R.style.mailbox_old_subject);
                    mSubject.setTextAppearance(getContext(), R.style.mailbox_old);
                    mPreviewText.setTextAppearance(getContext(), R.style.mailbox_old);

                    mIndicator.setImageResource(mConversationItem.isMailMode() ? R.drawable.ic_mail : R.drawable.ic_msg);
                }

                mSubject.setVisibility(mConversationItem.isMailMode() ? View.VISIBLE : View.GONE);
            }

            mHasAttach.setVisibility(mConversationItem.getHasAttachment() ? View.VISIBLE : View.GONE);
        }

        private void initEventListener() {
            mView.findViewById(R.id.mailbox_conversation_action).setOnClickListener(mClickHandler);
            mView.findViewById(R.id.mailbox_conversation_info).setOnClickListener(mClickHandler);
            mView.findViewById(R.id.mailbox_conversation_avatar).setOnClickListener(mClickHandler);
        }

        @Override
        public ConversationList.ConversationItem getItem() {
            return mConversationItem;
        }

        @Override
        public void setItem(ConversationList.ConversationItem item) {
            if (item != null) {
                mConversationItem = item;
            }
        }

        @Override
        public void setSelected(Boolean selected) {
            if (selected != null && selected) {
                mView.findViewById(R.id.mailbox_conversation_mark).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.mailbox_conversation_action_panel).setVisibility(View.GONE);
                mAvatar.setAlpha(0.6f);
                mSelectedItem.add(mConversationItem);
            } else {
                mView.findViewById(R.id.mailbox_conversation_mark).setVisibility(View.INVISIBLE);
                mView.findViewById(R.id.mailbox_conversation_action_panel).setVisibility(View.VISIBLE);
                mAvatar.setAlpha(1f);
                mSelectedItem.remove(mConversationItem);
            }
        }
    }

    public class ConversationListException extends Exception {
        public ConversationListException() {
            super("ConversationList list is empty!");
        }
    }

    public class SelectedConversationItems {

        private HashMap<String, ConversationList.ConversationItem> mList;

        public SelectedConversationItems() {
            mList = new HashMap<>();
        }

        public HashMap<String, ConversationList.ConversationItem> getSelectedItems() {
            return mList;
        }

        public void clear() {
            mList.clear();
        }

        public void add(ConversationList.ConversationItem item) {
            if (item == null || isSelected(item)) {
                return;
            }

            mList.put(item.getStrConversationId(), item);
        }

        public void remove(ConversationList.ConversationItem item) {
            if (item == null || !isSelected(item)) {
                return;
            }

            Iterator<Map.Entry<String, ConversationList.ConversationItem>> iterator = mList.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, ConversationList.ConversationItem> entry = iterator.next();

                if (entry.getValue().getConversationId() == item.getConversationId()) {
                    iterator.remove();

                    break;
                }
            }
        }

        public Boolean isSelected(ConversationList.ConversationItem item) {
            return mList.containsKey(item.getStrConversationId());
        }

        public int size() {
            return mList.size();
        }

        public String[] getSelectedIdList() {
            return mList.keySet().toArray(new String[mList.size()]);
        }
    }
}
