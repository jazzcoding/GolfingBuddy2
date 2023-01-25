package com.golfingbuddy.ui.search.classes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by jk on 12/12/14.
 */
public class BasicQuestionsJsonParser {

    public BasicQuestionsJsonParser() {

    }

    public HashMap<String, Object> parse( JsonObject items ) {
        HashMap<String, Object> data = new HashMap<String, Object>();


        if ( items == null || !items.isJsonObject() )
        {
            return data;
        }

        Set<Map.Entry<String,JsonElement>> entrySet = items.entrySet();

        for ( Map.Entry<String,JsonElement> entry:entrySet )
        {
            String key = entry.getKey();
            JsonElement element = entry.getValue();

            if ( null == element || !element.isJsonArray() )
            {
                continue;
            }

            ArrayList<HashMap<String, Object>> list = parseQuestionList(element);

            data.put(key, list);
        }

        return data;
    }

    protected ArrayList<HashMap<String, Object>> parseQuestionList(JsonElement element)
    {
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

        for ( JsonElement j:element.getAsJsonArray() )
        {
            JsonObject item = j.getAsJsonObject();

            if ( !item.has("name") )
            {
                continue;
            }

            HashMap map = parseQuestion(item);

            if ( map.size() > 0 && map.containsKey("name") ) {
                list.add(map);
            }
        }

        return list;
    }

    protected HashMap parseQuestion( JsonObject item )
    {
        HashMap map = new HashMap();

        putAsString("name", map, item);
        putAsString("label", map, item);
        putAsString("presentation", map, item);

        putAsQuestionValues("options", map, item);

        if ( item.has("custom") ) {
            JsonElement tmp  = item.get("custom");

            if ( null != tmp  && tmp.isJsonObject() && !tmp.isJsonNull() )
            {
                HashMap<String, String> custom = parseCustomParams(tmp.getAsJsonObject());
                map.put("custom", custom);
            }
        }

        if (item.has("sectionName")) {
            putAsString("sectionName", map, item);
        }

        if (item.has("required")) {
            putAsString("required", map, item);
        }

        return map;
    }

    public void putAsString( String key, HashMap map, JsonObject item )
    {
        if ( item.has(key) ) {
            JsonElement tmp  = item.get(key);

            if ( null != tmp  && tmp.isJsonPrimitive() && !tmp.isJsonNull() )
            {
                map.put(key, tmp.getAsString());
            }
        }
    }

    protected void putAsQuestionValues( String key, HashMap map, JsonObject item )
    {
        if ( item.has(key) ) {
            JsonElement tmp  = item.get(key);

            if ( null != tmp  && tmp.isJsonObject() && !tmp.isJsonNull() )
            {
                ArrayList<HashMap<String, String>> values =parseValues(tmp.getAsJsonObject());

                map.put(key, values);
            }
        }
    }

    protected ArrayList<HashMap<String, String>> parseValues( JsonObject list )
    {
        ArrayList<HashMap<String, String>> listMap = new ArrayList<HashMap<String, String>>();

        if ( !list.has("values") ) {
            return listMap;
        }

        JsonElement tmp = list.get("values");

        if ( tmp != null && tmp.isJsonArray() && !tmp.isJsonNull() )
        {
            JsonArray values = tmp.getAsJsonArray();

            for ( JsonElement value:values )
            {
                if( !value.isJsonObject() )
                {
                    continue;
                }

                JsonObject object = value.getAsJsonObject();

                HashMap<String, String> map = new HashMap<String, String>();
                putAsString( "label", map, object );
                putAsString( "value", map, object );

                listMap.add(map);
            }
        }

        return listMap;
    }

    protected HashMap<String, String> parseCustomParams( JsonObject tmp )
    {
        HashMap<String, String> map = new HashMap<String, String>();

        if ( null != tmp  && tmp.isJsonObject() && !tmp.isJsonNull() && tmp.has("year_range") )
        {
            JsonElement yearRange = tmp.get("year_range");
            if ( null != yearRange  && yearRange.isJsonObject() && !yearRange.isJsonNull() ) {

                JsonObject object = yearRange.getAsJsonObject();

                putAsString( "from", map, object );
                putAsString( "to", map, object );
            }
        }

        return map;
    }
}
