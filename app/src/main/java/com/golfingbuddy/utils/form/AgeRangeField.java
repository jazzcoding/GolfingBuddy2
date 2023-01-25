package com.golfingbuddy.utils.form;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

import com.golfingbuddy.R;

/**
 * Created by jk on 12/12/14.
 */
public class AgeRangeField extends RangeField {

    public AgeRangeField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public AgeRangeField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AgeRangeField(Context context) {
        super(context, null);
        init(context);
    }

    @Override
    protected String getPreferenceName() {
        return getKey();
    }

    @Override
    protected void changeSummary() {
        Resources resources = this.getContext().getResources();

        if (valueFrom != null && valueTo != null) {
            setSummary(String.format(resources.getString(
                    R.string.speedmatches_filter_age), valueFrom, valueTo));
        } else {
            setSummary(String.format(resources.getString(
                    R.string.speedmatches_filter_age), defaultValueFrom, defaultValueTo));
        }
    }
}
