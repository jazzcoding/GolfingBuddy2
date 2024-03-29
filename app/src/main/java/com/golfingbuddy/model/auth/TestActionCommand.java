package com.golfingbuddy.model.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.golfingbuddy.core.SkBaseCommand;

import java.util.Random;

/**
 * Created by sardar on 6/5/14.
 */
public class TestActionCommand extends SkBaseCommand {

    private static final String TAG = "TestActionCommand";

    private String arg1;
    private String arg2;

    @Override
    public void doExecute(Intent intent, Context context, ResultReceiver callback) {
        Bundle data = new Bundle();

        Random rnd = new Random();

        try {
            int progress = 0;
            sendProgress(progress);

            while (progress < 100) {
                Thread.sleep(rnd.nextInt(300) + 200);
                if (cancelled) {
                    Log.w(TAG, "Command was cancelled");
                    return;
                }

                progress += rnd.nextInt(40);
                if (progress > 100) {
                    progress = 100;
                }
                sendProgress(progress);
            }

        } catch (InterruptedException e) {
            Log.wtf(TAG, "WTF");
        }

        if (TextUtils.isEmpty(arg1) || TextUtils.isEmpty(arg2)) {
            data.putString("error", "Surprise!");
            notifyFailure(data);
        } else {
            data.putString("data", arg1 + arg2);
            notifySuccess(data);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(arg1);
        dest.writeString(arg2);
    }

    public static final Parcelable.Creator<TestActionCommand> CREATOR = new Parcelable.Creator<TestActionCommand>() {
        public TestActionCommand createFromParcel(Parcel in) {
            return new TestActionCommand(in);
        }

        public TestActionCommand[] newArray(int size) {
            return new TestActionCommand[size];
        }
    };

    private TestActionCommand(Parcel in) {
        arg1 = in.readString();
        arg2 = in.readString();
    }

    public TestActionCommand(String arg1, String arg2) {
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

}
