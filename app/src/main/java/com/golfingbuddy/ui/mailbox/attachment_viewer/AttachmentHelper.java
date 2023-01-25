package com.golfingbuddy.ui.mailbox.attachment_viewer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.mailbox.chat.model.ConversationHistory;

import java.util.Arrays;
import java.util.List;

/**
 * Created by kairat on 4/1/15.
 */
public class AttachmentHelper {

    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_UNKNOW = "unknow";

    public static final String[] IMAGES = {
            "image/jpeg",
            "jpg", "jpeg",
            "image/png",
            "png",
            "image/gif",
            "gif",
            "image/bmp", "image/x-bmp", "image/x-ms-bmp",
            "bmp"
    };

    public static final String[] VIDEOS = {
            "video/3gpp",
            "3gp",
            "video/x-msvideo",
            "avi",
            "video/x-f4v",
            "f4v",
            "video/x-flv",
            "flv",
            "video/x-m4v",
            "m4v",
            "video/mpeg",
            "mpeg",
            "video/mp4",
            "mp4",
            "video/ogg",
            "ogv"
    };

    public static final String[] AUDIOS = {
            "audio/mpeg",
            "mpga", "mp3",
            "audio/mp4",
            "mp4a",
            "audio/ogg",
            "oga",
            "audio/x-wav",
            "wav",
            "audio/x-ms-wma",
            "wma"
    };

    public static class Builder {
        private Context mContext;
        private String mAttachmentUrl;

        public Builder(Context context) {
            mContext = context;
        }

        public String getAttachmentUrl() {
            return mAttachmentUrl;
        }

        public void setAttachmentUrl(String url) {
            mAttachmentUrl = url;
        }

        public void open() {
            if (mAttachmentUrl == null) {
                return;
            }

            String type = getAttachmentType(mAttachmentUrl);

            if (type == null) {
                return;
            }

            if (type.equals(TYPE_IMAGE)) {
                ConversationHistory.Messages.Message.Attachment attachment = new ConversationHistory.Messages.Message.Attachment();
                attachment.setDownloadUrl(mAttachmentUrl);
                Intent intent = new Intent(mContext, AttachmentViewerActivity.class);
                intent.putExtra("attachment", attachment);
                mContext.startActivity(intent);
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(mAttachmentUrl), getAttachmentMimeType(mAttachmentUrl));

                PackageManager manager = mContext.getPackageManager();
                List<ResolveInfo> info = manager.queryIntentActivities(intent, 0);

                if (info.isEmpty()) {
                    Toast.makeText(mContext, mContext.getString(R.string.mailbox_attachment_open_app_not_found, mAttachmentUrl.substring(mAttachmentUrl.lastIndexOf('/') + 1)), Toast.LENGTH_LONG).show();
                } else {
                    mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.mailbox_attachment_open_use_app)));
                }
            }
        }
    }

    public static String getAttachmentMimeType(String attachment) {
        if (attachment == null) {
            return null;
        }

        String ext = MimeTypeMap.getFileExtensionFromUrl(attachment);

        if (ext.isEmpty()) {
            ext = getAttachmentExtension(attachment);
        }

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
    }

    public static String getAttachmentExtension(String attachment) {
        if (attachment == null) {
            return null;
        }

        String url = attachment;

        int fragment = url.lastIndexOf('#');

        if (fragment > 0) {
            url = url.substring(0, fragment);
        }

        int query = url.lastIndexOf('?');

        if (query > 0) {
            url = url.substring(0, query);
        }

        return url.substring((url.lastIndexOf(".") + 1), url.length());
    }

    public static String getAttachmentType(String attachment) {
        String type = getAttachmentMimeType(attachment);

        if (type == null) {
            type = getAttachmentExtension(attachment);
        }

        List<String> imageList = Arrays.asList(IMAGES);

        if (imageList.contains(type)) {
            return TYPE_IMAGE;
        }

        List<String> videoList = Arrays.asList(VIDEOS);

        if (videoList.contains(type)) {
            return TYPE_VIDEO;
        }

        List<String> audiosList = Arrays.asList(AUDIOS);

        if (audiosList.contains(type)) {
            return TYPE_AUDIO;
        }

        return TYPE_UNKNOW;
    }
}
