package com.golfingbuddy.ui.base.main_activity_fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.core.SkApplication;

/**
 * Created by jk on 1/19/15.
 */
public class AboutFragment extends Fragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(getResources().getString(R.string.about_fargment_title));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_fragment, container, false);
        TextView textView = (TextView) view.findViewById(R.id.content);
        String text = getResources().getString(R.string.about_fargment_context);
        text = String.format(text, SkApplication.getSiteInfo().getSiteName());
        textView.setText(text);

        return view;
    }
}
