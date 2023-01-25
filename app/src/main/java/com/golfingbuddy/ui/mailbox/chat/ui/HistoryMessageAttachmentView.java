package com.golfingbuddy.ui.mailbox.chat.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.mailbox.attachment_viewer.AttachmentHelper;
import com.golfingbuddy.ui.mailbox.chat.model.ConversationHistory;
import com.golfingbuddy.utils.SKImageView;
import com.squareup.picasso.Callback;

/**
 * Created by kairat on 2/27/15.
 */
public class HistoryMessageAttachmentView extends HistoryCustomMessageView {
    private ConversationHistory.Messages.Message.Attachment mAttachment;

    public HistoryMessageAttachmentView(Context context) {
        super(context);
    }

    public HistoryMessageAttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HistoryMessageAttachmentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void init() {
        mView = inflate(getContext(), R.layout.mailbox_history_attachment, this);
        setWillNotDraw(false);

        super.init();
    }

    public ConversationHistory.Messages.Message.Attachment getAttachment() {
        return mAttachment;
    }

    public void setAttachment(ConversationHistory.Messages.Message.Attachment attachment) {
        this.mAttachment = attachment;
    }

    public Boolean build() {
        if (mAttachment == null || !super.build()) {
            return false;
        }

        final SKImageView image = (SKImageView) mView.findViewById(R.id.mailbox_history_attachment_image);
        String type = AttachmentHelper.getAttachmentType(mAttachment.getDownloadUrl());

        switch (type) {
            case AttachmentHelper.TYPE_IMAGE:
                image.setImageUrl(mAttachment.getDownloadUrl(), new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        image.setImageResource(R.drawable.ic_file);
                    }
                });
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                break;
            case AttachmentHelper.TYPE_AUDIO:
                image.setImageResource(R.drawable.ic_tune);
                break;
            default:
                image.setImageResource(R.drawable.ic_file);
                break;
        }

        return true;
    }
}
