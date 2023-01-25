package com.golfingbuddy.ui.mailbox.compose;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.ui.mailbox.AuthorizationInfo;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;
import com.golfingbuddy.ui.mailbox.mail.image.MailImage;
import com.golfingbuddy.utils.SKFileUtils;
import com.golfingbuddy.utils.SkApi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by kairat on 3/18/15.
 */
public class ComposePresenterImpl implements ComposePresenter, ComposeModelListener {

    private static final int GALLERY_RESULT = 1;
    private static final int CAMERA_RESULT = 2;
    private static final String albumName = "SkadateX";
    private static final String tmpFileName = "mailbox_compose_camera_attachment";
    private static final String tmpFileExt = ".jpg";

    private String mCurrentPhotoPath;

    private ComposeView mComposeView;
    private ComposeModel mComposeModel;

    public ComposePresenterImpl(ComposeView composeView) {
        mComposeView = composeView;
        mComposeModel = new ComposeModelImpl();
    }

    @Override
    public void attachmentClick() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(mComposeView.getContext());
        myAlertDialog.setTitle(R.string.mailbox_attachment_dialog_title);

        myAlertDialog.setPositiveButton(R.string.mailbox_attachment_dialog_gallery, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                galleryIntent.putExtra("return-data", true);
                mComposeView.startActivity(galleryIntent, GALLERY_RESULT);
            }
        });

        myAlertDialog.setNegativeButton(R.string.mailbox_attachment_dialog_camera, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                try {
                    File f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    mComposeView.startActivity(cameraIntent, CAMERA_RESULT);
                } catch ( IOException e ) {
                    e.printStackTrace();
                    mComposeView.failCreateDirectory();
                }
            }
        });
        myAlertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( resultCode != Activity.RESULT_OK ) {
            return;
        }

        File file = null;
        String path = null;
        String type = null;

        switch ( requestCode ) {
            case GALLERY_RESULT:
                if ( data != null && data.getData() != null ) {
                    Uri uri = data.getData();
                    path = SKFileUtils.getPath(mComposeView.getContext(), uri);
                    type = data.getType();
                    file = new File(path);

                    if ( file.exists() && (type == null || type.isEmpty()) ) {
                        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(path));
                    }
                }

                break;
            case CAMERA_RESULT:
                path = mCurrentPhotoPath;
                type = "image/jpeg";
                file = new File(path);
                break;
            default: return;
        }

        if ( file == null || type == null || !file.exists() ) {
            mComposeView.fileNotFound();
        } else {
            int slotId = mComposeModel.attachAttachment(mComposeView.getUid(), type, path, this);
            mComposeView.prepareSlotForAttachment(slotId);
        }
    }

    @Override
    public void deleteAttachment(MailImage.Message.Attachment attachment) {
        mComposeModel.deleteAttachment(attachment, this);
    }

    @Override
    public void getRecipient(String recipientId) {
        mComposeModel.getRecipientInfo(recipientId, this);
    }

    @Override
    public void suggestionListRequest(String constraint, ArrayList<String> idList) {
        mComposeModel.suggestionListRequest(constraint, idList, this);
    }

    @Override
    public void sendMessage(ComposeInterface compose) {
        mComposeModel.sendMessage(compose, this);
        mComposeView.showProcessDialog();
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);

            if ( !storageDir.mkdirs() ) {
                if ( !storageDir.exists() ) {
                    mComposeView.failCreateDirectory();

                    return null;
                }
            }

        } else {
            mComposeView.failPermissionDirectory();
        }

        return storageDir;
    }

    private File createImageFile() throws IOException {
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(tmpFileName, tmpFileExt, albumF);
        return imageF;
    }

    private File setUpPhotoFile() throws IOException {
        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private JsonObject prepareData(Bundle bundle) {
        JsonObject json = SkApi.processResult(bundle);

        if (json == null || !json.has("result")) {
            return null;
        }

        JsonObject tmpObject = json.getAsJsonObject("result");

        if (tmpObject.has("error") && tmpObject.getAsJsonPrimitive("error").getAsBoolean()) {
            mComposeView.onResponseError(tmpObject.get("message").getAsString());

            return null;
        }

        return json.getAsJsonObject("result");
    }

    @Override
    public void onAttachAttachment(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        JsonObject json = prepareData(bundle);

        if (json == null) {
            mComposeView.destroySlotForAttachment(requestId);

            return;
        }

        MailImage.Message.Attachment attachment = new MailImage.Message.Attachment();
        attachment.setId(json.get("id").getAsInt());
        attachment.setDownloadUrl(json.get("downloadUrl").getAsString());
        attachment.setFileSize(json.get("size").getAsInt());
        mComposeView.updateSlotForAttachment(requestId, attachment);
    }

    @Override
    public void onDeleteAttachment(MailImage.Message.Attachment attachment, int requestId, Intent requestIntent, int resultCode, Bundle data) {

    }

    @Override
    public void onSuggestListResponse(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        JsonObject json = prepareData(bundle);

        mComposeView.onSuggestionListResponse(json != null ? json.getAsJsonArray("list") : null);
    }

    @Override
    public void onMailSend(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        JsonObject json = prepareData(bundle);
        AuthorizationInfo.getInstance().updateAuthorizationInfo(json);
        mComposeView.hideProcessDialog();

        if (json != null) {
            mComposeView.successSendFeedback();
            mComposeView.onSendMessage(new Gson().fromJson(json.get("conversation"), ConversationList.ConversationItem.class));
        }
    }

    @Override
    public void onRecipientReady(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
        JsonObject json = prepareData(bundle);

        if (json != null) {
            JsonObject tmpObject = json.getAsJsonObject("info");
            Recipient recipient = new Recipient();
            recipient.setOpponentId(tmpObject.get("opponentId").getAsString());
            recipient.setDisplayName(tmpObject.get("displayName").getAsString());

            if (!tmpObject.get("avatarUrl").isJsonNull()) {
                recipient.setAvatarUrl(tmpObject.get("avatarUrl").getAsString());
            }

            mComposeView.setRecipient(recipient);
        }
    }
}
