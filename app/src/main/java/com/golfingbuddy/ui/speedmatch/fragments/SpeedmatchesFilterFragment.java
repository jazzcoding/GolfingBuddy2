package com.golfingbuddy.ui.speedmatch.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.speedmatch.classes.SpeedmatchDistance;
import com.golfingbuddy.utils.form.AgeRangeField;
import com.golfingbuddy.utils.form.MultiCheckboxDialogField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kairat on 12/18/14.
 */
public class SpeedmatchesFilterFragment extends PreferenceFragment {

    private HashMap<String, Object> settings;

    public SpeedmatchesFilterFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        this.settings = (HashMap<String, Object>)bundle.getSerializable("settings");

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
        setPreferenceScreen(screen);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        ArrayList<HashMap<String, String>> matchSexData = (ArrayList<HashMap<String, String>>)this.settings.get("matchSexData");
        ArrayList<String> matchSex = (ArrayList<String>) this.settings.get("matchSex");
        CharSequence[] showMeEntriesValue = new CharSequence[matchSexData.size()];
        CharSequence[] showMeEntries = new CharSequence[matchSexData.size()];
        Set<String> values = new HashSet<>();

        for (int i = 0, j = matchSexData.size(); i < j; i++) {
            HashMap<String, String> data = matchSexData.get(i);
            showMeEntries[i] = data.get("label");
            showMeEntriesValue[i] = data.get("id");

            if (matchSex.indexOf(data.get("id")) >= 0) {
                values.add(data.get("id"));
            }
        }

        MultiCheckboxDialogField matchSexField = new MultiCheckboxDialogField(getActivity());
        matchSexField.setTitle(getResources().getString(R.string.speedmatches_filter_show_me_title));
        matchSexField.setKey("speedmatches_show_me");
        matchSexField.setEntries(showMeEntries);
        matchSexField.setEntryValues(showMeEntriesValue);
        matchSexField.setValues(values);
        getPreferenceScreen().addPreference(matchSexField);

        AgeRangeField matchAgeField = new AgeRangeField(getActivity());
        matchAgeField.setTitle(getResources().getString(R.string.speedmatches_filter_age_title));
        matchAgeField.setKey("speedmatches_age");

        String matchAgeValue = ((String)this.settings.get("matchAge"));
        matchAgeField.setValue(matchAgeValue);

        String defaultAgeRange = ((String)this.settings.get("defaultAgeRange"));
        String[] defaultAgeRangeValues = defaultAgeRange.split("-");

        if (defaultAgeRangeValues.length == 2) {
            int defFrom = Integer.parseInt(defaultAgeRangeValues[0]);
            int defTo = Integer.parseInt(defaultAgeRangeValues[1]);
            matchAgeField.setRange(defFrom, defTo);

            SharedPreferences settings = getPreferenceManager().getSharedPreferences();
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("speedmatches_age_range", defaultAgeRange);
            editor.commit();
        } else {
            matchAgeField.setRange(18, 100);
        }

        getPreferenceScreen().addPreference(matchAgeField);

        String[] distaceRange = {"5", "25", "50", "100"};
        SpeedmatchDistance distanceField = new SpeedmatchDistance(getActivity());
        if (settings.get("distanceUnits") != null) {
            distanceField.setDistanceUnits((String) settings.get("distanceUnits"));
        }
        distanceField.setTitle(R.string.speedmatches_filter_distance);

        if (settings.get("googlemapLocation") != null) {
            HashMap<String, Object> googlemapLocation = (HashMap<String, Object>)settings.get("googlemapLocation");

            if (googlemapLocation.containsKey("address")) {
                distanceField.setSummaryLabel(googlemapLocation.get("address").toString());
            }

            Object distance = googlemapLocation.get("distance");

            if (distance instanceof Double) {
                distanceField.setValue(((Double) distance).intValue());
            }
            else if (distance instanceof String) {
                if (! ((String) distance).isEmpty() )
                {
                    distanceField.setValue(Integer.valueOf((distance + "")));
                }
            } else {
                distanceField.setValue((Integer) distance);
            }
        }

        distanceField.setKey("speedmatches_distance");
        distanceField.setRange(distaceRange);

        getPreferenceScreen().addPreference(distanceField);

        return view;
    }
}
