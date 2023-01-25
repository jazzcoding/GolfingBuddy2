package com.golfingbuddy.ui.profile;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;

public class AlbumPhotoListViewActivity extends SkBaseInnerActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.album_photo_list_view_activity);
        setEmulateBackButton(true);
        int albumId = getIntent().getExtras().getInt("albumId");
        int userId = getIntent().getExtras().getInt("userId");
        setTitle(getIntent().getExtras().getString("name"));

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PhotoViewFragment pvFrag = PhotoViewFragment.newInstance(PhotoViewFragment.ENTITY_TYPE.ALBUM, albumId, getApp().getUserInfo().getUserId() == userId, "", false );
        fragmentTransaction.add(R.id.root_layout, pvFrag);
        fragmentTransaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}