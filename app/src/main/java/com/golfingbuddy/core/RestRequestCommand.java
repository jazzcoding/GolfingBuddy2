package com.golfingbuddy.core;

import android.content.Intent;
import android.os.Parcel;

import com.google.gson.JsonObject;
import com.golfingbuddy.ui.auth.AuthActivity;
import com.golfingbuddy.ui.auth.UrlActivity;
import com.golfingbuddy.ui.base.StatusActivity;
import com.golfingbuddy.ui.base.email_verification.EmailVerification;
import com.golfingbuddy.ui.fbconnect.FacebookLoginStepManager;
import com.golfingbuddy.ui.join.view.JoinForm;
import com.golfingbuddy.utils.SkApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sardar on 6/5/14.
 */
public abstract class RestRequestCommand extends SkBaseCommand {

    protected HashMap<String, String> params = new HashMap();
    protected RestRequestCommand() {
    }

    protected SkRestInterface getRest() {

        return SkApplication.getRestAdapter();
    }

    protected void processResult( JsonObject result ){

        //no redirects from classes
        ArrayList<Class> list = new ArrayList<>();
        list.addAll(Arrays.asList(UrlActivity.class, AuthActivity.class, JoinForm.class, FacebookLoginStepManager.class, StatusActivity.class));

        if( SkApplication.getApplication().getForegroundActivityClass() != null && list.contains(SkApplication.getApplication().getForegroundActivityClass())  ){
            return;
        }

        if(SkApi.propExistsAndNotNull(result, "type") && result.get("type").getAsString().equals("userError")){
            if( SkApi.propExistsAndNotNull(result, "data") ){
                JsonObject tempObject = result.get("data").getAsJsonObject();

                if( SkApi.propExistsAndNotNull(tempObject, "exception") &&  tempObject.get("exception").getAsString().equals("ApiAccessException")){
                    if( SkApi.propExistsAndNotNull(tempObject, "userData") ){
                        tempObject = tempObject.get("userData").getAsJsonObject();

                        if( SkApi.propExistsAndNotNull(tempObject, "type") )
                        {
                            String type = tempObject.get("type").getAsString();
                            Intent in = null;

                            if( type.equals("not_authenticated") ){
                                in = new Intent(SkApplication.getApplication(), AuthActivity.class);
                            }

                            else if( type.equals("suspended") ){
                                in = new Intent(SkApplication.getApplication(), StatusActivity.class);
                                in.putExtra("type", StatusActivity.STATUS_LIST.SUSPENDED);
                            }

                            else if( type.equals("not_approved") ){
                                in = new Intent(SkApplication.getApplication(), StatusActivity.class);
                                in.putExtra("type", StatusActivity.STATUS_LIST.NOT_APPROVED);
                            }

                            else if( type.equals("not_verified") ){
                                in = new Intent(SkApplication.getApplication(), EmailVerification.class);
                            }

                            else if( type.equals("maintenance") ){
                                in = new Intent(SkApplication.getApplication(), StatusActivity.class);
                                in.putExtra("type", StatusActivity.STATUS_LIST.MAINTENANCE);
                            }

                            if( in != null ){
                                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                SkApplication.getApplication().startActivity(in);
                            }
                        }
                    }
                }
            }
        }


    }


    /* parcelable methods */
    public RestRequestCommand(Parcel in) {
        super(in);
        params = new HashMap();

        int size = in.readInt();

        for( int i=0; i<size; i++ )
        {
            params.put(in.readString(), in.readString());
        }
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(params.size());

        for( Map.Entry<String, String> entry:params.entrySet() ){
            parcel.writeString(entry.getKey());
            parcel.writeString(entry.getValue());
        }
    }
}
