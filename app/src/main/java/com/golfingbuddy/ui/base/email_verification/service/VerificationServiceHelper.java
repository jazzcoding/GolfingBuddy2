package com.golfingbuddy.ui.base.email_verification.service;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.base.email_verification.classes.Data;
import com.golfingbuddy.ui.base.email_verification.classes.OnValidationFinishListener;
import com.golfingbuddy.ui.base.email_verification.classes.ServerCallbackListener;
import com.golfingbuddy.ui.join.interfaces.OnValidationEndListener;
import com.golfingbuddy.ui.search.service.QuestionService;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by jk on 2/19/16.
 */
public class VerificationServiceHelper {

    boolean isValidationStart = false;

    public final static int VALIDATE_TYPE_FIELD = 0;
    public final static int VALIDATE_TYPE_SUBMIT = 1;
    public final static int VALIDATE_TYPE_RESEND= 2;

    LinkedList<QuestionService.QuestionParams> questionList;
    SkApplication application;

    public VerificationServiceHelper(SkApplication application) {

        this.application = application;

        questionList = new LinkedList<QuestionService.QuestionParams>();

        QuestionService.QuestionParams email = new QuestionService.QuestionParams();
        email.setName("verifyEmail");
        email.setPresentation(QuestionService.getInstance().QUESTION_PRESENTATION_TEXT);
        email.setLabel(application.getApplicationContext().getResources().getString(R.string.your_email_label));
        email.setRequired(true);

        if ( application.getUserInfo() != null ) {
            email.setValue(application.getUserInfo().getEmail());
        }

        questionList.add(email);

        QuestionService.QuestionParams verifyCode = new QuestionService.QuestionParams();
        verifyCode.setName("verifyEmailCode");
        verifyCode.setPresentation(QuestionService.getInstance().QUESTION_PRESENTATION_TEXT);
        verifyCode.setLabel(application.getApplicationContext().getResources().getString(R.string.verification_code_field_label));
        verifyCode.setRequired(true);

        questionList.add(verifyCode);
    }

    public LinkedList<QuestionService.QuestionParams> getQuestionList() {
        return questionList;
    }

    public LinkedList<QuestionService.QuestionParams> prepareValidationData(HashMap<String, String> data, int type) {
        // prepare data for validation
        LinkedList<QuestionService.QuestionParams> list = new LinkedList<QuestionService.QuestionParams>();

        for (QuestionService.QuestionParams question:questionList)
        {
            if ( question == null )
            {
                continue;
            }

            switch (type)
            {
                case VALIDATE_TYPE_SUBMIT:
                    if ( !"verifyEmailCode".equals(question.getName()) ) {
                        continue;
                    }
                    break;
                case VALIDATE_TYPE_RESEND:
                    if ( !"verifyEmail".equals(question.getName()) ) {
                        continue;
                    }

                case VALIDATE_TYPE_FIELD:
                    // validate only questions existing in data HashMap
                    if ( !data.containsKey(question.getName()) ) {
                        continue;
                    }
                    break;

            }

            // prepare value
            String value = null;
            if( data.containsKey(question.getName()) )
            {
                value = data.get(question.getName());
            }

            // set value
            question.setValue(value);

            list.add(question);
        }

        return list;
    }

    public void verifyEmail(HashMap<String, String> data, ServerCallbackListener listener) {
        verifyTask task = new verifyTask(data, listener);
        task.execute();
    }

    public void resendEmail(String email, String userId, final ServerCallbackListener listener) {
        resendTask task = new resendTask(email, userId, listener);
        task.execute();
    }

    public void validate(final HashMap<String, String> data, final int type, final OnValidationFinishListener validationListener, final ServerCallbackListener callbackListener ) {

        if ( isValidationStart )
        {
            return;
        }

        if ( application == null )
        {
            return;
        }

        if ( type == VALIDATE_TYPE_SUBMIT && ( data == null || data.size() ==0 )  )
        {
            return;
        }

        isValidationStart = true;

        LinkedList<QuestionService.QuestionParams> list = prepareValidationData(data, type);

        validationListener.onRemoveErrors(list);

        BaseServiceHelper helper = BaseServiceHelper.getInstance(application);

        // validate questions
        QuestionService.getInstance().validate(application.getApplicationContext(), helper, list, new OnValidationEndListener() {
            @Override
            public void onValidationEnd(HashMap<String, QuestionService.QuestionValidationInfo> validationInfo) {
                isValidationStart = false;

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

                        validationListener.onDrawErrors(validationInfo.values(), type);

                        return;
                    }
                }

                switch (type) {
                    case VALIDATE_TYPE_SUBMIT:
                        verifyTask task = new verifyTask(data, callbackListener);
                        task.execute();
                        break;

                    case VALIDATE_TYPE_RESEND:

                        if (!data.containsKey("verifyEmail")) {
                            return;
                        }

                        if (application == null || application.getUserInfo() == null || application.getUserInfo().getUserId() == 0) {
                            return;
                        }

                        resendTask task1 = new resendTask(data.get("verifyEmail"), String.valueOf(application.getUserInfo().getUserId()), callbackListener);
                        task1.execute();
                        break;
                }
            }

            @Override
            public void onError(Exception ex) {
                isValidationStart = false;
                //mViewModel.showError(app.getResources().getString(R.string.form_validation_error));
            }
        });
    }

    private void verifyEmailCommand(HashMap<String, String> data, final ServerCallbackListener listener) {
        BaseServiceHelper helper = BaseServiceHelper.getInstance(application);

        helper.runRestRequest(BaseRestCommand.ACTION_TYPE.VERIFY_EMAIL, data, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int rId, Intent requestIntent, int resultCode, Bundle bundle) {
                Data validationData = new Data();

                try {
                    String result = bundle.getString("data");

                    if (result != null) {
                        JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                        JsonElement listData = dataObject.get("data");
                        JsonElement type = dataObject.get("type");

                        if (listData != null && listData.isJsonObject() && type != null && "success".equals(type.getAsString())) {
                            JsonObject data = listData.getAsJsonObject();

                            Gson g = new GsonBuilder().create();
                            validationData = g.fromJson(data, Data.class);

                            if (validationData != null && validationData.isValid() ) {
                                listener.onSuccess();

                            } else {
                                listener.onFaild();

                            }
                        }
                    }
                } catch (Exception ex) {
                    listener.onError(ex);
                    Log.i("email_verification", ex.getMessage(), ex);
                }
            }
        });
    }

    private void resendCodeCommand(String email, String userId, final ServerCallbackListener listener) {
        BaseServiceHelper helper = BaseServiceHelper.getInstance(application);

        if ( userId == null || userId.trim().isEmpty() || email == null || email.isEmpty() )
        {
            return;
        }

        HashMap<String, String> data = new HashMap<String, String>();
        data.put("userId", userId );
        data.put("email", email);

        helper.runRestRequest(BaseRestCommand.ACTION_TYPE.RESEND_VERIFY_EMAIL, data, new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int rId, Intent requestIntent, int resultCode, Bundle bundle) {
                Data validationData = new Data();

                try {
                    String result = bundle.getString("data");

                    if (result != null) {
                        JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                        JsonElement listData = dataObject.get("data");
                        JsonElement type = dataObject.get("type");

                        if (listData != null && listData.isJsonObject() && type != null && "success".equals(type.getAsString())) {
                            JsonObject data = listData.getAsJsonObject();

                            Gson g = new GsonBuilder().create();
                            validationData = g.fromJson(data, Data.class);

                            if ( validationData != null && validationData.isValid() ) {
                                listener.onSuccess();
                            } else {
                                listener.onFaild();
                            }
                        }
                    }
                } catch (Exception ex) {
                    listener.onError(ex);
                    Log.i("email_verification", ex.getMessage(), ex);
                }
            }
        });
    }

    class verifyTask extends AsyncTask<String, String, String> {

        HashMap<String, String> data = new HashMap<String, String>();
        ServerCallbackListener listener;

        public verifyTask(HashMap<String, String> data, final ServerCallbackListener listener) {
            this.data = data;
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            VerificationServiceHelper.this.verifyEmailCommand(data, listener);
        }
    }

    class resendTask extends AsyncTask<String, String, String> {

        String email;
        String userId;
        ServerCallbackListener listener;

        public resendTask(String email, String userId, final ServerCallbackListener listener) {
            this.email = email;
            this.userId = userId;
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            VerificationServiceHelper.this.resendCodeCommand(email, userId, listener);
        }
    }
}
