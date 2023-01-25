package com.golfingbuddy.ui.memberships;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;
import com.golfingbuddy.ui.memberships.fragments.CreditsFragment;
import com.golfingbuddy.ui.memberships.fragments.MembershipsAndCreditsFragment;

/**
 * Created by jk on 3/26/15.
 */
public class MembershipAndCreditsActivity extends SkBaseInnerActivity {

    MembershipsAndCreditsFragment fragment;

    public MembershipAndCreditsActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.credits_activity);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragment = MembershipsAndCreditsFragment.getInstance(true, true, true);
        fragmentTransaction.add(R.id.fragment, fragment);
        fragmentTransaction.commit();

//            //getActionBar().setDisplayHomeAsUpEnabled(true);
//            setEmulateBackButton(true);
//            setTitle(R.string.credit_pack_label);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //notify parent activity about list changes
        Intent intent = new Intent(CreditsFragment.ON_ACTIVITY_RESULT);
        intent.putExtra("requestCode", requestCode);
        intent.putExtra("resultCode", resultCode);
        intent.putExtra("data", data);

        LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);

    }
}

