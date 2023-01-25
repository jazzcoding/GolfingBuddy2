package com.golfingbuddy.ui.memberships.classes;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.SkStaticData;
import com.golfingbuddy.core.SkAnalytics;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;
import com.golfingbuddy.ui.memberships.classes.billing_utils.BillingHelper;
import com.golfingbuddy.ui.memberships.classes.billing_utils.IabResult;
import com.golfingbuddy.ui.memberships.classes.billing_utils.Inventory;
import com.golfingbuddy.ui.memberships.classes.billing_utils.Purchase;
import com.golfingbuddy.utils.SkApi;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jk on 3/26/15.
 */
public class MembershipBillingHelper {

    String TAG = " BILLING : ";
    // (arbitrary) request code for the purchase flow
    public static final int RC_REQUEST = 10001;

    BillingHelper mBillingHelper;
    SkBaseInnerActivity activity;


    BillingHelper.OnIabSetupFinishedListener mOnBillingInitListener;
    BillingHelper.QueryInventoryFinishedListener mOnBillingInitInventoryFinished;
    OnConsumeSuccessFinishedListener mOnConsumeSuccsessFinishedListener;

    OnPurchaseFinishedListener mOnPurchaseFinishedListener;

    BillingHelper.OnIabSetupFinishedListener onIabSetupFinishedListener = new BillingHelper.OnIabSetupFinishedListener() {
        public void onIabSetupFinished(IabResult result) {
            if (!result.isSuccess()) {
                // Oh noes, there was a problem.
                complain("Problem setting up in-app billing: " + result);
                return;
            }

            // Have we been disposed of in the meantime? If so, quit.
            if (mBillingHelper == null) return;

            // IAB is fully set up. Now, let's get an inventory of stuff we own.
            mBillingHelper.queryInventoryAsync(new BillingHelper.QueryInventoryFinishedListener() {
                public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                    // Have we been disposed of in the meantime? If so, quit.
                    if (mBillingHelper == null) return;

                    // Is it a failure?
                    if (result.isFailure()) {
                        complain("Failed to query inventory: " + result);
                        return;
                    }

                    List<String> purchases = inventory.getAllOwnedSkus();
                    // consume all purchased items
                    for (String purchase : purchases) {
                        final Purchase purchaseInfo = inventory.getPurchase(purchase);

//                            if( purchaseInfo != null ) {
//                                mBillingHelper.consumeAsync(purchaseInfo, mConsumeFinishedListener);
//                            }
                        // ---- PRE VERIFY SALE
                        consumeSale(purchaseInfo);
                        // add membership or credit pack to user

                        // ---- PRE VERIFY SALE END
                    }

                    if( mOnBillingInitInventoryFinished != null )
                    {
                        mOnBillingInitInventoryFinished.onQueryInventoryFinished(result, inventory);
                    }
                }
            });

            if( mOnBillingInitListener != null )
            {
                mOnBillingInitListener.onIabSetupFinished(result);
            }
        }
    };

    BillingHelper.OnConsumeFinishedListener mPreverifySale = new BillingHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(final Purchase purchase, final IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mBillingHelper == null) return;

            //
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("purchase", purchase.getOriginalJson());
                params.put("signature", purchase.getSignature());

                BaseServiceHelper helper = new BaseServiceHelper(activity.getApplication());
                Log.d(TAG, "verify sale on server" );
                // add membership or credit pack to user
                helper.runRestRequest(BaseRestCommand.ACTION_TYPE.VERIFY_SALE, params, new SkServiceCallbackListener() {
                    @Override
                    public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
                        VerifySaleInfo verifySaleInfo = null;

                        try {
                            String result = bundle.getString("data");

                            if (result != null) {
                                JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                                JsonElement data = dataObject.get("data");
                                JsonElement type = dataObject.get("type");

                                if ( data != null && data.isJsonObject() && type != null && "success".equals(type.getAsString()) )
                                {
                                    JsonObject verifyData = data.getAsJsonObject();

                                    Gson g = new GsonBuilder().create();
                                    verifySaleInfo = g.fromJson(verifyData, VerifySaleInfo.class);
                                }
                            }
                        }
                        catch( Exception ex )
                        {
                            Log.i(TAG, " verify sale error: " + ex.getMessage(), ex);
                        }
                        finally {
                            if ( verifySaleInfo != null )
                            {
                                Log.d(TAG, " sale registred: " + verifySaleInfo.getRegistered() );

                                if ( verifySaleInfo.getError() != null ) {
                                    Log.d(TAG, " verify sale error: "+ verifySaleInfo.getError());
                                }
                            }
                        }

                        if ( mOnConsumeSuccsessFinishedListener != null )
                        {
                            mOnConsumeSuccsessFinishedListener.onSuccess(purchase, result, verifySaleInfo);
                        }
                    }
                }
                );
            } else {
                complain("Error while consuming: " + result);

                //setWaitScreen(false);
                Log.d(TAG, "End consumption flow.");
            }
        }
    };

    BillingHelper.OnConsumeFinishedListener mConsumeFinishedListener = new BillingHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(final Purchase purchase, final IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mBillingHelper == null) return;

            //
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("purchase", purchase.getOriginalJson());
                params.put("signature", purchase.getSignature());

                BaseServiceHelper helper = new BaseServiceHelper(activity.getApplication());
                Log.d(TAG, "verify sale on server" );
                // add membership or credit pack to user
                helper.runRestRequest(BaseRestCommand.ACTION_TYPE.VERIFY_SALE, params, new SkServiceCallbackListener() {
                        @Override
                        public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
                            VerifySaleInfo verifySaleInfo = null;

                            try {
                                String result = bundle.getString("data");

                                if (result != null) {
                                    JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                                    JsonElement data = dataObject.get("data");
                                    JsonElement type = dataObject.get("type");

                                    if ( data != null && data.isJsonObject() && type != null && "success".equals(type.getAsString()) )
                                    {
                                        JsonObject verifyData = data.getAsJsonObject();

                                        Gson g = new GsonBuilder().create();
                                        verifySaleInfo = g.fromJson(verifyData, VerifySaleInfo.class);
                                    }
                                }
                            }
                            catch( Exception ex )
                            {
                                Log.i(TAG, " verify sale error: " + ex.getMessage(), ex);
                            }
                            finally {
                                if ( verifySaleInfo != null )
                                {
                                    Log.d(TAG, " sale registred: " + verifySaleInfo.getRegistered() );

                                    if ( verifySaleInfo.getError() != null ) {
                                        Log.d(TAG, " verify sale error: "+ verifySaleInfo.getError());
                                    }
                                }
                            }

                            if ( mOnConsumeSuccsessFinishedListener != null )
                            {
                                JsonObject prosessResult = SkApi.processResult(bundle);
                                if ( prosessResult != null ) {
                                    SkAnalytics.logPurchase(prosessResult);
                                }

                                mOnConsumeSuccsessFinishedListener.onSuccess(purchase, result, verifySaleInfo);
                            }
                        }
                    }
                );
            } else {
                complain("Error while consuming: " + result);

                //setWaitScreen(false);
                Log.d(TAG, "End consumption flow.");
            }
        }
    };

    // Callback for when a purchase is finished
    BillingHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new BillingHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mBillingHelper == null) return;

            if (result.isFailure()) {
                complain(result.toString());

                if ( mOnPurchaseFinishedListener != null )
                {
                    mOnPurchaseFinishedListener.onFail(result, purchase);
                }

                return;
            }

            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");

                if ( mOnPurchaseFinishedListener != null )
                {
                    mOnPurchaseFinishedListener.onFail(result, purchase);
                }

                return;
            }

            Log.d(TAG, "Purchase successful.");

            if ( mOnPurchaseFinishedListener != null )
            {
                mOnPurchaseFinishedListener.onSuccess(result, purchase);
            }

            consumeSale(purchase);

            //alert("Purchase successful.");
            //setWaitScreen(false);
        }
    };

    public MembershipBillingHelper(SkBaseInnerActivity activity)
    {
        this.activity = activity;

        mBillingHelper = new BillingHelper(activity, SkStaticData.base64EncodedPublicKey);
        mBillingHelper.enableDebugLogging(true);
    }

    public void initBillingHelper()
    {
        mBillingHelper.startSetup(onIabSetupFinishedListener);
    }

    // ------- GETTERS AND SETTERS -----------------

    public BillingHelper getmBillingHelper() {
        return mBillingHelper;
    }

    public BillingHelper.OnIabSetupFinishedListener getmOnBillingInitListener() {
        return mOnBillingInitListener;
    }

    public void setmOnBillingInitListener(BillingHelper.OnIabSetupFinishedListener mOnBillingInitListener) {
        this.mOnBillingInitListener = mOnBillingInitListener;
    }

    public BillingHelper.QueryInventoryFinishedListener getmOnBillingInitInventoryFinished() {
        return mOnBillingInitInventoryFinished;
    }

    public void setmOnBillingInitInventoryFinished(BillingHelper.QueryInventoryFinishedListener mOnBillingInitInventoryFinished) {
        this.mOnBillingInitInventoryFinished = mOnBillingInitInventoryFinished;
    }

    public OnConsumeSuccessFinishedListener getmOnConsumeSuccsessFinishedListener() {
        return mOnConsumeSuccsessFinishedListener;
    }

    public void setmOnConsumeSuccsessFinishedListener(OnConsumeSuccessFinishedListener mOnConsumeSuccsessFinishedListener) {
        this.mOnConsumeSuccsessFinishedListener = mOnConsumeSuccsessFinishedListener;
    }

//    // ---------------------------
//
//    public BillingHelper.OnIabSetupFinishedListener getOnIabSetupFinishedListener() {
//        return onIabSetupFinishedListener;
//    }
//
//    public void setOnIabSetupFinishedListener(BillingHelper.OnIabSetupFinishedListener onIabSetupFinishedListener) {
//        this.onIabSetupFinishedListener = onIabSetupFinishedListener;
//    }
//
//    public BillingHelper.OnConsumeFinishedListener getIabConsumeFinishedListener() {
//        return mConsumeFinishedListener;
//    }

//    public void setConsumeFinishedListener(BillingHelper.OnConsumeFinishedListener mConsumeFinishedListener) {
//        this.mConsumeFinishedListener = mConsumeFinishedListener;
//    }
//
    public BillingHelper.OnIabPurchaseFinishedListener getIabtPurchaseFinishedListener() {
        return mPurchaseFinishedListener;
    }

    public void setIabtPurchaseFinishedListener(BillingHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener) {
        this.mPurchaseFinishedListener = mPurchaseFinishedListener;
    }

    public OnPurchaseFinishedListener getmOnPurchaseFinishedListener() {
        return mOnPurchaseFinishedListener;
    }

    public void setmOnPurchaseFinishedListener(OnPurchaseFinishedListener listener) {
        this.mOnPurchaseFinishedListener = listener;
    }

    public void onDestroy()
    {
        if (mBillingHelper != null) {
            mBillingHelper.dispose();
            mBillingHelper = null;
        }
    }



    // -------- END -------------

    // BILLING UTILITES
    void complain(String message) {
        Log.d(TAG, message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(activity.getApplication());
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }

    public String generateDeveloperPayload(String productId) {

        String userId = String.valueOf(activity.getApp().getUserInfo().getUserId());
        PayloadInfo payload = new PayloadInfo();
        payload.setUserId(userId);
        payload.setProductId(productId);
        payload.setHash(generateHash(userId, productId));

        Gson g = new GsonBuilder().create();
        return Base64.encodeToString((g.toJson(payload)).getBytes(), Base64.NO_WRAP);
    }

    String generateHash(String userId, String productId)
    {
        return Base64.encodeToString((userId + productId).getBytes(), Base64.NO_WRAP);
    }

    public void consumeSale( final Purchase purchaseInfo )
    {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("purchase", purchaseInfo.getOriginalJson());
        params.put("signature", purchaseInfo.getSignature());

        BaseServiceHelper helper = new BaseServiceHelper(activity.getApplication());
        Log.d(TAG, "pre verify sale on server" );

        helper.runRestRequest(BaseRestCommand.ACTION_TYPE.PRE_VERIFY_SALE, params, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
                VerifySaleInfo verifySaleInfo = null;

                try {
                    String result = bundle.getString("data");

                    if (result != null) {
                        JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                        JsonElement data = dataObject.get("data");
                        JsonElement type = dataObject.get("type");

                        if ( data != null && data.isJsonObject() && type != null && "success".equals(type.getAsString()) )
                        {
                            JsonObject verifyData = data.getAsJsonObject();

                            Gson g = new GsonBuilder().create();
                            verifySaleInfo = g.fromJson(verifyData, VerifySaleInfo.class);
                        }
                    }
                }
                catch( Exception ex )
                {
                    Log.i(TAG, " preverify sale error: " + ex.getMessage(), ex);
                }
                finally {

                    if ( verifySaleInfo != null && "true".equals(verifySaleInfo.getIsValid()) )
                    {
                        if ( verifySaleInfo.getError() != null ) {
                            Log.d(TAG, " preverify sale error: "+ verifySaleInfo.getError());
                        }

                        if (purchaseInfo != null && verifyDeveloperPayload(purchaseInfo)) {
                            mBillingHelper.consumeAsync(purchaseInfo, mConsumeFinishedListener);
                        }
                    }
                }
            }
        });
    }

    public boolean verifyDeveloperPayload(Purchase p) {
        String encodedHash = p.getDeveloperPayload();

        if ( encodedHash == null )
        {
            return false;
        }

        byte[] decodedHash = Base64.decode(encodedHash, Base64.NO_WRAP);
        String payload = null;
        try {
            payload = new String(decodedHash, "UTF-8");
        }
        catch ( UnsupportedEncodingException ex)
        {
            Log.e(TAG, "Unsupported encoding UTF-8");
            return false;
        }

        Gson g = new GsonBuilder().create();
        PayloadInfo payloadInfo = g.fromJson(payload, PayloadInfo.class);

        if ( payloadInfo == null )
        {
            return false;
        }

        String userId = String.valueOf(activity.getApp().getUserInfo().getUserId());
        if ( payloadInfo.getUserId() == null || !payloadInfo.getUserId().equals(userId) )
        {
            return false;
        }

        String ProductId = p.getSku();
        if ( payloadInfo.getProductId() == null || !payloadInfo.getProductId().equals(ProductId) )
        {
            return false;
        }

        String hash = generateHash(userId, ProductId);
        if ( payloadInfo.getHash() == null || !payloadInfo.getHash().equals(hash) )
        {
            return false;
        }

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */
        return true;
    }

    public interface OnConsumeSuccessFinishedListener {
        public void onSuccess(Purchase purchase, IabResult result, VerifySaleInfo verifyInfo);
    }

    public interface OnPurchaseFinishedListener {
        public void onSuccess(IabResult result, Purchase purchase);
        public void onFail(IabResult result, Purchase purchase);
    }

}
