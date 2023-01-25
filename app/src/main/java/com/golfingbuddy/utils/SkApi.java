package com.golfingbuddy.utils;

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Created by sardar on 12/10/14.
 */
final public class SkApi {

    private static String PROP_TYPE = "type";
    private static String VAL_TYPE_SUCCESS = "success";
    private static String VAL_TYPE_USER_NOT_AUTHENTICATED = "not_authenticated";
    private static String VAL_TYPE_USER_ERROR = "userError";
    private static String PROP_DATA = "data";
    private static String PROP_EXCEPTION = "exception";

    public enum API_RESULT{
        SUCCESS,
        NOT_AUTHENTICATED,
        UNDEFINED_RESULT,
        EMPTY_RESULT
    }

    public static API_RESULT checkResult( Bundle data ){

        String result = data.getString("data");
        if (result != null) {
            JsonObject obj = new Gson().fromJson(result, JsonObject.class);
            if( obj.has(PROP_TYPE) ){
                String type = obj.get(PROP_TYPE).getAsString();

                if( type.equals(VAL_TYPE_SUCCESS) && obj.has(PROP_DATA) ){
                    return API_RESULT.SUCCESS;
                }
            }
        }

        return  API_RESULT.UNDEFINED_RESULT;
    }

    public static JsonObject processResult( Bundle data ){
        String result = data.getString("data");
        if (result != null) {
            JsonObject obj = new Gson().fromJson(result, JsonObject.class);
            if (obj.has(PROP_TYPE) && obj.get(PROP_TYPE).getAsString().equals(VAL_TYPE_SUCCESS)) {
                if (obj.has(PROP_DATA)) {
                    return obj.get(PROP_DATA).getAsJsonObject();
                }
            }
        }
        return null;
    }

    public static boolean propExistsAndNotNull( JsonObject obj, String prop ){
        return obj != null && obj.has(prop) && !obj.get(prop).isJsonNull();
    }
}
