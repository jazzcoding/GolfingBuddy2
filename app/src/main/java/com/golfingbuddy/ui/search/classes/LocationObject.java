package com.golfingbuddy.ui.search.classes;

import android.location.Address;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.golfingbuddy.utils.SKLocation;
import com.golfingbuddy.utils.SkGeocoder;

import java.util.Arrays;

/**
 * Created by jk on 1/14/15.
 *
 */
public class LocationObject {
    private String address;
    private String latitude;
    private String longitude;
    private String northEastLat;
    private String northEastLng;
    private String southWestLat;
    private String southWestLng;
    private String country;
    private String json;

    public LocationObject() {

    }

    public LocationObject( SkGeocoder.Location location) {
        if ( location != null )
        {
            latitude = location.getGeometry().getLocation().getLat();
            longitude = location.getGeometry().getLocation().getLng();
            northEastLat = location.getGeometry().getViewport().getNortheast().getLat();
            northEastLng = location.getGeometry().getViewport().getNortheast().getLng();
            southWestLat = location.getGeometry().getViewport().getSouthwest().getLat();
            southWestLng = location.getGeometry().getViewport().getSouthwest().getLng();
            this.address = location.getFormattedAddress();
            Gson g = new GsonBuilder().create();
            json = g.toJson(location);

            if ( location.getAddressComponents().length > 0 )
            {
                for ( SkGeocoder.AddressComponent item:location.getAddressComponents() )
                {
                    if ( item != null && item.getTypes() != null
                            && item.getTypes().length > 0 && Arrays.asList(item.getTypes()).contains("country") )
                    {
                        country =  item.getShortName();
                    }
                }
            }
        }
    }

    public LocationObject( Address address) {

        if ( address != null )
        {
            latitude = String.valueOf(address.getLatitude());
            longitude = String.valueOf(address.getLongitude());
            northEastLat = String.valueOf(address.getLatitude());
            northEastLng = String.valueOf(address.getLongitude());
            southWestLat = String.valueOf(address.getLatitude());
            southWestLng = String.valueOf(address.getLongitude());
            this.address = SKLocation.getAddressString(address);
            country = address.getCountryCode();
            Gson g = new GsonBuilder().create();
            json = g.toJson(address);
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getNorthEastLat() {
        return northEastLat;
    }

    public void setNorthEastLat(String northEastLat) {
        this.northEastLat = northEastLat;
    }

    public String getNorthEastLng() {
        return northEastLng;
    }

    public void setNorthEastLng(String northEastLng) {
        this.northEastLng = northEastLng;
    }

    public String getSouthWestLat() {
        return southWestLat;
    }

    public void setSouthWestLat(String southWestLat) {
        this.southWestLat = southWestLat;
    }

    public String getSouthWestLng() {
        return southWestLng;
    }

    public void setSouthWestLng(String southWestLng) {
        this.southWestLng = southWestLng;
    }

    @Override
    public String toString()
    {
        return "LocationObject [address=" + address + ", latitude=" + latitude + ", " +
                "longitude=" + longitude + ", northEastLat=" + northEastLat + ","
                + ", northEastLng=" + northEastLng + ","
                + ", southWestLat=" + southWestLat + ","
                + ", southWestLng=" + southWestLng + ","
                + ", country=" + country + ","
                + ", json=" + json + "]" ;
    }
}
