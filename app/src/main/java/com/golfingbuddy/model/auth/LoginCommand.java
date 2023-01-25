package com.golfingbuddy.model.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;

import com.google.gson.JsonObject;
import com.golfingbuddy.core.RestRequestCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sardar on 6/5/14.
 */
public class LoginCommand extends RestRequestCommand {

    private String username;
    private String password;

    public LoginCommand(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    @Override
    protected void doExecute(Intent intent, Context context, ResultReceiver callback) {
        Map params = new HashMap<String, String>(2);
        params.put("username", username);
        params.put("password", password);

        JsonObject result = new JsonObject();
        try {
            result = getRest().authenticate(params);
        } catch (Exception e) {
            e.toString();
        }
        Bundle a = new Bundle();
        a.putString("data", result.toString());
        callback.send(RESPONSE_SUCCESS, a);
    }

    /* parcel implementation */
    public LoginCommand(Parcel in) {
        super(in);
        username = in.readString();
        password = in.readString();

    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(username);
        parcel.writeString(password);
    }

    public static final Parcelable.Creator<LoginCommand> CREATOR = new Parcelable.Creator<LoginCommand>() {
        public LoginCommand createFromParcel(Parcel in) {
            return new LoginCommand(in);
        }

        public LoginCommand[] newArray(int size) {
            return new LoginCommand[size];
        }
    };
}
