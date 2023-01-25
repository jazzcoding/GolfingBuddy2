package com.golfingbuddy.ui.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;
import com.golfingbuddy.ui.memberships.SubscribeOrBuyActivity;

/**
 * Created by jk on 4/6/15.
 */

public class SubscribePhotoAdd extends SkBaseInnerActivity {

    String pluginKey = null;
    String actionKey = null;
    String message = null;

    public static int RESULT_CODE = 3421;

    public SubscribePhotoAdd() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.photo_view_subscribe);

        Intent intent = getIntent();
        pluginKey = intent.getStringExtra("pluginKey");
        actionKey = intent.getStringExtra("actionKey");
        message  = intent.getStringExtra("message");

        if (pluginKey == null || actionKey == null) {
            return;
        }

        setEmulateBackButton(true);

        if( message != null && !message.isEmpty() ) {
            //setTitle(message);
            ((TextView)findViewById(R.id.subscribe_text)).setText(message);
        }
        else
        {
            setTitle(R.string.subscribe_photo_add_title);
        }


        findViewById(R.id.subscribe_img).setClickable(true);
        findViewById(R.id.subscribe_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubscribePhotoAdd.this, SubscribeOrBuyActivity.class);
                intent.putExtra("pluginKey", pluginKey);
                intent.putExtra("actionKey", actionKey);

                startActivityForResult(intent, RESULT_CODE);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String resultFilePath = null;
        Bitmap resultBitmap = null;

        if (requestCode == RESULT_CODE) {
            this.finish();
        }
    }
}