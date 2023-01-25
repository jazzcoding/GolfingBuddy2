package com.golfingbuddy.ui.mailbox.mail.adapter.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.mailbox.AttachmentHolderInterface;
import com.golfingbuddy.ui.mailbox.compose.ComposeView;
import com.golfingbuddy.ui.mailbox.mail.adapter.MailMessageAttachment;
import com.golfingbuddy.ui.mailbox.mail.adapter.MailMessageView;
import com.golfingbuddy.ui.mailbox.mail.image.MailImage;
import com.golfingbuddy.ui.mailbox.reply.ReplyView;
import com.golfingbuddy.utils.SKDimensions;
import com.golfingbuddy.utils.SKImageView;

import java.text.DecimalFormat;

/**
 * Created by kairat on 3/14/15.
 */
public class MailAttachment extends LinearLayout implements MailMessageAttachment {

    private View mView;
    private MailImage.Message.Attachment mAttachment;
    private AttachmentHolderInterface mListenerView;

    public MailAttachment(Context context) {
        super(context);
        init();
    }

    public MailAttachment(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MailAttachment(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mView = inflate(getContext(), R.layout.mail_message_attachment, this);
        setWillNotDraw(false);
    }

    @Override
    public MailImage.Message.Attachment getAttachment() {
        return mAttachment;
    }

    @Override
    public void setAttachment(MailImage.Message.Attachment attachment) {
        mAttachment = attachment;
    }

    @Override
    public void setListener(MailMessageView view) {
        mListenerView = view;
    }

    @Override
    public void setListener(AttachmentHolderInterface view) {
        mListenerView = view;
    }

    public void reset() {
        mView.findViewById(R.id.mailbox_message_attachment_progress_bar).setVisibility(VISIBLE);
        mView.findViewById(R.id.mailbox_message_attachment_image).setVisibility(GONE);

        SKImageView imageView = (SKImageView) mView.findViewById(R.id.mailbox_message_attachment_image);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageResource(R.drawable.ic_file);

        ((TextView) mView.findViewById(R.id.mailbox_message_attachment_name)).setText("");
        ((TextView) mView.findViewById(R.id.mailbox_message_attachment_info)).setText("");

        View view = mView.findViewById(R.id.mailbox_message_attachment_delete);
        view.getLayoutParams().width = 0;
    }

    public void build() {
        if (mAttachment == null || mAttachment.getDownloadUrl() == null) {
            return;
        }

        mView.findViewById(R.id.mailbox_message_attachment_progress_bar).setVisibility(GONE);
        mView.findViewById(R.id.mailbox_message_attachment_image).setVisibility(VISIBLE);

        String url = mAttachment.getDownloadUrl();
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        if (type != null) {
            switch (type) {
                case "image/jpeg":
                case "image/png":
                case "image/gif":
                case "image/x-ms-bmp":
                    SKImageView imageView = (SKImageView) mView.findViewById(R.id.mailbox_message_attachment_image);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setImageUrl(mAttachment.getDownloadUrl());
                    break;
                case "audio/mpeg":
                    ((SKImageView) mView.findViewById(R.id.mailbox_message_attachment_image)).setImageResource(R.drawable.ic_tune);
                    break;
            }
        }

        int index = url.lastIndexOf('/');
        String name = (url.substring(index + 1));
        String info = String.format(".%s (%s)", extension, readableFileSize(mAttachment.getFileSize()));

        ((TextView) mView.findViewById(R.id.mailbox_message_attachment_name)).setText(name.substring(0, name.lastIndexOf('.')));
        ((TextView) mView.findViewById(R.id.mailbox_message_attachment_info)).setText(info);
        mView.findViewById(R.id.mailbox_message_attachment_image_content).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListenerView.onAttachmentClick(MailAttachment.this);
            }
        });

        if (mListenerView instanceof ComposeView || mListenerView instanceof ReplyView) {
            View view = mView.findViewById(R.id.mailbox_message_attachment_delete);
            view.getLayoutParams().width = SKDimensions.convertDpToPixel(23, getContext());
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (  mListenerView instanceof ReplyView )
                    {
                        ( (ReplyView)mListenerView ).deleteSlotForAttachment(mAttachment);
                    }

                    if (  mListenerView instanceof ComposeView )
                    {
                        ( (ComposeView)mListenerView ).deleteSlotForAttachment(mAttachment);
                    }

                }
            });
        }
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }

        size *= 1024;

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) ((Math.log10(size) / Math.log10(1024)));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
