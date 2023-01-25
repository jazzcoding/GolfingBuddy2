package com.golfingbuddy.ui.mailbox.mail.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.model.base.classes.SkUser;
import com.golfingbuddy.ui.mailbox.AuthorizationInfo;
import com.golfingbuddy.ui.mailbox.compose.ui.ComposeAttachmentAdapter;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;
import com.golfingbuddy.ui.mailbox.mail.image.MailImage;
import com.golfingbuddy.ui.mailbox.mail.MailView;
import com.golfingbuddy.utils.SKRoundedImageView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by kairat on 3/14/15.
 */
public class MailListAdapter extends ArrayAdapter<MailImage.Message> {

    private Context mContext;
    private LayoutInflater mInflater;
    private MailView mMailView;
    private ArrayList<MailImage.Message> mMessages;
    private ArrayList<String> mExpandList;

    public MailListAdapter(Context context, MailView mailView) {
        super(context, R.layout.mailbox_mail);

        mContext = context;
        mMailView = mailView;
        mInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMessages = new ArrayList<>();
        mExpandList = new ArrayList<>();
    }

    @Override
    public void addAll(Collection<? extends MailImage.Message> collection) {
        if (collection == null || collection.isEmpty()) {
            return;
        }

        Boolean isExist;

        for (MailImage.Message message : collection) {
            isExist = false;

            for (MailImage.Message tmpMessage : mMessages) {
                if (tmpMessage.getId() == message.getId()) {
                    isExist = true;

                    break;
                }
            }

            if (!isExist) {
                mMessages.add(message);
            }
        }

        Collections.sort(mMessages);
    }

    @Override
    public void add(MailImage.Message object) {
        if (object == null || mMessages.contains(object)) {
            return;
        }

        mMessages.add(object);
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public MailImage.Message getItem(int position) {
        return mMessages.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View conversationView = convertView;

        if ( conversationView == null ) {
            conversationView = mInflater.inflate(R.layout.mailbox_mail_message, null);
            Holder holder = new Holder(getItem(position));
            holder.setView(conversationView);
            conversationView.setTag(holder);
        } else {
            Holder holder = (Holder) conversationView.getTag();
            holder.setMessage(getItem(position));
        }

        ((MailMessageView) conversationView.getTag()).render();

        if (position == getCount() - 1) {
            ((MailMessageView) conversationView.getTag()).expandMessage();
        }

        return conversationView;
    }

    @Override
    public void clear() {
        super.clear();

        mMessages.clear();
        mExpandList.clear();
    }

    public Boolean updateItem(MailImage.Message message) {
        if (message == null || mMessages.isEmpty()) {
            return false;
        }

        for (MailImage.Message tmpMessage : mMessages) {
            if (message.getId() == tmpMessage.getId()) {
                tmpMessage.setReadMessageAuthorized(message.getReadMessageAuthorized());
                tmpMessage.setText(message.getText());

                return true;
            }
        }

        return false;
    }

    private class Holder implements MailMessageView {

        private MailImage.Message mMessage;
        private View mView;
        private GridView mAttachmentGrid;
        private ComposeAttachmentAdapter mAttachmentAdapter;

        public Holder(MailImage.Message message) {
            mMessage = message;
        }

        private void init() {
            if (mView == null) {
                return;
            }

            mAttachmentGrid = (GridView) mView.findViewById(R.id.mailbox_mail_attachment_grid);
            mAttachmentAdapter = new ComposeAttachmentAdapter(getContext(), this);
            mAttachmentGrid.setAdapter(mAttachmentAdapter);
        }

        @Override
        public void render() {
            if ( mView == null ) {
                return;
            }

            reset();

            ConversationList.ConversationItem item = mMailView.getConversationItem();
            SkUser user = SkApplication.getUserInfo();
            ((SKRoundedImageView) mView.findViewById(R.id.mailbox_message_avatar)).setImageUrl(mMessage.getIsAuthor() ?
                    user.getAvatarUrl() : item.getAvatarUrl());
            ((TextView) mView.findViewById(R.id.mailbox_message_display_name)).setText(mMessage.getIsAuthor() ?
                    user.getDisplayName() : item.getDisplayName());
            ((TextView) mView.findViewById(R.id.mailbox_message_time_label)).setText(mMessage.getDisplayDate());

            AuthorizationInfo.ActionInfo info = AuthorizationInfo.getInstance().getActionInfo("read_message");

            if (mMessage.getReadMessageAuthorized() || info == null) {
                if (mMessage.getIsSystem()) {
                    TextView textView = (TextView) mView.findViewById(R.id.mailbox_message_message);
                    textView.setTextColor(mContext.getResources().getColor(R.color.matches_remark));

                    if (mMessage.getSystemType() != null) {
                        switch (mMessage.getSystemType()) {
                            case "renderWink":
                            case "winkIsSent":
                                if (!mMessage.getIsAuthor()) {
                                    ImageView icon = (ImageView) mView.findViewById(R.id.mailbox_message_authorize);
                                    icon.setVisibility(View.VISIBLE);
                                    icon.setImageResource(R.drawable.wink);

                                    textView.setText(getContext().getString(R.string.mailbox_wink_message_text));
                                }
                                break;
                        }
                    } else {
                        textView.setText(Html.fromHtml(mMessage.getText()));
                    }
                } else {
                    String text = mMessage.getText();
                    text = text.replaceAll("(?i)<p style=\"margin:0;\">(<br />)?", "<br>");
                    text = text.replaceAll("(?i)</p>", "");
                    ((TextView) mView.findViewById(R.id.mailbox_message_message)).setText(Html.fromHtml(text));

                    mAttachmentGrid.setVisibility(View.GONE);
                    mAttachmentAdapter.clear();
                    mAttachmentAdapter.notifyDataSetChanged();

                    if (mMessage.hasAttachments()) {
                        mAttachmentAdapter.addSlots(mMessage.getAttachments());
                        mAttachmentAdapter.notifyDataSetChanged();
                    }

                    if (mExpandList.indexOf(Integer.toString(mMessage.getId())) != -1) {
                        expandMessage();
                    }
                }
            } else {
                ImageView icon = (ImageView) mView.findViewById(R.id.mailbox_message_authorize);
                icon.setVisibility(View.VISIBLE);
                TextView textView = (TextView) mView.findViewById(R.id.mailbox_message_message);
                textView.setTextColor(mContext.getResources().getColor(R.color.matches_remark));

                if (info.isAuthorized()) {
                    icon.setImageResource(R.drawable.ic_envelope);
                    textView.setText(mContext.getResources().getString(R.string.mailbox_authorize_read_message));
                } else {
                    if (info.getStatus().equalsName(AuthorizationInfo.STATUS.PROMOTED) && info.isBillingEnable()) {
                        icon.setImageResource(R.drawable.ic_lock);
                    } else {
                        icon.setImageResource(R.drawable.ic_unlock);
                    }

                    textView.setText(info.getMessage());
                }
            }

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMailView.onMailMessageClick(Holder.this);
                }
            });
        }

        @Override
        public View getView() {
            return mView;
        }

        public void setView(View view) {
            mView = view;

            init();
        }

        @Override
        public MailImage.Message getMessage() {
            return mMessage;
        }

        @Override
        public void setMessage(MailImage.Message message) {
            mMessage = message;
        }

        @Override
        public void onAttachmentClick(MailMessageAttachment attachment) {
            mMailView.onMailAttachmentClick(attachment);
        }

        @Override
        public void expandMessage() {
            ((TextView) mView.findViewById(R.id.mailbox_message_message)).setMaxLines(Integer.MAX_VALUE);
            mView.findViewById(R.id.mailbox_mail_attachment_grid).setVisibility(View.VISIBLE);

            String id = Integer.toString(mMessage.getId());

            if (!mExpandList.contains(id)) {
                mExpandList.add(id);
            }
        }

        private void reset() {
            mView.findViewById(R.id.mailbox_message_authorize).setVisibility(View.GONE);
            ((TextView) mView.findViewById(R.id.mailbox_message_message)).setTextColor(
                    mContext.getResources().getColor(R.color.matches_black_text_color)
            );
        }
    }
}
