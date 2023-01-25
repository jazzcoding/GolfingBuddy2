package com.golfingbuddy.ui.memberships.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.memberships.MembershipDetailsActivity;
import com.golfingbuddy.ui.memberships.classes.Membership;
import com.golfingbuddy.ui.memberships.classes.MembershipData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jk on 2/6/15.
 */
public class MembershipsFragment extends Fragment {

    public static final int RELOAD_ACTIVITY_RESULT_CODE = 10020;

    public static String PARAMS_DATA = "data";
    MembershipData mData = null;
    MembershipAdapter mAdapter = null;
    AbsListView mListView;
    protected String mCurrentMembership = null;

    View mView = null;

    public MembershipsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if ( args.containsKey(PARAMS_DATA) )
        {
            String str = args.getString(PARAMS_DATA);
            Gson g = new GsonBuilder().create();
            mData = g.fromJson(str, MembershipData.class);
        }

        mAdapter = new MembershipAdapter(getActivity(), R.layout.membership_item, new ArrayList<Membership>());

//        if (mData != null) {
            Membership[] memberships = mData.getMembershipTypes();

            mCurrentMembership = mData.getCurrentMembership();

            if (memberships != null && memberships.length > 0) {
                List<Membership> list = Arrays.asList(memberships);
                mAdapter.addAll(list);
            }

//        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == MembershipsFragment.RELOAD_ACTIVITY_RESULT_CODE )
        {
            loadData();
        }
    }

    public void loadData() {
        if ( getActivity() == null )
        {
            return;
        }

        // -- TODO: remove dirty hack
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // ---
        //mView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        BaseServiceHelper helper = new BaseServiceHelper(getActivity().getApplication());

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
                                    mData = g.fromJson(membershipData, MembershipData.class);
                                }
                            }
                        }
                        catch( Exception ex )
                        {
                            Log.i(" build search form  ", ex.getMessage(), ex);
                        }
                        finally {
                            Log.e("RESULT", "0");
                            if ( mData != null ) {
                                //mView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                                Membership[] memberships = mData.getMembershipTypes();

                                mCurrentMembership = mData.getCurrentMembership();
                                mAdapter.clear();
                                Log.e("RESULT", "1");
                                if (memberships != null && memberships.length > 0) {
                                    List<Membership> list = Arrays.asList(memberships);

                                    mAdapter.addAll(list);
                                    mAdapter.notifyDataSetChanged();

                                    Log.e("RESULT", "2");
                                }
                            }
                        }
                    }
                }
        );
    }

    private void redirectToMembershipDetails( Membership membership  ) {
        if ( membership != null && membership.getId() != null ) {
            Intent intent = new Intent(getActivity(), MembershipDetailsActivity.class);
            intent.putExtra(MembershipDetailsActivity.PARAM_MEMBERSHIP_ID, membership.getId() );
            startActivityForResult(intent, RELOAD_ACTIVITY_RESULT_CODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.membership_fragment, container, false);

        mListView = (AbsListView) mView.findViewById(R.id.membershipList);
        mListView.setClickable(true);
        /* mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Membership membership = mAdapter.getItem(position);

                redirectToMembershipDetails(membership);
            }
        }); */

        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        return mView;
    }

    class MembershipAdapter extends ArrayAdapter<Membership> {

        Context context;
        int layoutResourceId;
        ArrayList<Membership> data = null;


        public MembershipAdapter(Context context, int layoutResourceId, ArrayList<Membership> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;

            this.data = data;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {

            MembershipHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new MembershipHolder();
                holder.title = (TextView) row.findViewById(R.id.title);
                holder.summary = (TextView) row.findViewById(R.id.summary);
                holder.currentMembersipIcon = (ImageView) row.findViewById(R.id.currentMembersipIcon);
                holder.infoIcon = (ImageView) row.findViewById(R.id.icon);
                holder.contentLayout = (LinearLayout) row.findViewById(R.id.content_layout);
                row.setTag(holder);
            } else {
                holder = (MembershipHolder) row.getTag();
            }

            final Membership membership = data.get(position);

            if (membership != null) {
                holder.setData(membership);
            }

            return row;
        }
    }

    class MembershipHolder {
        public TextView title;
        public TextView summary;
        public ImageView currentMembersipIcon;
        public ImageView infoIcon;
        public LinearLayout contentLayout;

        public void setData( final Membership membership )
        {
            if ( membership == null )
            {
                return;
            }

            if ( membership.getLabel() != null ) {
                title.setText(membership.getLabel());
            }

            summary.setVisibility(View.GONE);

            currentMembersipIcon.setVisibility(View.GONE);
            if (membership.getId() != null && membership.getId().equals(MembershipsFragment.this.mCurrentMembership) )
            {
                currentMembersipIcon.setVisibility(View.VISIBLE);
            }

            infoIcon.setClickable(true);
            infoIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    redirectToMembershipDetails(membership);
                }
            });

            contentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    redirectToMembershipDetails(membership);
                }
            });
        }
    }
}
