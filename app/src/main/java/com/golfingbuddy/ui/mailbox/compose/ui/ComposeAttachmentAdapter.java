package com.golfingbuddy.ui.mailbox.compose.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.mailbox.AttachmentHolderInterface;
import com.golfingbuddy.ui.mailbox.mail.adapter.ui.MailAttachment;
import com.golfingbuddy.ui.mailbox.mail.image.MailImage;

import java.util.ArrayList;

/**
 * Created by kairat on 3/24/15.
 */
public class ComposeAttachmentAdapter extends ArrayAdapter<MailImage.Message.Attachment> {

    private Context mContext;
    private AttachmentHolderInterface mHolder;
    private LayoutInflater mInflater;
    private ArrayList<MailImage.Message.Attachment> mAttachments;

    public ComposeAttachmentAdapter(Context context, AttachmentHolderInterface holder) {
        super(context, R.layout.mail_message_attachment);

        mContext = context;
        mHolder = holder;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAttachments = new ArrayList<>();
    }

    private MailImage.Message.Attachment findAttachment(int id) {
        for (MailImage.Message.Attachment attachment : mAttachments) {
            if (attachment.getId() == id) {
                return attachment;
            }
        }

        return null;
    }

    public void addSlot(int slotId) {
        MailImage.Message.Attachment attachment = new MailImage.Message.Attachment();
        attachment.setId(slotId);
        mAttachments.add(attachment);
    }

    public void addSlot(MailImage.Message.Attachment attachment) {
        if (attachment == null) {
            return;
        }

        mAttachments.add(attachment);
    }

    public void addSlots(ArrayList<MailImage.Message.Attachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        mAttachments.addAll(attachments);
    }

    public Boolean removeSlot(int slotId) {
        MailImage.Message.Attachment attachment = findAttachment(slotId);

        if (attachment != null) {
            return removeSlot(attachment);
        }

        return false;
    }

    public Boolean removeSlot(MailImage.Message.Attachment attachment) {
        return mAttachments.remove(attachment);
    }

    public void updateSlot(int slotId, MailImage.Message.Attachment attachment) {
        for (int i = 0, j = mAttachments.size(); i < j; i++) {
            MailImage.Message.Attachment slot = mAttachments.get(i);

            if (slot.getId() == slotId) {
                mAttachments.set(i, attachment);

                return;
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        mAttachments.clear();
    }

    @Override
    public int getCount() {
        return mAttachments.size();
    }

    @Override
    public MailImage.Message.Attachment getItem(int position) {
        return mAttachments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MailAttachment mailAttachmentView = (MailAttachment) convertView;

        if (mailAttachmentView == null) {
            mailAttachmentView = new MailAttachment(mContext);
            mailAttachmentView.setAttachment(getItem(position));
            mailAttachmentView.setListener(mHolder);
        } else {
            mailAttachmentView.setAttachment(getItem(position));
        }

        mailAttachmentView.reset();
        mailAttachmentView.build();

        return mailAttachmentView;
    }
}
