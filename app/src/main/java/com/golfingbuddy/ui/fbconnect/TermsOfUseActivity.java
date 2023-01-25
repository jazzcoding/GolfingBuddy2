package com.golfingbuddy.ui.fbconnect;

import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkBaseActivity;
import com.golfingbuddy.utils.SKLanguage;

/**
 * Created by kairat on 11/26/15.
 */
public class TermsOfUseActivity extends SkBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.terms_fragment);

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        SkApplication.getLanguage().getCustomPage("terms-of-use", new SKLanguage.SKLanguageCustomPageListener() {
            public void callback(String text) {
                TextView textView = (TextView) TermsOfUseActivity.this.findViewById(R.id.content);
                textView.setText(Html.fromHtml(String.format(text)));
                textView.setMovementMethod(new ScrollingMovementMethod());

                int duration = getResources().getInteger(R.integer.matches_duration);
                textView.setVisibility(View.VISIBLE);
                textView.animate().alpha(1f).setDuration(duration).setListener(null);

                TermsOfUseActivity.this.findViewById(R.id.terms_progress_bar).setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();

            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }
}
