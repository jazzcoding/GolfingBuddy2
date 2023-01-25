package com.golfingbuddy.ui.join.view;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkBaseActivity;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.join.interfaces.JoinView;
import com.golfingbuddy.ui.join.presenter.JoinStepManager;
import com.golfingbuddy.ui.search.classes.QuestionObject;
import com.golfingbuddy.ui.search.classes.SectionObject;
import com.golfingbuddy.ui.search.service.QuestionService;
import com.golfingbuddy.utils.SkApi;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static com.golfingbuddy.ui.auth.AuthActivity.successSignInAction;

public class JoinForm extends SkBaseActivity implements JoinView, View.OnClickListener {
    public static String PREFIX = "join";
    protected StepFragment mFragment;
    protected JoinStepManager mPresenter;
    private LinearLayout nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_activity);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        mFragment = StepFragment.newInstance(PREFIX);
        mFragment.setPreferencePrefix(PREFIX);

        fragmentTransaction.replace(R.id.questions, mFragment);
        fragmentTransaction.commit();

        mPresenter = new JoinStepManager(this, this.getApplication());

        mFragment.setPresenter(mPresenter);

        nextButton = (LinearLayout) findViewById(R.id.join_next_step);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JoinForm.this.mPresenter.onNextClicked(getData());
                //Crop.pickImage(JoinForm.this);
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private HashMap<String, String> getData() {
        return QuestionService.getInstance().getData(JoinForm.this, JoinForm.this.PREFIX);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home || id == android.R.id.home || id == R.id.homeAsUp) {
            mPresenter.onBackClicked(getData());
            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void clean() {
        QuestionService.getInstance().removeSharedPreferences(this, PREFIX);
        QuestionService.getInstance().removeSharedPreferences(this, "search");
        QuestionService.getInstance().removeSharedPreferences(this, "edit");
    }

    @Override
    public void showProgress() {
        if ( mFragment != null && mFragment.isAdded() ) {
            mFragment.hide();
            nextButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideProgress() {
        if ( mFragment != null && mFragment.isAdded() ) {
            mFragment.show();
            nextButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void drawQuestions(SectionObject[] sections, QuestionObject[] questions) {
        if ( mFragment != null && mFragment.isAdded() )
        {
            mFragment.drawQuestions(sections, questions);
        }
    }

    @Override
    public void removeErrors(List<QuestionObject> questions) {
        mFragment.removeErrors(questions);
    }

    @Override
    public void changeNextButtonName(int resorce) {
        TextView nextStepText = (TextView) findViewById(R.id.join_next_step_text);
        if ( nextStepText != null && resorce != 0 )
        {
            nextStepText.setText(resorce);
        }
    }

    @Override
    public void drawErrors(Collection<QuestionService.QuestionValidationInfo> errors) {
        mFragment.drawErrors(errors);
    }

    @Override
    protected void onResume() {
        mPresenter.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public void showMessage(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showError(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        mFragment.onClick(v);
    }

    //TODO remove this method and use auth service
    public void login(String username, String password) {

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", username);
        params.put("password", password);

        //need to remove auth token
        SkApplication.setAuthToken(null);

        BaseServiceHelper helper = BaseServiceHelper.getInstance(getApplication());
        helper.runRestRequest(BaseRestCommand.ACTION_TYPE.AUTHENTICATE, params, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                JsonObject resultData = SkApi.processResult(data);
                if (resultData != null) {
                    if (SkApi.propExistsAndNotNull(resultData, "token")) {
                        SkApplication.saveOrUpdateSiteInfo(data, (SkApplication) getApplication());
                        successSignInAction(JoinForm.this);
                    }
                } else {
                    String result = data.getString("data");
                    if (result != null) {
                        resultData = new Gson().fromJson(result, JsonObject.class);
                        if (SkApi.propExistsAndNotNull(resultData, "data")) {
                            resultData = resultData.getAsJsonObject("data");
                            if (resultData.has("exception")) {
                                if (SkApi.propExistsAndNotNull(resultData, "userData")) {
                                    JsonObject temp = resultData.getAsJsonObject("userData");

                                    if (SkApi.propExistsAndNotNull(temp, "message")) {
                                        Toast.makeText(JoinForm.this, temp.get("message").getAsString(), Toast.LENGTH_LONG).show();
                                    }

                                    JoinForm.this.finish();
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}
