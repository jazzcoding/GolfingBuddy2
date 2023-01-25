package com.golfingbuddy.ui.memberships;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;
import com.golfingbuddy.ui.memberships.classes.CreditAction;
import com.golfingbuddy.ui.memberships.classes.CreditsData;

import java.util.HashMap;

/**
 * Created by jk on 2/26/15.
 */
public class CreditActionsActivity extends SkBaseInnerActivity {

    public static final int ACTIVITY_RESULT_CODE = 10030;

    public CreditActionsActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.credit_actions_activity);

        loadData();

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        setEmulateBackButton(true);
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

    public void loadData() {
        BaseServiceHelper helper = new BaseServiceHelper(getApplication());

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        helper.runRestRequest(BaseRestCommand.ACTION_TYPE.GET_SUBSCRIBE_DATA, new SkServiceCallbackListener() {
                    @Override
                    public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {

                        HashMap<String, Object> user = new HashMap<String, Object>();
                        CreditsData mCreditsData = null;

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
                                    mCreditsData = g.fromJson(membershipData, CreditsData.class);
                                }
                            }
                        }
                        catch( Exception ex )
                        {
                            Log.i(" build search form  ", ex.getMessage(), ex);
                        }
                        finally {
                            if ( mCreditsData != null && Boolean.parseBoolean(mCreditsData.getActive()) )
                            {
                                renderActions(mCreditsData);
                            }
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            findViewById(R.id.credit_actions_content).setVisibility(View.VISIBLE);
                        }
                    }
                }
        );
    }

    public void renderActions( CreditsData mCreditsData) {
        CreditAction[] spendingActions = mCreditsData.getSpendingActions();
        CreditAction[] earningActions = mCreditsData.getEarningActions();
        LinearLayout spendingActionsLayout = (LinearLayout) findViewById(R.id.spending_actions);
        LinearLayout earningActionsLayout = (LinearLayout) findViewById(R.id.earning_actions);

        if ( spendingActions != null && spendingActions.length > 0 )
        {
            for ( CreditAction action:spendingActions )
            {
                spendingActionsLayout.addView(getItemView(action));
            }
        }

        if ( earningActions != null && earningActions.length > 0 )
        {
            for ( CreditAction action:earningActions )
            {
                earningActionsLayout.addView(getItemView(action));
            }
        }
    }

    public View getItemView( CreditAction action ) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.memberships_and_credits_fragment_activity, null, false);
        TextView title = (TextView) contentView.findViewById(R.id.title);
        TextView cost = (TextView) contentView.findViewById(R.id.cost);

        title.setText(action.getLabel());
        cost.setText(action.getAmount());
        cost.setVisibility(View.VISIBLE);
        return contentView;
    }
}
