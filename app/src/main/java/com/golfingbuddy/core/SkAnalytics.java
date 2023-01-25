package com.golfingbuddy.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.flurry.android.Constants;
import com.flurry.android.FlurryAgent;
import com.google.gson.JsonObject;
import com.golfingbuddy.model.base.classes.SkSite;
import com.golfingbuddy.model.base.classes.SkUser;

import java.util.HashMap;

/**
 * Created by kairat on 10/6/15.
 */
public final class SkAnalytics {
    private static boolean mIsInitialized = false;
    private static boolean mIsUserTracked = false;

    private SkAnalytics() {

    }

    public static void init() {
        LocalBroadcastManager.getInstance(SkApplication.getApplication()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SkAnalytics.onReceive();
            }
        }, new IntentFilter("base.site_info_updated"));

        LocalBroadcastManager.getInstance(SkApplication.getApplication()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SkAnalytics.mIsUserTracked = false;
            }
        }, new IntentFilter("base.user_logout"));
    }

    private static void onReceive() {
        setUserAgeAndGender();
        initAgent();
    }

    private static void initAgent() {
        if (mIsInitialized) {
            return;
        }

        SkSite siteInfo = SkApplication.getSiteInfo();

        if (siteInfo == null || TextUtils.isEmpty(siteInfo.getAnalyticsApiKey())) {
            return;
        }

        FlurryAgent.init(SkApplication.getApplication(), siteInfo.getAnalyticsApiKey().trim());
        mIsInitialized = true;
    }

    private static void setUserAgeAndGender() {
        if (mIsUserTracked) {
            return;
        }

        SkUser user = SkApplication.getUserInfo();

        if (user == null) {
            return;
        }

        FlurryAgent.setAge(user.getAge());

        if (user.getSex() == 1) {
            FlurryAgent.setGender(Constants.MALE);
        } else if (user.getSex() == 2) {
            FlurryAgent.setGender(Constants.FEMALE);
        }

        mIsUserTracked = true;
    }

    public static void trackPageView() {
        if ( mIsInitialized )
        FlurryAgent.onPageView();
    }

    public static boolean isLogPurchaseAvailable(JsonObject log) {
        if (log == null || !log.has("product")) {
            return false;
        }

        SkSite siteInfo = SkApplication.getSiteInfo();

        return siteInfo != null && siteInfo.isBillingEnabled();
    }

    public static void logPurchase(JsonObject log) {
        if (!isLogPurchaseAvailable(log)) {
            return;
        }

        JsonObject purchaseObject = log.getAsJsonObject("product");
        HashMap<String, String> articleParams = new HashMap<>();

        articleParams.put("username",
                String.format("%s(%d)",
                        purchaseObject.get("username").getAsString(),
                        purchaseObject.get("userId").getAsInt()
                )
        );

        switch (purchaseObject.get("pluginKey").getAsString()) {
            case "membership":
                articleParams.put("membership name", purchaseObject.get("membershipTitle").getAsString());
                articleParams.put("plan", purchaseObject.get("entityDescription").getAsString());
                FlurryAgent.logEvent("membership purchase", articleParams);
                break;
            case "usercredits":
                articleParams.put("credit pack", purchaseObject.get("entityDescription").getAsString());
                FlurryAgent.logEvent("credits purchase", articleParams);
                break;
        }
    }

    public static void onStart(Context context) {
        if (mIsInitialized) {

            if ( SkApplication.getSiteInfo() != null && SkApplication.getSiteInfo().getAnalyticsApiKey() != null)
            {
                FlurryAgent.onStartSession(context, SkApplication.getSiteInfo().getAnalyticsApiKey().trim());
            }
        }
    }

    public static void onStop(Context context) {
        if (mIsInitialized) {
            FlurryAgent.onEndSession(context);
        }
    }
}
