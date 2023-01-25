package com.golfingbuddy.utils.form;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.util.Arrays;

/**
 * Created by kairat on 12/23/14.
 */
public class SingleRangeField extends DialogPreference implements FormField {
    protected String mDialogLabel;
    protected String mSummaryLabel;
    protected Boolean mIsPredefinedMode = false;
    protected int mStep = 1;
    protected int mValue = -1;
    protected String[] mRange;

    protected Integer valueFrom = null;
    protected Integer valueTo = null;

    protected Integer defaultValueFrom = 0;
    protected Integer defaultValueTo = 60;

    protected Integer rangeFrom = 0;
    protected Integer rangeTo = 60;

    protected NumberPicker numberPicker = null;

    protected SharedPreferences prefs = null;

    public SingleRangeField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public SingleRangeField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SingleRangeField(Context context) {
        super(context, null);
        init(context);
    }

    protected void init(Context context) {
        QuestionService.getInstance().preparePreference(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setDialogLabel(String dialogLabel) {
        mDialogLabel = dialogLabel;
    }

    public void setSummaryLabel(String mSummaryLabel) {
        this.mSummaryLabel = mSummaryLabel;
    }

    public Boolean getIsPredefinedMode() {
        return mIsPredefinedMode;
    }

    public void setIsPredefinedMode(Boolean mIsPredefinedMode) {
        this.mIsPredefinedMode = mIsPredefinedMode;
    }

    public int getStep() {
        return mStep;
    }

    public void setStep(int step) {
        if (step <= 0) {
            return;
        }

        mStep = step;
    }

    @Override
    protected void onAttachedToActivity() {
        super.onAttachedToActivity();

        if (mRange.length > 0 && mValue > 0 && Arrays.asList(mRange).indexOf(mValue + "") > -1) {
            String value = prefs.getString(getKey(), mRange[Arrays.asList(mRange).indexOf(mValue + "")]);
            setValue(value);
        }
    }

//    public String getValue() {
//        if (valueFrom != null || valueTo != null) {
//            return valueFrom + "-" + valueTo;
//        }
//
//        return null;
//    }

    public String[] getValues() {
        if (valueFrom != null && valueTo != null) {
            int length = (valueTo - valueFrom);

            if (mIsPredefinedMode == true) {
                length /= mStep;
            }

            String[] values = new String[length];
            values[0] = Integer.toString(valueFrom);

            for (int i = 1, j = length - 1; i < j; i++) {
                values[i] = Integer.toString(i * mStep);
            }

            values[length] = Integer.toString(valueTo);

            return values;
        }

        return null;
    }

    public void setValue(String range) {
        if (range == null) {
            return;
        }

        valueFrom = Integer.parseInt(range);

        if (numberPicker != null) {
            numberPicker.setValue(valueFrom);
        }

        super.setDefaultValue(range);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getKey(), range);
        editor.commit();

        changeSummary();

        return;
    }

    public String getValue() {
        return String.valueOf(mValue);
    }

    public void setValue(int mValue) {
        this.mValue = mValue;
    }

    public void setRange(int from, int to) {
        rangeFrom = from;
        rangeTo = to;
        defaultValueFrom = from;
        defaultValueTo = to;
    }

    public void setRange(String[] values) {
        if (values != null && values.length > 0) {
            mRange = values;
        }
    }

    @Override
    protected View onCreateDialogView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.single_range_field, null);

        return view;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        TextView textView = (TextView) v.findViewById(R.id.single_range_label);
        textView.setText(mDialogLabel);

        numberPicker = (NumberPicker) v.findViewById(R.id.single_range_input);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        if (mRange != null) {
            numberPicker.setMaxValue(mRange.length - 1);
            numberPicker.setMinValue(0);

            numberPicker.setDisplayedValues(mRange);

            if (mValue >= 0) {
                numberPicker.setValue(Arrays.asList(mRange).indexOf(mValue + ""));
            }
            else {
                numberPicker.setValue(Integer.valueOf(mRange[0]));
            }
        }
        else
        {
            if (mIsPredefinedMode == true ) {

                numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        picker.setValue((newVal < oldVal) ? oldVal - mStep : oldVal + mStep);
                    }

                });
            }

            if (rangeFrom <= rangeTo) {
                numberPicker.setMinValue(rangeFrom);
                numberPicker.setMaxValue(rangeTo);
            }
            else
            {
                numberPicker.setMinValue(rangeFrom);
                numberPicker.setMaxValue(rangeFrom);
            }
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {

            mValue = Integer.valueOf(mRange[numberPicker.getValue()]);

            changeSummary();
            setValue(mValue);

            if (callChangeListener(getValue())) {
                persistString(getValue());
            }
        }
    }

    protected void changeSummary() {
        if (mValue > 0) {
            setSummary(mValue + " miles from " + mSummaryLabel);
        } else {
            setSummary("form " + defaultValueFrom);
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
