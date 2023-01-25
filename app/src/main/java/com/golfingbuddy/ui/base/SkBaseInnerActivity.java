package com.golfingbuddy.ui.base;

/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkBaseActivity;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.model.base.classes.SkMenuItem;
import com.golfingbuddy.ui.profile.ProfileViewActivity;
import com.golfingbuddy.utils.SKConfig;
import com.golfingbuddy.utils.SKDimensions;

import com.golfingbuddy.ui.memberships.CreditsActivity;
import com.golfingbuddy.ui.memberships.MembershipAndCreditsActivity;
import com.golfingbuddy.ui.memberships.MembershipsActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This example illustrates a common usage of the DrawerLayout widget
 * in the Android support library.
 * <p/>
 * <p>When a navigation (left) drawer is present, the host activity should detect presses of
 * the action bar's Up affordance as a signal to open and close the navigation drawer. The
 * ActionBarDrawerToggle facilitates this behavior.
 * Items within the drawer should fall into one of two categories:</p>
 * <p/>
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic policies as
 * list or tab navigation in that a view switch does not create navigation history.
 * This pattern should only be used at the root activity of a task, leaving some form
 * of Up navigation active for activities further down the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an alternate
 * parent for Up navigation. This allows a user to jump across an app's navigation
 * hierarchy at will. The application should treat this as it treats Up navigation from
 * a different task, replacing the current task stack using TaskStackBuilder or similar.
 * This is the only form of navigation drawer that should be used outside of the root
 * activity of a task.</li>
 * </ul>
 * <p/>
 * <p>Right side drawers should be used for actions, not navigation. This follows the pattern
 * established by the Action Bar that navigation should be to the left and actions to the right.
 * An action should be an operation performed on the current contents of the window,
 * for example enabling or disabling a data overlay on top of the current content.</p>
 */
public abstract class SkBaseInnerActivity extends SkBaseActivity {

    public static String EVENT_ON_MENU_DATA_USE = "base.on_menu_data_use";

    protected DrawerLayout mDrawerLayout;
    protected ListView mDrawerList;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected CharSequence mDrawerTitle;
    protected CharSequence mTitle;
    protected SidebarMenuAdapter mAdapter;
    private boolean emulateBack = false;
    private int currentLogoCounter;

    public enum MENU_ITEM_ACTION{
        INCREMENT,
        DECREMENT,
        SET
    }


//    private BroadcastReceiver connectionHandler = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            System.out.println("aaaaaaaaa");
//        }
//    };

    private BroadcastReceiver sidebarMenuReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SkMenuItem.ITEM_KEY key = (SkMenuItem.ITEM_KEY)intent.getSerializableExtra("key");

            if( key != null && mAdapter.getItemByKey(key) != null ){
                SkMenuItem item = mAdapter.getItemByKey(key);
                MENU_ITEM_ACTION action = (MENU_ITEM_ACTION)intent.getSerializableExtra("action");

                if( action != null ){
                    switch (action){
                        case INCREMENT:
                            item.setCount(item.getCount()+1);
                            setActionBarLogoCounter(currentLogoCounter+1);
                            break;

                        case DECREMENT:
                            if( item.getCount() > 0 ){
                                item.setCount(item.getCount()-1);
                                setActionBarLogoCounter(currentLogoCounter-1);
                            }
                            break;

                        case SET:
                            int val = intent.getIntExtra("value", -1);

                            if( val >= 0 ){
                                item.setCount(val);
                                setActionBarLogoCounter(val);
                            }
                            break;
                    }
                }

                SidebarMenuAdapter ad = new SidebarMenuAdapter();
                ad.setmData(mAdapter.getmData());
                mDrawerList.setAdapter(ad);
            }
        }
    };

    private BroadcastReceiver siteInfoUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSidebarData(SkApplication.getMenuInfo());
        }
    };

    private TextView noIntConnection;

    private BroadcastReceiver internetConnectionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if ( !intent.getBooleanExtra("data", true) ){
                noIntConnection.setVisibility(View.VISIBLE);
            }
            else{
                noIntConnection.setVisibility(View.GONE);
            }
        }
    };

    protected void onCreate(final Bundle savedInstanceState, int layoutResourceId) {
        setTheme(R.style.SkTheme);
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.base_inner_activity);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        noIntConnection = (TextView)findViewById(R.id.no_int_connection);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(layoutResourceId, null, false);
        mDrawerLayout.addView(contentView, 0);

        // TODO remove dirty hack
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mAdapter = new SidebarMenuAdapter();
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        updateSidebarData(SkApplication.getMenuInfo());
        LocalBroadcastManager.getInstance(this).registerReceiver(sidebarMenuReceiver, new IntentFilter("base.update_sidebar_menu_item_counter"));
        LocalBroadcastManager.getInstance(this).registerReceiver(siteInfoUpdateReceiver, new IntentFilter("base.site_info_updated"));
        LocalBroadcastManager.getInstance(this).registerReceiver(internetConnectionStateReceiver, new IntentFilter(SkApplication.EVENT_INTERNET_CONNECTION_STATE));


        String token = FirebaseInstanceId.getInstance().getToken(), localToken = SKConfig.getInstance(this).getStringValue("base", "gcmToken");

        if( true || localToken == null || !localToken.equals(token) ){
            SKConfig.getInstance(this).saveConfig("base", "gcmToken", token);
            HashMap<String, String> params = new HashMap();
            params.put("token", token);
            params.put("lang", getApp().getLocalityLanguageTag());

            BaseServiceHelper.getInstance(getApplication()).runRestRequest(BaseRestCommand.ACTION_TYPE.ADD_DEVICE_TOKEN, params);
        }
    }

    private CharSequence buTitle;
    private Boolean actionBarChEnabled = false;
    private ImageView actionBarLogo;
    private Boolean drawerIndicator = false, upButton = false;

    public void setActionBarCheckCounterEnable( boolean enable ){
        setActionBarCheckCounterEnable(enable, 0);
    }

    public void setActionBarCheckCounterEnable( boolean enable, Integer initialCount ){

        actionBarChEnabled = enable;

        if( actionBarLogo == null ){
            actionBarLogo = (ImageView) this.findViewById(android.R.id.home);
        }

        if( enable ){
            buTitle = getActionBar().getTitle();
            getActionBar().setTitle(initialCount.toString());
            actionBarLogo.setVisibility(View.VISIBLE);
            actionBarLogo.setImageResource(R.drawable.mark_menu_item);

            if( mDrawerToggle.isDrawerIndicatorEnabled() ){
                mDrawerToggle.setDrawerIndicatorEnabled(false);
                drawerIndicator = true;
            }
            //TODO add logic to get the button back
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
        else{
            if( buTitle != null) {
                getActionBar().setTitle(buTitle);
            } else{
                getActionBar().setTitle("");
            }

            if( drawerIndicator ){
                mDrawerToggle.setDrawerIndicatorEnabled(true);
                drawerIndicator = false;
            }

            actionBarLogo.setVisibility(View.GONE);
        }

    }

    public void setCheckedItemsCount( Integer count ){
        if( actionBarChEnabled ){
            getActionBar().setTitle(count.toString());
        }
    }

    public void updateSidebarData(ArrayList<SkMenuItem> list, Boolean runReceiver) {
        if (runReceiver) {
            Intent intent = new Intent(EVENT_ON_MENU_DATA_USE);
            intent.putExtra("data", new Gson().toJson(list));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

        if( list == null && mAdapter != null ){
            return;
        }

        int notificationsCount = 0;

        for( SkMenuItem item : list ){
            notificationsCount += item.getCount();
        }

        mAdapter.setmData(list);
        if( !actionBarChEnabled && notificationsCount != currentLogoCounter ){
            setActionBarLogoCounter(notificationsCount);
        }

        mDrawerList.setAdapter(mAdapter);

        //mAdapter.notifyDataSetChanged();
    }

    public void updateSidebarData( ArrayList<SkMenuItem> list ){
        updateSidebarData(list, true);
    }

    public void setEmulateBackButton(Boolean emulate) {
        setActionBarLogoCounter(0);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        emulateBack = emulate;
    }

    public void setActionBarLogoCounter(int count) {
        ImageView logo = (ImageView) findViewById(android.R.id.home);

        if (count < 1) {
            currentLogoCounter = 0;
            logo.setVisibility(View.GONE);
            return;
        }
        else{
            logo.setVisibility(View.VISIBLE);
        }

        currentLogoCounter = count;

        TextView v = new TextView(getApp());
        v.setText(new Integer(count).toString());
        v.setBackgroundResource(R.drawable.sidebar_menu_counterbg);
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        v.setDrawingCacheEnabled(true);
        v.setTextColor(Color.WHITE);
        v.setPadding(SKDimensions.convertDpToPixel(6, getApp()), 0, SKDimensions.convertDpToPixel(6, getApp()), 0);

        ActionBar.LayoutParams paramsExample = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, 1);
        paramsExample.setMargins(0, 0, 0, 0);
        v.setHeight(SKDimensions.convertDpToPixel(20, getApp()));
        v.setLayoutParams(paramsExample);

// this is the important code :)
// Without it the view will have a dimension of 0,0 and the bitmap will be null
        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());

        v.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);

        logo.setImageDrawable(new BitmapDrawable(getApp().getResources(), b));
        logo.setPadding(SKDimensions.convertDpToPixel(3, getApp()), 0, 0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch (item.getItemId()) {
            case android.R.id.home:
                if (emulateBack) {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    protected Boolean checkCustomItems(SkMenuItem item) {

        if ( item.getKey().equals(SkMenuItem.ITEM_KEY.MEMBERSHIPS_AND_CREDITS.toString()) ) {
            Intent in = new Intent(this, MembershipAndCreditsActivity.class);
            startActivity(in);
            return true;
        }

        if ( item.getKey().equals(SkMenuItem.ITEM_KEY.MEMBERSHIPS.toString()) ) {
            Intent in = new Intent(this, MembershipsActivity.class);
            startActivity(in);
            return true;
        }

        if ( item.getKey().equals(SkMenuItem.ITEM_KEY.CREDITS.toString()) ) {
            Intent in = new Intent(this, CreditsActivity.class);
            startActivity(in);
            return true;
        }
        
        if (item.getKey().equals(SkMenuItem.ITEM_KEY.USER.toString())) {
            Intent in = new Intent(this, ProfileViewActivity.class);
            in.putExtra("userId", getApp().getUserInfo().getUserId());
            startActivity(in);
            return true;
        }

        if (item.getKey().equals(SkMenuItem.ITEM_KEY.LOGOUT.toString())) {
            baseHelper.userLogout();
            return true;
        }

        return false;
    }

    protected void selectItem(SkMenuItem.ITEM_KEY key) {
        SkMenuItem item = mAdapter.getItemByKey(key);

        if( item == null ){
            return;
        }

        if (checkCustomItems(item)) {
            return;
        }

        mDrawerLayout.closeDrawer(mDrawerList);
        Intent in = new Intent(this, MainFragmentActivity.class);
        in.putExtra("key", key);
        startActivity(in);
    }

    protected void selectItem(int position) {
        SkMenuItem item = mAdapter.getItem(position);
        selectItem(SkMenuItem.ITEM_KEY.fromString(item.getKey()));
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    protected class SidebarMenuAdapterItem {
        private String label;
        private int type;
        private String avatarUrl;
        private int counter;
        private String key;

        public Integer getCounter() {
            return counter;
        }

        public void setCounter(int counter) {
            this.counter = counter;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    protected class SidebarMenuAdapter extends BaseAdapter {
        private ArrayList<SkMenuItem> mData = new ArrayList<SkMenuItem>();
        private LayoutInflater mInflater;

        public SidebarMenuAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public ArrayList<SkMenuItem> getmData() {
            return mData;
        }

        public void setmData(ArrayList<SkMenuItem> mData) {
            this.mData = mData;
            notifyDataSetChanged();
        }

        public int getPositionByKey( SkMenuItem.ITEM_KEY key ){
            for( int i=0; i<mData.size(); i++ ){
                if( mData.get(i).getKey().equals(key.toString()) ){
                    return i;
                }
            }

            return -1;
        }

        public SkMenuItem getItemByKey ( SkMenuItem.ITEM_KEY key ){
            for( SkMenuItem item : mData ){
                if( item.getKey().equals(key.toString()) ){
                    return item;
                }
            }

            return null;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public SkMenuItem getItem(int position) {
            if (mData.size() == 0) {
                return null;
            }

            return mData.get(position);
        }

        public void addItem(SkMenuItem item) {
            mData.add(mData.size(), item);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 5;
        }

        @Override
        public int getItemViewType(int position) {
            return mData.get(position).getType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            int type = getItemViewType(position);

            SkMenuItem currentItem = mData.get(position);
            Integer rId = getResources().getIdentifier("sidebar_label_" + currentItem.getKey(), "string", SkBaseInnerActivity.this.getPackageName());

            if (convertView == null) {
                holder = new ViewHolder();
                switch (type) {
                    case 1:
                        convertView = mInflater.inflate(R.layout.drawer_list_item_profile, null);
                        holder.textView = (TextView) convertView.findViewById(R.id.text1);
                        holder.textView.setText(SkApplication.getUserInfo().getDisplayName());
                        holder.textView.setHeight(SKDimensions.convertDpToPixel(60, SkBaseInnerActivity.this));
                        holder.avatar = (DrawerRoundedImageView) convertView.findViewById(R.id.avatar);
                        holder.pendingImage = (ImageView) convertView.findViewById(R.id.profile_pending_image);
                        holder.pendingBackground = (ImageView) convertView.findViewById(R.id.profile_pending_background);

                        holder.pendingImage.setVisibility(View.GONE);
                        holder.pendingBackground.setVisibility(View.GONE);

                        if( SkApplication.getUserInfo().getAvatarUrl() != null ) {
                            holder.avatar.setImageUrl(SkApplication.getUserInfo().getAvatarUrl());

                            if ( !SkApplication.getUserInfo().getIsAvatarApproved() )
                            {
                                holder.pendingImage.setVisibility(View.VISIBLE);
                                holder.pendingBackground.setVisibility(View.VISIBLE);
                            }
                        }
                        else {
                            holder.avatar.setImageResource(R.drawable.rounded_rectangle_2_copy_2);
                        }

                        break;
                    case 2:
                        convertView = mInflater.inflate(R.layout.drawer_list_item, null);
                        break;
                    case 3:
                        convertView = mInflater.inflate(R.layout.drawer_list_item_bottom, null);
                        holder.textView = (TextView) convertView.findViewById(R.id.text1);
                        if( rId != null ){
                            holder.textView.setText(getResources().getString(rId));
                        }
                        else{
                            holder.textView.setText("EMPTY_LABEL");
                        }
                        holder.textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                        holder.textView.setHeight(SKDimensions.convertDpToPixel(40, SkBaseInnerActivity.this));
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if( type == 2 ){
                holder.textView = (TextView) convertView.findViewById(R.id.text1);

                if( rId != null && rId != 0){
                    holder.textView.setText(getResources().getString(rId));
                }
                else{
                    holder.textView.setText("EMPTY_LABEL");
                }

                holder.circleView = (TextView) convertView.findViewById(R.id.counter);
                holder.textView.setHeight(SKDimensions.convertDpToPixel(50, SkBaseInnerActivity.this));

                if (currentItem.getCount() > 0) {
                    Integer cnt = currentItem.getCount();
                    holder.circleView.setText(cnt.toString());
                    holder.circleView.setVisibility(View.VISIBLE);
                } else {
                    holder.circleView.setVisibility(View.INVISIBLE);
                }
            }

            return convertView;
        }
    }

    public static class ViewHolder {
        public TextView textView;
        public TextView circleView;
        public DrawerRoundedImageView avatar;
        public ImageView pendingImage;
        public ImageView pendingBackground;
    }

}