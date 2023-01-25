package com.golfingbuddy.utils;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseServiceHelper;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by kairat on 2/4/15.
 */
final public class SKLanguage {
    private static SKLanguage mInstance;
    private BaseServiceHelper mBaseServiceHelper;

    private SKLanguage(Application application) {
        if ( application == null ) {
            throw new IllegalArgumentException("Invalid \"application\" parameter.");
        }

        mBaseServiceHelper = BaseServiceHelper.getInstance(application);
    }

    public static SKLanguage getInstance(Application application) {
        if ( mInstance == null ) {
            synchronized ( SKLanguage.class ) {
                mInstance = new SKLanguage(application);
            }
        }

        return mInstance;
    }

    /**
     * Example: copy/past
     *
     * <code>
        SkApplication.getLanguage().getText("admin", "questions_delete_section_confirmation_with_move_questions", new SKLanguage.SKLanguageCallbackListener(){
            @Override
            public void callback(SKLanguage.SKLanguageText text) {
                String foo = text.getText("admin", "questions_delete_section_confirmation_with_move_questions");
            }
        });
     * </code>
     */
    public void getText(String prefix, String key, SKLanguageCallbackListener listener) {
        HashMap<String, String[]> map = new HashMap<>();
        map.put(prefix, new String[]{key});

        getText(map, listener);
    }

    /**
     * Example: copy/past
     *
     * <code>
        HashMap<String, String[]> prefixKeys = new HashMap<>();
        prefixKeys.put("admin", new String[]{"questions_delete_section_confirmation_with_move_questions", "..."});

        SkApplication.getLanguage().getText(prefixKeys, new SKLanguage.SKLanguageCallbackListener(){
            @Override
            public void callback(SKLanguage.SKLanguageText text) {
                HashMap<String, String> map = new HashMap<>();
                map.put("sectionName", "var1");

                String foo = text.getText("admin", "questions_delete_section_confirmation_with_move_questions", map);
            }
        });
     * </code>
     */
    public void getText(HashMap<String, String[]> prefixKeys, final SKLanguageCallbackListener listener) {
        if ( prefixKeys == null || prefixKeys.isEmpty() ) {
            throw new IllegalArgumentException("Parameter \"prefixKeys\" is empty.");
        }

        mBaseServiceHelper.getText(prefixKeys, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                JsonObject dataObject = SkApi.processResult(data);
                SKLanguageText text = new SKLanguageText();

                if ( dataObject != null && dataObject.has("texts") ) {
                    for ( JsonElement row : dataObject.getAsJsonArray("texts") ) {
                        JsonObject tmp = row.getAsJsonObject();
                        text.addText(
                                tmp.get("prefix").getAsString(),
                                tmp.get("key").getAsString(),
                                tmp.get("value").getAsString()
                        );
                    }
                }

                listener.callback(text);
            }
        });
    }

    /**
     * Example: copy/past
     *
     * <code>
        SkApplication.getLanguage().getCustomPage("terms-of-use", new SKLanguage.SKLanguageCustomPageListener() {
            public void callback(String text) {
                String foo = text;
            }
        });
     * </code>
     */
    public void getCustomPage(String key, final SKLanguageCustomPageListener listener) {
        if ( key == null ) {
            throw new IllegalArgumentException("Invalid \"key\" parameter.");
        }

        mBaseServiceHelper.getCustomPage(key, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                JsonObject dataObject = SkApi.processResult(data);
                listener.callback(dataObject != null && dataObject.has("content") ? dataObject.get("content").getAsString() : null);
            }
        });
    }

    public interface SKLanguageCallbackListener {
        void callback(SKLanguageText text);
    }

    public interface SKLanguageCustomPageListener {
        void callback(String text);
    }

    final public static class SKLanguageText {
        private HashMap<String, String> mTexts;

        public SKLanguageText() {
            mTexts = new HashMap<>();
        }

        private String getKey(String prefix, String key) {
            return prefix.trim() + "+" + key.trim();
        }

        private void addText(String prefix, String key, String value) {
            mTexts.put(getKey(prefix, key), value);
        }

        public String getText(String prefix, String key) {
            return getText(prefix, key, null);
        }

        public String getText(String prefix, String key, HashMap<String, String> vars) {
            String prefixKey = getKey(prefix, key);

            if ( mTexts.containsKey(prefixKey) ) {
                String text = mTexts.get(prefixKey);

                if ( vars != null && !vars.isEmpty() ) {
                    Set<String> keys = vars.keySet();

                    for ( String _key : keys ) {
                        text = text.replace("{$" + _key + "}", vars.get(_key));
                    }
                }

                return text;
            } else {
                return prefixKey;
            }
        }
    }
}
