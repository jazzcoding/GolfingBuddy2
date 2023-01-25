package com.golfingbuddy.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Locale;

/**
 * Created by kairat on 12/22/14.
 */
public class SKLocation {

    public static String getAddressString( Address address)
    {

        int index = address.getMaxAddressLineIndex();
        String result = "";

        for ( int i =0; i <= index; i++ )
        {
            result += (i>0?", ":"")+address.getAddressLine(i);
        }

        return result;/* String.format(
                "%s, %s, %s",
                // The country of the address
                address.getCountryName() != null ? address.getCountryName() : "",
                // Locality is usually a city
                address.getLocality() != null ? address.getLocality() : "",
                // If there's a street address, add it
                address.getMaxAddressLineIndex() > 0 ?
                        address.getAddressLine(0) : ""); */
    }


    public interface SKGoogleLocationListener {
        public void onConnected(Location lastLocation);
        public void onLocationInfoReady(SKLocationInfo skLocationInfo);
    }

    public static class SKGoogleLocation implements ConnectionCallbacks, OnConnectionFailedListener, SKGoogleLocationListener {
        protected Context mContext;
        protected GoogleApiClient mGoogleApiClient;
        protected Location mLastLocation;
        protected SKLocationInfo mLocationInfo;

        public SKGoogleLocation(Context context) {
            mContext = context;
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        @Override
        final public void onConnected(Bundle bundle) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            onConnected(mLastLocation);

            if ( mLastLocation != null ) {
                (new GetAddressTask(mContext)).execute(mLastLocation);
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            // This space for rent
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            // This space for rent
        }

        public void connect() {
            if (mGoogleApiClient.isConnected()) {
                return;
            }

            mGoogleApiClient.connect();
        }

        public void destroy() {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }

        public SKLocationInfo getLocationInfo() {
            return mLocationInfo;
        }

        @Override
        public void onConnected(Location lastLocation) {

        }

        @Override
        public void onLocationInfoReady(SKLocationInfo skLocationInfo) {

        }

        private class GetAddressTask extends AsyncTask<Location, Void, SKLocationInfo> {
            Context mContext;

            public GetAddressTask(Context context) {
                super();
                mContext = context;
            }

            @Override
            protected SKLocationInfo doInBackground(Location... params) {
                Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                Location loc = params[0];
                List<Address> addresses;

                if ( loc == null )
                {
                    return null;
                }

                try {
                    addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                }
                catch (IOException e) {
                    Log.e("LocationsSampleException", "IO Exception in getFromLocation");
                    e.printStackTrace();

                    return null;
                }
                catch (IllegalFormatCodePointException e) {
                    String errorString = "Illegal arguments " + Double.toString(loc.getLatitude()) +
                            " , " +
                            Double.toString(loc.getLatitude()) +
                            " passed to address service";
                    Log.e("LocationSampleActivity", errorString);
                    e.printStackTrace();

                    return null;
                }

                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);

                    return new SKLocationInfo(address);
                }

                return  null;
            }

            @Override
            protected void onPostExecute(SKLocationInfo skLocationInfo) {
                mLocationInfo = skLocationInfo;
                SKGoogleLocation.this.onLocationInfoReady(mLocationInfo);
            }
        }
    }

    public static class SKLocationInfo {
        public String countryName;
        public String regionName;
        public String cityName;
        public Double longitude;
        public Double latitude;
        public Address address;

        public SKLocationInfo() {

        }

        public SKLocationInfo( Address address) {
            countryName = address.getCountryName();
            regionName = address.getAdminArea();
            cityName = address.getLocality();
            longitude = address.getLongitude();
            latitude = address.getLatitude();
            this.address = address;
        }

        public String getCountryName() {
            return countryName;
        }

        public String getRegionName() {
            return regionName;
        }

        public String getCityName() {
            return cityName;
        }

        public Double getLongitude() {
            return longitude;
        }

        public Double getLatitude() {
            return latitude;
        }

        public Address getAddress() {
            return address;
        }

        public String getRealAddress() {
            return String.format("%s %s %s", countryName, regionName, cityName);
        }
    }
}
