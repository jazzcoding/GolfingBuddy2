package com.golfingbuddy.core;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.TextView;

import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.model.base.classes.SkUser;
import com.golfingbuddy.ui.auth.AuthActivity;
import com.golfingbuddy.ui.auth.UrlActivity;
import com.golfingbuddy.ui.fbconnect.FacebookLoginStepManager;
import com.golfingbuddy.ui.fbconnect.TermsOfUseActivity;
import com.golfingbuddy.ui.join.view.JoinForm;
import com.golfingbuddy.utils.SKDimensions;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sardar on 4/30/14.
 */
public class SkBaseActivity extends Activity implements SkServiceCallbackListener {

    protected BaseServiceHelper baseHelper;
    protected boolean activityIsFinished;


    public SkBaseActivity() {

    }

    public SkApplication getApp() {
        return (SkApplication) getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        onCreate(savedInstanceState, true);
    }

    protected void onCreate(Bundle savedInstanceState, boolean getSiteData) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        baseHelper = BaseServiceHelper.getInstance(getApp());

        checkRedirects();

        if( getSiteData ){
            getSiteInfo();
        }

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleTextView = (TextView) findViewById(titleId);
        if( titleTextView != null ) {
            titleTextView.setPadding(SKDimensions.convertDpToPixel(5, this), 0, 0, 0);
        }

        SkAnalytics.trackPageView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SkAnalytics.onStart(this);
        SkAds.initView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBaseHelper().addListener(this);
        getApp().setForegroundActivityClass(getClass());
        SkAds.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getBaseHelper().removeListener(this);
        getApp().setForegroundActivityClass(null);
        SkAds.onPause(this);
    }

    @Override
    protected void onStop() {
        SkAnalytics.onStop(this);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getBaseHelper().removeListener(this);
        SkAds.onDestroy(this);
    }

    public BaseServiceHelper getBaseHelper(){
        return baseHelper;
    }

    private int siteInfoRequestId;

    private void getSiteInfo(){
        siteInfoRequestId = getBaseHelper().runRestRequest(BaseRestCommand.ACTION_TYPE.SITE_INFO);
    }

    protected void processSiteInfoData(){

    }

    private void checkRedirects(){

        ArrayList<Class> list = new ArrayList<>();
        list.addAll(Arrays.asList(UrlActivity.class, AuthActivity.class, JoinForm.class, FacebookLoginStepManager.class, TermsOfUseActivity.class));

        if( list.contains(getClass()) ){
            return;
        }

        // remove history and redirect to URL activity if URL is empty
        if( getApp().getSiteUrl() == null ){
            Intent in = new Intent(this, UrlActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(in);
            activityIsFinished = true;
            finish();
        }

        else if( getApp().getAuthToken() == null ){
            Intent in = new Intent(this, AuthActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(in);
            activityIsFinished = true;
            finish();
        }
    }

    @Override
    public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
        String aaa = (String)data.get("data");
    }

    public SkUser getCurrentUser(){
        return null;
    }
}
