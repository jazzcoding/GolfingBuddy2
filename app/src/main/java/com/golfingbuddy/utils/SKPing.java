package com.golfingbuddy.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Created by kairat on 4/1/15.
 */
public class SKPing {
    public final static String COMMANDS = "commands";
    public final static String ACTION_COLLECT = "base.collect_ping_data";
    public final static String ACTION_RESULT = "base.ping_result";
    public final static String RESULT = "result";

    private static SKPing mInstance;

    private Context mContext;

    public static SKPing getInstance(Context context) {
        if ( mInstance == null ) {
            synchronized ( SKPing.class ) {
                mInstance = new SKPing(context);
            }
        }

        return mInstance;
    }

    private SKPing(Context context) {
        mContext = context;
    }

    public HashMap<String, String> start() {
        Intent intent = new Intent(ACTION_COLLECT);
        intent.putExtra(COMMANDS, new HashMap<String, String>());

        LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);

        HashMap<String, String> commands = (HashMap<String, String>)intent.getSerializableExtra(COMMANDS);
        HashMap<String, String> result = new HashMap<>();

        if (!commands.isEmpty()) {
            result.put(COMMANDS, new Gson().toJson(commands));
        }

        return result;
    }
}
