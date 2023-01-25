package com.golfingbuddy.ui.memberships;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
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
import com.golfingbuddy.ui.memberships.classes.Benefit;
import com.golfingbuddy.ui.memberships.classes.BenefitActions;
import com.golfingbuddy.ui.memberships.classes.Membership;
import com.golfingbuddy.ui.memberships.classes.MembershipBillingHelper;
import com.golfingbuddy.ui.memberships.classes.MembershipData;
import com.golfingbuddy.ui.memberships.classes.Plan;
import com.golfingbuddy.ui.memberships.classes.VerifySaleInfo;
import com.golfingbuddy.ui.memberships.classes.billing_utils.BillingHelper;
import com.golfingbuddy.ui.memberships.classes.billing_utils.IabResult;
import com.golfingbuddy.ui.memberships.classes.billing_utils.Inventory;
import com.golfingbuddy.ui.memberships.classes.billing_utils.Purchase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * Created by jk on 2/11/15.
 */
public class MembershipDetailsActivity extends SkBaseInnerActivity {

    String TAG = " BILLING : ";
    public static final String PARAM_MEMBERSHIP_ID = "membership_id";
    //public static final int MENU_BUY_BUTTON_ID = 10101;

    BenifitsAdapter mBenifitAdapter = null;
    ListView mListView;
    Membership mData = null;
    String mMembershipId = null;
    View buyMenuItem;
    String mCurrentMembership;
    PopupMenu mPopupMenu = null;
    Menu mMenu = null;

    BillingHelper mBillingHelper;
    MembershipBillingHelper mHelper;

    public MembershipDetailsActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.memberships_details_activity);

        if ( getIntent().hasExtra(this.PARAM_MEMBERSHIP_ID) ) {
            mMembershipId = getIntent().getStringExtra(this.PARAM_MEMBERSHIP_ID);
        }

        if ( mMembershipId == null )
        {
            Log.e("MembershipDetailsActivity", "invalid membership type id");
            return;
        }

        mBenifitAdapter = new BenifitsAdapter(getApplication(), R.layout.membership_details_item, new ArrayList<Benefit>());

        mListView = (ListView) findViewById(R.id.benefits);
        mListView.setAdapter(mBenifitAdapter);

        loadData();

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        setEmulateBackButton(true);
        buyMenuItem = findViewById(R.id.buy_button);
    }

    protected void onBuyClicked(String ProductId) {
        String id = ProductId.toLowerCase();
        Log.i(TAG, "Buy " + id + " button clicked.");

        if ( !mBillingHelper.isBillingSupported() )
        {
            Toast.makeText(MembershipDetailsActivity.this, R.string.billing_unavailable, Toast.LENGTH_LONG).show();

            return;
        }

        String payload = mHelper.generateDeveloperPayload(id);

        mBillingHelper.launchPurchaseFlow(this, id, mHelper.RC_REQUEST,
                mHelper.getIabtPurchaseFinishedListener(), payload);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

//        buyMenuItem = menu.add(Menu.NONE, MENU_BUY_BUTTON_ID, Menu.NONE, R.string.base_buy_label);
//        buyMenuItem.setVisible(false);
//        buyMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        mMenu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.buy_membership_menu, menu);


        if (mData != null)
        {
            displayOrHideMenuItem(mData);
        }

        return super.onCreateOptionsMenu(menu);
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

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home || id == android.R.id.home || id == R.id.homeAsUp) {
            this.finish();
            return true;
        }

        if (id == R.id.buy_button) {
            if ( buyMenuItem == null )
            {
                buyMenuItem = findViewById(R.id.buy_button);
            }

            if ( mPopupMenu == null ) {
                mPopupMenu = new PopupMenu(this, buyMenuItem);
                if (mData != null && mData.getPlans() != null && mData.getPlans().length > 0) {
                    for (final Plan plan : mData.getPlans()) {
                        /* if( "true".equals(plan.getRecurring()) || "1".equals(plan.getRecurring()) ) {
                            continue;
                        } */

                        MenuItem popupItem = mPopupMenu.getMenu().add(Menu.NONE, Integer.parseInt(plan.getId()), Menu.NONE, plan.getLabel());
                        popupItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                if ( Double.parseDouble(plan.getPrice()) > 0 )
                                {
                                    onBuyClicked(plan.getProductId());
                                }
                                else
                                {
                                    // add trial membership
                                    HashMap<String, String> params = new HashMap<String, String>();
                                    params.put("plan", plan.getId());

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
                                            catch( Exception ex ) {
                                                Log.e(TAG, ex.getMessage(), ex);
                                                Toast.makeText(MembershipDetailsActivity.this, R.string.subscribe_trial_error, Toast.LENGTH_LONG).show();
                                            }
                                            finally {
                                                if ( info != null )
                                                {
                                                    if ( "true".equals(info.getRegistered()) )
                                                    {

                                                        Toast.makeText(MembershipDetailsActivity.this, R.string.subscribe_succsess, Toast.LENGTH_LONG).show();

                                                        Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable(){
                                                            @Override
                                                            public void run(){
                                                                MembershipDetailsActivity.this.finish();
                                                            }
                                                        }, Toast.LENGTH_LONG + 300);
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }

                                return true;
                            }
                        });
                    }
                }
            }
            mPopupMenu.show();
            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    public void loadData() {
        BaseServiceHelper helper = new BaseServiceHelper(getApplication());

        helper.runRestRequest(BaseRestCommand.ACTION_TYPE.GET_SUBSCRIBE_DATA, new SkServiceCallbackListener() {
                    @Override
                    public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {

                        HashMap<String, Object> user = new HashMap<String, Object>();
                        MembershipData mMembershipData = null;

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
                                    mMembershipData = g.fromJson(membershipData, MembershipData.class);
                                }
                            }
                        }
                        catch( Exception ex )
                        {
                            Log.i(" build search form  ", ex.getMessage(), ex);
                        }
                        finally {
                            if ( mMembershipData != null && Boolean.parseBoolean(mMembershipData.getActive()) )
                            {
                                mCurrentMembership = mMembershipData.getCurrentMembership();
                                if( mMembershipData.getMembershipTypes() != null && mMembershipData.getMembershipTypes().length > 0 )
                                {
                                    List<Membership> memberships = Arrays.asList(mMembershipData.getMembershipTypes());
                                    for ( Membership membership:memberships )
                                    {
                                        if ( membership != null && mMembershipId.equals(membership.getId()) )
                                        {
                                            mData = membership;
                                            setTitle(membership.getLabel());
                                            break;
                                        }
                                    }
                                }

                                renderBenifits(mData);
                            }
                        }
                    }
                }
        );
    }

    public void renderBenifits(Membership data) {
        if( data != null )
        {
            mBenifitAdapter.clear();
            mBenifitAdapter.addAll(data.getBenefits());
            mBenifitAdapter.notifyDataSetChanged();
            findViewById(R.id.progressBar).setVisibility(View.GONE);

            displayOrHideMenuItem(data);

            // prepare google play billing helper
            mHelper = new MembershipBillingHelper(this);
            mHelper.setmOnConsumeSuccsessFinishedListener(new MembershipBillingHelper.OnConsumeSuccessFinishedListener() {
                @Override
                public void onSuccess(Purchase purchase, IabResult result, VerifySaleInfo verifyInfo) {
                    MembershipDetailsActivity.this.finish();
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
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBillingHelper != null) {
            mBillingHelper.dispose();
            mBillingHelper = null;
        }
    }

    protected void displayOrHideMenuItem(Membership data) {
        if ( buyMenuItem == null )
        {
            buyMenuItem = findViewById(R.id.buy_button);
        }

        if ( mMenu != null && mCurrentMembership != null ) {
            mMenu.findItem(R.id.buy_button).setVisible(false);
            if (data.getPlans() != null && data.getPlans().length > 0 && !mCurrentMembership.equals(data.getId())) {
                mMenu.findItem(R.id.buy_button).setVisible(true);
            }
        }
    }

    class BenifitsAdapter extends ArrayAdapter<Benefit> {

        Context context;
        int layoutResourceId;
        ArrayList<Benefit> data = null;


        public BenifitsAdapter(Context context, int layoutResourceId, ArrayList<Benefit> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;

            this.data = data;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {

            BenefitHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new BenefitHolder();
                holder.sectionLable = (TextView) row.findViewById(R.id.section_label);
                holder.benifitActions = (LinearLayout) row.findViewById(R.id.benifit_actions);

                row.setTag(holder);
            } else {
                holder = (BenefitHolder) row.getTag();
            }

            final Benefit benifit = data.get(position);

            if (benifit != null) {
                holder.setData(benifit);
            }

            return row;
        }
    }

    class BenefitHolder {
        public TextView sectionLable;
        public LinearLayout benifitActions;

        public void setData( final Benefit benefit )
        {
            if ( benefit == null || benefit.getActions() == null || benefit.getActions().length == 0 )
            {
                sectionLable.setVisibility(View.GONE);
                benifitActions.setVisibility(View.GONE);
                return;
            }

            sectionLable.setVisibility(View.VISIBLE);
            benifitActions.setVisibility(View.VISIBLE);

            ArrayList<BenefitActions> list = new ArrayList<BenefitActions>();
            list.addAll(Arrays.asList(benefit.getActions()));

            int count = list.size();

            benifitActions.removeAllViews();

            for ( int position = 0; position < count; position++ )
            {
                BenefitActionsHolder holder = null;
                View row = null;

                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.benefit_action_item, null, false);

                holder = new BenefitActionsHolder();
                holder.label = (TextView) row.findViewById(R.id.benifit_label);
                holder.checkedIcon = (ImageView) row.findViewById(R.id.icon);

                row.setTag(holder);

                final BenefitActions benefitActions = (BenefitActions)list.get(position);

                if (benefitActions != null) {
                    holder.setData(benefitActions);
                }

                benifitActions.addView(row);
            }

            if ( benefit.getLabel() != null ) {
                sectionLable.setText(benefit.getLabel());
            }
        }
    }

    class BenifitActionsAdapter extends ArrayAdapter<BenefitActions> {

        Context context;
        int layoutResourceId;
        ArrayList<BenefitActions> data = null;


        public BenifitActionsAdapter(Context context, int layoutResourceId, ArrayList<BenefitActions> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;

            this.data = data;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {

            BenefitActionsHolder holder = null;

            //if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new BenefitActionsHolder();
                holder.label = (TextView) row.findViewById(R.id.benifit_label);
                holder.checkedIcon = (ImageView) row.findViewById(R.id.icon);

                row.setTag(holder);
//            } else {
//                holder = (BenefitActionsHolder) row.getTag();
//            }

            final BenefitActions benefit = data.get(position);

            if (benefit != null) {
                holder.setData(benefit);
            }

            return row;
        }
    }

    class BenefitActionsHolder {
        public TextView label;
        public ImageView checkedIcon;

        public void setData( final BenefitActions benefit )
        {
            if ( benefit == null )
            {
                return;
            }

            label.setText("");
            if ( benefit.getLabel() != null ) {
                label.setText(benefit.getLabel());
            }

            checkedIcon.setVisibility(View.GONE);
            if ( "true".equals(benefit.getAllowed()) )
            {
                checkedIcon.setVisibility(View.VISIBLE);
            }
        }
    }
}
