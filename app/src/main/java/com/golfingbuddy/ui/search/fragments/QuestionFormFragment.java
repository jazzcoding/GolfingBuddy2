package com.golfingbuddy.ui.search.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.search.service.QuestionService;
import com.golfingbuddy.utils.form.CustomLocation;

import java.util.HashMap;

public abstract class QuestionFormFragment extends PreferenceFragment {

    protected int requestId = -1;
    protected LinearLayout layout;
    protected ProgressBar progressBar;
    protected HashMap<String, Object> loadedData;

    public QuestionFormFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
        setPreferenceScreen(screen);

        this.loadData();
    }

    protected abstract String getPreferencePrefix();

    protected abstract void loadData();

    public void renderLocation( final PreferenceGroup screen)
    {
        final CustomLocation field = new CustomLocation(getActivity());
        field.setKey(getPreferencePrefix() + QuestionService.getInstance().QUESTION_NAME_CUSTOM_LOCATION);

        screen.addPreference(field);

        field.setDependency(getPreferencePrefix()+QuestionService.getInstance().QUESTION_NAME_CURRENT_LOCATION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View preferenceView = super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.question_form, container, false);

        // Set the adapter
        layout = (LinearLayout) view.findViewById(R.id.question_form);
        layout.addView(preferenceView);

        progressBar = (ProgressBar) view.findViewById(R.id.question_form_progressbar);
        //hide();
        return view;
    }

    public void show()
    {
        int duration = getResources().getInteger(R.integer.matches_duration);


        layout.setAlpha(0f);
        layout.setVisibility(View.VISIBLE);
        layout.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);


        progressBar.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        progressBar.setVisibility(View.GONE);
                        layout.setVisibility(View.VISIBLE);
                    }
                });
    }

    public void hide()
    {
        int duration = getResources().getInteger(R.integer.matches_duration);

        layout.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        layout.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });

        progressBar.setVisibility(View.VISIBLE);
        progressBar.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);
    }
}
