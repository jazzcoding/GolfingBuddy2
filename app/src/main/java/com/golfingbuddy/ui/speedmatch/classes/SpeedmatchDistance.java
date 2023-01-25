package com.golfingbuddy.ui.speedmatch.classes;

import android.content.Context;
import android.util.AttributeSet;

import com.golfingbuddy.R;
import com.golfingbuddy.utils.form.Distance;

/**
 * Created by jk on 3/5/16.
 */
public class SpeedmatchDistance extends Distance {
    public SpeedmatchDistance(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SpeedmatchDistance(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpeedmatchDistance(Context context) {
        super(context);
    }

    @Override
    protected void changeSummary() {
        if ( mDistanceUnits != null ) {
            setSummary(mValue + " " + context.getResources().getString(getDistanceUnitsResorce()));
        }
        else
        {
            setSummary(mValue + " ");
        }
    }
}
