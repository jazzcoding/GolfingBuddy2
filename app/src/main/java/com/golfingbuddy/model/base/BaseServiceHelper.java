package com.golfingbuddy.model.base;

import android.app.Application;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.core.SkServiceHelper;
import com.golfingbuddy.ui.auth.AuthActivity;

import java.util.HashMap;

/**
 * Created by sardar on 6/2/14.
 */

public class BaseServiceHelper extends SkServiceHelper {

    public BaseServiceHelper(Application app) {
        super(app);
    }

    private static Application app;

    private static class Holder {
        static final BaseServiceHelper INSTANCE = new BaseServiceHelper(app);
    }

    public static BaseServiceHelper getInstance(Application application) {
        app = application;
        return Holder.INSTANCE;
    }

    public int runRestRequest( BaseRestCommand.ACTION_TYPE type, SkServiceCallbackListener listener )
    {
        int requestId = createId();
        return runRequest(requestId, createIntent(application, new BaseRestCommand(type), requestId), listener);
    }

    public int runRestRequest( BaseRestCommand.ACTION_TYPE type, HashMap<String,String> params,SkServiceCallbackListener listener )
    {
        int requestId = createId();
        return runRequest(requestId, createIntent(application, new BaseRestCommand(type, params), requestId), listener);
    }

    public int runRestRequest( BaseRestCommand.ACTION_TYPE type )
    {
        int requestId = createId();
        return runRequest(requestId, createIntent(application, new BaseRestCommand(type), requestId));
    }

    public int runRestRequest( BaseRestCommand.ACTION_TYPE type, HashMap<String,String> params )
    {
        int requestId = createId();
        return runRequest(requestId, createIntent(application, new BaseRestCommand(type, params), requestId));
    }

    public int getSiteInfo( HashMap<String, String> data, SkServiceCallbackListener listener ){
        return runRestRequest(BaseRestCommand.ACTION_TYPE.SITE_INFO, data, listener);
    }

    public int getMatchesList( SkServiceCallbackListener listener ){
        return runRestRequest(BaseRestCommand.ACTION_TYPE.MATCHES_GET_LIST, listener);
    }

    public int getSearchQuestions( SkServiceCallbackListener listener ){
        return runRestRequest(BaseRestCommand.ACTION_TYPE.GET_SEARCH_QUESTIONS, listener);
    }

    public int getUserPhotoList( Integer userId, SkServiceCallbackListener listener ){
        HashMap<String, String> params = new HashMap<>();
        params.put("userId", userId.toString());
        return runRestRequest(BaseRestCommand.ACTION_TYPE.PHOTO_GET_LIST_FOR_USER, params, listener);
    }

    public int getAlbumPhotoList( Integer albumId, SkServiceCallbackListener listener ){
        HashMap<String, String> params = new HashMap<>();
        params.put("albumId", albumId.toString());
        return runRestRequest(BaseRestCommand.ACTION_TYPE.PHOTO_GET_LIST_FOR_ALBUM, params, listener);
    }

    public int getUserList( HashMap<String,String> criteriaList, int first, int count, SkServiceCallbackListener listener ){
        HashMap<String, String> params = new HashMap<>();

        String list = (new Gson()).toJson(criteriaList);
        params.put("criteriaList", list);
        params.put("first", String.valueOf(first));
        params.put("count", String.valueOf(count));

        return runRestRequest(BaseRestCommand.ACTION_TYPE.GET_USER_LIST, params, listener);
    }

    public int getUserAlbumList( Integer userId, SkServiceCallbackListener listener ){
        HashMap<String, String> params = new HashMap<>();
        params.put("userId", userId.toString());
        return runRestRequest(BaseRestCommand.ACTION_TYPE.PHOTO_GET_ALBUM_LIST_FOR_USER, params, listener);
    }

    public int saveQuestionValue( String questionName, String value, Integer userId, SkServiceCallbackListener listener ){

        HashMap<String, String> params = new HashMap<>();
        params.put("userId", userId.toString());
        params.put("name", questionName);
        params.put("value", value);

        return runRestRequest(BaseRestCommand.ACTION_TYPE.SAVE_QUESTION_VALUE, params, listener);
    }

    public int bookmark(int userId, Boolean mark, SkServiceCallbackListener listener) {
        int requestId = createId();
        HashMap<String, String> params = new HashMap<>();
        params.put("userId", Integer.toString(userId));
        params.put("mark", mark ? "1" : "0");
        return runRequest(requestId, createIntent(application, new BaseRestCommand(BaseRestCommand.ACTION_TYPE.BOOKMARK_MARK, params), requestId), listener);
    }

    public int getText(HashMap<String, String[]> prefixKeys, SkServiceCallbackListener listener) {
        int requestId = createId();

        HashMap<String, String> params = new HashMap<>();
        params.put("data", new Gson().toJson(prefixKeys));

        return runRequest(requestId, createIntent(application, new BaseRestCommand(BaseRestCommand.ACTION_TYPE.GET_TEXT, params), requestId), listener);
    }

    public int getCustomPage(String key, SkServiceCallbackListener listener) {
        int requestId = createId();

        HashMap<String, String> params = new HashMap<>();
        params.put("key", key);

        return runRequest(requestId, createIntent(application, new BaseRestCommand(BaseRestCommand.ACTION_TYPE.GET_CUSTOM_PAGE, params), requestId), listener);
    }

    public void userLogout(){
        runRestRequest(BaseRestCommand.ACTION_TYPE.LOGOUT);

        Intent intent = new Intent("base.user_logout");
        LocalBroadcastManager.getInstance(application).sendBroadcast(intent);

        Intent in = new Intent(application, AuthActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        application.startActivity(in);
        //SkApplication.setAuthToken(null);
    }
}
