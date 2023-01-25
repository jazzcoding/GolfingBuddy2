package com.golfingbuddy.ui.fbconnect.classes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ResultReceiver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.RestRequestCommand;

import java.util.HashMap;

/**
 * Created by kairat on 12/1/14.
 */
public class FacebookConnectRestRequestCommand extends RestRequestCommand {
    public enum Command {
        FIRST_STEP, SECOND_STEP, SAVE, TRY_LOGIN
    }

    private Command command;

    public FacebookConnectRestRequestCommand(String facebookId) {
        super();

        command = Command.TRY_LOGIN;
        params.put("facebookId", facebookId);
    }

    public FacebookConnectRestRequestCommand(int step) {
        super();

        command = Command.FIRST_STEP;
        params.put("step", Integer.toString(step));
    }

    public FacebookConnectRestRequestCommand(int step, String sex, String email) {
        super();

        command = Command.SECOND_STEP;
        params.put("step", Integer.toString(step));
        params.put("sex", sex);

        if (email != null) {
            params.put("email", email);
        }
    }

    public FacebookConnectRestRequestCommand(HashMap<String, String> data) {
        super();

        command = Command.SAVE;

        params.put("data", new Gson().toJson(data));
    }

    @Override
    protected void doExecute(Intent intent, Context context, ResultReceiver callback) {
        JsonObject result = new JsonObject();

        try {
            switch (command) {
                case FIRST_STEP:
                case SECOND_STEP:
                    result = getRest().getQuestions(params);
                    break;
                case SAVE:
                    result = getRest().saveFacebookUser(params);
                    break;
                case TRY_LOGIN:
                    result = getRest().tryLogin(params);
                    break;
            }
        } catch (Exception e) {
            e.toString();
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable("command", command);
        bundle.putString("data", result.toString());
        callback.send(RESPONSE_SUCCESS, bundle);
    }

    public FacebookConnectRestRequestCommand(Parcel in) {
        super(in);

        command = (Command)in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);

        parcel.writeSerializable(command);
    }

    public static final Creator<FacebookConnectRestRequestCommand> CREATOR = new Creator<FacebookConnectRestRequestCommand>() {
        public FacebookConnectRestRequestCommand createFromParcel(Parcel in) {
            return new FacebookConnectRestRequestCommand(in);
        }

        public FacebookConnectRestRequestCommand[] newArray(int size) {
            return new FacebookConnectRestRequestCommand[size];
        }
    };
}
