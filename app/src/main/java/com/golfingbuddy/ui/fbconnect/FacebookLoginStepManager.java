package com.golfingbuddy.ui.fbconnect;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.ProfilePictureView;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkBaseActivity;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.ui.fbconnect.classes.FacebookConnectRestRequestCommand;
import com.golfingbuddy.ui.fbconnect.fragments.FacebookLoginQuestions;
import com.golfingbuddy.ui.fbconnect.service.FacebookConnectService;
import com.golfingbuddy.ui.search.classes.BasicQuestionsJsonParser;
import com.golfingbuddy.ui.search.service.QuestionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

public class FacebookLoginStepManager extends SkBaseActivity implements View.OnClickListener {

    private enum STEP {
        FIRST, SECOND, TRY_LOGIN
    }

    public static final String PREFIX = "fbconnect";

    private HashMap<String, String> userData;
    private STEP mStep;
    private LinkedHashMap<String, String> mCategory;
    private LinkedHashMap<String, ArrayList<QuestionService.QuestionParams>> questions;

    private QuestionService questionService;
    FacebookConnectService serviceHelper;
    private SkServiceCallbackListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, false);
        setContentView(R.layout.fbconnect_step);

        QuestionService.getInstance().removeSharedPreferences(this, PREFIX);
        QuestionService.getInstance().removeSharedPreferences(this, "search");
        QuestionService.getInstance().removeSharedPreferences(this, "edit");
        Intent intent = getIntent();

        userData = new HashMap<>();
        userData.put("facebookId", intent.getStringExtra("facebookId"));
        userData.put("name", intent.getStringExtra("name"));
        userData.put("gender", intent.getStringExtra("gender"));
        userData.put("birthday", intent.getStringExtra("birthday"));
        userData.put("email", intent.getStringExtra("email"));

        serviceHelper = new FacebookConnectService(getApp());
        questionService = QuestionService.getInstance();
        mCategory = new LinkedHashMap<>();
        questions = new LinkedHashMap<>();

        listener = new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                FacebookConnectRestRequestCommand.Command command = (FacebookConnectRestRequestCommand.Command) data.getSerializable("command");

                String jsonString = data.getString("data");
                JsonParser jsonParser = new JsonParser();
                JsonObject json = jsonParser.parse(jsonString).getAsJsonObject().getAsJsonObject("data");
                questions.clear();

                switch (command) {
//                    case TRY_LOGIN:
//                        if (json.has("loggedIn") && json.get("loggedIn").getAsBoolean() == true) {
//                            Intent loggedIntent = new Intent();
//                            loggedIntent.putExtra("userData", jsonString);
//                            setResult(200);
//                            finish();
//                        } else {
//                            changeStep(STEP.FIRST);
//                        }
//
//                        return;
                    case FIRST_STEP:
                        questions.putAll(parse(json));
                        HashMap<String,Object> map = new HashMap();
                        map.put("name", "terms");
                        map.put("label", getResources().getString(R.string.fb_terms_1));
                        map.put("presentation", "checkbox");

                        HashMap<String,String> options = new HashMap<>();
                        options.put("value", "1");
                        options.put("label", "fb terms label");
                        ArrayList<HashMap<String,String>> o = new ArrayList<>();
                        o.add(options);
                        map.put("options", o);
                        map.put("required", "1");
                        questions.get("questions").add(new QuestionService.QuestionParams(map));
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        FacebookLoginQuestions qustionsFragment = new FacebookLoginQuestions();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("questions", questions);
                        bundle.putString("gender", userData.get("gender"));
                        qustionsFragment.setArguments(bundle);

                        fragmentTransaction.replace(R.id.fbconnect_first_step_content, qustionsFragment, "first");
                        fragmentTransaction.commit();
                        break;

                    case SECOND_STEP:
                        questions.putAll(parse(json));
                        FragmentTransaction stepTFragmentTransaction = getFragmentManager().beginTransaction();
                        FacebookLoginQuestions secondFragment = new FacebookLoginQuestions();
                        Bundle stepTwoBundle = new Bundle();
                        stepTwoBundle.putSerializable("questions", questions);
                        stepTwoBundle.putSerializable("category", mCategory);
                        stepTwoBundle.putString("name", userData.get("name"));

                        secondFragment.setArguments(stepTwoBundle);
                        stepTFragmentTransaction.replace(R.id.fbconnect_second_step_content, secondFragment, "second");
                        stepTFragmentTransaction.commit();
                        break;
                    case SAVE:
                        Boolean success = json.get("success").getAsBoolean();

                        if (success != null && success != true) {
                            int code = json.get("code").getAsInt();

                            switch (code) {
                                case -5:
                                    Toast.makeText(FacebookLoginStepManager.this, getResources().getString(R.string.fbconnect_error_email_duplicate), Toast.LENGTH_LONG).show();
                                    break;
                                case -4:
                                    Toast.makeText(FacebookLoginStepManager.this, getResources().getString(R.string.fbconnect_error_username), Toast.LENGTH_LONG).show();
                                    break;
                                case -2:
                                    Toast.makeText(FacebookLoginStepManager.this, getResources().getString(R.string.fbconnect_error_email), Toast.LENGTH_LONG).show();
                                    break;
                            }

                            changeStep(STEP.SECOND);
                        } else {
                            FragmentManager doneFragmentManager = getFragmentManager();
                            FragmentTransaction doneFragmentTransaction = doneFragmentManager
                                    .beginTransaction();

                            Fragment first = getFragment("first");

                            if (first != null) {
                                doneFragmentTransaction.detach(first);
                            }

                            Fragment second = getFragment("second");

                            if (second != null) {
                                doneFragmentTransaction.detach(second);
                            }

                            doneFragmentTransaction.commit();

                            Intent loggedIntent = new Intent();
                            loggedIntent.putExtra("userData", data);
                            setResult(200, loggedIntent);
                            finish();
                        }

                        return;
                }

                show(mStep);
            }
        };

        if (mStep == null) { // is first mStep
            changeStep(STEP.FIRST);
//            mStep = STEP.FIRST;
//            serviceHelper.getFirstStepQuestionList(listener);
        }
        else {
            if (!isValid()) {
                return;
            }

            changeStep(STEP.SECOND);
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.fbconnect_goto_step_two).setOnClickListener(this);
        findViewById(R.id.fbconnect_done).setOnClickListener(this);
    }

    private void changeStep(STEP step) {
        mStep = step;

        switch (step) {
            case SECOND:
                setTitle(R.string.fbconnect_second_step_title);
                HashMap<String, String> data = getDate();
                serviceHelper.getSecondStepQuestionList(data.get("sex"), userData.get("email"), listener);
                break;
            case FIRST:
                setTitle(R.string.fbconnect_first_step_title);
                ProfilePictureView imageView = (ProfilePictureView) findViewById(R.id.fbconnect_avatar);
                imageView.setProfileId(userData.get("facebookId"));
                TextView textView = (TextView) findViewById(R.id.fbconnect_display_name);
                textView.setText(userData.get("name"));
                serviceHelper.getFirstStepQuestionList(listener);
                break;
        }

        hide();
    }

    private void prepareChangeStep() {
        switch (mStep) {
            case FIRST:
                hide();
                setResult(500);
                finish();
                break;
            case SECOND:
                changeStep(STEP.FIRST);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        prepareChangeStep();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            prepareChangeStep();
        }

        return super.onMenuItemSelected(featureId, item);
    }

    private Boolean isValid() {
        HashMap<String, String> data = getDate();

        Set<String> keys = questions.keySet();
        Boolean isShow = false;
        Boolean result = true;

        PreferenceFragment preferenceFragment;

        switch (mStep) {
            case SECOND:
                preferenceFragment = ((PreferenceFragment) getFragment("second"));
                break;
            case FIRST:
            default:
                preferenceFragment = ((PreferenceFragment) getFragment("first"));
                break;
        }

        for (String key : keys) {
            for (QuestionService.QuestionParams question : questions.get(key)) {
                String value = data.get(question.getName());
                Preference preference;

                if (TextUtils.equals(question.getName(), "googlemap_location")) {
                    preference = preferenceFragment.getPreferenceScreen().findPreference(PREFIX + "custom_location");
                    value = data.get("custom_location");
                } else {
                    preference = preferenceFragment.getPreferenceScreen().findPreference(PREFIX + question.getName());
                }

                if ( preference == null )
                {
                    continue;
                }

                preference.setIcon(new ColorDrawable(Color.TRANSPARENT));

                if (preference.getKey().equals(PREFIX + "terms") && !((CheckBoxPreference) preference).isChecked()) {
                    if (!isShow) {
                        Toast.makeText(this, getResources().getString(R.string.fb_terms_error_msg), Toast.LENGTH_LONG).show();
                        isShow = true;
                    }
                    preference.setIcon(null);
                    preference.setIcon(R.drawable.atentions);

                    result = false;
                } else if (question.isRequired() && (value == null || value.trim().length() == 0 || value.trim().equals("0"))) {
                    if (!isShow) {
                        Toast.makeText(this, "\"" + question.getLabel() + "\"" + getText(R.string.is_required), Toast.LENGTH_LONG).show();
                        isShow = true;
                    }

                    preference.setIcon(null);
                    preference.setIcon(R.drawable.atentions);

                    result = false;
                } else if (question.getName().equals("email") && !Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches()) {
                    Toast.makeText(this, "Email is valid", Toast.LENGTH_LONG).show();
                    preference.setIcon(null);
                    preference.setIcon(R.drawable.atentions);

                    result = false;
                }
            }
        }

        return result;
    }

    private Fragment getFragment(String tag) {
        return getFragmentManager().findFragmentByTag(tag);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.terms_agree) {
            PreferenceFragment preferenceFragment = ((PreferenceFragment) getFragment("first"));
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preferenceFragment.findPreference(PREFIX + "terms");
            checkBoxPreference.setIcon(null);
            checkBoxPreference.setIcon(new ColorDrawable(Color.TRANSPARENT));
            checkBoxPreference.setChecked(true);

            Intent in = new Intent(FacebookLoginStepManager.this, TermsOfUseActivity.class);
            startActivity(in);
        }
        else
        {
        if (!isValid()) {
            return;
        }

        switch (mStep) {
            case SECOND:
                join();
                break;
            case FIRST:
            default:
                changeStep(STEP.SECOND);
                break;
        }
    }
    }

    private HashMap<String, String> getDate() {
        return questionService.getData(this, PREFIX);
    }

    private void join() {
        HashMap<String, String> data = getDate();
        data.put("facebookId", userData.get("facebookId"));
        data.put("realname", userData.get("name"));

        if (userData.containsKey("email") && userData.get("email") != null) {
            data.put("email", userData.get("email"));
        }

        serviceHelper.save(data, listener);
        hide();
    }

    private HashMap<String, ArrayList<QuestionService.QuestionParams>> parse(JsonObject json) {
//        JsonObject jsonUserList = json.get("list");

//        if (jsonUserList == null) {
//            return null;
//        }

        if (json.has("category")) {
            for (JsonElement jsonElement: json.getAsJsonArray("category")) {
                JsonObject jsonObject = (JsonObject)jsonElement;

                if (mCategory.containsKey(jsonObject.get("category").getAsString())) {
                    continue;
                }

                mCategory.put(jsonObject.get("category").getAsString(), jsonObject.get("label").getAsString());
            }
        }

        LinkedHashMap<String, ArrayList<QuestionService.QuestionParams>> speedmatcheses = new LinkedHashMap<>();

        BasicQuestionsJsonParser parcer = new BasicQuestionsJsonParser();

        HashMap<String, Object> data = parcer.parse(json);
        ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>)data.get("questions");

        if (list != null && !list.isEmpty()) {
            for (HashMap map : list) {
                String category;

                if (mStep == STEP.SECOND) {
                    category = mCategory.get(map.get("sectionName"));

                    if (!speedmatcheses.containsKey(category)) {
                        speedmatcheses.put(category, new ArrayList<QuestionService.QuestionParams>());
                    }
                }
                else
                {
                    category = "questions";

                    if (!speedmatcheses.containsKey(category)) {
                        speedmatcheses.put(category, new ArrayList<QuestionService.QuestionParams>());
                    }
                }

                QuestionService.QuestionParams questionParams = new QuestionService.QuestionParams(map);
                speedmatcheses.get(category).add(questionParams);
            }
        }

        return speedmatcheses;
    }

    public void show(STEP step) {
        int duration = getResources().getInteger(R.integer.matches_duration);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.fbconnect_progressBar);
        progressBar.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBar.setVisibility(View.GONE);
                    }
                });

        LinearLayout linearLayout;

        switch (step) {
            case SECOND:
                linearLayout = (LinearLayout) findViewById(R.id.fbconnect_second_step);
                break;
            case FIRST:
            default:
                linearLayout = (LinearLayout) findViewById(R.id.fbconnect_first_step);
                break;

        }

        linearLayout.setAlpha(0f);
        linearLayout.setVisibility(View.VISIBLE);
        linearLayout.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);
    }

    public void hide() {
        int duration = getResources().getInteger(R.integer.matches_duration);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.fbconnect_progressBar);
        progressBar.setAlpha(0f);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);

        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.fbconnect_first_step);
        linearLayout.setVisibility(View.INVISIBLE);

        final LinearLayout linearLayout1 = (LinearLayout) findViewById(R.id.fbconnect_second_step);
        linearLayout1.setVisibility(View.INVISIBLE);

        Fragment fragment = getFragmentManager().findFragmentByTag("first");

        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }

        Fragment second = getFragmentManager().findFragmentByTag("second");

        if (second != null) {
            getFragmentManager().beginTransaction().remove(second).commit();
        }
    }
}
