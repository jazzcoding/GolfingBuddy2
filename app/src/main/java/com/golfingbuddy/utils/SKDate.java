package com.golfingbuddy.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by kairat on 3/6/15.
 */
public class SKDate {
    private static SKDate mInstance;

    private Calendar mCalendar;
    private long mServerDiff;

    private SKDate() {
        mServerDiff = 0;
    }

    public static SKDate getInstance() {
        if ( mInstance == null ) {
            synchronized ( SKDate.class ) {
                mInstance = new SKDate();
            }
        }

        return mInstance;
    }

    public void initServerTime(int serverTimestamp, String serverTimezone) {
        mCalendar = Calendar.getInstance();
        mCalendar.setTime(new Date(serverTimestamp));
        mCalendar.setTimeZone(TimeZone.getTimeZone(serverTimezone));

        mServerDiff = (System.currentTimeMillis() / 1000l) - mCalendar.getTimeInMillis();
    }

    public Calendar getCalendar() {
        return mCalendar;
    }

    public long getServerTimestamp() {
        long time = System.currentTimeMillis() / 1000l;

        return time - mServerDiff;
    }
}
