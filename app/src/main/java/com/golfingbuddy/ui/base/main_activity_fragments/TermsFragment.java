package com.golfingbuddy.ui.base.main_activity_fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.utils.SKLanguage;

/**
 * Created by jk on 1/19/15.
 */
public class TermsFragment extends Fragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(getResources().getString(R.string.sidebar_label_terms));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.terms_fragment, container, false);

        SkApplication.getLanguage().getCustomPage("terms-of-use", new SKLanguage.SKLanguageCustomPageListener() {
            public void callback(String text) {
                TextView textView = (TextView) view.findViewById(R.id.content);
                textView.setText(Html.fromHtml(String.format(text)));
                textView.setMovementMethod(new ScrollingMovementMethod());

                int duration = getResources().getInteger(R.integer.matches_duration);
                textView.setVisibility(View.VISIBLE);
                textView.animate().alpha(1f).setDuration(duration).setListener(null);

                view.findViewById(R.id.terms_progress_bar).setVisibility(View.GONE);
            }
        });

        return view;
    }
}
