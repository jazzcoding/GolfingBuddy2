package com.golfingbuddy.ui.search.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;
import com.golfingbuddy.ui.search.classes.LocationObject;
import com.golfingbuddy.ui.search.classes.QuestionObject;
import com.golfingbuddy.ui.search.classes.SectionObject;
import com.golfingbuddy.ui.search.service.QuestionService;
import com.golfingbuddy.utils.SkApi;
import com.golfingbuddy.utils.form.FormField;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by jk on 1/10/15.
 */

public class EditFormFragment extends QuestionFormFragment {

    public static final String QUESTION_PREFIX = "edit_";

    protected int requestId = -1;
    protected int userId = 0;
    protected LinearLayout layout;
    protected ProgressBar progressBar;
    protected HashMap<String, Object> loadedData;
    protected Boolean isNeedToModerate = false;

    public static EditFormFragment newInstance() {

        EditFormFragment fragment = new EditFormFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }
    //
    public EditFormFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        userId = ((SkBaseInnerActivity) getActivity()).getApp().getUserInfo().getUserId();
        super.onCreate(savedInstanceState);

//        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
//        setPreferenceScreen(screen);
//
//        this.loadData();
    }

    @Override
    protected String getPreferencePrefix() {
        return QUESTION_PREFIX;
    }

    public static class UserData {
        public QuestionObject[][] editQuestions;
        public SectionObject[] editSections;
        public Boolean isBlocked;
        public Boolean isWinked;
        public Boolean isAdmin;
    }

    public static class QuestionList {
        public QuestionObject[] list;
    }

    protected void loadData() {

        if ( requestId > -1 )
        {
            return;
        }

        BaseServiceHelper helper = BaseServiceHelper.getInstance(getActivity().getApplication());

        HashMap<String,String> params = new HashMap<String,String>();
        params.put("userId", String.valueOf(userId));

        requestId = helper.runRestRequest(BaseRestCommand.ACTION_TYPE.PROFILE_INFO, params,
                new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int rId, Intent requestIntent, int resultCode, Bundle bundle) {
                HashMap<String, Object> user = new HashMap<String, Object>();

                UserData srd = null;

                try {
                    String result = bundle.getString("data");

                    if (result != null) {
                        JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                        JsonElement listData = dataObject.get("data");
                        JsonElement type = dataObject.get("type");

                        if (listData != null && listData.isJsonObject() && type != null && "success".equals(type.getAsString())) {
                            JsonObject data = listData.getAsJsonObject();

                            Gson g = new GsonBuilder().create();
                            srd = g.fromJson(data, UserData.class);
                        }
                    }
                } catch (Exception ex) {
                    Log.i(" build edit form  ", ex.getMessage(), ex);
                } finally {
                    EditFormFragment.this.requestId = -1;
                    if ( getActivity() != null ) {
                        renderForm(srd);
                        show();
                    }
                }
            }


        });
    }

    protected void renderForm( UserData srd)
    {
        if ( srd == null )
        {
            return;
        }

        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();

        if ( srd.editSections != null && srd.editSections.length > 0
                && srd.editQuestions != null && srd.editSections.length > 0 ) {
            for ( int i=0; i< srd.editSections.length ; i++ ) {

                SectionObject section = null;
                QuestionObject[] questions = null;

                if ( srd.editSections[i] != null ) {
                    section = srd.editSections[i];
                }

                if ( srd.editQuestions != null && srd.editQuestions.length > i ) {
                    if ( srd.editQuestions[i] != null ) {
                         questions = srd.editQuestions[i];
                    }
                }

                renderSection(screen, section, questions);
            }
        }
    }

    protected void renderSection(PreferenceScreen screen, SectionObject section, QuestionObject[] questions) {
        if ( section == null )
        {
            return;
        }

        if ( questions == null || questions.length == 0 )
        {
            return;
        }

        PreferenceCategory category = new PreferenceCategory(getActivity());
        category.setTitle( (String) section.getLabel() );

        screen.addPreference(category);

        for ( QuestionObject question:questions )
        {
            Preference preference = QuestionService.getInstance().getFormField(getActivity(),
                    new QuestionService.QuestionParams(question), getPreferencePrefix(), category);

            if ( preference != null ) {
                category.addPreference(preference);

                if ( question.getValue() != null ) {
                    ((FormField) preference).setValue(question.getValue());
                }

                preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (preference == null) {
                            return false;
                        }

                        if (!(preference instanceof com.golfingbuddy.utils.form.FormField)) {
                            return false;
                        }

                        if ( newValue == null && ((FormField) preference).getValue() == null )
                        {
                            return false;
                        }

                        String key = preference.getKey();
                        String name = key.substring(EditFormFragment.this.QUESTION_PREFIX.length());
                        String value = String.valueOf(newValue);

                        if (newValue instanceof Set) {
                            int val = QuestionService.getInstance().getIntValue((Set<String>) newValue);
                            value = String.valueOf(val);
                        }

                        if (newValue instanceof LocationObject) {
                            value = QuestionService.getInstance().toJson(newValue);
                        }

//                        if ( newValue != null && newValue.equals(((FormField) preference).getValue()) )
//                        {
//                            return false;
//                        }

                        BaseServiceHelper helper = BaseServiceHelper.getInstance(getActivity().getApplication());
                        int requestId = helper.saveQuestionValue(name, value, 1, new SkServiceCallbackListener() {
                            @Override
                            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                                JsonObject json = SkApi.processResult(data);
                                EditFormFragment.this.isNeedToModerate = json != null && json.has("isNeedToModerate") && json.get("isNeedToModerate").getAsBoolean();
                            }
                        });

                        ((FormField) preference).setValue(value);
                        Intent intent = new Intent("base.profile_question_changed");
                        intent.putExtra("name", name);
                        intent.putExtra("value", value);
                        intent.putExtra("textValue", preference.getSummary());
                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

                        return true;
                    }
                });
            }
        }
    }

    public Boolean getIsNeedToModerate() {
        return isNeedToModerate;
    }
}
