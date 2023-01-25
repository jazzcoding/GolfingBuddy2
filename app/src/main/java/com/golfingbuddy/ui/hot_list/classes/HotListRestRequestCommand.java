package com.golfingbuddy.ui.hot_list.classes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ResultReceiver;

import com.google.gson.JsonObject;
import com.golfingbuddy.core.RestRequestCommand;

/**
 * Created by kairat on 12/1/14.
 */
public class HotListRestRequestCommand extends RestRequestCommand {
    public enum COMMAND {
        LOAD_LIST, ADD, REMOVE
    }

    private COMMAND mCommand;

    public HotListRestRequestCommand() {
        super();

        mCommand = COMMAND.LOAD_LIST;
    }

    public HotListRestRequestCommand(COMMAND command) {
        super();

        mCommand = command;
    }

    @Override
    protected void doExecute(Intent intent, Context context, ResultReceiver callback) {
        JsonObject result = new JsonObject();

        try {
            switch ( mCommand ) {
                case LOAD_LIST:
                    result = getRest().getHotList();
                    break;
                case ADD:
                    result = getRest().addToHotList();
                    break;
                case REMOVE:
                    result = getRest().removeFromHotList();
                    break;
            }
        } catch (Exception e) {
            e.toString();
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable("command", mCommand);
        bundle.putString("data", result.toString());
        callback.send(RESPONSE_SUCCESS, bundle);
    }

    public HotListRestRequestCommand(Parcel in) {
        super(in);

        mCommand = (COMMAND)in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);

        parcel.writeSerializable(mCommand);
    }

    public static final Creator<HotListRestRequestCommand> CREATOR = new Creator<HotListRestRequestCommand>() {
        public HotListRestRequestCommand createFromParcel(Parcel in) {
            return new HotListRestRequestCommand(in);
        }

        public HotListRestRequestCommand[] newArray(int size) {
            return new HotListRestRequestCommand[size];
        }
    };
}
