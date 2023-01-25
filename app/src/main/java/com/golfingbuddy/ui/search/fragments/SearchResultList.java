package com.golfingbuddy.ui.search.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.matches.classes.SlideData;
import com.golfingbuddy.ui.matches.fragments.UserSlide;
import com.golfingbuddy.ui.memberships.SubscribeOrBuyActivity;
import com.golfingbuddy.ui.search.FilterActivity;
import com.golfingbuddy.ui.search.classes.AuthObject;
import com.golfingbuddy.ui.search.service.QuestionService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jk on 1/5/15.
 */
public class SearchResultList extends Fragment {
    protected  UserSlide mUserSlide;
    protected  View mView;

    protected int requestId = -1;

    public static int SUBSCRIBE_ACTIVITY_RESULT_CODE = 4567;

    protected final int ARG_ITEMS_PER_LOAD= 150;

    protected int mStartActivityResultId = 2121;
    protected String mAuthUserSearch = null;
    protected String mAuthUserSearchMsg = null;

    public static SearchResultList newInstance() {

        SearchResultList fragment = new SearchResultList();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }
    //
    public SearchResultList() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        QuestionService.getInstance().updateCurrentLocation(getActivity());
        mUserSlide = new UserSlide();
        mUserSlide.setCount(ARG_ITEMS_PER_LOAD);
        mUserSlide.setDrawPhotoIndex(true);
        mUserSlide.setEmptyLabel(getActivity().getResources().getString(R.string.search_result_no_users_text));
        mUserSlide.setUserSlideListener(new UserSlide.UserSlideListener());
    }

    public class SearchResultData {
        public SearchResultUser[] list;
        public String total;
        public SearchResultAuth auth;
    }

    public class SearchResultAuth {
        public AuthObject photo_view;
        public AuthObject base_search_users;
    }

    public class SearchResultUser {
        public String userId;
        public SearchResultPhoto[] photos;
        public String avatar;
        public String name;
        public String displayName;
        public String label;
        public String labelColor;
        public String location;
        public String ages;
        public String bookmarked;
    }

    public class SearchResultPhoto {
        public String src;
    }

    protected void loadData() {

        BaseServiceHelper helper = BaseServiceHelper.getInstance(getActivity().getApplication());

        requestId = helper.getUserList( QuestionService.getInstance().getFilterData(getActivity(), SearchFormFragment.QUESTION_PREFIX),
                0, ARG_ITEMS_PER_LOAD, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle bundle) {

                HashMap<String, Object> user = new HashMap<String, Object>();

                SearchResultData srd = null;

                try {
                    String result = bundle.getString("data");

                    if (result != null) {
                        JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                        JsonElement listData = dataObject.get("data");
                        JsonElement type = dataObject.get("type");

                        if ( listData != null && listData.isJsonObject() && type != null && "success".equals(type.getAsString()) )
                        {
                            JsonObject data = listData.getAsJsonObject();

                            Gson g = new GsonBuilder().create();
                            srd = g.fromJson(listData, SearchResultData.class);
                        }
                    }
                }
                catch( Exception ex )
                {
                    Log.i(" build search form  ", ex.getMessage(), ex);
                }
                finally {
                    SearchResultList.this.requestId = -1;
                    if ( getActivity() != null ) {
                        renderData(srd);
                    }
                }
            }
        });
    }

    protected ArrayList<SlideData> getSlideData( SearchResultUser[] list, SearchResultAuth auth )
    {
        ArrayList<SlideData> result = new ArrayList<SlideData>();

        if ( list != null && list.length > 0 )
        {
            for ( SearchResultUser item:list )
            {
                SlideData data = new SlideData();
                data.setUserId(Integer.parseInt(item.userId));
                data.setAges(Integer.parseInt(item.ages));
                data.setBookmarked(Boolean.parseBoolean(item.bookmarked));
                data.setDisplayName(item.displayName);
                data.setLabel(item.label);
                data.setLabelColor(item.labelColor);
                data.setLocation(item.location);
                data.setIsSubscribe(false);

                data.setIsAuthorized(true);

                ArrayList<String> photoList = new ArrayList<String>();

                if ( item.avatar != null && !item.avatar.isEmpty() )
                {
                    data.setAvatar(item.avatar);
                }

                if ( auth != null && auth.photo_view != null  ) {
                    data.setIsSubscribe(false);
                    if ( AuthObject.STATUS_AVAILABLE.equals(auth.photo_view.status) ) {
                        // set autorized to view photo
                        data.setIsAuthorized(true);
                    }
                    else if( AuthObject.STATUS_PROMOTED.equals( auth.photo_view.status ) )
                    {
                        data.setIsAuthorized(false);
                        data.setIsSubscribe(true);
                        if ( auth.photo_view.msg != null && !auth.photo_view.msg.isEmpty() ) {
                            data.setAuthorizeMsg(auth.photo_view.msg);
                        }
                    }
                    else
                    {
                        data.setIsAuthorized(false);
                        if ( auth.photo_view.msg != null && !auth.photo_view.msg.isEmpty() ) {
                            data.setAuthorizeMsg(auth.photo_view.msg);
                        }
                    }


                }

                if ( item.photos != null && item.photos.length > 0 ) {
                    for( SearchResultPhoto photo:item.photos )
                    {
                        photoList.add(photo.src);
                    }
                }
                data.setPhotos(photoList);

                result.add(data);
            }
        }

        return result;
    }

    protected void renderData( SearchResultData srd ) {
        if ( srd != null ) {
            if ( srd.auth != null && srd.auth.base_search_users != null  ) {

                mAuthUserSearch = srd.auth.base_search_users.status;
                mAuthUserSearchMsg = srd.auth.base_search_users.msg;

                View view = this.getView();
                if ( AuthObject.STATUS_AVAILABLE.equals(mAuthUserSearch) ) {
                    ArrayList<SlideData> data = getSlideData(srd.list, srd.auth);
                    mUserSlide.setSlideData(data);

                    if( this.mView != null )
                    {
                        this.mView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                        this.mView.findViewById(R.id.list).setVisibility(View.VISIBLE);
                        this.mView.findViewById(R.id.permissions).setVisibility(View.GONE);
                        this.mView.findViewById(R.id.subscribe).setVisibility(View.GONE);
                    }
                }
                else if ( AuthObject.STATUS_PROMOTED.equals(mAuthUserSearch) ) {
                    ArrayList<SlideData> data = getSlideData(srd.list, srd.auth);
                    mUserSlide.setSlideData(data);

                    if( this.mView != null )
                    {
                        if ( mAuthUserSearchMsg != null && !mAuthUserSearchMsg.isEmpty() ) {
                            ((TextView) this.mView.findViewById(R.id.subscribe_text)).setText(mAuthUserSearchMsg);
                        }

                        this.mView.findViewById(R.id.subscribe).setClickable(true);

                        this.mView.findViewById(R.id.subscribe).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), SubscribeOrBuyActivity.class);
                                intent.putExtra("pluginKey", "base");
                                intent.putExtra("actionKey", "search_users");

                                startActivityForResult(intent, SUBSCRIBE_ACTIVITY_RESULT_CODE);
                            }
                        });

                        this.mView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                        this.mView.findViewById(R.id.list).setVisibility(View.GONE);
                        this.mView.findViewById(R.id.permissions).setVisibility(View.GONE);
                        this.mView.findViewById(R.id.subscribe).setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    if( this.mView != null )
                    {
                        if ( mAuthUserSearchMsg != null && !mAuthUserSearchMsg.isEmpty() ) {
                            ((TextView) this.mView.findViewById(R.id.permissions_text)).setText(mAuthUserSearchMsg);
                        }

                        this.mView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                        this.mView.findViewById(R.id.list).setVisibility(View.GONE);
                        this.mView.findViewById(R.id.permissions).setVisibility(View.VISIBLE);
                        this.mView.findViewById(R.id.subscribe).setVisibility(View.GONE);
                    }
                }
            }
        }
        else
        {
            mUserSlide.setSlideData(new ArrayList<SlideData>());
        }

        //mUserSlide.show();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_result, container, false);
        this.mView = view;
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.list, this.mUserSlide);
        fragmentTransaction.commit();

        loadData();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_result_menu, menu);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(getResources().getString(R.string.search_result_title));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == mStartActivityResultId )
        {
            mUserSlide.hide();
            loadData();
        }

        if ( requestCode == SUBSCRIBE_ACTIVITY_RESULT_CODE )
        {
            mUserSlide.hide();
            loadData();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_result_filter:

                mUserSlide.hide();
                Intent intent = new Intent(getActivity(), FilterActivity.class);
                startActivityForResult(intent, mStartActivityResultId);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onStop() {
//
//        if ( requestId > 0 )
//        {
//            BaseServiceHelper helper = new BaseServiceHelper(getActivity().getApplication());
//            helper.cancelCommand(requestId);
//        }
//
//        super.onStop();
//    }
}
