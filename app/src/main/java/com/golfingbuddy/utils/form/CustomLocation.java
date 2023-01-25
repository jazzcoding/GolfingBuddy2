package com.golfingbuddy.utils.form;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.location.Address;
import android.location.Geocoder;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.search.classes.LocationObject;
import com.golfingbuddy.ui.search.service.QuestionService;
import com.golfingbuddy.utils.SkGeocoder;

import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Locale;

/**
 * Created by jk on 12/26/14.
 */

public class CustomLocation extends EditTextPreference implements FormField
{
    protected AutoCompleteTextView mAutocomplete = null;
    protected final String TAG = "AutoCompleteEditTextPreference";
    protected ArrayAdapter mAdapter = null;
    protected ArrayList<LocationObject> mAddresslist = null;
    protected ArrayList<String> mSuggestionList = null;
    protected LocationObject mLocation = null;
    protected Context mContext = null;

    public CustomLocation(Context context)
    {
        super(context);
        init(context);
    }

    public CustomLocation(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public CustomLocation(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    protected void init(Context context)
    {
        QuestionService.getInstance().preparePreference(this);

        this.mContext = context;
        mAddresslist = new ArrayList<LocationObject>();
        mSuggestionList = new ArrayList<String>();
        mAdapter = new PlacesAutoCompleteAdapter(getContext(), android.R.layout.simple_dropdown_item_1line, mSuggestionList);
        super.setTitle(context.getResources().getString(R.string.custom_location_default_title));
        setDialogTitle(context.getResources().getString(R.string.custom_location_default_dialog_title));
    }

    @Override
    public void setTitle(java.lang.CharSequence title) {
        setDialogTitle(title);
    }

    public void setValue(LocationObject address) {

        if ( address == null )
        {
            return;
        }
        this.mLocation = address;

        savePreference();
        setSummary(getLocationString());
    }

    public void setValue(Address address) {

        if ( address == null )
        {
            return;
        }

        LocationObject location = new LocationObject(address);

        this.mLocation = location;

        savePreference();
        setSummary(getLocationString());
    }

    @Override
    public void setValue(String value) {
        if ( value == null )
        {
            return;
        }

        LocationObject location = QuestionService.getInstance().fromJson(value, LocationObject.class);

        if ( location != null )
        {
            this.mLocation = location;

            savePreference();
            setSummary(getLocationString());
        }
    }

    @Override
    public String getValue() {
        return getJsonValue();
    }

    public LocationObject getLocation() {
        return this.mLocation;
    }

    @Override
    public void setText(String value) {

    }

    protected String getLocationString()
    {
        if ( mLocation == null )
        {
            return null;
        }

        return mLocation.getAddress();
    }

    protected void onLocationInfoReady(SkGeocoder.Location[] list)
    {
        if ( mAddresslist != null ) {
            mAddresslist.clear();
        }

        if ( mSuggestionList != null )
        {
            mSuggestionList.clear();
        }

        if ( list != null && list.length > 0 ) {
            for (SkGeocoder.Location item : list) {
                mSuggestionList.add(getLocationString());
                mAddresslist.add(new LocationObject(item));
            }
        }
    }

    /**
     * the default EditTextPreference does not make it easy to
     * use an AutoCompleteEditTextPreference field. By overriding this method
     * we perform surgery on it to use the type of edit field that
     * we want.
     */
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        // find the current EditText object
        final EditText editText = (EditText)view.findViewById(android.R.id.edit);

        // copy its layout params
        ViewGroup.LayoutParams params = editText.getLayoutParams();
        ViewGroup vg = (ViewGroup)editText.getParent();
        //String curVal = editText.getText().toString();

        // remove it from the existing layout hierarchy
        vg.removeView(editText);

        android.widget.AdapterView.OnItemClickListener onClicklistner = new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if ( mAddresslist != null && mAddresslist.size() > i && mSuggestionList != null && mSuggestionList.size() > i )
            {
                mLocation = mAddresslist.get(i);
                mAutocomplete.setText(getLocationString());

                mAddresslist.clear();
                mSuggestionList.clear();
            }
            }
        };

        mAutocomplete = new AutoCompleteTextView(getContext());
        mAutocomplete.setLayoutParams(params);
        mAutocomplete.setId(android.R.id.edit);
        mAutocomplete.setText(getLocationString());
        mAutocomplete.setAdapter(new PlacesAutoCompleteAdapter(mContext, android.R.layout.simple_list_item_1));
        mAutocomplete.setOnItemClickListener(onClicklistner);


        // add the new view to the layout
        vg.addView(mAutocomplete);
    }

    /**
     * Because the baseclass does not handle this correctly
     * we need to query our injected AutoCompleteTextView for
     * the value to save
     */
    protected void onDialogClosed(boolean positiveResult)
    {
        //super.onDialogClosed(positiveResult);

        if (positiveResult)
        {
            setValue(mLocation);
        }

        callChangeListener(mLocation);
    }

    protected void savePreference()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getPreferenceName(), getJsonValue());
        editor.commit();
    }

    protected String getPreferenceName()
    {
        return getKey();
    }

    protected String getJsonValue()
    {
        return QuestionService.getInstance().toJson(mLocation);
    }

    protected LocationObject getDefaultValue()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        return QuestionService.getInstance().fromJson(prefs.getString(getPreferenceName(), ""), LocationObject.class);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
//        if (restoreValue) {
//            restoreDefaultValue();
//        } else {
            if ( mLocation == null ) {
                try {
                    String value = PreferenceManager.getDefaultSharedPreferences(getContext())
                            .getString(getPreferenceName(), "");
                    setValue(value);
                } catch (Exception ex) {
                    Log.i(" CUSTOM LOCATION get default value  ", ex.getMessage(), ex);
                }
            }
//        }
    }

//    protected void restoreDefaultValue()
//    {
//        new SKLocation.SKGoogleLocation(mContext) {
//            @Override
//            public void onConnected(Location lastLocation) {
//
//            }
//
//            @Override
//            public void onLocationInfoReady(SKLocation.SKLocationInfo skLocationInfo) {
//
//                if ( skLocationInfo != null ) {
//                    CustomLocation.this.setValue(skLocationInfo.address);
//                }
//            }
//        };
//    }


    /**
     * again we need to override methods from the base class
     */
    public EditText getEditText()
    {
        return mAutocomplete;
    }

    protected SkGeocoder.Location[] autocomplete( String input )
    {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        List<Address> addresses;
        SkGeocoder.GeocoderResult result = null;

        try {

            result = SkGeocoder.geocoder(input);

            //addresses = geocoder.getFromLocationName(input, 2);

        }
//        catch (IOException e) {
//            Log.e("LocationsSampleException", "IO Exception in getFromLocation");
//            e.printStackTrace();
//
//            return null;
//        }
        catch (IllegalFormatCodePointException e) {
            String errorString = "Illegal arguments " + input +
                    " passed to address service";
            Log.e("LocationSampleActivity", errorString);
            e.printStackTrace();

            return null;
        }

        if (result != null && result.getResult() != null && result.getResult().length > 0) {
            return result.getResult();
        }

//        if (addresses != null && addresses.size() > 0) {
//            return addresses;
//        }

        return  null;
    }

    protected class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        protected ArrayList<String> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            resultList  = new ArrayList<String>();
        }
        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId, ArrayList<String> list) {
            super(context, textViewResourceId, list);

        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public android.widget.Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        SkGeocoder.Location[] list = CustomLocation.this.autocomplete(constraint.toString());
                        resultList.clear();

                        if ( list != null && list.length > 0 ) {
                            for (SkGeocoder.Location item : list) {
                                if (item != null) {
                                    resultList.add(item.getFormattedAddress());
                                }
                            }

                            CustomLocation.this.onLocationInfoReady(list);
                        }

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};
            return filter;
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