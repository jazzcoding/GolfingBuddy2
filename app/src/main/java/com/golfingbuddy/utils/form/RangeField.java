package com.golfingbuddy.utils.form;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.search.service.QuestionService;

/**
 * Created by jk on 12/12/14.
 */
public class RangeField extends DialogPreference implements FormField {

    protected Integer valueFrom = null;
    protected Integer valueTo = null;

    protected Integer defaultValueFrom = 18;
    protected Integer defaultValueTo = 100;

    protected Integer rangeFrom = 18;
    protected Integer rangeTo = 100;

    protected NumberPicker from = null;
    protected NumberPicker to = null;

    protected SharedPreferences prefs = null;

    SharedPreferences preferences;

    public RangeField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public RangeField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RangeField(Context context) {
        super(context, null);
        init(context);
    }

    public void init(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        QuestionService.getInstance().preparePreference(this);
    }

    @Override
    protected void onAttachedToActivity() {
        super.onAttachedToActivity();

        String range = getValue();

        if ( range == null )
        {
            String value = prefs.getString(getPreferenceName(), defaultValueFrom + "-" + defaultValueTo);
            setValue(value);
        }
    }

    public static int getMinute(String time) {
        String[] pieces = time.split(":");

        return (Integer.parseInt(pieces[1]));
    }

    public void setValue( String range )
    {
        if ( range == null )
        {
            return;
        }

        String[] values = range.split("-");

        if ( values.length == 2 )
        {
            int vFrom = Integer.parseInt(values[0]);
            int vTo = Integer.parseInt(values[1]);

            if ( vFrom < rangeFrom || vFrom > rangeTo || vTo < rangeFrom || vTo > rangeTo )
            {
                return;
            }

            valueFrom = vFrom;
            valueTo = vTo;

            if( valueFrom > valueTo )
            {
                valueTo = valueFrom;
            }

            if ( from != null )
            {
                from.setValue(valueFrom);
            }

            if ( to != null )
            {
                to.setValue(valueTo);
            }

            super.setDefaultValue(range);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getPreferenceName(), range);
            editor.commit();

            changeSummary();
            return;
        }

        return;
    }

    protected String getPreferenceName()
    {
        return getKey();
    }

    public String getValue()
    {
        if ( valueFrom != null || valueTo != null ) {
            return valueFrom+"-"+valueTo;
        }

        return null;
    }

    public void setRange( int from, int to )
    {
        rangeFrom = from;
        rangeTo = to;
        defaultValueFrom = from;
        defaultValueTo = to;

//        if ( valueFrom == null || valueTo == null ) {
//            setValue(defaultValueFrom + "-" + defaultValueTo);
//        }
    }

    public void setDefaultValue()
    {
        setValue(defaultValueFrom + "-" + defaultValueTo);
    }

    @Override
    public void setTitle(java.lang.CharSequence title) {
        super.setTitle(title);
        setDialogTitle(title);
    }

    @Override
    protected View onCreateDialogView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.range_field, null);

        return view;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        // --- FROM ---

        from = (NumberPicker) v.findViewById(R.id.from);

        if ( rangeFrom != null ) {
            from.setMinValue(rangeFrom);
        }

        if ( rangeTo != null ) {
            from.setMaxValue(rangeTo);
        }

        if ( valueFrom != null ) {
            from.setValue(valueFrom);
        }
        else
        {
            from.setValue(defaultValueFrom);
        }
        from.setWrapSelectorWheel(false);

        //from.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        // --- TO ---

        to = (NumberPicker) v.findViewById(R.id.to);

        if ( rangeFrom != null ) {
            to.setMinValue(rangeFrom);
        }

        if ( rangeTo != null ) {
            to.setMaxValue(rangeTo);
        }

        if ( valueTo != null ) {
            to.setValue(valueTo);
        }
        else
        {
            to.setValue(defaultValueTo);
        }
        to.setWrapSelectorWheel(false);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {

            valueFrom = from.getValue();
            valueTo = to.getValue();

            if ( valueFrom > valueTo )
            {
                valueTo = valueFrom;
            }

            changeSummary();
            setValue(valueFrom + "-" + valueTo);

            if (callChangeListener(getValue())) {
                persistString(getValue());
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String range = null;

        if (restoreValue) {
            if (defaultValue == null) {
                range = getPersistedString(defaultValueFrom+"-"+defaultValueTo);
            } else {
                range = getPersistedString(defaultValue.toString());
            }
        } else {
            range = defaultValue.toString();
        }

        setValue(range);

        changeSummary();
    }

    protected void changeSummary()
    {
        if ( valueFrom != null && valueTo != null ) {
            setSummary(String.format(getContext().getResources().getString(
                    R.string.range_field_summary_text), valueFrom, valueTo));
        }
        else
        {
            setSummary(String.format(getContext().getResources().getString(
                    R.string.range_field_summary_text), defaultValueFrom, defaultValueTo));
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
