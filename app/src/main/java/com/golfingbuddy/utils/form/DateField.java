package com.golfingbuddy.utils.form;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.search.service.QuestionService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jk on 1/12/15.
 */
public class DateField extends DialogPreference implements DatePickerDialog.OnDateSetListener, FormField
{
    public final String DATE_FORMAT = "yyyy/M/d";
    public final String MYSQL_DATETIME_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

    public final String SUMMARY_DATE_FORMAT = "MMM dd yyyy";

    protected Calendar value = null;

    protected Calendar minDate = null;
    protected Calendar maxDate = null;

    protected DatePicker mDatePicker = null;
    protected Calendar mCalendar = null;

    protected SharedPreferences mPrefs = null;

    SharedPreferences preferences;

    public DateField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public DateField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DateField(Context context) {
        super(context, null);
        init(context);
    }

    public void init(Context context) {

        QuestionService.getInstance().preparePreference(this);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        minDate = Calendar.getInstance();
        minDate.set(1900, 1, 1);

        maxDate = Calendar.getInstance();
        int year = maxDate.get(Calendar.YEAR);
        int month = maxDate.get(Calendar.MONTH);
        int day = maxDate.get(Calendar.DAY_OF_MONTH);

        minDate.set(year - 18, month, day);


    }

    @Override
    protected void onAttachedToActivity() {
        super.onAttachedToActivity();

        String value = getValue();

        if ( value == null )
        {
            value = mPrefs.getString(getPreferenceName(), "");
            setValue(value);
        }
    }

    protected Date parseDate(String date)
    {
        Date d = null;
        SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT);

        try {
            d = sd.parse(date);
        }
        catch( ParseException ex )
        {
            Log.i(" Invalid date  ", ex.getMessage(), ex);
        }

        if ( d == null ) {
            sd = new SimpleDateFormat(MYSQL_DATETIME_DATE_FORMAT);

            try {
                d = sd.parse(date);
            } catch (ParseException ex) {
                Log.i(" Invalid date  ", ex.getMessage(), ex);
            }
        }

        return d;
    }

    protected void OnsetValue()
    {
        if ( mDatePicker != null && value != null )
        {
            mDatePicker.updateDate(value.get(Calendar.YEAR),value.get(Calendar.MONTH),value.get(Calendar.DAY_OF_MONTH));
        }

        changeSummary();
    }

    public void setValue( Date d )
    {
        if ( d == null )
        {
            return;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(d);
        validateValue(c);
    }

    public void setValue( int year, int month, int day )
    {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        validateValue(c);
    }

    public void setValue( String date )
    {
        if ( date == null )
        {
            return;
        }

        Date d = null;

        d = parseDate(date);

        if ( d != null )
        {
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            validateValue(c);
        }
    }

    public void validateValue( Calendar c )
    {
        if ( c == null ) {
            return;
        }

        if ( minDate.getTime().getTime() > c.getTime().getTime() )
        {
            value = minDate;
        }
        else if ( c.getTime().getTime() > maxDate.getTime().getTime() )
        {
            value = maxDate;
        }

        if ( value == null )
        {
            value = Calendar.getInstance();
        }
        value.setTime(c.getTime());

        SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT);

        persistString(sd.format(c.getTime()));

        OnsetValue();
    }

    protected String getPreferenceName()
    {
        return getKey();
    }

    public String getValue()
    {

        if ( value != null ) {

            SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT);
            return sd.format(value.getTime());
        }

        return null;
    }

    public void setMaxYear( int maxYear )
    {
//        if ( maxYear > minDate.get(Calendar.YEAR) ) {
            int month = maxDate.get(Calendar.MONTH);
            int day = maxDate.get(Calendar.DAY_OF_MONTH);

            maxDate.set(maxYear, month, day);

            if ( value == null ) {
                setValue(maxYear, month, day);
            }
//        }
    }

    public void setMinYear( int minYear )
    {
//        if ( minYear < maxDate.get(Calendar.YEAR) ) {
            int month = minDate.get(Calendar.MONTH);
            int day = minDate.get(Calendar.DAY_OF_MONTH);

            minDate.set(minYear, month, day);

        if ( value == null ) {
            setValue(maxDate.get(Calendar.YEAR), month, day);
        }
//        }
    }

    public void updateValue()
    {

    }

    @Override
    public void setTitle(java.lang.CharSequence title) {
        super.setTitle(title);
        setDialogTitle(title);
    }

    @Override
    protected View onCreateDialogView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.date_field, null);

        return view;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        // --- FROM ---

        mDatePicker = (DatePicker) v.findViewById(R.id.date_picker);
        mDatePicker.setMinDate(minDate.getTime().getTime());
        mDatePicker.setMaxDate(maxDate.getTime().getTime());
        mDatePicker.setCalendarViewShown(false);
        mDatePicker.setSpinnersShown(true);

        if ( value != null )
        {
            mDatePicker.updateDate(value.get(Calendar.YEAR),value.get(Calendar.MONTH),value.get(Calendar.DAY_OF_MONTH));
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {

            setValue(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());

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
        String date = null;

        if ( value == null ) {
            if (restoreValue) {
                if (defaultValue == null) {
                    date = getPersistedString("");
                } else {
                    date = getPersistedString(defaultValue.toString());
                }
            } else {
                date = defaultValue.toString();
            }
        }
        else
        {
            date = getValue();
        }

        setValue(date);

        changeSummary();
    }

    protected void changeSummary()
    {
        if ( value != null ) {
            SimpleDateFormat sd = new SimpleDateFormat(SUMMARY_DATE_FORMAT);

            setSummary(sd.format(value.getTime()));
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

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

