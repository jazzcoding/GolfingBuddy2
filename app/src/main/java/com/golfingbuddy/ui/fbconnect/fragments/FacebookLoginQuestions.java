package com.golfingbuddy.ui.fbconnect.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.fbconnect.FacebookLoginStepManager;
import com.golfingbuddy.ui.search.classes.LocationObject;
import com.golfingbuddy.ui.search.service.QuestionService;
import com.golfingbuddy.utils.form.CustomLocation;
import com.golfingbuddy.utils.form.Distance;
import com.golfingbuddy.utils.form.SelectDialogField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by kairat on 12/18/14.
 */
public class FacebookLoginQuestions extends PreferenceFragment {

    private HashMap<String, ArrayList<QuestionService.QuestionParams>> questions;
    private HashMap<String, String> mCategory;
    protected LinearLayout layout;
    private Bundle bundle;

    public FacebookLoginQuestions() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();

        questions = (HashMap<String, ArrayList<QuestionService.QuestionParams>>)bundle.getSerializable("questions");
        mCategory = (HashMap<String, String>)bundle.getSerializable("category");

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
        setPreferenceScreen(screen);

        renderForm();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View preferenceView = super.onCreateView(inflater, container, savedInstanceState);

        return preferenceView;
    }

    private void renderForm() {
        renderFrom(null);
    }

    private void renderFrom(String sexValue) {
        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();

        ArrayList<QuestionService.QuestionParams> basicQuestions = questions.get("questions");

        if (basicQuestions != null && !basicQuestions.isEmpty()) {
            renderQuestions(screen, basicQuestions);

//            Preference matchSex = screen.findPreference(PREFIX + "match_sex");
//
//            if (matchSex != null && sexValue != null) {
//                ((ListPreference) matchSex).setValue(sexValue);
//
//                matchSex.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                    @Override
//                    public boolean onPreferenceChange(Preference preference, Object o) {
//                        if (preference == null || !(preference instanceof ListPreference)) {
//                            return false;
//                        }
//
//                        if (o != null && o.equals(((ListPreference) preference).getValue())) {
//                            return false;
//                        }
//
//                        renderFrom((String) o);
//
//                        return true;
//                    }
//                });
//            }
        } else {
            renderAdvancedQuestions(screen, questions);
        }

//        ArrayList<QuestionService.QuestionParams> advancedQuestions = questions.get("advanced");
//
//        if (advancedQuestions != null && !advancedQuestions.isEmpty()) {
//            SwitchPreference advancedOptions = new SwitchPreference(getActivity());
//            advancedOptions.setKey("advancedOptions");
//            advancedOptions.setTitle(R.string.fbconnect_advanced_options_label);
//            advancedOptions.setDefaultValue(false);
//
//            screen.addPreference(advancedOptions);
//
//            OnOptionsChangeLisner advancedOptionsListner = new OnOptionsChangeLisner();
//
//            renderAdvancedQuestions(screen, advancedQuestions);
//
//            advancedOptionsListner.setPreferenseGroup(screen);
//            advancedOptions.setOnPreferenceChangeListener(advancedOptionsListner);
//
//            advancedOptionsListner.onPreferenceChange(advancedOptions, advancedOptions.isChecked());
//        }
    }

    private int renderQuestions( final PreferenceGroup screen, ArrayList<QuestionService.QuestionParams> basicQuestions)
    {
        int count = 0;

        for ( final QuestionService.QuestionParams item: basicQuestions ) {
            QuestionService service = QuestionService.getInstance();
            Preference formElement = service.getFormField(getActivity(), item, FacebookLoginStepManager.PREFIX);

            if (formElement.getKey().equals(FacebookLoginStepManager.PREFIX + "terms")) {
                formElement.setLayoutResource(R.layout.custom_terms_preference);
            } else {
            formElement.setLayoutResource(R.layout.custom_preference);
            }

            formElement.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue != null) {
                        if (newValue instanceof String && newValue.toString().trim().length() > 0) {
                            preference.setIcon(new ColorDrawable(Color.TRANSPARENT));
                        } else if (newValue instanceof HashSet && !((HashSet) newValue).isEmpty()) {
                            preference.setIcon(new ColorDrawable(Color.TRANSPARENT));
                        } else if (newValue instanceof LocationObject) {
                            preference.setIcon(new ColorDrawable(Color.TRANSPARENT));
                        } else if (preference.getKey().equals(FacebookLoginStepManager.PREFIX + "terms")) {
                            preference.setIcon(new ColorDrawable(Color.TRANSPARENT));
                        }
                    }

                    return true;
                }
            });

            if ( formElement != null ) {
                count++;

                if (formElement.getKey().equals(FacebookLoginStepManager.PREFIX + QuestionService.getInstance().QUESTION_NAME_CURRENT_LOCATION)) {
                    Distance distance = new Distance(getActivity());
                    distance.setKey(FacebookLoginStepManager.PREFIX + QuestionService.getInstance().QUESTION_NAME_DISTANCE);
                    distance.setDefaultValue("20");

                    screen.addPreference(distance);
                    screen.addPreference(formElement);

                    renderLocation(screen);
                } else {
                    screen.addPreference(formElement);

                    switch (formElement.getKey()) {
                        case FacebookLoginStepManager.PREFIX + "realname":
                            if (bundle.getString("name") != null) {
                                formElement.setDefaultValue(bundle.getString("name"));
                            }
                            break;
                        case FacebookLoginStepManager.PREFIX + "sex":
                            if (bundle.containsKey("gender") && bundle.getString("gender") != null) {
                                String gender = bundle.getString("gender").toLowerCase();
                                Pattern pattern = Pattern.compile("\\b" + Pattern.quote(gender) + "\\b", Pattern.CASE_INSENSITIVE);

                                for (HashMap<String, String> value : item.getOptions()) {
                                    if (pattern.matcher(value.get("label")).find()) {
                                        ((SelectDialogField) formElement).setValue(value.get("value"));

                                        break;
                                    }
                                }
                            }

                            break;
                    }


                }
            }
        }

        return count;
    }

    private void renderAdvancedQuestions(PreferenceGroup screen, HashMap<String, ArrayList<QuestionService.QuestionParams>> questions) {
        if (questions != null && questions.size() > 0) {
            Set<String> keys = questions.keySet();
            for (String key : keys) {
                ArrayList<QuestionService.QuestionParams> _questions = questions.get(key);
                PreferenceCategory category = new PreferenceCategory(getActivity());
                category.setTitle(mCategory.get(_questions.get(0).getSectionName()));

                screen.addPreference(category);
                renderQuestions(category, _questions);
            }
        }
    }

    private void renderLocation(final PreferenceGroup screen) {
        final CustomLocation field = new CustomLocation(getActivity());
        field.setKey(FacebookLoginStepManager.PREFIX + QuestionService.getInstance().QUESTION_NAME_CUSTOM_LOCATION);

        screen.addPreference(field);

        field.setDependency(FacebookLoginStepManager.PREFIX + QuestionService.getInstance().QUESTION_NAME_CURRENT_LOCATION);
    }
}
