package com.golfingbuddy.ui.base.email_verification;

import android.app.Activity;

import com.golfingbuddy.R;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.model.base.classes.SkUser;
import com.golfingbuddy.ui.auth.AuthActivity;
import com.golfingbuddy.ui.base.email_verification.classes.OnValidationFinishListener;
import com.golfingbuddy.ui.base.email_verification.classes.ServerCallbackListener;
import com.golfingbuddy.ui.base.email_verification.service.VerificationServiceHelper;
import com.golfingbuddy.ui.search.service.QuestionService;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by jk on 2/18/16.
 */
public class PresenterImp implements Presenter {
    LinkedList<QuestionService.QuestionParams> questionList;
    SkApplication application;
    MainView mView;
    VerificationServiceHelper mHelper;
    int userId = 0;

    OnValidationFinishListener validationEndListener = new OnValidationFinishListener() {
        @Override
        public void onRemoveErrors(LinkedList<QuestionService.QuestionParams> list) {
            mView.removeErrors(list);
        }

        @Override
        public void onDrawErrors(Collection<QuestionService.QuestionValidationInfo> errors, int type) {
            mView.drawErrors(errors);

            switch (type) {
                case VerificationServiceHelper.VALIDATE_TYPE_SUBMIT:
                    mView.showMessage(application.getResources().getString(R.string.form_validation_error));
                    break;

                case VerificationServiceHelper.VALIDATE_TYPE_RESEND:
                    mView.showMessage(application.getResources().getString(R.string.form_validation_error));
                    break;
            }
        }
    };

    public PresenterImp( MainView view, SkApplication app ) {
        mView = view;
        mView.clean();

        userId = app.getUserInfo().getUserId();
        application = app;

        mHelper = new VerificationServiceHelper(app);
    }

    public LinkedList<QuestionService.QuestionParams> getQuestions() {
        return mHelper.getQuestionList();
    }

    @Override
    public void onDoneClicked(HashMap<String, String> data) {
        mHelper.validate(data, VerificationServiceHelper.VALIDATE_TYPE_SUBMIT, validationEndListener, new ServerCallbackListener() {
            @Override
            public void onSuccess() {
                mView.showMessage(application.getString(R.string.email_verification_success));
                mView.clean();

                SkUser userInfo = application.getUserInfo();
                userInfo.setIsEmailVerified(true);
                application.setUserInfo(userInfo);

                AuthActivity.successSignInAction((Activity)mView);
            }

            @Override
            public void onFaild() {
                mView.showMessage(application.getString(R.string.email_verification_failed));
            }

            @Override
            public void onError(Exception ex) {
                mView.showMessage(application.getString(R.string.email_verification_failed));
            }
        });
    }

    @Override
    public void onResendClicked(HashMap<String, String> data) {
        mHelper.validate(data, VerificationServiceHelper.VALIDATE_TYPE_RESEND, validationEndListener, new ServerCallbackListener() {
            @Override
            public void onSuccess() {
                mView.showMessage(application.getString(R.string.resend_email_verification_success));
            }

            @Override
            public void onFaild() {
                mView.showMessage(application.getString(R.string.resend_email_verification_failed));
            }

            @Override
            public void onError(Exception ex) {
                mView.showMessage(application.getString(R.string.resend_email_verification_failed));
            }
        });
    }

    @Override
    public void validateField(HashMap<String, String> data) {
        mHelper.validate(data, VerificationServiceHelper.VALIDATE_TYPE_FIELD, validationEndListener, new ServerCallbackListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFaild() {

            }

            @Override
            public void onError(Exception ex) {

            }
        });
    }
}
