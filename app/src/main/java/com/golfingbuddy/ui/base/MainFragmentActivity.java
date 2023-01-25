package com.golfingbuddy.ui.base;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.model.base.classes.SkMenuItem;
import com.golfingbuddy.ui.base.main_activity_fragments.AboutFragment;
import com.golfingbuddy.ui.base.main_activity_fragments.TermsFragment;
import com.golfingbuddy.ui.hot_list.HotListView;
import com.golfingbuddy.ui.mailbox.conversation_list.MailboxFragment;
import com.golfingbuddy.ui.matches.MatchesList;
import com.golfingbuddy.ui.search.fragments.SearchResultList;
import com.golfingbuddy.ui.speedmatch.SpeedmatchesView;
import com.golfingbuddy.ui.user_list.fragments.BookmarkListFragment;
import com.golfingbuddy.ui.user_list.fragments.GuestListFragment;
import com.golfingbuddy.utils.SkApi;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public class MainFragmentActivity extends SkBaseInnerActivity {

    private HashMap<SkMenuItem.ITEM_KEY, Class<? extends Fragment>> fragments = new HashMap<>();

    public MainFragmentActivity() {
        super();
        fragments.put(SkMenuItem.ITEM_KEY.SEARCH, SearchResultList.class);
        fragments.put(SkMenuItem.ITEM_KEY.GUESTS, GuestListFragment.class);
        fragments.put(SkMenuItem.ITEM_KEY.BOOKMARKS, BookmarkListFragment.class);
        fragments.put(SkMenuItem.ITEM_KEY.ABOUT, AboutFragment.class);
        fragments.put(SkMenuItem.ITEM_KEY.MATCHES, MatchesList.class);
        fragments.put(SkMenuItem.ITEM_KEY.SPEED_MATCH, SpeedmatchesView.class);
        fragments.put(SkMenuItem.ITEM_KEY.TERMS, TermsFragment.class);
        fragments.put(SkMenuItem.ITEM_KEY.MAILBOX, MailboxFragment.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_main_fragment);
        boolean fragmentDetected = false;
        Bundle cBundle = getIntent().getExtras();

        if( cBundle != null )
        {
            SkMenuItem.ITEM_KEY key = (SkMenuItem.ITEM_KEY)cBundle.get("key");


            if( key == null && cBundle.getString("type") != null ){
                switch ( cBundle.getString("type") ){
                    case "message":
                        key = SkMenuItem.ITEM_KEY.MAILBOX;
                        break;

                    case "guest":
                        key = SkMenuItem.ITEM_KEY.GUESTS;
                        break;

                    case "wink":
                        key = SkMenuItem.ITEM_KEY.MAILBOX;
                        break;

                    case "speedmatch":
                        key = SkMenuItem.ITEM_KEY.MATCHES;
                        break;
                }
            }

            if( key != null ) {
                fragmentDetected = true;
                selectItem(key);
            }
        }

        if( !fragmentDetected ){
            selectItem(1);
        }

        Intent intent = getIntent();

        if (intent.hasExtra("showHotList")) {
            BaseServiceHelper.getInstance(SkApplication.getApplication()).runRestRequest(BaseRestCommand.ACTION_TYPE.HOTLIST_COUNT, new SkServiceCallbackListener() {
                @Override
                public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                    JsonObject jsonObject = SkApi.processResult(data);

                    if (jsonObject != null && !jsonObject.isJsonNull() && jsonObject.has("count") && !jsonObject.get("count").isJsonNull() && jsonObject.getAsJsonPrimitive("count").getAsInt() > 0)  {
                        startActivity(new Intent(MainFragmentActivity.this, HotListView.class));
                        overridePendingTransition(R.anim.hotlist_top_slide_in, R.anim.hotlist_top_slide_out);
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void selectItem( SkMenuItem.ITEM_KEY key ){
        int position = mAdapter.getPositionByKey(key);

        if( position > 0 ){
            selectItem(position);
        }
    }

    @Override
    protected void selectItem(int position) {

        SkMenuItem item = mAdapter.getItem(position);

        if (item == null) {
            return;
        }

        if( checkCustomItems(item) )
        {
            return;
        }


        SkMenuItem.ITEM_KEY key = SkMenuItem.ITEM_KEY.fromString(item.getKey());

        //getting fragment
        Class fragmentClass = fragments.get(key);

        if( fragmentClass == null ){
            return;
        }

        try {
            fragmentClass.newInstance();
        } catch (InstantiationException e) {
            return;
        } catch (IllegalAccessException e) {
            return;
        }


        // update the main content by replacing fragments
        Fragment fragment = null;
        try {
            Constructor<?> ctor = fragmentClass.getConstructor();
            fragment = (Fragment) ctor.newInstance();
        } catch (Exception e) {
            return;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        }

        if( mDrawerLayout.isDrawerOpen(GravityCompat.START) ) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        Intent msgIntent = new Intent(this, MainFragmentActivity.class);

        if( extras != null ){
            msgIntent.putExtras(extras);
        }

        startActivity(msgIntent);
        finish();
    }

}
