package com.golfingbuddy.ui.mailbox.chat.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.golfingbuddy.R;

/**
 * Created by kairat on 4/9/15.
 */
public class HistoryMessageSystemView extends HistoryView {

    public HistoryMessageSystemView(Context context) {
        super(context);
        init();
    }

    public HistoryMessageSystemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HistoryMessageSystemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        mView = inflate(getContext(), R.layout.mailbox_history_system, this);
        setWillNotDraw(false);
    }

    public Boolean build() {
        if (!super.build()) {
            return false;
        }

        ((TextView) mView.findViewById(R.id.mailbox_history_system)).setText(mMessage.getText());

        if (!TextUtils.isEmpty(mMessage.getSystemType())) {
            switch (mMessage.getSystemType()) {
                case "renderWink":
                    if (!mMessage.getIsAuthor()) {
                        View winkContent = mView.findViewById(R.id.mailbox_history_wink_content);
                        winkContent.setVisibility(VISIBLE);
                        winkContent.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mChatView.onWinkBackClick(mMessage);
                            }
                        });
                    }
                    break;
                case "renderWinkBack":
                    if (!mMessage.getIsAuthor()) {
                        View winkContent = mView.findViewById(R.id.mailbox_history_wink_content);
                        winkContent.setVisibility(VISIBLE);
                    }
                    break;
            }
        }

        return true;
    }
}
