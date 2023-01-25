package com.golfingbuddy.ui.search.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.preference.TwoStatePreference;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.search.classes.AdvancedQuestionsJsonParcer;
import com.golfingbuddy.ui.search.classes.BasicQuestionsJsonParser;
import com.golfingbuddy.ui.search.service.QuestionService;
import com.golfingbuddy.utils.form.Distance;

import java.util.ArrayList;
import java.util.HashMap;


public class SearchFormFragment extends QuestionFormFragment {

    public static final String QUESTION_PREFIX = "search_";

    protected int requestId = -1;
    protected boolean sendRequest = false;
    protected LinearLayout layout;
    protected ProgressBar progressBar;
    protected HashMap<String, Object> loadedData;

    public static SearchFormFragment newInstance() {

        SearchFormFragment fragment = new SearchFormFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public SearchFormFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
        setPreferenceScreen(screen);

        this.loadData();
    }

    protected String getPreferencePrefix() {
        return QUESTION_PREFIX;
    }

    protected void loadData() {
        BaseServiceHelper helper = BaseServiceHelper.getInstance(getActivity().getApplication());
        requestId = helper.runRestRequest(BaseRestCommand.ACTION_TYPE.GET_SEARCH_QUESTIONS, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int rId, Intent requestIntent, int resultCode, Bundle bundle) {

                loadedData = new HashMap<String, Object>();
                QuestionsData myList = new QuestionsData();
                try {
                    String result = bundle.getString("data");

                    if (result != null) {
                        JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                        JsonElement listData = dataObject.get("data");
                        JsonElement type = dataObject.get("type");

                        if ( listData != null && listData.isJsonObject() && type != null && "success".equals(type.getAsString()) )
                        {
                            JsonObject data = listData.getAsJsonObject();

                            BasicQuestionsJsonParser parcer = new BasicQuestionsJsonParser();

                            if ( data.has("basicQuestions") )
                            {
                                JsonElement item = data.get("basicQuestions");

                                if ( item != null && !item.isJsonNull() && item.isJsonObject() )
                                {
                                    loadedData.put("basicQuestions", parcer.parse(item.getAsJsonObject()) );
                                }
                            }

                            if ( data.has("advancedQuestions") )
                            {
                                AdvancedQuestionsJsonParcer aParcer = new AdvancedQuestionsJsonParcer();
                                JsonElement item = data.get("advancedQuestions");

                                if ( item != null && !item.isJsonNull() && item.isJsonObject() )
                                {
                                    loadedData.put("advancedQuestions", aParcer.parse(item.getAsJsonObject()));
                                }
                            }

                            if ( data.has("selectedSex") )
                            {
                                JsonElement item = data.get("selectedSex");

                                if ( item != null && !item.isJsonNull() && item.isJsonPrimitive() )
                                {
                                    loadedData.put("selectedSex", item.getAsJsonPrimitive().getAsString() );
                                }
                            }

                            if ( data.has("auth") )
                            {
                                JsonElement item = data.get("auth");

                                if ( item != null && !item.isJsonNull() && item.isJsonObject()  )
                                {
                                    item = item.getAsJsonObject().get("base.search_users");

                                    if ( item != null && !item.isJsonNull() && item.isJsonObject()  )
                                    {
                                        HashMap <String, String> map = new HashMap<String, String>();

                                        parcer.putAsString("status", map, item.getAsJsonObject());
                                        parcer.putAsString("msg", map, item.getAsJsonObject());
                                        parcer.putAsString("authorizedBy", map, item.getAsJsonObject());

                                        loadedData.put("auth", map );
                                    }


                                }
                            }

                            if ( data.has("distanceUnits") )
                            {
                                JsonElement item = data.get("distanceUnits");

                                if ( item != null && !item.isJsonNull() && item.isJsonPrimitive()  )
                                {
                                    loadedData.put("distanceUnits", item.getAsString());
                                }
                            }

                            //loadedData = parcer.getData();
                        }
                    }
                }
                catch( Exception ex )
                {
                    Log.i(" build search form  ", ex.getMessage(), ex);
                }
                finally {
                    requestId = -1;
                    if ( getActivity() != null ) {
                        String sexValue = (String) loadedData.get("selectedSex");
                        sexValue = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(QUESTION_PREFIX + "match_sex", sexValue);
                        renderFrom(sexValue);
                        show();
                    }
                }

            }
        });
    }

    public void renderFrom( String sexValue )
    {
        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();

        HashMap<String, ArrayList> basicQuestions = (HashMap<String,ArrayList>)loadedData.get("basicQuestions");

        if ( basicQuestions != null )
        {
            ArrayList<HashMap<String, Object>> fields = new ArrayList<HashMap<String, Object>>();

            if ( !basicQuestions.containsKey(sexValue) && basicQuestions.size() > 0 )
                for (HashMap.Entry<String, ArrayList> item:basicQuestions.entrySet()) {
                    sexValue = item.getKey();
                    fields = item.getValue();
                    break;
                }
            else
            {
                fields = (ArrayList<HashMap<String, Object>>) basicQuestions.get(sexValue);
            }

            if ( fields != null && fields.size() > 0 )
            {
                renderQuestions(screen, fields);

                ListPreference matchSex = (ListPreference) screen.findPreference(QUESTION_PREFIX + "match_sex");

                if (matchSex != null) {
                    matchSex.setValue(sexValue);

                    matchSex.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object o) {
                            if (preference == null || !(preference instanceof ListPreference)) {
                                return false;
                            }

                            if (o != null && o.equals(((ListPreference) preference).getValue())) {
                                return false;
                            }

                            renderFrom((String) o);

                            return true;
                        }
                    });
                }
            }
        }

        HashMap<String, ArrayList> advancedQuestions = (HashMap<String,ArrayList>)loadedData.get("advancedQuestions");

        if ( advancedQuestions != null && advancedQuestions.containsKey(sexValue) )
        {
            SwitchPreference advancedOptions = new SwitchPreference(getActivity());

            QuestionService.getInstance().preparePreference(advancedOptions);

            advancedOptions.setKey("advancedOptions");
            advancedOptions.setTitle(R.string.search_advanced_options_label);
            advancedOptions.setDefaultValue(false);

            screen.addPreference(advancedOptions);

            OnOptionsChangeLisner advancedOptionsListner = new OnOptionsChangeLisner();

            renderAdvancedQuestions(screen, (ArrayList<HashMap<String, Object>>) advancedQuestions.get(sexValue), advancedOptionsListner);

            advancedOptionsListner.setPreferenseGroup(screen);
            advancedOptions.setOnPreferenceChangeListener(advancedOptionsListner);

            advancedOptionsListner.onPreferenceChange(advancedOptions, advancedOptions.isChecked());
        }
    }

    public void renderAdvancedQuestions( PreferenceGroup screen, ArrayList<HashMap<String,Object>> advancedQuestions, OnOptionsChangeLisner advancedOptions)
    {
        for ( HashMap<String,Object> item:(ArrayList<HashMap<String,Object>>) advancedQuestions ) {

            if( item == null || !item.containsKey("label") || !item.containsKey("questions"))
            {
                continue;
            }

            ArrayList<HashMap<String,Object>> questions = (ArrayList<HashMap<String, Object>>) item.get("questions");

            if ( questions != null && questions.size() > 0 )
            {
                PreferenceCategory category = new PreferenceCategory(getActivity());
                category.setTitle( (String) item.get("label") );

                screen.addPreference(category);

                if ( advancedOptions != null )
                {
                    advancedOptions.add(category);
                }

                int count = renderQuestions(category, questions);

                if ( count == 0 && advancedOptions != null )
                {
                    category.removePreference(category);
                }
            }
        }
    }

    public int renderQuestions( final PreferenceGroup screen, ArrayList<HashMap<String,Object>> basicQuestions)
    {
        int count = 0;

        for ( final HashMap<String,Object> item:(ArrayList<HashMap<String,Object>>) basicQuestions ) {
            QuestionService service = QuestionService.getInstance();
            Preference formElement = service.getSearchFormField(getActivity(), service.getQuestionParams(item), QUESTION_PREFIX);

            if ( formElement != null ) {
                count++;

                if ( formElement.getKey().equals(QUESTION_PREFIX+QuestionService.getInstance().QUESTION_NAME_CURRENT_LOCATION) )
                {
                    Distance distance = new Distance(getActivity());
                    distance.setKey(QUESTION_PREFIX+QuestionService.getInstance().QUESTION_NAME_DISTANCE);
                    distance.setDefaultValue("25");

                    String[] range = new String[]{"5", "25", "50", "100"};

                    distance.setRange(range);


                    if ( loadedData.containsKey("distanceUnits") ) {
                        distance.setDistanceUnits((String) loadedData.get("distanceUnits"));
                    }

                    screen.addPreference(distance);

                    screen.addPreference(formElement);

                    renderLocation(screen);
                }
                else
                {
                    screen.addPreference(formElement);
                }
            }
        }

        return count;
    }

//    public void renderLocation( final PreferenceGroup screen)
//    {
//        final CustomLocation field = new CustomLocation(getActivity());
//        field.setKey(QUESTION_PREFIX + QuestionService.getInstance().QUESTION_NAME_CUSTOM_LOCATION);
//
//        screen.addPreference(field);
//
//        field.setDependency(QUESTION_PREFIX+QuestionService.getInstance().QUESTION_NAME_CURRENT_LOCATION);
//    }
//    protected void show()
//    {
//        int duration = getResources().getInteger(R.integer.matches_duration);
//
//
//        layout.setAlpha(0f);
//        layout.setVisibility(View.VISIBLE);
//        layout.animate()
//                .alpha(1f)
//                .setDuration(duration)
//                .setListener(null);
//
//
//        progressBar.animate()
//                .alpha(0f)
//                .setDuration(duration)
//                .setListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        progressBar.setVisibility(View.GONE);
//                    }
//                });
//    }
//
//    public void hide()
//    {
//        int duration = getResources().getInteger(R.integer.matches_duration);
//
//        layout.animate()
//                .alpha(0f)
//                .setDuration(duration)
//                .setListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        layout.setVisibility(View.GONE);
//                    }
//                });
//
//        progressBar.setVisibility(View.VISIBLE);
//        progressBar.animate()
//                .alpha(1f)
//                .setDuration(duration)
//                .setListener(null);
//    }
//
//    @Override
//    public void onDetach() {
//        if ( requestId > -1) {
//            BaseServiceHelper helper = new BaseServiceHelper(getActivity().getApplication());
//            helper.cancelCommand(requestId);
//        }
//
//        super.onDetach();
//    }

    public class QuestionsData {

        public ArrayList<HashMap<String, Object>> basicQuestions;
        public ArrayList<HashMap<String, Object>> advancedQuestions;
        public String selectedSex;
        public ArrayList<HashMap<String, Object>> auth;


        public QuestionsData() {
            basicQuestions = new ArrayList<HashMap<String, Object>>();
            advancedQuestions = new ArrayList<HashMap<String, Object>>();
            selectedSex = new String();
            auth = new ArrayList<HashMap<String, Object>>();
        }
    }

    public class OnOptionsChangeLisner implements Preference.OnPreferenceChangeListener {

        ArrayList<Preference> list = null;
        PreferenceGroup screen = null;

        public OnOptionsChangeLisner() {
            list = new ArrayList<Preference>();
        }

        public void add( Preference item )
        {
            if ( item != null ) {
                this.list.add(item);
            }
        }

        public void setPreferenseGroup( PreferenceGroup item )
        {
            if ( item != null ) {
                screen = item;
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {

            if ( preference == null || !(preference instanceof TwoStatePreference) || list.size() == 0 || screen == null  )
            {
                return false;
            }

            for(Preference item:list) {
                //item.setEnabled(((TwoStatePreference) preference).isChecked());
                if ( o != null && (Boolean)o )
                {
                    screen.addPreference(item);
                }
                else
                {
                    screen.removePreference(item);
                }
            }

            return true;
        }
    }

//    @Override
//    public void onStop() {
//        if ( requestId > -1 ) {
//            BaseServiceHelper helper = new BaseServiceHelper(getActivity().getApplication());
//            helper.cancelCommand(requestId);
//        }
//
//        super.onStop();
//    }
}
