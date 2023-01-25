package com.golfingbuddy.ui.base;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.core.SkBaseActivity;

public class StatusActivity extends SkBaseActivity {

    public enum STATUS_LIST{
        SUSPENDED,
        NOT_APPROVED,
        NOT_VERIFIED,
        MAINTENANCE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.status_activity);
        ImageView bg = (ImageView)findViewById(R.id.bgImage);
        TextView text = (TextView)findViewById(R.id.text);

        TextView logoutBtn = (TextView)findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                baseHelper.userLogout();
            }
        });

        STATUS_LIST type = (STATUS_LIST)getIntent().getSerializableExtra("type");

        String title = getResources().getString(R.string.suspended_activity_title),
                textVal = getResources().getString(R.string.suspended_activity_text);
        int resId = R.drawable.lock_disable;

        switch ( type ){
            case SUSPENDED:
                resId = R.drawable.lock_disable;
                title = getResources().getString(R.string.suspended_activity_title);
                textVal = getResources().getString(R.string.suspended_activity_text);
                break;

            case NOT_APPROVED:
                resId = R.drawable.lock_disable;
                title = getResources().getString(R.string.not_approve_activity_title);
                textVal = getResources().getString(R.string.not_approve_activity_text);
                break;

            case NOT_VERIFIED:
                resId = R.drawable.lock_disable;
                title = getResources().getString(R.string.verify_activity_title);
                textVal = getResources().getString(R.string.verify_activity_text);
                break;

            case MAINTENANCE:
                resId = R.drawable.ic_maintenance;
                title = getResources().getString(R.string.maintenance_activity_title);
                textVal = getResources().getString(R.string.maintenance_activity_text);
                break;

        }

        setTitle(title);
        text.setText(textVal);
        bg.setImageResource(resId);
    }
}
