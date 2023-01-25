package com.golfingbuddy.ui.search.classes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by jk on 12/17/14.
 */
public class AdvancedQuestionsJsonParcer extends BasicQuestionsJsonParser{

    @Override
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

            ArrayList<HashMap<String, Object>> sections = parseSections(element.getAsJsonArray());
            data.put(key, sections);
        }

        return data;
    }

    public ArrayList<HashMap<String, Object>> parseSections( JsonArray items ) {

        ArrayList<HashMap<String, Object>> sections = new ArrayList<HashMap<String, Object>>();

        for ( JsonElement element:items )
        {
            if ( null == element || !element.isJsonObject() )
            {
                continue;
            }

            HashMap<String, Object> section = new HashMap<String, Object>();

            putAsString("name", section, element.getAsJsonObject());
            putAsString("label", section, element.getAsJsonObject());

            JsonElement questions = element.getAsJsonObject().get("questions");

            if ( questions != null && !questions.isJsonNull() && questions.isJsonArray() )
            {
                ArrayList<HashMap<String, Object>> questionsList = parseQuestionList(questions);
                section.put("questions", questionsList);
            }

            sections.add(section);
        }

        return sections;
    }

}
