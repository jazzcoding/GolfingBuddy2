package com.golfingbuddy.utils.form;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.golfingbuddy.R;
import com.golfingbuddy.ui.search.service.QuestionService;

/**
 * Created by jk on 2/3/16.
 */
public class Checkbox extends CheckBoxPreference implements FormField  {
    public Checkbox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Checkbox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public Checkbox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Checkbox(Context context) {
        super(context);
    }

    public void init() {
        QuestionService.getInstance().preparePreference(this);
    }

    @Override
    public void setValue(String value) {

        int val = 0;
        boolean b = false;

        if (value != null) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                Log.i("checkbox_set_value", ex.getMessage());
            }

            b = Boolean.parseBoolean(value);
        }

        setChecked(val == 1 || b);
    }

    @Override
    public String getValue() {
        return isChecked() ? "1" : "0";
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

    @Override
    protected boolean callChangeListener(Object newValue) {
        displayError();
        return super.callChangeListener(newValue);
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
                errorIcon.setVisibility(View.INVISIBLE);
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
