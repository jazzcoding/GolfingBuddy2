package com.golfingbuddy.ui.join.presenter;

import android.app.Application;
import android.net.Uri;
import android.os.AsyncTask;

import com.golfingbuddy.R;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.join.classes.JoinUserData;
import com.golfingbuddy.ui.join.interfaces.OnJoinListener;
import com.golfingbuddy.ui.join.interfaces.OnLoadedListener;
import com.golfingbuddy.ui.join.interfaces.OnTmpAvatarUploaded;
import com.golfingbuddy.ui.join.interfaces.OnUserJoinListener;
import com.golfingbuddy.ui.join.interfaces.OnValidationEndListener;
import com.golfingbuddy.ui.join.interfaces.JoinView;
import com.golfingbuddy.ui.join.interfaces.Presenter;
import com.golfingbuddy.ui.join.interfaces.QuestionsModel;
import com.golfingbuddy.ui.join.interfaces.UserModel;
import com.golfingbuddy.ui.join.model.JoinQuestionsModel;
import com.golfingbuddy.ui.join.model.UserData;
import com.golfingbuddy.ui.search.classes.QuestionObject;
import com.golfingbuddy.ui.search.classes.SectionObject;
import com.golfingbuddy.ui.search.service.QuestionService;

import java.util.HashMap;
import java.util.LinkedList;


public class JoinStepManager implements Presenter, OnLoadedListener, OnUserJoinListener {
    private JoinQuestionsModel mQuestionsModel;
    private JoinView mViewModel;
    private UserModel mUserMode;
    private Application app;
    private BaseServiceHelper helper;

    public JoinStepManager(JoinView view, Application application) {
        helper = BaseServiceHelper.getInstance(application);

        mViewModel = view;
        mViewModel.clean();

        mUserMode = new UserData(application, helper);

        app = application;

        mQuestionsModel = new JoinQuestionsModel(helper, this);
        mQuestionsModel.loadData(mUserMode.getStep(), mUserMode.getGender());
    }

    // questions listener
    @Override
    public void onSuccess(QuestionsModel model) {
        if ( mUserMode == null || model == null || mViewModel == null )
        {
            return;
        }

        QuestionObject[] questions = model.getQuestions();
        SectionObject[] sections = model.getSections();

        HashMap<String, String> data = mUserMode.getData(mUserMode.getStep());
        
        if ( questions != null && data != null && data.size() > 0 )
        {
            for (QuestionObject question:questions)
            {
                if ( data.containsKey(question.getName()) ) {
                    question.setValue(data.get(question.getName()));
                }
            }
        }

        int nextButtonText = R.string.join_form_next_button_label;

        if ( mUserMode.getStep() == UserData.STEP_2 ) {
            nextButtonText = R.string.join_form_join_button_label;
        }

        mViewModel.changeNextButtonName(nextButtonText);
        mViewModel.drawQuestions(sections, questions);
        mViewModel.hideProgress();
    }

    @Override
    public void onError(QuestionsModel model, Exception ex) {

    }
    // question listener end

    // save user listener
    @Override
    public void onSaveSuccess(HashMap<String, String> data) {

    }

    @Override
    public void onSaveFaild(HashMap<String, String> data) {

    }

    // save user end

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        mViewModel = null;
        mUserMode = null;
        mQuestionsModel = null;

    }

    @Override
    public void onNextClicked(HashMap<String, String> data) {

        validate(data);
    }

    // Question data validation
    public void validate(final HashMap<String, String> data) {
        validate(data, true);
    }

    // Question data validation
    public void validate(final HashMap<String, String> data, final boolean validateAllQuestions) {

        if( mQuestionsModel.isLoadInProcess() )
        {
            return;
        }

        if ( app == null )
        {
            return;
        }

        if ( !validateAllQuestions && ( data == null || data.size() ==0 )  )
        {
            return;
        }

        // prepare data for validation
        LinkedList<QuestionService.QuestionParams> questionParams = new LinkedList<QuestionService.QuestionParams>();

        LinkedList<QuestionObject> list = new LinkedList<QuestionObject>();


        for (QuestionObject question:mQuestionsModel.getQuestions())
        {
            if ( question == null )
            {
                continue;
            }

            // validate only questions existing in data HashMap
            if ( !validateAllQuestions && !data.containsKey(question.getName()) ) {
                continue;
            }

            // prepare value
            String value = null;
            if( data.containsKey(question.getName()) )
            {
                value = data.get(question.getName());
            }

            // set value
            QuestionService.QuestionParams params = new QuestionService.QuestionParams(question);
            params.setValue(value);

            questionParams.add(params);
            list.add(question);
        }

        mViewModel.removeErrors(list);

        final BaseServiceHelper helper = BaseServiceHelper.getInstance(app);

        // validate questions
        QuestionService.getInstance().validate(app.getApplicationContext(), helper, questionParams, new OnValidationEndListener() {
            @Override
            public void onValidationEnd(HashMap<String, QuestionService.QuestionValidationInfo> validationInfo) {

                // draw errors if validation faild
                if (validationInfo != null && validationInfo.size() > 0) {
                    boolean isValid = true;

                    for (QuestionService.QuestionValidationInfo info : validationInfo.values()) {
                        if (!info.getIsValid()) {
                            isValid = false;
                            break;
                        }
                    }

                    if (!isValid) {
                        mViewModel.drawErrors(validationInfo.values());

                        if ( validateAllQuestions )
                        {
                            mViewModel.showError(app.getResources().getString(R.string.form_validation_error));
                        }
                        return;
                    }
                }

                if (validateAllQuestions) {
                    switch (mUserMode.getStep()) {
                        case UserData.STEP_1:

                            mUserMode.setData(data);
                            mUserMode.setStep(UserData.STEP_2);

                            mViewModel.showProgress();
                            mViewModel.clean();

                            // hack to avoid java.util.ConcurrentModificationException from BaseServiceHelper
                            new LoadData().execute(UserData.STEP_2);

                            break;

                        case UserData.STEP_2:

                            mUserMode.setData(data);

                            mViewModel.showProgress();
                            mViewModel.clean();

                            mUserMode.save(new OnJoinListener() {
                                @Override
                                public void onSuccess(JoinUserData data) {
                                    mViewModel.showMessage(app.getResources().getString(R.string.user_join_succsess));

                                    HashMap<String, String> firstStepData = mUserMode.getData(UserData.STEP_1);

                                    String[] params = new String[2];
                                    params[0] = firstStepData.get("username");
                                    params[1] = firstStepData.get("password");

                                    mUserMode.clean();
                                    mViewModel.clean();

                                    Login login = new Login();
                                    login.execute(params);
                                }

                                @Override
                                public void onFailed(JoinUserData data) {
                                    mViewModel.showError(app.getResources().getString(R.string.user_join_error));
                                    mUserMode.clean();
                                    mViewModel.clean();
                                    mViewModel.close();
                                }

                                @Override
                                public void onError(Exception ex) {
                                    mViewModel.showError(app.getResources().getString(R.string.user_join_error));
                                    mUserMode.clean();
                                    mViewModel.clean();
                                    mViewModel.close();
                                }
                            });

                            break;
                    }
                }
            }

            @Override
            public void onError(Exception ex) {
                //mViewModel.showError(app.getResources().getString(R.string.form_validation_error));
            }
        });
    }

    @Override
    public void onBackClicked(HashMap<String, String> data) {

        if( mQuestionsModel.isLoadInProcess() )
        {
            return;
        }

        switch(mUserMode.getStep()) {
            case UserData.STEP_1:

                mUserMode.setData(data);
                mUserMode.setStep(UserData.STEP_1);

                mViewModel.showProgress();
                mViewModel.clean();
                mViewModel.close();

                break;

            case UserData.STEP_2:

                mUserMode.setData(data);
                mUserMode.setStep(UserData.STEP_1);

                mViewModel.showProgress();
                mViewModel.clean();

                new LoadData().execute(UserData.STEP_1);

                break;
        }
    }

    @Override
    public void uploadTmpAvatar(Uri uri, OnTmpAvatarUploaded listener) {
        mUserMode.saveTmpAvatar(uri, listener);
    }

    private class LoadData extends AsyncTask<Integer, Integer, Integer> {

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Integer doInBackground(Integer... params) {
            if ( params != null && params.length != 0 && params[0] != null )
            {
                return params[0];
            }

            return null;
        }

        protected void onPostExecute(Integer step) {
            if ( step == UserData.STEP_2 || step == UserData.STEP_1 ) {
                mQuestionsModel.loadData(step, mUserMode.getGender());
            }
        }
    }

    private class Login extends AsyncTask<String, HashMap<String, String>, HashMap<String, String>> {
        @Override
        protected HashMap<String, String> doInBackground(String... params) {

            HashMap<String, String> data = new HashMap<String, String>();

            if ( params != null && params.length >= 2 && params[0] != null && params[1] != null )
            {
                data.put("username", params[0]);
                data.put("password", params[1]);
            }

            return data;
        }

        protected void onPostExecute(HashMap<String, String> params) {

            if ( params.containsKey("username") && params.containsKey("password") ) {
                String username = params.get("username");
                String password = params.get("password");
                mViewModel.login(username, password);
            }
        }
    }
}
