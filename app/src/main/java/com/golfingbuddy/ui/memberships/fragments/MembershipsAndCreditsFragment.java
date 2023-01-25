package com.golfingbuddy.ui.memberships.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.base.classes.SlidingTabLayout;
import com.golfingbuddy.ui.memberships.classes.CreditsData;
import com.golfingbuddy.ui.memberships.classes.MembershipData;

import java.util.HashMap;

/**
 * Created by jk on 2/6/15.
 */
public class MembershipsAndCreditsFragment extends Fragment {

    public static String PARAMS_IS_MEMBERSHIPS_ACTIVE = "IS_MEMBERSHIPS_ACTIVE";
    public static String PARAMS_IS_CREDITS_ACTIVE = "IS_CREDITS_ACTIVE";
    public static String PARAMS_DISPLAY_TITLE = "DISPLAY_TITLE";

    public static String RELOAD_EVENT = "membership_and_credits_reload";


    int requestId = -1;

    View mView;

    FragmentPagerAdapter mTabsAdapter;
    SlidingTabLayout mSlidingTabLayout;
    ViewPager viewPager;
    MembershipData mMembershipData = null;
    CreditsData mCreditsData = null;

//    BroadcastReceiver mReloadReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            MembershipsAndCreditsFragment.this.loadData();
//        }
//    };

    boolean isMembershipActive = true;
    boolean isCreditsActive = true;
    boolean displayTitle = true;

    public static MembershipsAndCreditsFragment getInstance(Boolean isMembershipActive, Boolean isCreditsActive, boolean displayTitle) {

        MembershipsAndCreditsFragment fragment = new MembershipsAndCreditsFragment();

        Bundle bundle = new Bundle();
        bundle.putBoolean(MembershipsAndCreditsFragment.PARAMS_IS_MEMBERSHIPS_ACTIVE, isMembershipActive);
        bundle.putBoolean(MembershipsAndCreditsFragment.PARAMS_IS_CREDITS_ACTIVE, isCreditsActive);
        bundle.putBoolean(MembershipsAndCreditsFragment.PARAMS_DISPLAY_TITLE, displayTitle);

        fragment.setArguments(bundle);

        return fragment;
    }

    public MembershipsAndCreditsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if ( bundle != null ) {
            if ( bundle.containsKey(MembershipsAndCreditsFragment.PARAMS_IS_MEMBERSHIPS_ACTIVE) ) {
                isMembershipActive = bundle.getBoolean(MembershipsAndCreditsFragment.PARAMS_IS_MEMBERSHIPS_ACTIVE, true);
            }

            if ( bundle.containsKey(MembershipsAndCreditsFragment.PARAMS_IS_CREDITS_ACTIVE) ) {
                isCreditsActive = bundle.getBoolean(MembershipsAndCreditsFragment.PARAMS_IS_CREDITS_ACTIVE, true);
            }

            if ( bundle.containsKey(MembershipsAndCreditsFragment.PARAMS_DISPLAY_TITLE) ) {
                displayTitle = bundle.getBoolean(MembershipsAndCreditsFragment.PARAMS_DISPLAY_TITLE, true);
            }
        }

//        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReloadReceiver, new IntentFilter(RELOAD_EVENT));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.memberships_and_credits_fragment, container, false);

        viewPager = (ViewPager) mView.findViewById(R.id.pager);
        mTabsAdapter = new TabsAdapter(getFragmentManager());
        viewPager.setAdapter(mTabsAdapter);
        viewPager.setVisibility(View.GONE);


        mSlidingTabLayout = (SlidingTabLayout) mView.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setVisibility(View.GONE);
        mSlidingTabLayout.setViewPager(viewPager);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tab_divider_border);
            }
        });

        loadData();

        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if ( displayTitle ) {
            getActivity().setTitle(getResources().getString(R.string.memberships_and_credits_title));
        }
    }

    public class TabsAdapter extends FragmentPagerAdapter {

        MembershipsFragment mMembershipFragment;
        CreditsFragment mCredistFragment;

        public int itemCount = 2;

        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {
            Gson g = new GsonBuilder().create();
            Bundle b = new Bundle();
            switch (index) {
                case 0: {
                    if ( mMembershipFragment == null ) {
                        b.putString(CreditsFragment.PARAMS_DATA, g.toJson(mMembershipData));

                        mMembershipFragment = new MembershipsFragment();
                        mMembershipFragment.setArguments(b);
                    }
                    return mMembershipFragment;
                }

                case 1: {
                    if ( mCredistFragment == null ) {
                        b.putString(CreditsFragment.PARAMS_DATA, g.toJson(mCreditsData));

                        mCredistFragment = new CreditsFragment();
                        mCredistFragment.setArguments(b);
                    }
                    return mCredistFragment;
                }
            }

            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            int resource;

            switch (position) {
                case 0:
                    resource = R.string.membership_tab_title;
                    break;

                case 1:
                    resource = R.string.credit_tab_title;
                    break;
                default:
                    resource = R.string.membership_tab_title;
            }

            return getResources().getString(resource);
        }

        public void setItemCount(int count) {
            // get item count - equal to number of tabs
            itemCount = count;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            // get item count - equal to number of tabs
            return itemCount;
        }
    }

    public void loadData() {
        if ( getActivity() == null )
        {
            return;
        }

        mView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.GONE);
        mSlidingTabLayout.setVisibility(View.GONE);

        BaseServiceHelper helper = new BaseServiceHelper(getActivity().getApplication());

        requestId = helper.runRestRequest(BaseRestCommand.ACTION_TYPE.GET_SUBSCRIBE_DATA, new SkServiceCallbackListener() {
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
                                    mMembershipData = g.fromJson(membershipData, MembershipData.class);
                                    mCreditsData = g.fromJson(membershipData, CreditsData.class);
                                }
                            }
                        }
                        catch( Exception ex )
                        {
                            Log.i(" build search form  ", ex.getMessage(), ex);
                        }
                        finally {
                            MembershipsAndCreditsFragment.this.requestId = -1;
                            boolean displayTabs = false;

                            mView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                            if ( mMembershipData != null && Boolean.parseBoolean(mMembershipData.getActive()) && isMembershipActive && mCreditsData != null && Boolean.parseBoolean(mCreditsData.active) && isCreditsActive )
                            {
                                viewPager.setVisibility(View.VISIBLE);
                                mSlidingTabLayout.setVisibility(View.VISIBLE);
                                displayTabs = true;
                            }

                            ((TabsAdapter) mTabsAdapter).setItemCount(2);
                            if ( displayTabs )
                            {
                                ((TabsAdapter) mTabsAdapter).setItemCount(2);
                            }
                            else
                            {
                                viewPager.setOnTouchListener(new View.OnTouchListener()
                                {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event)
                                    {
                                        return true;
                                    }
                                });
                            }
                            ((TabsAdapter) mTabsAdapter).notifyDataSetChanged();

                            if ( mMembershipData != null && Boolean.parseBoolean(mMembershipData.getActive()) && isMembershipActive )
                            {
                                renderMemberships(displayTabs);
                            }

                            if ( mCreditsData != null && Boolean.parseBoolean(mCreditsData.active) && isCreditsActive )
                            {
                                renderCredits(displayTabs);
                            }
                        }
                    }
                }
        );
    }

    public void renderMemberships(boolean displayTabs) {
        if ( !displayTabs )
        {
            viewPager.setCurrentItem(0);
        }
        else
        {
            mSlidingTabLayout.setVisibility(View.VISIBLE);
        }

        viewPager.setVisibility(View.VISIBLE);

    }

    public void renderCredits(boolean displayTabs) {
        if ( !displayTabs )
        {
            viewPager.setCurrentItem(1);
        }
        else {
            mSlidingTabLayout.setVisibility(View.VISIBLE);
        }
        ((TabsAdapter) mTabsAdapter).notifyDataSetChanged();

        viewPager.setVisibility(View.VISIBLE);
    }

//    public void onDestroyView() {
//        if (getActivity() != null) {
//            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReloadReceiver);
//        }
//    }
}