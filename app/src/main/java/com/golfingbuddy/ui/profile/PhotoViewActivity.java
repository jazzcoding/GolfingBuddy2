package com.golfingbuddy.ui.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.golfingbuddy.R;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkBaseActivity;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class PhotoViewActivity extends SkBaseActivity {

    private int position;
    private PhotoViewFragment.ENTITY_TYPE entityType;
    private int entityId;
    private boolean owner;
    private ArrayList<String> photoIdList = new ArrayList();
    private ArrayList<Photo> photoUrlList = new ArrayList();
    private BaseServiceHelper baseHelper;
    private boolean actionBarVisible = false;
    private ViewPager pager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.PhotoViewTheme);
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.photoview_activity);
        baseHelper = BaseServiceHelper.getInstance(getApplication());

        position = getIntent().getIntExtra("position", 0);
        entityType = (PhotoViewFragment.ENTITY_TYPE)getIntent().getSerializableExtra("type");
        entityId = getIntent().getIntExtra("entityId", 0);
        owner = ( (SkApplication)getApplication()).getUserInfo().getUserId() == entityId;

        pager = (ViewPager)findViewById(R.id.pager);
        pager.setPageTransformer(true, new DepthPageTransformer());
        pager.setOffscreenPageLimit(6);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().hide();
        ImageView logo = (ImageView) findViewById(android.R.id.home);
        logo.setVisibility(View.INVISIBLE);

        SkServiceCallbackListener listener = new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                String result = data.getString("data");
                if (result != null) {
                    JsonObject a = new Gson().fromJson(result, JsonObject.class);
                    if( a.has("data") ){
                        a = a.getAsJsonObject("data");
                        JsonArray list = a.getAsJsonArray("list");
                        JsonObject currentObject;

                        for( int i = 0; i < list.size(); i++ ){
                            currentObject = list.get(i).getAsJsonObject();

                            if( currentObject != null && currentObject.has("mainUrl") ){
                                photoUrlList.add(new Photo(
                                        currentObject.get("mainUrl").getAsString(),
                                        currentObject.get("approved").getAsBoolean()
                                ));

                                if( currentObject.has("id") ){
                                    photoIdList.add(currentObject.get("id").getAsString());
                                }
                            }


                        }
                    }
                }
                pager.setAdapter(new FullScreenImageAdapter(PhotoViewActivity.this, photoUrlList));
                pager.setCurrentItem(position);
            }
        };

        switch ( entityType ){
            case USER:
                baseHelper.getUserPhotoList(entityId, listener);
                break;

            case ALBUM:
                baseHelper.getAlbumPhotoList(entityId, listener);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        int resource = owner ? R.menu.photoview_activity_owner : R.menu.photoview_activity_guest;
        getMenuInflater().inflate(resource, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        String photoId = "";
        if( photoIdList.size() > position ) {
            photoId = photoIdList.get(position);
        }

        final String finalPhotoId = photoId;

        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_flag) {

            DialogInterface.OnClickListener d = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("entityId", finalPhotoId);
                    params.put("entityType", "photo");
                    params.put("reason", String.valueOf(which));

                    baseHelper.runRestRequest(BaseRestCommand.ACTION_TYPE.FLAG_CONTENT, params);

                    Toast feedback = Toast.makeText(getApplication(), R.string.photo_flag_message, 3000);
                    feedback.show();
                }

            };

            AlertDialog alert = new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.photo_flag_alert_title)
                    .setItems(R.array.flagReasonList, d)
                    .show();

            return true;
        }

        if (id == R.id.action_delete) {

            AlertDialog alert = new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.photo_delete_alert_title)
                    .setMessage(R.string.photo_delete_alert_message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if ( finalPhotoId == null || finalPhotoId.isEmpty() )
                            {
                                return;
                            }

                            ArrayList<Integer> photoList = new ArrayList<Integer>();
                            photoList.add(Integer.parseInt(finalPhotoId));

                            HashMap<String, String> params = new HashMap<>();
                            params.put("idList", TextUtils.join(",", photoList));
                            baseHelper.runRestRequest(BaseRestCommand.ACTION_TYPE.PHOTO_DELETE, params);

                            //notify parent activity about list changes
                            Intent intent = new Intent("photo.manage_list_delete");
                            intent.putIntegerArrayListExtra("idList", photoList);
                            LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);

                            photoUrlList.remove(position);
                            photoIdList.remove(position);

                            FullScreenImageAdapter mAdapter = new FullScreenImageAdapter(PhotoViewActivity.this, photoUrlList);

                            pager.setAdapter(mAdapter);
                            int count = pager.getAdapter().getCount();

                            if ( count > position )
                            {
                                pager.setCurrentItem(position, false);
                            }
                            else if ( count > 0 && position == count )
                            {
                                pager.setCurrentItem(position - 1, true);
                            }
                            else
                            {
                                pager.setCurrentItem(0, false);
                                onBackPressed();
                            }

                            Toast feedback = Toast.makeText(getApplication(), R.string.photo_delete_message, 3000);
                            feedback.show();
                        }
                    })
                    .setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();

            return true;
        }

        if( id == R.id.home || id == android.R.id.home || id == R.id.homeAsUp ){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public class FullScreenImageAdapter extends PagerAdapter {

        private Activity activity;
        private ArrayList<Photo> imagePaths;
        private LayoutInflater inflater;

        // constructor
        public FullScreenImageAdapter(Activity activity, ArrayList<Photo> imagePaths) {
            this.activity = activity;
            this.imagePaths = imagePaths;
        }

        @Override
        public int getCount() {
            return this.imagePaths.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((RelativeLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View viewLayout = inflater.inflate(R.layout.photoview_activity_photo, container, false);
            viewLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if( actionBarVisible ) {
                        getActionBar().hide();
                        actionBarVisible = false;
                    }
                    else {
                        getActionBar().show();
                        actionBarVisible = true;
                    }
                }
            });

            final ImageView imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDisplay);
            final ProgressBar progressBar = (ProgressBar) viewLayout.findViewById(R.id.progressBar);

            final Photo photo = photoUrlList.get(position);
            
            Picasso.with(getApplication()).load(photoUrlList.get(position).getUrl()).into(imgDisplay, new Callback(){
                @Override
                public void onSuccess() {
                    imgDisplay.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    
                    if ( !photo.getIsApproved() ) {

                        ColorFilter filter = new LightingColorFilter(0xFFFFFFFF, 0x55666666);

                        imgDisplay.setAlpha((float)0.3);
                        imgDisplay.setColorFilter(filter);
                        viewLayout.findViewById(R.id.photo_view_pending).setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onError() {

                }
            });

            ((ViewPager) container).addView(viewLayout);

            return viewLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((RelativeLayout) object);

        }
    }

    private class Photo {
        private String mUrl;
        private Boolean mIsApproved;

        public Photo(String url, Boolean isApproved) {
            mUrl = url;
            mIsApproved = isApproved;
        }

        public String getUrl() {
            return mUrl;
        }

        public void setUrl(String mUrl) {
            this.mUrl = mUrl;
        }

        public Boolean getIsApproved() {
            return mIsApproved;
        }

        public void setIsApproved(Boolean mIsApproved) {
            this.mIsApproved = mIsApproved;
        }
    }
}

class DepthPageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        } else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);

        } else if (position <= 1) { // (0,1]
            // Fade the page out.
            view.setAlpha(1 - position);

            // Counteract the default slide transition
            view.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = MIN_SCALE
                    + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}