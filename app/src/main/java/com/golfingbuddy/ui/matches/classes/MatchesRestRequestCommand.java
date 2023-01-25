package com.golfingbuddy.ui.matches.classes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;

import com.google.gson.JsonObject;
import com.golfingbuddy.core.RestRequestCommand;

/**
 * Created by kairat on 12/1/14.
 */
public class MatchesRestRequestCommand extends RestRequestCommand {
    private String sort;

    public MatchesRestRequestCommand(String sort) {
        super();

        this.sort = sort;
    }

    public MatchesRestRequestCommand(String sort, int page) {
        super();

        this.sort = sort;
        params.put("page", Integer.toString(page));
    }

    @Override
    protected void doExecute(Intent intent, Context context, ResultReceiver callback) {
        params.put("sort", sort);

        JsonObject result = new JsonObject();

        try {
            result = getRest().getMatchesList(params);
        } catch (Exception e) {
            e.toString();
        }
        processResult(result);
        Bundle bundle = new Bundle();
        bundle.putString("data", result.toString());
        callback.send(RESPONSE_SUCCESS, bundle);
    }

    public MatchesRestRequestCommand(Parcel in) {
        super(in);
        sort = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(sort);
    }

    public static final Parcelable.Creator<MatchesRestRequestCommand> CREATOR = new Parcelable.Creator<MatchesRestRequestCommand>() {
        public MatchesRestRequestCommand createFromParcel(Parcel in) {
            return new MatchesRestRequestCommand(in);
        }

        public MatchesRestRequestCommand[] newArray(int size) {
            return new MatchesRestRequestCommand[size];
        }
    };
}
