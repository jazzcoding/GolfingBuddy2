package com.golfingbuddy.ui.search;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;
import com.golfingbuddy.ui.search.fragments.EditFormFragment;

/**
 * Created by jk on 1/15/15.
 */
public class EditProfileActivity extends SkBaseInnerActivity {
    EditFormFragment fragment;

    public EditProfileActivity() {
        //super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState,R.layout.activity_search_filter);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        fragment = new EditFormFragment();

        fragmentTransaction.replace(R.id.filter, fragment);
        fragmentTransaction.commit();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setEmulateBackButton(true);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home || id == android.R.id.home || id == R.id.homeAsUp) {
            Intent intent = new Intent();
            intent.putExtra("isNeedToModerate", fragment.getIsNeedToModerate());
            setResult(RESULT_OK, intent);
            this.finish();
            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }
}
