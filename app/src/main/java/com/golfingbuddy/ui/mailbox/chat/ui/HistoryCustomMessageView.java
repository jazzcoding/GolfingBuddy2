package com.golfingbuddy.ui.mailbox.chat.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.mailbox.AuthorizationInfo;

/**
 * Created by kairat on 4/8/15.
 */
public class HistoryCustomMessageView extends HistoryView {

    protected AuthorizationInfo.ActionInfo mInfo;

    protected HistoryCustomMessageView(Context context) {
        super(context);
    }

    protected HistoryCustomMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected HistoryCustomMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AuthorizationInfo.ActionInfo getInfo() {
        return mInfo;
    }

//    protected void init() {
//        super
//    }

    public void setAuthorizationInfo(AuthorizationInfo.ActionInfo info) {
        mInfo = info;
    }

    protected Boolean build() {
        if (!super.build()) {
            return false;
        }

        if (mMessage.getReadMessageAuthorized() || mMessage.getIsAuthor() || mInfo == null || mInfo.isAuthorizedByDefault()) {
            init();

            return true;
        }

        mView = inflate(getContext(), R.layout.mailbox_history_authorize, this);
        setWillNotDraw(false);
        super.init();

        ImageView icon = (ImageView) mView.findViewById(R.id.mailbox_history_authorize_icon);
        TextView message = (TextView) mView.findViewById(R.id.mailbox_history_authorize_message);
        View action = mView.findViewById(R.id.mailbox_history_authorize_action);

        if (!mMessage.getReadMessageAuthorized() && mInfo.isAuthorized()) {
            icon.setImageResource(R.drawable.ic_envelope_lock);
            message.setText(getResources().getString(R.string.mailbox_authorize_read_message));

            mView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mChatView.onAuthorizeActionClick(mMessage, v);
                }
            });

            return false;
        } else if (mInfo.getStatus().equalsName(AuthorizationInfo.STATUS.DISABLED)) {
            icon.setImageResource(R.drawable.ic_unlock);
            message.setText(mInfo.getMessage());
            action.setVisibility(GONE);

            return false;
        } else if (mInfo.getStatus().equalsName(AuthorizationInfo.STATUS.PROMOTED)) {
            icon.setImageResource(R.drawable.ic_lock);
            message.setText(mInfo.getMessage());

            mView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mChatView.onAuthorizeActionClick(mMessage, v);
                }
            });

            return false;
        }

        return true;
    }

    protected void replaceView() {
        int resourceId;

        if (mView instanceof HistoryMessageAttachmentView) {
            resourceId = R.layout.mailbox_history_attachment;
        } else {
            resourceId = R.layout.mailbox_history_message;
        }

        ViewGroup parent = (ViewGroup) this.getParent();
        int index = parent.indexOfChild(this);
        parent.removeView(this);
        mView = inflate(getContext(), resourceId, this);
        parent.addView(this, index);
    }
}
