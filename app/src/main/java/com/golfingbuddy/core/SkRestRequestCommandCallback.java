package com.golfingbuddy.core;

import android.os.Parcelable;

import com.google.gson.JsonArray;

/**
 * Created by sardar on 6/18/14.
 */
public abstract class SkRestRequestCommandCallback implements Parcelable{
    public abstract JsonArray call( SkRestInterface skRest );
}
