package com.golfingbuddy.ui.speedmatch;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;

import com.golfingbuddy.R;
import com.golfingbuddy.core.SkBaseActivity;
import com.golfingbuddy.ui.speedmatch.fragments.SpeedmatchesFilterFragment;

import java.util.HashMap;

public class SpeedmatchesFilterActivity extends SkBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.speedmatches_filter);
//        ActionBar actionBar = getActionBar();
//        View view = getLayoutInflater().inflate(R.layout.speedmatches_filter_action_bar, null);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        findViewById(R.id.speedmatches_filter_img_btn).setOnClickListener(this);
//        findViewById(R.id.speedmatches_filter_text_btn).setOnClickListener(this);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        SpeedmatchesFilterFragment speedmatchesFilter = new SpeedmatchesFilterFragment();
        HashMap<String, Object> data = (HashMap<String, Object>)getIntent().getSerializableExtra("settings");
        Bundle bundle = new Bundle();
        bundle.putSerializable("settings", data);
        speedmatchesFilter.setArguments(bundle);
        fragmentTransaction.replace(R.id.speedmatches_filter_content, speedmatchesFilter);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();

            Fragment fragment = new SpeedmatchesFilterFragment();

            fragmentTransaction.detach(fragment);
            fragmentTransaction.commit();
            finish();

            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }
}
