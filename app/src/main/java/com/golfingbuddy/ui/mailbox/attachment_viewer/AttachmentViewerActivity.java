package com.golfingbuddy.ui.mailbox.attachment_viewer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.mailbox.chat.model.ConversationHistory;
import com.golfingbuddy.utils.SKImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by kairat on 3/31/15.
 */
public class AttachmentViewerActivity extends Activity {

    private ConversationHistory.Messages.Message.Attachment mAttachment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.PhotoViewTheme);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.mailbox_attachment_viewer_view);

        Intent intent = getIntent();

        if (!intent.hasExtra("attachment")) {
            terminate();

            return;
        }

        try {
            mAttachment = intent.getParcelableExtra("attachment");
        } catch (Exception e) {
            terminate();

            return;
        }

        String type = AttachmentHelper.getAttachmentType(mAttachment.getDownloadUrl());

        if (type == null || !type.equals(AttachmentHelper.TYPE_IMAGE)) {
            terminate();

            return;
        }

        ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ImageView logo = (ImageView) findViewById(android.R.id.home);
        logo.setVisibility(View.INVISIBLE);

        ((SKImageView) findViewById(R.id.mailbox_attachment_image)).setImageUrl(mAttachment.getDownloadUrl());
    }

    public void terminate() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mailbox_attachment_viewer_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mailbox_attachment_menu_save:
                Picasso.with(this).load(mAttachment.getDownloadUrl()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mAttachment.getDownloadUrl().substring(mAttachment.getDownloadUrl().lastIndexOf('/')));

                                try
                                {
                                    file.createNewFile();
                                    FileOutputStream stream = new FileOutputStream(file);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    stream.close();

                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }

                            }
                        }).start();
                        //Toast.makeText(AttachmentViewerActivity.this, "Download dir:TODO", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
                break;
            default:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
