package com.golfingbuddy.ui.user_list.classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class ListData implements Parcelable {
    HashMap<String,HashMap<String,String>> data_map = new HashMap<String,HashMap<String,String>>();

    public ListData() {
    }

    public void setData( HashMap<String,HashMap<String,String>> data )
    {
        if ( data != null && !data.isEmpty() ) {
            data_map.putAll(data);
        }
    }

    public HashMap<String,HashMap<String,String>> getData()
    {
        return data_map;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        final int N = data_map.size();
        dest.writeInt(N);

        if (N > 0) {



            for(Map.Entry<String, HashMap<String, String>> entry : data_map.entrySet()) {

                String key = entry.getKey();
                HashMap<String, String> value = entry.getValue();

                int count = value.size();
                dest.writeString(key);
                dest.writeInt(count);

                for(Map.Entry<String, String> itemEntry : value.entrySet()) {
                    String itemKey = itemEntry.getKey();
                    String itemValue = itemEntry.getValue();

                    dest.writeString(itemKey);
                    dest.writeString(itemValue);
                }
            }
        }
    }

    public static final Creator<ListData> CREATOR = new Creator<ListData>() {
        public ListData createFromParcel(Parcel source) {
            return new ListData(source);
        }
        public ListData[] newArray(int size) {
            return new ListData[size];
        }
    };

    private ListData(Parcel source) {
        final int N = source.readInt();

        for (int i=0; i<N; i++) {
            String key = source.readString();
            int count = source.readInt();

            HashMap<String, String> map = new HashMap();

            for (int j=0; j<count; j++) {
                String itemKey = source.readString();
                String itemValue = source.readString();

                map.put(itemKey, itemValue);
            }

            data_map.put(key, map);
        }
    }
}


