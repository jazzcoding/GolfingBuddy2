package com.golfingbuddy.utils.form;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.golfingbuddy.R;

/**
 * Created by jk on 12/30/14.
 */
public class Distance extends SingleRangeField implements FormField {

    protected String DISTANCE_UNIT_KMS = "km";
    protected String DISTANCE_UNIT_MILES = "miles";

    protected String mDistanceUnits ;
    protected Context context;

    public Distance(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public Distance(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Distance(Context context) {
        super(context);
        init(context);
    }

    protected void init(Context context)
    {
        mDistanceUnits = null;

        super.init(context);

        this.context = context;

        setTitle(context.getResources().getString(R.string.location_distance_default_title));
        setDialogTitle(context.getResources().getString(R.string.location_distance_default_dialog_title));
    }

    protected void changeSummary() {
        if ( mDistanceUnits != null ) {
            setSummary(mValue + " " + context.getResources().getString(getDistanceUnitsResorce()) + " " + context.getResources().getString(R.string.location_distance_from));
        }
        else
        {
            setSummary(mValue + " " + context.getResources().getString(R.string.location_distance_from));
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String value = "50";

        if (restoreValue) {
            if (defaultValue == null) {
                if ( mRange != null && mRange[0] != null ) {
                    value = getPersistedString(String.valueOf(mRange[0]));
                }
            } else {
                value = getPersistedString(defaultValue.toString());
            }
        } else {
            value = defaultValue.toString();
        }

        setValue(Integer.parseInt(value));
    }

    @Override
    protected View onCreateDialogView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.distance_field, null);

        return view;
    }

    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        if ( mDistanceUnits != null ) {
            TextView mDistanceUnitsView = (TextView) v.findViewById(R.id.distance_units_label);
        }
    }

    public void setDistanceUnits(String value) {

        if ( DISTANCE_UNIT_KMS.equals(value) || DISTANCE_UNIT_MILES.equals(value)) {
            mDistanceUnits = value;
            displayDistanceUnits();

            if ( DISTANCE_UNIT_KMS.equals(value)  )
            {
                setDialogTitle(context.getResources().getString(R.string.location_distance_kms_dialog_title));
            }
            else if (DISTANCE_UNIT_MILES.equals(value))
            {
                setDialogTitle(context.getResources().getString(R.string.location_distance_miles_dialog_title));
            }
        }
    }

    protected View errorIcon;
    protected TextView error;
    protected String errorMessage;

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        errorIcon = view.findViewById(R.id.error_icon);
        error = (TextView)view.findViewById(R.id.error);

        displayError();

        return view;
    }

    protected void displayDistanceUnits() {
        if ( mDistanceUnits != null )
        {
            changeSummary();
        }
    }

    protected int getDistanceUnitsResorce() {
        return DISTANCE_UNIT_KMS.equals(mDistanceUnits) ? R.string.location_distance_kms : R.string.location_distance_miles;
    }

    protected void displayError() {
        if ( errorMessage != null )
        {
            if ( error != null ) {
                error.setText(errorMessage);
                error.setVisibility(View.VISIBLE);
            }

            if ( errorIcon != null )
            {
                errorIcon.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            if ( error != null ) {
                error.setText("");
                error.setVisibility(View.GONE);
            }

            if ( errorIcon != null )
            {
                errorIcon.setVisibility(View.GONE);
            }
        }
        notifyChanged();
    }

    @Override
    public void setError(String value) {
        errorMessage = value;
        displayError();
    }
}
