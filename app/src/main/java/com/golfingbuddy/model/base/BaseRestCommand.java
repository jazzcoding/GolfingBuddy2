package com.golfingbuddy.model.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ResultReceiver;

import com.google.gson.JsonObject;
import com.golfingbuddy.core.RestRequestCommand;

import java.util.HashMap;

/**
 * Created by sardar on 6/5/14.
 */
public class BaseRestCommand extends RestRequestCommand {

    ACTION_TYPE type;

    public enum ACTION_TYPE{
        AUTHENTICATE,
        BASE_ACTIVITY_INFO,
        BLOCK_USER,
        BOOKMARK_LIST,
        BOOKMARK_MARK,
        CHECK_API,
        FLAG_CONTENT,
        JOIN_USER,
        GET_CUSTOM_PAGE,
        GET_SEARCH_QUESTIONS,
        GET_TEXT,
        GET_USER_LIST,
        GUESTS_LIST,
        GET_SUBSCRIBE_DATA,
        GET_PAYMENT_OPTIONS,
        GET_AUTHORIZATION_ACTION_STATUS,
        GET_SIGN_UP_QUESTIONS,
        LOGOUT,
        MATCHES_GET_LIST,
        PHOTO_DELETE,
        PHOTO_GET_ALBUM_LIST_FOR_USER,
        PHOTO_GET_LIST_FOR_ALBUM,
        PHOTO_GET_LIST_FOR_USER,
        PROFILE_INFO,
        SAVE_QUESTION_VALUE,
        SEND_WINK,
        SITE_INFO,
        SPEEDMATCHES_GET_LIST,
        HOTLIST_COUNT,
        VALIDATE_QUESTIONS,
        VERIFY_SALE,
        PRE_VERIFY_SALE,
        SUBSCRIBE_TO_TRIAL_PLAN,
        USER_MARK_APPROVAL,
        ADD_DEVICE_TOKEN,
        ACCEPT_WINK,
        IGNORE_WINK,
        VERIFY_EMAIL,
        RESEND_VERIFY_EMAIL
    }

    public BaseRestCommand(ACTION_TYPE actionType) {
        super();
        type = actionType;
    }

    public BaseRestCommand(ACTION_TYPE actionType, HashMap<String,String> vars) {
        super();
        type = actionType;
        params = vars;
    }


    @Override
    protected void doExecute(Intent intent, Context context, ResultReceiver callback) {

        JsonObject result = new JsonObject();

        try {
            switch ( type ) {
                case AUTHENTICATE:
                    result = getRest().authenticate(params);
                    break;

                case BLOCK_USER:
                    result = getRest().blockUser(params);
                    break;

                case BASE_ACTIVITY_INFO:
                    result = getRest().userSiteInfo();
                    break;

                case BOOKMARK_LIST:
                    result = getRest().getBookmarksList();
                    break;

                case CHECK_API:
                    result = getRest().checkApi();
                    break;

                case GET_SEARCH_QUESTIONS:
                    result = getRest().getSearchQuestions();
                    break;

                case GET_USER_LIST:
                    result = getRest().getUserList(params);
                    break;

                case GUESTS_LIST:
                    result = getRest().getGuestsList();
                    break;

                case GET_PAYMENT_OPTIONS:
                    result = getRest().getPaymentOptions(params);
                    break;

                case GET_AUTHORIZATION_ACTION_STATUS:
                    result = getRest().getAuthorizationActionStatus(params);
                    break;

                case GET_SIGN_UP_QUESTIONS:
                    result = getRest().getSignUpQuestions(params);
                    break;

                case GET_SUBSCRIBE_DATA:
                    result = getRest().getSubscribeData();
                    break;

                case LOGOUT:
                    getRest().logout();
                    break;

                case PHOTO_DELETE:
                    result = getRest().deletePhotoList(params);
                    break;

                case PHOTO_GET_ALBUM_LIST_FOR_USER:
                    result = getRest().getUserAlbumList(Integer.parseInt(params.get("userId")));
                    break;

                case PHOTO_GET_LIST_FOR_ALBUM:
                    result = getRest().getAlbumPhotoList(Integer.parseInt(params.get("albumId")));
                    break;

                case PHOTO_GET_LIST_FOR_USER:
                    result = getRest().getUserPhotoList(Integer.parseInt(params.get("userId")));
                    break;


                case MATCHES_GET_LIST:
                    //result = getRest().getMatchesList();
                    break;

                case SAVE_QUESTION_VALUE:
                    result = getRest().saveQuestion(params);
                    break;

                case SEND_WINK:
                    result = getRest().sendWink(params);
                    break;

                case SITE_INFO:
                    result = getRest().siteInfo(params);
                    break;

                case SUBSCRIBE_TO_TRIAL_PLAN:
                    result = getRest().subscribeToTrialPlan(params);
                    break;

                case PROFILE_INFO:
                    result = getRest().getUserDetailsInfo(Integer.parseInt(params.get("userId")));
                    break;

                case PRE_VERIFY_SALE:
                    result = getRest().preverifySale(params);
                    break;

                case FLAG_CONTENT:
                    result = getRest().sendReport(params);
                    break;

                case BOOKMARK_MARK:
                    result = getRest().bookmark(params);
                    break;

                case GET_TEXT:
                    result = getRest().getText(params);
                    break;

                case GET_CUSTOM_PAGE:
                    result = getRest().getCustomPage(params);
                    break;

                case VALIDATE_QUESTIONS:
                    result = getRest().validateQuestions(params);
                    break;

                case JOIN_USER:
                    result = getRest().joinUser(params);
                    break;

                case VERIFY_SALE:
                    result = getRest().verifySale(params);
                    break;

                case HOTLIST_COUNT:
                    result = getRest().getHotCount();
                    break;

                case USER_MARK_APPROVAL:
                    result = getRest().userMarkApproval(params);
                    break;

                case ADD_DEVICE_TOKEN:
                    result = getRest().addUserDeviceId(params);
                    break;
                case ACCEPT_WINK:
                    result = getRest().acceptWink(params);
                    break;

                case IGNORE_WINK:
                    result = getRest().ignoreWink(params);

                case VERIFY_EMAIL:
                    result = getRest().verifyEmail(params);
                    break;

                case RESEND_VERIFY_EMAIL:
                    result = getRest().resendVerifyEmail(params);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        processResult(result);
        Bundle resultBundle = new Bundle();
        resultBundle.putString("data", result.toString());
        callback.send(RESPONSE_SUCCESS, resultBundle);
    }

    /* parcel implementation */
    public BaseRestCommand(Parcel in) {
        super(in);
        type = (ACTION_TYPE)in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeSerializable(type);
    }

    public static final Creator<BaseRestCommand> CREATOR = new Creator<BaseRestCommand>() {
        public BaseRestCommand createFromParcel(Parcel in) {
            return new BaseRestCommand(in);
        }

        public BaseRestCommand[] newArray(int size) {
            return new BaseRestCommand[size];
        }
    };
}