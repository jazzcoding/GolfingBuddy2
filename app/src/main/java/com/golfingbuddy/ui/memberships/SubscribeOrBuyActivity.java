package com.golfingbuddy.ui.memberships;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.SkStaticData;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;
import com.golfingbuddy.ui.memberships.classes.CreditPack;
import com.golfingbuddy.ui.memberships.classes.MembershipBillingHelper;
import com.golfingbuddy.ui.memberships.classes.VerifySaleInfo;
import com.golfingbuddy.ui.memberships.classes.billing_utils.BillingHelper;
import com.golfingbuddy.ui.memberships.classes.billing_utils.IabResult;
import com.golfingbuddy.ui.memberships.classes.billing_utils.Inventory;
import com.golfingbuddy.ui.memberships.classes.billing_utils.Purchase;

import java.util.HashMap;

/**
 * Created by jk on 2/27/15.
 */
public class SubscribeOrBuyActivity extends SkBaseInnerActivity {

    String TAG = " BILLING : ";
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;

    String pluginKey = null;
    String actionKey = null;

    BillingHelper mBillingHelper;
    MembershipBillingHelper mHelper;


    public SubscribeOrBuyActivity() {
//        mServiceConn = new ServiceConnection() {
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//                mService = null;
//            }
//
//            @Override
//            public void onServiceConnected(ComponentName name,
//                                           IBinder service)
//            {
//                mService = IInAppBillingService.Stub.asInterface(service);
//            }
//        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.subscribe_activity);

//        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
//        serviceIntent.setPackage("com.android.vending");
//        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        Intent intent = getIntent();
        pluginKey = intent.getStringExtra("pluginKey");
        actionKey = intent.getStringExtra("actionKey");

        if ( pluginKey == null || actionKey == null )
        {
            return;
        }

        loadData();

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        setEmulateBackButton(true);
    }

    @Override
    public void onDestroy() {

        if (mHelper != null) {
            mHelper.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    public void startIntentSenderForResult(IntentSender intent, int requestCode,
            @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags)
            throws IntentSender.SendIntentException {
        setWaitScreen(false);
        super.startIntentSenderForResult(intent,requestCode,fillInIntent,flagsMask,flagsValues,extraFlags);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home || id == android.R.id.home || id == R.id.homeAsUp) {
            this.finish();
            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    protected void loadData() {

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("pluginKey", pluginKey);
        params.put("actionKey", actionKey);

        BaseServiceHelper helper = new BaseServiceHelper(getApplication());

        helper.runRestRequest(BaseRestCommand.ACTION_TYPE.GET_PAYMENT_OPTIONS, params, new SkServiceCallbackListener() {
                    @Override
                    public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
                        Suggest suggestData = null;

                        try {
                            String result = bundle.getString("data");

                            if (result != null) {
                                JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                                JsonElement data = dataObject.get("data");
                                JsonElement type = dataObject.get("type");

                                if ( data != null && data.isJsonObject() && type != null && "success".equals(type.getAsString()) )
                                {
                                    JsonObject subscribeData = data.getAsJsonObject();

                                    Gson g = new GsonBuilder().create();
                                    suggestData = g.fromJson(subscribeData, Suggest.class);
                                }
                            }
                        }
                        catch( Exception ex )
                        {
                            Log.e(TAG, ex.getMessage(), ex);
                        }
                        finally {
                            if ( suggestData != null )
                            {
                                render(suggestData);
                            }
                        }
                    }
                }
        );
    }

    protected void addBillingActions(final Suggest suggestData) {
        if (SkStaticData.base64EncodedPublicKey.contains("CONSTRUCT_YOUR")) {
            alert("Please put your app's public key in SkStaticData.java.");
            return;
        }

        // prepare google play billing helper
        mHelper = new MembershipBillingHelper(this);
        mHelper.setmOnConsumeSuccsessFinishedListener(new MembershipBillingHelper.OnConsumeSuccessFinishedListener() {
            @Override
            public void onSuccess(Purchase purchase, IabResult result, VerifySaleInfo verifyInfo) {
                setWaitScreen(false);
                SubscribeOrBuyActivity.this.finish();
            }
        });

        mHelper.setmOnBillingInitInventoryFinished(new BillingHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                setWaitScreen(false);
            }
        });

        mHelper.setmOnPurchaseFinishedListener(new MembershipBillingHelper.OnPurchaseFinishedListener() {
            @Override
            public void onSuccess(IabResult result, Purchase purchase) {
                setWaitScreen(false);
                //Toast.makeText(SubscribeOrBuyActivity.this, R.string.subscribe_succsess, 3000);
            }

            @Override
            public void onFail(IabResult result, Purchase purchase) {
                setWaitScreen(false);
                //Toast.makeText(SubscribeOrBuyActivity.this, R.string.subscribe_error, 3000);
            }
        });

        mHelper.initBillingHelper();

        mBillingHelper = mHelper.getmBillingHelper();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mBillingHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mBillingHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    // User clicked the "Buy Gas" button
    protected void onBuyClicked(String ProductId) {
        Log.i(TAG, "Buy "+ProductId+" button clicked.");

        setWaitScreen(true);

        if ( !mBillingHelper.isBillingSupported() )
        {
            Toast.makeText(SubscribeOrBuyActivity.this, R.string.billing_unavailable, Toast.LENGTH_LONG).show();

            return;
        }

        String payload = mHelper.generateDeveloperPayload(ProductId.toLowerCase());

        mBillingHelper.launchPurchaseFlow(this, ProductId.toLowerCase(), mHelper.RC_REQUEST,
                mHelper.getIabtPurchaseFinishedListener(), payload);
    }

    protected void setWaitScreen(boolean switcher)
    {
        if (switcher)
        {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            findViewById(R.id.content).setVisibility(View.GONE);
            findViewById(R.id.no_content).setVisibility(View.GONE);
        }
        else
        {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            findViewById(R.id.content).setVisibility(View.VISIBLE);
            findViewById(R.id.no_content).setVisibility(View.GONE);
        }
    }

    protected void render(final Suggest suggestData) {

        boolean showMembership = false;
        boolean showCredits = false;

        if ( Boolean.parseBoolean( suggestData.getMembershipActive() ) && suggestData.getPlan() != null  )
        {
            showMembership = true;
            LinearLayout membershipLayout = (LinearLayout) findViewById(R.id.memberships_layout);

            LinearLayout membership = (LinearLayout) findViewById(R.id.membership);

            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View contentView = inflater.inflate(R.layout.membership_item, null, false);

            TextView title = (TextView) contentView.findViewById(R.id.title);
            TextView summary = (TextView) contentView.findViewById(R.id.summary);
            ImageView image = (ImageView) contentView.findViewById(R.id.currentMembersipIcon);
            image.setVisibility(View.GONE);

            contentView.findViewById(R.id.divider).setVisibility(View.VISIBLE);

            image = (ImageView) contentView.findViewById(R.id.icon);
            image.setVisibility(View.GONE);

            contentView.setClickable(true);

            contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( suggestData.getPlan().price != null && Float.parseFloat(suggestData.getPlan().price) > 0 ) {
                        SubscribeOrBuyActivity.this.onBuyClicked(suggestData.getPlan().getProductId());
                    }
                    else
                    {
                        // add trial membership
                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put("plan", suggestData.getPlan().getId());

                        BaseServiceHelper helper = new BaseServiceHelper(getApplication());

                        helper.runRestRequest(BaseRestCommand.ACTION_TYPE.SUBSCRIBE_TO_TRIAL_PLAN, params, new SkServiceCallbackListener() {
                                @Override
                                public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {
                                    VerifySaleInfo info = null;

                                    try {
                                        String result = bundle.getString("data");

                                        if (result != null) {
                                            JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                                            JsonElement data = dataObject.get("data");
                                            JsonElement type = dataObject.get("type");

                                            if ( data != null && data.isJsonObject() && type != null && "success".equals(type.getAsString()) )
                                            {
                                                JsonObject subscribeData = data.getAsJsonObject();

                                                Gson g = new GsonBuilder().create();
                                                info = g.fromJson(subscribeData, VerifySaleInfo.class);
                                            }
                                        }
                                    }
                                    catch( Exception ex )
                                    {
                                        Toast.makeText(SubscribeOrBuyActivity.this, R.string.subscribe_trial_error, Toast.LENGTH_LONG).show();
                                        Log.e(TAG, ex.getMessage(), ex);
                                    }
                                    finally {
                                        if ( info != null )
                                        {
                                            if ( "true".equals(info.getRegistered()) )
                                            {
                                                Toast.makeText(SubscribeOrBuyActivity.this, R.string.subscribe_succsess, Toast.LENGTH_LONG).show();

                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable(){
                                                    @Override
                                                    public void run(){
                                                        SubscribeOrBuyActivity.this.finish();
                                                    }
                                                }, Toast.LENGTH_LONG + 300);

                                            }
//                                            else
//                                            {
//                                                if ( info.getError() != null && !info.getError().isEmpty() )
//                                                {
//                                                    Toast.makeText(SubscribeOrBuyActivity.this.getApplication(), R.string.subscribe_trial_error, 3000);
//                                                }
//                                            }
                                        }
                                    }
                                }
                            }
                        );
                    }
                }
            });

            membership.addView(contentView);

            title.setText("");

            if (  suggestData.getType() != null && suggestData.getType().getLabel() != null )
            {
                title.setText(suggestData.getType().getLabel());
            }

            summary.setText("");
            if ( suggestData.getPlan().getTitle() != null )
            {
                summary.setText(suggestData.getPlan().getTitle());
            }

            View membershipLearnMore =findViewById(R.id.membership_learn_more);
            membershipLearnMore.setClickable(true);
            final String membershipId = suggestData.getType().getId();
            membershipLearnMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MembershipDetailsActivity.class);
                    intent.putExtra(MembershipDetailsActivity.PARAM_MEMBERSHIP_ID, membershipId );
                    startActivity(intent);
                }
            });

            membershipLayout.setVisibility(View.VISIBLE);
        }

        if ( Boolean.parseBoolean( suggestData.getCreditsActive() ) && suggestData.getPack() != null  )
        {
            showCredits = true;
            LinearLayout membershipLayout = (LinearLayout) findViewById(R.id.credits_layout);
            LinearLayout membership = (LinearLayout) findViewById(R.id.credit_pack);

            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View contentView = inflater.inflate(R.layout.memberships_and_credits_fragment_activity, null, false);
            TextView title = (TextView) contentView.findViewById(R.id.title);

            contentView.setClickable(true);
            contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SubscribeOrBuyActivity.this.onBuyClicked(suggestData.getPack().getProductId());
                }
            });

            membership.addView(contentView);

            title.setText(suggestData.getPack().getTitle());
            membershipLayout.setVisibility(View.VISIBLE);

            View creditsLearnMore =findViewById(R.id.credits_learn_more);
            creditsLearnMore.setClickable(true);
            creditsLearnMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), CreditsActivity.class);
                    startActivity(intent);
                }
            });
        }

        if ( !showCredits && !showMembership )
        {
            setWaitScreen(false);
            findViewById(R.id.no_content).setVisibility(View.VISIBLE);
        }
        else {
            setWaitScreen(true);
            addBillingActions(suggestData);
        }
    }

    void complain(String message) {
        Log.d(TAG, message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }

    class Suggest {

        String current;
        String balance;
        CreditPack pack;
        Plan plan;
        MType type;
        String membershipActive;
        String creditsActive;

        public String getCurrent() {
            return current;
        }

        public void setCurrent(String current) {
            this.current = current;
        }

        public String getBalance() {
            return balance;
        }

        public void setBalance(String balance) {
            this.balance = balance;
        }

        public CreditPack getPack() {
            return pack;
        }

        public void setPack(CreditPack pack) {
            this.pack = pack;
        }

        public Plan getPlan() {
            return plan;
        }

        public void setPlan(Plan plan) {
            this.plan = plan;
        }

        public MType getType() {
            return type;
        }

        public void setType(MType type) {
            this.type = type;
        }

        public String getMembershipActive() {
            return membershipActive;
        }

        public void setMembershipActive(String membershipActive) {
            this.membershipActive = membershipActive;
        }

        public String getCreditsActive() {
            return creditsActive;
        }

        public void setCreditsActive(String creditsActive) {
            this.creditsActive = creditsActive;
        }
    }

    class Plan {
        String id;
        String title;
        String productId;
        String price;
        String period;

        public String getPrice() {
            return price;
        }

        public void setPrice(String productId) {
            this.price = price;
        }

        public String getPreriod() {
            return period;
        }

        public void setPreriod(String productId) {
            this.period = period;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    class MType {
        String id;
        String label;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

}
//    Array
//            (
//                    [current] => Free
//                    [balance] => 10
//                    [pack] =>
//                    [plan] => Array
//                    (
//                    [id] => 14
//                    [title] => 10 days - 2 USD
//                    [productId] => MEMBERSHIP_PLAN_14
//            )
//
//            [type] => Array
//        (
//        [label] => VIP
//        )
//
//        [membershipActive] => 1
//        [creditsActive] => 1
//        )
