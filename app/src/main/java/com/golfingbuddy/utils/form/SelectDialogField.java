package com.golfingbuddy.utils.form;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.search.service.QuestionService;

import java.util.Iterator;

/**
 * Created by jk on 12/12/14.
 */
public class SelectDialogField extends ListPreference implements FormField {
    public SelectDialogField(Context context) {
        super(context);
        init();
    }

    public SelectDialogField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {
        QuestionService.getInstance().preparePreference(this);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        changeSummary();
    }

    @Override
    public void setTitle(java.lang.CharSequence title) {
        super.setTitle(title);
        setDialogTitle(title);
    }

    public void setValues(java.util.Set<java.lang.String> value) {

        if ( value == null || value.size() == 0 )
        {
            return;
        }

        Iterator<String> iterator = value.iterator();

        while ( iterator.hasNext() )
        {
            String item = iterator.next();

            if ( item != null )
            {
                super.setValue(item);
                break;
            }
        }
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        changeSummary();
    }

    protected void changeSummary()
    {
        String value = this.getValue();

        CharSequence[] entries = getEntries();
        CharSequence[] entriesValues = getEntryValues();

        if ( entries == null || value == null || entriesValues == null
                || entries.length == 0 || entriesValues.length == 0 )
        {
            setSummary("");
            return;
        }

        int i = 0;

        for (CharSequence item:entriesValues)
        {
            if( item.equals(value) && entries.length > i )
            {
                setSummary(entries[i]);
                return;
            }
            i++;
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, java.lang.Object defaultValue)
    {
        super.onSetInitialValue(restoreValue, defaultValue);

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
