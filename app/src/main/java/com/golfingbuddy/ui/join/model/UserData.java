package com.golfingbuddy.ui.join.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.join.classes.JoinUserData;
import com.golfingbuddy.ui.join.classes.UploadTmpAvatarData;
import com.golfingbuddy.ui.join.interfaces.OnJoinListener;
import com.golfingbuddy.ui.join.interfaces.OnTmpAvatarUploaded;
import com.golfingbuddy.ui.join.interfaces.UserModel;
import com.golfingbuddy.ui.search.service.QuestionService;

import java.io.File;
import java.util.HashMap;

import retrofit.mime.TypedFile;

/**
 * Created by jk on 1/25/16.
 */
public class UserData implements UserModel {
    private int requestId = -1;

    final public static int STEP_1 = 1;
    final public static int STEP_2 = 2;

    final public static String PREFERENCE_STEP = "USERDATA_JOIN_STEP";
    final public static String DATA_PREFIX = "USERDATA_JOIN_USER_DATA_";
    final public static String PREFERENCE_FILE_KEY = "USERDATA_FILE_KEY";

    private int mStep;
    private SharedPreferences mPreferences;
    private Context context;
    String fileKey;
    BaseServiceHelper mHelper;

    private HashMap<String,String>[] mStepData = new HashMap[3];

    public UserData(Context context, BaseServiceHelper helper) {

        mHelper = helper;

        mStepData[STEP_1] = new HashMap<String, String>();
        mStepData[STEP_2] = new HashMap<String, String>();

        this.context = context;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        mPreferences = preferences;

        mStep = STEP_1;
        if ( !preferences.contains(PREFERENCE_STEP) )
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(PREFERENCE_STEP, mStep);
            editor.commit();
        }
        else
        {
            mStep = preferences.getInt(PREFERENCE_STEP, mStep);
        }

        loadDataFromLocalStorage();
    }

    public void clean()
    {
        clean(context);
    }

    public static void clean(Context context)
    {
        QuestionService.getInstance().removeSharedPreferences(context, PREFERENCE_STEP);
        QuestionService.getInstance().removeSharedPreferences(context, DATA_PREFIX);
    }

    @Override
    public int getStep() {
        return mStep;
    }

    @Override
    public void setStep(int step) {
        if ( step == STEP_1 || step == STEP_2 )
        {
            mStep = step;
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putInt(PREFERENCE_STEP, step);
            editor.commit();
        }
    }

    public void setFileKey(String key) {
        fileKey = key;
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREFERENCE_FILE_KEY, fileKey);
        editor.commit();
    }

    public int getGender() {
        HashMap<String,String> data =  getData(STEP_1);
        int sex = 0;

        if ( data.containsKey("sex") && data.get("sex") != null )
        {
            sex = Integer.parseInt(data.get("sex"));
        }
        return sex;
    }

    @Override
    public HashMap<String, String> getData() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.putAll(mStepData[STEP_1]);
        map.putAll(mStepData[STEP_2]);
        map.put("fileKey", fileKey);
        return map;
    }

    @Override
    public HashMap<String, String> getData(int step) {
        HashMap<String, String> map = new HashMap<String, String>();

        if( step == STEP_1 || step == STEP_2 ) {
            map.putAll(mStepData[step]);
        }

        return map;
    }

    @Override
    public void setData(HashMap<String, String> data) {

        mStepData[getStep()].putAll(data);
        saveDataToLocalStorage();
    }

    @Override
    public void save( final OnJoinListener listener ) {
        if ( requestId > -1 )
        {
            return;
        }

        requestId = mHelper.runRestRequest( BaseRestCommand.ACTION_TYPE.JOIN_USER, getData(),
            new SkServiceCallbackListener() {
                @Override
                public void onServiceCallback(int rId, Intent requestIntent, int resultCode, Bundle bundle) {
                    try {
                        String result = bundle.getString("data");

                        if (result != null)
                        {
                            JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                            JsonElement listData = dataObject.get("data");
                            JsonElement type = dataObject.get("type");

                            if (listData != null && listData.isJsonObject() && type != null && "success".equals(type.getAsString()))
                            {
                                JsonObject data = listData.getAsJsonObject();

                                JoinUserData joinData = new Gson().fromJson(data.getAsJsonObject("data"), JoinUserData.class);

                                if ( "true".equals(joinData.getResult()) || "1".equals(joinData.getResult()) )
                                {
                                    listener.onSuccess(joinData);
                                }
                                else
                                {
                                    listener.onFailed(joinData);
                                }
                            }
                            else
                            {
                                listener.onFailed(null);
                            }
                        }
                        else
                        {
                            listener.onFailed(null);
                        }
                    } catch (Exception ex) {
                        Log.i("join user", ex.getMessage(), ex);
                        listener.onError(ex);
                    } finally {
                        UserData.this.requestId = -1;
                    }
                }
            } );
    }

    @Override
    public void saveTmpAvatar(Uri uri, OnTmpAvatarUploaded listener) {
        new saveTmpAvatarTask(uri, listener).execute(new String[0]);
    }

    // ------ work with local storage -----

    private void saveDataToLocalStorage() {
        QuestionService.getInstance().removeSharedPreferences(this.context, DATA_PREFIX);

        saveDataToLocalStorage(STEP_1);
        saveDataToLocalStorage(STEP_2);
    }

    private void saveDataToLocalStorage(int step) {
        HashMap<String, String> data = getData(step);

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(DATA_PREFIX + step, getGson().toJson(data));
        editor.commit();
    }

    private void loadDataFromLocalStorage() {
        loadDataFromLocalStorage(STEP_1);
        loadDataFromLocalStorage(STEP_2);

        fileKey = mPreferences.getString(PREFERENCE_FILE_KEY, "");
    }

    private void loadDataFromLocalStorage(int step) {

        String dataStr = mPreferences.getString(DATA_PREFIX + step, "");

        HashMap <String,String> data = new HashMap<String, String>();
        data = getGson().fromJson(dataStr, data.getClass());

        if ( data != null )
        {
            this.mStepData[step] = data;
        }
    }

    private Gson getGson() {
        ExclusionStrategy ex = new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        };
        Gson gson = new GsonBuilder().addDeserializationExclusionStrategy(ex).addSerializationExclusionStrategy(ex).create();
        return gson;
    }

    class saveTmpAvatarTask extends AsyncTask<String, JsonObject, JsonObject> {
        private Uri uri;
        private OnTmpAvatarUploaded listener;
        private Exception error;

        public saveTmpAvatarTask(Uri uri, OnTmpAvatarUploaded listener) {
            this.uri = uri;
            this.listener = listener;
            this.error = null;
        }

        @Override
        protected JsonObject doInBackground(String... params) {

            JsonObject result = null;

            try {
                TypedFile avatar = new TypedFile("image/jpeg", new File(uri.getPath()));
                result = SkApplication.getRestAdapter().uploadTmpAvatar(avatar);
            }
            catch (Exception ex) {
                Log.i(" upload tmp avatar  ", ex.getMessage(), ex);
                error = ex;
            }

            return result;
        }

        @Override
        protected void onPostExecute(JsonObject result) {
            if ( error != null )
            {
                listener.onError(error);
                return;
            }

            if ( result == null )
            {
                listener.onFailed(null);
                return;
            }

            try {

                if (result != null && result.has("data"))
                {
                    JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                    JsonElement listData = dataObject.get("data");
                    JsonElement type = dataObject.get("type");

                    if (listData != null && listData.isJsonObject() && type != null && "success".equals(type.getAsString())
                            && listData.getAsJsonObject().has("result") && listData.getAsJsonObject().get("result") != null
                            && listData.getAsJsonObject().get("result").isJsonObject() )
                    {

                        JsonObject data = listData.getAsJsonObject().getAsJsonObject("result");
                        UploadTmpAvatarData uploadResult = UserData.this.getGson().fromJson(data, UploadTmpAvatarData.class);

                        if ( "true".equals(uploadResult.getResult()) )
                        {
                            setFileKey(uploadResult.getFileKey());
                            listener.onSuccess(uploadResult.getMessage());
                        }
                        else if ( uploadResult != null )
                        {
                            listener.onFailed(uploadResult.getMessage());
                        }
                        else
                        {
                            listener.onFailed(null);
                        }
                    }
                    else
                    {
                        listener.onFailed(null);
                    }
                }
            }
            catch (Exception ex) {
                Log.i(" upload tmp avatar  ", ex.getMessage(), ex);
                listener.onError(ex);
            }
        }
    }

}
