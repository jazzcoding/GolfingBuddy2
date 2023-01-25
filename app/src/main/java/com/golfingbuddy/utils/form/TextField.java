package com.golfingbuddy.utils.form;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.search.service.QuestionService;

/**
 * Created by jk on 12/15/14.
 */
public class TextField extends EditTextPreference implements FormField {

    public TextField(Context context) {
        super(context);
        init();
    }

    public TextField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TextField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TextField(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
        QuestionService.getInstance().preparePreference(this);
    }

    @Override
    public void setTitle(java.lang.CharSequence title) {
        super.setTitle(title);
        setDialogTitle(title);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        changeSummary();
    }

    public void setValue(String value) {
        setText(value);
    }

    @Override
    public String getValue() {
        return this.getText();
    }

    public void setText(String value) {
        super.setText(value);
        changeSummary();
    }

    protected void changeSummary()
    {
        String value = this.getText();

        setSummary(value);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, java.lang.Object defaultValue)
    {
        String text = null;

        if ( getValue() == null ) {
            if (restoreValue) {
                if (defaultValue == null) {
                    text = getPersistedString("");
                } else {
                    text = getPersistedString(defaultValue.toString());
                }
            } else {
                text = defaultValue.toString();
            }
        }
        else
        {
            text = getValue();
        }

        setValue(text);

        changeSummary();
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
