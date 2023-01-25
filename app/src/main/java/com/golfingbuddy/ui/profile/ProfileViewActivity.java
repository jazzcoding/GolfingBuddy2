package com.golfingbuddy.ui.profile;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.golfingbuddy.R;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;
import com.golfingbuddy.ui.base.classes.SlidingTabLayout;
import com.golfingbuddy.ui.mailbox.send.Send;
import com.golfingbuddy.ui.memberships.SubscribeOrBuyActivity;
import com.golfingbuddy.ui.search.classes.AuthObject;
import com.golfingbuddy.utils.SkApi;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

import retrofit.mime.TypedFile;

public class ProfileViewActivity extends SkBaseInnerActivity implements ActionBar.TabListener {

    public enum ACTION {
        MESSAGE,
        WINK,
        BOOKMARK,
        BLOCK
    }

    private ViewPager viewPager;
    private SlidingTabLayout mSlidingTabLayout;
    private Integer userId;
    private TabsPagerAdapter mAdapter;
    private int buttonCount, columnWidth;
    private GridView gridView;
    private GridViewAdapter gadapter;
    private RelativeLayout rootView;
    private LinearLayout noPremissionLayout, subscribeLayout;
    private HashMap<ACTION, ButtonItem> buttonList;
    private Boolean isOwner = false, photoView = false;
    private String photoUpload;
    private AuthObject profileView;

    private ProgressBar progressBar;
    private JsonObject data;

    private BroadcastReceiver bookMarkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Integer pUserId = intent.getIntExtra("userId", 0);

            if (pUserId.equals(0) || userId.equals(pUserId)) {
                return;
            }

            boolean status = intent.getBooleanExtra("status", false);

            ButtonItem currentItem = buttonList.get(ACTION.BOOKMARK);

            if (currentItem != null) {
                currentItem.setActive(status);
                gadapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.user_profile_view);
        userId = getIntent().getIntExtra("userId", 0);

        if (userId == 0) {
            return;
        }

        isOwner = (userId == getApp().getUserInfo().getUserId());

        if (isOwner) {
            setTitle(getResources().getString(R.string.my_profile_view_activity_title));
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("userId", userId.toString());

        refreshView(params);


        // Initilization
        viewPager = (ViewPager) findViewById(R.id.pager);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        rootView = (RelativeLayout) findViewById(R.id.root_layout);
        gridView = (GridView) findViewById(R.id.gridView);
        noPremissionLayout = (LinearLayout) findViewById(R.id.permissions);
        subscribeLayout = (LinearLayout) findViewById(R.id.subscribe);

        setEmulateBackButton(true);
        LocalBroadcastManager.getInstance(this).registerReceiver(bookMarkReceiver, new IntentFilter("base.bookmark_change_status"));
    }

    private void refreshView( HashMap<String, String> params ){
        baseHelper.runRestRequest(BaseRestCommand.ACTION_TYPE.PROFILE_INFO, params, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle bData) {
                if (SkApi.checkResult(bData) == SkApi.API_RESULT.SUCCESS) {
                    data = SkApi.processResult(bData);

                    //notify details fragment
                    Intent intent = new Intent("base.profile_view_get_data");
                    intent.putExtra("data", data.toString());
                    LocalBroadcastManager.getInstance(ProfileViewActivity.this).sendBroadcast(intent);

                    // get auth info
                    if (SkApi.propExistsAndNotNull(data, "auth")) {
                        JsonObject authData = data.getAsJsonObject("auth"), temp;

                        if (SkApi.propExistsAndNotNull(authData, "base.view_profile")) {
                            temp = authData.getAsJsonObject("base.view_profile");

                            if (SkApi.propExistsAndNotNull(temp, "status")) {
                                profileView = new Gson().fromJson(temp, AuthObject.class);
                            }
                        }

                        if (SkApi.propExistsAndNotNull(authData, "photo.upload")) {
                            temp = authData.getAsJsonObject("photo.upload");

                            if (SkApi.propExistsAndNotNull(temp, "status")) {
                                photoUpload = temp.get("status").getAsString();
                            }
                        }

                        if (SkApi.propExistsAndNotNull(authData, "photo.view")) {
                            temp = authData.getAsJsonObject("photo.view");

                            if (SkApi.propExistsAndNotNull(temp, "status")) {
                                photoView = temp.get("status").getAsString().equals("available");
                            }
                        }
                    }

                    if( !isOwner )
                    {
                        if ( profileView == null || profileView.status == null || profileView.status.equals(AuthObject.STATUS_DISABLED)) {
                            showLimitedPermissions(profileView.msg);
                            return;
                        }

                        if (profileView.status.equals(AuthObject.STATUS_PROMOTED)) {
                            showSubscribe(profileView.msg);
                            return;
                        }
                    }

                    mAdapter = new TabsPagerAdapter(getFragmentManager());
                    viewPager.setAdapter(mAdapter);

                    mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
                    mSlidingTabLayout.setViewPager(viewPager);
                    mSlidingTabLayout.setDistributeEvenly(true);
                    mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                        @Override
                        public int getIndicatorColor(int position) {
                            return getResources().getColor(R.color.tab_divider_border);
                        }
                    });

                    if( !SkApplication.isPluginActive(SkApplication.PLUGINS.PHOTO) )
                    {
                        mSlidingTabLayout.setVisibility(View.GONE);
                    }

                    if (!isOwner) {

                        if (SkApi.propExistsAndNotNull(data, "viewBasic")) {
                            JsonObject personalData = data.get("viewBasic").getAsJsonObject();

                            if (SkApi.propExistsAndNotNull(personalData, "realname")) {
                                ProfileViewActivity.this.setTitle(personalData.get("realname").getAsString());
                            }
                        }
                    }

                    buttonList = getDefaultButtons();

                    if (data.get("isBlocked").getAsBoolean() && buttonList.get(ACTION.BLOCK) != null) {
                        buttonList.get(ACTION.BLOCK).setActive(true);
                    }

                    if (data.get("isBookmarked").getAsBoolean() && buttonList.get(ACTION.BOOKMARK) != null) {
                        buttonList.get(ACTION.BOOKMARK).setActive(true);
                    }

                    if (data.get("isWinked").getAsBoolean() && buttonList.get(ACTION.WINK) != null) {
                        buttonList.get(ACTION.WINK).setActive(true);
                    }

                    if (data.get("isAdmin").getAsBoolean()) {
                        buttonList.remove(ACTION.BLOCK);
                    }

                    ArrayList<ACTION> keyList = new ArrayList<>();

                    for (ACTION action : buttonList.keySet()) {
                        keyList.add(action);
                    }

                    if (!isOwner) {
                        buttonCount = buttonList.size();
                        initGridLayout();
                        gadapter = new GridViewAdapter(ProfileViewActivity.this, R.layout.profile_view_bottom_button, keyList);
                        gridView.setAdapter(gadapter);
                        //rootView.setPadding(0, 0, 0, SKDimensions.convertDpToPixel(50, ProfileViewActivity.this));
                        gridView.setVisibility(View.VISIBLE);
                    }

                    progressBar.setVisibility(View.GONE);
                    rootView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showProgressbar()
    {
        progressBar.setVisibility(View.VISIBLE);
        rootView.setVisibility(View.GONE);
        noPremissionLayout.setVisibility(View.GONE);
        subscribeLayout.setVisibility(View.GONE);
    }

    private void showLimitedPermissions(String msg){
        progressBar.setVisibility(View.GONE);
        rootView.setVisibility(View.GONE);
        noPremissionLayout.setVisibility(View.VISIBLE);
        subscribeLayout.setVisibility(View.GONE);

        if ( msg != null && !msg.isEmpty() ) {
            ((TextView) noPremissionLayout.findViewById(R.id.permissions_text)).setText(msg);
        }
    }

    int SUBSCRIBE_ACTIVITY_RESULT_CODE = new Random().nextInt();

    private void showSubscribe(String msg){

        if ( msg != null && !msg.isEmpty() ) {
            TextView subscribeText = (TextView) subscribeLayout.findViewById(R.id.subscribe_text);
            subscribeText.setText(msg);
        }

        ImageView subscribeImg = (ImageView) subscribeLayout.findViewById(R.id.subscribe_img);
        subscribeImg.setClickable(true);
        subscribeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileViewActivity.this, SubscribeOrBuyActivity.class);
                intent.putExtra("pluginKey", "base");
                intent.putExtra("actionKey", "view_profile");

                startActivityForResult(intent, SUBSCRIBE_ACTIVITY_RESULT_CODE);
            }
        });

        progressBar.setVisibility(View.GONE);
        rootView.setVisibility(View.GONE);
        noPremissionLayout.setVisibility(View.GONE);
        subscribeLayout.setVisibility(View.VISIBLE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Crop.REQUEST_PICK:
                    beginCrop(result.getData());
                    break;
                case Crop.REQUEST_CAMERA:
                    String foo = DetailsViewFragment.getCurrentAvatarPath();
                    Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
                    Crop.of(Uri.fromFile(new File(foo)), destination).asSquare().start(this);
                    break;
                case Crop.REQUEST_CROP:
                    handleCrop(resultCode, result);
                    break;
            }
        }

        if (requestCode == SUBSCRIBE_ACTIVITY_RESULT_CODE) {
            this.showProgressbar();

            HashMap<String, String> params = new HashMap<>();
            params.put("userId", userId.toString());

            this.refreshView(params);
        }
    }
    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            new AvatarChangeTask(Crop.getOutput(result)).execute();
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private HashMap<ACTION, ButtonItem> getDefaultButtons() {
        HashMap<ACTION, ButtonItem> list = new LinkedHashMap<>();

        ButtonItem item;

        if (getApp().isPluginActive(SkApplication.PLUGINS.MAILBOX)) {
            item = new ButtonItem();
            item.setActive(false);
            item.setListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Send(ProfileViewActivity.this, userId);
                }
            });
            item.setResourceId(R.drawable.chat);
            item.setActiveResourceId(R.drawable.chat);
            list.put(ACTION.MESSAGE, item);
        }

        if (getApp().isPluginActive(SkApplication.PLUGINS.WINK)) {
            item = new ButtonItem();
            item.setActive(false);
            item.setListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ButtonItem currentItem = buttonList.get(ACTION.WINK);

                    if( currentItem.getActive() ){
                        Toast.makeText(ProfileViewActivity.this, getString(R.string.profile_view_wink_exists), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentItem.setActive(true);
                    Toast.makeText(ProfileViewActivity.this, getString(R.string.profile_view_send_wink), Toast.LENGTH_SHORT).show();

                    HashMap<String, String> params = new HashMap<>();
                    params.put("userId", userId.toString());
                    ProfileViewActivity.this.baseHelper.runRestRequest(BaseRestCommand.ACTION_TYPE.SEND_WINK, params);

                    gadapter.notifyDataSetChanged();
                }
            });
            item.setResourceId(R.drawable.wink);
            item.setActiveResourceId(R.drawable.wink_st2);
            list.put(ACTION.WINK, item);
        }

        if (getApp().isPluginActive(SkApplication.PLUGINS.BOOKMARKS)) {

            item = new ButtonItem();
            item.setActive(false);
            item.setListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ButtonItem currentItem = buttonList.get(ACTION.BOOKMARK);

                    if (currentItem.getActive()) {
                        currentItem.setActive(false);
                        Toast.makeText(ProfileViewActivity.this, getString(R.string.profile_view_bookmark_off), Toast.LENGTH_SHORT).show();
                    } else {
                        currentItem.setActive(true);
                        Toast.makeText(ProfileViewActivity.this, getString(R.string.profile_view_bookmark_on), Toast.LENGTH_SHORT).show();
                    }

                    ProfileViewActivity.this.baseHelper.bookmark(userId, currentItem.getActive(), new SkServiceCallbackListener() {
                        @Override
                        public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {

                        }
                    });

                    gadapter.notifyDataSetChanged();

                    Intent intent = new Intent("base.bookmark_change_status");
                    intent.putExtra("userId", userId);
                    intent.putExtra("status", currentItem.getActive());
                    LocalBroadcastManager.getInstance(ProfileViewActivity.this).sendBroadcast(intent);
                }
            });
            item.setResourceId(R.drawable.bookmark);
            item.setActiveResourceId(R.drawable.bookmark_st2);
            list.put(ACTION.BOOKMARK, item);
        }

        item = new ButtonItem();
        item.setActive(false);
        item.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonItem currentItem = buttonList.get(ACTION.BLOCK);

                if (currentItem.getActive()) {
                    currentItem.setActive(false);
                    Toast.makeText(ProfileViewActivity.this, getString(R.string.profile_view_block_off), Toast.LENGTH_SHORT).show();
                } else {
                    currentItem.setActive(true);
                    Toast.makeText(ProfileViewActivity.this, getString(R.string.profile_view_block_on), Toast.LENGTH_SHORT).show();
                }

                HashMap<String, String> params = new HashMap<>();
                params.put("userId", userId.toString());
                Integer block = currentItem.getActive() ? 1 : 0;
                params.put("block", block.toString());
                ProfileViewActivity.this.baseHelper.runRestRequest(BaseRestCommand.ACTION_TYPE.BLOCK_USER, params);

                gadapter.notifyDataSetChanged();
            }
        });
        item.setResourceId(R.drawable.block);
        item.setActiveResourceId(R.drawable.block_st2);
        list.put(ACTION.BLOCK, item);

        return list;
    }

    private void initGridLayout() {
        Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        columnWidth = (int) ((size.x - ((buttonCount + 1) * padding)) / buttonCount);

        gridView.setNumColumns(buttonCount);
        gridView.setColumnWidth(columnWidth);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setPadding((int) padding, (int) padding, (int) padding, (int) padding);
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        System.out.print(true);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        System.out.print(true);
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        System.out.print(true);
    }

    public class TabsPagerAdapter extends FragmentPagerAdapter {

        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {

            switch (index) {
                case 0:
                    return DetailsViewFragment.newInstance(userId, isOwner, data);
                case 1:
                    return PhotoViewFragment.newInstance(PhotoViewFragment.ENTITY_TYPE.USER, userId, isOwner, photoUpload, photoView);
            }

            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            int resource;

            switch (position) {
                case 0:
                    resource = R.string.profile_view_tab_details;
                    break;

                case 1:
                    resource = R.string.profile_view_tab_photo;
                    break;
                default:
                    resource = R.string.profile_view_tab_photo;
            }

            return getResources().getString(resource);

        }

        @Override
        public int getCount() {
            // get item count - equal to number of tabs
            if ( !SkApplication.isPluginActive(SkApplication.PLUGINS.PHOTO) )
            {
                return 1;
            }

            return 2;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bookMarkReceiver);
    }

    class GridViewAdapter extends ArrayAdapter {
        private Context context;
        private int layoutResourceId;
        private ArrayList<ACTION> data;

        public GridViewAdapter(Context context, int layoutResourceId, ArrayList<ACTION> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
                row.getLayoutParams().height = columnWidth;
                holder = new ViewHolder();
                holder.image = (ImageView) row.findViewById(R.id.button);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            ButtonItem item = buttonList.get(data.get(position));

            if (item.getActive()) {
                holder.image.setImageResource(item.getActiveResourceId());
            } else {
                holder.image.setImageResource(item.getResourceId());
            }

            holder.image.setOnClickListener(item.getListener());

            return row;
        }

        class ViewHolder {
            ImageView image;
        }
    }

    class ButtonItem {
        private int resourceId;
        private int activeResourceId;
        private Boolean active;
        private View.OnClickListener listener;

        public int getResourceId() {
            return resourceId;
        }

        public void setResourceId(int resourceId) {
            this.resourceId = resourceId;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        public View.OnClickListener getListener() {
            return listener;
        }

        public void setListener(View.OnClickListener listener) {
            this.listener = listener;
        }

        public int getActiveResourceId() {
            return activeResourceId;
        }

        public void setActiveResourceId(int activeResourceId) {
            this.activeResourceId = activeResourceId;
        }
    }

    class AvatarChangeTask extends AsyncTask<String, Integer, Integer> {
        private Uri uri;

        public AvatarChangeTask(Uri uri) {
            this.uri = uri;
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {
                TypedFile avatar = new TypedFile("image/jpeg", new File(uri.getPath()));
                JsonObject result = SkApplication.getRestAdapter().userAvatarChange(avatar, SkApplication.getUserInfo().getUserId());
                if (result != null && result.has("data")) {
                    final JsonObject personalData = result.getAsJsonObject("data");
                    ProfileViewActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FragmentManager fragmentManager = getFragmentManager();
                            Bundle bundle = new Bundle();
                            bundle.putInt("0", 0);
                            Fragment fragment = fragmentManager.getFragment(bundle, "0");
                            final ImageView avatar = (ImageView) fragment.getView().findViewById(R.id.avatar);
                            final View pendingLayout = fragment.getView().findViewById(R.id.profile_pending_layout);
                            Picasso.with(ProfileViewActivity.this).load(personalData.get("avatar").getAsJsonObject().get("bigAvatarUrl").getAsString()).centerCrop().fit().into(avatar, new Callback() {
                                @Override
                                public void onSuccess() {
                                    JsonObject avatar = personalData.get("avatar").getAsJsonObject();

                                    if (avatar.has("approved") && !avatar.get("approved").getAsBoolean()) {
                                        pendingLayout.setVisibility(View.VISIBLE);
                                    }
                                    else
                                    {
                                        pendingLayout.setVisibility(View.INVISIBLE);
                                    }
                                }

                                @Override
                                public void onError() {
                                    avatar.setImageResource(R.drawable.no_avatar);
                                }
                            });
                        }
                    });
                }

                Boolean foo = true;
            }
            catch( Exception ex )
            {
                Log.e("Avatar change", ex.getMessage(), ex);
            }

            return 1;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }
}