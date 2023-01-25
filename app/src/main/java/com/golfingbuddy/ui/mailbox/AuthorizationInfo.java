package com.golfingbuddy.ui.mailbox;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.utils.SkApi;

import java.util.HashMap;

/**
 * Created by kairat on 4/6/15.
 */
public class AuthorizationInfo {

    public static final String DEFAULT_AUTHORIZE = "base";
    public static final String CREDITS_AUTHORIZE = "usercredits";

    private static AuthorizationInfo mInstance;

    public static AuthorizationInfo getInstance() {
        if (mInstance == null) {
            synchronized (AuthorizationInfo.class) {
                mInstance = new AuthorizationInfo();
            }
        }

        return mInstance;
    }

    private HashMap<String, ActionInfo> mInfo;
    private Boolean mIsBillingEnable;
    private Boolean mSubscribeEnable;
    private Boolean mCreditsEnable;

    private AuthorizationInfo() {
        mInfo = new HashMap<>();
    }

    public void updateAuthorizationInfo(JsonObject info) {
        if (info == null || !info.has("billingInfo")) {
            return;
        }

        JsonObject billingInfo = info.getAsJsonObject("billingInfo");

        if (billingInfo.has("authorization")) {
            for (JsonElement action : billingInfo.getAsJsonArray("authorization")) {
                JsonObject tmpAction = action.getAsJsonObject();
                String actionName = tmpAction.get("actionName").getAsString();

                if (!mInfo.containsKey(actionName)) {
                    mInfo.put(actionName, new ActionInfo());
                }

                JsonObject status = tmpAction.getAsJsonObject("status");
                ActionInfo actionInfo = mInfo.get(actionName);

                try {
                    actionInfo.setStatus(STATUS.valueOf(status.get("status").getAsString().toUpperCase()));
                } catch (Exception e) {
                    actionInfo.setStatus(STATUS.DISABLED);
                }

                actionInfo.setIsAuthorized(actionInfo.getStatus().equalsName(STATUS.AVAILABLE));
                actionInfo.setAuthorizedBy(status.has("authorizedBy") ? status.get("authorizedBy").getAsString() : null);

                if (!actionInfo.getStatus().equalsName(STATUS.AVAILABLE)) {
                    actionInfo.setMessage(status.get("msg").getAsString());
                }
            }
        }

        mIsBillingEnable = billingInfo.has("billingEnabled") && billingInfo.get("billingEnabled").getAsBoolean();
        mSubscribeEnable = billingInfo.has("subscribeEnable") && billingInfo.get("subscribeEnable").getAsBoolean();
        mCreditsEnable = billingInfo.has("creditsEnable") && billingInfo.get("creditsEnable").getAsBoolean();
    }

    public ActionInfo getActionInfo(String actionName) {
        if (actionName != null && mInfo.containsKey(actionName)) {
            return mInfo.get(actionName);
        }

        return null;
    }

    public Boolean isBillingEnable() {
        return mIsBillingEnable != null && mIsBillingEnable;
    }

    public Boolean isSubscribeEnable() {
        return mSubscribeEnable != null && mSubscribeEnable;
    }

    public Boolean isCreditsEnable() {
        return mCreditsEnable != null && mCreditsEnable;
    }

    public void getActionInfo(String[] actions, final ActionInfoReady listener) {
        if (actions == null || actions.length == 0) {
            return;
        }

        new Service(SkApplication.getApplication()).getActionInfo(actions, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                JsonObject jsonObject = SkApi.processResult(data);
                AuthorizationInfo.this.updateAuthorizationInfo(jsonObject);
                listener.onReady();
            }
        });
    }

    public interface ActionInfoReady {
        void onReady();
    }

    public enum STATUS {
        DISABLED("disabled"), PROMOTED("promoted"), AVAILABLE("available");

        private final String mName;

        STATUS(String name) {
            mName = name;
        }

        public Boolean equalsName(String name) {
            return name != null && name.equals(mName);
        }

        public Boolean equalsName(STATUS status) {
            return equalsName(status.toString());
        }

        @Override
        public String toString() {
            return mName;
        }
    }


    public class ActionInfo {
        private Boolean mIsAuthorized;
        private STATUS mStatus;
        private int mCost;
        private String mMessage;
        private String mAuthorizedBy;

        public Boolean isAuthorized() {
            return mIsAuthorized;
        }

        public void setIsAuthorized(Boolean isAuthorized) {
            mIsAuthorized = isAuthorized;
        }

        public STATUS getStatus() {
            return mStatus;
        }

        public void setStatus(STATUS status) {
            mStatus = status;
        }

        public int getCost() {
            return mCost;
        }

        public void setCost(int cost) {
            this.mCost = cost;
        }

        public String getMessage() {
            return mMessage;
        }

        public void setMessage(String message) {
            this.mMessage = message;
        }

        public String getAuthorizedBy() {
            return mAuthorizedBy;
        }

        public void setAuthorizedBy(String authorizedBy) {
            mAuthorizedBy = authorizedBy;
        }

        public Boolean isAuthorizedByDefault() {
            return mAuthorizedBy != null && mAuthorizedBy.equals(DEFAULT_AUTHORIZE);
        }

        public Boolean isBillingEnable() {
            return AuthorizationInfo.this.isBillingEnable();
        }

        public Boolean isSubscribeEnable() {
            return AuthorizationInfo.this.isSubscribeEnable();
        }

        public Boolean isCreditsEnable() {
            return AuthorizationInfo.this.isCreditsEnable();
        }
    }
}
