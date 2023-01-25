package com.golfingbuddy.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;

/**
 * Created by sardar on 5/30/14.
 */
@SuppressLint("ParcelCreator")
public abstract class SkBaseCommand implements Parcelable {

    public static String EXTRA_PROGRESS = SkApplication.PACKAGE.concat(".EXTRA_PROGRESS");

    public static final int RESPONSE_SUCCESS = 0;

    public static final int RESPONSE_FAILURE = 1;

    public static final int RESPONSE_PROGRESS = 2;

    protected ResultReceiver sfCallback;

    protected volatile boolean cancelled = false;

    public SkBaseCommand() {

    }

    public final void execute(Intent intent, Context context, ResultReceiver callback) {
        this.sfCallback = callback;
        doExecute(intent, context, callback);
    }

    protected abstract void doExecute(Intent intent, Context context, ResultReceiver callback);

    protected void notifySuccess(Bundle data) {
        sendUpdate(RESPONSE_SUCCESS, data);
    }

    protected void notifyFailure(Bundle data) {
        sendUpdate(RESPONSE_FAILURE, data);
    }

    protected void sendProgress(int progress) {
        Bundle b = new Bundle();
        b.putInt(EXTRA_PROGRESS, progress);

        sendUpdate(RESPONSE_PROGRESS, b);
    }

    private void sendUpdate(int resultCode, Bundle data) {
        if (sfCallback != null) {
            sfCallback.send(resultCode, data);
        }
    }

    public void cancel() {
        cancelled = true;
    }

    /* parcelable methods */

    public SkBaseCommand(Parcel in) {
        sfCallback = (ResultReceiver) in.readValue(ResultReceiver.class.getClassLoader());
        cancelled = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeValue(sfCallback);
        parcel.writeByte((byte) (cancelled ? 1 : 0));
    }

}

