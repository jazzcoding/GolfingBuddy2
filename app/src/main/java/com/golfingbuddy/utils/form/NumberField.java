package com.golfingbuddy.utils.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.search.service.QuestionService;

public class NumberField extends DialogPreference implements FormField {

    protected int value = 0;
    protected int maxValue = 100;
    protected int minValue = 0;
    protected int defaultValue = 20;

    protected NumberPicker np= null;

    public NumberField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public NumberField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NumberField(Context context) {
        super(context, null);
        init();
    }

    protected void init() {
        QuestionService.getInstance().preparePreference(this);
    }

    protected void setMaxValue(int value) {
        maxValue = value;
    }

    protected void setMinValue(int value) {
        minValue = value;
    }

    protected void setDefaultValue(int value) {
        minValue = value;
    }

    protected int getMaxValue() {
        return maxValue;
    }

    protected int getMinValue() {
        return minValue;
    }

    protected int getDefaultValue() {
        return minValue;
    }

    public void setValue(int value) {

        if ( value < minValue )
        {
            this.value = minValue;
        }
        else if ( value > maxValue )
        {
            this.value = maxValue;
        }
        else
        {
            this.value = value;
        }
        persistString(String.valueOf(value));
        changeSummary();
    }

    @Override
    protected View onCreateDialogView() {
        np = new NumberPicker(getContext());
        np.setWrapSelectorWheel(false);
        return (np);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        np.setMaxValue(maxValue);
        np.setMinValue(minValue);

        np.setValue(value);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {

            value = np.getValue();
            setValue(value);

            if (callChangeListener(value)) {
                persistString(String.valueOf(value));
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String value = null;

        if (restoreValue) {
            if (defaultValue == null) {
                value = getPersistedString(String.valueOf(this.defaultValue));
            } else {
                value = getPersistedString(defaultValue.toString());
            }
        } else {
            value = defaultValue.toString();
        }

        setValue(Integer.parseInt(value));
    }

    protected void changeSummary() {

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

    public void setValue(String value) {
        setValue(Integer.parseInt(value));
    }

    @Override
    public String getValue() {
        return String.valueOf(value);
    }
}
