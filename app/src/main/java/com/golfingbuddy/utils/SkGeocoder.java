
package com.golfingbuddy.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SkGeocoder
{

    public static GeocoderResult reverseGeocoder(String lat, String lng)
    {
        HttpURLConnection connection = null;
        URL serverAddress = null;

        GeocoderResult result = null;

        try
        {
            // build the URL using the latitude & longitude you want to lookup
            // NOTE: I chose XML return format here but you can choose something else
            String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat +"," + lng +
                    "&oe=utf8&sensor=true";

            serverAddress = new URL(url);//&key=" + R.string.GOOGLE_MAPS_API_KEY);
            //set up out communications stuff
            connection = null;

            //Set up the initial connection
            connection = (HttpURLConnection)serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(10000);

            connection.connect();

            try
            {
                InputStreamReader isr = new InputStreamReader(connection.getInputStream(), "UTF-8");

                StringBuilder inputStringBuilder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(isr);
                String line = bufferedReader.readLine();

                while(line != null){
                    inputStringBuilder.append(line);inputStringBuilder.append('\n');
                    line = bufferedReader.readLine();
                }

                String resultString = inputStringBuilder.toString();
                Gson g = new GsonBuilder().create();
                result = g.fromJson(resultString, GeocoderResult.class);
            }
            catch (Exception ex)
            {
                Log.i(" geocoder parce json ", ex.getMessage(), ex);
            }

        }
        catch (Exception ex)
        {
            Log.i(" geocoder connect error  ", ex.getMessage(), ex);
        }

        return result;
    }

    public static GeocoderResult geocoder(String loc)
    {

        HttpURLConnection connection = null;
        URL serverAddress = null;

        GeocoderResult result = null;

        try
        {

            // build the URL using the latitude & longitude you want to lookup
            // NOTE: I chose XML return format here but you can choose something else
            serverAddress = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(loc, "UTF-8") +
                    "&oe=utf8&sensor=true");//&key=" + R.string.GOOGLE_MAPS_API_KEY);
            //set up out communications stuff
            connection = null;

            //Set up the initial connection
            connection = (HttpURLConnection)serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(10000);

            connection.connect();

            try
            {
                InputStreamReader isr = new InputStreamReader(connection.getInputStream(), "UTF-8");

                StringBuilder inputStringBuilder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(isr);
                String line = bufferedReader.readLine();

                while(line != null){
                    inputStringBuilder.append(line);
                    line = bufferedReader.readLine();
                }

                String resultString = inputStringBuilder.toString();
                Gson g = new GsonBuilder().create();
                result = g.fromJson(resultString, GeocoderResult.class);
            }
            catch (Exception ex)
            {
                Log.i(" geocoder parce json ", ex.getMessage(), ex);
            }

        }
        catch (Exception ex)
        {
            Log.i(" geocoder connect error  ", ex.getMessage(), ex);
        }

        return result;
    }

    public static class GeocoderResult
    {
        private Location[] results;
        private String status;

        public Location[] getResult() {
            return results;
        }

        public void setResult(Location[] result) {
            this.results = result;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class Location
    {
        @SerializedName("address_components")
        private AddressComponent[] addressComponents;

        @SerializedName("formatted_address")
        private String formattedAddress;

        private Geomentry geometry;

        @SerializedName("partial_match")
        private String partialMatch;
        private String[] types;

        public AddressComponent[] getAddressComponents() {
            return addressComponents;
        }

        public void setAddressComponents(AddressComponent[] addressComponents) {
            this.addressComponents = addressComponents;
        }

        public String getFormattedAddress() {
            return formattedAddress;
        }

        public void setFormattedAddress(String formattedAddress) {
            this.formattedAddress = formattedAddress;
        }

        public Geomentry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geomentry geometry) {
            this.geometry = geometry;
        }

        public String getPartialMatch() {
            return partialMatch;
        }

        public void setPartialMatch(String partialMatch) {
            this.partialMatch = partialMatch;
        }

        public String[] getTypes() {
            return types;
        }

        public void setTypes(String[] types) {
            this.types = types;
        }
    }

    public static class AddressComponent
    {
        @SerializedName("long_name")
        private String longName;
        @SerializedName("short_name")
        private String shortName;
        private String[] types;

        public String getLongName() {
            return longName;
        }

        public void setLongName(String longName) {
            this.longName = longName;
        }

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public String[] getTypes() {
            return types;
        }

        public void setTypes(String[] types) {
            this.types = types;
        }
    }

    public static class LatLng
    {
        private String lat;
        private String lng;

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }
    }

    public static class LatLngBounds
    {
        private LatLng northeast;
        private LatLng southwest;

        public LatLng getNortheast() {
            return northeast;
        }

        public void setNortheast(LatLng northeast) {
            this.northeast = northeast;
        }

        public LatLng getSouthwest() {
            return southwest;
        }

        public void setSouthwest(LatLng southwest) {
            this.southwest = southwest;
        }
    }

    public static class Geomentry
    {
        private LatLngBounds bounds;
        private LatLng location;
        @SerializedName("location_type")
        private String locationType;
        private LatLngBounds viewport;

        public LatLngBounds getBounds() {
            return bounds;
        }

        public void setBounds(LatLngBounds bounds) {
            this.bounds = bounds;
        }

        public LatLng getLocation() {
            return location;
        }

        public void setLocation(LatLng location) {
            this.location = location;
        }

        public String getLocationType() {
            return locationType;
        }

        public void setLocationType(String locationType) {
            this.locationType = locationType;
        }

        public LatLngBounds getViewport() {
            return viewport;
        }

        public void setViewport(LatLngBounds viewport) {
            this.viewport = viewport;
        }
    }
}

