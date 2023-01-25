package com.golfingbuddy.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by kairat on 1/21/15.
 */
final public class SKConfig {
    private static SKConfig mInstance;
    private static final String PREFERENCE_KEY = "SKConfig";

    private String defaultStringValue   = null;
    private int defaultIntValue         = 0;
    private Boolean defaultBooleanValue = null;
    private Float defaultFloatValue     = null;
    private Long defaultLongValue       = null;

    private SharedPreferences mSharedPreferences;

    private SKConfig(Context context) {
        if ( context == null ) {
            throw new IllegalArgumentException("Illegal \"context\" parameter");
        }

        mSharedPreferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public static SKConfig getInstance(Context context) {
        if ( mInstance == null ) {
            synchronized ( SKConfig.class ) {
                mInstance = new SKConfig(context);
            }
        }

        return mInstance;
    }

    private String getKey(String key, String name) {
        if ( key == null || key.trim().length() == 0 || name == null || name.trim().length() == 0 ) {
            throw new IllegalArgumentException("Parameter \"key\" or \"name\" is empty");
        }

        return key.trim() + "+" + name.trim();
    }

    public Boolean configExists(String key, String name) {
        return mSharedPreferences.contains(getKey(key, name));
    }

    public String getStringValue(String key, String name) {
        return getStringValue(key, name, defaultStringValue);
    }

    public String getStringValue(String key, String name, String defaultValue) {
        return mSharedPreferences.getString(getKey(key, name), defaultValue);
    }

    public int getIntValue(String key, String name) {
        return getIntValue(key, name, defaultIntValue);
    }

    public int getIntValue(String key, String name, int defaultValue) {
        return mSharedPreferences.getInt(getKey(key, name), defaultValue);
    }

    public Boolean getBooleanValue(String key, String name) {
        return getBooleanValue(key, name, defaultBooleanValue);
    }

    public Boolean getBooleanValue(String key, String name, Boolean defaultValue) {
        return mSharedPreferences.getBoolean(getKey(key, name), defaultValue);
    }

    public Float getFloatValue(String key, String name) {
        return getFloatValue(key, name, defaultFloatValue);
    }

    public Float getFloatValue(String key, String name, Float defaultValue) {
        return mSharedPreferences.getFloat(getKey(key, name), defaultValue);
    }

    public Long getLongValue(String key, String name) {
        return getLongValue(key, name, defaultLongValue);
    }

    public Long getLongValue(String key, String name, Long defaultValue) {
        return mSharedPreferences.getLong(getKey(key, name), defaultValue);
    }

    public HashMap<String, ?> getValues(String key) {
        if ( mSharedPreferences.getAll().isEmpty() ) {
            return null;
        }

        Map<String, ?> map = mSharedPreferences.getAll();
        Set<String> keys = map.keySet();
        HashMap<String, Object> resultMap = new HashMap<>();

        for ( String _key : keys ) {
            if ( _key.startsWith(key) ) {
                resultMap.put(_key, map.get(_key));
            }
        }

        return resultMap;
    }

    public Boolean addConfig(String key, String name, Object value) {
        String _key = getKey(key, name);
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        if ( value instanceof String ) {
            editor.putString(_key, value.toString());
        } else if ( value instanceof Integer ) {
            editor.putInt(_key, (Integer) value);
        } else if ( value instanceof Boolean ) {
            editor.putBoolean(_key, (Boolean) value);
        } else if ( value instanceof Float ) {
            editor.putFloat(_key, (Float) value);
        } else if ( value instanceof Long ) {
            editor.putLong(_key, (Long) value);
        }

        return editor.commit();
    }

    public Boolean saveConfig(String key, String name, Object value) {
        return addConfig(key, name, value);
    }

    public Boolean removeConfig(String key, String name) {
        if ( !configExists(key, name) ) {
            return false;
        }

        String _key = getKey(key, name);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(_key);

        return editor.commit();
    }

    public Boolean removeConfigs(String key) {
        if ( mSharedPreferences.getAll().isEmpty() ) {
            return null;
        }

        Map<String, ?> map = mSharedPreferences.getAll();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        Set<String> keys = map.keySet();

        for ( String _key : keys ) {
            if ( _key.startsWith(key) ) {
                editor.remove(_key);
            }
        }

        return editor.commit();
    }

    public Boolean batchSaveConfig(String key, HashMap<String, Object> data) {
        if ( key == null || data == null ) {
            return false;
        }

        Set<String> keys = data.keySet();

        for ( String _key : keys ) {
            saveConfig(key, _key, data.get(_key));
        }

        return true;
    }
}
