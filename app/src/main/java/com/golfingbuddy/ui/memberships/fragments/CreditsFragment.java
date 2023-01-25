package com.golfingbuddy.ui.memberships.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;
import com.golfingbuddy.ui.memberships.CreditActionsActivity;
import com.golfingbuddy.ui.memberships.classes.CreditPack;
import com.golfingbuddy.ui.memberships.classes.CreditsData;
import com.golfingbuddy.ui.memberships.classes.MembershipBillingHelper;
import com.golfingbuddy.ui.memberships.classes.VerifySaleInfo;
import com.golfingbuddy.ui.memberships.classes.billing_utils.BillingHelper;
import com.golfingbuddy.ui.memberships.classes.billing_utils.IabResult;
import com.golfingbuddy.ui.memberships.classes.billing_utils.Inventory;
import com.golfingbuddy.ui.memberships.classes.billing_utils.Purchase;

import java.util.HashMap;

/**
 * Created by jk on 2/6/15.
 */
public class CreditsFragment extends Fragment {

    String TAG = "Billing:";
    public static String PARAMS_DATA = "data";
    public static String ON_ACTIVITY_RESULT = "credits_fragment_billing_activity_result";
    CreditsData mData = null;

    BillingHelper mBillingHelper;
    MembershipBillingHelper mHelper;
    View mView;

    BroadcastReceiver mPaymetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int requestCode = intent.getIntExtra("requestCode", -100);
            int resultCode = intent.getIntExtra("resultCode", -100);

            Intent data = new Intent();
            Bundle bundle = intent.getExtras();
            if ( bundle != null && bundle.containsKey("data") ) {
                data = (Intent) bundle.getParcelable("data");
            }

            if ( requestCode ==  -100 && resultCode == -100 )
            {
                return;
            }

            Log.e(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
            if (mBillingHelper == null) return;

            // Pass on the activity result to the helper for handling
            if (!mBillingHelper.handleActivityResult(requestCode, resultCode, data)) {

            }
            else {
                Log.d(TAG, "onActivityResult handled by IABUtil.");
            }
        }
    };

    public CreditsFragment() {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mPaymetReceiver, new IntentFilter(ON_ACTIVITY_RESULT));

        if ( args.containsKey(PARAMS_DATA) )
        {
            String str = args.getString(PARAMS_DATA);
            Gson g = new GsonBuilder().create();
            mData = g.fromJson(str, CreditsData.class);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.credits_fragment, container, false);

        // prepare google play billing helper
        mHelper = new MembershipBillingHelper( (SkBaseInnerActivity) getActivity());
        mHelper.setmOnConsumeSuccsessFinishedListener(new MembershipBillingHelper.OnConsumeSuccessFinishedListener() {
            @Override
            public void onSuccess(Purchase purchase, IabResult result, VerifySaleInfo verifyInfo) {
                CreditsFragment.this.reloadCreditBalance();
            }
        });

        mHelper.setmOnBillingInitInventoryFinished(new BillingHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv) {

            }
        });

        mHelper.setmOnPurchaseFinishedListener(new MembershipBillingHelper.OnPurchaseFinishedListener() {
            @Override
            public void onSuccess(IabResult result, Purchase purchase) {

            }

            @Override
            public void onFail(IabResult result, Purchase purchase) {

            }
        });

        mHelper.initBillingHelper();

        mBillingHelper = mHelper.getmBillingHelper();

        renderCredits();

        return mView;
    }

    // User clicked the "Buy Gas" button

    protected void onBuyClicked(String ProductId) {
        String id = ProductId.toLowerCase();
        Log.i(TAG, "Buy "+id+" button clicked.");

        if ( !mBillingHelper.isBillingSupported() )
        {
            Toast.makeText(CreditsFragment.this.getActivity(), R.string.billing_unavailable, Toast.LENGTH_LONG).show();
            return;
        }

        String payload = mHelper.generateDeveloperPayload(id);

        mBillingHelper.launchPurchaseFlow(this.getActivity(), id, mHelper.RC_REQUEST,
                mHelper.getIabtPurchaseFinishedListener(), payload);
    }

    @Override
    public void onDestroy() {

        if (mBillingHelper != null) {
            mBillingHelper.dispose();
            mBillingHelper = null;
        }
        super.onDestroy();
    }

    public void reloadCreditBalance()  {
        if ( getActivity() == null )
        {
            return;
        }

        mView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        BaseServiceHelper helper = new BaseServiceHelper(getActivity().getApplication());

        // -- TODO: remove dirty hack
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // ---

        helper.runRestRequest(BaseRestCommand.ACTION_TYPE.GET_SUBSCRIBE_DATA, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {

                  HashMap<String, Object> user = new HashMap<String, Object>();

//                    SearchResultData srd = null;

                    try {
                        String result = bundle.getString("data");

                        if (result != null) {
                            JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                            JsonElement data = dataObject.get("data");
                            JsonElement type = dataObject.get("type");

                            if ( data != null && data.isJsonObject() && type != null && "success".equals(type.getAsString()) )
                            {
                                JsonObject membershipData = data.getAsJsonObject();

                                Gson g = new GsonBuilder().create();
                                mData = g.fromJson(membershipData, CreditsData.class);
                            }
                        }
                    }
                    catch( Exception ex )
                    {
                        Log.i(" build search form  ", ex.getMessage(), ex);
                    }
                    finally {
                        if ( mData != null && Boolean.parseBoolean(mData.active) )
                        {
                            TextView creditsBalance = (TextView) mView.findViewById(R.id.credits_balance);
                            creditsBalance.setText(getString(R.string.credits_balance_label, mData.getBalance()));
                            creditsBalance.invalidate();
                        }
                        mView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    }

                }
            }
        );
    }

    public void renderCredits() {
        if ( mData != null ) {

            LinearLayout costOfAction = (LinearLayout) mView.findViewById(R.id.cost_of_action);
            LinearLayout creditPacks = (LinearLayout) mView.findViewById(R.id.credit_packs);

            TextView creditsBalance = (TextView) mView.findViewById(R.id.credits_balance);
            creditsBalance.setText(getString(R.string.credits_balance_label, mData.getBalance()));

            LayoutInflater inflater = this.getActivity().getLayoutInflater();
            View creditItem = inflater.inflate(R.layout.memberships_and_credits_fragment_activity, null, false);
            ((TextView) creditItem.findViewById(R.id.title)).setText(getString(R.string.credits_check_cost_of_actions_title));
            creditItem.setClickable(true);
            creditItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CreditActionsActivity.class);
                    startActivity(intent);
                }
            });

            costOfAction.addView(creditItem);

            if ( mData.getPacks() != null && mData.getPacks().length > 0 ) {
                for ( final CreditPack pack:mData.getPacks() )
                {
                    if ( pack != null )
                    {
                        View creditPackItem = inflater.inflate(R.layout.memberships_and_credits_fragment_activity, null, false);
                        TextView text = (TextView) creditPackItem.findViewById(R.id.title);
                        text.setText(pack.getTitle());//getString(R.string.credits_check_cost_of_actions_title, pack.getTitle(), pack.getPrice()));

                        View layout = creditPackItem.findViewById(R.id.layout);
                        layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            onBuyClicked(pack.getProductId());
                            }
                        });

                        creditPacks.addView(creditPackItem);
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    public void onDestroyView() {
        if (getActivity() != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mPaymetReceiver);
        }

        super.onDestroyView();
    }
}
