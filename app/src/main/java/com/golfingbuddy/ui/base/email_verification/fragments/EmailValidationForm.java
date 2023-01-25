package com.golfingbuddy.ui.base.email_verification.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.search.service.QuestionService;
import com.golfingbuddy.utils.form.FormField;
import com.soundcloud.android.crop.Crop;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jk on 2/18/16.
 */
public class EmailValidationForm extends PreferenceFragment {
    protected String prefix;
    protected com.golfingbuddy.ui.base.email_verification.Presenter presenter;

    public static EmailValidationForm newInstance() {

        EmailValidationForm fragment = new EmailValidationForm();
        return fragment;
    }
    //
    public EmailValidationForm() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
        setPreferenceScreen(screen);

        if ( presenter != null ) {
            drawQuestions(presenter.getQuestions());
        }
    }

    public void drawQuestions(LinkedList<QuestionService.QuestionParams> list) {
        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();

        if ( list != null )
        {
            for ( QuestionService.QuestionParams item:list )
            {
                Preference preference = QuestionService.getInstance().getFormField(getActivity(), item, getPreferencePrefix(), screen);

                if ( preference != null ) {
                    screen.addPreference(preference);
                    ((FormField) preference).setValue(item.getValue());


                    preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {

                            if (EmailValidationForm.this.presenter != null) {
                                HashMap<String, String> value = QuestionService.getInstance().getData(EmailValidationForm.this.getActivity(), EmailValidationForm.this.getPreferencePrefix(), preference.getKey(), newValue);
                                EmailValidationForm.this.presenter.validateField(value);
                            }

                            return true;
                        }
                    });
                }
            }
        }
    }

    public void drawErrors(Collection<QuestionService.QuestionValidationInfo> errors) {
        if ( errors == null || errors.size() == 0 )
        {
            return;
        }

        PreferenceScreen screen = getPreferenceScreen();

        for (QuestionService.QuestionValidationInfo error:errors)
        {
            if ( error != null && !error.getIsValid()) {
                Preference preference = screen.findPreference(prefix + error.getName());

                if ( preference != null && error.getMessage() != null )
                {
                    ( (FormField) preference).setError(error.getMessage());
                    preference.setSummary(preference.getSummary());
                }
            }
        }
    }

    public void removeErrors(List<QuestionService.QuestionParams> questions) {
        if ( questions == null || questions.size() == 0 )
        {
            return;
        }

        PreferenceScreen screen = getPreferenceScreen();

        for (QuestionService.QuestionParams question:questions)
        {
            Preference preference = screen.findPreference(prefix + question.getName());

            if ( preference != null )
            {
                ( (FormField) preference).setError(null);
            }
        }
    }

    public void setPreferencePrefix(String prefix) {
        this.prefix = prefix;
    }

    public String  getPreferencePrefix() {
        return this.prefix;
    }

    public void setPresenter(com.golfingbuddy.ui.base.email_verification.Presenter presenter) {
        this.presenter = presenter;
    }

    public void getQuestionParams(String prefix) {
        this.prefix = prefix;
    }
}
