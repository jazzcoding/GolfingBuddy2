package com.golfingbuddy.ui.speedmatch.classes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ResultReceiver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.RestRequestCommand;

import java.util.ArrayList;

/**
 * Created by kairat on 12/1/14.
 */
public class SpeedmatchesRestRequestCommand extends RestRequestCommand {
    public enum Command {
        LOAD_LIST, LIKE_USER, SKIP_USER, APPEND_LIST
    }

    private Command command;

    public SpeedmatchesRestRequestCommand(SpeedmatchesFilter filter) {
        super();

        command = Command.LOAD_LIST;
        params.put("filter", new Gson().toJson(filter));
    }

    public SpeedmatchesRestRequestCommand(ArrayList<String> excludeList) {
        super();

        command = Command.APPEND_LIST;
        this.params.put("exclude", new Gson().toJson(excludeList));
    }

    public SpeedmatchesRestRequestCommand(int userId, Command command) {
        super();

        this.command = command;
        this.params.put("userId", Integer.toString(userId));
    }

    @Override
    protected void doExecute(Intent intent, Context context, ResultReceiver callback) {
        JsonObject result = new JsonObject();

        try {
            switch (command) {
                case LOAD_LIST:
                case APPEND_LIST:
                    result = getRest().getSpeedmatchesList(params);
                    break;
                case LIKE_USER:
                    result = getRest().likeUser(params);
                    break;
                case SKIP_USER:
                    result = getRest().skipUser(params);
                    break;
            }
        } catch (Exception e) {
            e.toString();
        }
        processResult(result);
        Bundle bundle = new Bundle();
        bundle.putSerializable("command", command);
        bundle.putString("data", result.toString());
        callback.send(RESPONSE_SUCCESS, bundle);
    }

    public SpeedmatchesRestRequestCommand(Parcel in) {
        super(in);
        command = (Command)in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeSerializable(command);
    }

    public static final Creator<SpeedmatchesRestRequestCommand> CREATOR = new Creator<SpeedmatchesRestRequestCommand>() {
        public SpeedmatchesRestRequestCommand createFromParcel(Parcel in) {
            return new SpeedmatchesRestRequestCommand(in);
        }

        public SpeedmatchesRestRequestCommand[] newArray(int size) {
            return new SpeedmatchesRestRequestCommand[size];
        }
    };
}
