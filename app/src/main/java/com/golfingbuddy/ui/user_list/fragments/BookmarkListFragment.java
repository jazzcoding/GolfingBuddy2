package com.golfingbuddy.ui.user_list.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jk on 11/19/14.
 */

public class BookmarkListFragment extends SimpleUserListFragment {

    private BroadcastReceiver mBookmarkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String userId = String.valueOf(intent.getIntExtra("userId", 0));
            Boolean status = intent.getBooleanExtra("status", false);

            if ( status == false )
            {
                if( content.containsKey(userId) )
                {
                    if ( mAdapter != null ) {
                        mAdapter.remove(content.get(userId));
                    }
                    content.remove(userId);
                }

                if ( contentOrder.contains(userId) )
                {
                    contentOrder.remove(userId);
                }

                if ( showedItems.contains(userId) )
                {
                    contentOrder.remove(userId);
                }
            }
            else
            {
                loadMore = true;
            }
        }
    };

    public static BookmarkListFragment newInstance() {

        BookmarkListFragment fragment = new BookmarkListFragment();
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBookmarkReceiver, new IntentFilter("base.bookmark_change_status"));

        // TODO: Change Adapter to display your content
        mAdapter = new SimpleUserListAdapter((Context) getActivity(), R.layout.bookmark_list_item, new ArrayList<HashMap<String, String>>() );

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(getResources().getString(R.string.bookmarks_list_title));
    }

    protected void loadData( final boolean renderList )
    {
        if ( sendRequest == true )
        {
            return;
        }

        sendRequest = true;
        BaseServiceHelper helper = BaseServiceHelper.getInstance(getActivity().getApplication());

        requestId = helper.runRestRequest(BaseRestCommand.ACTION_TYPE.BOOKMARK_LIST, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int rId, Intent requestIntent, int resultCode, Bundle bundle) {
                try {
                    String result = bundle.getString("data");

                    if (result != null) {
                        JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                        JsonElement listData = dataObject.get("data");
                        JsonElement type = dataObject.get("type");

                        if ( listData != null && listData.isJsonObject() && type != null )
                        {
                            listData = listData.getAsJsonObject().get("list");

                            if ( listData != null && listData.isJsonArray() && type != null && !type.isJsonNull() && "success".equals(type.getAsString()) )
                            {
                                SimpleListJsonParcer parcer = new SimpleListJsonParcer(content);
                                parcer.parce(listData.getAsJsonArray());

                                HashMap<String, HashMap<String, String>> loadedData = parcer.getData();
                                ArrayList<String> order = parcer.getOrder();

                                if ( loadedData.size() == 0 || loadedData.size() < 50 ) {
                                    loadMore = false;
                                }

                                content.putAll(loadedData);
                                contentOrder.addAll(order);
                            }
                        }
                    }
                }
                finally {

                    if (renderList) {
                        renderList();
                    }

                    sendRequest = false;
                    requestId = 0;
                }

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        TextView v = (TextView)view.findViewById(R.id.textView);
        v.setText(R.string.empty_bookmark_list_text);

        return view;
    }

    @Override
    public void onDestroy()
    {
        if ( getActivity() != null ) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBookmarkReceiver);
        }
        super.onDestroy();
    }
}
