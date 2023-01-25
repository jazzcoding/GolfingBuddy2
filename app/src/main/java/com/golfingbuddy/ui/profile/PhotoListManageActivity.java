package com.golfingbuddy.ui.profile;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;

public class PhotoListManageActivity extends SkBaseInnerActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.photo_list_manage_activity);

        PhotoViewFragment.ENTITY_TYPE entityType = (PhotoViewFragment.ENTITY_TYPE)getIntent().getExtras().getSerializable("entityType");
        int entityId = getIntent().getExtras().getInt("entityId");

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PhotoManageFragment pvFrag = PhotoManageFragment.newInstance(entityType, entityId);
        fragmentTransaction.add(R.id.root_layout, pvFrag);
        fragmentTransaction.commit();

        setActionBarLogoCounter(0);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        if( id == R.id.home ){
//            NavUtils.navigateUpFromSameTask(this);
//        }



        return super.onOptionsItemSelected(item);
    }
}