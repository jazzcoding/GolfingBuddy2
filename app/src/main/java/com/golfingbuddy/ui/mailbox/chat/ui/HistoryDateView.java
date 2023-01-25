package com.golfingbuddy.ui.mailbox.chat.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.golfingbuddy.R;

/**
 * Created by kairat on 2/26/15.
 */
public class HistoryDateView extends HistoryView {
    private String mDate;

    public HistoryDateView(Context context) {
        super(context);
        init();
    }

    public HistoryDateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HistoryDateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        this.mDate = date;
    }

    protected void init() {
        mView = inflate(getContext(), R.layout.mailbox_history_date, this);
        setWillNotDraw(false);
    }

    public Boolean build() {
        if ( mView == null || mDate == null ) {
            return false;
        }

        ((TextView) mView.findViewById(R.id.mailbox_history_date)).setText(mDate);

        return true;
    }
}
