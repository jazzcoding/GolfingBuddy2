package com.golfingbuddy.ui.speedmatch.classes;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kairat on 12/19/14.
 */
public class SpeedmatchesFilter {
    @SerializedName("match_sex")
    private ArrayList<String> matchSex;

    @SerializedName("match_sex_data")
    private ArrayList<FilterMatchesData> matchSexData;

    @SerializedName("match_age")
    private String matchAge;

    @SerializedName("googlemap_location")
    private HashMap<String, Object> googlemapLocation;

    @SerializedName("distanceUnits")
    private String distanceUnits;

    @SerializedName("defaultAgeRange")
    private String defaultAgeRange;

    public String getDistanceUnits() {
        return distanceUnits;
    }

    public void setDistanceUnits(String distanceUnits) {
        this.distanceUnits = distanceUnits;
    }

    public ArrayList<String> getMatchSex() {
        return matchSex;
    }

    public void setMatchSex(ArrayList<String> match_sex) {
        this.matchSex = match_sex;
    }

    public String getMatchAge() {
        return matchAge;
    }

    public void setMatchAge(String match_age) {
        this.matchAge = match_age;
    }

    public void setDefaultAgeRange(String defaultAgeRange) {
        this.defaultAgeRange = defaultAgeRange;
    }

    public HashMap<String, Object> getGooglemapLocation() {
        return googlemapLocation;
    }

    public void setGooglemapLocation(HashMap<String, Object> googlemap_location) {
        this.googlemapLocation = googlemap_location;
    }

    public HashMap<String, Object> getData() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("googlemapLocation", this.googlemapLocation);
        result.put("matchAge", this.matchAge);
        result.put("matchSex", this.matchSex);
        result.put("distanceUnits", this.distanceUnits);
        result.put("defaultAgeRange", this.defaultAgeRange);

        ArrayList<HashMap<String, String>> data = new ArrayList<>();

        if ( matchSexData != null ) {
            for (FilterMatchesData filterMatchesData : matchSexData) {
                HashMap<String, String> _data = new HashMap<>();
                _data.put("id", Integer.toString(filterMatchesData.getId()));
                _data.put("label", filterMatchesData.getLabel());
                data.add(_data);
            }
        }

        result.put("matchSexData", data);

        return result;
    }

    public class FilterMatchesData {
        private int id;
        private String label;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String value) {
            this.label = value;
        }
    }
}
