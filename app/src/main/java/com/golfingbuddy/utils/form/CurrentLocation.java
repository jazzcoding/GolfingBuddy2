package com.golfingbuddy.utils.form;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.golfingbuddy.R;
import com.google.android.gms.common.ConnectionResult;
import com.golfingbuddy.ui.search.service.QuestionService;
import com.golfingbuddy.utils.SKLocation;

/**
 * Created by jk on 12/26/14.
 */
public class CurrentLocation extends SwitchPreference implements FormField {

    protected Address mAddress = null;
    protected SKLocation.SKGoogleLocation location = null;
    protected OnPreferenceChangeListener locationChangeListener = null;
    protected boolean disableDependences = true;


    public CurrentLocation(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public CurrentLocation(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CurrentLocation(Context context) {
        super(context);
        init(context);
    }

    private void onError()
    {
        disableDependences = false;
        setChecked(false);
        setEnabled(false);

        Toast toast = Toast.makeText(getContext(), getContext().getResources().getString(R.string.invalid_current_location_error_message), 3000);
        toast.show();
    }

    public void init(final Context context) {

        final CurrentLocation self = this;
        setDisableDependentsState(true);

        location = new SKLocation.SKGoogleLocation(context) {
            @Override
            public void onConnectionSuspended(int i) {
                CurrentLocation.this.onError();
            }

            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                CurrentLocation.this.onError();
            }

            @Override
            public void onConnected(Location lastLocation) {
                if ( lastLocation == null )
                {
                    CurrentLocation.this.onError();
                }
                callChangeListener(getAddress());
            }

            @Override
            public void onLocationInfoReady(SKLocation.SKLocationInfo skLocationInfo) {
                if ( skLocationInfo != null ) {
                    CurrentLocation.this.setValue(skLocationInfo.address);
                }
                else
                {
                    CurrentLocation.this.onError();
                }
            }
        };

        QuestionService.getInstance().preparePreference(this);
        location.connect();
    }

    @Override
    public void setValue(String value) {

    }

    @Override
    public String getValue() {
        return getAddressString(this.mAddress);
    }

    protected String getAddressString( Address address )
    {
        if ( address == null )
        {
            return "";
        }

        return SKLocation.getAddressString(address);
    }

    public Address getAddress() {
        return this.mAddress;
    }

    public void setValue(Address address) {

        if ( address == null )
        {
            return;
        }

        this.mAddress = address;
        //savePreference();
        setSummary(SKLocation.getAddressString(address));
    }

//    protected String getPreferenceName()
//    {
//        return getKey();
//    }
//
//    protected void savePreference()
//    {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
//
//        SharedPreferences.Editor editor = prefs.edit();
//        Gson g = new GsonBuilder().create();
//        editor.putString(getPreferenceName(), g.toJson(this.mAddress));
//        editor.commit();
//    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        boolean value = true;

        if (restoreValue) {
            if (defaultValue == null) {
                value = super.getPersistedBoolean(true);
            } else {
                value = getPersistedBoolean(Boolean.parseBoolean(defaultValue.toString()));
            }
        } else {
            value = getPersistedBoolean(Boolean.parseBoolean(defaultValue.toString()));
        }

        setChecked(value);
        callChangeListener(value);
    }

    @Override
    public boolean shouldDisableDependents() {
        return super.shouldDisableDependents() && disableDependences;
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
