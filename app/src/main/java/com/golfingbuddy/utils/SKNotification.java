package com.golfingbuddy.utils;

import android.content.Context;

/**
 * Created by kairat on 4/2/15.
 */
public class SKNotification {
    private static SKNotification mInstance;

    private Context mContext;

    public static SKNotification getInstance(Context context) {
        if ( mInstance == null ) {
            synchronized ( SKNotification.class ) {
                mInstance = new SKNotification(context);
            }
        }

        return mInstance;
    }

    private SKNotification(Context context) {
        mContext = context;
    }

    public Notification build() {
        return new Notification(mContext);
    }

    private class Notification {

        private Context mContext;

        private String mTitle;
        private String mText;

        public Notification(Context context) {
            mContext = context;
        }

    }
}
